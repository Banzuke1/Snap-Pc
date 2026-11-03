package com.snappc.optimizer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

public class AutoProfile {
    public final String name;
    public final int width, height, fpsCap;
    public final float tempC;
    public final int level, thermal;
    public final boolean plugged;

    private AutoProfile(String n, int w, int h, int f, float t, int l, int th, boolean p) {
        name = n; width = w; height = h; fpsCap = f;
        tempC = t; level = l; thermal = th; plugged = p;
    }

    public static AutoProfile choose(Context c) {
        Intent b = c.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        float temp = 30f;
        int level = 100;
        boolean plugged = false;
        if (b != null) {
            temp = b.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300) / 10f;
            int scale = Math.max(1, b.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
            level = b.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) * 100 / scale;
            plugged = b.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
        }
        int thermal = 0;
        if (Build.VERSION.SDK_INT >= 29) {
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            if (pm != null) thermal = pm.getCurrentThermalStatus();
        }
        if (temp >= 40f || thermal >= 2 || (!plugged && level < 20))
            return new AutoProfile("COOL", 854, 480, 30, temp, level, thermal, plugged);
        if (plugged && temp < 36f && thermal == 0)
            return new AutoProfile("PERFORMANCE", 1280, 720, 30, temp, level, thermal, plugged);
        return new AutoProfile("BALANCED", 960, 540, 30, temp, level, thermal, plugged);
    }
}
