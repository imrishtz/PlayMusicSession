package com.music.session.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class StorageUtil {

    private final String STORAGE = " com.music.session.STORAGE";
    private SharedPreferences mPreferences;
    private String TAG = "StorageUtil";
    private static volatile StorageUtil sStorage;
    private StorageUtil(Context context) {
        mPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
    }
    public static void  initStorage(Context context) {
        sStorage = new StorageUtil(context);
    }
    static StorageUtil getInstance() {
        return sStorage;
    }

    void storeAudioIndex(int index) {
        Log.v(TAG, "storeAudioIndex = " + index);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    int loadAudioIndex() {
        Log.v(TAG, "loadAudioIndex = " + mPreferences.getInt("audioIndex", -1));
        return mPreferences.getInt("audioIndex", 0);//return -1 if no data found
    }

    void clearCachedAudioPlaylist() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
