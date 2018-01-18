package com.example.de.testssapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import model.DataModel;
import model.DirectionHelper;
import model.ITourTracker;
import model.POI;
import network.WatchNotifier;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, ITourTracker {

    GeoDataClient mGeoDataClient;
    PlaceDetectionClient mPlaceDetectionClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    GoogleMap mMap;
    boolean mLocationPermissionGranted;
    private static final String TAG = MapActivity.class.getSimpleName();
    // Used for selecting the current place.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 14;
    private static final int DIRECTION_DELAY_MILLIS = 20000;
    private Location mLastKnownLocation;
    private Handler handler = new Handler();
    private DirectionHelper directionHelper;

    //Orientation
    private int lastDirection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        DataModel.Companion.getInstance().getTourTrackers().add(this);

        WatchNotifier.setGoogleApiClient(new GoogleApiHelper(this).getGoogleApiClient());

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);

        DataModel.Companion.getInstance().getTourStatistics().setWalkedDuration(System.currentTimeMillis());

        for (POI testPOI : DataModel.Companion.getInstance().getSelectedPOIs()) {
            if (testPOI.getInfoText() != null && testPOI.getName() != null && testPOI.getPhoto() != null) {
                Log.d("MapActivity", "Sendnonnull " + testPOI.getName());
                WatchNotifier.sendInfoData(testPOI.getPhoto(), testPOI.getName(), testPOI.getInfoText());
                break;
            }
        }
    }


    @Override
    public void OnTourEnd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_finish_title);
        builder.setMessage(R.string.dialog_want_to_finish);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                Intent intent = new Intent(getApplicationContext(), EndScreenActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void OnPOIReached(POI poi) {
        Toast.makeText(this, poi.getName(), Toast.LENGTH_LONG).show();
        if (poi.getInfoText() != null) {
            WatchNotifier.sendInfoData(poi.getPhoto(), poi.getName(), getString(R.string.no_info_text));
        } else {
            WatchNotifier.sendInfoData(poi.getPhoto(), poi.getName(), poi.getInfoText());
        }
    }

    /**
     * Sets up the options menu.
     *
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     *
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_exit_tour) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_want_to_exit);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Finish the App
                    //finishAndRemoveTask();
                    //finishAffinity();
                    finish();
                    Intent intent = new Intent(getApplicationContext(), EndScreenActivity.class);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
        }

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        directionHelper = DirectionHelper.Companion.getInstance(mMap);
        List<POI> pois = directionHelper.makeTours();
        directionHelper.addPolylineDirection(this, pois);

        mLastKnownLocation = DataModel.Companion.getInstance().getLastLocation();
        Log.d("Map", "StartLoc " + mLastKnownLocation.toString());
        if (mLastKnownLocation != null) {
            // Set the camera zoom.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), 12));

            MarkerOptions mOptions = new MarkerOptions();
            mOptions.position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
            Marker locMarker = mMap.addMarker(mOptions);
            locMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        }

        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                // Repeat this the same runnable code block again another 30 seconds
                // 'this' is referencing the Runnable object
                directionHelper.updateVisitedPOIs();
                String nextInstruction = directionHelper.nextDirection(MapActivity.this);

                String distance = nextInstruction.split("in")[1];
                distance = distance.replaceAll("\\s+","");
                Log.d(TAG, "Distance:#" + distance + "#");

                nextInstruction = nextInstruction.split(" ")[1];
                Log.d(TAG, "Direction:#" + nextInstruction + "#");
                int direction = directionToDegree(nextInstruction);
                if(lastDirection != -1) {
                    int turn = direction - lastDirection;
                    nextInstruction = degreeToTurn(turn);
                }
                lastDirection = direction;


                Log.e(getClass().getSimpleName(), nextInstruction);
                WatchNotifier.sendNavData(nextInstruction, distance);
                //Toast.makeText(MapActivity.this, nextInstruction, 3*1000).show();
                handler.postDelayed(this, DIRECTION_DELAY_MILLIS);
            }
        };
        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        }
                        /*
                        else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                        */
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_want_to_exit);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Finish the App
                finish();
                Intent intent = new Intent(getApplicationContext(), EndScreenActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private int directionToDegree(String directionString) {
        int degree;
        switch(directionString) {
            case "north":       degree = 0; break;
            case "northeast":   degree = 45; break;
            case "east":        degree = 90; break;
            case "southeast":   degree = 135; break;
            case "south":       degree = 180; break;
            case "southwest":   degree = 225; break;
            case "west":        degree = 270; break;
            case "northwest":   degree = 315; break;
            default:            degree = 0; break;
        }
        return degree;
    }

    private String degreeToTurn(int degree) {
        //Eingabe: -359 bis +359
        if (degree < -180) {
            degree = degree + 360;
        }
        if (degree > 180) {
            degree = degree - 360;
        }
        //nun Werte von -180 bis +180

        String turnCommand = "do nothing";
        if (degree >= -22 && degree <= 22) {
            turnCommand = "ahead";
        } else if (degree >= 23 && degree <= 67) {
            turnCommand = "hright";
        } else if (degree >= 68 && degree <= 112) {
            turnCommand = "right";
        } else if (degree >= 113 && degree <= 157) {
            turnCommand = "sright";
        } else if (degree >= 158 || degree <= -158) {
            turnCommand = "back";
        } else if (degree >= -157 && degree <= -113) {
            turnCommand = "sleft";
        } else if (degree >= -112 && degree <= -68) {
            turnCommand = "left";
        } else if (degree >= -67 && degree <= -23) {
            turnCommand = "hleft";
        }
        return turnCommand;
    }


}

