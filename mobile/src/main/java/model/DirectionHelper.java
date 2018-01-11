package model;


import android.content.Context;
import android.content.pm.LabeledIntent;
import android.util.Log;

import com.example.de.testssapplication.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DirectionHelper {

    private OkHttpClient client;
    private static DirectionHelper helper;
    private DataModel model;
    private GoogleMap mMap;

    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;

    private DirectionHelper(GoogleMap map){
        client = new OkHttpClient();
        model = DataModel.getInstance();
        mMap = map;
    }

    public static DirectionHelper getInstance(GoogleMap map){
        if (helper == null){
            helper = new DirectionHelper(map);
        }
        return helper;
    }

    public void addPolylineDirection(Context context, List<POI> pois){
        mMap.clear();
        for (int i = 0; i < pois.size()-1; i++){
            POI p1 = pois.get(i);
            POI p2 = pois.get(i+1);
            PolylineOptions p = getPolylineDirection(context, p1.getLatitude(), p1.getLongitude(),
                    p2.getLatitude(), p2.getLongitude());
            Polyline polyline1 = mMap.addPolyline(p);
            polyline1.setEndCap(new RoundCap());
            polyline1.setWidth(POLYLINE_STROKE_WIDTH_PX);
            polyline1.setColor(COLOR_GREEN_ARGB);
            polyline1.setJointType(JointType.ROUND);
            Marker currentPOI = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(p2.getLatitude(), p2.getLongitude())).title(p2.getName()+
                            "\nRating: "+
                            p2.getRating() + "\n"+ p2.getVicinity()));
            currentPOI.showInfoWindow();
        }

        // If the user selected a round tour a polyline from the last to first POI is added.
        Log.d("DirectionHelper", "ROUNDTOUR " + DataModel.getInstance().getTourConfiguration().isRoundTour());
        if (DataModel.getInstance().getTourConfiguration().isRoundTour()) {
            POI p1 = pois.get(pois.size()-1);
            POI p2 = pois.get(0);
            PolylineOptions p = getPolylineDirection(context, p1.getLatitude(), p1.getLongitude(),
                    p2.getLatitude(), p2.getLongitude());
            Polyline polyline = mMap.addPolyline(p);
            polyline.setEndCap(new RoundCap());
            polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
            polyline.setColor(COLOR_GREEN_ARGB);
            polyline.setJointType(JointType.ROUND);
        }
    }

    private PolylineOptions getPolylineDirection(Context context, double startLat, double
            startLng,
                                                double endLat, double endLng){
        String direction_url = "https://maps.googleapis.com/maps/api/directions/json?origin="+
                startLat+","+startLng+"&destination="+endLat+","+endLng+"&key="+
                context.getString(R.string.google_maps_key)+"&mode=walking";
        Request request = new Request.Builder().url(direction_url).build();
        Response response = null;
        JSONObject resp = null;
        JSONArray routesJsonArray = null;
        PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
        try {
            response = client.newCall(request).execute();
            resp = new JSONObject(response.body().string().trim());
            routesJsonArray = resp.getJSONArray("routes");
            JSONObject route =  routesJsonArray.getJSONObject(0);
            JSONArray steps = route.getJSONArray("legs").
                    getJSONObject(0).getJSONArray("steps");
            for (int i = 0; i < steps.length(); i++) {
                //start location
                JSONObject start_location = steps.getJSONObject(i).getJSONObject("start_location");
                double lat1 = start_location.getDouble("lat");
                double lng1 = start_location.getDouble("lng");
                polylineOptions.add(new LatLng(lat1, lng1));
            }
            polylineOptions.add(new LatLng(endLat, endLng));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polylineOptions;
    }

    public List<POI> makeTours(){
        List<POI> selectedPois = model.getSelectedPOIs();
        Log.e(getClass().getSimpleName(), String.valueOf(selectedPois.size()));
        List<POI> tour = new ArrayList<>();
        List<POI> pois = new ArrayList<>();
        pois.addAll(selectedPois);
        POI start = new POI();
        start.setLatitude(model.getLastLocation().getLatitude());
        start.setLongitude(model.getLastLocation().getLongitude());
        tour.add(start);
        findNext(start, tour, pois);
        return tour;
    }

    private void findNext( POI start, List<POI> tour, List<POI> pois){
        if (pois.size() == 0) return;
        double length = Double.MAX_VALUE;
        POI currentPoi = null;
        for (POI poi: pois){
            Log.e(getClass().getSimpleName(), poi.toString());
            if (poi.getLatitude() != start.getLatitude() && poi.getLongitude() != start.getLongitude()){
                double currentDis = LatLngTool.distance(new com.javadocmd.simplelatlng.LatLng(poi.getLatitude(),
                        poi.getLongitude()), new com.javadocmd.simplelatlng.LatLng(start
                        .getLatitude(), start.getLongitude()), LengthUnit.METER);
                if ( currentDis < length){
                    length = currentDis;
                    currentPoi = poi;

                }
            }
        }
        tour.add(currentPoi);
        pois.remove(currentPoi);
        findNext(currentPoi, tour, pois);
    }
}
