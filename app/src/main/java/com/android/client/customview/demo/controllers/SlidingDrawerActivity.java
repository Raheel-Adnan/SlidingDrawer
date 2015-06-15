package com.android.client.customview.demo.controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.client.customview.demo.R;

import static com.android.client.customview.demo.controllers.SlidingDrawerFragment.ARG_STICK_TO;

public class SlidingDrawerActivity extends AppCompatActivity  {

    private static final String TAG = "SlidingDrawerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_drawer);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            final int stickTo = getIntent().getIntExtra(ARG_STICK_TO, 0);
            Fragment fragment = SlidingDrawerFragment.newInstance(stickTo);
            fragmentManager.beginTransaction().replace(R.id.content_fragment, fragment).commit();
        }
    }
}
