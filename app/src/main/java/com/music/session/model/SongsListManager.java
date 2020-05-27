package com.music.session.model;

import android.content.Context;

public class SongsListManager {
    private static int numTypes = 0;
    public static final int SONGS = numTypes++;
    public static final int ARTISTS = numTypes++;

    private static SongsListManager manager;
    private StorageUtil storage = StorageUtil.getInstance();
    private AbstractSongsList[] listTypes = new AbstractSongsList[numTypes];
    private int listType = 0;

    public static SongsListManager getManager(Context context) {
        if (manager == null) {
            manager = new SongsListManager(context);
        }
        return manager;
    }
    static SongsListManager getManager() throws InstantiationError{
        if (manager == null) {
            throw new InstantiationError();
        }
        return manager;
    }
    private SongsListManager(Context context) {
        listTypes[SONGS] = new SongsListSongs(context);
        listTypes[ARTISTS] = new SongsListArtists(context);
    }

    public void setListType(int type) {
        listType = type;
    }

    Audio getSong(int index) {
        return listTypes[listType].getSong(index);
    }

    public void storeIndex(int index) {
        storage.storeAudioIndex(index);
    }
    int loadIndex() {
        return storage.loadAudioIndex();
    }

    boolean isEmptyList() {
        return listTypes[0].isEmpty();
    }
    int getSize() {
        return listTypes[0].getSize();
    }
}
