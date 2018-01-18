package model;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * POI representation class.
 * This class wraps up content of a POI.
 * It implements the Comparable Interface and is compared based on the distance to the
 * current location.
 */
public class POI implements Comparable {

    private String name;
    private double longitude;
    private double latitude;
    private double rating = -1.0;   // Google rating from 0.0 to 5.0 stars.
    private String vicinity; // = Address of the poi
    private Bitmap photo;    // Bitmap of a photo that is queried from the google place.

    private String id;
    private String place_id;
    private String reference;

    private String infoText;
    private WikipediaFetchTask wikipediaFetchTask;

    private boolean visited = false;

    private int index;

    // TODO update this usage by using the location!
    private double distance_to_start = -1.0; // Distance of the POI in kilo meters to the current position.

    // Indicate if the user wants to visit this POI.
    private boolean selected = false;

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof POI)) {
            return false;
        }

        POI poi2 = (POI) o;
        if (id != poi2.getId()) {
            return false;
        }
        if (place_id != poi2.getPlace_id()) {
            return false;
        }

        return true;
    }

    public double getDistanceToStart() {
        return distance_to_start;
    }

    public void setDistanceToStart(double distance_to_start) {
        this.distance_to_start = distance_to_start;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setIndex(int index) {this.index = index;}

    public int getIndex() {return index;}

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    @Override
    public int compareTo(@NonNull Object o) {

        POI other = (POI) o;
        if (getDistanceToStart() < other.getDistanceToStart()) {
            return -1;
        } else if(getDistanceToStart() > other.getDistanceToStart()) {
            return 1;
        }

        return 0;
    }

    public void startWikiFetchTask() {
        wikipediaFetchTask = new WikipediaFetchTask();
        wikipediaFetchTask.execute(name);
    }

    private class WikipediaFetchTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            String name = strings[0];
            if (name == null) {
                return null;
            }

            boolean gotResult = false;
            String wikiResult = "";

            while((!name.equals("")) && !gotResult) {

                String wikiURL = "https://de.wikipedia.org/w/api.php?action=opensearch&search=" + name + "&limit=2&namespace=0&redirects=resolve&format=json";
                OkHttpClient wikiClient = new OkHttpClient();
                final Request request = new Request.Builder().url(wikiURL).build();
                Response response = null;

                try {
                    response = wikiClient.newCall(request).execute();
                    JSONArray jsonObjectToken = new JSONArray(response.body().string().trim());
                    JSONArray infoTextArray = jsonObjectToken.getJSONArray(2);

                    if (infoTextArray.optString(1).equals("")) {
                        //wenn nur ein Eintrag vorhanden ist
                        wikiResult = infoTextArray.optString(0);
                    } else {
                        //wenn mehrer Einträge vorhanden sind -> immer Begriffserklärungsseite?!
                        wikiResult = infoTextArray.optString(1);
                    }

                    if (wikiResult.equals("")) {
                        //kein InfoText zum Suchbegriff gefunden
                        if(!name.contains(" ")) {
                            //kein Leerzeichen mehr vorhanden
                            name = "";
                        } else {
                            String n[] = name.split(" ");
                            Log.d("Async-WikiFetch", "length:" + n.length);
                            String newname = "";
                            int i;
                            for (i = 0; i < (n.length - 2); i++) {
                                newname += n[i] + " ";
                            }
                            newname += n[i];
                            name = newname;
                        }
                        Log.d("Async-WikiFetch", "new name:" + name);
                    } else {
                        Log.d("Async-WikiFetch", "got result!");
                        gotResult = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(gotResult) {
                Log.d("Async-WikiFetch", "InfoText zu " + name + ": " + wikiResult);
                infoText = wikiResult;
            } else {
                Log.d("Async-WikiFetch", "No Info Found for " + name);
            }

            return null;
        }

    }
}
