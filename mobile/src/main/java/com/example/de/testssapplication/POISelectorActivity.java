package com.example.de.testssapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import Fragments.POIListFragment;
import Fragments.POIMapFragment;
import model.DataModel;
import network.POIFetcher;

public class POISelectorActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter; // The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections.
    private ViewPager mViewPager; // The {@link ViewPager} that will host the section contents.
    private DataModel model;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poiselector);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_activity_poiselector);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                finish();
                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(i);
            }
        });
        model = DataModel.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        new GetCurrentLocationTask().execute();


    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.

            if (position == 0) {
                return new POIListFragment();
            } else {
                POIMapFragment mapFragment = new POIMapFragment();
                mapFragment.setParent(POISelectorActivity.this);
                return mapFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private class GetCurrentLocationTask extends AsyncTask<Void, Void, Location> {

        @SuppressLint("MissingPermission")
        @Override
        protected Location doInBackground(Void... voids) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(POISelectorActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                currentLocation = location;
                            }
                        }
                    });
            return currentLocation;
        }

        @Override
        protected void onPostExecute(Location location) {
            if (location != null)
                model.setLastKnownLocation(location);
            else
                new GetCurrentLocationTask().execute();
        }
    }
}
