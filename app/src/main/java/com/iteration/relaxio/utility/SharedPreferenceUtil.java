package com.iteration.relaxio.utility;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iteration.relaxio.model.Sound;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for storing and retrieving shared preference values.
 */
public class SharedPreferenceUtil {

    public static String getPreference(Context context, String prefKey, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(prefKey, defValue);
    }

    public static void setPreference(Context context, String prefKey, String value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(prefKey, value);
        editor.commit();
    }

    public static List<Sound> getSoundList(Context context, String prefKey) {
        Gson gson = new Gson();
        List<Sound> soundList = new ArrayList<>();
        Type type = new TypeToken<List<Sound>>() {
        }.getType();
        soundList = gson.fromJson(PreferenceManager.getDefaultSharedPreferences(context).getString(prefKey, ""), type);
        return soundList;
    }

    public static void setSoundList(Context context, String prefKey, List<Sound> list) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(prefKey, json);
        editor.commit();
    }

    public static boolean getPreference(Context context, String prefKey, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(prefKey, defValue);
    }

    public static void setPreference(Context context, String prefKey, boolean value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(prefKey, value);
        editor.commit();
    }

    public static int getPreference(Context context, String prefKey, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(prefKey, defValue);
    }

    public static void setPreference(Context context, String prefKey, int value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(prefKey, value);
        editor.commit();
    }

    public static long getPreference(Context context, String prefKey, long defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(prefKey, defValue);
    }

    public static void setPreference(Context context, String prefKey, long value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(prefKey, value);
        editor.commit();
    }

    public static void clearAllData(Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }
}