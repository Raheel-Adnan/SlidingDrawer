package com.github.ali.android.client.customview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.github.ali.android.client.customview.BuildConfig;
import com.github.ali.android.client.customview.R;

import static com.github.ali.android.client.customview.SlidingDrawerUtils.getLocationInXAxis;
import static com.github.ali.android.client.customview.SlidingDrawerUtils.getLocationInYAxis;
import static com.github.ali.android.client.customview.SlidingDrawerUtils.getRawDisplayHeight;
import static com.github.ali.android.client.customview.SlidingDrawerUtils.getRawDisplayWidth;
import static com.github.ali.android.client.customview.SlidingDrawerUtils.isClicked;

public class SlidingDrawer extends FrameLayout {

    private static final String TAG = "SlidingDrawer";

    /* If in Debug/development mode */
    public static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Special value for the position of the layer. STICK_TO_BOTTOM means that the
     * view will stay attached to the bottom part of the screen, and come from
     * there into the viewable area.
     */
    public static final int STICK_TO_BOTTOM = 1;


    /**
     * Special value for the position of the layer. STICK_TO_LEFT means that the
     * view shall be attached to the left side of the screen, and come from
     * there into the viewable area.
     */
    public static final int STICK_TO_LEFT = 2;

    /**
     * Special value for the position of the layer. STICK_TO_RIGHT means that the
     * view shall be attached to the right side of the screen, and come from
     * there into the viewable area.
     */
    public static final int STICK_TO_RIGHT = 3;

    /**
     * Special value for the position of the layer. STICK_TO_TOP means that the
     * view shall be attached to the top side of the screen, and come from
     * there into the viewable area.
     */
    public static final int STICK_TO_TOP = 4;


    /**
     * The default size of the panel that sticks out when closed
     */
    private static final int DEFAULT_SLIDING_LAYER_OFFSET = 200;

    /* Duration for certain animations we use */
    private static final int TRANSLATION_ANIM_DURATION = 300;

    private static final PanelState DEFAULT_SLIDE_STATE = PanelState.CLOSE;

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

    /**
     * Value for the orientation of the layer in the screen
     */
    private scrollState mScrollOrientation;

    private boolean init;

    private enum PanelState {OPEN, CLOSE}

    private enum scrollState {VERTICAL, HORIZONTAL}

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

        switch (mStickTo) {
            case STICK_TO_TOP:
            case STICK_TO_BOTTOM:
                mScrollOrientation = scrollState.VERTICAL;
                break;
            case STICK_TO_LEFT:
            case STICK_TO_RIGHT:
                mScrollOrientation = scrollState.HORIZONTAL;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (init) {
            post(new Runnable() {
                @Override
                public void run() {
                    notifyActionForState(mSlideState, false);
                }
            });

            init = false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                switch (mStickTo) {
                    case STICK_TO_BOTTOM:
                    case STICK_TO_TOP:
                        mInitialCoordinate = event.getY();
                        break;
                    case STICK_TO_LEFT:
                    case STICK_TO_RIGHT:
                        mInitialCoordinate = event.getX();
                        break;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                float coordinate = 0;
                switch (mStickTo) {
                    case STICK_TO_BOTTOM:
                    case STICK_TO_TOP:
                        coordinate = event.getY();

                        break;
                    case STICK_TO_LEFT:
                    case STICK_TO_RIGHT:
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
        final int coordinate;
        final int distance = getDistance();
        final int tapCoordinate;

        switch (mStickTo) {
            case STICK_TO_BOTTOM:
                coordinate = (int) event.getRawY();
                tapCoordinate = (int) event.getRawY();
                break;

            case STICK_TO_LEFT:
                coordinate = parent.getWidth() - (int) event.getRawX();
                tapCoordinate = (int) event.getRawX();
                break;

            case STICK_TO_RIGHT:
                coordinate = (int) event.getRawX();
                tapCoordinate = (int) event.getRawX();
                break;

            case STICK_TO_TOP:
                coordinate = getRawDisplayHeight(getContext()) - (int) event.getRawY();
                tapCoordinate = (int) event.getRawY();
                break;

            default:
                throw new IllegalStateException("Failed to initialize coordinates.");
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

                    case STICK_TO_RIGHT:
                        _delta = coordinate - ((RelativeLayout.LayoutParams)
                                getLayoutParams()).leftMargin;
                        break;

                    case STICK_TO_TOP:
                        _delta = coordinate - ((RelativeLayout.LayoutParams)
                                getLayoutParams()).bottomMargin;
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
                    case STICK_TO_RIGHT:
                        if (farMargin > distance &&
                                closeMargin > mOffsetDistance - getWidth()) {
                            layoutParams.rightMargin = closeMargin;
                            layoutParams.leftMargin = farMargin;
                        }
                        break;
                    case STICK_TO_TOP:
                        if (farMargin > distance &&
                                closeMargin > mOffsetDistance - getHeight()) {
                            layoutParams.topMargin = closeMargin;
                            layoutParams.bottomMargin = farMargin;
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
                                notifyActionAndAnimateForState(PanelState.OPEN, getHeight() - mOffsetDistance, true);
                            } else if (Math.abs(getRawDisplayHeight(getContext()) -
                                    tapCoordinate - getHeight()) < mOffsetDistance &&
                                    mSlideState == PanelState.OPEN) {
                                notifyActionAndAnimateForState(PanelState.CLOSE, getHeight() - mOffsetDistance, true);
                            }
                        } else {
                            smoothScrollToAndNotify(diff);
                        }

                        break;

                    case STICK_TO_TOP:
                        if (isClicked(getContext(), diff, pressDuration)) {
                            final int y = getLocationInYAxis(this);
                            if (tapCoordinate - Math.abs(y) <= mOffsetDistance &&
                                    mSlideState == PanelState.CLOSE) {
                                notifyActionAndAnimateForState(PanelState.OPEN, getHeight() - mOffsetDistance, true);
                            } else if (getHeight() - (tapCoordinate - Math.abs(y)) < mOffsetDistance &&
                                    mSlideState == PanelState.OPEN) {
                                notifyActionAndAnimateForState(PanelState.CLOSE, getHeight() - mOffsetDistance, true);
                            }
                        } else {
                            smoothScrollToAndNotify(diff);
                        }

                        break;

                    case STICK_TO_LEFT:
                        if (isClicked(getContext(), diff, pressDuration)) {
                            if (tapCoordinate <= mOffsetDistance &&
                                    mSlideState == PanelState.CLOSE) {
                                notifyActionAndAnimateForState(PanelState.OPEN, getWidth() - mOffsetDistance, true);
                            } else if (tapCoordinate > getWidth() - mOffsetDistance &&
                                    mSlideState == PanelState.OPEN) {
                                notifyActionAndAnimateForState(PanelState.CLOSE, getWidth() - mOffsetDistance, true);
                            }
                        } else {
                            smoothScrollToAndNotify(diff);
                        }

                        break;

                    case STICK_TO_RIGHT:
                        if (isClicked(getContext(), diff, pressDuration)) {
                            if (parent.getWidth() - tapCoordinate <= mOffsetDistance &&
                                    mSlideState == PanelState.CLOSE) {
                                notifyActionAndAnimateForState(PanelState.OPEN, getWidth() - mOffsetDistance, true);
                            } else if (parent.getWidth() - tapCoordinate > getWidth() - mOffsetDistance &&
                                    mSlideState == PanelState.OPEN) {
                                notifyActionAndAnimateForState(PanelState.CLOSE, getWidth() - mOffsetDistance, true);
                            }
                        } else {
                            smoothScrollToAndNotify(diff);
                        }

                        break;
                }
                break;
        }
        return true;
    }

    private void smoothScrollToAndNotify(int diff) {

        int length = getLength();

        PanelState stateToApply;
        if (diff > 0) {
            if (diff > length / 2.5) {
                stateToApply = PanelState.CLOSE;
                notifyActionAndAnimateForState(stateToApply, getTranslationFor(stateToApply), true);
            } else if (mSlideState == PanelState.OPEN) {
                stateToApply = PanelState.OPEN;
                notifyActionAndAnimateForState(stateToApply, getTranslationFor(stateToApply), false);
            }
        } else {
            if (Math.abs(diff) > length / 2.5) {
                stateToApply = PanelState.OPEN;
                notifyActionAndAnimateForState(stateToApply, getTranslationFor(stateToApply), true);
            } else if (mSlideState == PanelState.CLOSE) {
                stateToApply = PanelState.CLOSE;
                notifyActionAndAnimateForState(stateToApply, getTranslationFor(stateToApply), false);
            }
        }
    }

    private int getTranslationFor(PanelState stateToApply) {
        switch (mStickTo) {

            case STICK_TO_BOTTOM:

                switch (stateToApply) {
                    case OPEN:
                        return getHeight() - (getRawDisplayHeight(getContext()) - getLocationInYAxis(this));

                    case CLOSE:
                        return getRawDisplayHeight(getContext()) - getLocationInYAxis(this) - mOffsetDistance;
                }
                break;

            case STICK_TO_TOP:

                final int actionBarDiff = getRawDisplayHeight(getContext()) - ((View) getParent()).getHeight();
                final int y = getLocationInYAxis(this) + getHeight();

                switch (stateToApply) {
                    case OPEN:
                        return getHeight() - y + actionBarDiff;

                    case CLOSE:
                        return y - mOffsetDistance - actionBarDiff;
                }
                break;

            case STICK_TO_LEFT:

                final int x = getLocationInXAxis(this) + getWidth();

                switch (stateToApply) {
                    case OPEN:
                        return getWidth() - x;
                    case CLOSE:
                        return x - mOffsetDistance;
                }
                break;

            case STICK_TO_RIGHT:

                switch (stateToApply) {
                    case OPEN:
                        return getWidth() - (getRawDisplayWidth(getContext()) - getLocationInXAxis(this));
                    case CLOSE:
                        return getRawDisplayWidth(getContext()) - getLocationInXAxis(this) - mOffsetDistance;
                }
                break;
        }
        throw new IllegalStateException("Failed to return translation for drawer.");
    }

    private void notifyActionAndAnimateForState(final PanelState stateToApply,
                                                final int translation, final boolean notify) {
        switch (mStickTo) {
            case STICK_TO_BOTTOM:
                switch (stateToApply) {
                    case OPEN:
                        animate()
                                .translationY(-translation)
                                .setDuration(TRANSLATION_ANIM_DURATION)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        notifyActionForState(stateToApply, notify);
                                        setTranslationY(0);
                                    }
                                });

                        break;
                    case CLOSE:
                        animate()
                                .translationY(translation)
                                .setDuration(TRANSLATION_ANIM_DURATION)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        notifyActionForState(stateToApply, notify);
                                        setTranslationY(0);
                                    }
                                });
                        break;
                }
                break;

            case STICK_TO_TOP:
                switch (stateToApply) {
                    case OPEN:
                        animate()
                                .translationY(translation)
                                .setDuration(TRANSLATION_ANIM_DURATION)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        notifyActionForState(stateToApply, notify);
                                        setTranslationY(0);
                                    }
                                });
                        break;
                    case CLOSE:
                        animate()
                                .translationY(-translation)
                                .setDuration(TRANSLATION_ANIM_DURATION)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        notifyActionForState(stateToApply, notify);
                                        setTranslationY(0);
                                    }
                                });
                        break;
                }
                break;

            case STICK_TO_LEFT:
                switch (stateToApply) {
                    case OPEN:
                        animate()
                                .translationX(translation)
                                .setDuration(TRANSLATION_ANIM_DURATION)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        notifyActionForState(stateToApply, notify);
                                        setTranslationX(0);
                                    }
                                });
                        break;
                    case CLOSE:
                        animate()
                                .translationX(-translation)
                                .setDuration(TRANSLATION_ANIM_DURATION)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        notifyActionForState(stateToApply, notify);
                                        setTranslationX(0);
                                    }
                                });
                        break;
                }
                break;

            case STICK_TO_RIGHT:
                switch (stateToApply) {
                    case OPEN:
                        animate()
                                .translationX(-translation)
                                .setDuration(TRANSLATION_ANIM_DURATION)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        notifyActionForState(stateToApply, notify);
                                        setTranslationX(0);
                                    }
                                });
                        break;
                    case CLOSE:
                        animate()
                                .translationX(translation)
                                .setDuration(TRANSLATION_ANIM_DURATION)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        notifyActionForState(stateToApply, notify);
                                        setTranslationX(0);
                                    }
                                });
                        break;
                }
                break;
        }
    }

    private void notifyActionForState(PanelState stateToApply, boolean notify) {

        final int distance = getDistance();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                getLayoutParams();

        switch (mStickTo) {
            case STICK_TO_BOTTOM:

                switch (stateToApply) {
                    case OPEN:
                        params.bottomMargin = 0;
                        params.topMargin = distance;
                        break;
                    case CLOSE:
                        params.bottomMargin = mOffsetDistance - getHeight();
                        params.topMargin = distance - (mOffsetDistance - getHeight());
                        break;
                }

                break;

            case STICK_TO_LEFT:

                switch (stateToApply) {
                    case OPEN:
                        params.leftMargin = 0;
                        params.rightMargin = distance;
                        break;
                    case CLOSE:
                        params.leftMargin = mOffsetDistance - getWidth();
                        params.rightMargin = distance - (mOffsetDistance - getWidth());
                        break;
                }

                break;

            case STICK_TO_RIGHT:

                switch (stateToApply) {
                    case OPEN:
                        params.rightMargin = 0;
                        params.leftMargin = distance;
                        break;
                    case CLOSE:
                        params.rightMargin = mOffsetDistance - getWidth();
                        params.leftMargin = distance - (mOffsetDistance - getWidth());
                        break;
                }

                break;

            case STICK_TO_TOP:

                switch (stateToApply) {
                    case OPEN:
                        params.topMargin = 0;
                        params.bottomMargin = distance;
                        break;
                    case CLOSE:
                        params.topMargin = mOffsetDistance - getHeight();
                        params.bottomMargin = distance - (mOffsetDistance - getHeight());
                        break;
                }

                break;
        }
        if (notify) {
            notifyActionFinished(stateToApply);
        }
        setLayoutParams(params);
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

    public boolean isOpened() {
        return mSlideState == PanelState.OPEN;
    }

    public boolean isClosed() {
        return mSlideState == PanelState.CLOSE;
    }

    @SuppressWarnings("unused")
    public void openDrawer() {
        notifyActionAndAnimateForState(PanelState.OPEN, getLength() - mOffsetDistance, !isOpened());
    }

    @SuppressWarnings("unused")
    public void closeDrawer() {
        notifyActionAndAnimateForState(PanelState.CLOSE, getLength() - mOffsetDistance, !isClosed());
    }

    private int getDistance() {
        final View parent = (View) getParent();

        switch (mScrollOrientation) {
            case VERTICAL:
                return parent.getHeight() -
                        parent.getPaddingTop() -
                        parent.getPaddingBottom() -
                        getHeight();
            case HORIZONTAL:
                return parent.getWidth() -
                        parent.getPaddingLeft() -
                        parent.getPaddingRight() -
                        getWidth();
        }
        throw new IllegalStateException("Scroll orientation is not initialized.");
    }

    private int getLength() {
        switch (mScrollOrientation) {
            case VERTICAL:
                return getHeight();
            case HORIZONTAL:
                return getWidth();
        }
        throw new IllegalStateException("Scroll orientation is not initialized.");
    }
}
