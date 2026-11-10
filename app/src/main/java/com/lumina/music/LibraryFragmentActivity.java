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

public class LibraryFragmentActivity extends Fragment {
	
	private LibraryFragmentBinding binding;
	
	private ArrayList<HashMap<String, Object>> playlist = new ArrayList<>();
	
	private Intent intent = new Intent();
	
	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater _inflater, @Nullable ViewGroup _container, @Nullable Bundle _savedInstanceState) {
		binding = LibraryFragmentBinding.inflate(_inflater, _container, false);
		initialize(_savedInstanceState, binding.getRoot());
		initializeLogic();
		return binding.getRoot();
	}
	
	private void initialize(Bundle _savedInstanceState, View _view) {
		binding.vscrollMain.setVerticalFadingEdgeEnabled(true);
		binding.vscrollMain.setFadingEdgeLength(70);
		
		
		binding.relativelayout3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				intent.setClass(getContext().getApplicationContext(), ShoeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		
		binding.relativelayout4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				intent.setClass(getContext().getApplicationContext(), StatsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}
	
	private void initializeLogic() {
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final androidx.recyclerview.widget.RecyclerView rvPlaylists = getActivity().findViewById(R.id.rvPlaylists);
		
		final Runnable loadPlaylistsData = () -> {
			playlist.clear();
			ArrayList<HashMap<String, Object>> rawPlaylists = PlaylistManager.getPlaylistsListMap(getContext());
			
			for (HashMap<String, Object> pMap : rawPlaylists) {
				String pName = pMap.containsKey("name") && pMap.get("name") != null ? pMap.get("name").toString() : "";
				ArrayList<HashMap<String, Object>> pSongs = PlaylistManager.getPlaylistSongs(getContext(), pName);
				
				long lastAlbumId = -1L;
				if (pSongs != null && !pSongs.isEmpty()) {
					HashMap<String, Object> lastSong = pSongs.get(pSongs.size() - 1);
					if (lastSong.containsKey("albumId") && lastSong.get("albumId") != null) {
						try {
							lastAlbumId = Long.parseLong(lastSong.get("albumId").toString());
						} catch (Exception ignored) {}
					} else if (lastSong.containsKey("album_id") && lastSong.get("album_id") != null) {
						try {
							lastAlbumId = Long.parseLong(lastSong.get("album_id").toString());
						} catch (Exception ignored) {}
					}
				}
				
				pMap.put("lastAlbumId", lastAlbumId);
				playlist.add(pMap);
			}
		};
		
		loadPlaylistsData.run();
		
		if (rvPlaylists != null && rvPlaylists.getAdapter() != null) {
			rvPlaylists.getAdapter().notifyDataSetChanged();
		}
		
		final android.widget.FrameLayout btnCreatePlaylist = getActivity().findViewById(R.id.btnCreatePlaylist);
		if (btnCreatePlaylist != null) {
			btnCreatePlaylist.setOnClickListener(v -> {
				if (getContext() == null) return;
				
				final com.google.android.material.bottomsheet.BottomSheetDialog createPlaylistDialog = 
				new com.google.android.material.bottomsheet.BottomSheetDialog(getContext());
				
				final float density = getContext().getResources().getDisplayMetrics().density;
				
				boolean isNightMode = (getContext().getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
				
				int glassBgColor = isNightMode ? android.graphics.Color.parseColor("#FF12141D") : android.graphics.Color.parseColor("#FFF5F5F7");
				int glassStrokeColor = isNightMode ? android.graphics.Color.parseColor("#33FFFFFF") : android.graphics.Color.parseColor("#25000000");
				int titleTextColor = isNightMode ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#12141D");
				int subtitleTextColor = isNightMode ? android.graphics.Color.parseColor("#B0FFFFFF") : android.graphics.Color.parseColor("#70000000");
				int iconBgColor = isNightMode ? android.graphics.Color.parseColor("#25FFFFFF") : android.graphics.Color.parseColor("#15000000");
				int closeBtnBgColor = isNightMode ? android.graphics.Color.parseColor("#20FFFFFF") : android.graphics.Color.parseColor("#15000000");
				int cancelBtnBgColor = isNightMode ? android.graphics.Color.parseColor("#26FFFFFF") : android.graphics.Color.parseColor("#15000000");
				int cancelBtnStrokeColor = isNightMode ? android.graphics.Color.parseColor("#20FFFFFF") : android.graphics.Color.parseColor("#15000000");
				int inputBgColor = isNightMode ? android.graphics.Color.parseColor("#1AFFFFFF") : android.graphics.Color.parseColor("#10000000");
				int inputStrokeColor = isNightMode ? android.graphics.Color.parseColor("#25FFFFFF") : android.graphics.Color.parseColor("#20000000");
				
				final android.widget.LinearLayout rootLayout = new android.widget.LinearLayout(getContext());
				rootLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
				rootLayout.setPadding((int) (20 * density), (int) (12 * density), (int) (20 * density), (int) (24 * density));
				
				android.graphics.drawable.GradientDrawable glassBg = new android.graphics.drawable.GradientDrawable();
				glassBg.setColor(glassBgColor); 
				glassBg.setCornerRadii(new float[]{
					30 * density, 30 * density, 30 * density, 30 * density, 0, 0, 0, 0
				});
				glassBg.setStroke((int) (1 * density), glassStrokeColor);
				rootLayout.setBackground(glassBg);
				
				final View handle = new View(getContext());
				android.widget.LinearLayout.LayoutParams handleLp = new android.widget.LinearLayout.LayoutParams((int) (38 * density), (int) (4 * density));
				handleLp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
				handleLp.bottomMargin = (int) (16 * density);
				handle.setLayoutParams(handleLp);
				android.graphics.drawable.GradientDrawable handleShape = new android.graphics.drawable.GradientDrawable();
				handleShape.setColor(isNightMode ? android.graphics.Color.parseColor("#40FFFFFF") : android.graphics.Color.parseColor("#40000000"));
				handleShape.setCornerRadius(2 * density);
				handle.setBackground(handleShape);
				rootLayout.addView(handle);
				
				android.widget.LinearLayout headerLayout = new android.widget.LinearLayout(getContext());
				headerLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
				headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
				headerLayout.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
				
				android.widget.FrameLayout iconContainer = new android.widget.FrameLayout(getContext());
				android.widget.LinearLayout.LayoutParams iconLp = new android.widget.LinearLayout.LayoutParams((int) (42 * density), (int) (42 * density));
				iconContainer.setLayoutParams(iconLp);
				android.graphics.drawable.GradientDrawable iconBg = new android.graphics.drawable.GradientDrawable();
				iconBg.setColor(iconBgColor);
				iconBg.setCornerRadius(14 * density);
				iconBg.setStroke((int) (1 * density), glassStrokeColor);
				iconContainer.setBackground(iconBg);
				
				android.widget.ImageView imgIcon = new android.widget.ImageView(getContext());
				android.widget.FrameLayout.LayoutParams imgLp = new android.widget.FrameLayout.LayoutParams((int) (24 * density), (int) (24 * density));
				imgLp.gravity = android.view.Gravity.CENTER;
				imgIcon.setLayoutParams(imgLp);
				imgIcon.setImageResource(R.drawable.default_music_image);
				iconContainer.addView(imgIcon);
				headerLayout.addView(iconContainer);
				
				android.widget.LinearLayout headerTextLayout = new android.widget.LinearLayout(getContext());
				headerTextLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
				android.widget.LinearLayout.LayoutParams textLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
				textLp.leftMargin = (int) (12 * density);
				textLp.setMarginStart((int) (12 * density));
				headerTextLayout.setLayoutParams(textLp);
				
				TextView tvHeaderTitle = new TextView(getContext());
				tvHeaderTitle.setText("New Playlist");
				tvHeaderTitle.setTextSize(18);
				tvHeaderTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
				tvHeaderTitle.setTextColor(titleTextColor);
				
				TextView tvHeaderSubtitle = new TextView(getContext());
				tvHeaderSubtitle.setText("Enter a name for your playlist");
				tvHeaderSubtitle.setTextSize(12);
				tvHeaderSubtitle.setTextColor(subtitleTextColor);
				
				headerTextLayout.addView(tvHeaderTitle);
				headerTextLayout.addView(tvHeaderSubtitle);
				headerLayout.addView(headerTextLayout);
				
				android.widget.FrameLayout btnClose = new android.widget.FrameLayout(getContext());
				android.widget.LinearLayout.LayoutParams closeLp = new android.widget.LinearLayout.LayoutParams((int) (32 * density), (int) (32 * density));
				btnClose.setLayoutParams(closeLp);
				android.graphics.drawable.GradientDrawable closeBg = new android.graphics.drawable.GradientDrawable();
				closeBg.setColor(closeBtnBgColor);
				closeBg.setCornerRadius(16 * density);
				btnClose.setBackground(closeBg);
				
				TextView txtClose = new TextView(getContext());
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
				
				final EditText inputName = new EditText(getContext());
				inputName.setHint("Playlist Name");
				inputName.setHintTextColor(subtitleTextColor);
				inputName.setTextColor(titleTextColor);
				inputName.setTextSize(14);
				inputName.setSingleLine(true);
				inputName.setPadding((int) (16 * density), (int) (14 * density), (int) (16 * density), (int) (14 * density));
				
				android.graphics.drawable.GradientDrawable inputBg = new android.graphics.drawable.GradientDrawable();
				inputBg.setColor(inputBgColor);
				inputBg.setCornerRadius(16 * density);
				inputBg.setStroke((int) (1 * density), inputStrokeColor);
				inputName.setBackground(inputBg);
				
				android.widget.LinearLayout.LayoutParams inputLp = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
				inputLp.topMargin = (int) (20 * density);
				inputLp.bottomMargin = (int) (20 * density);
				inputName.setLayoutParams(inputLp);
				rootLayout.addView(inputName);
				
				android.widget.LinearLayout bottomButtonsLayout = new android.widget.LinearLayout(getContext());
				bottomButtonsLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
				android.widget.LinearLayout.LayoutParams bBtnsLp = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
				bBtnsLp.topMargin = (int) (4 * density);
				bottomButtonsLayout.setLayoutParams(bBtnsLp);
				
				TextView btnCancelDialog = new TextView(getContext());
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
				
				TextView btnCreateDialog = new TextView(getContext());
				btnCreateDialog.setText("Create");
				btnCreateDialog.setTextSize(14);
				btnCreateDialog.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
				btnCreateDialog.setTextColor(android.graphics.Color.WHITE);
				btnCreateDialog.setGravity(android.view.Gravity.CENTER);
				android.widget.LinearLayout.LayoutParams createLp = new android.widget.LinearLayout.LayoutParams(0, (int) (46 * density), 1.0f);
				btnCreateDialog.setLayoutParams(createLp);
				
				android.graphics.drawable.GradientDrawable createBg = new android.graphics.drawable.GradientDrawable();
				createBg.setColor(android.graphics.Color.parseColor("#402196F3"));
				createBg.setCornerRadius(23 * density);
				createBg.setStroke((int) (1 * density), android.graphics.Color.parseColor("#602196F3"));
				btnCreateDialog.setBackground(createBg);
				
				bottomButtonsLayout.addView(btnCancelDialog);
				bottomButtonsLayout.addView(btnCreateDialog);
				rootLayout.addView(bottomButtonsLayout);
				
				createPlaylistDialog.setContentView(rootLayout);
				createPlaylistDialog.setCanceledOnTouchOutside(true);
				
				btnCancelDialog.setOnClickListener(v1 -> createPlaylistDialog.dismiss());
				btnClose.setOnClickListener(vClose -> createPlaylistDialog.dismiss());
				
				btnCreateDialog.setOnClickListener(v12 -> {
					String name = inputName.getText().toString().trim();
					if (!name.isEmpty()) {
						PlaylistManager.createPlaylist(getContext(), name);
						
						loadPlaylistsData.run();
						
						if (rvPlaylists != null && rvPlaylists.getAdapter() != null) {
							rvPlaylists.getAdapter().notifyDataSetChanged();
						}
					}
					createPlaylistDialog.dismiss();
				});
				
				createPlaylistDialog.setOnShowListener(dialogInterface -> {
					com.google.android.material.bottomsheet.BottomSheetDialog d = 
					(com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;
					final android.widget.FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
					if (bottomSheet != null) {
						bottomSheet.setBackgroundResource(android.R.color.transparent);
						com.google.android.material.bottomsheet.BottomSheetBehavior<android.widget.FrameLayout> behavior = 
						com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
						behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
						behavior.setSkipCollapsed(true);
					}
				});
				
				createPlaylistDialog.show();
			});
		}
		
		binding.rvPlaylists.setAdapter(new RvPlaylistsAdapter(playlist));
		binding.rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
	}
	public class RvPlaylistsAdapter extends RecyclerView.Adapter<RvPlaylistsAdapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public RvPlaylistsAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getActivity().getLayoutInflater();
			View _v = _inflater.inflate(R.layout.listplay, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			ListplayBinding binding = ListplayBinding.bind(_view);
			
			final View customView = _view;
			
			TextView txtPlaylistName = customView.findViewById(R.id.txtPlaylistName);
			TextView txtSongCount = customView.findViewById(R.id.txtSongCount);
			ImageView imgPlaylist = customView.findViewById(R.id.imgPlaylist);
			
			// تغيير ربط cardDeletePlaylist كـ FrameLayout
			android.widget.FrameLayout cardDeletePlaylist = customView.findViewById(R.id.cardDeletePlaylist);
			
			final HashMap<String, Object> currentItem = playlist.get((int)_position);
			final String name = currentItem.containsKey("name") && currentItem.get("name") != null ? currentItem.get("name").toString() : "";
			String count = currentItem.containsKey("count") && currentItem.get("count") != null ? currentItem.get("count").toString() : "0";
			
			if (txtPlaylistName != null) {
				txtPlaylistName.setText(name);
			}
			
			if (txtSongCount != null) {
				txtSongCount.setText(count + " Tracks");
			}
			
			if (imgPlaylist != null) {
				long albumId = -1;
				
				if (currentItem.containsKey("last_album_id") && currentItem.get("last_album_id") != null) {
					try {
						albumId = Long.parseLong(currentItem.get("last_album_id").toString());
					} catch (Exception ignored) {}
				} else {
					ArrayList<HashMap<String, Object>> playlistSongs = PlaylistManager.getPlaylistSongs(customView.getContext(), name);
					if (playlistSongs != null && !playlistSongs.isEmpty()) {
						HashMap<String, Object> lastSong = playlistSongs.get(playlistSongs.size() - 1);
						if (lastSong.containsKey("album_id") && lastSong.get("album_id") != null) {
							try {
								albumId = Long.parseLong(lastSong.get("album_id").toString());
							} catch (Exception ignored) {}
						}
					}
				}
				
				if (albumId > 0) {
					ImageLoader.loadAlbumArt(customView.getContext(), albumId, imgPlaylist, R.drawable.ic_add);
				} else {
					imgPlaylist.setImageResource(R.drawable.ic_add);
				}
			}
			
			customView.setClickable(true);
			customView.setFocusable(true);
			
			customView.setOnClickListener(v -> {
				PlaylistManager.setSelectedPlaylistName(name);
				Intent intent = new Intent(customView.getContext(), PlaylistActivity.class);
				intent.putExtra("playlist_name", name);
				customView.getContext().startActivity(intent);
			});
			
			// ديالوج حذف القائمة الزجاجي (Liquid Glass Dialog)
			if (cardDeletePlaylist != null) {
				cardDeletePlaylist.setOnClickListener(v -> {
					android.content.Context context = customView.getContext();
					float density = context.getResources().getDisplayMetrics().density;
					android.util.TypedValue typedValue = new android.util.TypedValue();
					
					// حاوية FrameLayout خلفيتها bg_glass_card الزجاجية
					android.widget.FrameLayout cardView = new android.widget.FrameLayout(context);
					cardView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
					android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
					android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
					));
					cardView.setBackgroundResource(R.drawable.bg_glass_card);
					
					LinearLayout layout = new LinearLayout(context);
					layout.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
					android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
					android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
					));
					layout.setOrientation(LinearLayout.VERTICAL);
					int padding = (int)(24 * density);
					layout.setPadding(padding, padding, padding, padding);
					
					// العنوان
					TextView txtTitle = new TextView(context);
					txtTitle.setText("Delete Playlist");
					txtTitle.setTextSize(18);
					txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
					context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorError, typedValue, true);
					txtTitle.setTextColor(typedValue.data);
					layout.addView(txtTitle);
					
					// الرسالة النصية
					TextView txtMessage = new TextView(context);
					txtMessage.setText("Are you sure you want to delete \"" + name + "\"?");
					txtMessage.setTextSize(14);
					context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true);
					txtMessage.setTextColor(typedValue.data);
					
					LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, 
					LinearLayout.LayoutParams.WRAP_CONTENT
					);
					msgParams.setMargins(0, (int)(12 * density), 0, (int)(24 * density));
					txtMessage.setLayoutParams(msgParams);
					layout.addView(txtMessage);
					
					// حاوية الأزرار
					LinearLayout btnLayout = new LinearLayout(context);
					btnLayout.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
					));
					btnLayout.setOrientation(LinearLayout.HORIZONTAL);
					btnLayout.setGravity(android.view.Gravity.END);
					
					// زر الإلغاء الزجاجي الفرعي (Secondary Glass)
					android.widget.FrameLayout frameCancel = new android.widget.FrameLayout(context);
					LinearLayout.LayoutParams lpCancel = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					(int)(40 * density)
					);
					lpCancel.setMarginEnd((int)(10 * density));
					frameCancel.setLayoutParams(lpCancel);
					frameCancel.setBackgroundResource(R.drawable.bg_glass_button_secondary);
					frameCancel.setClickable(true);
					frameCancel.setFocusable(true);
					
					TextView btnCancel = new TextView(context);
					btnCancel.setText("Cancel");
					context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
					btnCancel.setTextColor(typedValue.data);
					btnCancel.setTextSize(13);
					btnCancel.setTypeface(null, android.graphics.Typeface.BOLD);
					btnCancel.setGravity(android.view.Gravity.CENTER);
					btnCancel.setPadding((int)(18 * density), 0, (int)(18 * density), 0);
					frameCancel.addView(btnCancel);
					
					// زر الحذف الأحمر الزجاجي (Error Glass)
					android.widget.FrameLayout frameDelete = new android.widget.FrameLayout(context);
					LinearLayout.LayoutParams lpDelete = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					(int)(40 * density)
					);
					frameDelete.setLayoutParams(lpDelete);
					frameDelete.setBackgroundResource(R.drawable.bg_glass_button_error);
					frameDelete.setClickable(true);
					frameDelete.setFocusable(true);
					
					TextView btnDelete = new TextView(context);
					btnDelete.setText("Delete");
					context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnError, typedValue, true);
					btnDelete.setTextColor(typedValue.data);
					btnDelete.setTextSize(13);
					btnDelete.setTypeface(null, android.graphics.Typeface.BOLD);
					btnDelete.setGravity(android.view.Gravity.CENTER);
					btnDelete.setPadding((int)(20 * density), 0, (int)(20 * density), 0);
					frameDelete.addView(btnDelete);
					
					btnLayout.addView(frameCancel);
					btnLayout.addView(frameDelete);
					layout.addView(btnLayout);
					
					cardView.addView(layout);
					
					com.google.android.material.dialog.MaterialAlertDialogBuilder builder = 
					new com.google.android.material.dialog.MaterialAlertDialogBuilder(context);
					builder.setView(cardView);
					
					final androidx.appcompat.app.AlertDialog deleteDialog = builder.create();
					if (deleteDialog.getWindow() != null) {
						deleteDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
					}
					
					frameCancel.setOnClickListener(v1 -> deleteDialog.dismiss());
					
					frameDelete.setOnClickListener(v1 -> {
						PlaylistManager.deletePlaylist(context, name);
						
						playlist.clear();
						playlist.addAll(PlaylistManager.getPlaylistsListMap(context));
						
						androidx.recyclerview.widget.RecyclerView localRv = customView.getRootView().findViewById(R.id.rvPlaylists);
						if (localRv != null && localRv.getAdapter() != null) {
							localRv.getAdapter().notifyDataSetChanged();
						}
						
						deleteDialog.dismiss();
					});
					
					deleteDialog.show();
					
					if (deleteDialog.getWindow() != null) {
						int dialogWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.86);
						deleteDialog.getWindow().setLayout(dialogWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
					}
				});
			}
			
			if (txtPlaylistName != null) {
				txtPlaylistName.setTypeface(Typeface.createFromAsset(customView.getContext().getAssets(), "fonts/bold.ttf"), 1);
			}
			if (txtSongCount != null) {
				txtSongCount.setTypeface(Typeface.createFromAsset(customView.getContext().getAssets(), "fonts/regular.ttf"), 0);
			}
			
			binding.txtPlaylistName.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/bold.ttf"), 1);
			binding.txtSongCount.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/regular.ttf"), 0);
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