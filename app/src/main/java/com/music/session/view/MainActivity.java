// Credit: https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/

package com.music.session.view;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.music.session.R;
import com.music.session.model.MediaPlayerService;
import com.music.session.model.SongIndexing;
import com.music.session.model.SongsList;
import com.music.session.model.StorageUtil;
import com.music.session.model.onClipartsReadyListener;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private MediaPlayerService player;
    boolean serviceBound = false;
    private boolean isSongs = true;
    private boolean isThreadRunning = false;
    private Handler mainHandler = new Handler();
    private static StorageUtil storage;
    Button playPauseButton, skipNextButton, restartOrLastButton, menu;
    SongIndexing currentPlaying = null;
    SeekBar seekBar;
    TextView totalTime;
    TextView songName;
    TextView artistAlbumName;
    ImageView currClipArt;
    private int duration;
    AllSongs AllSongsFragment;
    Artists ArtistFragment;
    ArrayList<SongIndexing> audioList;
    String TAG = "MainActivity";
    int seekTo = -1;
    Button allSongsFragmentButton, artistsFragmentButton;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment artistsFragment;
    Fragment allSongsFragment;
    Context mContext;
    TextView seekBarHint;
    Switch shuffleSwitch;
    private final int TIME_TO_GO_LAST_SONG = 3000;
    private boolean isShuffle = false;
    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.music.session.PlayNewAudio";
    // Change to your package name
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate imri");
        StorageUtil.initStorage(getApplicationContext());
        storage = StorageUtil.getInstance();
        mContext = this;
        SongsList.initSongsList(getApplicationContext());
        ArtistFragment = new Artists(this);
        AllSongsFragment = new AllSongs(this);
        storage.registerClipartsReadyListener(new ClipartsReadyListener());
        //audioList = mSongListInstance.getAllSongs(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();

        seekBar = findViewById(R.id.seekbar);
        songName = findViewById(R.id.song_name_music_player);
        artistAlbumName = findViewById(R.id.artist_and_album_music_player);
        totalTime = findViewById(R.id.total_time);
        seekBarHint = findViewById(R.id.curr_time);
        currClipArt = findViewById(R.id.curr_clipart);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.VISIBLE);
                totalTime.setVisibility(View.VISIBLE);
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
                if (player != null){
                    player.seekTo(seekBar.getProgress());
                } else {
                    seekTo = seekBar.getProgress();
                }
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
                } else {
                    playAudio(storage.loadAudioIndex());
                    playPauseButton.setBackgroundResource(R.drawable.ic_pause);
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

        restartOrLastButton = (Button) findViewById(R.id.restart_or_last_song);
        restartOrLastButton.setOnClickListener(new View.OnClickListener() {
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

        allSongsFragmentButton = (Button) findViewById(R.id.all_songs);
        artistsFragmentButton = (Button) findViewById(R.id.all_artists);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        allSongsFragment = AllSongsFragment;
        fragmentTransaction.add(R.id.container, allSongsFragment, "check");
        fragmentTransaction.commit();
        allSongsFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, allSongsFragment)
                        .commit();
                isSongs = true;
                MediaPlayerService.setListType(isSongs);
                allSongsFragmentButton.setBackground(getDrawable(R.drawable.button_menu_pressed));
                artistsFragmentButton.setBackground(getDrawable(R.drawable.button_menu));
            }
        });
        artistsFragment = ArtistFragment;
        artistsFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, artistsFragment)
                        .commit();
                isSongs = false;
                MediaPlayerService.setListType(isSongs);
                artistsFragmentButton.setBackground(getDrawable(R.drawable.button_menu_pressed));
                allSongsFragmentButton.setBackground(getDrawable(R.drawable.button_menu));

            }
        });
        allSongsFragmentButton.performClick();
        setCurrentPlaying();
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            Log.i(TAG, "onServiceConnected: imri" + player);
            serviceBound = true;
           // Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    public void playAudio(int audioIndex, boolean isSongs) {
        MediaPlayerService.setListType(isSongs);
        playAudio(audioIndex);
    }
    public void playAudio(int audioIndex) {
        //Check is service is active
        if (seekTo == 0) {
            seekBar.setProgress(seekTo);
        }
        duration = 0;
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
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
            storage.storeAudioIndex(audioIndex);
            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_AUDIO);

            sendBroadcast(broadcastIntent);
        }

        playPauseButton.setBackgroundResource(R.drawable.ic_pause);

        if (!isThreadRunning) {
            new Thread(new UiThreadRunner()).start();
           // new Handler().post(new UiThreadRunner() );
            isThreadRunning = true;
        }
    }

    private void loadAudio() {
        if (isSongs) {
            storage.storeAudio(((AllSongs) allSongsFragment).getAllSongs());
        } else {
            storage.storeAudio(((Artists) artistsFragment).getAllSongs());
        }
        Log.v(TAG, "cursor.close");
    }



    public class UiThreadRunner implements Runnable {
        int currentPosition;
        boolean isPassedTimeVisible = true;
        SongIndexing temp = null;
        @Override
        public void run() {
            // Code here will run in UI thread
            Log.v("imri", "imri UiThreadRunner run");
            isThreadRunning = true;
            while (true) {
                if (serviceBound) {
                    int i = 0;
                    while (player.isPlaying()) {
                        playPauseButton.setBackgroundResource(R.drawable.ic_pause);
                        try {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (seekTo != 0) {
                                        player.seekTo(seekTo);
                                        seekTo = 0;
                                    }
                                    currentPosition = player.getCurrentPosition();
                                    temp = currentPlaying;
                                    currentPlaying = audioList.get(player.getAudioIndex());
                                    if (temp != currentPlaying) {
                                        Log.v(TAG,"imri currentPlaying = " + currentPlaying.getRealIndex());
                                        Bitmap currBitmap = currentPlaying.getClipArt();
                                        if (currBitmap != null) {
                                            currClipArt.setImageBitmap(currBitmap);
                                        } else {
                                            currClipArt.setImageDrawable(getDrawable(R.mipmap.ic_simplay_op));
                                        }
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

                            Thread.sleep(400);
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
                    playPauseButton.setBackgroundResource(R.drawable.ic_play);
                }
            }
        }
    }

    private void setCurrentPlaying() {
        audioList = storage.loadAudio();
        int audioIndex = storage.loadAudioIndex();
        Log.v(TAG, "imri audioIndex = " + audioIndex + "audioList" + audioList);
        if (audioList != null && !audioList.isEmpty() && audioIndex != SongsList.INVALID_INDEX) {
            currentPlaying = audioList.get(audioIndex);
            Log.v(TAG, "currentPlaying = " + currentPlaying.getDuration());
            duration = Integer.valueOf(currentPlaying.getDuration());
            seekBar.setMax(duration);
            String time = getTime(duration);
            totalTime.setText(time);
            songName.setText(currentPlaying.getTitle());
            if (currentPlaying.getClipArt() != null) {
                currClipArt.setImageBitmap(currentPlaying.getClipArt());
            } else {
                currClipArt.setImageDrawable(getDrawable(R.mipmap.ic_simplay_op));
            }
            artistAlbumName.setText((currentPlaying.getArtist() + " - " + currentPlaying.getAlbum()));
        } else if (audioList != null && audioList.isEmpty()){
            Toast.makeText(MainActivity.this, "You have no audio files in your phone :(", Toast.LENGTH_LONG).show();
            songName.setText("");
            seekBarHint.setText("");
            totalTime.setText("");
            artistAlbumName.setText("");
        } else { //first time opening app
            loadAudio();
            songName.setText("");
            seekBarHint.setText("");
            totalTime.setText("");
            artistAlbumName.setText("");
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

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(MainActivity.this, v);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.main_menu);
        popup.show();
        MenuItem item = popup.getMenu().findItem(R.id.shuffle);
        if (item != null) {
            item.setChecked(isShuffle);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.shuffle) {
            if (isShuffle) {
                MediaPlayerService.shuffleOff();
                isShuffle = false;
            } else {
                MediaPlayerService.shuffleOn();
                isShuffle = true;
            }
            item.setChecked(isShuffle);
        }
        return true;
    }
     class ClipartsReadyListener implements onClipartsReadyListener {
        @Override
        public void onClipartsReadyEvent() {
            audioList = storage.loadAudio();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                        Intent intent = new Intent(MainActivity.this, AskPermission.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("EXIT", true);
                        startActivity(intent);
                    }
                }).create().show();
    }
}
