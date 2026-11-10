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

public class ArtpActivity extends AppCompatActivity {
	
	private ArtpBinding binding;
	
	private ArrayList<HashMap<String, Object>> list_music = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = ArtpBinding.inflate(getLayoutInflater());
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
		android.util.TypedValue typedValue = new android.util.TypedValue();
		getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
		int backgroundColor = typedValue.data;
		
		getWindow().setStatusBarColor(backgroundColor);
		getWindow().setNavigationBarColor(backgroundColor);
		
		boolean isDarkTheme = (getResources().getConfiguration().uiMode & 
		android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
		android.content.res.Configuration.UI_MODE_NIGHT_YES;
		
		androidx.core.view.WindowInsetsControllerCompat controller = 
		androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
		
		if (controller != null) {
			controller.setAppearanceLightStatusBars(!isDarkTheme);
			controller.setAppearanceLightNavigationBars(!isDarkTheme);
		}
		
		String artistName = getIntent().getStringExtra("artist_name");
		if (artistName == null || artistName.isEmpty()) {
			artistName = "Unknown Artist";
		}
		
		final android.widget.TextView txtArtistName = findViewById(R.id.txtArtistName);
		if (txtArtistName != null) {
			txtArtistName.setText(artistName);
		}
		
		final java.util.ArrayList<java.util.HashMap<String, Object>> artistSongs = com.lumina.music.MusicCache.getSongsByArtist(this, artistName);
		list_music.clear();
		if (artistSongs != null) {
			list_music.addAll(artistSongs);
		}
		
		final android.widget.TextView txtSongCount = findViewById(R.id.txtSongCount);
		if (txtSongCount != null) {
			txtSongCount.setText(list_music.size() + " Tracks");
		}
		
		long totalDurationMs = 0;
		for (java.util.HashMap<String, Object> song : list_music) {
			if (song.containsKey("duration") && song.get("duration") != null) {
				try {
					totalDurationMs += Long.parseLong(song.get("duration").toString());
				} catch (Exception ignored) {}
			}
		}
		
		long totalMinutes = (totalDurationMs / 1000) / 60;
		long totalSeconds = (totalDurationMs / 1000) % 60;
		final android.widget.TextView txtTotalDuration = findViewById(R.id.txtTotalDuration);
		if (txtTotalDuration != null) {
			txtTotalDuration.setText(String.format("%02d:%02d", totalMinutes, totalSeconds));
		}
		
		final android.widget.FrameLayout btnPlayAll = findViewById(R.id.btnPlayAll);
		if (btnPlayAll != null) {
			btnPlayAll.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (list_music != null && !list_music.isEmpty()) {
						com.lumina.music.MusicService.songList = new java.util.ArrayList<>(list_music);
						Intent serviceIntent = new Intent(v.getContext(), com.lumina.music.MusicService.class);
						serviceIntent.setAction("ACTION_PLAY");
						serviceIntent.putExtra("index", 0);
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
							v.getContext().startForegroundService(serviceIntent);
						} else {
							v.getContext().startService(serviceIntent);
						}
					} else {
						android.widget.Toast.makeText(v.getContext(), "No tracks to play", android.widget.Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
		
		final android.widget.FrameLayout btnShuffle = findViewById(R.id.btnShuffle);
		if (btnShuffle != null) {
			btnShuffle.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (list_music != null && !list_music.isEmpty()) {
						java.util.ArrayList<java.util.HashMap<String, Object>> shuffledList = new java.util.ArrayList<>(list_music);
						java.util.Collections.shuffle(shuffledList);
						com.lumina.music.MusicService.songList = shuffledList;
						Intent serviceIntent = new Intent(v.getContext(), com.lumina.music.MusicService.class);
						serviceIntent.setAction("ACTION_PLAY");
						serviceIntent.putExtra("index", 0);
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
							v.getContext().startForegroundService(serviceIntent);
						} else {
							v.getContext().startService(serviceIntent);
						}
					} else {
						android.widget.Toast.makeText(v.getContext(), "No tracks to shuffle", android.widget.Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
		
		binding.rvArtistTracks.setAdapter(new RvArtistTracksAdapter(list_music));
		binding.rvArtistTracks.setLayoutManager(new LinearLayoutManager(this));
		binding.txtArtistName.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtSongCount.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.txtTotalDuration.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.nestedScrollView.setVerticalFadingEdgeEnabled(true);
		binding.nestedScrollView.setFadingEdgeLength(70);
		
	}
	
	public class RvArtistTracksAdapter extends RecyclerView.Adapter<RvArtistTracksAdapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public RvArtistTracksAdapter(ArrayList<HashMap<String, Object>> _arr) {
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
			
			com.lumina.music.SongViewHolder holder = (com.lumina.music.SongViewHolder) _view.getTag();
			if (holder == null) {
				holder = new com.lumina.music.SongViewHolder(_view);
				_view.setTag(holder);
			}
			
			HashMap<String, Object> map = list_music.get(_position);
			
			holder.title.setText(String.valueOf(map.get("title")));
			holder.art.setText(String.valueOf(map.get("artist")));
			
			long albumId = -1;
			if (map.get("album_id") != null) {
				try {
					albumId = Long.parseLong(String.valueOf(map.get("album_id")));
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
			
			final int pos = _position;
			holder.linearm.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(android.view.View v) {
					android.content.Context context = v.getContext();
					
					if (context != null) {
						com.lumina.music.MusicService.songList = new java.util.ArrayList<>(list_music);
						com.lumina.music.MusicService.currentIndex = pos;
						
						android.content.Intent playIntent = new android.content.Intent(context, com.lumina.music.MusicService.class);
						playIntent.setAction("ACTION_PLAY");
						playIntent.putExtra("index", pos);
						
						if (list_music.get(pos).containsKey("path")) {
							playIntent.putExtra("path", String.valueOf(list_music.get(pos).get("path")));
						}
						
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
							context.startForegroundService(playIntent);
						} else {
							context.startService(playIntent);
						}
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