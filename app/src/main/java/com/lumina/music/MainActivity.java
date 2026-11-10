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
import androidx.vectordrawable.*;
import androidx.vectordrawable.animated.*;
import com.bumptech.glide.*;
import com.bumptech.glide.gifdecoder.*;
import com.lumina.music.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {
	
	private Timer _timer = new Timer();
	
	private MainBinding binding;
	
	private Intent intent = new Intent();
	private TimerTask timer;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = MainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		final android.view.View splashContainer = findViewById(R.id.splash_content_container);
		final android.view.View maintenanceContainer = findViewById(R.id.maintenance_container);
		
		final android.view.View maintIconWrapper = findViewById(R.id.maint_icon_wrapper);
		final android.widget.TextView txtMaintTitle = findViewById(R.id.txt_maint_title);
		final android.widget.TextView txtMaintMessage = findViewById(R.id.txt_maint_message);
		final android.view.View maintBottomContainer = findViewById(R.id.maint_bottom_container);
		final android.view.View btnMaintUpdate = findViewById(R.id.btn_maint_update);
		final android.widget.TextView btnMaintSkip = findViewById(R.id.btn_maint_skip);
		final android.view.View containerDontRemind = findViewById(R.id.container_dont_remind);
		final android.widget.CheckBox chkDontRemind = findViewById(R.id.chk_dont_remind);
		
		final android.view.View txtM = findViewById(R.id.txt_m);
		final android.view.View fullNameContainer = findViewById(R.id.full_name_container);
		final android.widget.TextView txtSubtitle = findViewById(R.id.txt_subtitle);
		final android.view.View bottomContainer = findViewById(R.id.bottom_container);
		
		if (containerDontRemind != null && chkDontRemind != null) {
			containerDontRemind.setOnClickListener(v -> chkDontRemind.setChecked(!chkDontRemind.isChecked()));
		}
		
		final long startTime = System.currentTimeMillis();
		final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
		final java.util.concurrent.atomic.AtomicBoolean isProceeded = new java.util.concurrent.atomic.AtomicBoolean(false);
		
		final Runnable proceedToAppRunnable = new Runnable() {
			@Override
			public void run() {
				if (!isProceeded.compareAndSet(false, true)) return;
				
				long elapsedTime = System.currentTimeMillis() - startTime;
				long remainingTime = Math.max(0, 2600 - elapsedTime);
				
				mainHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed())) return;
						android.content.Intent intent = new android.content.Intent();
						intent.setClass(getApplicationContext(), HomeworkActivity.class);
						intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						finish();
					}
				}, remainingTime);
			}
		};
		
		final java.util.Comparator<String> versionComparator = (v1, v2) -> {
			if (v1 == null || v2 == null) return 0;
			String[] parts1 = v1.replaceAll("[^0-9.]", "").split("\\.");
			String[] parts2 = v2.replaceAll("[^0-9.]", "").split("\\.");
			int length = Math.max(parts1.length, parts2.length);
			for (int i = 0; i < length; i++) {
				int p1 = i < parts1.length && !parts1[i].isEmpty() ? Integer.parseInt(parts1[i]) : 0;
				int p2 = i < parts2.length && !parts2[i].isEmpty() ? Integer.parseInt(parts2[i]) : 0;
				if (p1 < p2) return -1;
				if (p1 > p2) return 1;
			}
			return 0;
		};
		
		if (txtM != null) {
			txtM.setAlpha(0f);
			txtM.setScaleX(5.0f);
			txtM.setScaleY(5.0f);
		}
		if (fullNameContainer != null) {
			fullNameContainer.setVisibility(android.view.View.GONE);
			fullNameContainer.setAlpha(0f);
		}
		if (txtSubtitle != null) {
			txtSubtitle.setAlpha(0f);
			txtSubtitle.setTranslationY(24f);
		}
		if (bottomContainer != null) {
			bottomContainer.setAlpha(0f);
			bottomContainer.setTranslationY(40f);
		}
		
		if (txtM != null) {
			txtM.post(() -> {
				txtM.setPivotX(txtM.getWidth() / 2f);
				txtM.setPivotY(txtM.getHeight() / 2f);
				
				txtM.animate()
				.alpha(1f)
				.scaleX(1.0f)
				.scaleY(1.0f)
				.setDuration(1200)
				.setInterpolator(new android.view.animation.DecelerateInterpolator(2.5f))
				.withEndAction(() -> {
					txtM.setVisibility(android.view.View.GONE);
					
					if (fullNameContainer != null) {
						fullNameContainer.setVisibility(android.view.View.VISIBLE);
						fullNameContainer.setScaleX(0.88f);
						fullNameContainer.setScaleY(0.88f);
						
						fullNameContainer.animate()
						.alpha(1f)
						.scaleX(1.0f)
						.scaleY(1.0f)
						.setDuration(550)
						.setInterpolator(new android.view.animation.OvershootInterpolator(1.1f))
						.start();
					}
					
					if (txtSubtitle != null) {
						txtSubtitle.animate()
						.alpha(1f)
						.translationY(0f)
						.setDuration(750)
						.setStartDelay(180)
						.setInterpolator(new android.view.animation.DecelerateInterpolator(1.8f))
						.start();
					}
					
					if (bottomContainer != null) {
						bottomContainer.animate()
						.alpha(1f)
						.translationY(0f)
						.setDuration(850)
						.setStartDelay(350)
						.setInterpolator(new android.view.animation.DecelerateInterpolator(1.8f))
						.start();
					}
				})
				.start();
			});
		}
		
		okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
		.connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
		.readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
		.cache(null)
		.build();
		
		String configUrl = "https://raw.githubusercontent.com/Hance-PDv/Lumina/main/config.json?t=" + System.currentTimeMillis();
		
		okhttp3.Request request = new okhttp3.Request.Builder()
		.url(configUrl)
		.header("User-Agent", "Lumina-AndroidApp")
		.header("Cache-Control", "no-cache, no-store, must-revalidate")
		.header("Pragma", "no-cache")
		.build();
		
		client.newCall(request).enqueue(new okhttp3.Callback() {
			@Override
			public void onFailure(okhttp3.Call call, java.io.IOException e) {
				proceedToAppRunnable.run();
			}
			
			@Override
			public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
				if (response.isSuccessful() && response.body() != null) {
					try {
						String jsonString = response.body().string().trim();
						org.json.JSONObject json = new org.json.JSONObject(jsonString);
						
						final String minVersionName = json.optString("min_version_name", "0.0");
						final boolean isForceUpdate = json.optBoolean("is_force_update", false);
						final String title = json.optString("title", "New Update Available");
						final String message = json.optString("message", "A new version of Lumina is available.");
						final String updateUrl = json.optString("update_url", "https://github.com/Hance-PDv/Lumina");
						
						String currentVersionName = "0.0";
						try {
							android.content.pm.PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
							currentVersionName = pInfo.versionName;
						} catch (Exception ignored) {}
						
						boolean isOutdated = versionComparator.compare(currentVersionName, minVersionName) < 0;
						
						android.content.SharedPreferences updatePrefs = getSharedPreferences("lumina_prefs", MODE_PRIVATE);
						String ignoredVersion = updatePrefs.getString("key_ignored_update_version", "");
						boolean isIgnoredByPrefs = !isForceUpdate && minVersionName.equals(ignoredVersion);
						
						if (isOutdated && !isIgnoredByPrefs) {
							mainHandler.post(() -> {
								if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed())) return;
								
								if (splashContainer != null) {
									splashContainer.animate()
									.alpha(0f)
									.setDuration(300)
									.withEndAction(() -> splashContainer.setVisibility(android.view.View.GONE))
									.start();
								}
								
								if (maintenanceContainer != null) {
									maintenanceContainer.setVisibility(android.view.View.VISIBLE);
									maintenanceContainer.setAlpha(1f);
									
									if (txtMaintTitle != null) txtMaintTitle.setText(title);
									if (txtMaintMessage != null) txtMaintMessage.setText(message);
									
									if (isForceUpdate) {
										if (btnMaintSkip != null) btnMaintSkip.setVisibility(android.view.View.GONE);
										if (containerDontRemind != null) containerDontRemind.setVisibility(android.view.View.GONE);
									} else {
										if (containerDontRemind != null) containerDontRemind.setVisibility(android.view.View.VISIBLE);
										if (chkDontRemind != null) chkDontRemind.setChecked(false);
										
										if (btnMaintSkip != null) {
											btnMaintSkip.setVisibility(android.view.View.VISIBLE);
											btnMaintSkip.setOnClickListener(v -> {
												if (chkDontRemind != null && chkDontRemind.isChecked()) {
													getSharedPreferences("lumina_prefs", MODE_PRIVATE)
													.edit()
													.putString("key_ignored_update_version", minVersionName)
													.apply();
												}
												
												if (!isProceeded.get()) {
													isProceeded.set(true);
													android.content.Intent intent = new android.content.Intent();
													intent.setClass(getApplicationContext(), HomeworkActivity.class);
													intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
													startActivity(intent);
													finish();
												}
											});
										}
									}
									
									if (maintIconWrapper != null) {
										maintIconWrapper.setAlpha(0f);
										maintIconWrapper.setScaleX(0.4f);
										maintIconWrapper.setScaleY(0.4f);
										maintIconWrapper.animate()
										.alpha(1f)
										.scaleX(1.0f)
										.scaleY(1.0f)
										.setDuration(600)
										.setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
										.start();
									}
									
									if (txtMaintTitle != null) {
										txtMaintTitle.setAlpha(0f);
										txtMaintTitle.setTranslationY(30f);
										txtMaintTitle.animate()
										.alpha(1f)
										.translationY(0f)
										.setDuration(500)
										.setStartDelay(150)
										.setInterpolator(new android.view.animation.DecelerateInterpolator(2.0f))
										.start();
									}
									
									if (txtMaintMessage != null) {
										txtMaintMessage.setAlpha(0f);
										txtMaintMessage.setTranslationY(30f);
										txtMaintMessage.animate()
										.alpha(1f)
										.translationY(0f)
										.setDuration(500)
										.setStartDelay(250)
										.setInterpolator(new android.view.animation.DecelerateInterpolator(2.0f))
										.start();
									}
									
									if (maintBottomContainer != null) {
										maintBottomContainer.setAlpha(0f);
										maintBottomContainer.setTranslationY(40f);
										maintBottomContainer.animate()
										.alpha(1f)
										.translationY(0f)
										.setDuration(550)
										.setStartDelay(350)
										.setInterpolator(new android.view.animation.DecelerateInterpolator(2.0f))
										.start();
									}
									
									if (btnMaintUpdate != null) {
										btnMaintUpdate.setOnClickListener(v -> {
											try {
												android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(updateUrl));
												startActivity(browserIntent);
											} catch (Exception ignored) {}
										});
									}
								}
							});
							response.close();
							return;
						}
					} catch (Exception ignored) {}
				}
				response.close();
				proceedToAppRunnable.run();
			}
		});
		
		binding.textview1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
		binding.txtFlow.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 1);
		binding.txtFlow.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bold.ttf"), 0);
		binding.txtVersion.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/regular.ttf"), 0);
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
		
	}
	
}