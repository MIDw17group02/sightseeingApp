package com.example.de.testssapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import model.DataModel;
import network.POIFetcher;
import network.WatchNotifier;

public class ConfigurationActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    //Log-Tag
    public static String TAG = "Phone-Configuration";

    private Button selectPOIs;
    private Switch switchRound;
    ProgressDialog progressDialog;
    private Spinner distanceSpinner;
    private Spinner durationSpinner;
    private FusedLocationProviderClient mFusedLocationClient;
    final int location_permission_request = 1;
    private DataModel model;
    private Location currentLocation = null;

    //watchID for watchConnectin
    private GoogleApiClient mGoogleApiClient;
    private String watchId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        model = DataModel.getInstance();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request Permissions from User
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, location_permission_request);
        }

        setTitle(getString(R.string.title_activity_configuration));

        progressDialog = new ProgressDialog(this);

        switchRound = (Switch) findViewById(R.id.switch_roundtour);
        switchRound.setText(getString(R.string.roundOff));
        switchRound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                switchRound.setText(checked ? getString(R.string.roundOn) : getString(R.string.roundOff));
            }
        });

        distanceSpinner = (Spinner) findViewById(R.id.spinner_distance);
        final ArrayAdapter<CharSequence> sp_adapter_1 = ArrayAdapter.createFromResource(this, R.array.distance_array, R.layout.double_spinner_item);
        sp_adapter_1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(sp_adapter_1);
        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String distance_str = (String) adapterView.getItemAtPosition(i);
                distance_str = distance_str.replace(" km", "");
                model.getTourConfiguration().setDistance(Double.valueOf(distance_str));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        distanceSpinner.setSelection(3);

        durationSpinner = (Spinner) findViewById(R.id.spinner_duration);
        ArrayAdapter<CharSequence> sp_adapter_2 = ArrayAdapter.createFromResource(this, R.array.duration_array, R.layout.double_spinner_item);
        sp_adapter_2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(sp_adapter_2);
        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String duration_str = (String) adapterView.getItemAtPosition(i);
                duration_str = duration_str.replace(" h", "");
                model.getTourConfiguration().setDistance(Double.valueOf(duration_str));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        durationSpinner.setSelection(1);

        selectPOIs = (Button) findViewById(R.id.continueSelectionButton);
        selectPOIs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int radius;
                double distance = model.getTourConfiguration().getDistance(); // Distance is in km => *1000
                if (switchRound.isActivated()) {
                    radius = (int) (distance * 1000.0 / 2.0);
                } else {
                    radius = (int) (distance * 1000.0);
                }

                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(getString(R.string.loading_pois));
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Run network fetch in background.
                new Thread() {
                    public void run() {
                        if (model.getLastLocation() != null) {
                            POIFetcher.requestPOIs(getApplicationContext(), radius);
                        } else {
                            POIFetcher.requestPOIs(getApplicationContext(),
                                    52.3758916,
                                    9.7320104,
                                    2000);
                        }
                        progressDialog.dismiss();
                        Intent i = new Intent(getApplicationContext(), POISelectorActivity.class);
                        startActivity(i);
                    }
                }.start();
            }
        });

        //GoogleApiClient hinzufügen
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        model.setGoogleApiClient(mGoogleApiClient);
        //Node-ID der Uhr suchen (momentan basierend auf dem Namen)
        Log.d(TAG, "Searching for connected Devices ...");
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {

                for (Node node : getConnectedNodesResult.getNodes()) {
                    Log.d(TAG, "ConnectedDevice '"+node.getDisplayName()+"', NodeId = "+node.getId());

                    if( node.getDisplayName().equalsIgnoreCase("Moto 360 22P4")){
                        watchId = node.getId();
                        Log.d(TAG,"Watch found and assigned! ("+node.getId()+")");
                        //add GoogleClient and WatchID to WatchNotifier
                        WatchNotifier.setGoogleApiClient(mGoogleApiClient);
                        WatchNotifier.setWatchId(watchId);
                    }
                }
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        new GetCurrentLocationTask().execute();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case location_permission_request:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permissions were granted continue with the app.
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, model);
                    new GetCurrentLocationTask().execute();
                } else {
                    // Permissions were denied. Show dialog and close app.
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle(getString(R.string.error_dialog_title));
                    builder.setMessage(getString(R.string.dialog_no_permissions));
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Finish the App
                            finishAndRemoveTask();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private class GetCurrentLocationTask extends AsyncTask<Void, Void, Location> {

        @SuppressLint("MissingPermission")
        @Override
        protected Location doInBackground(Void... voids) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(ConfigurationActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                currentLocation = location;
                            }
                        }
                    });
            return currentLocation;
        }

        @Override
        protected void onPostExecute(Location location) {
            if (location != null)
                model.setLastKnownLocation(location);
            else
                new GetCurrentLocationTask().execute();
        }
    }
}
