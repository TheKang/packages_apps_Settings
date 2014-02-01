package com.android.settings.slim.performance;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesProvider {

    public static final String SETTINGS_KEY = "com.slim.performance_preferences";
    public static final String VOLTAGE_KEY = "com.slim.performance_voltages";
    public static final String GOVERNOR_KEY = "com.slim.performance_governor_";
    public static final String IO_KEY = "com.slim.performance_io_";

    public static final String VOLT_SET_ON_BOOT = "volt_set_on_boot";

    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences(SETTINGS_KEY, 0);
    }

    public static SharedPreferences.Editor put(Context context) {
        return context.getSharedPreferences(SETTINGS_KEY, 0).edit();
    }

    public static int getInt(Context context, String key, int def) {
        return get(context).getInt(key, def);
    }

    public static void putInt(Context context, String key, int value) {
        put(context).putInt(key, value).commit();
    }

    public static String getString(Context context, String key, String def) {
        return get(context).getString(key ,def);
    }

    public static void putString(Context context, String key, String value) {
        put(context).putString(key, value).commit();
    }

    public static boolean getBoolean(Context context, String key, boolean def) {
        return get(context).getBoolean(key, def);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        put(context).putBoolean(key, value).commit();
    }

    public static void putVoltage(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(VOLTAGE_KEY, 0).edit();
        editor.putString(key, value).commit();
    }

    public static void putGovernor(Context context, String key, String value) {
        String key_file = GOVERNOR_KEY + Utils.readOneLine(Paths.GOV_FILE).toLowerCase();
        SharedPreferences.Editor editor = context.getSharedPreferences(key_file, 0).edit();
        editor.putString(key, value).commit();
    }

    public static String getGovernor(Context context, String key, String def) {
        String key_file = GOVERNOR_KEY + Utils.readOneLine(Paths.GOV_FILE).toLowerCase();
        SharedPreferences pref = context.getSharedPreferences(key_file, 0);
        return pref.getString(key, def);
    }

    public static void putIO(Context context, String key, String value) {
        String key_file = IO_KEY + Utils.getCurrentIOScheduler();
        SharedPreferences.Editor editor = context.getSharedPreferences(key_file, 0).edit();
        editor.putString(key, value).commit();
    }

    public static String getIO(Context context, String key, String def) {
        String key_file = IO_KEY + Utils.getCurrentIOScheduler();
        SharedPreferences pref = context.getSharedPreferences(key_file, 0);
        return pref.getString(key, def);
    }
}
