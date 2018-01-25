package network

import android.graphics.Bitmap
import android.util.Log

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable

import java.io.ByteArrayOutputStream

/**
 * Created by Justin on 06.12.2017.
 */

object WatchNotifier {
    //Log-Tag
    var TAG = "Phone-SendingClass"

    //DataApi-Variables
    private var mGoogleApiClient: GoogleApiClient? = null
    private var watchId: String? = null
    private val DATA_PATH = "/watch_data"
    private val DATA_PATH_POI = "/watch_data_poi"
    private val OPEN_NAV_CMD = "open-nav-app"
    private val OPEN_INFO_CMD = "open-info-app"

    //set GoogleApiClient
    fun setGoogleApiClient(googleApiC: GoogleApiClient) {
        mGoogleApiClient = googleApiC
    }

    //set WatchID
    fun setWatchId(wID: String) {
        watchId = wID
    }

    //Schicke Sehenswürdigkeit Info
    fun sendInfoData(bitmap: Bitmap, name: String, info: String) {
        val asset = createAssetFromBitmap(bitmap)
        Log.d(TAG, "ASSET " + asset)
        Log.d(TAG, "client: " + mGoogleApiClient!!.toString() + "connected:" + mGoogleApiClient!!.isConnected)

        val putDataMapReq = PutDataMapRequest.create(DATA_PATH_POI)
        putDataMapReq.dataMap.putString("name", name)
        putDataMapReq.dataMap.putString("info", info)
        putDataMapReq.dataMap.putAsset("image", asset)
        putDataMapReq.setUrgent()

        Log.d(TAG, "test1")
        val putDataReq = putDataMapReq.asPutDataRequest()
        Log.d(TAG, "test2")
        val pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq)
        Log.d(TAG, "test3")

        if (mGoogleApiClient == null) {
            Log.d(TAG, "apiclient == null")
        }
        if (watchId == null) {
            Log.d(TAG, "watchid == null")
        }

        pendingResult.setResultCallback { result ->
            if (result.status.isSuccess) {
                Log.d(TAG, "Data item set: " + result.dataItem.uri)

                //Messeage an Uhr senden (starten der App)
                //Wearable.MessageApi.sendMessage(mGoogleApiClient, watchId, DATA_PATH_POI, OPEN_INFO_CMD.getBytes());
            }
        }
    }

    //Schicke Navigations Daten
    fun sendNavData(direction: String, distance: String) {
        Log.d(TAG, "Try sending data...")
        val putDataMapReq = PutDataMapRequest.create(DATA_PATH)
        putDataMapReq.dataMap.putString("dir", direction)
        putDataMapReq.dataMap.putString("dis", distance)
        putDataMapReq.setUrgent()
        val putDataReq = putDataMapReq.asPutDataRequest()

        Log.d(TAG, "client: " + mGoogleApiClient!!.toString() + "connected:" + mGoogleApiClient!!.isConnected)
        Log.d(TAG, "watchID: " + watchId)

        if (mGoogleApiClient == null) {
            Log.d(TAG, "apiclient == null")
        }
        if (watchId == null) {
            Log.d(TAG, "watchid == null")
        }
        val pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq)

        pendingResult.setResultCallback { result ->
            if (result.status.isSuccess) {
                Log.d(TAG, "Data item set: " + result.dataItem.uri)
                //Messeage an Uhr senden (starten der App)
                //Wearable.MessageApi.sendMessage(mGoogleApiClient, watchId, DATA_PATH, OPEN_NAV_CMD.getBytes());
            } else {
                Log.d(TAG, "fail sending Data to watch")
            }
        }
    }

    //transform given Bitmap to Asset
    private fun createAssetFromBitmap(bitmap: Bitmap): Asset {
        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
        return Asset.createFromBytes(byteStream.toByteArray())
    }


}
