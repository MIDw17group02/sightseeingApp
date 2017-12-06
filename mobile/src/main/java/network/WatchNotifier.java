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
    private GoogleApiClient mGoogleApiClient;
    private String watchId = "";
    private static final String DATA_PATH = "/watch_data";
    private static final String OPEN_NAV_CMD = "open-nav-app";
    private static final String OPEN_INFO_CMD = "open-info-app";



    public WatchNotifier(GoogleApiClient googleApiC, String wID) {
        //GoogleApiClient zum syncen der Daten zwischen Handy und Uhr
        mGoogleApiClient = googleApiC;
        watchId = wID;
    }

    /*
     * Schicke Sehenswürdigkeit Info
     */
    public void sendInfoData(Bitmap bitmap, String name, String info){

        Asset asset = createAssetFromBitmap(bitmap);

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(DATA_PATH);
        putDataMapReq.getDataMap().putString("name", name);
        putDataMapReq.getDataMap().putString("info", info);
        putDataMapReq.getDataMap().putAsset("image", asset);
        putDataMapReq.setUrgent();

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(final DataApi.DataItemResult result) {
                if(result.getStatus().isSuccess()) {
                    Log.d(TAG, "Data item set: " + result.getDataItem().getUri());

                    //Messeage an Uhr senden (starten der App)
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, watchId, DATA_PATH, OPEN_INFO_CMD.getBytes());

                    //Toast.makeText(getBaseContext(), "Daten erfolgreich an Uhr gesendet.", Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }

    /*
     * Schicke Navigations Daten
     */
    public void sendNavData(String direction, String distance){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(DATA_PATH);
        putDataMapReq.getDataMap().putString("dir", direction);
        putDataMapReq.getDataMap().putString("dis", distance);
        putDataMapReq.setUrgent();

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(final DataApi.DataItemResult result) {
                if(result.getStatus().isSuccess()) {
                    Log.d(TAG, "Data item set: " + result.getDataItem().getUri());

                    //Messeage an Uhr senden (starten der App)
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, watchId, DATA_PATH, OPEN_NAV_CMD.getBytes());

                    //Toast.makeText(getBaseContext(), "Daten erfolgreich an Uhr gesendet.", Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }


}