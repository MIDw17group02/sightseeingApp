package com.example.de.testssapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeApi
import com.google.android.gms.wearable.Wearable

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.concurrent.ExecutionException

import network.WatchNotifier

class TestWatchSending : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    //DataApi-Variables
    private var mGoogleApiClient: GoogleApiClient? = null
    private var watchId = ""
    private val wn: WatchNotifier? = null

    var dirButton: Button? = null
    var poiButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_watch_sending)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        //GoogleApiClient zum syncen der Daten zwischen Handy und Uhr
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()

        dirButton = findViewById<View>(R.id.direction) as Button
        poiButton = findViewById<View>(R.id.poi) as Button
        //Node-ID der Uhr suchen (momentan basierend auf dem Namen)
        Log.d(TAG, "Searching for connected Devices ...")
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback { getConnectedNodesResult ->
            for (node in getConnectedNodesResult.nodes) {
                Log.d(TAG, "ConnectedDevice '" + node.displayName + "', NodeId = " + node.id)

                if (node.displayName.equals("Moto 360 26CX", ignoreCase = true)) {
                    watchId = node.id
                    Log.d(TAG, "Watch found and assigned! (" + node.id + ")")
                }
            }
        }
        //class for sending
        WatchNotifier.setGoogleApiClient(mGoogleApiClient!!)
        WatchNotifier.setWatchId(watchId)

        poiButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val name = "Heldenstatue"
                val info = "Das Hermannsdenkmal ist eine Kolossalstatue in der Nähe von Hiddesen südwestlich von Detmold in Nordrhein-Westfalen im südlichen Teutoburger Wald. Es wurde zwischen 1838 und 1875 nach Entwürfen von Ernst von Bandel erbaut und am 16. August 1875 eingeweiht.\n" +
                        "\n" +
                        "Das Denkmal soll an den Cheruskerfürsten Arminius erinnern, insbesondere an die sogenannte Schlacht im Teutoburger Wald, in der germanische Stämme unter seiner Führung den drei römischen Legionen XVII, XVIII und XIX unter Publius Quinctilius Varus im Jahre 9 eine entscheidende Niederlage beibrachten.\n" +
                        "\n" +
                        "Mit einer Figurhöhe von 26,57 Metern und einer Gesamthöhe von 53,46 Metern ist es die höchste Statue Deutschlands und war von 1875 bis zur Erbauung der Freiheitsstatue 1886 die höchste Statue der westlichen Welt."
                val url = "http://vignette3.wikia.nocookie.net/goanimate-v2/images/7/77/Mrhappy0902_468x442.jpg"
                var bitmap: Bitmap? = null
                try {
                    bitmap = MyTask().execute(url).get()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }

                WatchNotifier.sendInfoData(bitmap!!, name, info)
            }
        })

        dirButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val direction = "rechts"
                val distance = "100m"
                WatchNotifier.sendNavData(direction, distance)
            }
        })
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {

    }

    private inner class MyTask : AsyncTask<String, String, Bitmap>() {
        override fun doInBackground(vararg params: String): Bitmap? {
            val src = params[0]
            try {
                val url = java.net.URL(src)
                val connection = url
                        .openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                return BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }

        }

        override fun onPostExecute(result: Bitmap) {
            super.onPostExecute(result)
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume: connecting GoogleApiClient")
        mGoogleApiClient!!.connect()
    }

    override fun onPause() {
        super.onPause()

        Log.d(TAG, "onPause: disconnecting GoogleApiClient & removing GoogleApiListener")
        Wearable.DataApi.removeListener(mGoogleApiClient, this)
        mGoogleApiClient!!.disconnect()
    }

    override fun onConnected(bundle: Bundle?) {
        Log.d(TAG, "onConnected: adding GoogleApiListener")

        Wearable.DataApi.addListener(mGoogleApiClient, this)
    }

    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "onConnectionSuspended: " + i)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        var str = ""
        if (connectionResult != null) {
            str = connectionResult.toString()
        }
        Log.d(TAG, "onConnectionFailed: " + str)
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer?) {
        var str = ""
        if (dataEventBuffer != null) {
            str = "Received Buffersize = " + dataEventBuffer.count
        }
        Log.d(TAG, "onDataChanged: " + str)
    }

    companion object {

        //Log-Tag
        var TAG = "Phone-TestWatch"

        private val DATA_PATH = "/watch_data"
        private val OPEN_NAV_CMD = "open-nav-app"
        private val OPEN_INFO_CMD = "open-info-app"

        private fun createAssetFromBitmap(bitmap: Bitmap): Asset {
            val byteStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
            return Asset.createFromBytes(byteStream.toByteArray())
        }
    }

}
