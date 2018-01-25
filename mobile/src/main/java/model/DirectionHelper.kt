package model


import android.content.Context
import android.content.pm.LabeledIntent
import android.location.Location
import android.util.Log

import com.example.de.testssapplication.R
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.util.ArrayList

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class DirectionHelper private constructor(private val mMap: GoogleMap) {

    private val client: OkHttpClient
    private val model: DataModel
    private val className = javaClass.simpleName

    init {
        client = OkHttpClient()
        model = DataModel.instance

    }

    fun addPolylineDirection(context: Context, pois: List<POI>) {
        mMap.clear()
        for (i in 0 until pois.size - 1) {
            val p1 = pois[i]
            val p2 = pois[i + 1]
            val p = getPolylineDirection(context, p1.latitude, p1.longitude,
                    p2.latitude, p2.longitude)
            val polyline1 = mMap.addPolyline(p)
            polyline1.endCap = RoundCap()
            polyline1.width = POLYLINE_STROKE_WIDTH_PX.toFloat()
            polyline1.color = COLOR_GREEN_ARGB
            polyline1.jointType = JointType.ROUND
            val currentPOI = mMap.addMarker(MarkerOptions()
                    .position(LatLng(p2.latitude, p2.longitude)).title(p2.name +
                    "\nRating: " +
                    p2.rating + "\n" + p2.vicinity))
            if (i == 0) currentPOI.showInfoWindow()
        }

        // If the user selected a round tour a polyline from the last to first POI is added.
        Log.d("DirectionHelper", "ROUNDTOUR " + DataModel.instance.tourConfiguration!!.isRoundTour)
        if (DataModel.instance.tourConfiguration!!.isRoundTour) {
            val p1 = pois[pois.size - 1]
            val p2 = pois[0]
            val p = getPolylineDirection(context, p1.latitude, p1.longitude,
                    p2.latitude, p2.longitude)
            val polyline = mMap.addPolyline(p)
            polyline.endCap = RoundCap()
            polyline.width = POLYLINE_STROKE_WIDTH_PX.toFloat()
            polyline.color = COLOR_GREEN_ARGB
            polyline.jointType = JointType.ROUND
        }
    }

    private fun getPolylineDirection(context: Context, startLat: Double, startLng: Double,
                                     endLat: Double, endLng: Double): PolylineOptions {
        val direction_url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                startLat + "," + startLng + "&destination=" + endLat + "," + endLng + "&key=" +
                context.getString(R.string.google_maps_key) + "&mode=walking"
        val request = Request.Builder().url(direction_url).build()
        var response: Response? = null
        var resp: JSONObject? = null
        var routesJsonArray: JSONArray? = null
        val polylineOptions = PolylineOptions().clickable(true)
        try {
            response = client.newCall(request).execute()
            resp = JSONObject(response!!.body().string().trim { it <= ' ' })
            routesJsonArray = resp.getJSONArray("routes")
            val route = routesJsonArray!!.getJSONObject(0)
            val steps = route.getJSONArray("legs").getJSONObject(0).getJSONArray("steps")
            for (i in 0 until steps.length()) {
                //start location
                val start_location = steps.getJSONObject(i).getJSONObject("start_location")
                val lat1 = start_location.getDouble("lat")
                val lng1 = start_location.getDouble("lng")
                polylineOptions.add(LatLng(lat1, lng1))
            }
            polylineOptions.add(LatLng(endLat, endLng))
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return polylineOptions
    }

    fun makeTours(): List<POI> {
        val selectedPois = model.selectedPOIs
        Log.e(javaClass.simpleName, selectedPois.size.toString())
        val tour = ArrayList<POI>()
        val pois = ArrayList<POI>()
        pois.addAll(selectedPois)
        val start = POI()
        start.latitude = model.getLastLocation()!!.latitude
        start.longitude = model.getLastLocation()!!.longitude
        tour.add(start)
        findNext(start, tour, pois)
        return tour
    }

    private fun findNext(start: POI?, tour: MutableList<POI>, pois: MutableList<POI>) {
        if (pois.size == 0) return
        var length = java.lang.Double.MAX_VALUE
        var currentPoi: POI? = null
        for (poi in pois) {
            Log.e(javaClass.simpleName, poi.toString())
            if (poi.latitude != start!!.latitude && poi.longitude != start.longitude) {
                val currentDis = LatLngTool.distance(com.javadocmd.simplelatlng.LatLng(poi.latitude,
                        poi.longitude), com.javadocmd.simplelatlng.LatLng(start
                        .latitude, start.longitude), LengthUnit.METER)
                if (currentDis < length) {
                    length = currentDis
                    currentPoi = poi

                }
            }
        }
        tour.add(currentPoi!!)
        pois.remove(currentPoi)
        findNext(currentPoi, tour, pois)
    }

    /**
     * check if poi is reached
     * @param poi
     * @return
     */
    private fun isPOIReached(poi: POI): Boolean {
        val lastKnownLocation = model.getLastLocation()
        return if (LatLngTool.distance(com.javadocmd.simplelatlng.LatLng(lastKnownLocation!!.latitude,
                        lastKnownLocation.longitude),
                        com.javadocmd.simplelatlng.LatLng(poi.latitude, poi.longitude),
                        // todo distance to poi
                        LengthUnit.METER) <= 10) {
            true
        } else false
    }

    /**
     * update visited pois
     */
    fun updateVisitedPOIs() {
        val lastKnownLocation = model.getLastLocation()
        val selectedPOIs = model.selectedPOIs
        val visitedPOIs :MutableList<POI>? = model.visitedPOIs
        for (poi in selectedPOIs) {
            if (isPOIReached(poi) && !visitedPOIs!!.contains(poi)) {
                visitedPOIs.add(poi)
                model.visitedPOIs = visitedPOIs
            }
        }
    }

    /**
     * calculate the next direction to show on both phone and watch screen
     * @return
     */
    fun nextDirection(context: Context): String {
        val lastKnownLocation = model.getLastLocation() ?: return "undefined"
        val selectedPOIs = model.selectedPOIs
        val visitedPOIs = model.visitedPOIs
        var next = selectedPOIs[0]
        //determine next poi to visit
        for (poi in selectedPOIs) {
            if (!visitedPOIs!!.contains(poi)) {
                next = poi
            }
        }
        val direction_url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                lastKnownLocation.latitude + "," + lastKnownLocation.longitude + "&destination=" + next.latitude + "," + next.longitude + "&key=" +
                context.getString(R.string.google_maps_key) + "&mode=walking"
        val request = Request.Builder().url(direction_url).build()
        var response: Response? = null
        var resp: JSONObject? = null
        var routesJsonArray: JSONArray? = null
        try {
            response = client.newCall(request).execute()
            resp = JSONObject(response!!.body().string().trim { it <= ' ' })
            routesJsonArray = resp.getJSONArray("routes")
            val route = routesJsonArray!!.getJSONObject(0)
            val steps = route.getJSONArray("legs").getJSONObject(0).getJSONArray("steps")
            var instruction = steps.getJSONObject(0).getString("html_instructions")
            val distanceObject = steps.getJSONObject(0).getJSONObject("distance").getString("text")
            instruction = (instruction + " in " + distanceObject).replace("<b>", "").replace("</b>", "")
            Log.e(className, instruction)
            return instruction
            // todo ????
            /*
            if (instruction.contains("Left") || instruction.contains("right")){
                return "left";
            }
            else if (instruction.contains("Right") || instruction.contains("right")){
                return "right";
            }
            else if (instruction.contains("Head") || instruction.contains("head") ){
                return "head";
            }
            else{
                return "undefined";
            }
            */
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return "undefined"
    }

    companion object {
        private var helper: DirectionHelper? = null
        private val COLOR_GREEN_ARGB = -0xc771c4
        private val POLYLINE_STROKE_WIDTH_PX = 12

        fun getInstance(map: GoogleMap): DirectionHelper {
            if (helper == null) {
                helper = DirectionHelper(map)
            }
            return helper!!
        }
    }

}
