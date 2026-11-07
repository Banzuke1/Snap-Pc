package com.winlator.cmod.inputcontrols;
import android.view.InputDevice;
import android.view.MotionEvent;

public class PadProfile {
    private static boolean heldL2, heldR2;

    public static boolean onKey(int idx, boolean pressed, GamepadState s) {
        if (idx == ExternalController.IDX_BUTTON_L2) {
            heldL2 = pressed;
            s.triggerL = pressed ? 1.0f : 0.0f;
            s.setPressed(idx, pressed);
            return true;
        }
        if (idx == ExternalController.IDX_BUTTON_R2) {
            heldR2 = pressed;
            s.triggerR = pressed ? 1.0f : 0.0f;
            s.setPressed(idx, pressed);
            return true;
        }
        return false;
    }

    public static void onMotion(MotionEvent e, GamepadState s) {
        float l = Math.max(e.getAxisValue(MotionEvent.AXIS_LTRIGGER), e.getAxisValue(MotionEvent.AXIS_BRAKE));
        float r = Math.max(e.getAxisValue(MotionEvent.AXIS_RTRIGGER), e.getAxisValue(MotionEvent.AXIS_GAS));
        if (heldL2) l = 1.0f;
        if (heldR2) r = 1.0f;
        s.triggerL = l;
        s.triggerR = r;
        s.setPressed(ExternalController.IDX_BUTTON_L2, l > 0.5f);
        s.setPressed(ExternalController.IDX_BUTTON_R2, r > 0.5f);
        InputDevice d = e.getDevice();
        if (d != null && d.getMotionRange(MotionEvent.AXIS_HAT_X) != null) {
            float x = e.getAxisValue(MotionEvent.AXIS_HAT_X);
            float y = e.getAxisValue(MotionEvent.AXIS_HAT_Y);
            s.dpad[0] = y < -0.5f;
            s.dpad[1] = x > 0.5f;
            s.dpad[2] = y > 0.5f;
            s.dpad[3] = x < -0.5f;
        }
    }
}
