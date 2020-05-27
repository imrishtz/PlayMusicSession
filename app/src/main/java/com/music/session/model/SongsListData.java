package com.music.session.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.music.session.view.MainActivity;

import java.io.InputStream;
import java.util.ArrayList;

class SongsListData {
    private static final String TAG = "SongsListData";
    private static SongsListData data;
    private static ArrayList<Audio> mSongsList = new ArrayList<>();
    private static Context mContext;
    static SongsListData getSongsListData(Context context) {
        if (data == null) {
            data = new SongsListData(context);
        }
        return data;
    }

    private SongsListData(Context context) {
        getAllAudioFromDevice(context);
        mContext = context;
        final ContentResolver contentResolver = context.getContentResolver();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "start run: imri");
                new getImagesAsyncTask(contentResolver).execute();
            }
        });
    }

    private void getAllAudioFromDevice(final Context context) {
        String selectionMusic = MediaStore.Audio.Media.IS_MUSIC + "!= ? AND ";
        String selectionMp3 = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";
        String ext = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
        String[] selExtARGS = new String[]{" 0", ext};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, null, selectionMusic + selectionMp3, selExtARGS, null);
        if (cursor != null && cursor.getCount() > 0) {
            final ArrayList<Audio> tempAudioList = new ArrayList<>();
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                if (artist.equals("<unknown>")) {
                    artist = "Unknown";
                }
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                context.grantUriPermission(context.getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                tempAudioList.add(new Audio(contentUri.toString(), title, album, albumId, artist, name, duration));
            }
            cursor.close();
            mSongsList = tempAudioList;
        }
    }

    ArrayList<Audio> getAllSongs() {
        return mSongsList;
    }

    private static class getImagesAsyncTask extends AsyncTask<Object, Void, Void> {
        ContentResolver mContentResolver;
        getImagesAsyncTask(ContentResolver contentResolver) {
            mContentResolver = contentResolver;
        }
        @Override
        protected Void doInBackground(Object[] params) {
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Bitmap bitmap = null;
            String lastAlbumId = "";
            for (int i = 0; i < mSongsList.size(); ++i) {
                Audio song = mSongsList.get(i);
                String id = song.getAlbumId();
                if (!id.equals(lastAlbumId)) {
                    lastAlbumId = id;
                    Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(id));
                    InputStream in = null;
                    try {
                        in = mContentResolver.openInputStream(uri);
                    } catch (Exception e) {}
                    bitmap = BitmapFactory.decodeStream(in);
                }
                if (bitmap != null) {
                    song.setClipArt(bitmap);
                }
            }

            finished();
            return null;
        }

    }

    private static void finished() {
        Intent intent;
        if (!mSongsList.isEmpty()) {
            intent = new Intent(MainActivity.SHOW_MAIN);
        } else {
            intent = new Intent(MainActivity.NO_AUDIO);
        }
        mContext.sendBroadcast(intent);
        mContext = null;
        Log.i(TAG, "finished: imri");
    }
    int getSize(){ return mSongsList.size();}
    boolean isEmpty() {return mSongsList.isEmpty();}
    Audio getSong(int index) { return mSongsList.get(index);}
}
