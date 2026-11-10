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
import java.util.regex.*;
import org.json.*;

public class StatsActivity extends AppCompatActivity {
	
	private StatsBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = StatsBinding.inflate(getLayoutInflater());
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
		
		binding.linear15.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				
			}
		});
	}
	
	private void initializeLogic() {
		final com.lumina.music.StatsManager statsManager = new com.lumina.music.StatsManager(this);
		final android.view.View decorView = getWindow().getDecorView();
		
		final android.widget.FrameLayout cardBtnBack = decorView.findViewById(R.id.cardBtnBack);
		final android.widget.ImageView btnBack = decorView.findViewById(R.id.btnBack);
		
		final android.widget.TextView txtTotalTime = decorView.findViewById(R.id.txtTotalTime);
		final android.widget.TextView txtTopTrackDetail = decorView.findViewById(R.id.txtTopTrackDetail);
		final android.widget.TextView txtTotalPlaysDetail = decorView.findViewById(R.id.txtTotalPlaysDetail);
		final android.widget.TextView txtExploredDetail = decorView.findViewById(R.id.txtExploredDetail);
		
		final android.widget.FrameLayout cardResetStats = decorView.findViewById(R.id.cardResetStats);
		
		final android.widget.TextView txtDailyAvg = decorView.findViewById(R.id.txtDailyAvg);
		final android.widget.TextView txtWeeklyAvg = decorView.findViewById(R.id.txtWeeklyAvg);
		
		final android.widget.LinearLayout cardTopTrack1 = decorView.findViewById(R.id.cardTopTrack1);
		final android.widget.LinearLayout cardTopTrack2 = decorView.findViewById(R.id.cardTopTrack2);
		final android.widget.LinearLayout cardTopTrack3 = decorView.findViewById(R.id.cardTopTrack3);
		
		final android.widget.ImageView imgTopTrackArt1 = decorView.findViewById(R.id.imgTopTrackArt1);
		final android.widget.TextView txtTopTrackTitle1 = decorView.findViewById(R.id.txtTopTrackTitle1);
		final android.widget.TextView txtTopTrackArtist1 = decorView.findViewById(R.id.txtTopTrackArtist1);
		final android.widget.TextView txtTopTrackPlays1 = decorView.findViewById(R.id.txtTopTrackPlays1);
		
		final android.widget.ImageView imgTopTrackArt2 = decorView.findViewById(R.id.imgTopTrackArt2);
		final android.widget.TextView txtTopTrackTitle2 = decorView.findViewById(R.id.txtTopTrackTitle2);
		final android.widget.TextView txtTopTrackArtist2 = decorView.findViewById(R.id.txtTopTrackArtist2);
		final android.widget.TextView txtTopTrackPlays2 = decorView.findViewById(R.id.txtTopTrackPlays2);
		
		final android.widget.ImageView imgTopTrackArt3 = decorView.findViewById(R.id.imgTopTrackArt3);
		final android.widget.TextView txtTopTrackTitle3 = decorView.findViewById(R.id.txtTopTrackTitle3);
		final android.widget.TextView txtTopTrackArtist3 = decorView.findViewById(R.id.txtTopTrackArtist3);
		final android.widget.TextView txtTopTrackPlays3 = decorView.findViewById(R.id.txtTopTrackPlays3);
		
		final Runnable refreshUI = new Runnable() {
			@Override
			public void run() {
				if (txtTotalTime != null) txtTotalTime.setText(statsManager.getFormattedListenedTime());
				
				String topTrack = statsManager.getTopTrackName();
				if (txtTopTrackDetail != null) {
					txtTopTrackDetail.setText((topTrack == null || topTrack.equalsIgnoreCase("None") || topTrack.trim().isEmpty()) ? "No tracks played yet" : topTrack);
				}
				
				if (txtTotalPlaysDetail != null) txtTotalPlaysDetail.setText(String.valueOf(statsManager.getTotalPlays()));
				if (txtExploredDetail != null) txtExploredDetail.setText(String.valueOf(statsManager.getExploredCount()));
				
				long totalMs = statsManager.getTotalListenedMs();
				long totalMinutes = totalMs / 60000;
				
				if (txtDailyAvg != null) txtDailyAvg.setText(totalMinutes + "m / day");
				
				double totalHours = totalMinutes / 60.0;
				if (txtWeeklyAvg != null) txtWeeklyAvg.setText(String.format(java.util.Locale.US, "%.1fh / week", totalHours));
				
				java.util.ArrayList<java.util.HashMap<String, Object>> top3 = statsManager.getTopTracks(3);
				
				if (cardTopTrack1 != null) cardTopTrack1.setVisibility(android.view.View.GONE);
				if (cardTopTrack2 != null) cardTopTrack2.setVisibility(android.view.View.GONE);
				if (cardTopTrack3 != null) cardTopTrack3.setVisibility(android.view.View.GONE);
				
				if (top3 != null) {
					if (top3.size() >= 1 && cardTopTrack1 != null) {
						java.util.HashMap<String, Object> song = top3.get(0);
						if (txtTopTrackTitle1 != null) txtTopTrackTitle1.setText(String.valueOf(song.get("title")));
						if (txtTopTrackArtist1 != null) txtTopTrackArtist1.setText(String.valueOf(song.get("artist")));
						if (txtTopTrackPlays1 != null) txtTopTrackPlays1.setText(String.valueOf(song.get("plays")));
						setAlbumArt(song, imgTopTrackArt1);
						cardTopTrack1.setVisibility(android.view.View.VISIBLE);
					}
					if (top3.size() >= 2 && cardTopTrack2 != null) {
						java.util.HashMap<String, Object> song = top3.get(1);
						if (txtTopTrackTitle2 != null) txtTopTrackTitle2.setText(String.valueOf(song.get("title")));
						if (txtTopTrackArtist2 != null) txtTopTrackArtist2.setText(String.valueOf(song.get("artist")));
						if (txtTopTrackPlays2 != null) txtTopTrackPlays2.setText(String.valueOf(song.get("plays")));
						setAlbumArt(song, imgTopTrackArt2);
						cardTopTrack2.setVisibility(android.view.View.VISIBLE);
					}
					if (top3.size() >= 3 && cardTopTrack3 != null) {
						java.util.HashMap<String, Object> song = top3.get(2);
						if (txtTopTrackTitle3 != null) txtTopTrackTitle3.setText(String.valueOf(song.get("title")));
						if (txtTopTrackArtist3 != null) txtTopTrackArtist3.setText(String.valueOf(song.get("artist")));
						if (txtTopTrackPlays3 != null) txtTopTrackPlays3.setText(String.valueOf(song.get("plays")));
						setAlbumArt(song, imgTopTrackArt3);
						cardTopTrack3.setVisibility(android.view.View.VISIBLE);
					}
				}
			}
			
			private void setAlbumArt(java.util.HashMap<String, Object> song, android.widget.ImageView imageView) {
				if (song == null || imageView == null) return;
				
				long albumId = -1;
				if (song.get("album_id") != null) {
					try {
						albumId = Long.parseLong(String.valueOf(song.get("album_id")));
					} catch (Exception ignored) {}
				}
				
				com.lumina.music.ImageLoader.loadAlbumArt(
				imageView.getContext(),
				albumId,
				imageView,
				R.drawable.default_music_image
				);
			}
		};
		
		refreshUI.run();
		
		final android.content.BroadcastReceiver statsReceiver = new android.content.BroadcastReceiver() {
			@Override
			public void onReceive(android.content.Context context, android.content.Intent intent) {
				refreshUI.run();
			}
		};
		
		android.content.IntentFilter filter = new android.content.IntentFilter("MFLOW_UPDATE_PLAYER_UI");
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(statsReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
		} else {
			registerReceiver(statsReceiver, filter);
		}
		
		decorView.addOnAttachStateChangeListener(new android.view.View.OnAttachStateChangeListener() {
			@Override
			public void onViewAttachedToWindow(android.view.View v) {}
			@Override
			public void onViewDetachedFromWindow(android.view.View v) {
				try {
					unregisterReceiver(statsReceiver);
				} catch (Exception ignored) {}
			}
		});
		
		if (cardBtnBack != null) {
			cardBtnBack.setOnClickListener(v -> finish());
		} else if (btnBack != null) {
			btnBack.setOnClickListener(v -> finish());
		}
		
		if (cardResetStats != null) {
			cardResetStats.setOnClickListener(v -> {
				float density = v.getContext().getResources().getDisplayMetrics().density;
				android.util.TypedValue typedValue = new android.util.TypedValue();
				
				android.widget.FrameLayout rootCard = new android.widget.FrameLayout(v.getContext());
				rootCard.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
				android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
				android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
				));
				rootCard.setBackgroundResource(R.drawable.bg_glass_card);
				
				android.widget.LinearLayout root = new android.widget.LinearLayout(v.getContext());
				root.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
				android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
				android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
				));
				root.setOrientation(android.widget.LinearLayout.VERTICAL);
				root.setPadding(
				(int)(24 * density),
				(int)(24 * density),
				(int)(24 * density),
				(int)(20 * density)
				);
				
				android.widget.TextView title = new android.widget.TextView(v.getContext());
				title.setText("Reset All Stats?");
				v.getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorError, typedValue, true);
				title.setTextColor(typedValue.data);
				title.setTextSize(18);
				title.setTypeface(null, android.graphics.Typeface.BOLD);
				root.addView(title);
				
				android.widget.TextView message = new android.widget.TextView(v.getContext());
				message.setText("This will permanently delete all your listening history, plays, and top tracks.");
				v.getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true);
				message.setTextColor(typedValue.data);
				message.setTextSize(14);
				message.setPadding(0, (int)(12 * density), 0, (int)(24 * density));
				root.addView(message);
				
				android.widget.LinearLayout btnContainer = new android.widget.LinearLayout(v.getContext());
				btnContainer.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
				));
				btnContainer.setOrientation(android.widget.LinearLayout.HORIZONTAL);
				btnContainer.setGravity(android.view.Gravity.END);
				
				android.widget.FrameLayout frameCancel = new android.widget.FrameLayout(v.getContext());
				android.widget.LinearLayout.LayoutParams lpCancel = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
				(int)(40 * density)
				);
				lpCancel.setMarginEnd((int)(10 * density));
				frameCancel.setLayoutParams(lpCancel);
				frameCancel.setBackgroundResource(R.drawable.bg_glass_button_secondary);
				frameCancel.setClickable(true);
				frameCancel.setFocusable(true);
				
				android.widget.TextView btnCancel = new android.widget.TextView(v.getContext());
				btnCancel.setText("Cancel");
				v.getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
				btnCancel.setTextColor(typedValue.data);
				btnCancel.setTextSize(13);
				btnCancel.setTypeface(null, android.graphics.Typeface.BOLD);
				btnCancel.setGravity(android.view.Gravity.CENTER);
				btnCancel.setPadding((int)(18 * density), 0, (int)(18 * density), 0);
				frameCancel.addView(btnCancel);
				
				android.widget.FrameLayout frameReset = new android.widget.FrameLayout(v.getContext());
				android.widget.LinearLayout.LayoutParams lpReset = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
				(int)(40 * density)
				);
				frameReset.setLayoutParams(lpReset);
				frameReset.setBackgroundResource(R.drawable.bg_glass_button_error);
				frameReset.setClickable(true);
				frameReset.setFocusable(true);
				
				android.widget.TextView btnReset = new android.widget.TextView(v.getContext());
				btnReset.setText("Reset");
				v.getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnError, typedValue, true);
				btnReset.setTextColor(typedValue.data);
				btnReset.setTextSize(13);
				btnReset.setTypeface(null, android.graphics.Typeface.BOLD);
				btnReset.setGravity(android.view.Gravity.CENTER);
				btnReset.setPadding((int)(20 * density), 0, (int)(20 * density), 0);
				frameReset.addView(btnReset);
				
				btnContainer.addView(frameCancel);
				btnContainer.addView(frameReset);
				root.addView(btnContainer);
				
				rootCard.addView(root);
				
				com.google.android.material.dialog.MaterialAlertDialogBuilder builder = 
				new com.google.android.material.dialog.MaterialAlertDialogBuilder(v.getContext());
				builder.setView(rootCard);
				
				final androidx.appcompat.app.AlertDialog dialog = builder.create();
				if (dialog.getWindow() != null) {
					dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
				}
				
				frameCancel.setOnClickListener(view -> dialog.dismiss());
				
				frameReset.setOnClickListener(view -> {
					statsManager.resetStats();
					refreshUI.run();
					dialog.dismiss();
				});
				
				dialog.show();
				
				if (dialog.getWindow() != null) {
					int dialogWidth = (int) (v.getContext().getResources().getDisplayMetrics().widthPixels * 0.86);
					dialog.getWindow().setLayout(dialogWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				}
			});
		}
		
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
		
		binding.txtTopTrackTitle1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtTopTrackArtist1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.txtTopTrackTitle2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtTopTrackArtist2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.txtTopTrackTitle3.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtTopTrackArtist3.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.txtHeaderTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtTotalTime.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtTopTrackDetail.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtTotalPlaysDetail.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.txtExploredDetail.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.txtDailyAvg.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.txtWeeklyAvg.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.vscroll2.setVerticalFadingEdgeEnabled(true);
		binding.vscroll2.setFadingEdgeLength(70);
		
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
	}
}