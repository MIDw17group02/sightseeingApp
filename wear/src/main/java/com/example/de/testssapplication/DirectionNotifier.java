package com.example.de.testssapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

public class DirectionNotifier extends WearableActivity {

    private TextView directionView;
    private TextView ditanceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction_notifier);

        directionView = (TextView) findViewById(R.id.direction);
        ditanceView = (TextView) findViewById(R.id.distance);

        Intent intent = getIntent();
        if( intent.hasExtra("dir") && intent.hasExtra("dis") ) {
            String direction = intent.getStringExtra("dir");
            String distance = intent.getStringExtra("dis");
            directionView.setText(direction);
            ditanceView.setText(distance);
        }

    }

}
