package com.zeer.prismatic;

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Patcher {
    public static final String LIB = "libcocos2dcpp.so";
    public static final String URL = "https://raw.githubusercontent.com/Zeer866/prismatic-men-/main/patches.json";
    public static volatile JSONArray patches = new JSONArray();
    public static final Map<String, List<byte[]>> originals = new HashMap<String, List<byte[]>>();
    static long base = 0;

    static String read(InputStream in) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        int n;
        while ((n = in.read(b)) > 0) o.write(b, 0, n);
        in.close();
        return o.toString("UTF-8");
    }

    public static String load(Activity a) {
        File cache = new File(a.getFilesDir(), "prismatic_patches.json");
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(URL).openConnection();
            c.setConnectTimeout(6000);
            c.setReadTimeout(8000);
            String txt = read(c.getInputStream());
            new JSONObject(txt); // validar antes de guardar
            FileOutputStream f = new FileOutputStream(cache);
            f.write(txt.getBytes("UTF-8"));
            f.close();
        } catch (Exception e) { /* sin red: usa la copia guardada */ }
        try {
            patches = new JSONObject(read(new FileInputStream(cache))).getJSONArray("patches");
            return "ok";
        } catch (Exception e) {
            return "sin patches.json (" + e.getMessage() + ")";
        }
    }

    public static long findBase() throws Exception {
        BufferedReader r = new BufferedReader(new FileReader("/proc/self/maps"));
        String l;
        long min = 0;
        while ((l = r.readLine()) != null) {
            if (l.contains(LIB)) {
                long s = Long.parseLong(l.substring(0, l.indexOf('-')), 16);
                if (min == 0 || s < min) min = s;
            }
        }
        r.close();
        if (min == 0) throw new Exception("no encontre " + LIB + " en memoria");
        return min;
    }

    static byte[] hex(String s) {
        s = s.replace(" ", "");
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) b[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        return b;
    }

    /** Devuelve null si salio bien, o el texto del error. */
    public static String apply(JSONObject p, boolean on) {
        try {
            String id = p.getString("id");
            JSONArray ed = p.optJSONArray(android.os.Process.is64Bit() ? "arm64" : "arm32");
            if (ed == null || ed.length() == 0) return "Sin parche para esta arquitectura";
            for (int i = 0; i < ed.length(); i++)
                if (ed.getJSONObject(i).optString("offset", "").isEmpty()) return "Falta el offset en patches.json";
            if (base == 0) base = findBase();
            RandomAccessFile mem = new RandomAccessFile("/proc/self/mem", "rw");
            try {
                if (on) {
                    if (originals.containsKey(id)) return null;
                    List<byte[]> orig = new ArrayList<byte[]>();
                    for (int i = 0; i < ed.length(); i++) {
                        JSONObject e = ed.getJSONObject(i);
                        long addr = base + Long.decode(e.getString("offset"));
                        byte[] n = hex(e.getString("on"));
                        byte[] o = new byte[n.length];
                        mem.seek(addr);
                        mem.readFully(o);
                        orig.add(o);
                        mem.seek(addr);
                        mem.write(n);
                    }
                    originals.put(id, orig);
                } else {
                    List<byte[]> orig = originals.get(id);
                    if (orig == null) return null;
                    for (int i = 0; i < ed.length(); i++) {
                        long addr = base + Long.decode(ed.getJSONObject(i).getString("offset"));
                        mem.seek(addr);
                        mem.write(orig.get(i));
                    }
                    originals.remove(id);
                }
            } finally {
                mem.close();
            }
            return null;
        } catch (Throwable t) {
            return "Error: " + t.getMessage();
        }
    }
    }
