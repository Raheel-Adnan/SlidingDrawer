package com.android.client.customview.demo.controllers;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.android.client.customview.demo.R;
import com.github.ali.android.client.customview.view.SlidingDrawer;

import static com.android.client.customview.demo.controllers.SlidingDrawerFragment.ARG_STICK_TO;

public class SlidingDrawerActivity extends AppCompatActivity {

    private static final String TAG = "SlidingDrawerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_drawer);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            final int stickTo = getIntent().getIntExtra(ARG_STICK_TO, 0);
            SlidingDrawerFragment fragment = SlidingDrawerFragment.newInstance(stickTo);
            fragmentManager.beginTransaction().replace(
                    R.id.content_fragment,
                    fragment,
                    SlidingDrawerFragment.TAG)
                    .commit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                final SlidingDrawerFragment fragment = (SlidingDrawerFragment)
                        getSupportFragmentManager().
                                findFragmentByTag(SlidingDrawerFragment.TAG);
                final SlidingDrawer slidingDrawer = fragment.getSlidingDrawer();
                if (slidingDrawer.isOpened()) {
                    slidingDrawer.closeDrawer();
                    return true;
                }

            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
