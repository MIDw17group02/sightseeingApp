package network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import com.example.de.testssapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import model.DataModel;
import model.POI;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Class for creating network requests.
 * This class only implements static methods.
 * Results of Requests are stored to the Singleton instance of the Data Model class.
 *
 */
public class POIFetcher {

    /**
     * Send a http request to the Google API server.
     * Request all POIs in a given radius[m] around the last location stored in DataModel.
     * The result will be stored to the DataModel.
     * @param context Application Context for execution
     * @param radius  Search radius in meter
     */
    public static void requestPOIs(Context context, int radius) {
        DataModel model = DataModel.getInstance();
        Location location = model.getLastLocation();
        if (location != null) {
            Log.e("fafafafa","location not null");
            requestPOIs(context, location.getLatitude(), location.getLongitude(), radius);
        }
    }

    @Deprecated
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
    public static void requestPOIs(final Context context, double latitude, double longitude, int radius) {

        Location mLoc = new Location("mLoc");
        mLoc.setLatitude(latitude);
        mLoc.setLongitude(longitude);

        /*String poiURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + String.valueOf(latitude) + "," + String.valueOf(longitude) +
                "&radius=" +String.valueOf(radius) +
                "&types=park|museum" + "&rankBy.Distance" +
                "&key=" + context.getString(R.string.google_maps_key);*/
        String poiURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + String.valueOf(latitude) + "," + String.valueOf(longitude) +
                "&radius=" +String.valueOf(radius) +
                "&keyword=attraction" + "&rankBy.Distance" + "&language=de" +
                "&key=" + context.getString(R.string.google_maps_key);
        Log.d("Phone-WikiFetch", poiURL);

        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(poiURL).build();
        Response response = null;
        DataModel.getInstance().clearPOIs();

        try {
            response = client.newCall(request).execute();
            //TODO React to ZERO RESULTS
            JSONObject jsonObjectToken = new JSONObject(response.body().string().trim());
            JSONArray places = jsonObjectToken.getJSONArray("results");

            for (int i = 0; i < places.length(); i++) {
                JSONObject placeJSON = places.getJSONObject(i);
                Log.d("TAG", placeJSON.toString());
                POI poi = getPOIFromJSON(context, placeJSON);
                Log.d("POIFetcher", "Fetched POI " + poi.getName());

                Location poiLoc = new Location("poiLoc");
                poiLoc.setLatitude(poi.getLatitude());
                poiLoc.setLongitude(poi.getLongitude());
                poi.setDistanceToStart(mLoc.distanceTo(poiLoc)/1000);

                DataModel model = DataModel.getInstance();
                if (poi.getPhoto() != null && poi.getInfoText() != null) // Better for Showcase
                    model.addPOI(poi);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Async
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
               e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                //TODO React to ZERO RESULTS
                Log.d("POIFETCHER", response.toString());
                JSONObject jsonObjectToken = null;
                try {
                    jsonObjectToken = new JSONObject(response.body().string().trim());
                    JSONArray places = jsonObjectToken.getJSONArray("results");

                    for (int i = 0; i < places.length(); i++) {
                        JSONObject placeJSON = places.getJSONObject(i);
                        POI poi = getPOIFromJSON(context, placeJSON);
                        Log.d("POIFetcher", "Fetched POI " + poi.getName());
                        DataModel model = DataModel.getInstance();
                        Log.d("POIFetcher", model.toString());
                        model.addPOI(poi);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    /**
     * Parse the POI data from a JSON Objekt.
     * Also Download photo if it is referenced and store it in the POI.
     * @param context  The executing Application Context.
     * @param poiJSON  The POI JSON that should be transferred.
     * @return         The POI Object from the JSON
     */
    private static POI getPOIFromJSON(Context context, JSONObject poiJSON) {

        POI result = new POI();

        try {
            if (poiJSON.has("geometry")) {
                JSONObject geometry = poiJSON.getJSONObject("geometry");
                if (geometry.has("location")) {
                    JSONObject location = geometry.getJSONObject("location");
                    if (location.has("lat")) result.setLatitude(location.getDouble("lat"));
                    if (location.has("lng")) result.setLongitude(location.getDouble("lng"));
                }
            }
            if (poiJSON.has("id")) result.setId(poiJSON.getString("id"));
            if (poiJSON.has("name")) result.setName(poiJSON.getString("name"));
            if (poiJSON.has("place_id")) result.setPlace_id(poiJSON.getString("place_id"));
            if (poiJSON.has("vicinity")) result.setVicinity(poiJSON.getString("vicinity"));
            if (poiJSON.has("rating")) result.setRating(poiJSON.getDouble("rating"));

            if (poiJSON.has("photos")) {
                JSONArray photos = poiJSON.getJSONArray("photos");
                for (int i = 0; i < photos.length(); i++) {
                    JSONObject photo = photos.getJSONObject(i);
                    if (photo.has("photo_reference")) {
                        String photoReference = photo.getString("photo_reference");
                        String photoURL = "https://maps.googleapis.com/maps/api/place/photo?" +
                                "maxwidth=600&" +
                                "photoreference=" + photoReference +
                                "&key=" + context.getString(R.string.google_maps_key);

                        OkHttpClient photoClient = new OkHttpClient();
                        Request request = new Request.Builder().url(photoURL).build();
                        Response response = null;

                        try {
                            response = photoClient.newCall(request).execute();
                            InputStream is = response.body().byteStream();
                            Bitmap bm = BitmapFactory.decodeStream(is);
                            result.setPhoto(bm);

                            break;
                        } catch (IOException e) {
                            Log.d("getPOIFromJSON", "Could not download photo!");
                            e.printStackTrace();
                        }
                    }
                }
            }

            //TODO wikipedia api fetch -> Problem: Oft keine oder mehrere Einträge
            if (poiJSON.has("name")) {
                String name = poiJSON.getString("name");
                //name = name.replaceAll(" ", "+");
                boolean gotResult = false;
                String infoText = "";

                while((!name.equals("")) && !gotResult) {
                    String wikiURL = "https://de.wikipedia.org/w/api.php?action=opensearch&search=" + name + "&limit=2&namespace=0&redirects=resolve&format=json";
                    OkHttpClient wikiClient = new OkHttpClient();
                    final Request request = new Request.Builder().url(wikiURL).build();
                    Response response = null;

                    try {
                        response = wikiClient.newCall(request).execute();
                        //TODO React to ZERO RESULTS
                        JSONArray jsonObjectToken = new JSONArray(response.body().string().trim());
                        JSONArray infoTextAarray = jsonObjectToken.getJSONArray(2);
                        if (infoTextAarray.optString(1).equals("")) {
                            //wenn nur ein Eintrag vorhanden ist
                            infoText = infoTextAarray.optString(0);
                        } else {
                            //wenn mehrer Einträge vorhanden sind -> immer Begriffserklärungsseite?!
                            infoText = infoTextAarray.optString(1);
                        }
                        if (infoText.equals("")) {
                            //kein InfoText zum Suchbegriff gefunden
                            if(!name.contains(" ")) {
                                //kein Leerzeichen mehr vorhanden
                                name = "";
                            } else {
                                String n[] = name.split(" ");
                                Log.d("Phone-WikiFetch", "length:" + n.length);
                                String newname = "";
                                int i;
                                for (i = 0; i < (n.length - 2); i++) {
                                    newname += n[i] + " ";
                                }
                                newname += n[i];
                                name = newname;
                            }
                            Log.d("Phone-WikiFetch", "new name:" + name);
                        } else {
                            Log.d("Phone-WikiFetch", "got result!");
                            gotResult = true;
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(gotResult == true) {
                    Log.d("Phone-WikiFetch", "InfoText zu " + name + ": " + infoText);
                    result.setInfoText(infoText);
                }
                else {
                    Log.d("Phone-WikiFetch", "No Info Found for " + name);
                    result.setInfoText(null);
                    //those will be deleted, by setting null
                }

            }

        } catch (JSONException e) {
            Log.d("getPOIFromJSON", "Parsing error!");
            e.printStackTrace();
        }

        return result;
    }

}
