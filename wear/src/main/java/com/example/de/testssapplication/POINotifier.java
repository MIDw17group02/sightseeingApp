package com.example.de.testssapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class POINotifier extends WearableActivity {

    private ImageView view;
    private TextView nameView;
    private TextView infoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poinotifier);

        nameView = (TextView) findViewById(R.id.name);
        infoView = (TextView) findViewById(R.id.info);
        view = (ImageView) findViewById(R.id.imageView);


        Intent intent = getIntent();
        if( intent.hasExtra("image") && intent.hasExtra("name") && intent.hasExtra("info") ) {
            Bitmap bitmap = (Bitmap) intent.getParcelableExtra("image");
            view.setImageBitmap(bitmap);

            String name = intent.getStringExtra("name");
            String info = intent.getStringExtra("info");
            nameView.setText(name);
            infoView.setText(info);
        }
    }

}
