// Credit: https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/

package com.music.session.view;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.AudioManager;
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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.music.session.R;
import com.music.session.model.Audio;
import com.music.session.model.MediaPlayerService;
import com.music.session.model.SongsListManager;
import com.music.session.model.StorageUtil;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    public static final String NO_AUDIO = "com.music.session.view.NO_AUDIO";
    public static final String SHOW_MAIN = "com.music.session.view.SHOW_MAIN";
    Context mContext;

    private MediaPlayerService player;
    boolean serviceBound = false;
    Audio currentPlaying = null;

    private static SongsListManager songsManager;

    private boolean isThreadRunning = false;
    private Handler mainHandler = new Handler();

    Button playPauseButton, skipNextButton, restartOrLastButton, menu;
    SeekBar seekBar;
    TextView totalTime;
    TextView songName;
    TextView artistAlbumName;
    ImageView currClipArt;
    AllSongs AllSongsFragment;
    Artists ArtistFragment;
    String TAG = "MainActivity";
    private int duration;
    private int seekTo = -1;

    Button allSongsFragmentButton, artistsFragmentButton;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment artistsFragment;
    Fragment allSongsFragment;

    TextView seekBarHint;
    private final int TIME_TO_GO_LAST_SONG = 3000;
    private boolean isShuffle = false;
    private ProgressDialog dialog;
    private static boolean isFirstTime = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter(MainActivity.SHOW_MAIN);
        intentFilter.addAction(NO_AUDIO);
        registerReceiver(broadcastReceiver, intentFilter);
        StorageUtil.initStorage(getApplicationContext());
        songsManager = SongsListManager.getManager(this);
        Log.v(TAG, "onCreate imri firstTime" + isFirstTime);
        mContext = this;
        intentFilter.addAction(MediaPlayerService.START_PLAYING);
        intentFilter.addAction(MediaPlayerService.PAUSE_PLAYING);
        AllSongsFragment = new AllSongs(this);
        ArtistFragment = new Artists(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        allSongsFragment = AllSongsFragment;

        setContentView(R.layout.activity_main);
        seekBar = findViewById(R.id.seekbar);
        songName = findViewById(R.id.song_name_music_player);
        songName.setText(R.string.welcome);
        artistAlbumName = findViewById(R.id.artist_and_album_music_player);
        artistAlbumName.setText(R.string.select_song);
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
                int newProgress = seekBar.getProgress();
                if (player != null ) {
                    player.seekTo(newProgress);
                } else {
                    seekTo = newProgress;
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
        playPauseButton = findViewById(R.id.play_pause);
        playPauseButton.setBackgroundResource(R.drawable.ic_play);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBound) {
                    Intent broadcastIntent;
                    if (player.isPlaying()) {
                        broadcastIntent = new Intent(MediaPlayerService.ACTION_PAUSE);
                        playPauseButton.setBackgroundResource(R.drawable.ic_play);
                    } else {
                        broadcastIntent = new Intent(MediaPlayerService.ACTION_PLAY);
                        playPauseButton.setBackgroundResource(R.drawable.ic_pause);
                    }
                    sendBroadcast(broadcastIntent);
                } else {
                    playPauseButton.setBackgroundResource(R.drawable.ic_pause);
                    bindService();
                }
            }
        });
        skipNextButton = findViewById(R.id.skip_next_song);
        skipNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent broadcastIntent = new Intent(MediaPlayerService.ACTION_NEXT);
                sendBroadcast(broadcastIntent);
            }
        });

        restartOrLastButton = findViewById(R.id.restart_or_last_song);
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
        if (isFirstTime) {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Loading your media");
            dialog.show();
            mainHandler.postDelayed(timer, 5000);
        } else {
            mainHandler.post(loadFragmentAgain);
        }
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: intent.getAction()" + intent.getAction());
            String action = intent.getAction();
            if (action != null) {
                if (isFirstTime) {
                    if (action.equals(SHOW_MAIN)) {
                        dialog.dismiss();
                        mainHandler.post(loadFragmentAgain);
                        isFirstTime = false;
                        mainHandler.removeCallbacks(timer);
                    } else if (action.equals(NO_AUDIO)) {
                        mainHandler.post(timer);
                    }
                } else if (action.equals(MediaPlayerService.START_PLAYING)) {
                    playPauseButton.setBackgroundResource(R.drawable.ic_pause);
                } else if (action.equals(MediaPlayerService.PAUSE_PLAYING)) {
                    playPauseButton.setBackgroundResource(R.drawable.ic_play);
                }
            }
        }
    };


    public void playAudioPressed() {
        playPauseButton.setBackgroundResource(R.drawable.ic_pause);
    }
    public void playAudio(int audioIndex, boolean isSongs) {
        songsManager.setListType(isSongs ? SongsListManager.SONGS : SongsListManager.ARTISTS);
        playAudio(audioIndex);
    }

    void bindService() {
        Intent playerIntent = new Intent(this, MediaPlayerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(playerIntent);
        } else {
            startService(playerIntent);
        }
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        if (!isThreadRunning) {
            isThreadRunning = true;
            mainHandler.post(r);
        }
    }
    public void playAudio(int audioIndex) {
        playAudioPressed();
        if (seekTo == 0) {
            seekBar.setProgress(seekTo);
        }
        duration = 0;
        songsManager.storeIndex(audioIndex);
        if (!serviceBound) {
            bindService();
        } else {
            Intent broadcastIntent = new Intent(MediaPlayerService.BROADCAST_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected: imri" + player);
            isThreadRunning = false;
            serviceBound = false;
        }
    };

    Runnable r = new Runnable() {
        int currentPosition;
        boolean isPassedTimeVisible = true;
        Audio temp = null;
        @Override
        public void run() {
            Log.i(TAG, "run: imri");
            if (serviceBound) {
                /*try {*/
                if (player != null && player.isPlaying()) {
                    if (seekTo != 0) {
                        player.seekTo(seekTo);
                        seekTo = 0;
                    }
                    currentPosition = player.getCurrentPosition();
                    temp = currentPlaying;
                    currentPlaying = player.getActiveAudio();
                    if (temp != currentPlaying) {
                        Log.v(TAG, "imri currentPlaying = " + currentPlaying.index);
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
                        artistAlbumName.setText((currentPlaying.getArtist() + " - " + currentPlaying.getAlbum()));
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
            }
            mainHandler.postDelayed(this, 300);
        }
    };
    Runnable timer = new Runnable() {
        @Override
        public void run() {
            artistAlbumName.setText(getString(R.string.no_audio));
            playPauseButton.setOnClickListener(null);
            artistsFragmentButton.setOnClickListener(null);
            allSongsFragmentButton.setOnClickListener(null);
            skipNextButton.setOnClickListener(null);
            restartOrLastButton.setOnClickListener(null);
            menu.setOnClickListener(null);
            seekBar.setOnSeekBarChangeListener(null);
            TextView tv = findViewById(R.id.no_songs_text);
            tv.setVisibility(View.VISIBLE);
            dialog.dismiss();
        }
    };
    Runnable loadFragmentAgain = new Runnable() {
        @Override
        public void run() {
            try {
                allSongsFragmentButton = findViewById(R.id.all_songs);
                artistsFragmentButton = findViewById(R.id.all_artists);
                allSongsFragmentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, allSongsFragment)
                                .commit();
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
                        artistsFragmentButton.setBackground(getDrawable(R.drawable.button_menu_pressed));
                        allSongsFragmentButton.setBackground(getDrawable(R.drawable.button_menu));

                    }
                });
                fragmentTransaction.add(R.id.container, allSongsFragment, "check");
                fragmentTransaction.commit();
                allSongsFragmentButton.performClick();
            } catch (Exception e) {
                mainHandler.postDelayed(this, 2000);
            }
        }
    };

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
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: imri");
        mainHandler.removeCallbacks(r);
        unregisterReceiver(broadcastReceiver);
        if (serviceBound) {
            mContext.unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
