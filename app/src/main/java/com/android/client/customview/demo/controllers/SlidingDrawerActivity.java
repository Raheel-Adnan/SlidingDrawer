package com.android.client.customview.demo.controllers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.ali.android.client.customview.view.SlidingDrawer;
import com.android.client.customview.demo.R;

public class SlidingDrawerActivity extends AppCompatActivity implements
        SlidingDrawer.OnInteractListener {

    private static final String TAG = "SlidingDrawerActivity";

    private ImageView mSlidingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_drawer);

        mSlidingImage = (ImageView) findViewById(R.id.slidingImage);

        final SlidingDrawer mSlidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
        mSlidingDrawer.setOnInteractListener(this);
    }

    @Override
    public void onOpened() {
        Log.d(TAG, "onOpened()");
        mSlidingImage.setImageResource(R.drawable.ic_arrow_up);
    }

    @Override
    public void onClosed() {
        Log.d(TAG, "onClosed()");
        mSlidingImage.setImageResource(R.drawable.ic_arrow_down);
    }
}
