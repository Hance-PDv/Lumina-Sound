package com.lumina.music;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LyricsFetcher {

    private static final String USER_AGENT = "MFlow v3.7 (https://github.com/Hance-PDv/Music-Flow)";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static volatile String activeRequestTag = "";

    public interface OnLyricsLoadedListener {
        void onLyricsLoaded(File lrcFile, String lrcContent);
    }

    public static void fetchCurrentLyrics(final Context context, final AppleLyricsView lyricsView, final ProgressBar loadingView) {
        if (lyricsView != null) {
            lyricsView.setLyricsFromLrc("");
        }
        fetchCurrentLyrics(context, (lrcFile, lrcContent) -> {
            if (lyricsView != null) {
                lyricsView.setLyricsFromLrc(lrcContent != null ? lrcContent : "");
            }
        }, loadingView);
    }

    public static void fetchCurrentLyrics(final Context context, final OnLyricsLoadedListener listener, final ProgressBar loadingView) {
        showLoadingState(loadingView);

        int index = MusicService.currentIndex;
        ArrayList<HashMap<String, Object>> list = MusicService.songList;

        if (list == null || list.isEmpty()) {
            list = MusicCache.getSongs(context);
        }

        if (list == null || index < 0 || index >= list.size()) {
            showNoLyrics("", listener, loadingView);
            return;
        }

        HashMap<String, Object> song = list.get(index);
        String title = song.get("title") != null ? song.get("title").toString() : "";
        String artist = song.get("artist") != null ? song.get("artist").toString() : "";

        long duration = 0;
        try {
            if (MusicService.mediaPlayer != null) {
                duration = MusicService.mediaPlayer.getDuration();
            } else if (song.get("duration") != null) {
                duration = Long.parseLong(song.get("duration").toString());
            }
        } catch (Exception ignored) {}

        fetchLyrics(context, title, artist, duration, listener, loadingView);
    }

    public static void fetchLyrics(final Context context, String title, String artist, final long durationMs, final AppleLyricsView lyricsView, final ProgressBar loadingView) {
        if (lyricsView != null) {
            lyricsView.setLyricsFromLrc("");
        }
        fetchLyrics(context, title, artist, durationMs, (lrcFile, lrcContent) -> {
            if (lyricsView != null) {
                lyricsView.setLyricsFromLrc(lrcContent != null ? lrcContent : "");
            }
        }, loadingView);
    }

    public static void fetchLyrics(final Context context, String title, String artist, final long durationMs, final OnLyricsLoadedListener listener, final ProgressBar loadingView) {
        showLoadingState(loadingView);

        if (title == null || title.trim().isEmpty()) {
            showNoLyrics("", listener, loadingView);
            return;
        }

        cancelAllPendingCalls();

        final String cleanedTitle = cleanTitle(title);
        boolean isUnknownArtist = (artist == null || artist.trim().isEmpty() || artist.equalsIgnoreCase("<unknown>") || artist.equalsIgnoreCase("unknown"));
        final String cleanedArtist = isUnknownArtist ? "" : cleanArtist(artist);

        final String requestTag = (cleanedTitle + "_" + cleanedArtist + "_" + (durationMs / 1000)).replaceAll("[^a-zA-Z0-9]", "_");
        activeRequestTag = requestTag;

        String cacheKey = requestTag + ".lrc";
        final File cacheFile = new File(context.getCacheDir(), cacheKey);

        if (cacheFile.exists()) {
            final String cachedLrc = readTextFile(cacheFile);
            mainHandler.post(() -> {
                if (!requestTag.equals(activeRequestTag)) return;
                hideLoading(loadingView);
                if (listener != null) {
                    listener.onLyricsLoaded(cacheFile, cachedLrc);
                }
            });
            return;
        }

        fetchLyricsStrictly(cleanedTitle, cleanedArtist, durationMs, false, requestTag, syncedLyrics -> {
            if (!requestTag.equals(activeRequestTag)) return;

            if (syncedLyrics != null && !syncedLyrics.trim().isEmpty()) {
                saveAndReturn(requestTag, cacheFile, syncedLyrics, listener, loadingView);
            } else {
                mainHandler.post(() -> showNoLyrics(requestTag, listener, loadingView));
            }
        });
    }

    private interface OnRawLyricsFetchedListener {
        void onFetched(String syncedLyrics);
    }

    private static void fetchLyricsStrictly(final String title, final String artist, final long durationMs, final boolean isRetry, final String requestTag, final OnRawLyricsFetchedListener rawListener) {
        if (!requestTag.equals(activeRequestTag)) return;

        final int targetSeconds = (int) (durationMs / 1000);

        if (!artist.isEmpty() && targetSeconds > 0) {
            String getUrl = "https://lrclib.net/api/get?track_name=" + Uri.encode(title) 
                    + "&artist_name=" + Uri.encode(artist) 
                    + "&duration=" + targetSeconds;

            Request getRequest = new Request.Builder()
                    .url(getUrl)
                    .tag(requestTag)
                    .header("User-Agent", USER_AGENT)
                    .header("X-User-Agent", USER_AGENT)
                    .build();

            client.newCall(getRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (!requestTag.equals(activeRequestTag)) return;
                    if (!isRetry) {
                        fetchLyricsStrictly(title, artist, durationMs, true, requestTag, rawListener);
                    } else {
                        performSearchFallback(title, artist, targetSeconds, false, requestTag, rawListener);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!requestTag.equals(activeRequestTag)) {
                        response.close();
                        return;
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject item = new JSONObject(responseData);
                            if (item.has("syncedLyrics") && !item.isNull("syncedLyrics")) {
                                String synced = item.getString("syncedLyrics");
                                if (synced != null && !synced.trim().isEmpty()) {
                                    rawListener.onFetched(synced);
                                    return;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                    performSearchFallback(title, artist, targetSeconds, false, requestTag, rawListener);
                }
            });
        } else {
            performSearchFallback(title, artist, targetSeconds, false, requestTag, rawListener);
        }
    }

    private static void performSearchFallback(final String title, final String artist, final int targetSeconds, final boolean isRetry, final String requestTag, final OnRawLyricsFetchedListener rawListener) {
        if (!requestTag.equals(activeRequestTag)) return;

        StringBuilder query = new StringBuilder(title);
        if (!artist.isEmpty()) {
            query.append(" ").append(artist);
        }

        String searchUrl = "https://lrclib.net/api/search?q=" + Uri.encode(query.toString());

        Request request = new Request.Builder()
                .url(searchUrl)
                .tag(requestTag)
                .header("User-Agent", USER_AGENT)
                .header("X-User-Agent", USER_AGENT)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!requestTag.equals(activeRequestTag)) return;
                if (!isRetry) {
                    performSearchFallback(title, artist, targetSeconds, true, requestTag, rawListener);
                } else {
                    rawListener.onFetched(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!requestTag.equals(activeRequestTag)) {
                    response.close();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JSONArray results = new JSONArray(responseData);

                        String bestSyncedLyrics = null;
                        String fallbackLyrics = null;

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject item = results.getJSONObject(i);
                            if (!item.has("syncedLyrics") || item.isNull("syncedLyrics")) continue;

                            String synced = item.getString("syncedLyrics");
                            if (synced == null || synced.trim().isEmpty()) continue;

                            if (fallbackLyrics == null) {
                                fallbackLyrics = synced;
                            }

                            if (targetSeconds > 0 && item.has("duration") && !item.isNull("duration")) {
                                int apiDuration = (int) Math.round(item.getDouble("duration"));
                                if (Math.abs(apiDuration - targetSeconds) <= 4) {
                                    bestSyncedLyrics = synced;
                                    break;
                                }
                            }
                        }

                        rawListener.onFetched(bestSyncedLyrics != null ? bestSyncedLyrics : fallbackLyrics);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rawListener.onFetched(null);
            }
        });
    }

    private static String cleanTitle(String input) {
        if (input == null) return "";
        String cleaned = input.replaceAll("(?i)\\(.*?official.*?\\)", "")
                .replaceAll("(?i)\\[.*?official.*?\\]", "")
                .replaceAll("(?i)\\(.*?lyric.*?\\)", "")
                .replaceAll("(?i)\\[.*?lyric.*?\\]", "")
                .replaceAll("(?i)\\(.*?remastered.*?\\)", "")
                .replaceAll("(?i)ft\\..*", "")
                .replaceAll("(?i)feat\\..*", "")
                .replaceAll("\\.mp3$", "")
                .trim();
        return cleaned.isEmpty() ? input.trim() : cleaned;
    }

    private static String cleanArtist(String input) {
        if (input == null) return "";
        return input.replaceAll("(?i),.*", "").trim();
    }

    private static void cancelAllPendingCalls() {
        try {
            for (Call call : client.dispatcher().queuedCalls()) {
                call.cancel();
            }
            for (Call call : client.dispatcher().runningCalls()) {
                call.cancel();
            }
        } catch (Exception ignored) {}
    }

    private static void saveAndReturn(final String requestTag, final File cacheFile, final String syncedLyrics, final OnLyricsLoadedListener listener, final ProgressBar loadingView) {
        try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
            fos.write(syncedLyrics.getBytes("UTF-8"));
        } catch (Exception ignored) {}

        mainHandler.post(() -> {
            if (!requestTag.equals(activeRequestTag)) return;
            hideLoading(loadingView);
            if (listener != null) {
                listener.onLyricsLoaded(cacheFile, syncedLyrics);
            }
        });
    }

    private static String readTextFile(File file) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static void showLoadingState(final ProgressBar loadingView) {
        mainHandler.post(() -> {
            if (loadingView != null) {
                loadingView.setVisibility(View.VISIBLE);
            }
        });
    }

    private static void hideLoading(final ProgressBar loadingView) {
        mainHandler.post(() -> {
            if (loadingView != null) {
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    private static void showNoLyrics(final String requestTag, final OnLyricsLoadedListener listener, final ProgressBar loadingView) {
        mainHandler.post(() -> {
            if (!requestTag.isEmpty() && !requestTag.equals(activeRequestTag)) return;
            hideLoading(loadingView);
            if (listener != null) {
                listener.onLyricsLoaded(null, null);
            }
        });
    }
}
