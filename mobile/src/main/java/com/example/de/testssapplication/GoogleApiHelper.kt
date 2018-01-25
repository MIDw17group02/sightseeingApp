package com.example.de.testssapplication

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.wearable.Wearable

class GoogleApiHelper(private val context: FragmentActivity) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    var googleApiClient: GoogleApiClient? = null
        private set
    private var connectionListener: ConnectionListener? = null
    private var connectionBundle: Bundle? = null

    val isConnected: Boolean
        get() = googleApiClient != null && googleApiClient!!.isConnected

    init {
        buildGoogleApiClient()
        connect()
    }

    fun setConnectionListener(connectionListener: ConnectionListener) {
        this.connectionListener = connectionListener
        if (this.connectionListener != null && isConnected) {
            connectionListener.onConnected(connectionBundle)
        }
    }

    fun connect() {
        if (googleApiClient != null) {
            googleApiClient!!.connect()
        }
    }

    fun disconnect() {
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            googleApiClient!!.disconnect()
        }
    }

    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(context)
                .enableAutoManage(context /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Wearable.API)
                .build()
    }

    override fun onConnected(bundle: Bundle?) {
        connectionBundle = bundle
        if (connectionListener != null) {
            connectionListener!!.onConnected(bundle)
        }
    }

    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "onConnectionSuspended: googleApiClient.connect()")
        googleApiClient!!.connect()
        if (connectionListener != null) {
            connectionListener!!.onConnectionSuspended(i)
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed: connectionResult = " + connectionResult)
        if (connectionListener != null) {
            connectionListener!!.onConnectionFailed(connectionResult)
        }
    }

    interface ConnectionListener {
        fun onConnectionFailed(connectionResult: ConnectionResult)

        fun onConnectionSuspended(i: Int)

        fun onConnected(bundle: Bundle?)
    }

    companion object {
        private val TAG = "Phone-Api-Helper"
    }
}