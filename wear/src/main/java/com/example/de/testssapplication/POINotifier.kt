package com.example.de.testssapplication

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView

class POINotifier : WearableActivity() {

    private val view: ImageView? = null
    private val nameView: TextView? = null
    private val infoView: TextView? = null
    private var mShowingBack: Boolean = false

    private var bitmap: Bitmap? = null
    private var name: String? = null
    private var info: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poinotifier)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        val intent = intent
        if (intent.hasExtra("image") && intent.hasExtra("name") && intent.hasExtra("info")) {
            bitmap = intent.getParcelableExtra<Bitmap>("image")
            name = intent.getStringExtra("name")
            info = intent.getStringExtra("info")
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(longArrayOf(0, 200, 50, 200), -1) //vibrate
        }


        mShowingBack = false
        fragmentManager
                .beginTransaction()
                .add(R.id.container, CardFrontFragment())
                .commit()
    }

    private fun flipCard() {
        var newFragment: Fragment? = null
        if (mShowingBack) {
            newFragment = CardFrontFragment()
        } else {
            newFragment = CardBackFragment()
        }
        mShowingBack = !mShowingBack

        // Create and commit a new fragment transaction that adds the fragment for
        // the back of the card, uses custom animations, and is part of the fragment
        // manager's back stack.

        fragmentManager
                .beginTransaction()

                // Replace the default fragment animations with animator resources
                // representing rotations when switching to the back of the card, as
                // well as animator resources representing rotations when flipping
                // back to the front (e.g. when the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)

                // Replace any fragments currently in the container view with a
                // fragment representing the next page (indicated by the
                // just-incremented currentPage variable).
                .replace(R.id.container, newFragment)

                // Add this transaction to the back stack, allowing users to press
                // Back to get to the front of the card.
                .addToBackStack(null)

                // Commit the transaction.
                .commit()
    }


    /**
     * A fragment representing the front of the card.
     */
    @SuppressLint("ValidFragment")
    inner class CardFrontFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView_front = inflater.inflate(R.layout.fragment_card_front, container, false)
            val iv = rootView_front.findViewById<View>(R.id.imageView) as ImageView
            iv.setImageBitmap(bitmap)

            rootView_front.setOnClickListener { flipCard() }

            return rootView_front
        }
    }

    /**
     * A fragment representing the back of the card.
     */
    @SuppressLint("ValidFragment")
    inner class CardBackFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView_back = inflater.inflate(R.layout.fragment_card_back, container, false)
            val nameView = rootView_back.findViewById<View>(R.id.name) as TextView
            nameView.text = name
            val infoView = rootView_back.findViewById<View>(R.id.info) as TextView
            infoView.text = info

            rootView_back.setOnClickListener { flipCard() }

            return rootView_back
        }
    }

}
