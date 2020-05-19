package com.music.session.model;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.music.session.view.MainActivity;
import com.music.session.R;

import org.w3c.dom.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MediaPlayerService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private MediaBrowserCompat mediaBrowserCompat;
    private static String TAG = "MediaPlayerService";
    private MediaButtonReceiver mediaButtonReceiver;
    private static final int PLAYBACK_STATUS_PLAYING = 263;
    private static final int PLAYBACK_STATUS_PAUSED = 264;
    private static boolean isShuffle = false;
    private static boolean isSongs = true;
    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    private AudioManager audioManager;

    private MediaPlayer mediaPlayer;
    //path to the audio file
    private Uri mediaFile;
    //Used to pause/resume MediaPlayer
    private int resumePosition;
    private MediaController mController;
    private LinkedList<Integer> playedSongs = new LinkedList<>();
    Integer currSongInList = -1;
    private boolean isPrevious = false;
    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    //List of available Audio files
    private ArrayList<SongIndexing> audioList;
    private int audioIndex = 0;
    private SongIndexing activeAudio; //an object of the currently playing audio
    public static final String ACTION_PLAY = "com.music.session.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.music.session.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.music.session.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.music.session.ACTION_NEXT";
    public static final String ACTION_STOP = "com.music.session.ACTION_STOP";

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        AudioAttributes aa = new AudioAttributes.Builder()
                .setFlags(AudioAttributes.USAGE_MEDIA | AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mediaPlayer.setAudioAttributes(aa);
        // Open a specific media item using ParcelFileDescriptor.
        ContentResolver resolver = getApplicationContext()
                .getContentResolver();

        if (activeAudio != null) {
            try {
                if (!isPrevious) {
                    ++currSongInList;
                    if (currSongInList + 1 > playedSongs.size()) {
                        playedSongs.add(activeAudio.mIndex);
                    }
                } else { //isPrevious - not changing playedSongs
                    isPrevious = false;
                }
                Log.i(TAG, "imri initMediaPlayer: currSongInList" + currSongInList + " playedSongs" + playedSongs);
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(activeAudio.getUri()));
            } catch (IOException e) {
                e.printStackTrace();
                stopSelf();
            }
            mediaPlayer.prepareAsync();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    public static void shuffleOn() {
        isShuffle = true;
    }

    public static void shuffleOff() {
        isShuffle = false;
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    boolean isPaused = false;

    public boolean isPaused() {
        return isPaused;
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
            isPaused = true;
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
            isPaused = false;
        }
    }

    public void seekTo(int progress) {

        mediaPlayer.seekTo(progress);
        resumePosition = progress;
    }

    public int getAudioIndex() {
        return audioIndex;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        if (allowBrowsing(clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
        }
    }
    boolean allowBrowsing(@NonNull String clientPackageName, int clientUid)  {
        return true;
    }
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
//  Browsing not allowed
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }

        // Assume for example that the music catalog is already loaded/cached.

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID.equals(parentId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        skipToNext();
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }


    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        buildNotification(PLAYBACK_STATUS_PLAYING);
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public boolean requestFocus() {
        if (!handleAudioFocus || currentFocus == AudioManager.AUDIOFOCUS_GAIN) {
            return true;
        }
        if (audioManager == null) {
            return false;
        }

        int status;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder afBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);
            AudioAttributes.Builder aaBuilder = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
            lastFocusRequest = afBuilder.setAudioAttributes(aaBuilder.build()).build();
            status = audioManager.requestAudioFocus(lastFocusRequest);
        } else {
            status = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status) {
            currentFocus = AudioManager.AUDIOFOCUS_GAIN;
            return true;
        }
        startRequested = true;
        return false;
    }

    public boolean abandonFocus() {
        if (!handleAudioFocus) {
            return true;
        }
        if (audioManager == null) {
            return false;
        }

        startRequested = false;
        int status = audioManager.abandonAudioFocus(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (lastFocusRequest != null) {
                status = audioManager.abandonAudioFocusRequest(lastFocusRequest);
                if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status) {
                    // reset lastFocusRequest on success, there is no reason to try again
                    lastFocusRequest = null;
                }
            } else {
                // no focus was requested, return success
                status = audioManager.AUDIOFOCUS_REQUEST_GRANTED;
            }
        } else {
            status = audioManager.abandonAudioFocus(this);
        }
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status;
    }

    protected AudioFocusRequest lastFocusRequest;
    protected boolean startRequested = false;
    protected boolean pausedForLoss = false;
    protected int currentFocus = 0;
    protected boolean handleAudioFocus = true;


    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
        register_handleActions();
        audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        Log.i(TAG, "onCreate: audioManager" + audioManager);
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
        IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        mediaFilter.setPriority(30000);

    }
    public static void setListType(boolean isSongs) {
        MediaPlayerService.isSongs = isSongs;
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //Load data from SharedPreferences
            Log.i(TAG, "imri onStartCommand: intent" + intent);
            //MediaButtonReceiver.handleIntent(mediaSession, intent);
            StorageUtil storage = StorageUtil.getInstance();
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != SongsList.INVALID_INDEX && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (!requestFocus()) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            if (isPlaying()) {
                buildNotification(PLAYBACK_STATUS_PLAYING);
            }
        }


        // TODO IMRI ADD ^^
        handleIncomingActions(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
        unregisterReceiver(handleActions);

        //clear cached playlist
        if (StorageUtil.getInstance() != null)
             StorageUtil.getInstance().clearCachedAudioPlaylist() ;
    }

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: imri becomingNoisyReceiver");
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PLAYBACK_STATUS_PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            audioIndex = StorageUtil.getInstance().loadAudioIndex();
            if (audioIndex != SongsList.INVALID_INDEX && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
        }
    };

    private void register_handleActions() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_STOP);
        registerReceiver(handleActions, filter);
    }

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.BROADCAST_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }
    private PlaybackStateCompat.Builder stateBuilder;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        mediaSession.setMediaButtonReceiver(null);
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //Set mediaSession's MetaData
        updateMetaData();
        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_STOP |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        Log.v(TAG, "imri mediaSession.setCallback");
        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
/*
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                return super.onMediaButtonEvent(mediaButtonEvent);
            }
*/
            @Override
            public void onPlay() {
                super.onPlay();
                Log.v(TAG, "imri onPlay");
                resumeMedia();
                buildNotification(PLAYBACK_STATUS_PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                Log.v(TAG, "imri onPause");
                buildNotification(PLAYBACK_STATUS_PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.v(TAG, "imri onSkipToNext");
                skipToNext();
                updateMetaData();
                buildNotification(PLAYBACK_STATUS_PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.v(TAG, "imri onSkipToPrevious");
                skipToPrevious();
                updateMetaData();
                buildNotification(PLAYBACK_STATUS_PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                Log.v(TAG, "imri onStop");
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                Log.v(TAG, "imri onSeekTo");
                super.onSeekTo(position);
            }
        });
        setSessionToken(mediaSession.getSessionToken());
    }

    private void updateMetaData() {
        //Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
        //        R.drawable.image); //replace with medias albumArt
        // Update the current metadata
        if (audioList != null) {
            Bitmap albumArt = audioList.get(audioIndex).getClipArt();
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                    .build());
        }
    }

    SongIndexing getActiveAudio() {
        return activeAudio;
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    private void skipToNext() {
        Log.v(TAG, "imri skipToNext: current audioIndex = " + audioIndex +
                " audioList.size() = " + audioList.size() + " isShuffle = " + isShuffle +
                " isSongs =" + isSongs + " currSongInList =" + currSongInList + " playedSongs " + playedSongs);
        if ((currSongInList + 1) < playedSongs.size()) {
            audioIndex = playedSongs.get(currSongInList + 1);
        } else if (isShuffle) {
            Random rand = new Random();
            audioIndex = rand.nextInt((audioList.size() - 1) + 1);
        } else {
            int nextIndex;
            if (isSongs) {
                int titleIndex = SongsList.getTitleIndexFromReal(audioIndex);
                nextIndex = SongsList.getRealIndexFromTitle(++titleIndex);
            } else {
                int artistIndex = SongsList.getArtistIndexFromReal(audioIndex);
                nextIndex = SongsList.getRealIndexFromArtist(++artistIndex);
            }
            if (nextIndex >= audioList.size()) {
                nextIndex = 0;
            }
            audioIndex = nextIndex;
        }
        //if last in playlist
        activeAudio = audioList.get(audioIndex);

        //Update stored index
        StorageUtil.getInstance().storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();

    }

    private void skipToPrevious() {
        int prevToPlay = -1;
        Log.v(TAG, "imri skipToPrevious() audioIndex" + audioIndex);
        if (playedSongs.size() > 0 && currSongInList > 0) {
            prevToPlay = playedSongs.get(--currSongInList);
            audioIndex = audioList.get(prevToPlay).mIndex;
            isPrevious = true;
            Log.v(TAG, "imri skipToPrevious() prevToPlay" + prevToPlay + " activeAudio = " + activeAudio.getRealIndex());
        } else { // reached last song
            if (audioIndex == 0) {
                //if first in playlist
                //set index to the last of audioList
                audioIndex = audioList.size() - 1;
            } else {
                //get previous in playlist
                --audioIndex;
            }
        }
        activeAudio = audioList.get(audioIndex);
        //Update stored index
        StorageUtil.getInstance().storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Log.i(TAG, "playbackAction: imri actionNumber" + actionNumber);
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private BroadcastReceiver handleActions = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "buildNotification: imri onReceivehandleIncomingActions");
            handleIncomingActions(intent);
        }
    };

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;
        Log.i(TAG, "handleIncomingActions: imri playbackAction" + playbackAction);
        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }
    public class MediaButtonIntentReceiver extends BroadcastReceiver {

        public MediaButtonIntentReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, " imri onReceive");
            String intentAction = intent.getAction();
            if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                return;
            }
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return;
            }
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                Log.v(TAG, " imri receive");
                // do something
            }
            abortBroadcast();
        }
    }
    private final String CHANNEL_ID = "personal_notification";

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "imri Channel";
            String description = "imri Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            channel.setDescription("no sound");
            channel.setSound(null,null);
            channel.enableLights(false);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void buildNotification(int playbackStatus) {
        Log.i(TAG, "buildNotification: imri playbackStatus" + playbackStatus);
        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PLAYBACK_STATUS_PLAYING) {
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PLAYBACK_STATUS_PAUSED) {
            notificationAction = R.drawable.ic_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Bitmap largeIcon = audioList != null && !audioList.isEmpty() ? audioList.get(audioIndex).getClipArt() : null;
                builder.setShowWhen(false)
                // Set the Notification style
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.black))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getTitle())
                .setContentInfo(activeAudio.getAlbum())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
        startForeground(NOTIFICATION_ID, builder.build());
    }
}
