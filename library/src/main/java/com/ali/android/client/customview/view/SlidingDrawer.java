package com.ali.android.client.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ali.android.client.customview.BuildConfig;
import com.ali.android.client.customview.R;

import static com.ali.android.client.customview.SlidingDrawerUtils.getRawDisplayHeight;
import static com.ali.android.client.customview.SlidingDrawerUtils.isClicked;

public class SlidingDrawer extends FrameLayout {

    private static final String TAG = "SlidingDrawer";

    /* If in Debug/development mode */
    public static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Special value for the position of the layer. STICK_TO_BOTTOM means that the
     * view will stay attached to the bottom part of the screen, and come from
     * there into the viewable area.
     */
    private static final int STICK_TO_BOTTOM = 1;


    /**
     * Special value for the position of the layer. STICK_TO_LEFT means that the
     * view shall be attached to the left side of the screen, and come from
     * there into the viewable area.
     */
    private static final int STICK_TO_LEFT = 2;


    /**
     * The default size of the panel that sticks out when closed
     */
    private static final int DEFAULT_SLIDING_LAYER_OFFSET = 200;

    private static final PanelState DEFAULT_SLIDE_STATE = PanelState.OPEN;

    /* Positions of the last motion event */
    private float mInitialCoordinate;

    /* Drag threshold */
    private int mTouchSlop;

    private int _delta;
    private int _lastCoordinate;
    private long _pressStartTime;

    /**
     * The size of the panel that sticks out when closed
     */
    private int mOffsetDistance;

    /**
     * Value for the position of the layer in the screen
     */
    private int mStickTo;

    private boolean init;

    private enum PanelState {OPEN, CLOSE}

    private PanelState mSlideState = DEFAULT_SLIDE_STATE;

    private OnInteractListener mOnInteractListener;

    public SlidingDrawer(Context context) {
        this(context, null);
    }

    public SlidingDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SlidingLayer, 0, 0);

        try {
            mStickTo = a.getInteger(R.styleable.SlidingLayer_stickTo, STICK_TO_BOTTOM);
            mOffsetDistance = a.getDimensionPixelSize(R.styleable.SlidingLayer_offsetDistance,
                    DEFAULT_SLIDING_LAYER_OFFSET);
        } finally {
            a.recycle();
        }

        //Get system constants for touch thresholds
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        init = true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (init) {
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                    getLayoutParams();

            final View parent = (View) getParent();

            switch (mStickTo) {
                case STICK_TO_BOTTOM:
                    params.topMargin = parent.getHeight() - getHeight();
                    break;
                case STICK_TO_LEFT:
                    params.rightMargin = parent.getWidth() - getWidth();
                    break;
            }

            setLayoutParams(params);

            init = false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                switch (mStickTo) {
                    case STICK_TO_BOTTOM:
                        mInitialCoordinate = event.getY();
                        break;
                    case STICK_TO_LEFT:
                        mInitialCoordinate = event.getX();
                        break;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                float coordinate = 0;
                switch (mStickTo) {
                    case STICK_TO_BOTTOM:
                        coordinate = event.getY();

                        break;
                    case STICK_TO_LEFT:
                        coordinate = event.getX();
                        break;
                }

                final int diff = (int) Math.abs(coordinate - mInitialCoordinate);

                //Verify that either difference is enough to be a drag
                if (diff > mTouchSlop) {
                    //Start capturing events
                    if (DEBUG) Log.d(TAG, "drag captured.");
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        final View parent = (View) getParent();
        int coordinate = 0;
        int distance = 0;
        int tapCoordinate = 0;

        switch (mStickTo) {
            case STICK_TO_BOTTOM:
                coordinate = (int) event.getRawY();
                tapCoordinate = (int) event.getRawY();

                distance = parent.getHeight() -
                        parent.getPaddingTop() -
                        parent.getPaddingBottom() -
                        getHeight();
                break;
            case STICK_TO_LEFT:
                coordinate = parent.getWidth() - (int) event.getRawX();
                tapCoordinate = (int) event.getRawX();

                distance = parent.getWidth() -
                        parent.getPaddingLeft() -
                        parent.getPaddingRight() -
                        getWidth();
                break;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                switch (mStickTo) {
                    case STICK_TO_BOTTOM:
                        _delta = coordinate - ((RelativeLayout.LayoutParams)
                                getLayoutParams()).topMargin;
                        break;

                    case STICK_TO_LEFT:
                        _delta = coordinate - ((RelativeLayout.LayoutParams)
                                getLayoutParams()).rightMargin;
                        break;
                }

                _lastCoordinate = coordinate;
                _pressStartTime = System.currentTimeMillis();

                break;

            case MotionEvent.ACTION_MOVE:

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        getLayoutParams();

                final int farMargin = coordinate - _delta;
                final int closeMargin = distance - farMargin;

                switch (mStickTo) {
                    case STICK_TO_BOTTOM:
                        if (farMargin > distance &&
                                closeMargin > mOffsetDistance - getHeight()) {
                            layoutParams.bottomMargin = closeMargin;
                            layoutParams.topMargin = farMargin;
                        }
                        break;
                    case STICK_TO_LEFT:
                        if (farMargin > distance &&
                                closeMargin > mOffsetDistance - getWidth()) {
                            layoutParams.leftMargin = closeMargin;
                            layoutParams.rightMargin = farMargin;
                        }

                        break;
                }
                setLayoutParams(layoutParams);
                break;

            case MotionEvent.ACTION_UP:

                final int diff = coordinate - _lastCoordinate;
                final long pressDuration = System.currentTimeMillis() - _pressStartTime;

                switch (mStickTo) {
                    case STICK_TO_BOTTOM:
                        if (isClicked(getContext(), diff, pressDuration)) {
                            if (tapCoordinate > parent.getHeight() - mOffsetDistance &&
                                    mSlideState == PanelState.CLOSE) {
                                notifyActionForState(PanelState.OPEN, distance);
                            } else if (Math.abs(getRawDisplayHeight(getContext()) -
                                    tapCoordinate - getHeight()) < mOffsetDistance &&
                                    mSlideState == PanelState.OPEN) {
                                notifyActionForState(PanelState.CLOSE, distance);
                            }
                        } else if (diff > 0) {
                            if (diff > getHeight() / 2.5) {
                                notifyActionForState(PanelState.CLOSE, distance);
                            } else if (mSlideState == PanelState.OPEN) {
                                notifyActionForState(PanelState.OPEN, distance);
                            }
                        } else {
                            if (Math.abs(diff) > getHeight() / 2.5) {
                                notifyActionForState(PanelState.OPEN, distance);
                            } else if (mSlideState == PanelState.CLOSE) {
                                notifyActionForState(PanelState.CLOSE, distance);
                            }
                        }
                        break;

                    case STICK_TO_LEFT:
                        if (isClicked(getContext(), diff, pressDuration)) {
                            if (tapCoordinate <= mOffsetDistance &&
                                    mSlideState == PanelState.CLOSE) {
                                notifyActionForState(PanelState.OPEN, distance);
                            } else if (tapCoordinate > getWidth() - mOffsetDistance &&
                                    mSlideState == PanelState.OPEN) {
                                notifyActionForState(PanelState.CLOSE, distance);
                            }
                        } else if (diff > 0) {
                            if (diff > getWidth() / 2.5) {
                                notifyActionForState(PanelState.CLOSE, distance);
                            } else if (mSlideState == PanelState.OPEN) {
                                notifyActionForState(PanelState.OPEN, distance);
                            }
                        } else {
                            if (Math.abs(diff) > getWidth() / 2.5) {
                                notifyActionForState(PanelState.OPEN, distance);
                            } else if (mSlideState == PanelState.CLOSE) {
                                notifyActionForState(PanelState.CLOSE, distance);
                            }
                        }
                        break;
                }
                break;
        }
        return true;
    }

    private void notifyActionForState(PanelState state, int distance) {

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                getLayoutParams();

        switch (mStickTo) {
            case STICK_TO_BOTTOM:

                switch (state) {
                    case OPEN:
                        params.bottomMargin = 0;
                        params.topMargin = distance;
                        notifyActionFinished(PanelState.OPEN);
                        break;
                    case CLOSE:
                        params.bottomMargin = mOffsetDistance - getHeight();
                        params.topMargin = distance - (mOffsetDistance - getHeight());
                        notifyActionFinished(PanelState.CLOSE);
                        break;
                }

                break;

            case STICK_TO_LEFT:

                switch (state) {
                    case OPEN:
                        params.leftMargin = 0;
                        params.rightMargin = distance;
                        notifyActionFinished(PanelState.OPEN);
                        break;
                    case CLOSE:
                        params.leftMargin = mOffsetDistance - getWidth();
                        params.rightMargin = distance - (mOffsetDistance - getWidth());
                        notifyActionFinished(PanelState.CLOSE);
                        break;
                }
                setLayoutParams(params);
                break;
        }
    }

    private void notifyActionFinished(PanelState state) {

        switch (state) {
            case OPEN:
                mSlideState = PanelState.OPEN;
                if (mOnInteractListener != null) {
                    mOnInteractListener.onOpened();
                }
                break;
            case CLOSE:
                mSlideState = PanelState.CLOSE;
                if (mOnInteractListener != null) {
                    mOnInteractListener.onClosed();
                }
                break;
        }
    }

    /**
     * Sets the listener to be invoked after a switch change
     * {@link OnInteractListener}.
     *
     * @param listener Listener to set
     */
    @SuppressWarnings("unused")
    public void setOnInteractListener(OnInteractListener listener) {
        mOnInteractListener = listener;
    }

    @SuppressWarnings("unused")
    public interface OnInteractListener {

        void onOpened();

        void onClosed();
    }
}
