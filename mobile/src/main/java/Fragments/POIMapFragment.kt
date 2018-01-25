package Fragments

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

import com.example.de.testssapplication.GoogleApiHelper
import com.example.de.testssapplication.POISelectorActivity
import com.example.de.testssapplication.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

import model.DataModel
import model.POI

/**
 * Created by de on 02.12.2017.
 */

class POIMapFragment : Fragment(), OnMapReadyCallback,
        /* GoogleMap.OnMarkerClickListener,*/
        GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var model: DataModel? = null
    private var parent: POISelectorActivity? = null
    private var mMap: GoogleMap? = null

    // The entry point to the Fused Location Provider.
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    // Default location is set to the Leibniz University Hanover.
    private val mDefaultLocation = LatLng(52.382974, 9.719682)
    private var mLocationPermissionGranted: Boolean = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var mLastKnownLocation: Location? = null

    var fab: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, viewGroup: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Using the same map as the dynamic map where the route will be shown later.
        val rootView = inflater!!.inflate(R.layout.activity_map, viewGroup, false)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(parent!!)

        val mapFragment = this.childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*
        // Using two separate maps.
        View rootView = inflater.inflate(R.layout.tab_map, viewGroup, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.staticMap);
        mapFragment.getMapAsync(this);

        */

        if (savedInstanceState != null) {
            //mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mLastKnownLocation = model!!.getLastLocation()
            val mCameraPosition = savedInstanceState.getParcelable<CameraPosition>(KEY_CAMERA_POSITION)

        }

        model = DataModel.instance
        val mGoogleApiClient = GoogleApiHelper(activity).googleApiClient

        if (mMap != null) updateMarkers()

        return rootView
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        if (mMap != null) {
            outState!!.putParcelable(KEY_CAMERA_POSITION, mMap!!.cameraPosition)
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation)
            super.onSaveInstanceState(outState)
        }
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(map: GoogleMap) {
        mMap = map

        updateMarkers()

        // mMap.setOnMarkerClickListener(this);

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap!!.setInfoWindowAdapter(this)
        mMap!!.setOnInfoWindowClickListener(this)

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }


    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        // Inflate the layouts for the info window, title and snippet.
        val infoWindow = layoutInflater.inflate(R.layout.custom_info_contents,
                parent!!.findViewById<View>(R.id.map) as FrameLayout, false)

        val markerPOI = model!!.getPOI(marker.tag as Int)

        val title = infoWindow.findViewById<TextView>(R.id.title)
        title.text = marker.title

        val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
        snippet.text = marker.snippet

        snippet.setTextColor(if (markerPOI!!.isSelected) Color.GREEN else Color.RED)

        return infoWindow
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                mLastKnownLocation = model!!.getLastLocation()
                if (mLastKnownLocation != null) {
                    // Set the map's camera position to the current location of the device.
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(mLastKnownLocation!!.latitude,
                                    mLastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    mMap!!.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM.toFloat()))
                    mMap!!.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(parent!!.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(parent!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Updates the markers shown on the map.
     * Green markers are selected POIs while red markers are unselected POIs.
     */

    fun updateMarkers() {

        mMap!!.clear()

        val poiList = model!!.getNearbyPOIs()

        for (i in poiList!!.indices) {
            val poi = poiList[i]
            poi.index = i
            val mPOI = mMap!!.addMarker(MarkerOptions()
                    .position(LatLng(poi.latitude, poi.longitude))
                    .title(poi.name))
            mPOI.tag = i
            if (poi.isSelected) {
                mPOI.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mPOI.snippet = "Ausgewählt"
            } else {
                mPOI.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                mPOI.snippet = "Nicht ausgewählt"
            }
        }

        if (fab != null) {
            if (model!!.selectedPOIs.size == 0){
                fab!!.setForeground(getResources().getDrawable(R.drawable.right_arrow_red))
            }
            else {
                fab!!.setForeground(getResources().getDrawable(R.drawable.right_arrow))
            }
            //fab.setForeground(getResources().getDrawable(model.getSelectedPOIs().size() == 0 ? R.drawable.right_arrow_red : R.drawable.right_arrow));
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (mLocationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap!!.isMyLocationEnabled = false
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    fun updateCamera() {

        mLastKnownLocation = model!!.getLastLocation()
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(mLastKnownLocation!!.latitude,
                        mLastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    override fun onConnected(bundle: Bundle?) {

    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

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

    override fun onInfoWindowClick(marker: Marker) {

        val markerPOI = model!!.getPOI(marker.tag as Int)
        markerPOI!!.isSelected = !markerPOI.isSelected
        updateMarkers()

    }

    fun setParent(parent: POISelectorActivity) {
        this.parent = parent
    }

    companion object {

        private val TAG = POIMapFragment::class.java.simpleName
        private val KEY_CAMERA_POSITION = "camera_position"
        private val KEY_LOCATION = "location"
        private val DEFAULT_ZOOM = 12
        private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        fun newInstance(): POIMapFragment {
            return POIMapFragment()
        }
    }

}
