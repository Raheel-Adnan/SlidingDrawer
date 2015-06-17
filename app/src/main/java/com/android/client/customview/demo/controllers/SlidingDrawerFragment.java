package com.android.client.customview.demo.controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.ali.android.client.customview.view.SlidingDrawer;
import com.android.client.customview.demo.R;

import static com.github.ali.android.client.customview.view.SlidingDrawer.STICK_TO_BOTTOM;
import static com.github.ali.android.client.customview.view.SlidingDrawer.STICK_TO_LEFT;
import static com.github.ali.android.client.customview.view.SlidingDrawer.STICK_TO_RIGHT;
import static com.github.ali.android.client.customview.view.SlidingDrawer.STICK_TO_TOP;

public class SlidingDrawerFragment extends Fragment implements
        SlidingDrawer.OnInteractListener {

    public static final String TAG = "SlidingDrawerFragment";

    public static final String ARG_STICK_TO = "stickTo";

    private ImageView mSlidingImage;
    private SlidingDrawer mSlidingDrawer;

    public static SlidingDrawerFragment newInstance(int stickTo) {
        SlidingDrawerFragment fragment = new SlidingDrawerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STICK_TO, stickTo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        final Bundle args = getArguments();
        int resource = 0;
        switch (args.getInt(ARG_STICK_TO)) {
            case STICK_TO_BOTTOM:
                resource = R.layout.fragment_sliding_drawer_bottom;
                break;
            case STICK_TO_LEFT:
                resource = R.layout.fragment_sliding_drawer_left;
                break;
            case STICK_TO_RIGHT:
                resource = R.layout.fragment_sliding_drawer_right;
                break;
            case STICK_TO_TOP:
                resource = R.layout.fragment_sliding_drawer_top;
        }
        return inflater.inflate(resource, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        mSlidingImage = (ImageView) view.findViewById(R.id.slidingImage);

        mSlidingDrawer = (SlidingDrawer) view.findViewById(R.id.slidingDrawer);
        mSlidingDrawer.setOnInteractListener(this);
    }

    @Override
    public void onOpened() {
        if (SlidingDrawer.DEBUG) Log.d(TAG, "onOpened()");
        mSlidingImage.setImageResource(R.drawable.ic_arrow_down);
    }

    @Override
    public void onClosed() {
        if (SlidingDrawer.DEBUG) Log.d(TAG, "onClosed()");
        mSlidingImage.setImageResource(R.drawable.ic_arrow_up);
    }

    public SlidingDrawer getSlidingDrawer() {
        return mSlidingDrawer;
    }

}
