package com.lumina.music;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicService extends Service implements AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_START_SLEEP = "com.mflow.music.ACTION_START_SLEEP";
    public static final String ACTION_STOP_SLEEP = "com.mflow.music.ACTION_STOP_SLEEP";
    public static final String ACTION_SLEEP_TICK = "com.mflow.music.ACTION_SLEEP_TICK";
    public static final String ACTION_SLEEP_FINISH = "com.mflow.music.ACTION_SLEEP_FINISH";
    public static final String EXTRA_SLEEP_TIME = "extra_sleep_time";
    public static final String ACTION_SEEK = "com.mflow.music.ACTION_SEEK";
    
    public static final String ACTION_UPDATE_AUDIO_EFFECTS = "com.mflow.music.ACTION_UPDATE_AUDIO_EFFECTS";

    public static final String PREF_NAME = "mflow_prefs";
    public static final String KEY_SLOWED_SONG = "key_slowed_song";

    public static SleepManager sleepManager;

    public static MediaPlayer mediaPlayer;
    public static ArrayList<HashMap<String, Object>> songList = new ArrayList<>();
    public static int currentIndex = -1;
    public static int repeatMode = 0;
    public static long lastSavedPosition = 0;

    private MediaSessionHelper mediaSessionHelper;
    private NotificationHelper notificationHelper;
    private AudioFocusHelper audioFocusHelper;
    private PlaybackStateManager stateManager;
    private StatsManager statsManager;
    private OnlinePlaybackHelper playbackHelper;
    
    private boolean resumeOnFocusGain = false;
    private long playbackStartTime = 0;

    private boolean isFinishSongEnabled = false;
    private boolean isPendingStopForFinishSong = false;

    private static final Uri ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart");
    private final ExecutorService artworkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override public IBinder onBind(Intent intent) { return null; }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                    return capabilities != null && (
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    );
                } else {
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    return activeNetwork != null && activeNetwork.isConnected();
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean isOnlineSong(HashMap<String, Object> song) {
        if (song == null) return false;
        String rawPath = getRawAudioPath(song);
        return rawPath.startsWith("http://") || rawPath.startsWith("https://");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationHelper = new NotificationHelper(this);
        audioFocusHelper = new AudioFocusHelper(this, this);
        stateManager = new PlaybackStateManager(this);
        statsManager = new StatsManager(this);
        playbackHelper = new OnlinePlaybackHelper(this);

        if (songList == null || songList.isEmpty()) {
            songList = MusicCache.getSongs(this);
        }

        sleepManager = new SleepManager(this, new SleepManager.MediaPlayerProvider() {
            @Override
            public boolean isPlaying() {
                return mediaPlayer != null && mediaPlayer.isPlaying();
            }

            @Override
            public long getCurrentPosition() {
                return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
            }

            @Override
            public long getDuration() {
                return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
            }

            @Override
            public void pauseMusic() {
                if (isFinishSongEnabled) {
                    isPendingStopForFinishSong = true;
                } else {
                    pauseMusicDirectly();
                }
            }
        });

        repeatMode = stateManager.getRepeatMode();
        restoreLastPlaybackState();

        mediaSessionHelper = new MediaSessionHelper(this, new MediaSessionHelper.MediaSessionCallback() {
            @Override public void onPlay() { togglePlayPause(); }
            @Override public void onPause() { togglePlayPause(); }
            @Override public void onSkipToNext() { playNext(); }
            @Override public void onSkipToPrevious() { playPrevious(); }
            @Override public void onSeekTo(long pos) { seekTo(pos); }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (songList == null || songList.isEmpty()) {
            songList = MusicCache.getSongs(this);
        }

        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            switch (action) {
                case "ACTION_PLAY":
                    int index = intent.getIntExtra("index", currentIndex);
                    if (index != -1) {
                        currentIndex = index;
                        lastSavedPosition = 0;
                        playSong(currentIndex, true);
                    }
                    break;

                case ACTION_SEEK:
                    long seekPos = intent.getLongExtra("position", -1);
                    if (seekPos != -1) {
                        seekTo(seekPos);
                    }
                    break;

                case ACTION_UPDATE_AUDIO_EFFECTS:
                    applyAudioOptimizations();
                    break;

                case NotificationHelper.ACTION_TOGGLE:
                    togglePlayPause();
                    break;

                case NotificationHelper.ACTION_NEXT:
                    playNext();
                    break;

                case NotificationHelper.ACTION_PREV:
                    playPrevious();
                    break;

                case "ACTION_TOGGLE_REPEAT":
                    toggleRepeatState();
                    break;

                case "ACTION_TOGGLE_FAV":
                    toggleFavoriteState();
                    break;

                case NotificationHelper.ACTION_DISMISS:
                    if (sleepManager != null) sleepManager.stop();
                    savePlaybackState();
                    releaseMediaPlayer();
                    stopSelf();
                    break;

                case ACTION_START_SLEEP:
                    isPendingStopForFinishSong = false;
                    long duration = intent.getLongExtra("duration", 0);
                    isFinishSongEnabled = intent.getBooleanExtra("finish_current_song", false);

                    if (duration > 0 && sleepManager != null) {
                        sleepManager.start(duration);
                    }
                    break;

                case ACTION_STOP_SLEEP:
                    isPendingStopForFinishSong = false;
                    if (sleepManager != null) {
                        sleepManager.stop();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private String getRawAudioPath(HashMap<String, Object> song) {
        if (song == null) return "";
        if (song.containsKey("stream_url") && song.get("stream_url") != null) {
            String url = song.get("stream_url").toString().trim();
            if (!url.isEmpty()) return url;
        }
        if (song.containsKey("path") && song.get("path") != null) {
            String p = song.get("path").toString().trim();
            if (!p.isEmpty()) return p;
        }
        if (song.containsKey("audio") && song.get("audio") != null) {
            String a = song.get("audio").toString().trim();
            if (!a.isEmpty()) return a;
        }
        return "";
    }

    private String getAudioPath(HashMap<String, Object> song) {
        String rawPath = getRawAudioPath(song);
        if (rawPath.isEmpty()) return "";
        if (playbackHelper != null && (rawPath.startsWith("http://") || rawPath.startsWith("https://"))) {
            return playbackHelper.getPlayablePath(rawPath);
        }
        return rawPath;
    }

    private boolean isValidSong(HashMap<String, Object> song) {
        if (song == null) return false;
        String rawPath = getRawAudioPath(song);
        if (rawPath.isEmpty()) return false;
        if (rawPath.startsWith("http://") || rawPath.startsWith("https://")) return true;
        return new File(rawPath).exists();
    }

    private void pauseMusicDirectly() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            togglePlayPause();
        }
    }

    private void stopSongAndResetToStart() {
        flushListenedTime();
        lastSavedPosition = 0;

        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                mediaPlayer.seekTo(0);
            } catch (Exception ignored) {}
        }

        savePlaybackState();
        refreshNotificationAndUI();
        sendUIBroadcast();
    }

    public void seekTo(final long positionMs) {
        lastSavedPosition = positionMs;
        
        if (mediaPlayer != null) {
            mediaPlayer.setOnSeekCompleteListener(mp -> {
                boolean isPlaying = mp.isPlaying();
                long actualPos = mp.getCurrentPosition();
                
                mediaSessionHelper.updatePlaybackState(isPlaying, actualPos);
                savePlaybackState();
                sendUIBroadcast();
            });
            mediaPlayer.seekTo((int) positionMs);
        } else {
            mediaSessionHelper.updatePlaybackState(false, positionMs);
            savePlaybackState();
            sendUIBroadcast();
        }
    }

    private void flushListenedTime() {
        if (playbackStartTime > 0) {
            long duration = System.currentTimeMillis() - playbackStartTime;
            if (duration > 0) {
                statsManager.addListenedTime(duration);
            }
            playbackStartTime = 0;
        }
    }

    private void recordCurrentSongPlay() {
        if (songList != null && currentIndex >= 0 && currentIndex < songList.size()) {
            HashMap<String, Object> song = songList.get(currentIndex);
            String rawPath = getRawAudioPath(song);
            
            if (isValidSong(song)) {
                String title = song.get("title") != null ? song.get("title").toString() : "Unknown Title";
                String artist = song.get("artist") != null ? song.get("artist").toString() : "Unknown Artist";
                
                long albumId = 0L;
                if (song.get("album_id") != null) {
                    try {
                        albumId = Long.parseLong(song.get("album_id").toString());
                    } catch (Exception ignored) {}
                }

                statsManager.trackSongPlay(rawPath, title, artist, albumId);
            }
        }
    }

    private void playSong(int index, final boolean startImmediately) {
        if (songList == null || songList.isEmpty()) {
            songList = MusicCache.getSongs(this);
        }

        if (songList == null || songList.isEmpty()) {
            currentIndex = -1;
            stopSongAndResetToStart();
            return;
        }

        if (index < 0 || index >= songList.size()) return;

        HashMap<String, Object> song = songList.get(index);

        if (!isValidSong(song)) {
            songList.remove(index);
            if (songList.isEmpty()) {
                currentIndex = -1;
                stopSongAndResetToStart();
                return;
            }
            if (index >= songList.size()) {
                index = 0;
            }
            currentIndex = index;
            playSong(currentIndex, startImmediately);
            return;
        }

        if (isOnlineSong(song) && !isNetworkAvailable()) {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.seekTo(0);
                    if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                } catch (Exception ignored) {}
            }
            sendUIBroadcast();
            return;
        }

        if (startImmediately) {
            startForegroundServiceWithNotification();
        }

        String filePath = getAudioPath(song);

        if (startImmediately && !audioFocusHelper.requestAudioFocus()) return;

        try {
            flushListenedTime();

            if (mediaPlayer != null) {
                savePlaybackState();
                try {
                    if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                } catch (Exception ignored) {}
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = new MediaPlayer();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                );
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            mediaPlayer.setDataSource(filePath);

            mediaPlayer.setOnPreparedListener(mp -> {
                applyAudioOptimizations();

                mp.setOnSeekCompleteListener(innerMp -> {
                    boolean isPlaying = innerMp.isPlaying();
                    mediaSessionHelper.updatePlaybackState(isPlaying, innerMp.getCurrentPosition());
                });

                if (lastSavedPosition > 0) {
                    mp.seekTo((int) lastSavedPosition);
                }

                if (startImmediately) {
                    mp.start();
                    playbackStartTime = System.currentTimeMillis();
                    recordCurrentSongPlay();
                    refreshNotificationAndUI();
                } else {
                    refreshNotificationAndUI();
                }
                sendUIBroadcast();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                flushListenedTime();
                lastSavedPosition = 0;

                if (isPendingStopForFinishSong) {
                    isPendingStopForFinishSong = false;
                    stopSongAndResetToStart();
                    return;
                }

                if (repeatMode == 1) {
                    playSong(currentIndex, true);
                } else {
                    int nextIndex = (currentIndex + 1) % songList.size();
                    if (nextIndex >= 0 && nextIndex < songList.size()) {
                        HashMap<String, Object> nextSong = songList.get(nextIndex);
                        if (isOnlineSong(nextSong) && !isNetworkAvailable()) {
                            stopSongAndResetToStart();
                            return;
                        }
                    }
                    playNext();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                flushListenedTime();
                savePlaybackState();
                refreshNotificationAndUI();
                if (!isNetworkAvailable() && currentIndex >= 0 && currentIndex < songList.size() && isOnlineSong(songList.get(currentIndex))) {
                    stopSongAndResetToStart();
                    return true;
                }
                playNext();
                return true;
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
            refreshNotificationAndUI();
            if (!isNetworkAvailable() && isOnlineSong(song)) {
                stopSongAndResetToStart();
            } else {
                playNext();
            }
        }
    }

    public void applyAudioOptimizations() {
        if (mediaPlayer == null) return;

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isSlowedEnabled = prefs.getBoolean(KEY_SLOWED_SONG, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                boolean isCurrentlyPlaying = false;
                try {
                    isCurrentlyPlaying = mediaPlayer.isPlaying();
                } catch (Exception ignored) {}

                PlaybackParams params = mediaPlayer.getPlaybackParams();
                float targetSpeed = isSlowedEnabled ? 0.97f : 1.0f;
                params.setSpeed(targetSpeed);
                params.setPitch(targetSpeed);

                mediaPlayer.setPlaybackParams(params);

                if (!isCurrentlyPlaying) {
                    mediaPlayer.pause();
                }
            } catch (Exception ignored) {}
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) {
            if (currentIndex != -1) playSong(currentIndex, true);
            return;
        }

        if (mediaPlayer.isPlaying()) {
            flushListenedTime();
            lastSavedPosition = mediaPlayer.getCurrentPosition();
            savePlaybackState();
            mediaPlayer.pause();
            refreshNotificationAndUI();
        } else {
            if (currentIndex >= 0 && currentIndex < songList.size()) {
                HashMap<String, Object> song = songList.get(currentIndex);
                if (isOnlineSong(song) && !isNetworkAvailable()) {
                    sendUIBroadcast();
                    return;
                }
            }
            if (audioFocusHelper.requestAudioFocus()) {
                mediaPlayer.start();
                playbackStartTime = System.currentTimeMillis();
                startForegroundServiceWithNotification();
                sendUIBroadcast();
            }
        }
    }

    private void playNext() {
        if (songList == null || songList.isEmpty()) {
            songList = MusicCache.getSongs(this);
        }
        if (songList == null || songList.isEmpty()) return;

        int nextIndex = (currentIndex + 1) % songList.size();
        if (nextIndex >= 0 && nextIndex < songList.size()) {
            HashMap<String, Object> nextSong = songList.get(nextIndex);
            if (isOnlineSong(nextSong) && !isNetworkAvailable()) {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.seekTo(0);
                        if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                    } catch (Exception ignored) {}
                }
                sendUIBroadcast();
                return;
            }
        }

        int attempts = 0;
        int maxAttempts = songList.size();

        do {
            currentIndex = (currentIndex + 1) % songList.size();
            attempts++;

            if (isValidSong(songList.get(currentIndex))) {
                lastSavedPosition = 0;
                playSong(currentIndex, true);
                return;
            }
        } while (attempts < maxAttempts);

        currentIndex = -1;
        stopSongAndResetToStart();
    }

    private void playPrevious() {
        if (songList == null || songList.isEmpty()) {
            songList = MusicCache.getSongs(this);
        }
        if (songList == null || songList.isEmpty()) return;

        int prevIndex = (currentIndex - 1 + songList.size()) % songList.size();
        if (prevIndex >= 0 && prevIndex < songList.size()) {
            HashMap<String, Object> prevSong = songList.get(prevIndex);
            if (isOnlineSong(prevSong) && !isNetworkAvailable()) {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.seekTo(0);
                        if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                    } catch (Exception ignored) {}
                }
                sendUIBroadcast();
                return;
            }
        }

        int attempts = 0;
        int maxAttempts = songList.size();

        do {
            currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
            attempts++;

            if (isValidSong(songList.get(currentIndex))) {
                lastSavedPosition = 0;
                playSong(currentIndex, true);
                return;
            }
        } while (attempts < maxAttempts);

        currentIndex = -1;
        stopSongAndResetToStart();
    }

    private void toggleRepeatState() {
        repeatMode = (repeatMode + 1) % 2;
        stateManager.saveRepeatMode(repeatMode);
        refreshNotificationAndUI();
    }

    private void toggleFavoriteState() {
        if (songList != null && currentIndex >= 0 && currentIndex < songList.size()) {
            HashMap<String, Object> song = songList.get(currentIndex);
            String rawPath = getRawAudioPath(song);
            
            if (isValidSong(song)) {
                ArrayList favorites = PrefsManager.getFavorites(this);
                if (favorites == null) favorites = new ArrayList();

                int favIndex = -1;
                for (int i = 0; i < favorites.size(); i++) {
                    Object obj = favorites.get(i);
                    if (obj instanceof HashMap) {
                        HashMap favMap = (HashMap) obj;
                        String favPath = getRawAudioPath(favMap);
                        if (rawPath.equalsIgnoreCase(favPath)) {
                            favIndex = i;
                            break;
                        }
                    }
                }

                if (favIndex != -1) {
                    favorites.remove(favIndex);
                } else {
                    favorites.add(song);
                }

                PrefsManager.saveFavorites(this, favorites);
                refreshNotificationAndUI();
            }
        }
    }

    private void savePlaybackState() {
        long currentPos = (mediaPlayer != null) ? mediaPlayer.getCurrentPosition() : lastSavedPosition;
        stateManager.savePlaybackState(currentIndex, (int) currentPos);
    }

    private void restoreLastPlaybackState() {
        if (currentIndex == -1) {
            currentIndex = stateManager.getLastIndex();
        }
        lastSavedPosition = stateManager.getLastPosition();

        if (songList != null && !songList.isEmpty()) {
            if (currentIndex < 0 || currentIndex >= songList.size() || !isValidSong(songList.get(currentIndex))) {
                currentIndex = -1;
                for (int i = 0; i < songList.size(); i++) {
                    if (isValidSong(songList.get(i))) {
                        currentIndex = i;
                        break;
                    }
                }
            }
        }
    }

    private void startForegroundServiceWithNotification() {
        if (currentIndex < 0 || songList == null || currentIndex >= songList.size()) return;

        HashMap<String, Object> song = songList.get(currentIndex);
        if (!isValidSong(song)) return;

        String title = song.get("title") != null ? song.get("title").toString() : "Unknown Title";
        String artist = song.get("artist") != null ? song.get("artist").toString() : "Unknown Artist";

        boolean isPlaying = mediaPlayer != null && mediaPlayer.isPlaying();
        long duration = mediaPlayer != null ? mediaPlayer.getDuration() : 0;
        long currentPos = (mediaPlayer != null && isPlaying) ? mediaPlayer.getCurrentPosition() : lastSavedPosition;

        artworkExecutor.execute(() -> {
            Bitmap albumArt = getHighResAlbumArt(MusicService.this, song);
            mainHandler.post(() -> {
                mediaSessionHelper.updateMetadata(title, artist, albumArt, duration);
                mediaSessionHelper.updatePlaybackState(isPlaying, currentPos);

                startForeground(
                    NotificationHelper.NOTIFICATION_ID,
                    notificationHelper.buildNotification(
                        title, artist, albumArt, isPlaying, mediaSessionHelper.getSessionToken()
                    )
                );
            });
        });
    }

    public void refreshNotificationAndUI() {
        long currentPos = (mediaPlayer != null && mediaPlayer.isPlaying()) ? mediaPlayer.getCurrentPosition() : lastSavedPosition;
        refreshNotificationWithPosition(currentPos);
    }

    private void refreshNotificationWithPosition(long currentPos) {
        if (currentIndex >= 0 && songList != null && currentIndex < songList.size()) {
            HashMap<String, Object> song = songList.get(currentIndex);
            if (isValidSong(song)) {
                String title = song.get("title") != null ? song.get("title").toString() : "Unknown Title";
                String artist = song.get("artist") != null ? song.get("artist").toString() : "Unknown Artist";

                boolean isPlaying = mediaPlayer != null && mediaPlayer.isPlaying();
                long duration = mediaPlayer != null ? mediaPlayer.getDuration() : 0;

                artworkExecutor.execute(() -> {
                    Bitmap albumArt = getHighResAlbumArt(MusicService.this, song);
                    mainHandler.post(() -> {
                        mediaSessionHelper.updateMetadata(title, artist, albumArt, duration);
                        mediaSessionHelper.updatePlaybackState(isPlaying, currentPos);

                        notificationHelper.updateNotification(
                            title, artist, albumArt, isPlaying, mediaSessionHelper.getSessionToken()
                        );
                    });
                });
            }
        }
        sendUIBroadcast();
    }

    public static Bitmap getHighResAlbumArt(Context context, HashMap<String, Object> song) {
        if (song == null || context == null) return null;

        String onlineArtUrl = "";
        if (song.containsKey("artwork_url") && song.get("artwork_url") != null) {
            onlineArtUrl = song.get("artwork_url").toString().trim();
        } else if (song.containsKey("artwork") && song.get("artwork") != null) {
            String art = song.get("artwork").toString().trim();
            if (art.startsWith("http://") || art.startsWith("https://")) {
                onlineArtUrl = art;
            }
        }

        if (!onlineArtUrl.isEmpty()) {
            try {
                return Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(onlineArtUrl)
                        .submit(320, 320)
                        .get();
            } catch (Exception ignored) {}
        }

        if (song.containsKey("artwork") && song.get("artwork") != null) {
            String artworkPath = song.get("artwork").toString().trim();
            if (!artworkPath.isEmpty() && new File(artworkPath).exists()) {
                try {
                    return BitmapFactory.decodeFile(artworkPath);
                } catch (Exception ignored) {}
            }
        }

        long albumId = -1;
        if (song.get("album_id") != null) {
            try {
                albumId = Long.parseLong(song.get("album_id").toString());
            } catch (Exception ignored) {}
        }

        if (albumId > 0) {
            try {
                Uri sArtworkUri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId);
                InputStream inputStream = context.getContentResolver().openInputStream(sArtworkUri);
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    return bitmap;
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    flushListenedTime();
                    lastSavedPosition = mediaPlayer.getCurrentPosition();
                    savePlaybackState();
                    mediaPlayer.pause();
                    refreshNotificationAndUI();
                }
                resumeOnFocusGain = false;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    flushListenedTime();
                    lastSavedPosition = mediaPlayer.getCurrentPosition();
                    savePlaybackState();
                    mediaPlayer.pause();
                    refreshNotificationAndUI();
                    resumeOnFocusGain = true;
                }
                break;

            case AudioManager.AUDIOFOCUS_GAIN:
                if (resumeOnFocusGain && mediaPlayer != null) {
                    mediaPlayer.start();
                    playbackStartTime = System.currentTimeMillis();
                    startForegroundServiceWithNotification();
                    sendUIBroadcast();
                    resumeOnFocusGain = false;
                }
                break;
        }
    }

    private void sendUIBroadcast() {
        Intent intent = new Intent("MFLOW_UPDATE_PLAYER_UI");
        intent.setPackage(getPackageName());
        intent.putExtra("is_playing", mediaPlayer != null && mediaPlayer.isPlaying());
        intent.putExtra("repeat_mode", repeatMode);
        intent.putExtra("position", mediaPlayer != null ? mediaPlayer.getCurrentPosition() : lastSavedPosition);
        sendBroadcast(intent);
    }

    private void releaseMediaPlayer() {
        flushListenedTime();
        if (sleepManager != null) {
            sleepManager.stop();
        }
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        audioFocusHelper.abandonAudioFocus();
        mediaSessionHelper.release();

        if (playbackHelper != null) {
            playbackHelper.release();
            playbackHelper = null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }

        if (artworkExecutor != null && !artworkExecutor.isShutdown()) {
            artworkExecutor.shutdown();
        }
    }

    @Override
    public void onDestroy() {
        flushListenedTime();
        savePlaybackState();
        releaseMediaPlayer();
        super.onDestroy();
    }
}
