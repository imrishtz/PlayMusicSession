package com.music.session.model;

import android.graphics.Bitmap;

public class SongIndexing{
    int mIndex;
    Audio mAudio;

    SongIndexing(int index, Audio audio) {
        mIndex = index;
        mAudio = audio;
    }

    public String getUri() {
        return mAudio.getUri();
    }
    public String getDuration() {
        return mAudio.getDuration();
    }
    public String getTitle() {
        return mAudio.getTitle();
    }
    public String getAlbum() {
        return mAudio.getAlbum();
    }

    public String getArtist() {
        return mAudio.getArtist();
    }
    public void setClipArt(Bitmap clipart) {
        mAudio.setClipArt(clipart);
    }
    public Bitmap getClipArt() { return mAudio.getClipArt();}
    public int getRealIndex() {return mIndex;}

}