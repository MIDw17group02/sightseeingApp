package Fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.de.testssapplication.R;

import java.text.DecimalFormat;

import model.DataModel;
import model.POI;

/**
 * This fragment refers to the List Tab in the POI Selection Activity.
 * It basically implements a Adapter that wraps the POIs of the DataModel single instance.
 * The Adapter is used for displayment of the data in the GridView of the fragment.
 */
public class POIListFragment extends Fragment {

    private DataModel model;
    private POIGridAdapter poiAdapter;
    public FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_list, viewGroup, false);

        model = DataModel.getInstance();
        final GridView gridView = rootView.findViewById(R.id.poi_grid_list);
        poiAdapter = new POIGridAdapter();
        gridView.setAdapter(poiAdapter);

        // React to user clicks on a POI.
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                POI poi = model.getPOI(position);
                poi.setSelected(!poi.isSelected());
                poiAdapter.notifyDataSetChanged();
            }
        });

        return rootView;
    }

    public void notifySelectionChange() {
        poiAdapter.notifyDataSetChanged();
    }

    private class POIGridAdapter extends BaseAdapter {

        public POIGridAdapter() {}

        @Override
        public int getCount() {
            if (fab != null) {
                fab.setForeground(getResources().getDrawable(model.getSelectedPOIs().size() == 0 ? R.drawable.right_arrow_red : R.drawable.right_arrow));
            }
            return model.getPOIcount();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint("ResourceType")
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {

            POI poi = model.getPOI(i);
            View v;

            if (convertView == null) {
                final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                v = layoutInflater.inflate(R.layout.grid_view_poi, null);
            } else {
                v = (View) convertView;
            }

            TextView poiTextView = (TextView) v.findViewById(R.id.poi_grid_name);
            ImageView poiImageView = (ImageView) v.findViewById(R.id.poi_grid_image);
            TextView poiTextDistance = (TextView) v.findViewById(R.id.poi_grid_distance);

            String name = poi.getName();
            if (name != null) {
                poiTextView.setText(poi.getName());
            }
            double distance = poi.getDistanceToStart();
            if (distance >= 0.0) { // Check if the distance is stored in the object.
                if (distance < 1.0) { // Check for the used unit.
                    poiTextDistance.setText(String.valueOf((int) (distance*1000.0)) + " m");
                } else {
                    DecimalFormat df = new DecimalFormat("#.##");
                    poiTextDistance.setText(String.valueOf(df.format(distance)) + " km");
                }
            }

            Bitmap bm = poi.getPhoto();
            if (bm == null) { // Use a default Image if the Bitmap is empty.
                bm = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
            }
            int width = (int) getResources().getDimension(R.dimen.grid_poi_element_width);
            int textSize = (int) getResources().getDimension(R.dimen.grid_poi_element_textSize);
            // Scale the Bitmap size.
            bm = Bitmap.createScaledBitmap(bm, width, (width - 3 * textSize), false);
            poiImageView.setImageBitmap(bm);
            // Leave a padding at the top for displayment of the POI name.
            poiImageView.setPadding(0,textSize+6,0,0);

            if (poi.isSelected()) {
                // If the user selected a poi, send draw a green border around it.
                v.setBackgroundResource(R.layout.grid_poi_shape);
            } else {
                v.setBackground(null);
            }

            return v;
        }
    }
}
