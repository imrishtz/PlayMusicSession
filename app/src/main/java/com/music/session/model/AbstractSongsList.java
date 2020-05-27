package com.music.session.model;

import android.content.Context;
import java.util.ArrayList;

public abstract class AbstractSongsList {

    static SongsListData data = null;
    static final int INVALID_INDEX = -1;

    AbstractSongsList(Context context){
        if (data == null) {
            data = SongsListData.getSongsListData(context);
        }
    }

    abstract public Audio getSong(int index);
    public ArrayList<Audio> getAllSongs() {return data.getAllSongs();}
    public int getSize() {return  data.getSize();}
    boolean isEmpty() {return  data.isEmpty();}

}
