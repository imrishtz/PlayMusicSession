package com.music.session;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SongsList {

    private final String TAG = "SongsList";
    private static volatile SongsList sSongsListInstance = new SongsList();
    private List<Audio> mSongsList;
    private Context mContext;

    public static SongsList getInstance() {
        return sSongsListInstance;
    }

    private SongsList(){}

    public void setSongsList(List<Audio> songsList) {
        mSongsList = songsList;
    }

    public List<Audio> getAllSongs(Context context) {
        mContext = context;
        getAllAudioFromDevice(context);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                for (Audio song : mSongsList) {
                    mmr.setDataSource(mContext, Uri.parse(song.getUri()));
                    byte[] data = mmr.getEmbeddedPicture();
                    if (data != null) {
                        song.setClipArt(BitmapFactory.decodeByteArray(data, 0, data.length));
                    }
                }
            }
        });
        return mSongsList;
    }

    public List<Audio> sortByAbc() {
        List<Audio> sortedSongsList = mSongsList;
        Collections.sort(sortedSongsList, new Comparator<Audio>() {
            @Override
            public int compare(final Audio object1, final Audio object2) {
                return object1.getTitle().compareTo(object2.getTitle());
            }
        });
        return sortedSongsList;
    }
    public Audio getSongByIndex(int index) {
        return mSongsList.get(index % mSongsList.size());
    }
    private void getAllAudioFromDevice(final Context context) {
        final List<Audio> tempAudioList = new ArrayList<>();
        final String [] STAR= {"*"};
        String selectionMusic = MediaStore.Audio.Media.IS_MUSIC + "!= ? AND ";
        String selectionMp3 = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";
        String ext = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
        String[] selExtARGS = new String[]{" 0",ext};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor= context.getContentResolver().query(uri, null, selectionMusic + selectionMp3 , selExtARGS, null);
        if (cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                context.grantUriPermission(context.getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // Save to audioList
                tempAudioList.add(new Audio(contentUri.toString(), title, album, artist, name, duration));
            }
        }
        cursor.close();
        mSongsList = tempAudioList;
    }

    private Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    public class SetAlbumImageThread extends Thread {

        public void run(){
            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (Audio song : mSongsList) {
                mmr.setDataSource(mContext, Uri.parse(song.getUri()));
                byte[] data = mmr.getEmbeddedPicture();
                if (data != null) {
                    song.setClipArt(BitmapFactory.decodeByteArray(data, 0, data.length));
                }
            }
        }
    }
}
