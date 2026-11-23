package com.zeer.prismatic;

import android.app.Activity;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tools {
    public static boolean fpsOn, keepOn, brightOn;
    static TextView fpsView;
    static long last, frames;

    public static void setFps(Activity a, boolean on) {
        fpsOn = on;
        if (fpsView == null) {
            fpsView = new TextView(a);
            fpsView.setTextColor(0xFFB48CFF);
            fpsView.setBackgroundColor(0x88000000);
            fpsView.setTextSize(11);
            fpsView.setPadding(8, 2, 8, 2);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
            lp.gravity = Gravity.BOTTOM | Gravity.START;
            a.addContentView(fpsView, lp);
        }
        fpsView.setVisibility(on ? View.VISIBLE : View.GONE);
        if (!on) return;
        last = System.nanoTime();
        frames = 0;
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            public void doFrame(long t) {
                if (!fpsOn) return;
                frames++;
                if (t - last >= 1000000000L) {
                    fpsView.setText(frames + " FPS");
                    frames = 0;
                    last = t;
                }
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

    public static void setKeepOn(Activity a, boolean on) {
        keepOn = on;
        if (on) a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static void setBright(Activity a, boolean on) {
        brightOn = on;
        WindowManager.LayoutParams lp = a.getWindow().getAttributes();
        lp.screenBrightness = on ? 1f : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        a.getWindow().setAttributes(lp);
    }

    static void copy(File a, File b) throws IOException {
        InputStream in = new FileInputStream(a);
        OutputStream out = new FileOutputStream(b);
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        in.close();
        out.close();
    }

    public static String backup(Activity a) {
        try {
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File dst = new File(a.getExternalFilesDir(null), "prismatic_backup/" + stamp);
            dst.mkdirs();
            int n = 0;
            File[] fs = a.getFilesDir().listFiles();
            if (fs != null) for (File f : fs)
                if (f.getName().startsWith("CC") && f.getName().endsWith(".dat")) {
                    copy(f, new File(dst, f.getName()));
                    n++;
                }
            return n + " archivos guardados en prismatic_backup/" + stamp;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /** Restaura el ultimo backup y cierra el juego para que no lo sobrescriba. */
    public static String restore(Activity a) {
        try {
            File[] ds = new File(a.getExternalFilesDir(null), "prismatic_backup").listFiles();
            if (ds == null || ds.length == 0) return "No hay backups";
            Arrays.sort(ds);
            File[] fs = ds[ds.length - 1].listFiles();
            if (fs == null || fs.length == 0) return "El backup esta vacio";
            for (File f : fs) copy(f, new File(a.getFilesDir(), f.getName()));
            android.os.Process.killProcess(android.os.Process.myPid());
            return "ok";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static String info(Activity a) {
        String base;
        try { base = "0x" + Long.toHexString(Patcher.findBase()); }
        catch (Exception e) { base = "error: " + e.getMessage(); }
        long s = (System.currentTimeMillis() - PrismaticMod.start) / 1000;
        return "Prismatic Menu 1.0 by Zeer\n"
            + "Arquitectura: " + (android.os.Process.is64Bit() ? "arm64" : "arm32") + "\n"
            + "Base de " + Patcher.LIB + ": " + base + "\n"
            + "Parches cargados: " + Patcher.patches.length() + "\n"
            + "Sesion: " + (s / 60) + "m " + (s % 60) + "s\n"
            + "Android " + android.os.Build.VERSION.RELEASE;
    }
          }
