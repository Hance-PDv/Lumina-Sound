package com.lumina.music;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import androidx.activity.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.*;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.runtime.*;
import androidx.lifecycle.viewmodel.savedstate.*;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.vectordrawable.*;
import androidx.vectordrawable.animated.*;
import com.bumptech.glide.*;
import com.bumptech.glide.gifdecoder.*;
import com.lumina.music.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;
import org.json.*;

public class MostActivity extends AppCompatActivity {
	
	private MostBinding binding;
	private double old_size = 0;
	
	private ArrayList<HashMap<String, Object>> list_music = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = MostBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		
		binding.btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});
	}
	
	private void initializeLogic() {
		// 1. جلب لون الخلفية أو السطح (Surface) مباشرة من ثيم Material 3 الحالي
		android.util.TypedValue typedValue = new android.util.TypedValue();
		getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
		int backgroundColor = typedValue.data;
		
		// 2. تطبيق اللون على شريط الحالة وأزرار التنقل لتتطابق تماماً مع الثيم
		getWindow().setStatusBarColor(backgroundColor);
		getWindow().setNavigationBarColor(backgroundColor);
		
		// 3. ضبط لون الأيقونات (فواتح أو غوامق) تلقائياً حسب الوضع الحالي (داكن أو فاتح)
		boolean isDarkTheme = (getResources().getConfiguration().uiMode & 
		android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
		android.content.res.Configuration.UI_MODE_NIGHT_YES;
		
		androidx.core.view.WindowInsetsControllerCompat controller = 
		androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
		
		if (controller != null) {
			// إذا كان الثيم فاتحاً، تصبح الأيقونات داكنة والعكس صحيح لضمان وضوحها
			controller.setAppearanceLightStatusBars(!isDarkTheme);
			controller.setAppearanceLightNavigationBars(!isDarkTheme);
		}
		
		final androidx.recyclerview.widget.RecyclerView recyclerview1 = findViewById(R.id.recyclerview1);
		final android.widget.EditText etSearchQuery = findViewById(R.id.etSearchQuery);
		final android.widget.TextView tvTracksCount = findViewById(R.id.tvTracksCount);
		
		final android.widget.FrameLayout cardBack = findViewById(R.id.cardBack);
		final android.widget.ImageView btnBack = findViewById(R.id.btnBack);
		final android.view.View cardSearchInput = findViewById(R.id.cardSearchInput);
		
		if (cardSearchInput != null) {
			cardSearchInput.setAlpha(0.96f);
		}
		
		if (cardBack != null) {
			cardBack.setAlpha(0.96f);
			cardBack.setOnClickListener(v -> finish());
		} else if (btnBack != null) {
			btnBack.setOnClickListener(v -> finish());
		}
		
		final java.util.ArrayList<java.util.HashMap<String, Object>> all_songs = com.lumina.music.MusicCache.getSongs(this);
		
		recyclerview1.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
		
		list_music.clear();
		if (all_songs != null) {
			list_music.addAll(all_songs);
		}
		
		if (tvTracksCount != null) {
			tvTracksCount.setText(list_music.size() + " tracks");
		}
		
		if (recyclerview1.getAdapter() != null) {
			recyclerview1.getAdapter().notifyDataSetChanged();
		}
		
		etSearchQuery.addTextChangedListener(new android.text.TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String query = s.toString().trim().toLowerCase();
				list_music.clear();
				
				if (query.isEmpty()) {
					if (all_songs != null) {
						list_music.addAll(all_songs);
					}
				} else if (all_songs != null) {
					for (java.util.HashMap<String, Object> song : all_songs) {
						String title = song.get("title") != null ? String.valueOf(song.get("title")).toLowerCase() : "";
						String artist = song.get("artist") != null ? String.valueOf(song.get("artist")).toLowerCase() : "";
						
						if (title.contains(query) || artist.contains(query)) {
							list_music.add(song);
						}
					}
				}
				
				if (tvTracksCount != null) {
					tvTracksCount.setText(list_music.size() + " tracks");
				}
				
				if (recyclerview1.getAdapter() != null) {
					recyclerview1.getAdapter().notifyDataSetChanged();
				}
			}
			
			@Override
			public void afterTextChanged(android.text.Editable s) {}
		});
		
		if (list_music.size() == 0) {
			binding.NoMusicFoundLayout.setVisibility(View.VISIBLE);
			binding.recyclerview1.setVisibility(View.GONE);
		} else {
			binding.recyclerview1.setVisibility(View.VISIBLE);
			binding.NoMusicFoundLayout.setVisibility(View.GONE);
		}
		binding.recyclerview1.setAdapter(new Recyclerview1Adapter(list_music));
		binding.recyclerview1.setLayoutManager(new LinearLayoutManager(this));
		binding.vscroll2.setVerticalFadingEdgeEnabled(true);
		binding.vscroll2.setFadingEdgeLength(70);
		
		binding.text.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.tvTracksCount.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
	}
	
	public class Recyclerview1Adapter extends RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Recyclerview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getLayoutInflater();
			View _v = _inflater.inflate(R.layout.music, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			MusicBinding binding = MusicBinding.bind(_view);
			
			if (list_music == null || _position < 0 || _position >= list_music.size()) {
				return;
			}
			
			com.lumina.music.SongViewHolder holder = (com.lumina.music.SongViewHolder) _view.getTag();
			if (holder == null) {
				holder = new com.lumina.music.SongViewHolder(_view);
				_view.setTag(holder);
			}
			
			final HashMap<String, Object> map = list_music.get(_position);
			
			holder.title.setText(String.valueOf(map.get("title")));
			holder.art.setText(String.valueOf(map.get("artist")));
			
			long albumId = 0L;
			if (map.containsKey("album_id") && map.get("album_id") != null) {
				try {
					albumId = Long.parseLong(map.get("album_id").toString());
				} catch (Exception ignored) {}
			}
			
			com.lumina.music.ImageLoader.loadAlbumArt(
			_view.getContext(),
			albumId,
			holder.img_music,
			R.drawable.default_music_image
			);
			
			android.util.TypedValue typedValue = new android.util.TypedValue();
			_view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
			holder.linearm.setBackgroundResource(typedValue.resourceId);
			
			holder.linearm.setOnClickListener(v -> {
				android.content.Context context = v.getContext();
				
				if (context != null) {
					java.util.ArrayList<java.util.HashMap<String, Object>> fullList = com.lumina.music.MusicCache.getSongs(context);
					if (fullList == null || fullList.isEmpty()) {
						fullList = new java.util.ArrayList<>(list_music);
					}
					
					int realIndex = -1;
					String targetPath = map.containsKey("path") && map.get("path") != null ? map.get("path").toString() : "";
					
					if (!targetPath.isEmpty()) {
						for (int i = 0; i < fullList.size(); i++) {
							java.util.HashMap<String, Object> song = fullList.get(i);
							if (song.containsKey("path") && targetPath.equals(String.valueOf(song.get("path")))) {
								realIndex = i;
								break;
							}
						}
					}
					
					if (realIndex == -1) {
						String targetTitle = String.valueOf(map.get("title"));
						String targetArtist = String.valueOf(map.get("artist"));
						for (int i = 0; i < fullList.size(); i++) {
							java.util.HashMap<String, Object> song = fullList.get(i);
							if (targetTitle.equals(String.valueOf(song.get("title"))) &&
							targetArtist.equals(String.valueOf(song.get("artist")))) {
								realIndex = i;
								break;
							}
						}
					}
					
					if (realIndex == -1) {
						realIndex = _position;
					}
					
					com.lumina.music.MusicService.songList = new java.util.ArrayList<>(fullList);
					com.lumina.music.MusicService.currentIndex = realIndex;
					com.lumina.music.MusicService.lastSavedPosition = 0;
					
					android.content.Intent playIntent = new android.content.Intent(context, com.lumina.music.MusicService.class);
					playIntent.setAction("ACTION_PLAY");
					playIntent.putExtra("index", realIndex);
					
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
						context.startForegroundService(playIntent);
					} else {
						context.startService(playIntent);
					}
				}
			});
			
			binding.title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
			binding.art.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		}
		
		@Override
		public int getItemCount() {
			return _data.size();
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder {
			public ViewHolder(View v) {
				super(v);
			}
		}
	}
}