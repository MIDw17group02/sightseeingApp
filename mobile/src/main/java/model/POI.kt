package model

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log

import org.json.JSONArray
import org.json.JSONException

import java.io.IOException

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * POI representation class.
 * This class wraps up content of a POI.
 * It implements the Comparable Interface and is compared based on the distance to the
 * current location.
 */
class POI : Comparable<POI> {

    var name: String? = null
    var longitude: Double = 0.toDouble()
    var latitude: Double = 0.toDouble()
    var rating = -1.0   // Google rating from 0.0 to 5.0 stars.
    var vicinity: String? = null // = Address of the poi
    var photo: Bitmap? = null    // Bitmap of a photo that is queried from the google place.

    var id: String? = null
    var place_id: String? = null
    var reference: String? = null

    var infoText: String? = null
    private var wikipediaFetchTask: WikipediaFetchTask? = null

    var isVisited = false

    var index: Int = 0

    // TODO update this usage by using the location!
    var distanceToStart = -1.0 // Distance of the POI in kilo meters to the current position.

    // Indicate if the user wants to visit this POI.
    var isSelected = false

    override fun equals(o: Any?): Boolean {

        if (o !is POI) {
            return false
        }

        val poi2 = o as POI?
        if (id !== poi2!!.id) {
            return false
        }
        return if (place_id !== poi2!!.place_id) {
            false
        } else true

    }

    override fun compareTo(o: POI): Int {

        val other = o as POI
        if (distanceToStart < other.distanceToStart) {
            return -1
        } else if (distanceToStart > other.distanceToStart) {
            return 1
        }

        return 0
    }

    fun startWikiFetchTask() {
        wikipediaFetchTask = WikipediaFetchTask()
        wikipediaFetchTask!!.execute(name)
    }

    private inner class WikipediaFetchTask : AsyncTask<String, Void, Void>() {

        override fun doInBackground(vararg strings: String): Void? {

            var name: String? = strings[0] ?: return null

            var gotResult = false
            var wikiResult = ""

            while (name != "" && !gotResult) {

                val wikiURL = "https://de.wikipedia.org/w/api.php?action=opensearch&search=$name&limit=2&namespace=0&redirects=resolve&format=json"
                val wikiClient = OkHttpClient()
                val request = Request.Builder().url(wikiURL).build()
                var response: Response? = null

                try {
                    response = wikiClient.newCall(request).execute()
                    val jsonObjectToken = JSONArray(response!!.body().string().trim { it <= ' ' })
                    val infoTextArray = jsonObjectToken.getJSONArray(2)

                    if (infoTextArray.optString(1) == "") {
                        //wenn nur ein Eintrag vorhanden ist
                        wikiResult = infoTextArray.optString(0)
                    } else {
                        //wenn mehrer Einträge vorhanden sind -> immer Begriffserklärungsseite?!
                        wikiResult = infoTextArray.optString(1)
                    }

                    if (wikiResult == "") {
                        //kein InfoText zum Suchbegriff gefunden
                        if (!name!!.contains(" ")) {
                            //kein Leerzeichen mehr vorhanden
                            name = ""
                        } else {
                            val n = name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            Log.d("Async-WikiFetch", "length:" + n.size)
                            var newname = ""
                            var i: Int
                            i = 0
                            while (i < n.size - 2) {
                                newname += n[i] + " "
                                i++
                            }
                            newname += n[i]
                            name = newname
                        }
                        Log.d("Async-WikiFetch", "new name:" + name)
                    } else {
                        Log.d("Async-WikiFetch", "got result!")
                        gotResult = true
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            if (gotResult) {
                Log.d("Async-WikiFetch", "InfoText zu $name: $wikiResult")
                infoText = wikiResult
            } else {
                Log.d("Async-WikiFetch", "No Info Found for " + name!!)
            }

            return null
        }

    }
}
