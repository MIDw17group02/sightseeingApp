package com.example.de.testssapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

import model.DataModel
import model.DirectionHelper
import model.ITourTracker
import model.POI
import network.WatchNotifier


class MapActivity : AppCompatActivity(), OnMapReadyCallback, ITourTracker {

    private var mGeoDataClient: GeoDataClient? = null
    private var mPlaceDetectionClient: PlaceDetectionClient? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    internal var mMap: GoogleMap? = null
    internal var mLocationPermissionGranted: Boolean = false
    private var mLastKnownLocation: Location? = null
    private val handler = Handler()
    private var directionHelper: DirectionHelper? = null

    //Orientation
    private var lastDirection = -1
    private var lastInstruction = ""
    private var lastDistance = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        DataModel.instance.tourTrackers.add(this)

        WatchNotifier.setGoogleApiClient(GoogleApiHelper(this).googleApiClient!!)

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        map.getMapAsync(this)

        DataModel.instance.tourStatistics?.walkedDuration = System.currentTimeMillis()

        for (testPOI in DataModel.instance.selectedPOIs) {
            if (testPOI.infoText != null && testPOI.name != null && testPOI.photo != null) {
                Log.d("MapActivity", "Sendnonnull " + testPOI.name)
                WatchNotifier.sendInfoData(testPOI.photo!!, testPOI.name!!, testPOI.infoText!!)
                break
            }
        }
    }


    override fun OnTourEnd() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_finish_title)
        builder.setMessage(R.string.dialog_want_to_finish)
        builder.setPositiveButton(R.string.yes) { dialogInterface, i ->
            finish()
            val intent = Intent(applicationContext, EndScreenActivity::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton(R.string.no) { dialogInterface, i -> dialogInterface.dismiss() }

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun OnPOIReached(poi: POI) {
        Toast.makeText(this, poi.name, Toast.LENGTH_LONG).show()
        if (poi.infoText != null) {
            WatchNotifier.sendInfoData(poi.photo!!, poi.name!!, getString(R.string.no_info_text))
        } else {
            WatchNotifier.sendInfoData(poi.photo!!, poi.name!!, poi.infoText!!)
        }
    }

    /**
     * Sets up the options menu.
     *
     * @param menu The options menu.
     * @return Boolean.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return true
    }

    /**
     * Handles a click on the menu option to get a place.
     *
     * @param item The menu item to handle.
     * @return Boolean.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_exit_tour) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialog_want_to_exit)
            builder.setPositiveButton(R.string.yes) { dialogInterface, i ->
                // Finish the App
                //finishAndRemoveTask();
                //finishAffinity();
                finish()
                val intent = Intent(applicationContext, EndScreenActivity::class.java)
                startActivity(intent)
            }
            builder.setNegativeButton(R.string.no) { dialogInterface, i -> dialogInterface.dismiss() }

            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.show()
        }

        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        directionHelper = DirectionHelper.getInstance(mMap!!)
        val pois = directionHelper!!.makeTours()
        directionHelper!!.addPolylineDirection(this, pois)

        mLastKnownLocation = DataModel.instance.getLastLocation()
        Log.d("Map", "StartLoc " + mLastKnownLocation!!.toString())
        if (mLastKnownLocation != null) {
            // Set the camera zoom.
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(mLastKnownLocation!!.latitude,
                            mLastKnownLocation!!.longitude), 12f))

            val mOptions = MarkerOptions()
            mOptions.position(LatLng(mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude))
            val locMarker = mMap!!.addMarker(mOptions)
            locMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

        }

        val runnableCode = object : Runnable {
            override fun run() {
                // Do something here on the main thread
                // Repeat this the same runnable code block again another 30 seconds
                // 'this' is referencing the Runnable object
                directionHelper!!.updateVisitedPOIs()
                var nextInstruction = directionHelper!!.nextDirection(this@MapActivity)

                var distance = nextInstruction.split("in".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                distance = distance.replace("\\s+".toRegex(), "")
                Log.d(TAG, "Distance:#$distance#")

                if (lastDirection != -1) {
                    nextInstruction = nextInstruction.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                    val direction = directionToDegree(nextInstruction)
                    val turn = direction - lastDirection
                    nextInstruction = degreeToTurn(turn)
                    lastDirection = direction
                }
                Log.d(TAG, "Direction:#$nextInstruction#")
                if (nextInstruction != lastInstruction || distance != lastDistance) {
                    //nur senden, wenn sich was verändert
                    WatchNotifier.sendNavData(nextInstruction, distance)
                    Log.d(TAG, "send new Direction-Instruction")
                    lastInstruction = nextInstruction
                    lastDistance = distance
                }

                Log.e(javaClass.simpleName, nextInstruction)
                //Toast.makeText(MapActivity.this, nextInstruction, 3*1000).show();
                handler.postDelayed(this, DIRECTION_DELAY_MILLIS.toLong())
            }
        }
        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode)
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient!!.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result
                        if (mLastKnownLocation != null) {
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    LatLng(mLastKnownLocation!!.latitude,
                                            mLastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
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

        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            }
        }
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

    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.dialog_want_to_exit)
        builder.setPositiveButton(R.string.yes) { dialogInterface, i ->
            // Finish the App
            finish()
            val intent = Intent(applicationContext, EndScreenActivity::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton(R.string.no) { dialogInterface, i -> dialogInterface.dismiss() }

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun directionToDegree(directionString: String): Int {
        val degree: Int
        when (directionString) {
            "north" -> degree = 0
            "northeast" -> degree = 45
            "east" -> degree = 90
            "southeast" -> degree = 135
            "south" -> degree = 180
            "southwest" -> degree = 225
            "west" -> degree = 270
            "northwest" -> degree = 315
            else -> degree = 0
        }
        return degree
    }

    private fun degreeToTurn(degree: Int): String {
        var degree = degree
        //Eingabe: -359 bis +359
        if (degree < -180) {
            degree = degree + 360
        }
        if (degree > 180) {
            degree = degree - 360
        }
        //nun Werte von -180 bis +180

        var turnCommand = "do nothing"
        if (degree >= -22 && degree <= 22) {
            turnCommand = "ahead"
        } else if (degree >= 23 && degree <= 67) {
            turnCommand = "hright"
        } else if (degree >= 68 && degree <= 112) {
            turnCommand = "right"
        } else if (degree >= 113 && degree <= 157) {
            turnCommand = "sright"
        } else if (degree >= 158 || degree <= -158) {
            turnCommand = "back"
        } else if (degree >= -157 && degree <= -113) {
            turnCommand = "sleft"
        } else if (degree >= -112 && degree <= -68) {
            turnCommand = "left"
        } else if (degree >= -67 && degree <= -23) {
            turnCommand = "hleft"
        }
        return turnCommand
    }

    companion object {
        private val TAG = MapActivity::class.java.simpleName
        // Used for selecting the current place.
        private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private val DEFAULT_ZOOM = 14
        private val DIRECTION_DELAY_MILLIS = 20000
    }


}

