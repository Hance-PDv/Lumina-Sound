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

public class PlaylistActivity extends AppCompatActivity {
	
	private PlaylistBinding binding;
	
	private ArrayList<HashMap<String, Object>> list_song = new ArrayList<>();
	
	private Intent intent = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = PlaylistBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
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
		
		binding.txtPlaylistTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		final TextView txtPlaylistTitle = findViewById(R.id.txtPlaylistTitle);
		
		final android.widget.FrameLayout cardBack = findViewById(R.id.cardBack);
		final ImageView btnBack = findViewById(R.id.btnBack);
		
		final androidx.recyclerview.widget.RecyclerView rvPlaylistSongs = findViewById(R.id.rvPlaylistSongs);
		
		final android.widget.FrameLayout btnDeletePlaylist = findViewById(R.id.btnDeletePlaylist);
		final android.widget.FrameLayout btnAddSongs = findViewById(R.id.btnAddSongs);
		
		final android.widget.FrameLayout btnPlayAll = findViewById(R.id.btnPlayAll);
		final android.widget.FrameLayout btnShuffle = findViewById(R.id.btnShuffle);
		
		if (cardBack != null) cardBack.setAlpha(0.96f);
		if (btnDeletePlaylist != null) btnDeletePlaylist.setAlpha(0.96f);
		if (btnAddSongs != null) btnAddSongs.setAlpha(0.96f);
		if (btnPlayAll != null) btnPlayAll.setAlpha(0.96f);
		if (btnShuffle != null) btnShuffle.setAlpha(0.96f);
		
		if (btnPlayAll != null) {
			btnPlayAll.setOnClickListener(v -> {
				if (list_song != null && !list_song.isEmpty()) {
					com.lumina.music.MusicService.songList = new java.util.ArrayList<>(list_song);
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
			});
		}
		
		if (btnShuffle != null) {
			btnShuffle.setOnClickListener(v -> {
				if (list_song != null && !list_song.isEmpty()) {
					java.util.ArrayList<java.util.HashMap<String, Object>> shuffledList = new java.util.ArrayList<>(list_song);
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
			});
		}
		
		if (rvPlaylistSongs != null) {
			androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback itemTouchCallback = 
			new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
			androidx.recyclerview.widget.ItemTouchHelper.UP | androidx.recyclerview.widget.ItemTouchHelper.DOWN, 0) {
				
				@Override
				public boolean isLongPressDragEnabled() {
					return false; 
				}
				
				@Override
				public boolean onMove(androidx.recyclerview.widget.RecyclerView recyclerView, 
				androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, 
				androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
					
					int fromPos = viewHolder.getAdapterPosition();
					int toPos = target.getAdapterPosition();
					
					if (fromPos < 0 || toPos < 0 || fromPos >= list_song.size() || toPos >= list_song.size()) {
						return false;
					}
					
					if (fromPos < toPos) {
						for (int i = fromPos; i < toPos; i++) {
							java.util.Collections.swap(list_song, i, i + 1);
						}
					} else {
						for (int i = fromPos; i > toPos; i--) {
							java.util.Collections.swap(list_song, i, i - 1);
						}
					}
					
					if (recyclerView.getAdapter() != null) {
						recyclerView.getAdapter().notifyItemMoved(fromPos, toPos);
					}
					return true;
				}
				
				@Override
				public void onSwiped(androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {}
				
				@Override
				public void onSelectedChanged(androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int actionState) {
					super.onSelectedChanged(viewHolder, actionState);
					if (actionState == androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
						viewHolder.itemView.animate()
						.scaleX(1.03f)
						.scaleY(1.03f)
						.translationZ(16f)
						.setDuration(120)
						.start();
					}
				}
				
				@Override
				public void clearView(androidx.recyclerview.widget.RecyclerView recyclerView, 
				androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder) {
					super.clearView(recyclerView, viewHolder);
					
					viewHolder.itemView.animate()
					.scaleX(1.0f)
					.scaleY(1.0f)
					.translationZ(0f)
					.setDuration(120)
					.start();
					
					android.content.Context context = recyclerView.getContext();
					String currentPlaylist = PlaylistManager.getSelectedPlaylistName();
					if (currentPlaylist != null && !currentPlaylist.isEmpty()) {
						PlaylistManager.savePlaylistSongs(context, currentPlaylist, list_song);
					}
				}
			};
			
			androidx.recyclerview.widget.ItemTouchHelper touchHelper = new androidx.recyclerview.widget.ItemTouchHelper(itemTouchCallback);
			touchHelper.attachToRecyclerView(rvPlaylistSongs);
			rvPlaylistSongs.setTag(R.id.rvPlaylistSongs, touchHelper);
		}
		
		final ImageView imageview_delete = findViewById(R.id.imageview_delete);
		final CheckBox checkboxPlaylist = findViewById(R.id.checkboxPlaylist);
		final ImageView btn_remove_playlist = findViewById(R.id.btn_remove_playlist);
		
		if (imageview_delete != null) {
			if (imageview_delete.getTag() == null) {
				imageview_delete.setTag("inactive");
				imageview_delete.setImageResource(R.drawable.icon_delete);
			} else if ("active".equals(imageview_delete.getTag())) {
				imageview_delete.setImageResource(R.drawable.icon_close);
			} else {
				imageview_delete.setImageResource(R.drawable.icon_delete);
			}
		}
		
		if (btnDeletePlaylist != null && imageview_delete != null) {
			btnDeletePlaylist.setOnClickListener(v -> {
				if (list_song == null || list_song.isEmpty()) {
					return;
				}
				
				boolean isActive = "active".equals(imageview_delete.getTag());
				
				if (isActive) {
					imageview_delete.setTag("inactive");
					imageview_delete.setImageResource(R.drawable.icon_delete);
					
					if (btn_remove_playlist != null) {
						btn_remove_playlist.setVisibility(View.GONE);
					}
					
					if (checkboxPlaylist != null) {
						checkboxPlaylist.setChecked(false);
						checkboxPlaylist.setVisibility(View.GONE);
					}
				} else {
					imageview_delete.setTag("active");
					imageview_delete.setImageResource(R.drawable.icon_close);
					
					if (checkboxPlaylist != null) {
						checkboxPlaylist.setVisibility(View.VISIBLE);
					}
				}
				
				if (rvPlaylistSongs != null && rvPlaylistSongs.getAdapter() != null) {
					rvPlaylistSongs.getAdapter().notifyDataSetChanged();
				}
			});
		}
		
		if (checkboxPlaylist != null) {
			checkboxPlaylist.setOnCheckedChangeListener((buttonView, isChecked) -> {
				if (rvPlaylistSongs != null) {
					for (int i = 0; i < rvPlaylistSongs.getChildCount(); i++) {
						android.view.View child = rvPlaylistSongs.getChildAt(i);
						com.lumina.music.SongViewHolder2 childHolder = (com.lumina.music.SongViewHolder2) child.getTag();
						if (childHolder != null && childHolder.bottomremove2 != null) {
							childHolder.bottomremove2.setChecked(isChecked);
						}
					}
				}
			});
		}
		
		if (btn_remove_playlist != null) {
			btn_remove_playlist.setOnClickListener(v -> {
				try {
					String currentPlaylist = PlaylistManager.getSelectedPlaylistName();
					android.content.Context context = v.getContext();
					if (currentPlaylist != null && context != null && rvPlaylistSongs != null) {
						java.util.List<HashMap<String, Object>> songsToRemove = new java.util.ArrayList<>();
						for (int i = 0; i < rvPlaylistSongs.getChildCount(); i++) {
							android.view.View child = rvPlaylistSongs.getChildAt(i);
							com.lumina.music.SongViewHolder2 childHolder = (com.lumina.music.SongViewHolder2) child.getTag();
							if (childHolder != null && childHolder.bottomremove2 != null && childHolder.bottomremove2.isChecked()) {
								int adapterPos = rvPlaylistSongs.getChildAdapterPosition(child);
								if (adapterPos >= 0 && adapterPos < list_song.size()) {
									songsToRemove.add(list_song.get(adapterPos));
								}
							}
						}
						
						for (HashMap<String, Object> song : songsToRemove) {
							PlaylistManager.removeSongFromPlaylist(context, currentPlaylist, song);
						}
						
						java.util.ArrayList<HashMap<String, Object>> updatedList = PlaylistManager.getPlaylistSongs(context, currentPlaylist);
						list_song.clear();
						if (updatedList != null) {
							list_song.addAll(updatedList);
						}
						
						if (imageview_delete != null) {
							imageview_delete.setTag("inactive");
							imageview_delete.setImageResource(R.drawable.icon_delete);
						}
						if (checkboxPlaylist != null) {
							checkboxPlaylist.setChecked(false);
							checkboxPlaylist.setVisibility(View.GONE);
						}
						if (btn_remove_playlist != null) {
							btn_remove_playlist.setVisibility(View.GONE);
						}
						
						if (rvPlaylistSongs.getAdapter() != null) {
							rvPlaylistSongs.getAdapter().notifyDataSetChanged();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		
		if (cardBack != null) {
			cardBack.setOnClickListener(v -> {
				if (imageview_delete != null) {
					imageview_delete.setTag("inactive");
				}
				finish();
			});
		} else if (btnBack != null) {
			btnBack.setOnClickListener(v -> {
				if (imageview_delete != null) {
					imageview_delete.setTag("inactive");
				}
				finish();
			});
		}
		
		String playlistName = PlaylistManager.getSelectedPlaylistName();
		
		if (playlistName != null && !playlistName.trim().isEmpty()) {
			if (txtPlaylistTitle != null) {
				txtPlaylistTitle.setText(playlistName);
			}
			
			list_song.clear();
			java.util.ArrayList<HashMap<String, Object>> fetchedSongs = PlaylistManager.getPlaylistSongs(getApplicationContext(), playlistName);
			if (fetchedSongs != null) {
				list_song.addAll(fetchedSongs);
			}
			
			if (rvPlaylistSongs != null && rvPlaylistSongs.getAdapter() != null) {
				rvPlaylistSongs.getAdapter().notifyDataSetChanged();
			}
		}
		
		if (btnAddSongs != null) {
			btnAddSongs.setOnClickListener(v -> {
				final String currentPlaylist = PlaylistManager.getSelectedPlaylistName();
				if (currentPlaylist == null || currentPlaylist.trim().isEmpty()) return;
				
				final ArrayList<HashMap<String, Object>> availableSongs = PlaylistManager.getAvailableSongs(getApplicationContext(), currentPlaylist);
				final ArrayList<HashMap<String, Object>> selectedSongsToAdd = new ArrayList<>();
				
				final com.google.android.material.bottomsheet.BottomSheetDialog addSongsDialog = 
				new com.google.android.material.bottomsheet.BottomSheetDialog(PlaylistActivity.this);
				
				final float density = getResources().getDisplayMetrics().density;
				final int displayHeight = getResources().getDisplayMetrics().heightPixels;
				final int targetHeight = (int) (displayHeight * 0.75);
				
				boolean isNightMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
				
				int glassBgColor = isNightMode ? android.graphics.Color.parseColor("#FF12141D") : android.graphics.Color.parseColor("#FFF5F5F7");
				int glassStrokeColor = isNightMode ? android.graphics.Color.parseColor("#33FFFFFF") : android.graphics.Color.parseColor("#25000000");
				int titleTextColor = isNightMode ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#12141D");
				int subtitleTextColor = isNightMode ? android.graphics.Color.parseColor("#B0FFFFFF") : android.graphics.Color.parseColor("#70000000");
				int iconBgColor = isNightMode ? android.graphics.Color.parseColor("#25FFFFFF") : android.graphics.Color.parseColor("#15000000");
				int closeBtnBgColor = isNightMode ? android.graphics.Color.parseColor("#20FFFFFF") : android.graphics.Color.parseColor("#15000000");
				int cancelBtnBgColor = isNightMode ? android.graphics.Color.parseColor("#26FFFFFF") : android.graphics.Color.parseColor("#15000000");
				int cancelBtnStrokeColor = isNightMode ? android.graphics.Color.parseColor("#20FFFFFF") : android.graphics.Color.parseColor("#15000000");
				
				final android.widget.LinearLayout rootLayout = new android.widget.LinearLayout(PlaylistActivity.this);
				rootLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
				rootLayout.setPadding((int) (20 * density), (int) (12 * density), (int) (20 * density), (int) (24 * density));
				
				android.graphics.drawable.GradientDrawable glassBg = new android.graphics.drawable.GradientDrawable();
				glassBg.setColor(glassBgColor); 
				glassBg.setCornerRadii(new float[]{
					30 * density, 30 * density, 30 * density, 30 * density, 0, 0, 0, 0
				});
				glassBg.setStroke((int) (1 * density), glassStrokeColor);
				rootLayout.setBackground(glassBg);
				
				final View handle = new View(PlaylistActivity.this);
				android.widget.LinearLayout.LayoutParams handleLp = new android.widget.LinearLayout.LayoutParams((int) (38 * density), (int) (4 * density));
				handleLp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
				handleLp.bottomMargin = (int) (16 * density);
				handle.setLayoutParams(handleLp);
				android.graphics.drawable.GradientDrawable handleShape = new android.graphics.drawable.GradientDrawable();
				handleShape.setColor(isNightMode ? android.graphics.Color.parseColor("#40FFFFFF") : android.graphics.Color.parseColor("#40000000"));
				handleShape.setCornerRadius(2 * density);
				handle.setBackground(handleShape);
				rootLayout.addView(handle);
				
				android.widget.LinearLayout headerLayout = new android.widget.LinearLayout(PlaylistActivity.this);
				headerLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
				headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
				headerLayout.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
				
				android.widget.FrameLayout iconContainer = new android.widget.FrameLayout(PlaylistActivity.this);
				android.widget.LinearLayout.LayoutParams iconLp = new android.widget.LinearLayout.LayoutParams((int) (42 * density), (int) (42 * density));
				iconContainer.setLayoutParams(iconLp);
				android.graphics.drawable.GradientDrawable iconBg = new android.graphics.drawable.GradientDrawable();
				iconBg.setColor(iconBgColor);
				iconBg.setCornerRadius(14 * density);
				iconBg.setStroke((int) (1 * density), glassStrokeColor);
				iconContainer.setBackground(iconBg);
				
				android.widget.ImageView imgIcon = new android.widget.ImageView(PlaylistActivity.this);
				android.widget.FrameLayout.LayoutParams imgLp = new android.widget.FrameLayout.LayoutParams((int) (24 * density), (int) (24 * density));
				imgLp.gravity = android.view.Gravity.CENTER;
				imgIcon.setLayoutParams(imgLp);
				imgIcon.setImageResource(R.drawable.default_music_image);
				iconContainer.addView(imgIcon);
				headerLayout.addView(iconContainer);
				
				android.widget.LinearLayout headerTextLayout = new android.widget.LinearLayout(PlaylistActivity.this);
				headerTextLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
				android.widget.LinearLayout.LayoutParams textLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
				textLp.leftMargin = (int) (12 * density);
				textLp.setMarginStart((int) (12 * density));
				headerTextLayout.setLayoutParams(textLp);
				
				TextView tvHeaderTitle = new TextView(PlaylistActivity.this);
				tvHeaderTitle.setText("Add Songs");
				tvHeaderTitle.setTextSize(18);
				tvHeaderTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
				tvHeaderTitle.setTextColor(titleTextColor);
				
				TextView tvHeaderSubtitle = new TextView(PlaylistActivity.this);
				tvHeaderSubtitle.setText("Select tracks to add to playlist");
				tvHeaderSubtitle.setTextSize(12);
				tvHeaderSubtitle.setTextColor(subtitleTextColor);
				
				headerTextLayout.addView(tvHeaderTitle);
				headerTextLayout.addView(tvHeaderSubtitle);
				headerLayout.addView(headerTextLayout);
				
				android.widget.FrameLayout btnClose = new android.widget.FrameLayout(PlaylistActivity.this);
				android.widget.LinearLayout.LayoutParams closeLp = new android.widget.LinearLayout.LayoutParams((int) (32 * density), (int) (32 * density));
				btnClose.setLayoutParams(closeLp);
				android.graphics.drawable.GradientDrawable closeBg = new android.graphics.drawable.GradientDrawable();
				closeBg.setColor(closeBtnBgColor);
				closeBg.setCornerRadius(16 * density);
				btnClose.setBackground(closeBg);
				
				TextView txtClose = new TextView(PlaylistActivity.this);
				txtClose.setText("✕");
				txtClose.setTextSize(13);
				txtClose.setTextColor(titleTextColor);
				android.widget.FrameLayout.LayoutParams txtCloseLp = new android.widget.FrameLayout.LayoutParams(
				android.widget.FrameLayout.LayoutParams.WRAP_CONTENT, android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
				txtCloseLp.gravity = android.view.Gravity.CENTER;
				txtClose.setLayoutParams(txtCloseLp);
				btnClose.addView(txtClose);
				
				headerLayout.addView(btnClose);
				rootLayout.addView(headerLayout);
				
				final TextView txtEmptyDialog = new TextView(PlaylistActivity.this);
				txtEmptyDialog.setText("No available songs found");
				txtEmptyDialog.setTextSize(14);
				txtEmptyDialog.setTextColor(subtitleTextColor);
				txtEmptyDialog.setGravity(android.view.Gravity.CENTER);
				android.widget.LinearLayout.LayoutParams emptyLp = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, (int) (120 * density));
				emptyLp.topMargin = (int) (24 * density);
				emptyLp.bottomMargin = (int) (24 * density);
				txtEmptyDialog.setLayoutParams(emptyLp);
				txtEmptyDialog.setVisibility(availableSongs.isEmpty() ? View.VISIBLE : View.GONE);
				rootLayout.addView(txtEmptyDialog);
				
				final androidx.recyclerview.widget.RecyclerView rvAddSongs = new androidx.recyclerview.widget.RecyclerView(PlaylistActivity.this);
				android.widget.LinearLayout.LayoutParams rvLp = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
				rvLp.topMargin = (int) (16 * density);
				rvLp.bottomMargin = (int) (16 * density);
				rvAddSongs.setLayoutParams(rvLp);
				rvAddSongs.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(PlaylistActivity.this));
				rvAddSongs.setVisibility(availableSongs.isEmpty() ? View.GONE : View.VISIBLE);
				rootLayout.addView(rvAddSongs);
				
				android.widget.LinearLayout bottomButtonsLayout = new android.widget.LinearLayout(PlaylistActivity.this);
				bottomButtonsLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
				android.widget.LinearLayout.LayoutParams bBtnsLp = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
				bBtnsLp.topMargin = (int) (8 * density);
				bottomButtonsLayout.setLayoutParams(bBtnsLp);
				
				TextView btnCancelDialog = new TextView(PlaylistActivity.this);
				btnCancelDialog.setText("Cancel");
				btnCancelDialog.setTextSize(14);
				btnCancelDialog.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
				btnCancelDialog.setTextColor(titleTextColor);
				btnCancelDialog.setGravity(android.view.Gravity.CENTER);
				android.widget.LinearLayout.LayoutParams cancelLp = new android.widget.LinearLayout.LayoutParams(0, (int) (46 * density), 1.0f);
				cancelLp.rightMargin = (int) (8 * density);
				cancelLp.setMarginEnd((int) (8 * density));
				btnCancelDialog.setLayoutParams(cancelLp);
				
				android.graphics.drawable.GradientDrawable cancelBg = new android.graphics.drawable.GradientDrawable();
				cancelBg.setColor(cancelBtnBgColor);
				cancelBg.setCornerRadius(23 * density);
				cancelBg.setStroke((int) (1 * density), cancelBtnStrokeColor);
				btnCancelDialog.setBackground(cancelBg);
				
				TextView btnDoneDialog = new TextView(PlaylistActivity.this);
				btnDoneDialog.setText("Done");
				btnDoneDialog.setTextSize(14);
				btnDoneDialog.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
				btnDoneDialog.setTextColor(android.graphics.Color.WHITE);
				btnDoneDialog.setGravity(android.view.Gravity.CENTER);
				android.widget.LinearLayout.LayoutParams doneLp = new android.widget.LinearLayout.LayoutParams(0, (int) (46 * density), 1.0f);
				btnDoneDialog.setLayoutParams(doneLp);
				
				android.graphics.drawable.GradientDrawable doneBg = new android.graphics.drawable.GradientDrawable();
				doneBg.setColor(android.graphics.Color.parseColor("#402196F3"));
				doneBg.setCornerRadius(23 * density);
				doneBg.setStroke((int) (1 * density), android.graphics.Color.parseColor("#602196F3"));
				btnDoneDialog.setBackground(doneBg);
				
				bottomButtonsLayout.addView(btnCancelDialog);
				bottomButtonsLayout.addView(btnDoneDialog);
				rootLayout.addView(bottomButtonsLayout);
				
				addSongsDialog.setContentView(rootLayout);
				addSongsDialog.setCanceledOnTouchOutside(true);
				
				final boolean[] selectedState = new boolean[availableSongs.size()];
				
				if (!availableSongs.isEmpty()) {
					rvAddSongs.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
						
						@Override
						public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
							android.widget.LinearLayout itemView = new android.widget.LinearLayout(parent.getContext());
							itemView.setOrientation(android.widget.LinearLayout.HORIZONTAL);
							itemView.setGravity(android.view.Gravity.CENTER_VERTICAL);
							android.view.ViewGroup.LayoutParams itemLp = new android.view.ViewGroup.LayoutParams(
							android.view.ViewGroup.LayoutParams.MATCH_PARENT, (int) (64 * density));
							itemView.setLayoutParams(itemLp);
							itemView.setPadding((int) (8 * density), (int) (8 * density), (int) (8 * density), (int) (8 * density));
							
							android.widget.ImageView imgSongCover = new android.widget.ImageView(parent.getContext());
							android.widget.LinearLayout.LayoutParams imgCovLp = new android.widget.LinearLayout.LayoutParams((int) (48 * density), (int) (48 * density));
							imgSongCover.setLayoutParams(imgCovLp);
							imgSongCover.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
							imgSongCover.setClipToOutline(true);
							imgSongCover.setOutlineProvider(new android.view.ViewOutlineProvider() {
								@Override
								public void getOutline(android.view.View view, android.graphics.Outline outline) {
									outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 12 * density);
								}
							});
							itemView.addView(imgSongCover);
							
							android.widget.LinearLayout textContainer = new android.widget.LinearLayout(parent.getContext());
							textContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
							android.widget.LinearLayout.LayoutParams textConLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
							textConLp.leftMargin = (int) (12 * density);
							textConLp.setMarginStart((int) (12 * density));
							textContainer.setLayoutParams(textConLp);
							
							TextView txtSongTitle = new TextView(parent.getContext());
							txtSongTitle.setTextSize(14);
							txtSongTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
							txtSongTitle.setTextColor(titleTextColor);
							txtSongTitle.setSingleLine(true);
							textContainer.addView(txtSongTitle);
							
							TextView txtSongArtist = new TextView(parent.getContext());
							txtSongArtist.setTextSize(12);
							txtSongArtist.setTextColor(subtitleTextColor);
							txtSongArtist.setSingleLine(true);
							textContainer.addView(txtSongArtist);
							
							itemView.addView(textContainer);
							
							CheckBox chkSelectSong = new CheckBox(parent.getContext());
							android.widget.LinearLayout.LayoutParams chkLp = new android.widget.LinearLayout.LayoutParams(
							android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
							chkSelectSong.setLayoutParams(chkLp);
							itemView.addView(chkSelectSong);
							
							return new androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {};
						}
						
						@Override
						public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder holder, final int position) {
							android.widget.LinearLayout itemView = (android.widget.LinearLayout) holder.itemView;
							
							android.widget.ImageView imgSongCover = (android.widget.ImageView) itemView.getChildAt(0);
							android.widget.LinearLayout textContainer = (android.widget.LinearLayout) itemView.getChildAt(1);
							TextView txtSongTitle = (TextView) textContainer.getChildAt(0);
							TextView txtSongArtist = (TextView) textContainer.getChildAt(1);
							CheckBox chkSelectSong = (CheckBox) itemView.getChildAt(2);
							
							HashMap<String, Object> songMap = availableSongs.get(position);
							
							String title = songMap.containsKey("title") && songMap.get("title") != null ? songMap.get("title").toString() : "Unknown";
							txtSongTitle.setText(title);
							
							String artist = songMap.containsKey("artist") && songMap.get("artist") != null ? songMap.get("artist").toString() : "<unknown>";
							txtSongArtist.setText(artist);
							
							long albumId = -1;
							if (songMap.get("album_id") != null) {
								try {
									albumId = Long.parseLong(songMap.get("album_id").toString());
								} catch (Exception ignored) {}
							}
							ImageLoader.loadAlbumArt(PlaylistActivity.this, albumId, imgSongCover, R.drawable.default_music_image);
							
							chkSelectSong.setChecked(selectedState[position]);
							
							chkSelectSong.setOnClickListener(vBox -> {
								selectedState[position] = chkSelectSong.isChecked();
							});
							
							itemView.setOnClickListener(v1 -> {
								selectedState[position] = !selectedState[position];
								chkSelectSong.setChecked(selectedState[position]);
							});
						}
						
						@Override
						public int getItemCount() {
							return availableSongs.size();
						}
					});
				}
				
				btnCancelDialog.setOnClickListener(v12 -> addSongsDialog.dismiss());
				btnClose.setOnClickListener(vClose -> addSongsDialog.dismiss());
				
				btnDoneDialog.setOnClickListener(v13 -> {
					selectedSongsToAdd.clear();
					for (int i = 0; i < availableSongs.size(); i++) {
						if (selectedState[i]) {
							selectedSongsToAdd.add(availableSongs.get(i));
						}
					}
					
					if (!selectedSongsToAdd.isEmpty()) {
						PlaylistManager.addSongsToPlaylist(getApplicationContext(), currentPlaylist, selectedSongsToAdd);
						
						list_song.clear();
						java.util.ArrayList<HashMap<String, Object>> updatedList = PlaylistManager.getPlaylistSongs(getApplicationContext(), currentPlaylist);
						if (updatedList != null) {
							list_song.addAll(updatedList);
						}
						
						if (rvPlaylistSongs != null && rvPlaylistSongs.getAdapter() != null) {
							rvPlaylistSongs.getAdapter().notifyDataSetChanged();
						}
					}
					
					addSongsDialog.dismiss();
				});
				
				addSongsDialog.setOnShowListener(dialogInterface -> {
					com.google.android.material.bottomsheet.BottomSheetDialog d = 
					(com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;
					final android.widget.FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
					if (bottomSheet != null) {
						bottomSheet.setBackgroundResource(android.R.color.transparent);
						
						final com.google.android.material.bottomsheet.BottomSheetBehavior<android.widget.FrameLayout> behavior = 
						com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
						
						android.view.ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
						layoutParams.height = targetHeight;
						bottomSheet.setLayoutParams(layoutParams);
						
						behavior.setPeekHeight(targetHeight);
						behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
						behavior.setSkipCollapsed(true);
					}
				});
				
				addSongsDialog.show();
			});
		}
		
		binding.rvPlaylistSongs.setAdapter(new RvPlaylistSongsAdapter(list_song));
		binding.rvPlaylistSongs.setLayoutManager(new LinearLayoutManager(this));
		binding.vscroll4.setVerticalFadingEdgeEnabled(true);
		binding.vscroll4.setFadingEdgeLength(70);
		
	}
	
	public class RvPlaylistSongsAdapter extends RecyclerView.Adapter<RvPlaylistSongsAdapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public RvPlaylistSongsAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getLayoutInflater();
			View _v = _inflater.inflate(R.layout.dome, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			DomeBinding binding = DomeBinding.bind(_view);
			
			com.lumina.music.SongViewHolder2 holder = (com.lumina.music.SongViewHolder2) _view.getTag();
			if (holder == null) {
				holder = new com.lumina.music.SongViewHolder2(_view);
				_view.setTag(holder);
			}
			
			if (_position < 0 || _position >= list_song.size()) {
				return;
			}
			
			final HashMap<String, Object> map = list_song.get(_position);
			
			if (holder.title2 != null) {
				holder.title2.setText(map.containsKey("title") && map.get("title") != null ? map.get("title").toString() : "Unknown");
			}
			if (holder.art2 != null) {
				holder.art2.setText(map.containsKey("artist") && map.get("artist") != null ? map.get("artist").toString() : "<unknown>");
			}
			
			long albumId = -1;
			if (map.get("album_id") != null) {
				try {
					albumId = Long.parseLong(map.get("album_id").toString());
				} catch (Exception ignored) {}
			}
			
			if (holder.img_music2 != null) {
				com.lumina.music.ImageLoader.loadAlbumArt(
				_view.getContext(),
				albumId,
				holder.img_music2,
				R.drawable.default_music_image
				);
			}
			
			if (holder.linearm2 != null) {
				android.util.TypedValue typedValue = new android.util.TypedValue();
				_view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
				holder.linearm2.setBackgroundResource(typedValue.resourceId);
			}
			
			final com.lumina.music.SongViewHolder2 finalHolder = holder;
			
			if (holder.linearm2 != null) {
				// الضغط العادي: تشغيل الأغنية بالتحديث المباشر للموقع الحقيقي
				holder.linearm2.setOnClickListener(v -> {
					try {
						android.content.Context context = v.getContext();
						if (context instanceof android.app.Activity) {
							android.app.Activity act = (android.app.Activity) context;
							android.widget.ImageView imgDeleteHeader = act.findViewById(R.id.imageview_delete);
							if (imgDeleteHeader != null && "active".equals(imgDeleteHeader.getTag())) {
								if (finalHolder.bottomremove2 != null) {
									finalHolder.bottomremove2.setChecked(!finalHolder.bottomremove2.isChecked());
								}
								return;
							}
							
							// جلب الموُقع الديناميكي الحقيقي للأغنية بعد السحب والإفلات
							androidx.recyclerview.widget.RecyclerView rv = act.findViewById(R.id.rvPlaylistSongs);
							int currentPos = -1;
							if (rv != null) {
								androidx.recyclerview.widget.RecyclerView.ViewHolder vh = rv.findContainingViewHolder(v);
								if (vh != null) {
									currentPos = vh.getAdapterPosition();
								}
							}
							if (currentPos == -1) {
								currentPos = _position;
							}
							
							if (currentPos >= 0 && currentPos < list_song.size()) {
								com.lumina.music.MusicService.songList = new java.util.ArrayList<>(list_song);
								com.lumina.music.MusicService.currentIndex = currentPos;
								
								if (context != null) {
									android.content.Intent playIntent = new android.content.Intent(context, com.lumina.music.MusicService.class);
									playIntent.setAction("ACTION_PLAY");
									playIntent.putExtra("index", currentPos);
									
									if (list_song.get(currentPos).containsKey("path")) {
										playIntent.putExtra("path", String.valueOf(list_song.get(currentPos).get("path")));
									}
									
									if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
										context.startForegroundService(playIntent);
									} else {
										context.startService(playIntent);
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				
				// الضغط المطول: رفع الأغنية وبدء السحب لإعادة الترتيب
				holder.linearm2.setOnLongClickListener(v -> {
					try {
						android.content.Context context = v.getContext();
						if (context instanceof android.app.Activity) {
							android.app.Activity act = (android.app.Activity) context;
							android.widget.ImageView imgDeleteHeader = act.findViewById(R.id.imageview_delete);
							
							if (imgDeleteHeader != null && "active".equals(imgDeleteHeader.getTag())) {
								return false;
							}
							
							androidx.recyclerview.widget.RecyclerView rv = act.findViewById(R.id.rvPlaylistSongs);
							if (rv != null) {
								Object tag = rv.getTag(R.id.rvPlaylistSongs);
								if (tag instanceof androidx.recyclerview.widget.ItemTouchHelper) {
									androidx.recyclerview.widget.RecyclerView.ViewHolder vh = rv.findContainingViewHolder(v);
									if (vh != null) {
										((androidx.recyclerview.widget.ItemTouchHelper) tag).startDrag(vh);
										return true;
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				});
			}
			
			if (holder.bottomremove2 != null) {
				if (_view.getContext() instanceof android.app.Activity) {
					android.app.Activity act = (android.app.Activity) _view.getContext();
					android.widget.ImageView imgDeleteHeader = act.findViewById(R.id.imageview_delete);
					if (imgDeleteHeader != null && "active".equals(imgDeleteHeader.getTag())) {
						holder.bottomremove2.setVisibility(android.view.View.VISIBLE);
					} else {
						holder.bottomremove2.setVisibility(android.view.View.GONE);
						holder.bottomremove2.setChecked(false);
					}
				} else {
					holder.bottomremove2.setVisibility(android.view.View.GONE);
				}
				
				holder.bottomremove2.setOnCheckedChangeListener(null);
				
				holder.bottomremove2.setOnCheckedChangeListener((buttonView, isChecked) -> {
					try {
						android.content.Context context = buttonView.getContext();
						if (context instanceof android.app.Activity) {
							android.app.Activity act = (android.app.Activity) context;
							androidx.recyclerview.widget.RecyclerView rv = act.findViewById(R.id.rvPlaylistSongs);
							android.widget.ImageView btnRemovePlaylist = act.findViewById(R.id.btn_remove_playlist);
							android.widget.CheckBox chkPlaylist = act.findViewById(R.id.checkboxPlaylist);
							
							if (rv != null && btnRemovePlaylist != null) {
								boolean anyChecked = false;
								boolean allChecked = true;
								int visibleCount = 0;
								
								for (int i = 0; i < rv.getChildCount(); i++) {
									android.view.View child = rv.getChildAt(i);
									com.lumina.music.SongViewHolder2 childHolder = (com.lumina.music.SongViewHolder2) child.getTag();
									if (childHolder != null && childHolder.bottomremove2 != null && childHolder.bottomremove2.getVisibility() == android.view.View.VISIBLE) {
										visibleCount++;
										if (childHolder.bottomremove2.isChecked()) {
											anyChecked = true;
										} else {
											allChecked = false;
										}
									}
								}
								
								if (anyChecked) {
									btnRemovePlaylist.setVisibility(android.view.View.VISIBLE);
								} else {
									btnRemovePlaylist.setVisibility(android.view.View.GONE);
								}
								
								if (chkPlaylist != null && visibleCount > 0) {
									final androidx.recyclerview.widget.RecyclerView finalRv = rv;
									chkPlaylist.setOnCheckedChangeListener(null);
									chkPlaylist.setChecked(allChecked);
									chkPlaylist.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
										if (finalRv != null) {
											for (int j = 0; j < finalRv.getChildCount(); j++) {
												android.view.View c = finalRv.getChildAt(j);
												com.lumina.music.SongViewHolder2 ch = (com.lumina.music.SongViewHolder2) c.getTag();
												if (ch != null && ch.bottomremove2 != null) {
													ch.bottomremove2.setChecked(isChecked1);
												}
											}
										}
									});
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
			
			binding.title2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
			binding.art2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
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