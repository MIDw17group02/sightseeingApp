package network;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

/**
 * Created by Justin on 06.12.2017.
 */

public class WatchNotifier{
    //Log-Tag
    public static String TAG = "Phone-SendingClass";

    //DataApi-Variables
    private static GoogleApiClient mGoogleApiClient = null;
    private static String watchId = null;
    private static final String DATA_PATH = "/watch_data";
    private static final String DATA_PATH_POI = "/watch_data_poi";
    private static final String OPEN_NAV_CMD = "open-nav-app";
    private static final String OPEN_INFO_CMD = "open-info-app";

    //set GoogleApiClient
    public static void setGoogleApiClient(GoogleApiClient googleApiC) {
        mGoogleApiClient = googleApiC;
    }

    //set WatchID
    public static void setWatchId(String wID) {
        watchId = wID;
    }

    //Schicke Sehenswürdigkeit Info
    public static void sendInfoData(Bitmap bitmap, String name, String info){
        Asset asset = createAssetFromBitmap(bitmap);
        Log.d(TAG, "ASSET " + asset);
        Log.d(TAG, "client: " + mGoogleApiClient.toString() + "connected:" + mGoogleApiClient.isConnected());

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(DATA_PATH_POI);
        putDataMapReq.getDataMap().putString("name", name);
        putDataMapReq.getDataMap().putString("info", info);
        putDataMapReq.getDataMap().putAsset("image", asset);
        putDataMapReq.setUrgent();

        Log.d(TAG, "test1");
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Log.d(TAG, "test2");
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d(TAG, "test3");

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(final DataApi.DataItemResult result) {
                if(result.getStatus().isSuccess()) {
                    Log.d(TAG, "Data item set: " + result.getDataItem().getUri());

                    //Messeage an Uhr senden (starten der App)
                    //Wearable.MessageApi.sendMessage(mGoogleApiClient, watchId, DATA_PATH_POI, OPEN_INFO_CMD.getBytes());
                }
            }
        });
    }

    //Schicke Navigations Daten
    public static void sendNavData(String direction, String distance){
        Log.d(TAG, "Try sending data...");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(DATA_PATH);
        putDataMapReq.getDataMap().putString("dir", direction);
        putDataMapReq.getDataMap().putString("dis", distance);
        putDataMapReq.setUrgent();
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();

        Log.d(TAG, "api: " + mGoogleApiClient.toString());
        Log.d(TAG, "watchID: " + watchId);

        if(mGoogleApiClient == null) {
            Log.d(TAG, "apiclient == null");
        }
        if(watchId == null) {
            Log.d(TAG, "watchid == null");
        }
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(final DataApi.DataItemResult result) {
                if(result.getStatus().isSuccess()) {
                    Log.d(TAG, "Data item set: " + result.getDataItem().getUri());
                    //Messeage an Uhr senden (starten der App)
                    //Wearable.MessageApi.sendMessage(mGoogleApiClient, watchId, DATA_PATH, OPEN_NAV_CMD.getBytes());
                }
                else {
                    Log.d(TAG, "fail sending Data to watch");
                }
            }
        });
    }

    //transform given Bitmap to Asset
    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }


}
