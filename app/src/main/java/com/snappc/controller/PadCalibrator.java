package com.snappc.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.InputDevice;
import android.view.MotionEvent;

public class PadCalibrator {
    public int[] axes = new int[4];
    public int lTrig = -1, rTrig = -1;
    private boolean hasTrig;
    private final float[] center = new float[4];
    private final float[] dead = {0.08f, 0.08f, 0.08f, 0.08f};
    private final float[][] buf = new float[60][4];
    private int n = 0;
    private boolean calibrating = false;
    private final SharedPreferences prefs;
    private final String key;

    public PadCalibrator(Context c, InputDevice d) {
        prefs = c.getSharedPreferences("snappc_pad", 0);
        key = d.getDescriptor();
        mapAxes(d);
        load();
    }

    private static boolean has(InputDevice d, int a) {
        return d.getMotionRange(a) != null;
    }

    private void mapAxes(InputDevice d) {
        axes[0] = MotionEvent.AXIS_X;
        axes[1] = MotionEvent.AXIS_Y;
        boolean lt = has(d, MotionEvent.AXIS_LTRIGGER) && has(d, MotionEvent.AXIS_RTRIGGER);
        boolean gb = has(d, MotionEvent.AXIS_BRAKE) && has(d, MotionEvent.AXIS_GAS);
        hasTrig = lt || gb;
        boolean z = has(d, MotionEvent.AXIS_Z) && has(d, MotionEvent.AXIS_RZ);
        boolean rx = has(d, MotionEvent.AXIS_RX) && has(d, MotionEvent.AXIS_RY);
        if (hasTrig) {
            lTrig = lt ? MotionEvent.AXIS_LTRIGGER : MotionEvent.AXIS_BRAKE;
            rTrig = lt ? MotionEvent.AXIS_RTRIGGER : MotionEvent.AXIS_GAS;
            useZ(z);
        } else {
            useZ(!rx && z);
            if (rx && z) { lTrig = MotionEvent.AXIS_Z; rTrig = MotionEvent.AXIS_RZ; }
        }
    }

    private void useZ(boolean z) {
        axes[2] = z ? MotionEvent.AXIS_Z : MotionEvent.AXIS_RX;
        axes[3] = z ? MotionEvent.AXIS_RZ : MotionEvent.AXIS_RY;
    }

    private void load() {
        for (int i = 0; i < 4; i++) {
            center[i] = prefs.getFloat(key + ".c" + i, 0f);
            dead[i] = prefs.getFloat(key + ".d" + i, 0.08f);
        }
    }

    private void save() {
        SharedPreferences.Editor e = prefs.edit();
        for (int i = 0; i < 4; i++) {
            e.putFloat(key + ".c" + i, center[i]);
            e.putFloat(key + ".d" + i, dead[i]);
        }
        e.apply();
    }

    public void startCalibration() { n = 0; calibrating = true; }

    public boolean isCalibrating() { return calibrating; }

    private void relearn(MotionEvent e, float[] raw) {
        if (Math.abs(raw[2]) > 0.2f || Math.abs(raw[3]) > 0.2f) return;
        boolean isZ = axes[2] == MotionEvent.AXIS_Z;
        if (!isZ && !hasTrig) return;
        int a = isZ ? MotionEvent.AXIS_RX : MotionEvent.AXIS_Z;
        int b = isZ ? MotionEvent.AXIS_RY : MotionEvent.AXIS_RZ;
        if (Math.abs(e.getAxisValue(a)) > 0.6f || Math.abs(e.getAxisValue(b)) > 0.6f) {
            axes[2] = a;
            axes[3] = b;
        }
    }

    public void process(MotionEvent e, float[] out) {
        float[] raw = new float[4];
        for (int i = 0; i < 4; i++) raw[i] = e.getAxisValue(axes[i]);
        if (calibrating) {
            System.arraycopy(raw, 0, buf[n++], 0, 4);
            if (n >= buf.length) finish();
        } else {
            relearn(e, raw);
        }
        for (int i = 0; i < 4; i++) {
            float v = raw[i] - center[i];
            float s = Math.signum(v);
            v = Math.abs(v);
            out[i] = v < dead[i] ? 0f : s * Math.min(1f, (v - dead[i]) / (1f - dead[i]));
        }
        out[4] = lTrig < 0 ? 0f : e.getAxisValue(lTrig);
        out[5] = rTrig < 0 ? 0f : e.getAxisValue(rTrig);
    }

    private void finish() {
        for (int i = 0; i < 4; i++) {
            float m = 0;
            for (float[] r : buf) m += r[i];
            m /= buf.length;
            float dev = 0;
            for (float[] r : buf) dev = Math.max(dev, Math.abs(r[i] - m));
            center[i] = m;
            dead[i] = Math.min(0.3f, Math.max(0.06f, dev * 1.5f));
        }
        calibrating = false;
        save();
    }
}
