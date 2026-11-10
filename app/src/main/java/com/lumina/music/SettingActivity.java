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

public class SettingActivity extends AppCompatActivity {
	
	private SettingBinding binding;
	
	private Intent intent = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = SettingBinding.inflate(getLayoutInflater());
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
		
		binding.btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				float density = getResources().getDisplayMetrics().density;
				android.util.TypedValue typedValue = new android.util.TypedValue();
				
				final java.io.File privacyCacheFile = new java.io.File(getCacheDir(), "privacy_cache.txt");
				
				final android.widget.TextView tvContent = new android.widget.TextView(SettingActivity.this);
				tvContent.setText("Loading privacy policy...");
				getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true);
				tvContent.setTextColor(typedValue.data);
				tvContent.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13.5f);
				
				if (privacyCacheFile.exists()) {
					try {
						java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(privacyCacheFile), "UTF-8"));
						StringBuilder sb = new StringBuilder();
						String line;
						while ((line = br.readLine()) != null) {
							sb.append(line).append("\n");
						}
						br.close();
						if (sb.length() > 0) {
							tvContent.setText(sb.toString().trim());
						}
					} catch (Exception ignored) {}
				}
				
				android.widget.FrameLayout cardContainer = new android.widget.FrameLayout(SettingActivity.this);
				cardContainer.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
				android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
				android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
				));
				cardContainer.setBackgroundResource(R.drawable.bg_glass_card);
				
				android.widget.LinearLayout linearLayout = new android.widget.LinearLayout(SettingActivity.this);
				linearLayout.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
				android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
				android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
				));
				linearLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
				int p24 = (int) (24 * density);
				linearLayout.setPadding(p24, p24, p24, p24);
				
				android.widget.TextView tvTitle = new android.widget.TextView(SettingActivity.this);
				tvTitle.setText("Privacy Policy");
				getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
				tvTitle.setTextColor(typedValue.data);
				tvTitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18.0f);
				tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
				linearLayout.addView(tvTitle);
				
				android.widget.ScrollView scrollView = new android.widget.ScrollView(SettingActivity.this);
				scrollView.setVerticalScrollBarEnabled(false);
				android.widget.LinearLayout.LayoutParams scrollParams = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
				(int) (300 * density)
				);
				int p16 = (int) (16 * density);
				scrollParams.setMargins(0, p16, 0, 0);
				scrollView.setLayoutParams(scrollParams);
				
				scrollView.addView(tvContent);
				linearLayout.addView(scrollView);
				
				android.widget.FrameLayout frameGotIt = new android.widget.FrameLayout(SettingActivity.this);
				android.widget.LinearLayout.LayoutParams btnParams = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
				(int) (44 * density)
				);
				btnParams.setMargins(0, p16, 0, 0);
				frameGotIt.setLayoutParams(btnParams);
				frameGotIt.setBackgroundResource(R.drawable.bg_glass_button_primary);
				frameGotIt.setClickable(true);
				frameGotIt.setFocusable(true);
				
				android.widget.TextView btnGotIt = new android.widget.TextView(SettingActivity.this);
				btnGotIt.setText("GOT IT!");
				getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
				btnGotIt.setTextColor(typedValue.data);
				btnGotIt.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14.0f);
				btnGotIt.setTypeface(null, android.graphics.Typeface.BOLD);
				btnGotIt.setGravity(android.view.Gravity.CENTER);
				
				frameGotIt.addView(btnGotIt);
				linearLayout.addView(frameGotIt);
				cardContainer.addView(linearLayout);
				
				com.google.android.material.dialog.MaterialAlertDialogBuilder builder = 
				new com.google.android.material.dialog.MaterialAlertDialogBuilder(SettingActivity.this);
				builder.setView(cardContainer);
				
				final androidx.appcompat.app.AlertDialog dialog = builder.create();
				if (dialog.getWindow() != null) {
					dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
				}
				
				frameGotIt.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(android.view.View v) {
						dialog.dismiss();
					}
				});
				
				dialog.show();
				
				if (dialog.getWindow() != null) {
					int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.86);
					dialog.getWindow().setLayout(dialogWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				}
				
				okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
				.cache(null)
				.build();
				
				okhttp3.Request request = new okhttp3.Request.Builder()
				.url("https://raw.githubusercontent.com/Hance-PDv/Lumina/refs/heads/main/Privacy.txt?" + System.currentTimeMillis())
				.header("Cache-Control", "no-cache, no-store, must-revalidate")
				.header("Pragma", "no-cache")
				.build();
				
				client.newCall(request).enqueue(new okhttp3.Callback() {
					@Override
					public void onFailure(okhttp3.Call call, java.io.IOException e) {}
					
					@Override
					public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
						if (response.isSuccessful() && response.body() != null) {
							String remoteText = response.body().string().trim();
							if (!remoteText.isEmpty()) {
								try {
									java.io.FileOutputStream fos = new java.io.FileOutputStream(privacyCacheFile);
									fos.write(remoteText.getBytes("UTF-8"));
									fos.close();
								} catch (Exception ignored) {}
								
								new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										if (tvContent != null) {
											tvContent.setText(remoteText);
										}
									}
								});
							}
						}
						response.close();
					}
				});
				
			}
		});
		
		binding.btnContact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("https://t.me/X_hance_x"));
				startActivity(intent);
			}
		});
		
		binding.linear2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							java.net.URL url = new java.net.URL("https://raw.githubusercontent.com/Hance-PDv/Lumina/refs/heads/main/redirect.txt");
							java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
							conn.setRequestMethod("GET");
							conn.setConnectTimeout(5000);
							conn.setReadTimeout(5000);
							
							if (conn.getResponseCode() == 200) {
								java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
								final String targetUrl = reader.readLine().trim();
								reader.close();
								
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (!targetUrl.isEmpty() && targetUrl.startsWith("http")) {
											Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
											startActivity(i);
										}
									}
								});
							}
							conn.disconnect();
						} catch (Exception e) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									SketchwareUtil.showMessage(getApplicationContext(), "Check your internet connection");
								}
							});
						}
					}
				}).start();
				
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
		
		binding.txtTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.textview17.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.textview18.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtCheckUpdates.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtSlowedSong.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		com.google.android.material.materialswitch.MaterialSwitch switchSlowed = findViewById(R.id.switchSlowedSong);
		
		android.content.SharedPreferences prefs = getSharedPreferences("mflow_prefs", MODE_PRIVATE);
		
		switchSlowed.setChecked(prefs.getBoolean("key_slowed_song", false));
		
		switchSlowed.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefs.edit().putBoolean("key_slowed_song", isChecked).apply();
			
			android.content.Intent intent = new android.content.Intent(SettingActivity.this, MusicService.class);
			intent.setAction(MusicService.ACTION_UPDATE_AUDIO_EFFECTS);
			startService(intent);
		});
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
	}
}