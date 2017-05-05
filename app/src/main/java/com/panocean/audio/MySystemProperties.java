package com.panocean.audio;

import java.lang.reflect.Method;

/**
 * Created by chan on 7/27/16.
 */
public class MySystemProperties extends Exception {
    private static final String TAG = "MySystemProperties";

    // String SystemProperties.get(String key, String def){}
    public static String get(String key, String def) {
        String str = new String();
        try {
            str = Reflect.on(Class.forName("android.os.SystemProperties")).call("get",key, def).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    //int SystemProperties.get(String key, int def){}
    public static int getInt(String key, int def) {
        int value = 0;
        try {
          value = Reflect.on(Class.forName("android.os.SystemProperties")).call("getInt",key, def).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    //public static boolean getBoolean(String key, boolean def)
    public static boolean getBoolean(String key, boolean def) {
        boolean val = false;
        try {
            val = Reflect.on(Class.forName("android.os.SystemProperties")).call("getBoolean", key, def).get();
        } catch (Exception e){
            e.printStackTrace();
        }

        return val;
    }

    //public static long getLong(String key, long def)
    public static long getLong(String key, long def) {
        long val = 0;

        try {
            val = Reflect.on(Class.forName("android.os.SystemProperties")).call("getLong", key, def).get();
        } catch (Exception e){
            e.printStackTrace();
        }

        return val;
    }

    //public static void set(String key, String val)
    public static void set(String key, String def) {

        try {
            Reflect.on(Class.forName("android.os.SystemProperties")).call("set",key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getVersionRelease() {
        return get("ro.build.version.release", "none");
    }

}
