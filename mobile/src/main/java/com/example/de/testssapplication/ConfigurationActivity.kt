package com.example.de.testssapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Spinner
import android.widget.Switch

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeApi
import com.google.android.gms.wearable.Wearable

import model.DataModel
import model.TourConfiguration
import network.POIFetcher
import network.WatchNotifier

class ConfigurationActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private val GPS_UPDATE_MIN_MILLIS = 10000
    private val GPS_UPDATE_MIN_METERS = 20

    private val nextButton: FloatingActionButton? = null
    private var switchRound: Switch? = null
    private var progressDialog: ProgressDialog? = null
    private var tempoSpinner: Spinner? = null
    private var durationSpinner: Spinner? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    internal val location_permission_request = 1
    private var model: DataModel? = null
    private var currentLocation: Location? = null

    //watchID for watchConnectin
    private var mGoogleApiClient: GoogleApiClient? = null
    private var watchId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration2)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        model = DataModel.instance

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request Permissions from User
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), location_permission_request)
        } else {
            setUpGPS()
        }

        title = getString(R.string.title_activity_configuration)

        progressDialog = ProgressDialog(this)

        switchRound = findViewById<View>(R.id.switch_roundtour) as Switch
        switchRound!!.setOnCheckedChangeListener { compoundButton, checked ->
            model!!.tourConfiguration?.isRoundTour = checked
            //switchRound.setText(checked ? getString(R.string.roundOn) : getString(R.string.roundOff));
        }

        tempoSpinner = findViewById<View>(R.id.spinner_tempo) as Spinner
        val sp_adapter_1 = ArrayAdapter.createFromResource(this, R.array.tempo_array, R.layout.double_spinner_item)
        sp_adapter_1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tempoSpinner!!.adapter = sp_adapter_1
        tempoSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                model!!.tourConfiguration?.tempo = i
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        tempoSpinner!!.setSelection(1)

        durationSpinner = findViewById<View>(R.id.spinner_duration) as Spinner
        val sp_adapter_2 = ArrayAdapter.createFromResource(this, R.array.duration_array, R.layout.double_spinner_item)
        sp_adapter_2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        durationSpinner!!.adapter = sp_adapter_2
        durationSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                var duration_str = adapterView.getItemAtPosition(i) as String
                duration_str = duration_str.replace(" h", "")
                model!!.tourConfiguration?.duration = java.lang.Double.valueOf(duration_str)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        durationSpinner!!.setSelection(1)

        val nextButton = findViewById<View>(R.id.fab2) as FloatingActionButton
        nextButton.setOnClickListener {
            val radius: Int
            // Some fine tuning, taking into account, that the route can be longer when calculated.
            val lenDivisor = 2
            // Distance[km] = (Speed[km/h]- 1 km/h + Tempo[1] * 1 km/h) * Time[h]
            val configuration = model!!.tourConfiguration
            val distance = (configuration!!.avgWalkSpeed + configuration.tempo.toDouble() - 1.0) * configuration.duration
            if (configuration.isRoundTour) {
                radius = (distance * 1000.0 / 2.0).toInt() / lenDivisor
            } else {
                radius = (distance * 1000.0).toInt() / lenDivisor
            }

            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.setMessage(getString(R.string.loading_pois))
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()

            // Run network fetch in background.
            object : Thread() {
                override fun run() {
                    if (model!!.getLastLocation() != null) {
                        POIFetcher.requestPOIs(applicationContext, radius)
                    } else {
                        //POIFetcher.requestPOIs(getApplicationContext(),52.3758916,9.7320104,radius);
                        Log.e(TAG, "GPS Position Error")
                    }
                    progressDialog!!.dismiss()
                    val i = Intent(applicationContext, POISelectorActivity::class.java)
                    startActivity(i)
                }
            }.start()
        }

        //WatchID holen und der Kommunikationsklasse geben
        mGoogleApiClient = GoogleApiHelper(this).googleApiClient
        mGoogleApiClient!!.connect()
        //Node-ID der Uhr suchen (momentan basierend auf dem Namen)
        Log.d(TAG, "Searching for connected Devices ...")
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback { getConnectedNodesResult ->
            for (node in getConnectedNodesResult.nodes) {
                Log.d(TAG, "ConnectedDevice '" + node.displayName + "', NodeId = " + node.id)

                if (node.displayName.contains("Moto 360")) {
                    watchId = node.id
                    Log.d(TAG, "Watch found and assigned! (" + node.id + ")")
                    //add WatchID to WatchNotifier
                    WatchNotifier.setWatchId(watchId)
                }
            }
        }
    }

    /**
     * Set up the GPS Tracking.
     * Do not call this method, unless permissions have been granted already.
     */
    @SuppressLint("MissingPermission")
    private fun setUpGPS() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_MIN_MILLIS.toLong(), GPS_UPDATE_MIN_METERS.toFloat(), model)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        GetCurrentLocationTask().execute()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        for (i in permissions.indices)
            Log.d(TAG, permissions[i] + " " + grantResults[i] + " should be " + PackageManager.PERMISSION_GRANTED)

        when (requestCode) {
            location_permission_request -> if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions were granted continue with the app.
                setUpGPS()
            } else {
                // Permissions were denied. Show dialog and close app.
                val builder = AlertDialog.Builder(this)

                builder.setTitle(getString(R.string.error_dialog_title))
                builder.setMessage(getString(R.string.dialog_no_permissions))
                builder.setPositiveButton(R.string.ok) { dialog, id ->
                    // Finish the App
                    finishAndRemoveTask()
                }

                val dialog = builder.create()
                dialog.show()
            }
        }
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    override fun onConnected(bundle: Bundle?) {

    }

    override fun onConnectionSuspended(i: Int) {

    }

    private inner class GetCurrentLocationTask : AsyncTask<Void, Void, Location>() {

        @SuppressLint("MissingPermission")
        override fun doInBackground(vararg voids: Void): Location? {
            mFusedLocationClient!!.lastLocation
                    .addOnSuccessListener(this@ConfigurationActivity) { location ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currentLocation = location
                        }
                    }
            return currentLocation
        }

        override fun onPostExecute(location: Location?) {
            if (location != null)
                model!!.setLastLocation(location)
            else
                GetCurrentLocationTask().execute()
        }
    }

    override fun onBackPressed() {
        return
    }

    companion object {
        //Log-Tag
        var TAG = "Phone-Configuration"
    }
}
