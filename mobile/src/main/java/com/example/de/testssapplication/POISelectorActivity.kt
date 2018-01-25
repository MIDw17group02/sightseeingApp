package com.example.de.testssapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TabLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter

import Fragments.POIListFragment
import Fragments.POIMapFragment
import model.DataModel
import model.POI

class POISelectorActivity : AppCompatActivity() {

    private val rootLayout: CoordinatorLayout? = null
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null // The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections.
    private var mViewPager: ViewPager? = null // The {@link ViewPager} that will host the section contents.
    private var model: DataModel? = null
    private var mapFragment: POIMapFragment? = null
    private var listFragment: POIListFragment? = null
    private var nextButton: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poiselector)

        model = DataModel.instance

        // Update the title.
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.title_activity_poiselector)

        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<View>(R.id.container) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout

        mViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mViewPager))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    if (listFragment != null) {
                        listFragment!!.notifySelectionChange()
                    }
                }
                if (tab.position == 1) {
                    if (mapFragment != null) {
                        mapFragment!!.updateMarkers()
                        //mapFragment.updateCamera();
                        mapFragment!!.getDeviceLocation()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        nextButton = findViewById<View>(R.id.fab) as FloatingActionButton
        nextButton!!.setOnClickListener { view ->
            if (model!!.selectedPOIs.size == 0) {
                Snackbar.make(view, getString(R.string.dialog_no_poi_selected), Snackbar.LENGTH_LONG).setAction("Action", null).show()
            } else {
                finish()
                val i = Intent(applicationContext, MapActivity::class.java)
                startActivity(i)
            }
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.

            if (position == 0) {
                listFragment = POIListFragment()
                listFragment!!.fab = nextButton
                return listFragment!!
            } else {
                mapFragment = POIMapFragment()
                mapFragment!!.setParent(this@POISelectorActivity)
                mapFragment!!.fab = nextButton
                return mapFragment!!
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

}
