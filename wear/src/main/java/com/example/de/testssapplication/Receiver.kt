package com.example.de.testssapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataItemBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

import java.io.InputStream
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * Created by Justin on 06.12.2017.
 */
class Receiver : WearableListenerService() {

    private var mGoogleApiClient: GoogleApiClient? = null


    fun connectGoogleApiClient(): Boolean {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build()
        val connectionResult = mGoogleApiClient!!.blockingConnect(30, TimeUnit.SECONDS)
        if (!connectionResult.isSuccess) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.")
            return false
        }

        return true
    }

    /*
     * fetches info data from dataapi
     */
    fun fetchInfoAndStartActivity() {
        val results = Wearable.DataApi.getDataItems(mGoogleApiClient)
        results.setResultCallback { dataItems ->
            if (dataItems.count != 0) {

                for (item in dataItems) {
                    if (item.uri.path == DATA_PATH_POI) {
                        val dataMap = DataMapItem.fromDataItem(item).dataMap
                        Log.d("WATCH", dataMap.toString())

                        //name und info auslesen
                        val name = dataMap.getString("name")
                        val info = dataMap.getString("info")

                        Log.d(TAG, "fetchData(): Name = '$name' Info = '$info'")

                        //bild
                        val profileAsset = dataMap.getAsset("image")
                        var bitmap: Bitmap? = null
                        try {
                            bitmap = MyTask().execute(profileAsset).get()
                        } catch (e: IllegalArgumentException) {
                            e.printStackTrace()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        }

                        bitmap = scaleDownBitmap(bitmap!!, 100, applicationContext)
                        Log.d(TAG, "Resized Bitmap ?!")


                        //start app and pass navigation data
                        val intent = Intent(this@Receiver, POINotifier::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("image", bitmap)
                        intent.putExtra("name", name)
                        intent.putExtra("info", info)


                        Log.d(TAG, "starting watch activity")
                        startActivity(intent)
                    }
                }
            } else {
                Log.d(TAG, "fetchData(): no data")
            }

            dataItems.release()
        }
    }

    /*
     * fetches navigation data from dataapi
     */
    fun fetchNavAndStartActivity() {
        val results = Wearable.DataApi.getDataItems(mGoogleApiClient)
        results.setResultCallback { dataItems ->
            if (dataItems.count != 0) {

                for (item in dataItems) {
                    if (item.uri.path == DATA_PATH) {
                        val dataMap = DataMapItem.fromDataItem(item).dataMap

                        //name und info auslesen
                        val direction = dataMap.getString("dir")
                        val distance = dataMap.getString("dis")
                        Log.d(TAG, "fetchData(): Direction = '$direction' Distance = '$distance'")

                        //start app and pass navigation data
                        val intent = Intent(this@Receiver, DirectionNotifier::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("dir", direction)
                        intent.putExtra("dis", distance)

                        Log.d(TAG, "starting watch activity")
                        startActivity(intent)
                    }
                }
            } else {
                Log.d(TAG, "fetchData(): no data")
            }

            dataItems.release()
        }
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer?) {
        Log.d(TAG, "data Changed")
        super.onDataChanged(dataEventBuffer)

        connectGoogleApiClient()

        if (dataEventBuffer != null) {
            Log.d(TAG, "onDataChanged: count = " + dataEventBuffer.count)

            for (event in dataEventBuffer) {
                Log.d(TAG, "onDataChanged(): path= '" + event.dataItem.uri.path + "'")

                if (event.dataItem.uri.path == DATA_PATH) {
                    fetchNavAndStartActivity()
                } else if (event.dataItem.uri.path == DATA_PATH_POI) {
                    fetchInfoAndStartActivity()
                } else {
                    Log.d(TAG, "onDataChanged(): unmapped path='" + event.dataItem.uri.path + "'")
                }
            }
        }

    }

    private inner class MyTask : AsyncTask<Asset, String, Bitmap>() {
        override fun doInBackground(vararg params: Asset): Bitmap? {
            val asset = params[0]
                    ?: //throw new IllegalArgumentException("Asset must be non-null");
                    return null
            val result = mGoogleApiClient!!.blockingConnect(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            if (!result.isSuccess) {
                return null
            }
            // convert asset into a file descriptor and block until it's ready
            val assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().inputStream
            mGoogleApiClient!!.disconnect()

            if (assetInputStream == null) {
                println("Requested an unknown Asset.")
                return null
            }
            // decode the stream into a bitmap
            return BitmapFactory.decodeStream(assetInputStream)
        }

        override fun onPostExecute(result: Bitmap) {
            super.onPostExecute(result)
        }
    }

    fun loadBitmapFromAsset(asset: Asset?): Bitmap? {
        if (asset == null) {
            throw IllegalArgumentException("Asset must be non-null")
        }
        val result = mGoogleApiClient!!.blockingConnect(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        if (!result.isSuccess) {
            return null
        }
        // convert asset into a file descriptor and block until it's ready
        val assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().inputStream
        mGoogleApiClient!!.disconnect()

        if (assetInputStream == null) {
            println("Requested an unknown Asset.")
            return null
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream)
    }

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        super.onMessageReceived(messageEvent)

        if (messageEvent != null) {
            val path = messageEvent.path
            Log.d(TAG, "onMessageReceived. " + path)

            //check if message is meant for this app (a bit overhead, since intent-filter should check for it beforehand)
            if (path == DATA_PATH) {
                //check message, navigation data & open app
                val msg = String(messageEvent.data)
                if (msg == OPEN_INFO_CMD || msg == OPEN_NAV_CMD) {
                    // (this message should only arrive, after navigation data was set)
                    if (mGoogleApiClient == null || !mGoogleApiClient!!.isConnected) {
                        if (!connectGoogleApiClient()) {
                            return
                        }
                    }

                    //make sure newest data is available & open app
                    if (msg == OPEN_INFO_CMD) {

                        //fetchInfoAndStartActivity();
                    } else if (msg == OPEN_NAV_CMD) {
                        //fetchNavAndStartActivity();
                    }

                } else {
                    Log.d(TAG, "$msg != $OPEN_INFO_CMD || $OPEN_NAV_CMD")
                }
            } else {
                Log.d(TAG, path + " != " + DATA_PATH)
            }
        }
    }

    private fun scaleDownBitmap(photo: Bitmap, newHeight: Int, context: Context): Bitmap {
        var photo = photo

        val densityMultiplier = context.resources.displayMetrics.density

        val h = (newHeight * densityMultiplier).toInt()
        val w = (h * photo.width / photo.height.toDouble()).toInt()

        photo = Bitmap.createScaledBitmap(photo, w, h, true)

        return photo
    }

    companion object {
        //Log-Tag
        var TAG = "Wear-Receiver"
        private val TIMEOUT_MS = 200
        private val DATA_PATH = "/watch_data"
        private val DATA_PATH_POI = "/watch_data_poi"
        private val OPEN_NAV_CMD = "open-nav-app"
        private val OPEN_INFO_CMD = "open-info-app"
    }
}
