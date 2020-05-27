package com.music.session.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class Audio implements Serializable {

    private static int uniqueIdCounter = 0;
    public int index;
    private final String uri;
    private String title;
    private String name;
    private String album;
    private String albumId;
    private String artist;
    private String duration;
    private byte[] arr;
    private Bitmap mClipArt = null;

    public Audio(String uri, String title, String album, String albumId, String artist, String name,
                 String duration) {
        index = uniqueIdCounter++;
        this.uri = uri;
        this.title = title;
        this.album = album;
        this.albumId = albumId;
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
    public String getAlbum() {
        return album;
    }
    public String getAlbumId() {
        return albumId;
    }
    public String getArtist() {
        return artist;
    }
    public void setClipArt(Bitmap clipart) {
        mClipArt = clipart;
    }
    public Bitmap getClipArt() { return mClipArt;}

    public void setByte(byte[] byteArray) {
        arr = byteArray;
    }

    public byte[] getByte() {
        return arr;
    }
}
