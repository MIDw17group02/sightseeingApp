package com.example.de.testssapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutionException;

import network.WatchNotifier;

public class TestWatchSending extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    //Log-Tag
    public static String TAG = "Phone-TestWatch";

    //DataApi-Variables
    private GoogleApiClient mGoogleApiClient;
    private String watchId = "";
    private WatchNotifier wn;

    private static final String DATA_PATH = "/watch_data";
    private static final String OPEN_NAV_CMD = "open-nav-app";
    private static final String OPEN_INFO_CMD = "open-info-app";

    public Button dirButton;
    public Button poiButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_watch_sending);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //GoogleApiClient zum syncen der Daten zwischen Handy und Uhr
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        dirButton = (Button) findViewById(R.id.direction);
        poiButton = (Button) findViewById(R.id.poi);
        //Node-ID der Uhr suchen (momentan basierend auf dem Namen)
        Log.d(TAG, "Searching for connected Devices ...");
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {

                for (Node node : getConnectedNodesResult.getNodes()) {
                    Log.d(TAG, "ConnectedDevice '"+node.getDisplayName()+"', NodeId = "+node.getId());

                    if( node.getDisplayName().equalsIgnoreCase("Moto 360 26CX")){
                        watchId = node.getId();
                        Log.d(TAG,"Watch found and assigned! ("+node.getId()+")");
                    }
                }
            }
        });
        //class for sending
        wn = new WatchNotifier(mGoogleApiClient, watchId);

        poiButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = "Heldenstatue";
                String info = "Das Hermannsdenkmal ist eine Kolossalstatue in der Nähe von Hiddesen südwestlich von Detmold in Nordrhein-Westfalen im südlichen Teutoburger Wald. Es wurde zwischen 1838 und 1875 nach Entwürfen von Ernst von Bandel erbaut und am 16. August 1875 eingeweiht.\n" +
                        "\n" +
                        "Das Denkmal soll an den Cheruskerfürsten Arminius erinnern, insbesondere an die sogenannte Schlacht im Teutoburger Wald, in der germanische Stämme unter seiner Führung den drei römischen Legionen XVII, XVIII und XIX unter Publius Quinctilius Varus im Jahre 9 eine entscheidende Niederlage beibrachten.\n" +
                        "\n" +
                        "Mit einer Figurhöhe von 26,57 Metern und einer Gesamthöhe von 53,46 Metern ist es die höchste Statue Deutschlands und war von 1875 bis zur Erbauung der Freiheitsstatue 1886 die höchste Statue der westlichen Welt.";
                String url = "http://vignette3.wikia.nocookie.net/goanimate-v2/images/7/77/Mrhappy0902_468x442.jpg";
                Bitmap bitmap = null;
                try {
                    bitmap = new MyTask().execute(url).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                wn.sendInfoData(bitmap, name, info);
            }
        });

        dirButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String direction = "rechts";
                String distance = "100m";
                wn.sendNavData(direction, distance);
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class MyTask extends AsyncTask<String, String, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            String src = params[0];
            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
        }
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: connecting GoogleApiClient");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause: disconnecting GoogleApiClient & removing GoogleApiListener");
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: adding GoogleApiListener");

        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: "+i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        String str = "";
        if(connectionResult != null){
            str = connectionResult.toString();
        }
        Log.d(TAG, "onConnectionFailed: "+str);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        String str = "";
        if(dataEventBuffer != null){
            str = "Received Buffersize = "+dataEventBuffer.getCount();
        }
        Log.d(TAG, "onDataChanged: "+str);
    }

}
