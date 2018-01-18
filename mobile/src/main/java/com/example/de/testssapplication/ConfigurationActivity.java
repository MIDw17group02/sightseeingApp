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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import model.DataModel;
import model.TourConfiguration;
import network.POIFetcher;
import network.WatchNotifier;

public class ConfigurationActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    //Log-Tag
    public static String TAG = "Phone-Configuration";

    private final int GPS_UPDATE_MIN_MILLIS = 10000;
    private final int GPS_UPDATE_MIN_METERS = 20;

    private FloatingActionButton nextButton;
    private Switch switchRound;
    ProgressDialog progressDialog;
    private Spinner tempoSpinner;
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
        setContentView(R.layout.activity_configuration2);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        model = DataModel.getInstance();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request Permissions from User
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, location_permission_request);
        } else {
            setUpGPS();
        }

        setTitle(getString(R.string.title_activity_configuration));

        progressDialog = new ProgressDialog(this);

        switchRound = (Switch) findViewById(R.id.switch_roundtour);
        switchRound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                model.getTourConfiguration().setRoundTour(checked);
                //switchRound.setText(checked ? getString(R.string.roundOn) : getString(R.string.roundOff));
            }
        });

        tempoSpinner = (Spinner) findViewById(R.id.spinner_tempo);
        final ArrayAdapter<CharSequence> sp_adapter_1 = ArrayAdapter.createFromResource(this, R.array.tempo_array, R.layout.double_spinner_item);
        sp_adapter_1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tempoSpinner.setAdapter(sp_adapter_1);
        tempoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                model.getTourConfiguration().setTempo(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        tempoSpinner.setSelection(1);

        durationSpinner = (Spinner) findViewById(R.id.spinner_duration);
        ArrayAdapter<CharSequence> sp_adapter_2 = ArrayAdapter.createFromResource(this, R.array.duration_array, R.layout.double_spinner_item);
        sp_adapter_2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(sp_adapter_2);
        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String duration_str = (String) adapterView.getItemAtPosition(i);
                duration_str = duration_str.replace(" h", "");
                model.getTourConfiguration().setDuration(Double.valueOf(duration_str));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        durationSpinner.setSelection(1);

        FloatingActionButton nextButton = (FloatingActionButton) findViewById(R.id.fab2);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int radius;
                // Some fine tuning, taking into account, that the route can be longer when calculated.
                final int lenDivisor = 2;
                // Distance[km] = (Speed[km/h]- 1 km/h + Tempo[1] * 1 km/h) * Time[h]
                TourConfiguration configuration = model.getTourConfiguration();
                double distance = (configuration.getAvgWalkSpeed() + (double) configuration.getTempo() - 1.0) * configuration.getDuration();
                if (configuration.isRoundTour()) {
                    radius = (int) (distance * 1000.0 / 2.0) / lenDivisor;
                } else {
                    radius = (int) (distance * 1000.0) / lenDivisor;
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
                            //POIFetcher.requestPOIs(getApplicationContext(),52.3758916,9.7320104,radius);
                            Log.e(TAG, "GPS Position Error");
                        }
                        progressDialog.dismiss();
                        Intent i = new Intent(getApplicationContext(), POISelectorActivity.class);
                        startActivity(i);
                    }
                }.start();
            }
        });

        //WatchID holen und der Kommunikationsklasse geben
        mGoogleApiClient = new GoogleApiHelper(this).getGoogleApiClient();
        mGoogleApiClient.connect();
        //Node-ID der Uhr suchen (momentan basierend auf dem Namen)
        Log.d(TAG, "Searching for connected Devices ...");
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {

                for (Node node : getConnectedNodesResult.getNodes()) {
                    Log.d(TAG, "ConnectedDevice '"+node.getDisplayName()+"', NodeId = "+node.getId());

                    if( node.getDisplayName().contains("Moto 360")){
                        watchId = node.getId();
                        Log.d(TAG,"Watch found and assigned! ("+node.getId()+")");
                        //add WatchID to WatchNotifier
                        WatchNotifier.setWatchId(watchId);
                    }
                }
            }
        });
    }

    /**
     * Set up the GPS Tracking.
     * Do not call this method, unless permissions have been granted already.
     */
    @SuppressLint("MissingPermission")
    private void setUpGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_MIN_MILLIS, GPS_UPDATE_MIN_METERS, model);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        new GetCurrentLocationTask().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        for (int i = 0; i < permissions.length;i++)
        Log.d(TAG, permissions[i] + " " + grantResults[i] + " should be " + PackageManager.PERMISSION_GRANTED);

        switch (requestCode) {
            case location_permission_request:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permissions were granted continue with the app.
                    setUpGPS();
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
                model.setLastLocation(location);
            else
                new GetCurrentLocationTask().execute();
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }
}
