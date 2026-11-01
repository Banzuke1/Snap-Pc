package com.winlator;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SnapApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"logcat", "-d", "-t", "400", "*:W"});
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                ArrayList<String> lines = new ArrayList<>();
                String l;
                while ((l = r.readLine()) != null) lines.add(l);
                StringBuilder sb = new StringBuilder();
                for (int i = Math.max(0, lines.size() - 70); i < lines.size(); i++) sb.append(lines.get(i)).append('\n');
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("log", sb.toString()));
                Toast.makeText(this, "Naplo a vagolapra masolva", Toast.LENGTH_LONG).show();
            } catch (Exception e) {}
        }, 4000);
    }
}
