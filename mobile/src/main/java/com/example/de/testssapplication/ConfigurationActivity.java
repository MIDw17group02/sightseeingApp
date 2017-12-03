package com.example.de.testssapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import model.DataModel;
import network.POIFetcher;

public class ConfigurationActivity extends AppCompatActivity {

    private Button selectPOIs;
    private Switch switchRound;
    ProgressDialog progressDialog;
    private EditText EditTextDistance;
    private EditText EditTextDuration;

    final int location_permission_request = 1;
    private DataModel model;

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

        progressDialog = new ProgressDialog(this);
        EditTextDistance = (EditText) findViewById(R.id.edit_text_distance);
        EditTextDuration = (EditText) findViewById(R.id.edit_text_duration);

        selectPOIs = (Button) findViewById(R.id.continueSelectionButton);
        selectPOIs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int radius; // TODO check double value is not empty etc.
                if (switchRound.isActivated()) {
                    radius = (int) (Double.valueOf(EditTextDistance.getText().toString()) * 1000.0 / 2.0);
                } else {
                    radius = (int) (Double.valueOf(EditTextDistance.getText().toString()) * 1000.0);
                }

                /*if (model.getLastLocation() == null) {
                    POIFetcher.requestPOIs(getApplicationContext(), radius);
                }*/

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
                            POIFetcher.requestPOIs(getApplicationContext(), 52.3758916,  9.7320104, 1000);
                        }
                        progressDialog.dismiss();
                        Intent i = new Intent(getApplicationContext(), POISelectorActivity.class);
                        startActivity(i);
                    }
                }.start();
            }
        });

        switchRound = (Switch) findViewById(R.id.switch_roundtour);
        switchRound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    switchRound.setText(getString(R.string.roundOn));
                } else {
                    switchRound.setText(getString(R.string.roundOff));
                }
            }
        });

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

}
