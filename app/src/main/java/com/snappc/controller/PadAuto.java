package com.snappc.controller;

import android.view.InputDevice;
import android.view.MotionEvent;
import java.util.HashMap;

public class PadAuto {
    private static final HashMap<Integer, int[]> CACHE = new HashMap<>();
    private static boolean hatWas = false;

    private static int[] detect(InputDevice d) {
        boolean z = d.getMotionRange(MotionEvent.AXIS_Z) != null;
        boolean rz = d.getMotionRange(MotionEvent.AXIS_RZ) != null;
        boolean rx = d.getMotionRange(MotionEvent.AXIS_RX) != null;
        boolean ry = d.getMotionRange(MotionEvent.AXIS_RY) != null;
        int l = -1, r = -1;
        if (d.getMotionRange(MotionEvent.AXIS_LTRIGGER) != null) l = MotionEvent.AXIS_LTRIGGER;
        else if (d.getMotionRange(MotionEvent.AXIS_BRAKE) != null) l = MotionEvent.AXIS_BRAKE;
        if (d.getMotionRange(MotionEvent.AXIS_RTRIGGER) != null) r = MotionEvent.AXIS_RTRIGGER;
        else if (d.getMotionRange(MotionEvent.AXIS_GAS) != null) r = MotionEvent.AXIS_GAS;
        if (l == -1 && r == -1 && z && rz && rx && ry) { l = MotionEvent.AXIS_Z; r = MotionEvent.AXIS_RZ; }
        boolean hat = d.getMotionRange(MotionEvent.AXIS_HAT_X) != null && d.getMotionRange(MotionEvent.AXIS_HAT_Y) != null;
        return new int[]{l, r, hat ? 1 : 0};
    }

    private static float trig(MotionEvent e, InputDevice d, int axis) {
        if (axis == -1) return 0f;
        float v = e.getAxisValue(axis);
        InputDevice.MotionRange m = d.getMotionRange(axis);
        if (m != null && m.getMin() < 0f && m.getMax() > m.getMin()) v = (v - m.getMin()) / (m.getMax() - m.getMin());
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    public static float[] read(MotionEvent e) {
        InputDevice d = e.getDevice();
        if (d == null) return null;
        int[] c = CACHE.get(d.getId());
        if (c == null) { c = detect(d); CACHE.put(d.getId(), c); }
        float[] o = new float[7];
        o[0] = trig(e, d, c[0]);
        o[1] = trig(e, d, c[1]);
        o[5] = c[0] != -1 ? 1 : 0;
        o[6] = c[1] != -1 ? 1 : 0;
        if (c[2] == 1) {
            o[2] = e.getAxisValue(MotionEvent.AXIS_HAT_X);
            o[3] = e.getAxisValue(MotionEvent.AXIS_HAT_Y);
            boolean now = o[2] != 0f || o[3] != 0f;
            o[4] = (now || hatWas) ? 1 : 0;
            hatWas = now;
        }
        return o;
    }
}
