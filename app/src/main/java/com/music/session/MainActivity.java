// Credit: https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/

package com.music.session;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Runnable, PopupMenu.OnMenuItemClickListener {
    private SongsList mSongListInstance;
    private MediaPlayerService player;
    boolean serviceBound = false;
    private boolean isThreadRunning = false;
    private Handler mainHandler = new Handler();
    Button playPauseButton, skipNextButton, resartOrLastButton, menu;
    Audio currentPlaying = null;
    SeekBar seekBar;
    TextView totalTime;
    TextView songName;
    TextView artistAlbumName;
    private int duration;
    ArrayList<Audio> audioList;
    String TAG = "MainActivity";
    Button firstFragmentButton, secondFragmentButton;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment playListsFragment;
    Fragment allSongsFragment;
    Context mContext;
    TextView seekBarHint;
    Switch shuffleSwitch;
    private final int TIME_TO_GO_LAST_SONG = 3000;
    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.music.session.PlayNewAudio";
    // Change to your package name
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate imri");
        mContext = this;
        mSongListInstance = SongsList.getInstance();
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();
        // TODO
        // Play from http
        //playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
        loadAudio();
//play the first audio in the ArrayList
        Log.v(TAG, "Try to play");
        //playAudio(audioList.get(0).getUri());


        // maybe delete up

        seekBar = findViewById(R.id.seekbar);
        songName = findViewById(R.id.song_name_music_player);
        artistAlbumName = findViewById(R.id.artist_and_album_music_player);
        totalTime = findViewById(R.id.total_time);
        seekBarHint = findViewById(R.id.curr_time);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.VISIBLE);
                totalTime.setVisibility(View.VISIBLE);
                //musicSrv.seekTo(seekBar.getProgress());
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);
                totalTime.setVisibility(View.VISIBLE);
                int x = (int) Math.ceil(progress);
                seekBarHint.setText(getTime(x));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress());
            }
        });
        menu = findViewById(R.id.menu_button);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu(view);
            }
        });
        playPauseButton = (Button) findViewById(R.id.play_pause);
        playPauseButton.setBackgroundResource(R.drawable.ic_play);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBound) {
                    if (player.isPlaying()) {
                        playPauseButton.setBackgroundResource(R.drawable.ic_play);
                        Intent broadcastIntent = new Intent(MediaPlayerService.ACTION_PAUSE);
                        sendBroadcast(broadcastIntent);
                    } else {
                        playPauseButton.setBackgroundResource(R.drawable.ic_pause);
                        Intent broadcastIntent = new Intent(MediaPlayerService.ACTION_PLAY);
                        sendBroadcast(broadcastIntent);
                    }
                }
            }
        });
        skipNextButton = (Button) findViewById(R.id.skip_next_song);
        skipNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent broadcastIntent = new Intent(MediaPlayerService.ACTION_NEXT);
                sendBroadcast(broadcastIntent);
            }
        });

        resartOrLastButton = (Button) findViewById(R.id.restart_or_last_song);
        resartOrLastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBound) {
                    if ((player.getCurrentPosition() < TIME_TO_GO_LAST_SONG) && (currentPlaying != null)) {
                        Intent broadcastIntent = new Intent(MediaPlayerService.ACTION_PREVIOUS);
                        sendBroadcast(broadcastIntent);
                    } else {
                        player.seekTo(0);
                    }
                }
            }
        });

        firstFragmentButton = (Button) findViewById(R.id.all_songs);
        secondFragmentButton = (Button) findViewById(R.id.play_lists);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        allSongsFragment = new AllSongs();
        fragmentTransaction.add(R.id.container, allSongsFragment, "check");
        fragmentTransaction.commit();
// perform setOnClickListener event on First Button
        firstFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// load First Fragment
                getSupportFragmentManager()
                        .beginTransaction().replace(R.id.container, allSongsFragment)
                        .commit();
            }
        });
// perform setOnClickListener event on Second Button
        secondFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playListsFragment = new PlayLists();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, playListsFragment)
                        .commit();
            }
        });

    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
           // Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    public void setSongList(List<Audio> songList) {
        mSongListInstance.setSongsList(songList);
    }

    public void playAudio(int audioIndex) {
        //Check is service is active
        seekBar.setProgress(0);
        duration = 0;
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(playerIntent);
            } else {
                startService(playerIntent);
            }
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);
            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
       /* currentPlaying = audioList.get(audioIndex);
        Log.v(TAG, "imri currentPlaying = " + currentPlaying);
        duration = Integer.valueOf(currentPlaying.getDuration());
        String time = getTime(duration);
        seekBar.setMax(duration);
        totalTime.setText(time);
        songName.setText(currentPlaying.getTitle());
        artistAlbumName.setText(currentPlaying.getArtist() + " - " + currentPlaying.getAlbum());
        */
        playPauseButton.setBackgroundResource(R.drawable.ic_pause);

        if (!isThreadRunning) {
            new Thread(new UiThreadRunner()).start();
           // new Handler().post(new UiThreadRunner() );
            isThreadRunning = true;
        }
    }

    private void loadAudio() {
        final String [] STAR= {"*"};
        String selectionMusic = MediaStore.Audio.Media.IS_MUSIC + "!= ? AND ";
        String selectionMp3 = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";
        String ext = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
        String[] selExtARGS = new String[]{" 0",ext};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor= getContentResolver().query(uri, null, selectionMusic + selectionMp3 , selExtARGS, null);
        if (cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            audioList = new ArrayList<>();
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
                grantUriPermission(getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // Save to audioList
                audioList.add(new Audio(contentUri.toString(), title, album, artist, name, duration));
            }
            Log.v(TAG, "loadAudio audioList = " + audioList);
        }
        cursor.close();
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeAudio(audioList);
        Log.v(TAG, "cursor.close");
    }
    public class UiThreadRunner implements Runnable {
        int currentPosition;
        boolean isPassedTimeVisible = true;
        @Override
        public void run() {
            // Code here will run in UI thread
            Log.v("imri", "imri UiThreadRunner run");
            isThreadRunning = true;
            Log.v("imri", "imri run");
            while (true) {
                if (serviceBound) {
                    while (player.isPlaying()) {
                        try {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    currentPosition = player.getCurrentPosition();
                                    Audio temp = currentPlaying;
                                    currentPlaying = audioList.get(player.getAudioIndex());
                                    Log.v(TAG,"currentPlaying = " + currentPlaying.getDuration());
                                    if (temp != currentPlaying) {
                                        duration = Integer.valueOf(currentPlaying.getDuration());
                                        seekBar.setMax(duration);
                                        String time = getTime(duration);
                                        totalTime.setText(time);
                                        songName.setText(currentPlaying.getTitle());
                                        artistAlbumName.setText(currentPlaying.getArtist() + " - " + currentPlaying.getAlbum());
                                        artistAlbumName.setSelected(true);
                                    }
                                    seekBar.setProgress(currentPosition);
                                    if (player.isPaused()) {
                                        if (isPassedTimeVisible) {
                                            seekBarHint.setVisibility(View.INVISIBLE);
                                            isPassedTimeVisible = false;
                                        } else {
                                            seekBarHint.setVisibility(View.VISIBLE);
                                            isPassedTimeVisible = true;
                                        }
                                    }

                                }
                            });

                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Log.v(TAG," currentPlaying InterruptedException e = " + currentPlaying.getDuration());
                            e.printStackTrace();
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.v(TAG," currentPlaying Exception e = " + currentPlaying.getDuration());
                            break;
                        }
                    }
                }
            }
        }
    }
    // Updating console UI
    public void run() {
        isThreadRunning = true;
        Log.v("imri", "imri run");
        while (true) {
            if (serviceBound) {
                int currentPosition;
                boolean isPassedTimeVisible = true;
                while (player.isPlaying()) {
                    try {
                        currentPosition = player.getCurrentPosition();
                        currentPlaying = audioList.get(player.getAudioIndex());
                        Log.v(TAG,"currentPlaying = " + currentPlaying.getDuration());
                        duration = Integer.valueOf(currentPlaying.getDuration());
                        seekBar.setMax(duration);
                        String time = getTime(duration);
                        totalTime.setText(time);
                        songName.setText(currentPlaying.getTitle());
                        artistAlbumName.setText(currentPlaying.getArtist() + " - " + currentPlaying.getAlbum());
                        artistAlbumName.setSelected(true);
                        seekBar.setProgress(currentPosition);
                        if (player.isPaused()) {
                            if (isPassedTimeVisible) {
                                seekBarHint.setVisibility(View.INVISIBLE);
                                isPassedTimeVisible = false;
                            } else {
                                seekBarHint.setVisibility(View.VISIBLE);
                                isPassedTimeVisible = true;
                            }
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.v(TAG," currentPlaying InterruptedException e = " + currentPlaying.getDuration());
                        e.printStackTrace();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.v(TAG," currentPlaying Exception e = " + currentPlaying.getDuration());
                        break;
                    }
                }
            }
        }
    }
    String getTime(int timeInMilSec) {
        long second = (timeInMilSec / 1000) % 60;
        long minute = (timeInMilSec / (1000 * 60)) % 60;
        long hour = (timeInMilSec / (1000 * 60 * 60)) % 24;
        String time;
        if (hour > 0) {
            time = String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            time = String.format("%02d:%02d",minute, second);
        }
        return time;
    }
    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.all_songs_menu);
        popup.show();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        int x = R.id.shuffle;

        if (itemId == R.id.shuffle) {
            if (item.isChecked()) {
                MediaPlayerService.shuffleOff();
                item.setChecked(false);
            } else {
                MediaPlayerService.shuffleOn();
                item.setChecked(true);
            }
        }
        return false;
    }
}
