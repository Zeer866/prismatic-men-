package com.zeer.prismatic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class PrismaticMenu {
    static final int ACC = 0xFFB48CFF, BG = 0xFF14111F;

    static int dp(Activity a, int v) { return (int) (v * a.getResources().getDisplayMetrics().density); }

    static TextView text(Activity a, String s, int color, int size, boolean bold) {
        TextView t = new TextView(a);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(size);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(a, 10), 0, dp(a, 4));
        return t;
    }

    static Switch sw(Activity a, String label, boolean on, CompoundButton.OnCheckedChangeListener l) {
        Switch s = new Switch(a);
        s.setText(label);
        s.setTextColor(0xFFFFFFFF);
        s.setChecked(on);
        s.setPadding(0, dp(a, 6), 0, dp(a, 6));
        s.setOnCheckedChangeListener(l);
        return s;
    }

    static void btn(Activity a, LinearLayout col, String label, View.OnClickListener l) {
        Button b = new Button(a);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(0xFFFFFFFF);
        b.setBackgroundColor(0xFF2A2340);
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(a, 6);
        col.addView(b, lp);
    }

    static void toast(Activity a, String s) { Toast.makeText(a, s, Toast.LENGTH_LONG).show(); }

    public static void show(final Activity a) {
        LinearLayout col = new LinearLayout(a);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setBackgroundColor(BG);
        col.setPadding(dp(a, 18), dp(a, 10), dp(a, 18), dp(a, 10));
        col.addView(text(a, "Prismatic Menu  -  by Zeer", ACC, 18, true));

        col.addView(text(a, "PARCHES DEL JUEGO", ACC, 12, true));
        JSONArray ps = Patcher.patches;
        if (ps.length() == 0)
            col.addView(text(a, "Sin parches cargados. Revisa tu conexion y patches.json", 0xFFAAAAAA, 12, false));
        for (int i = 0; i < ps.length(); i++) {
            final JSONObject p = ps.optJSONObject(i);
            if (p == null) continue;
            col.addView(sw(a, p.optString("name") + "\n" + p.optString("desc"),
                Patcher.originals.containsKey(p.optString("id")),
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton b, boolean on) {
                        if (b.getTag() != null) return;
                        String err = Patcher.apply(p, on);
                        if (err != null) {
                            b.setTag(1);
                            b.setChecked(!on);
                            b.setTag(null);
                            toast(a, err);
                        }
                    }
                }));
        }

        col.addView(text(a, "HERRAMIENTAS", ACC, 12, true));
        col.addView(sw(a, "Contador de FPS", Tools.fpsOn, new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean on) { Tools.setFps(a, on); }
        }));
        col.addView(sw(a, "Pantalla siempre encendida", Tools.keepOn, new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean on) { Tools.setKeepOn(a, on); }
        }));
        col.addView(sw(a, "Brillo al maximo", Tools.brightOn, new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean on) { Tools.setBright(a, on); }
        }));

        col.addView(text(a, "DATOS", ACC, 12, true));
        btn(a, col, "Hacer backup de mis datos", new View.OnClickListener() {
            public void onClick(View v) { toast(a, Tools.backup(a)); }
        });
        btn(a, col, "Restaurar ultimo backup", new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(a)
                    .setTitle("Restaurar backup")
                    .setMessage("Se reemplazan tus datos actuales y el juego se cierra. Despues abrilo de nuevo.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Restaurar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) { toast(a, Tools.restore(a)); }
                    }).show();
            }
        });

        col.addView(text(a, "SISTEMA", ACC, 12, true));
        btn(a, col, "Diagnostico", new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(a).setMessage(Tools.info(a)).setPositiveButton("OK", null).show();
            }
        });
        btn(a, col, "Recargar parches", new View.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        final String r = Patcher.load(a);
                        a.runOnUiThread(new Runnable() {
                            public void run() { toast(a, "Parches: " + r + " (reabri el menu)"); }
                        });
                    }
                }).start();
            }
        });

        ScrollView sv = new ScrollView(a);
        sv.setBackgroundColor(BG);
        sv.addView(col);
        new AlertDialog.Builder(a).setView(sv).setPositiveButton("Cerrar", null).show();
    }
                    }
