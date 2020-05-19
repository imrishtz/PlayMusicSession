package com.music.session.model;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class Audio implements Serializable {

    private final String uri;
    private String title;
    private String name;
    private String album;
    private Long albumId;
    private String artist;
    private String duration;
    private Bitmap mClipArt = null;

    public Audio(String uri, String title, String album, String artist, String name,
                 String duration) {
        this.uri = uri;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.name = name;
        this.duration = duration;
    }

    public String getUri() {
        return uri;
    }
    public String getDuration() {
        return duration;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setClipArt(Bitmap clipart) {
        mClipArt = clipart;
    }
    public Bitmap getClipArt() { return mClipArt;}
}