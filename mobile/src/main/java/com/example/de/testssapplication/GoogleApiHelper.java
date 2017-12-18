package com.example.de.testssapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.wearable.Wearable;

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "Phone-Api-Helper";
    private FragmentActivity context;
    private GoogleApiClient mGoogleApiClient;
    private ConnectionListener connectionListener;
    private Bundle connectionBundle;

    public GoogleApiHelper(FragmentActivity context) {
        this.context = context;
        buildGoogleApiClient();
        connect();
    }

    public GoogleApiClient getGoogleApiClient() {
        return this.mGoogleApiClient;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        if (this.connectionListener != null && isConnected()) {
            connectionListener.onConnected(connectionBundle);
        }
    }

    public void connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean isConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(context /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        connectionBundle = bundle;
        if (connectionListener != null) {
            connectionListener.onConnected(bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: googleApiClient.connect()");
        mGoogleApiClient.connect();
        if (connectionListener != null) {
            connectionListener.onConnectionSuspended(i);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: connectionResult = " + connectionResult);
        if (connectionListener != null) {
            connectionListener.onConnectionFailed(connectionResult);
        }
    }

    public interface ConnectionListener {
        void onConnectionFailed(@NonNull ConnectionResult connectionResult);

        void onConnectionSuspended(int i);

        void onConnected(Bundle bundle);
    }
}