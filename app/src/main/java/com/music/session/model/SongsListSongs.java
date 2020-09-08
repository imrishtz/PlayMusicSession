package com.music.session.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SongsListSongs extends AbstractSongsList {
    private static int[] titleIndexToReal;
    private static int[] realIndexToTitle;


    public SongsListSongs(Context context) {
        super(context);
        ArrayList<Audio> songsList = getAllSongs();
        titleIndexToReal = new int[songsList.size()];
        realIndexToTitle = new int[songsList.size()];

        ArrayList<Audio> sortedList = new ArrayList<>(songsList);
        Collections.sort(sortedList, new Comparator<Audio>() {
            @Override
            public int compare(final Audio object1, final Audio object2) {
                return object1.getTitle().compareTo(object2.getTitle());
            }
        });
        for (int i = 0; i < songsList.size(); ++i) {
            titleIndexToReal[i] = sortedList.get(i).index;
            realIndexToTitle[sortedList.get(i).index] = i;
        }
    }
    @Override
    public Audio getSong(int index) {
        return data.getSong(titleIndexToReal[index]);
    }

    public static int getTitleIndex(int index) {
        return realIndexToTitle[index];
    }
}
