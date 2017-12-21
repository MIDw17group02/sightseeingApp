package Fragments;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.de.testssapplication.GoogleApiHelper;
import com.example.de.testssapplication.POISelectorActivity;
import com.example.de.testssapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import model.DataModel;
import model.POI;

/**
 * Created by de on 02.12.2017.
 */

public class POIMapFragment extends Fragment implements OnMapReadyCallback,
        /* GoogleMap.OnMarkerClickListener,*/
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private DataModel model;
    private POISelectorActivity parent;

    private static final String TAG = POIMapFragment.class.getSimpleName();
    private GoogleMap mMap;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // Default location is set to the Leibniz University Hanover.
    private final LatLng mDefaultLocation = new LatLng(52.382974, 9.719682);
    private static final int DEFAULT_ZOOM = 12;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    public FloatingActionButton fab;

    public POIMapFragment() {
    }

    public static POIMapFragment newInstance() {
        POIMapFragment fragment = new POIMapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {

        // Using the same map as the dynamic map where the route will be shown later.
        View rootView = inflater.inflate(R.layout.activity_map, viewGroup, false);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(parent);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
        // Using two separate maps.
        View rootView = inflater.inflate(R.layout.tab_map, viewGroup, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.staticMap);
        mapFragment.getMapAsync(this);

        */

        if (savedInstanceState != null) {
            //mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mLastKnownLocation = model.getLastLocation();
            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);

        }

        model = DataModel.getInstance();
        GoogleApiClient mGoogleApiClient = new GoogleApiHelper(getActivity()).getGoogleApiClient();

        if (mMap != null) updateMarkers();

        return rootView;
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        updateMarkers();

       // mMap.setOnMarkerClickListener(this);

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }


    @Override
    public View getInfoWindow(Marker marker) { return null;}

    @Override
    public View getInfoContents(Marker marker) {
        // Inflate the layouts for the info window, title and snippet.
        View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                (FrameLayout) parent.findViewById(R.id.map), false);

        POI markerPOI = model.getPOI((int)marker.getTag());

        TextView title = infoWindow.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = infoWindow.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());

        snippet.setTextColor(markerPOI.isSelected() ? Color.GREEN : Color.RED);

        //TODO: add image to info window
        //ImageView photo = new ImageView();
        //photo.setImageBitmap(markerPOI.getPhoto());
        //infoWindow.set

        return infoWindow;
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    public void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                mLastKnownLocation = model.getLastLocation();
                if (mLastKnownLocation != null) {
                    // Set the map's camera position to the current location of the device.
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        } catch (SecurityException e)  {
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
        if (ContextCompat.checkSelfPermission(parent.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(parent,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Updates the markers shown on the map.
     * Green markers are selected POIs while red markers are unselected POIs.
     */

    public void updateMarkers() {

        mMap.clear();

        List<POI> poiList = model.getNearbyPOIs();

        for (int i = 0; i < poiList.size(); i++) {
            POI poi = poiList.get(i);
            poi.setIndex(i);
            Marker mPOI = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(poi.getLatitude(), poi.getLongitude()))
                    .title(poi.getName()));
            mPOI.setTag(i);
            if (poi.isSelected()) {
                mPOI.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mPOI.setSnippet("Ausgewählt");
            }
            else {
                mPOI.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mPOI.setSnippet("Nicht ausgewählt");
            }
        }

        if (fab != null) {
            fab.setForeground(getResources().getDrawable(model.getSelectedPOIs().size() == 0 ? R.drawable.right_arrow_red : R.drawable.right_arrow));
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
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
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void updateCamera() {

        mLastKnownLocation = model.getLastLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastKnownLocation.getLatitude(),
                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*
    @Override
    public boolean onMarkerClick(Marker marker) {

        POI markerPOI = model.getPOI((int)marker.getTag());
        markerPOI.setSelected(!markerPOI.isSelected());
        //Toast.makeText(parent, marker.getTitle(), Toast.LENGTH_SHORT).show();
        marker.showInfoWindow();
        updateMarkers();

        return false;
    }
    */

    @Override
    public void onInfoWindowClick(Marker marker) {

        POI markerPOI = model.getPOI((int)marker.getTag());
        markerPOI.setSelected(!markerPOI.isSelected());
        updateMarkers();

    }
    public void setParent (POISelectorActivity parent) {
        this.parent = parent;
    }

}
