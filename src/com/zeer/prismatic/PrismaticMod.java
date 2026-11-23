package com.zeer.prismatic;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.gdpsmods.loader.ModEntry;

public class PrismaticMod implements ModEntry {
    public static long start;

    @Override
    public void onLoad(final Activity a) {
        start = System.currentTimeMillis();
        new Thread(new Runnable() {
            public void run() { Patcher.load(a); }
        }).start();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                Button b = new Button(a);
                b.setText("Prismatic");
                b.setAllCaps(false);
                b.setTextSize(12);
                b.setTextColor(0xFFFFFFFF);
                b.setBackgroundColor(0xCC4B2E83);
                float d = a.getResources().getDisplayMetrics().density;
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) (80 * d), (int) (36 * d));
                lp.gravity = Gravity.TOP | Gravity.START;
                lp.topMargin = (int) (8 * d);
                lp.leftMargin = (int) (8 * d);

                final float[] t = new float[5];
                b.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent e) {
                        switch (e.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                t[0] = e.getRawX() - v.getTranslationX();
                                t[1] = e.getRawY() - v.getTranslationY();
                                t[2] = e.getRawX();
                                t[3] = e.getRawY();
                                t[4] = 0;
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                if (Math.abs(e.getRawX() - t[2]) + Math.abs(e.getRawY() - t[3]) > 25) t[4] = 1;
                                v.setTranslationX(e.getRawX() - t[0]);
                                v.setTranslationY(e.getRawY() - t[1]);
                                return true;
                            case MotionEvent.ACTION_UP:
                                if (t[4] == 0) PrismaticMenu.show(a);
                                return true;
                        }
                        return false;
                    }
                });
                a.addContentView(b, lp);
                Toast.makeText(a, "Prismatic Menu cargado", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }
                  }
