package com.example.de.testssapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectionNotifier extends WearableActivity {
    //Log-Tag
    public static String TAG = "Wear-DirectionNotifier";

    private ImageView directionImage;
    private TextView directionView;
    private TextView ditanceView;

    private String direction;
    private String distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction_notifier);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        directionImage = (ImageView) findViewById(R.id.image);
        directionView = (TextView) findViewById(R.id.direction);
        ditanceView = (TextView) findViewById(R.id.distance);

        Intent intent = getIntent();
        if (intent.hasExtra("dir") && intent.hasExtra("dis")) {
            direction = intent.getStringExtra("dir");
            distance = intent.getStringExtra("dis");
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putString("direction", direction);
            editor.putString("distance", distance);
            editor.apply();
            if(direction.length() > 7) {
                directionView.setText(direction);
            }
            ditanceView.setText(distance);
        } else {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            direction = prefs.getString("direction", "undefined");
            distance = prefs.getString("distance", "undefined");
            directionView.setText(direction);
            ditanceView.setText(distance);
        }

        //set Image according direction
        if (!direction.equals("undefined")) {
            Log.d(TAG, "Setting image according direction \"" + direction + "\"");
            switch (direction) {
                case "ahead":
                    directionImage.setImageResource(R.drawable.ic_ahead);
                    break;
                case "back":
                    directionImage.setImageResource(R.drawable.ic_back);
                    break;
                case "hleft":
                    directionImage.setImageResource(R.drawable.ic_hleft);
                    break;
                case "hright":
                    directionImage.setImageResource(R.drawable.ic_hright);
                    break;
                case "left":
                    directionImage.setImageResource(R.drawable.ic_left);
                    break;
                case "right":
                    directionImage.setImageResource(R.drawable.ic_right);
                    break;
                case "sleft":
                    directionImage.setImageResource(R.drawable.ic_sleft);
                    break;
                case "sright":
                    directionImage.setImageResource(R.drawable.ic_sright);
                    break;
                default:
                    break;
            }


        }

    }

}
