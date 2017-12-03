package Fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.de.testssapplication.R;

import java.util.ArrayList;
import java.util.List;

import model.DataModel;
import model.POI;
import network.POIFetcher;

/**
 * Created by de on 02.12.2017.
 */

public class POIListFragment extends Fragment {

    DataModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_list, viewGroup, false);

        model = DataModel.getInstance();
        final GridView gridView = rootView.findViewById(R.id.poi_grid_list);
        final POIGridAdapter poiAdapter = new POIGridAdapter();
        gridView.setAdapter(poiAdapter);

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

    private class POIGridAdapter extends BaseAdapter {

        public POIGridAdapter() {}

        @Override
        public int getCount() {
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
            Log.d("WHY", "i " + String.valueOf(i) + " " + poi.getName());
            View v;

            if (convertView == null) {
                final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                //convertView = layoutInflater.inflate(R.layout.grid_view_poi, null);
                v = layoutInflater.inflate(R.layout.grid_view_poi, null);
            } else {
                v = (View) convertView;
            }

            ImageView poiImageView = (ImageView) v.findViewById(R.id.poi_grid_image);
            poiImageView.setPadding(5,5,5,5);
            TextView poiTextView = (TextView) v.findViewById(R.id.poi_grid_name);

            String name = poi.getName();
            if (name != null) {
                poiTextView.setText(poi.getName());
            }

            Bitmap bm = poi.getPhoto();
            if (bm == null) {
                bm = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
            }
            int width = (int) getResources().getDimension(R.dimen.grid_poi_element_width);
            int textSize = (int) getResources().getDimension(R.dimen.grid_poi_element_textSize);
            bm = Bitmap.createScaledBitmap(bm, width, (width - 3 * textSize), false);
            poiImageView.setImageBitmap(bm);

            if (poi.isSelected()) {
                v.setBackgroundResource(R.layout.grid_poi_shape);
            } else {
                v.setBackground(null);
            }

            return v;
        }
    }
}
