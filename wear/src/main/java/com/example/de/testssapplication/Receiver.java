package com.example.de.testssapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Justin on 06.12.2017.
 */
public class Receiver extends WearableListenerService {
    //Log-Tag
    public static String TAG = "Wear-Receiver";

    private GoogleApiClient mGoogleApiClient;
    private static final int TIMEOUT_MS = 200;
    private static final String DATA_PATH = "/watch_data";
    private static final String DATA_PATH_POI = "/watch_data_poi";
    private static final String OPEN_NAV_CMD = "open-nav-app";
    private static final String OPEN_INFO_CMD = "open-info-app";


    public boolean connectGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return false;
        }

        return true;
    }

    /*
     * fetches info data from dataapi
     */
    public void fetchInfoAndStartActivity(){
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                if (dataItems.getCount() != 0) {

                    for (DataItem item : dataItems) {
                        if ( item.getUri().getPath().equals(DATA_PATH_POI) ) {
                            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                            Log.d("WATCH", dataMap.toString());

                            //name und info auslesen
                            String name = dataMap.getString("name");
                            String info = dataMap.getString("info");

                            Log.d(TAG, "fetchData(): Name = '"+name+"' Info = '"+info+"'");

                            //bild
                            Asset profileAsset = dataMap.getAsset("image");
                            Bitmap bitmap = null;
                            try {
                                bitmap = new MyTask().execute(profileAsset).get();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            bitmap = scaleDownBitmap(bitmap, 100, getApplicationContext());
                            Log.d(TAG, "Resized Bitmap ?!");



                            //start app and pass navigation data
                            Intent intent = new Intent(Receiver.this, POINotifier.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("image", bitmap);
                            intent.putExtra("name", name);
                            intent.putExtra("info", info);


                            Log.d(TAG, "starting watch activity");
                            startActivity(intent);
                        }
                    }
                }else{
                    Log.d(TAG, "fetchData(): no data");
                }

                dataItems.release();
            }
        });
    }

    /*
     * fetches navigation data from dataapi
     */
    public void fetchNavAndStartActivity(){
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                if (dataItems.getCount() != 0) {

                    for (DataItem item : dataItems) {
                        if ( item.getUri().getPath().equals(DATA_PATH) ) {
                            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                            //name und info auslesen
                            String direction = dataMap.getString("dir");
                            String distance = dataMap.getString("dis");
                            Log.d(TAG, "fetchData(): Direction = '"+direction+"' Distance = '"+distance+"'");

                            //start app and pass navigation data
                            Intent intent = new Intent(Receiver.this, DirectionNotifier.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("dir", direction);
                            intent.putExtra("dis", distance);

                            Log.d(TAG, "starting watch activity");
                            startActivity(intent);
                        }
                    }
                }else{
                    Log.d(TAG, "fetchData(): no data");
                }

                dataItems.release();
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(TAG,"data Changed");
        super.onDataChanged(dataEventBuffer);

        connectGoogleApiClient();

        if(dataEventBuffer != null) {
            Log.d(TAG, "onDataChanged: count = " + dataEventBuffer.getCount());

            for (DataEvent event : dataEventBuffer) {
                Log.d(TAG, "onDataChanged(): path= '" + event.getDataItem().getUri().getPath() + "'");

                if( event.getDataItem().getUri().getPath().equals(DATA_PATH) ){
                    fetchNavAndStartActivity();
                }else if( event.getDataItem().getUri().getPath().equals(DATA_PATH_POI) ){
                    fetchInfoAndStartActivity();
                }else{
                    Log.d(TAG, "onDataChanged(): unmapped path='"+event.getDataItem().getUri().getPath()+"'");
                }
            }
        }

    }

    private class MyTask extends AsyncTask<Asset, String, Bitmap> {
        @Override
        protected Bitmap doInBackground(Asset... params) {
            Asset asset = params[0];
            if (asset == null) {
                //throw new IllegalArgumentException("Asset must be non-null");
                return null;
            }
            ConnectionResult result = mGoogleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!result.isSuccess()) {
                return null;
            }
            // convert asset into a file descriptor and block until it's ready
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().getInputStream();
            mGoogleApiClient.disconnect();

            if (assetInputStream == null) {
                System.out.println("Requested an unknown Asset.");
                return null;
            }
            // decode the stream into a bitmap
            return BitmapFactory.decodeStream(assetInputStream);
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
        }
    }
    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result = mGoogleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            System.out.println("Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        if(messageEvent != null){
            String path = messageEvent.getPath();
            Log.d(TAG, "onMessageReceived. " + path);

            //check if message is meant for this app (a bit overhead, since intent-filter should check for it beforehand)
            if(path.equals(DATA_PATH)) {
                //check message, navigation data & open app
                String msg = new String(messageEvent.getData());
                if( msg.equals(OPEN_INFO_CMD) || msg.equals(OPEN_NAV_CMD) ){
                    // (this message should only arrive, after navigation data was set)
                    if( mGoogleApiClient == null || !mGoogleApiClient.isConnected() ){
                        if( !connectGoogleApiClient() ){
                            return;
                        }
                    }

                    //make sure newest data is available & open app
                    if( msg.equals(OPEN_INFO_CMD) ) {

                        //fetchInfoAndStartActivity();
                    } else if( msg.equals(OPEN_NAV_CMD) ) {
                        //fetchNavAndStartActivity();
                    }

                }else{
                    Log.d(TAG, msg+" != "+OPEN_INFO_CMD+" || "+OPEN_NAV_CMD);
                }
            }else{
                Log.d(TAG, path+" != "+DATA_PATH);
            }
        }
    }

    private Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        photo=Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }
}
