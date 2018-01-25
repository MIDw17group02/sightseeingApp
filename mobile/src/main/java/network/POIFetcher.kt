package network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log

import com.example.de.testssapplication.R

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.io.InputStream

import model.DataModel
import model.POI
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Class for creating network requests.
 * This class only implements static methods.
 * Results of Requests are stored to the Singleton instance of the Data Model class.
 *
 */
object POIFetcher {

    /**
     * Send a http request to the Google API server.
     * Request all POIs in a given radius[m] around the last location stored in DataModel.
     * The result will be stored to the DataModel.
     * @param context Application Context for execution
     * @param radius  Search radius in meter
     */
    fun requestPOIs(context: Context, radius: Int) {
        val model = DataModel.instance
        val location = model.getLastLocation()
        if (location != null) {
            Log.e("fafafafa", "location not null")
            requestPOIs(context, location.latitude, location.longitude, radius)
        }
    }

    @Deprecated("")
            /**
             * Send a http request to the google api server.
             * Request all POIs in a given radius[m] around the given position.
             * The result will be stored to the DataModel.
             * Please keep in mind that requests with a big radius may take a while.
             * @param context   Application Context for execution
             * @param latitude  Coordinate Lat
             * @param longitude Coordinate Long
             * @param radius    Search radius in meter
             */
    fun requestPOIs(context: Context, latitude: Double, longitude: Double, radius: Int) {

        val mLoc = Location("mLoc")
        mLoc.latitude = latitude
        mLoc.longitude = longitude

        /*String poiURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + String.valueOf(latitude) + "," + String.valueOf(longitude) +
                "&radius=" +String.valueOf(radius) +
                "&types=park|museum" + "&rankBy.Distance" +
                "&key=" + context.getString(R.string.google_maps_key);*/
        val poiURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + latitude.toString() + "," + longitude.toString() +
                "&radius=" + radius.toString() +
                "&keyword=attraction" + "&rankBy.Distance" + "&language=de" +
                "&key=" + context.getString(R.string.google_maps_key)
        Log.d("Phone-WikiFetch", poiURL)

        val client = OkHttpClient()
        val request = Request.Builder().url(poiURL).build()
        var response: Response? = null
        DataModel.instance.clearPOIs()

        try {
            response = client.newCall(request).execute()
            //TODO React to ZERO RESULTS
            val jsonObjectToken = JSONObject(response!!.body().string().trim { it <= ' ' })
            val places = jsonObjectToken.getJSONArray("results")

            for (i in 0 until places.length()) {
                val placeJSON = places.getJSONObject(i)
                Log.d("TAG", placeJSON.toString())
                val poi = getPOIFromJSON(context, placeJSON)
                Log.d("POIFetcher", "Fetched POI " + poi.name!!)

                val poiLoc = Location("poiLoc")
                poiLoc.latitude = poi.latitude
                poiLoc.longitude = poi.longitude
                poi.distanceToStart = mLoc.distanceTo(poiLoc) / 1000.0

                val model = DataModel.instance
                if (poi.photo != null) model.addPOI(poi) // Better for Showcase
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * Parse the POI data from a JSON Objekt.
     * Also Download photo if it is referenced and store it in the POI.
     * @param context  The executing Application Context.
     * @param poiJSON  The POI JSON that should be transferred.
     * @return         The POI Object from the JSON
     */
    private fun getPOIFromJSON(context: Context, poiJSON: JSONObject): POI {

        val result = POI()

        try {
            if (poiJSON.has("geometry")) {
                val geometry = poiJSON.getJSONObject("geometry")
                if (geometry.has("location")) {
                    val location = geometry.getJSONObject("location")
                    if (location.has("lat")) result.latitude = location.getDouble("lat")
                    if (location.has("lng")) result.longitude = location.getDouble("lng")
                }
            }
            if (poiJSON.has("id")) result.id = poiJSON.getString("id")
            if (poiJSON.has("name")) {
                result.name = poiJSON.getString("name")
                result.startWikiFetchTask()
            }
            if (poiJSON.has("place_id")) result.place_id = poiJSON.getString("place_id")
            if (poiJSON.has("vicinity")) result.vicinity = poiJSON.getString("vicinity")
            if (poiJSON.has("rating")) result.rating = poiJSON.getDouble("rating")

            if (poiJSON.has("photos")) {
                val photos = poiJSON.getJSONArray("photos")
                for (i in 0 until photos.length()) {
                    val photo = photos.getJSONObject(i)
                    if (photo.has("photo_reference")) {
                        val photoReference = photo.getString("photo_reference")
                        val photoURL = "https://maps.googleapis.com/maps/api/place/photo?" +
                                "maxwidth=600&" +
                                "photoreference=" + photoReference +
                                "&key=" + context.getString(R.string.google_maps_key)

                        val photoClient = OkHttpClient()
                        val request = Request.Builder().url(photoURL).build()
                        var response: Response? = null

                        try {
                            response = photoClient.newCall(request).execute()
                            val `is` = response!!.body().byteStream()
                            val bm = BitmapFactory.decodeStream(`is`)
                            result.photo = bm
                            break
                        } catch (e: IOException) {
                            Log.d("getPOIFromJSON", "Could not download photo!")
                            e.printStackTrace()
                        }

                    }
                }
            }

        } catch (e: JSONException) {
            Log.d("getPOIFromJSON", "Parsing error!")
            e.printStackTrace()
        }

        return result
    }

}
