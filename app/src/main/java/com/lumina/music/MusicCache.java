package com.lumina.music;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MusicCache {

    private static final String FILE_NAME = "cached_songs.json";

    public static ArrayList<HashMap<String, Object>> getSongs(Context context) {
        ArrayList<HashMap<String, Object>> cachedList = getSavedSongs(context);
        ArrayList<HashMap<String, Object>> freshList = MusicFetcher.getAudioFiles(context);

        if (cachedList.isEmpty() || freshList.size() != cachedList.size()) {
            saveSongs(context, freshList);
            return freshList;
        }

        for (HashMap<String, Object> song : cachedList) {
            String path = song.get("path") != null ? song.get("path").toString() : "";
            if (!path.isEmpty() && !new File(path).exists()) {
                saveSongs(context, freshList);
                return freshList;
            }
        }

        return cachedList;
    }

    /**
     * فحص ما إذا كان الحقل يحتوي على أكثر من اسم فنان (مثل: ft, feat, &, الفواصل)
     */
    private static boolean isMultiArtist(String artist) {
        if (artist == null || artist.trim().isEmpty()) return false;
        String lower = artist.toLowerCase().trim();
        return lower.contains(",") 
            || lower.contains("&") 
            || lower.contains("/") 
            || lower.contains(";") 
            || lower.contains(" ft.") 
            || lower.contains(" ft ") 
            || lower.contains(" feat.") 
            || lower.contains(" feat ") 
            || lower.contains(" featuring ") 
            || lower.contains(" x ") 
            || lower.contains(" vs. ") 
            || lower.contains(" vs ") 
            || lower.contains(" and ");
    }

    /**
     * دالة تجميع الفنانين واستخراج بياناتهم للـ GridView (نظيفة بدون الفنانين المزدوجين)
     */
    public static ArrayList<HashMap<String, Object>> getArtists(Context context) {
        ArrayList<HashMap<String, Object>> artistList = new ArrayList<>();
        ArrayList<HashMap<String, Object>> allSongs = getSongs(context);

        if (allSongs == null || allSongs.isEmpty()) {
            return artistList;
        }

        // تجميع الأغاني حسب اسم الفنان مع الحفاظ على الترتيب
        LinkedHashMap<String, ArrayList<HashMap<String, Object>>> groupedMap = new LinkedHashMap<>();

        for (HashMap<String, Object> song : allSongs) {
            String artist = song.get("artist") != null ? song.get("artist").toString().trim() : "";
            if (artist.isEmpty() || artist.equalsIgnoreCase("<unknown>")) {
                artist = "Unknown Artist";
            }

            // استبعاد الأسماء المزدوجة والمشرتكة لضمان قائمة فنانين نظيفة
            if (isMultiArtist(artist)) {
                continue;
            }

            if (!groupedMap.containsKey(artist)) {
                groupedMap.put(artist, new ArrayList<>());
            }
            groupedMap.get(artist).add(song);
        }

        // تحويل المجموعات إلى القائمة النهائية للـ Adapter
        for (Map.Entry<String, ArrayList<HashMap<String, Object>>> entry : groupedMap.entrySet()) {
            String artistName = entry.getKey();
            ArrayList<HashMap<String, Object>> songs = entry.getValue();

            long albumId = -1L;
            if (!songs.isEmpty()) {
                HashMap<String, Object> firstSong = songs.get(0);
                if (firstSong.containsKey("album_id") && firstSong.get("album_id") != null) {
                    try {
                        albumId = Long.parseLong(firstSong.get("album_id").toString());
                    } catch (Exception ignored) {}
                } else if (firstSong.containsKey("albumId") && firstSong.get("albumId") != null) {
                    try {
                        albumId = Long.parseLong(firstSong.get("albumId").toString());
                    } catch (Exception ignored) {}
                }
            }

            HashMap<String, Object> artistMap = new HashMap<>();
            artistMap.put("artist", artistName);
            artistMap.put("count", songs.size());
            artistMap.put("album_id", albumId);
            artistMap.put("songs", songs);

            artistList.add(artistMap);
        }

        return artistList;
    }

    /**
     * جلب أغاني فنان محدد (تشمل الأغاني المنفردة والمشتركة له)
     */
    public static ArrayList<HashMap<String, Object>> getSongsByArtist(Context context, String artistName) {
        ArrayList<HashMap<String, Object>> resultList = new ArrayList<>();
        if (artistName == null || artistName.trim().isEmpty()) return resultList;

        String searchArtist = artistName.trim().toLowerCase();
        ArrayList<HashMap<String, Object>> allSongs = getSongs(context);

        for (HashMap<String, Object> song : allSongs) {
            String artist = song.get("artist") != null ? song.get("artist").toString().trim() : "";
            if (artist.isEmpty() || artist.equalsIgnoreCase("<unknown>")) {
                artist = "Unknown Artist";
            }

            String lowerArtist = artist.toLowerCase();

            // إدراج الأغنية إذا كانت تطابق الفنان بشكل كامل أو تحتوي على اسمه كفنان مشارك
            if (lowerArtist.equals(searchArtist) || lowerArtist.contains(searchArtist)) {
                resultList.add(song);
            }
        }
        return resultList;
    }

    public static void refreshCache(final Context context) {
        if (context == null) return;
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<HashMap<String, Object>> freshList = MusicFetcher.getAudioFiles(context);
                saveSongs(context, freshList);
            }
        }).start();
    }

    public static void saveSongs(Context context, ArrayList<HashMap<String, Object>> list) {
        if (context == null) return;
        
        File file = new File(context.getFilesDir(), FILE_NAME);

        if (list == null || list.isEmpty()) {
            if (file.exists()) {
                file.delete();
            }
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray();
            for (HashMap<String, Object> map : list) {
                JSONObject jsonObject = new JSONObject();
                for (String key : map.keySet()) {
                    jsonObject.put(key, map.get(key));
                }
                jsonArray.put(jsonObject);
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<HashMap<String, Object>> getSavedSongs(Context context) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        if (context == null) return list;

        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) return list;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            String jsonString = new String(data, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                HashMap<String, Object> map = new HashMap<>();

                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    map.put(key, jsonObject.get(key));
                }

                String path = map.get("path") != null ? map.get("path").toString() : "";
                if (!path.isEmpty() && new File(path).exists()) {
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
