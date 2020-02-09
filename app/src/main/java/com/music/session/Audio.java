package com.music.session;

import android.net.Uri;

import java.io.Serializable;

public class Audio implements Serializable {

    private final Uri uri;
    private String title;
    private String name;
    private String album;
    private String artist;

    public Audio(Uri uri, String title, String album, String artist, String name) {
        this.uri = uri;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.name = name;
    }

    public Uri getUri() {
        return uri;
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
}
