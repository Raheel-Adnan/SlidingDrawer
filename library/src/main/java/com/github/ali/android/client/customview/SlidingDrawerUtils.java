package com.github.ali.android.client.customview;

import android.content.Context;
import android.util.DisplayMetrics;

public class SlidingDrawerUtils {

    /**
     * Max allowed duration for a "click", in milliseconds.
     */
    private static final int MAX_CLICK_DURATION = 1000;

    /**
     * Max allowed distance to move during a "click", in DP.
     */
    private static final int MAX_CLICK_DISTANCE = 5;

    public static int getRawDisplayHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static boolean isClicked(Context context, float diff, long pressDuration) {
        return pressDuration < MAX_CLICK_DURATION &&
                distance(context, diff) < MAX_CLICK_DISTANCE;
    }

    private static float distance(Context context, float diff) {
        float distanceInPx = (float) Math.sqrt(diff * diff);
        return pxToDp(context, distanceInPx);
    }

    private static float pxToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
}
