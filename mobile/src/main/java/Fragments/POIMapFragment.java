package Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.de.testssapplication.R;

import model.DataModel;

/**
 * Created by de on 02.12.2017.
 */

public class POIMapFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab_map, viewGroup, false);
        return rootView;
    }

}
