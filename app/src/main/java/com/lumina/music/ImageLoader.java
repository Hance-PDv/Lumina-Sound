package com.lumina.music;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class ImageLoader {

    private static final Uri ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart");
    private static final String PREF_ARTIST_URLS = "artist_image_urls";

    private static final RequestOptions REQUEST_OPTIONS = new RequestOptions()
            .override(320, 320)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL);

    public static void loadAlbumArt(@Nullable Context context, @Nullable Uri uri, @NonNull ImageView imageView, @DrawableRes int placeholderRes) {
        if (context == null || uri == null) {
            imageView.setImageResource(placeholderRes);
            return;
        }

        Glide.with(imageView)
                .asBitmap()
                .load(uri)
                .apply(REQUEST_OPTIONS)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .into(imageView);
    }

    public static void loadAlbumArt(@Nullable Context context, long albumId, @NonNull ImageView imageView, @DrawableRes int placeholderRes) {
        Uri uri = (albumId > 0) ? ContentUris.withAppendedId(ALBUM_ART_URI, albumId) : null;
        loadAlbumArt(context, uri, imageView, placeholderRes);
    }

    public static void loadOnlineAlbumArt(@Nullable Context context, @Nullable String imageUrl, @NonNull ImageView imageView, @DrawableRes int placeholderRes) {
        if (context == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            imageView.setImageResource(placeholderRes);
            return;
        }

        Glide.with(imageView)
                .asBitmap()
                .load(imageUrl)
                .apply(REQUEST_OPTIONS)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .into(imageView);
    }

    public static void loadArtistImage(@Nullable final Context context, @Nullable final String artistName, @NonNull final ImageView imageView, @DrawableRes final int placeholderRes) {
        if (context == null || artistName == null || artistName.trim().isEmpty() || artistName.equalsIgnoreCase("Unknown Artist")) {
            imageView.setImageResource(placeholderRes);
            return;
        }

        imageView.setTag(artistName);

        String cachedUrl = getSavedArtistUrl(context, artistName);
        if (cachedUrl != null && !cachedUrl.isEmpty()) {
            Glide.with(imageView)
                    .asBitmap()
                    .load(cachedUrl)
                    .apply(REQUEST_OPTIONS)
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .into(imageView);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String imageUrl = fetchDeezerArtistImageUrl(artistName);

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    saveArtistUrl(context, artistName, imageUrl);
                }

                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!artistName.equals(imageView.getTag())) {
                            return;
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(imageView)
                                    .asBitmap()
                                    .load(imageUrl)
                                    .apply(REQUEST_OPTIONS)
                                    .placeholder(placeholderRes)
                                    .error(placeholderRes)
                                    .into(imageView);
                        } else {
                            imageView.setImageResource(placeholderRes);
                        }
                    }
                });
            }
        }).start();
    }

    private static String getSavedArtistUrl(Context context, String artistName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_ARTIST_URLS, Context.MODE_PRIVATE);
        return prefs.getString(artistName, null);
    }

    private static void saveArtistUrl(Context context, String artistName, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_ARTIST_URLS, Context.MODE_PRIVATE);
        prefs.edit().putString(artistName, url).apply();
    }

    private static String fetchDeezerArtistImageUrl(String artistName) {
        try {
            String encodedArtist = URLEncoder.encode(artistName, "UTF-8");
            URL url = new URL("https://api.deezer.com/search/artist?q=" + encodedArtist);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray data = json.optJSONArray("data");
                if (data != null && data.length() > 0) {
                    JSONObject firstArtist = data.getJSONObject(0);
                    if (firstArtist.has("picture_big") && !firstArtist.isNull("picture_big")) {
                        return firstArtist.getString("picture_big");
                    } else if (firstArtist.has("picture_medium") && !firstArtist.isNull("picture_medium")) {
                        return firstArtist.getString("picture_medium");
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static void clear(@NonNull ImageView imageView) {
        Glide.with(imageView).clear(imageView);
    }
}
