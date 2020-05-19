package com.music.session.model;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.music.session.R;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.security.auth.login.LoginException;

public class SongsList {

    private final String TAG = "SongsList";
    private ArrayList<SongIndexing> mSongsList;
    private Context mContext;
    private static volatile SongsList sSongsListInstance = null;
    private StorageUtil storage;
    public static final int INVALID_INDEX = -1;
    public static void initSongsList(Context context) {
        if (sSongsListInstance == null) {
            sSongsListInstance = new SongsList(context);
        }
    }
    public static SongsList getInstance() {
        return sSongsListInstance;
    }

    private SongsList(Context context){
        Log.v(TAG, "imri songlist created");
        storage = StorageUtil.getInstance();
        mContext = context;
        getAllAudioFromDevice(context);
        fillSortedIndexMap();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "start run: imri");
                int size = mSongsList.size();
                new getImagesAsyncTask().execute(new Range(0, size / 3));
                new getImagesAsyncTask().execute(new Range(size / 3, size * 2 / 3));
                new getImagesAsyncTask().execute(new Range(size * 2 / 3, size));

                /*
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                for (SongIndexing song : mSongsList) {
                    mmr.setDataSource(mContext.getApplicationContext(), Uri.parse(song.getUri()));

                    byte[] data = mmr.getEmbeddedPicture();
                    if (data != null) {
                        song.setClipArt(BitmapFactory.decodeByteArray(data, 0, data.length));
                    }
                }

                mmr.release();

                 */


                if (!mSongsList.isEmpty()) {
                    storage.storeAudio(mSongsList);
                    storage.onClipartsReadyEvent();
                }
            }
        });
    }
    class Range {
        int from;
        int to;
        Range(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
    public class getImagesAsyncTask extends AsyncTask<Range, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Range... params) {
            // Do blah blah with param1 and param2
            Range myClass = params[0];
            int from = myClass.from;
            int to = myClass.to;

            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (int i = from; i < to; ++i) {
                SongIndexing song = mSongsList.get(i);
                mmr.setDataSource(mContext.getApplicationContext(), Uri.parse(song.getUri()));
                byte[] data = mmr.getEmbeddedPicture();
                if (data != null) {
                    song.setClipArt(BitmapFactory.decodeByteArray(data, 0, data.length));
                }
            }
            mmr.release();
            finished(to);
            return null;
        }
    }
    public ArrayList<SongIndexing> getAllSongs() {
        return mSongsList;
    }
    volatile int counter = 0;
    private void finished(int who) {
        ++counter;
        if (!mSongsList.isEmpty()) {
            storage.onClipartsReadyEvent();
            storage.storeAudio(mSongsList);
        }
        if (counter > 2) {
            Log.i(TAG, "finish run: imri");
        }
    }
    private void getAllAudioFromDevice(final Context context) {
        final ArrayList<SongIndexing> tempAudioList = new ArrayList<>();
        final String [] STAR= {"*"};
        String selectionMusic = MediaStore.Audio.Media.IS_MUSIC + "!= ? AND ";
        String selectionMp3 = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";
        String ext = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
        String[] selExtARGS = new String[]{" 0",ext};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        int i = 0;
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
                tempAudioList.add(new SongIndexing(i++, new Audio(contentUri.toString(), title, album, artist, name, duration)));
            }
        }
        cursor.close();
        mSongsList = tempAudioList;
        storage.storeAudio(mSongsList);
    }

    void fillSortedIndexMap() {
        ArrayList<SongIndexing> sortedList = new ArrayList<>(mSongsList);
        titleIndexToReal = new int[mSongsList.size()];
        ArtistIndexToReal = new int[mSongsList.size()];
        RealIndexToTitle = new int[mSongsList.size()];
        RealIndexToArtist = new int[mSongsList.size()];
        Collections.sort(sortedList, new Comparator<SongIndexing>() {
            @Override
            public int compare(final SongIndexing object1, final SongIndexing object2) {
                return object1.mAudio.getTitle().compareTo(object2.mAudio.getTitle());
            }
        });
        for (int i = 0; i < mSongsList.size(); ++i) {
            titleIndexToReal[i] = sortedList.get(i).mIndex;
            RealIndexToTitle[sortedList.get(i).mIndex]= i;
        }
        Collections.sort(sortedList, new Comparator<SongIndexing>() {
            @Override
            public int compare(final SongIndexing song1, final SongIndexing song2) {
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
        for (int i = 0; i < mSongsList.size(); ++i) {
            ArtistIndexToReal[i] =  sortedList.get(i).mIndex;
            RealIndexToArtist[sortedList.get(i).mIndex] = i;
        }
    }
    private static int[] titleIndexToReal;
    private static int[] ArtistIndexToReal;
    private static int[] RealIndexToTitle;
    private static int[] RealIndexToArtist;

    public static int getRealIndexFromTitle(int titleIndex) { return titleIndexToReal[titleIndex];}
    public static int getTitleIndexFromReal(int realIndex) { return RealIndexToTitle[realIndex];}
    public static int getRealIndexFromArtist(int artistIndex) { return ArtistIndexToReal[artistIndex];}
    public static int getArtistIndexFromReal(int realIndex) { return RealIndexToArtist[realIndex];}
}
