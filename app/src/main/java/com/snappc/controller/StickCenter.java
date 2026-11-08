package com.snappc.controller;

import android.view.MotionEvent;
import java.util.HashMap;

public class StickCenter {
    private static final int N = 40;
    private static final HashMap<Long, float[]> map = new HashMap<>();

    public static synchronized float apply(int dev, int axis, float raw) {
        if (axis != MotionEvent.AXIS_X && axis != MotionEvent.AXIS_Y
                && axis != MotionEvent.AXIS_Z && axis != MotionEvent.AXIS_RZ && axis != MotionEvent.AXIS_HAT_X && axis != MotionEvent.AXIS_HAT_Y
                && axis != MotionEvent.AXIS_RX && axis != MotionEvent.AXIS_RY) return raw;
        long key = ((long) dev << 32) | axis;
        float[] s = map.get(key);
        if (s == null) { s = new float[3]; map.put(key, s); }
        if (s[2] < N) {
            s[0] += raw;
            s[2] += 1;
            if (s[2] >= N) {
                float avg = s[0] / N;
                s[1] = Math.abs(avg) < 0.25f ? avg : 0f;
            }
            return raw;
        }
        return raw - s[1];
    }
}
