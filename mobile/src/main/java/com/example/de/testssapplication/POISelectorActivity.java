package com.example.de.testssapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import Fragments.POIListFragment;
import Fragments.POIMapFragment;
import model.DataModel;
import model.POI;

public class POISelectorActivity extends AppCompatActivity {

    private CoordinatorLayout rootLayout;
    private SectionsPagerAdapter mSectionsPagerAdapter; // The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections.
    private ViewPager mViewPager; // The {@link ViewPager} that will host the section contents.
    private DataModel model;
    private POIMapFragment mapFragment;
    private POIListFragment listFragment;
    private FloatingActionButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poiselector);

        model = DataModel.getInstance();

        // Update the title.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_activity_poiselector);

        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    if (listFragment != null) {
                        listFragment.notifySelectionChange();
                    }
                }
                if (tab.getPosition() == 1) {
                    if (mapFragment != null) {
                        mapFragment.updateMarkers();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        nextButton = (FloatingActionButton) findViewById(R.id.fab);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model.getSelectedPOIs().size() == 0) {
                    Snackbar.make(view, getString(R.string.dialog_no_poi_selected), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    finish();
                    Intent i = new Intent(getApplicationContext(), MapActivity.class);
                    startActivity(i);
                }
            }
        });
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
                listFragment = new POIListFragment();
                listFragment.fab = nextButton;
                return listFragment;
            } else {
                mapFragment = new POIMapFragment();
                mapFragment.setParent(POISelectorActivity.this);
                mapFragment.fab = nextButton;
                return mapFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
