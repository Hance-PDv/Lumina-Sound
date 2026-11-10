package com.lumina.music;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONObject;

public class StatsManager {

    private static final String PREF_NAME = "mflow_stats_prefs";
    private static final String KEY_TOTAL_LISTENED_MS = "total_listened_ms";
    private static final String KEY_TOTAL_PLAYS = "total_plays";
    private static final String KEY_EXPLORED_SET = "explored_tracks_json";
    private static final String KEY_TRACKS_PLAY_COUNT = "tracks_play_count_json";

    private final Context context;
    private final SharedPreferences prefs;

    public StatsManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public synchronized void addListenedTime(long ms) {
        prefs.edit().putLong(KEY_TOTAL_LISTENED_MS, getTotalListenedMs() + ms).apply();
    }

    public long getTotalListenedMs() {
        return prefs.getLong(KEY_TOTAL_LISTENED_MS, 0L);
    }

    public String getFormattedListenedTime() {
        long totalSeconds = getTotalListenedMs() / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
    }

    public synchronized void trackSongPlay(String path, String title, String artist, long albumId) {
        int totalPlays = getTotalPlays() + 1;
        JSONObject exploredJson = getJsonObject(KEY_EXPLORED_SET);
        try {
            exploredJson.put(path, title);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject playCountJson = getJsonObject(KEY_TRACKS_PLAY_COUNT);
        try {
            JSONObject songObj = playCountJson.optJSONObject(path);
            int currentPlays = 0;
            if (songObj != null) {
                currentPlays = songObj.optInt("plays", 0);
            } else {
                songObj = new JSONObject();
            }

            songObj.put("title", title);
            if (artist == null || artist.trim().isEmpty() || artist.equalsIgnoreCase("<unknown>")) {
                artist = "Unknown Artist";
            }
            songObj.put("artist", artist);
            songObj.put("album_id", albumId);
            songObj.put("plays", currentPlays + 1);
            songObj.put("last_played", System.currentTimeMillis());

            playCountJson.put(path, songObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        prefs.edit()
            .putInt(KEY_TOTAL_PLAYS, totalPlays)
            .putString(KEY_EXPLORED_SET, exploredJson.toString())
            .putString(KEY_TRACKS_PLAY_COUNT, playCountJson.toString())
            .apply();
    }

    public synchronized void trackSongPlay(String path, String title) {
        trackSongPlay(path, title, "Unknown Artist", 0L);
    }

    public int getTotalPlays() {
        return prefs.getInt(KEY_TOTAL_PLAYS, 0);
    }

    public synchronized int getExploredCount() {
        JSONObject exploredJson = getJsonObject(KEY_EXPLORED_SET);
        ArrayList<String> missingPaths = new ArrayList<>();

        Iterator<String> keys = exploredJson.keys();
        while (keys.hasNext()) {
            String path = keys.next();
            if (path == null || path.isEmpty() || !new File(path).exists()) {
                missingPaths.add(path);
            }
        }

        if (!missingPaths.isEmpty()) {
            for (String path : missingPaths) {
                exploredJson.remove(path);
            }
            prefs.edit().putString(KEY_EXPLORED_SET, exploredJson.toString()).apply();
        }

        return exploredJson.length();
    }

    public String getTopTrackName() {
        ArrayList<HashMap<String, Object>> topTracks = getTopTracks(1);
        if (topTracks != null && !topTracks.isEmpty()) {
            Object titleObj = topTracks.get(0).get("title");
            if (titleObj != null) {
                return titleObj.toString();
            }
        }
        return "None";
    }

    public synchronized ArrayList<HashMap<String, Object>> getTopTracks(int limit) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        JSONObject playCountJson = getValidPlayCountJsonAndCleanup();

        Iterator<String> keys = playCountJson.keys();
        while (keys.hasNext()) {
            String path = keys.next();
            JSONObject songObj = playCountJson.optJSONObject(path);
            if (songObj != null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("path", path);
                map.put("title", songObj.optString("title", "Unknown Title"));
                map.put("artist", songObj.optString("artist", "Unknown Artist"));
                map.put("album_id", songObj.optLong("album_id", 0L));
                map.put("plays", songObj.optInt("plays", 0));
                map.put("last_played", songObj.optLong("last_played", 0L));

                if (songObj.optLong("album_id", 0L) == 0L) {
                    MediaDetails details = fetchDetailsFromMediaStore(path);
                    map.put("album_id", details.albumId);
                    if ("Unknown Artist".equals(map.get("artist"))) {
                        map.put("artist", details.artist);
                    }
                }
                list.add(map);
            }
        }

        list.sort((o1, o2) -> Integer.compare((Integer) o2.get("plays"), (Integer) o1.get("plays")));

        if (limit > 0 && list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        }

        return list;
    }

    private synchronized JSONObject getValidPlayCountJsonAndCleanup() {
        JSONObject playCountJson = getJsonObject(KEY_TRACKS_PLAY_COUNT);
        JSONObject exploredJson = getJsonObject(KEY_EXPLORED_SET);
        ArrayList<String> missingPaths = new ArrayList<>();

        Iterator<String> keys = playCountJson.keys();
        while (keys.hasNext()) {
            String path = keys.next();
            if (path == null || path.isEmpty() || !new File(path).exists()) {
                missingPaths.add(path);
            }
        }

        if (!missingPaths.isEmpty()) {
            for (String path : missingPaths) {
                playCountJson.remove(path);
                exploredJson.remove(path);
            }
            prefs.edit()
                .putString(KEY_TRACKS_PLAY_COUNT, playCountJson.toString())
                .putString(KEY_EXPLORED_SET, exploredJson.toString())
                .apply();
        }

        return playCountJson;
    }

    private static class MediaDetails {
        long albumId = 0L;
        String artist = "Unknown Artist";
    }

    private MediaDetails fetchDetailsFromMediaStore(String path) {
        MediaDetails mediaDetails = new MediaDetails();
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ARTIST},
                MediaStore.Audio.Media.DATA + "=?",
                new String[]{path},
                null)) {

            if (cursor != null && cursor.moveToFirst()) {
                mediaDetails.albumId = cursor.getLong(0);
                String artistStr = cursor.getString(1);
                if (artistStr != null && !artistStr.trim().isEmpty() && !artistStr.equalsIgnoreCase("<unknown>")) {
                    mediaDetails.artist = artistStr;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaDetails;
    }

    public synchronized void resetStats() {
        prefs.edit().clear().apply();
    }

    private JSONObject getJsonObject(String key) {
        try {
            return new JSONObject(prefs.getString(key, "{}"));
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
