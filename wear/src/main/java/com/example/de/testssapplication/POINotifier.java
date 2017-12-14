package com.example.de.testssapplication;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class POINotifier extends WearableActivity {

    private ImageView view;
    private TextView nameView;
    private TextView infoView;
    private boolean mShowingBack;

    private Bitmap bitmap;
    private String name;
    private String info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poinotifier);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Intent intent = getIntent();
        if( intent.hasExtra("image") && intent.hasExtra("name") && intent.hasExtra("info") ) {
            bitmap = (Bitmap) intent.getParcelableExtra("image");
            name = intent.getStringExtra("name");
            info = intent.getStringExtra("info");
        }


        mShowingBack = false;
        getFragmentManager()
                .beginTransaction()
                .add(R.id.container, new CardFrontFragment())
                .commit();
    }

    private void flipCard() {
        Fragment newFragment = null;
        if (mShowingBack) {
            newFragment = new CardFrontFragment();
        } else {
            newFragment = new CardBackFragment();
        }
        mShowingBack = !mShowingBack;

        // Create and commit a new fragment transaction that adds the fragment for
        // the back of the card, uses custom animations, and is part of the fragment
        // manager's back stack.

        getFragmentManager()
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
                .commit();
    }



    /**
     * A fragment representing the front of the card.
     */
    @SuppressLint("ValidFragment")
    public class CardFrontFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView_front = inflater.inflate(R.layout.fragment_card_front, container, false);
            ImageView iv = (ImageView)rootView_front.findViewById(R.id.imageView);
            iv.setImageBitmap(bitmap);

            rootView_front.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipCard();
                }
            });

            return rootView_front;
        }
    }

    /**
     * A fragment representing the back of the card.
     */
    @SuppressLint("ValidFragment")
    public class CardBackFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView_back = inflater.inflate(R.layout.fragment_card_back, container, false);
            TextView nameView = (TextView) rootView_back.findViewById(R.id.name);
            nameView.setText(name);
            TextView infoView = (TextView) rootView_back.findViewById(R.id.info);
            infoView.setText(info);

            rootView_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipCard();
                }
            });

            return rootView_back;
        }
    }

}
