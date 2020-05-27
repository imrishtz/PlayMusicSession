package com.music.session.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class StorageUtil {

    private final String STORAGE = " com.music.session.STORAGE";
    private SharedPreferences preferences;
    private Context context;
    private String TAG = "StorageUtil";
    private static volatile StorageUtil sStorage;
    private StorageUtil(Context context) {
        this.context = context;
    }
    public static void  initStorage(Context context) {
        sStorage = new StorageUtil(context);
    }
    static StorageUtil getInstance() {
        return sStorage;
    }

    void storeAudioIndex(int index) {
        Log.v(TAG, "storeAudioIndex = " + index);
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Log.v(TAG, "loadAudioIndex = " + preferences.getInt("audioIndex", -1));
        return preferences.getInt("audioIndex", 0);//return -1 if no data found
    }

    void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

}
