package com.music.session.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SongsListArtists extends AbstractSongsList {

    private static int[] ArtistIndexToReal;
    public SongsListArtists(Context context) {
        super(context);
        ArrayList<Audio> songsList = getAllSongs();
        ArtistIndexToReal = new int[songsList.size()];
        ArrayList<Audio> sortedList = new ArrayList<>(songsList);
        Collections.sort(sortedList, new Comparator<Audio>() {
            @Override
            public int compare(final Audio song1, final Audio song2) {
                String x1 = song1.getArtist();
                String x2 = song2.getArtist();
                int sComp = x1.compareTo(x2);

                if (sComp != 0) {
                    return sComp;
                }

                String y1 = song1.getAlbum();
                String y2 = song2.getAlbum();
                return y1.compareTo(y2);
            }
        });
        for (int i = 0; i < songsList.size(); ++i) {
            ArtistIndexToReal[i] =  sortedList.get(i).index;
        }

    }

    @Override
    public ArrayList<Audio> getAllSongs() {
        return data.getAllSongs();
    }

    @Override
    public Audio getSong(int index) {
        return data.getSong(ArtistIndexToReal[index]);
    }


}
