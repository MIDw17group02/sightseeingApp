package com.example.de.testssapplication;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.places.*;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    GeoDataClient mGeoDataClient;
    PlaceDetectionClient mPlaceDetectionClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    GoogleMap mMap;
    boolean mLocationPermissionGranted;
    private static final String TAG = MapActivity.class.getSimpleName();
    // Used for selecting the current place.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 10;
    private Location mLastKnownLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        /*
        //get access token
        String url = "https://api.yelp.com/oauth2/token?client_id=" +
                getString(R.string.yelp_client_id) + "&client_secret=" + getString(R.string.yelp_client_secret);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {

            }
        }).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            JSONObject jsonObjectToken = new JSONObject(response.body().string().trim());
            access_token = jsonObjectToken.getString("access_token");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String term = "dönner";                       // term
                String location = "Hannover, Germany";            // location
                String price = "1";
                OkHttpClient client = new OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder().
                        url("https://api.yelp.com/v3/businesses/search?term=" + term + "&location=" + location + "&limit=10&sort_by=rating&price=" + price + "").
                        addHeader("authorization", "Bearer " + access_token).
                        build();
                try {
                    okhttp3.Response response2 = client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response2.body().string().trim());
                    JSONArray myResponse = (JSONArray) jsonObject.get("businesses");
                    String s = "";
                    for (int i = 0; i < myResponse.length(); i++) {
                        s += myResponse.getJSONObject(i).getString("id");
                        s += "__\n";
                    }
                    textView.setText(s);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
        */

        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
    }

    //private String access_token;

    /**
     * Sets up the options menu.
     *
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
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
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPOIs();
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

    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPOIs() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted && mLastKnownLocation != null) {
            double mLong = mLastKnownLocation.getLongitude();
            double mLat = mLastKnownLocation.getLatitude();
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+
            "location="+mLat+","+mLong+"&radius=500&type=restaurant&key="+getString(R.string.google_maps_key);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                JSONObject jsonObjectToken = new JSONObject(response.body().string().trim());
                JSONArray jsonArray = jsonObjectToken.getJSONArray("results");
                String text = "";
                for (int i = 0; i< jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    text += jsonObject.getString("name");
                    text += "\n";
                }
                Toast.makeText(this.getApplicationContext(), text , Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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

}

