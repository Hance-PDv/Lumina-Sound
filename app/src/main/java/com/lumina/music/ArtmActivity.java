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

public class ArtmActivity extends AppCompatActivity {
	
	private ArtmBinding binding;
	
	private ArrayList<HashMap<String, Object>> list_art = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = ArtmBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		
		binding.cardview18.setOnClickListener(new View.OnClickListener() {
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
		
		list_art.clear();
		list_art.addAll(com.lumina.music.MusicCache.getArtists(this));
		
		binding.gridview1.setVerticalFadingEdgeEnabled(true);
		binding.gridview1.setFadingEdgeLength(70);
		
		if (list_art.size() == 0) {
			binding.NoMusicFoundLayout.setVisibility(View.VISIBLE);
			binding.gridview1.setVisibility(View.GONE);
		} else {
			binding.NoMusicFoundLayout.setVisibility(View.GONE);
			binding.gridview1.setVisibility(View.VISIBLE);
		}
		binding.gridview1.setAdapter(new Gridview1Adapter(list_art));
		binding.txtTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
	}
	
	public class Gridview1Adapter extends BaseAdapter {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Gridview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}
		
		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			ArtBinding binding = ArtBinding.inflate(getLayoutInflater());
			View _view = binding.getRoot();
			
			final TextView txtArtistName = _view.findViewById(R.id.txtArtistName);
			final ImageView imageArtist = _view.findViewById(R.id.imageArtist);
			
			if (list_art != null && _position < list_art.size()) {
				final HashMap<String, Object> map = list_art.get(_position);
				
				final String artistName = map.get("artist") != null ? map.get("artist").toString() : "Unknown Artist";
				txtArtistName.setText(artistName);
				
				com.lumina.music.ImageLoader.loadArtistImage(getApplicationContext(), artistName, imageArtist, R.drawable.icon_art);
				
				_view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _v) {
						Intent intent = new Intent(getApplicationContext(), ArtpActivity.class);
						intent.putExtra("artist_name", artistName);
						startActivity(intent);
					}
				});
			}
			
			
			return _view;
		}
	}
}