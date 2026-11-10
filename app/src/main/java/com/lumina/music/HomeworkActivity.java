package com.lumina.music;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.Intent;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
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
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.runtime.*;
import androidx.lifecycle.viewmodel.savedstate.*;
import androidx.vectordrawable.*;
import androidx.vectordrawable.animated.*;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import com.bumptech.glide.*;
import com.bumptech.glide.gifdecoder.*;
import com.lumina.music.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class HomeworkActivity extends AppCompatActivity {
	
	private HomeworkBinding binding;
	
	private LinkFragmentAdapter link;
	private Intent intent = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = HomeworkBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		link = new LinkFragmentAdapter(getApplicationContext(), getSupportFragmentManager());
		
		binding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int _position, float _positionOffset, int _positionOffsetPixels) {
				
			}
			
			@Override
			public void onPageSelected(int _position) {
				final androidx.viewpager.widget.ViewPager viewpager = findViewById(R.id.viewpager);
				final android.widget.LinearLayout tabHome = findViewById(R.id.tab_home);
				final android.widget.LinearLayout tabFav = findViewById(R.id.tab_fav);
				final android.widget.LinearLayout tabLibrary = findViewById(R.id.tab_library);
				
				final android.widget.ImageView iconHome = findViewById(R.id.icon_home);
				final android.widget.ImageView iconFav = findViewById(R.id.icon_fav);
				final android.widget.ImageView iconLibrary = findViewById(R.id.icon_library);
				
				final Runnable updateTabsSelection = () -> {
					if (viewpager == null) return;
					int position = viewpager.getCurrentItem();
					
					tabHome.setBackground(null);
					tabFav.setBackground(null);
					tabLibrary.setBackground(null);
					
					android.view.View activeTab = null;
					
					if (position == 0) {
						tabHome.setBackgroundResource(R.drawable.bg_active_pill);
						iconHome.setImageResource(R.drawable.icon_home1);
						iconFav.setImageResource(R.drawable.icon_explore1);
						iconLibrary.setImageResource(R.drawable.icon_library);
						activeTab = tabHome;
						
					} else if (position == 1) {
						tabFav.setBackgroundResource(R.drawable.bg_active_pill);
						iconHome.setImageResource(R.drawable.icon_home);
						iconFav.setImageResource(R.drawable.icon_explore);
						iconLibrary.setImageResource(R.drawable.icon_library);
						activeTab = tabFav;
						
					} else if (position == 2) {
						tabLibrary.setBackgroundResource(R.drawable.bg_active_pill);
						iconHome.setImageResource(R.drawable.icon_home);
						iconFav.setImageResource(R.drawable.icon_explore1);
						iconLibrary.setImageResource(R.drawable.icon_library1);
						activeTab = tabLibrary;
					}
					
					if (activeTab != null) {
						activeTab.setAlpha(0.6f);
						activeTab.setScaleX(0.9f);
						activeTab.setScaleY(0.9f);
						activeTab.animate()
						.alpha(1f)
						.scaleX(1f)
						.scaleY(1f)
						.setDuration(220)
						.start();
					}
				};
				
				updateTabsSelection.run();
				
				if (viewpager != null) {
					viewpager.addOnPageChangeListener(new androidx.viewpager.widget.ViewPager.OnPageChangeListener() {
						@Override
						public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
						
						@Override
						public void onPageSelected(int position) {
							updateTabsSelection.run();
						}
						
						@Override
						public void onPageScrollStateChanged(int state) {}
					});
				}
				
				if (tabHome != null && viewpager != null) {
					tabHome.setOnClickListener(v -> {
						viewpager.setCurrentItem(0, true);
						updateTabsSelection.run();
					});
				}
				if (tabFav != null && viewpager != null) {
					tabFav.setOnClickListener(v -> {
						viewpager.setCurrentItem(1, true);
						updateTabsSelection.run();
					});
				}
				if (tabLibrary != null && viewpager != null) {
					tabLibrary.setOnClickListener(v -> {
						viewpager.setCurrentItem(2, true);
						updateTabsSelection.run();
					});
				}
				
			}
			
			@Override
			public void onPageScrollStateChanged(int _scrollState) {
				
			}
		});
		
		binding.miniPlayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				intent.setClass(getApplicationContext(), FullplayerActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		
		binding.btnSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				intent.setClass(getApplicationContext(), SettingActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		
		binding.tabHome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				final androidx.viewpager.widget.ViewPager viewpager = findViewById(R.id.viewpager);
				final android.widget.LinearLayout tabHome = findViewById(R.id.tab_home);
				final android.widget.LinearLayout tabFav = findViewById(R.id.tab_fav);
				final android.widget.LinearLayout tabLibrary = findViewById(R.id.tab_library);
				final android.widget.ImageView iconHome = findViewById(R.id.icon_home);
				final android.widget.ImageView iconFav = findViewById(R.id.icon_fav);
				final android.widget.ImageView iconLibrary = findViewById(R.id.icon_library);
				
				if (viewpager != null) viewpager.setCurrentItem(0, true);
				
				tabHome.setBackgroundResource(R.drawable.bg_active_pill);
				tabFav.setBackground(null);
				tabLibrary.setBackground(null);
				
				iconHome.setImageResource(R.drawable.icon_home1);
				iconFav.setImageResource(R.drawable.icon_explore1);
				iconLibrary.setImageResource(R.drawable.icon_library);
				
				tabHome.setAlpha(0.6f);
				tabHome.setScaleX(0.9f);
				tabHome.setScaleY(0.9f);
				tabHome.animate()
				.alpha(1f)
				.scaleX(1f)
				.scaleY(1f)
				.setDuration(220)
				.start();
				
				android.view.animation.ScaleAnimation scaleAnim = new android.view.animation.ScaleAnimation(
				1f, 0.90f, 1f, 0.90f,
				android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
				android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
				);
				scaleAnim.setDuration(120);
				scaleAnim.setRepeatMode(android.view.animation.Animation.REVERSE);
				scaleAnim.setRepeatCount(1);
				((android.view.View)_view).startAnimation(scaleAnim);
				
			}
		});
		
		binding.tabFav.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				final androidx.viewpager.widget.ViewPager viewpager = findViewById(R.id.viewpager);
				final android.widget.LinearLayout tabHome = findViewById(R.id.tab_home);
				final android.widget.LinearLayout tabFav = findViewById(R.id.tab_fav);
				final android.widget.LinearLayout tabLibrary = findViewById(R.id.tab_library);
				final android.widget.ImageView iconHome = findViewById(R.id.icon_home);
				final android.widget.ImageView iconFav = findViewById(R.id.icon_fav);
				final android.widget.ImageView iconLibrary = findViewById(R.id.icon_library);
				
				if (viewpager != null) viewpager.setCurrentItem(1, true);
				
				tabHome.setBackground(null);
				tabFav.setBackgroundResource(R.drawable.bg_active_pill);
				tabLibrary.setBackground(null);
				
				iconHome.setImageResource(R.drawable.icon_home);
				iconFav.setImageResource(R.drawable.icon_explore);
				iconLibrary.setImageResource(R.drawable.icon_library);
				
				tabFav.setAlpha(0.6f);
				tabFav.setScaleX(0.9f);
				tabFav.setScaleY(0.9f);
				tabFav.animate()
				.alpha(1f)
				.scaleX(1f)
				.scaleY(1f)
				.setDuration(220)
				.start();
				
				android.view.animation.ScaleAnimation scaleAnim = new android.view.animation.ScaleAnimation(
				1f, 0.90f, 1f, 0.90f,
				android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
				android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
				);
				scaleAnim.setDuration(120);
				scaleAnim.setRepeatMode(android.view.animation.Animation.REVERSE);
				scaleAnim.setRepeatCount(1);
				((android.view.View)_view).startAnimation(scaleAnim);
				
			}
		});
		
		binding.tabLibrary.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				final androidx.viewpager.widget.ViewPager viewpager = findViewById(R.id.viewpager);
				final android.widget.LinearLayout tabHome = findViewById(R.id.tab_home);
				final android.widget.LinearLayout tabFav = findViewById(R.id.tab_fav);
				final android.widget.LinearLayout tabLibrary = findViewById(R.id.tab_library);
				final android.widget.ImageView iconHome = findViewById(R.id.icon_home);
				final android.widget.ImageView iconFav = findViewById(R.id.icon_fav);
				final android.widget.ImageView iconLibrary = findViewById(R.id.icon_library);
				
				if (viewpager != null) viewpager.setCurrentItem(2, true);
				
				tabHome.setBackground(null);
				tabFav.setBackground(null);
				tabLibrary.setBackgroundResource(R.drawable.bg_active_pill);
				
				iconHome.setImageResource(R.drawable.icon_home);
				iconFav.setImageResource(R.drawable.icon_explore1);
				iconLibrary.setImageResource(R.drawable.icon_library1);
				
				tabLibrary.setAlpha(0.6f);
				tabLibrary.setScaleX(0.9f);
				tabLibrary.setScaleY(0.9f);
				tabLibrary.animate()
				.alpha(1f)
				.scaleX(1f)
				.scaleY(1f)
				.setDuration(220)
				.start();
				
				android.view.animation.ScaleAnimation scaleAnim = new android.view.animation.ScaleAnimation(
				1f, 0.90f, 1f, 0.90f,
				android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
				android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
				);
				scaleAnim.setDuration(120);
				scaleAnim.setRepeatMode(android.view.animation.Animation.REVERSE);
				scaleAnim.setRepeatCount(1);
				((android.view.View)_view).startAnimation(scaleAnim);
				
			}
		});
		
		binding.imageview14.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				intent.setClass(getApplicationContext(), MostActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}
	
	private void initializeLogic() {
		CustomPermissionHandler.requestFromButton(this);
		
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
		
		link.setTabCount(3);
		binding.viewpager.setAdapter(link);
		binding.viewpager.setOffscreenPageLimit((int)3);
		((PagerAdapter)binding.viewpager.getAdapter()).notifyDataSetChanged();
		final android.widget.LinearLayout miniPlayer = findViewById(R.id.mini_player);
		final android.widget.TextView txtSong = findViewById(R.id.txt_song);
		final android.widget.TextView txtArtist = findViewById(R.id.txt_artist);
		final android.widget.ImageView imgCover = findViewById(R.id.img_cover);
		final android.widget.ImageView btnDown = findViewById(R.id.btn_down);
		final android.widget.ImageView btnPlay = findViewById(R.id.btn_play);
		
		android.view.View cardPlayer = findViewById(R.id.card_player);
		android.view.View cardBottom = findViewById(R.id.cardview37);
		android.view.View cardSearch = findViewById(R.id.cardview38);
		android.view.View cardSetting = findViewById(R.id.cardview6);
		
		if (cardPlayer != null) cardPlayer.setAlpha(0.93f);
		if (cardBottom != null) cardBottom.setAlpha(0.93f);
		if (cardSearch != null) cardSearch.setAlpha(0.96f);
		if (cardSetting != null) cardSetting.setAlpha(0.96f);
		
		if (btnPlay != null) {
			boolean isPlayingInit = com.lumina.music.MusicService.mediaPlayer != null && com.lumina.music.MusicService.mediaPlayer.isPlaying();
			btnPlay.setImageResource(isPlayingInit ? R.drawable.icon_pause : R.drawable.icon_play);
		}
		
		final Runnable updateMiniPlayerLogic = () -> {
			if (HomeworkActivity.this.isFinishing() || (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && HomeworkActivity.this.isDestroyed())) {
				return;
			}
			
			int index = com.lumina.music.MusicService.currentIndex;
			java.util.ArrayList<java.util.HashMap<String, Object>> list = com.lumina.music.MusicService.songList;
			
			if (list != null && index >= 0 && index < list.size()) {
				
				if (miniPlayer != null) {
					if (miniPlayer.getVisibility() != android.view.View.VISIBLE) {
						float slideHeight = miniPlayer.getHeight() > 0 ? miniPlayer.getHeight() : 200f;
						miniPlayer.setAlpha(0.93f);
						miniPlayer.setTranslationY(slideHeight);
						miniPlayer.setVisibility(android.view.View.VISIBLE);
						miniPlayer.animate()
						.alpha(0.93f)
						.translationY(0f)
						.setDuration(280)
						.setInterpolator(new android.view.animation.DecelerateInterpolator())
						.start();
					}
				}
				
				java.util.HashMap<String, Object> song = list.get(index);
				
				if (txtSong != null) {
					Object titleObj = song.get("title");
					txtSong.setText(titleObj != null ? titleObj.toString() : "");
				}
				
				if (txtArtist != null) {
					Object artistObj = song.get("artist");
					txtArtist.setText(artistObj != null ? artistObj.toString() : "");
				}
				
				if (imgCover != null) {
					String artworkUrl = "";
					if (song.containsKey("artwork_url") && song.get("artwork_url") != null) {
						artworkUrl = song.get("artwork_url").toString().trim();
					} else if (song.containsKey("artwork") && song.get("artwork") != null) {
						artworkUrl = song.get("artwork").toString().trim();
					}
					
					long albumId = -1;
					if (song.containsKey("album_id") && song.get("album_id") != null) {
						try {
							albumId = Long.parseLong(song.get("album_id").toString());
						} catch (Exception ignored) {}
					}
					
					if (!artworkUrl.isEmpty()) {
						com.lumina.music.ImageLoader.loadOnlineAlbumArt(
						HomeworkActivity.this,
						artworkUrl,
						imgCover,
						R.drawable.default_music_image
						);
					} else if (albumId > 0) {
						com.lumina.music.ImageLoader.loadAlbumArt(
						HomeworkActivity.this,
						albumId,
						imgCover,
						R.drawable.default_music_image
						);
					} else {
						imgCover.setImageResource(R.drawable.default_music_image);
					}
				}
				
				if (btnPlay != null) {
					boolean isPlaying = com.lumina.music.MusicService.mediaPlayer != null && com.lumina.music.MusicService.mediaPlayer.isPlaying();
					int targetRes = isPlaying ? R.drawable.icon_pause : R.drawable.icon_play;
					btnPlay.setImageResource(targetRes);
				}
				
			} else {
				if (miniPlayer != null && miniPlayer.getVisibility() == android.view.View.VISIBLE) {
					float slideHeight = miniPlayer.getHeight() > 0 ? miniPlayer.getHeight() : 200f;
					miniPlayer.animate()
					.alpha(0f)
					.translationY(slideHeight)
					.setDuration(250)
					.setInterpolator(new android.view.animation.AccelerateInterpolator())
					.withEndAction(() -> {
						miniPlayer.setVisibility(android.view.View.GONE);
						miniPlayer.setTranslationY(0f);
					})
					.start();
				}
			}
		};
		
		android.content.IntentFilter filter = new android.content.IntentFilter("MFLOW_UPDATE_PLAYER_UI");
		
		final android.content.BroadcastReceiver miniReceiver = new android.content.BroadcastReceiver() {
			@Override
			public void onReceive(android.content.Context context, android.content.Intent intent) {
				updateMiniPlayerLogic.run();
			}
		};
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(miniReceiver, filter, android.content.Context.RECEIVER_EXPORTED);
		} else {
			registerReceiver(miniReceiver, filter);
		}
		
		if (btnDown != null) {
			btnDown.setOnClickListener(v -> {
				android.content.Intent dismissIntent = new android.content.Intent(HomeworkActivity.this, com.lumina.music.MusicService.class);
				dismissIntent.setAction(com.lumina.music.NotificationHelper.ACTION_DISMISS);
				startService(dismissIntent);
				
				com.lumina.music.MusicService.currentIndex = -1;
				
				if (miniPlayer != null && miniPlayer.getVisibility() == android.view.View.VISIBLE) {
					float slideHeight = miniPlayer.getHeight() > 0 ? miniPlayer.getHeight() : 200f;
					miniPlayer.animate()
					.alpha(0f)
					.translationY(slideHeight)
					.setDuration(250)
					.setInterpolator(new android.view.animation.AccelerateInterpolator())
					.withEndAction(() -> {
						miniPlayer.setVisibility(android.view.View.GONE);
						miniPlayer.setTranslationY(0f);
					})
					.start();
				}
			});
		}
		
		if (btnPlay != null) {
			btnPlay.setOnClickListener(v -> {
				boolean isPlayingNow = com.lumina.music.MusicService.mediaPlayer != null && com.lumina.music.MusicService.mediaPlayer.isPlaying();
				
				btnPlay.setImageResource(isPlayingNow ? R.drawable.icon_play : R.drawable.icon_pause);
				
				android.content.Intent toggleIntent = new android.content.Intent(HomeworkActivity.this, com.lumina.music.MusicService.class);
				toggleIntent.setAction(com.lumina.music.NotificationHelper.ACTION_TOGGLE);
				startService(toggleIntent);
			});
		}
		
		if (miniPlayer != null) {
			miniPlayer.setOnClickListener(v -> {
				android.content.Intent openIntent = new android.content.Intent(HomeworkActivity.this, FullplayerActivity.class);
				startActivity(openIntent);
			});
		}
		
		getWindow().getDecorView().addOnAttachStateChangeListener(new android.view.View.OnAttachStateChangeListener() {
			@Override
			public void onViewAttachedToWindow(android.view.View v) {
				updateMiniPlayerLogic.run();
			}
			@Override
			public void onViewDetachedFromWindow(android.view.View v) {
				try { unregisterReceiver(miniReceiver); } catch (Exception ignored) {}
			}
		});
		
		updateMiniPlayerLogic.run();
		
		binding.txtTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtSong.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtArtist.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.txtHome.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 0);
		binding.txtFav.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 0);
		binding.txtFav.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 0);
	}
	
	public class LinkFragmentAdapter extends FragmentStatePagerAdapter {
		// This class is deprecated, you should migrate to ViewPager2:
		// https://developer.android.com/reference/androidx/viewpager2/widget/ViewPager2
		Context context;
		int tabCount;
		
		public LinkFragmentAdapter(Context context, FragmentManager manager) {
			super(manager);
			this.context = context;
		}
		
		public void setTabCount(int tabCount) {
			this.tabCount = tabCount;
		}
		
		@Override
		public int getCount() {
			return tabCount;
		}
		
		@Override
		public CharSequence getPageTitle(int _position) {
			return "";
		}
		
		
		@Override
		public Fragment getItem(int _position) {
			if (_position == 0) {
				return new HomeFragmentActivity();
			}
			if (_position == 1) {
				return new FavFragmentActivity();
			}
			if (_position == 2) {
				return new LibraryFragmentActivity();
			}
			return new Fragment();
		}
		
	}
	
}