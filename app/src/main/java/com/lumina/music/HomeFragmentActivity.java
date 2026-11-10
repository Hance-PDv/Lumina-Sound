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
import java.util.regex.*;
import org.json.*;

public class HomeFragmentActivity extends Fragment {
	
	private HomeFragmentBinding binding;
	private double old_size = 0;
	
	private Intent intent = new Intent();
	
	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater _inflater, @Nullable ViewGroup _container, @Nullable Bundle _savedInstanceState) {
		binding = HomeFragmentBinding.inflate(_inflater, _container, false);
		initialize(_savedInstanceState, binding.getRoot());
		initializeLogic();
		return binding.getRoot();
	}
	
	private void initialize(Bundle _savedInstanceState, View _view) {
		binding.scrollViewPage.setVerticalFadingEdgeEnabled(true);
		binding.scrollViewPage.setFadingEdgeLength(70);
		
		binding.tvFlowHeaderTitle.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/bold.ttf"), 1);
		binding.tvFavsSubtitle.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/regular.ttf"), 1);
		binding.tvArtistsSubtitle.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/regular.ttf"), 1);
		binding.tvBentoTitle.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/regular.ttf"), 1);
		binding.textview11.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/bold.ttf"), 1);
		binding.textview12.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/bold.ttf"), 1);
		binding.tvTrackCount.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/regular.ttf"), 1);
	}
	
	private void initializeLogic() {
	}
	
	@Override
	public void onResume() {
		super.onResume();
		android.view.View root = getView();
		if (root != null) {
			final android.view.View cardBentoHero = root.findViewById(R.id.cardBentoHero);
			final android.view.View cardBentoFavs = root.findViewById(R.id.cardBentoFavs);
			final android.view.View cardBentoArtists = root.findViewById(R.id.cardBentoArtists);
			final android.widget.TextView tvTrackCount = root.findViewById(R.id.tvTrackCount);
			final android.widget.ImageView imgBentoCover = root.findViewById(R.id.imgBentoCover);
			final android.view.View btnBentoPlay = root.findViewById(R.id.btnBentoPlay);
			
			final android.view.View btnFilterA = root.findViewById(R.id.btnFilterA);
			final android.view.View btnFilterB = root.findViewById(R.id.btnFilterB);
			final android.view.View btnFilterC = root.findViewById(R.id.btnFilterC);
			
			final androidx.recyclerview.widget.RecyclerView rvMainFlow = root.findViewById(R.id.rvMainFlow);
			
			if (rvMainFlow != null) {
				rvMainFlow.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
				rvMainFlow.setNestedScrollingEnabled(false);
			}
			
			final java.util.ArrayList<java.util.HashMap<String, Object>> allSongs = new java.util.ArrayList<>();
			final java.util.ArrayList<java.util.HashMap<String, Object>> displayList = new java.util.ArrayList<>();
			
			class MainFlowAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<MainFlowAdapter.ViewHolder> {
				private final java.util.ArrayList<java.util.HashMap<String, Object>> _data;
				
				public MainFlowAdapter(java.util.ArrayList<java.util.HashMap<String, Object>> _arr) {
					this._data = _arr != null ? _arr : new java.util.ArrayList<>();
				}
				
				class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
					com.lumina.music.SongViewHolder holder;
					
					ViewHolder(android.view.View v) {
						super(v);
						holder = new com.lumina.music.SongViewHolder(v);
					}
				}
				
				@Override
				public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
					android.view.View v = android.view.LayoutInflater.from(parent.getContext())
					.inflate(R.layout.music, parent, false);
					return new ViewHolder(v);
				}
				
				@Override
				public void onBindViewHolder(ViewHolder vh, int position) {
					final int pos = position;
					java.util.HashMap<String, Object> map = (_data != null && pos < _data.size()) ? _data.get(pos) : null;
					if (map == null) return;
					
					if (vh.holder.title != null) {
						vh.holder.title.setText(map.get("title") != null ? String.valueOf(map.get("title")) : "Unknown Title");
					}
					if (vh.holder.art != null) {
						vh.holder.art.setText(map.get("artist") != null ? String.valueOf(map.get("artist")) : "Unknown Artist");
					}
					
					if (vh.holder.img_music != null) {
						String artworkUrl = "";
						if (map.get("artwork_url") != null) {
							artworkUrl = String.valueOf(map.get("artwork_url")).trim();
						} else if (map.get("artwork") != null) {
							artworkUrl = String.valueOf(map.get("artwork")).trim();
						}
						
						long albumId = -1;
						if (map.get("album_id") != null) {
							try {
								albumId = Long.parseLong(String.valueOf(map.get("album_id")));
							} catch (Exception ignored) {}
						}
						
						android.content.Context context = vh.itemView.getContext();
						if (!artworkUrl.isEmpty() && (artworkUrl.startsWith("http://") || artworkUrl.startsWith("https://"))) {
							com.lumina.music.ImageLoader.loadOnlineAlbumArt(
							context,
							artworkUrl,
							vh.holder.img_music,
							R.drawable.default_music_image
							);
						} else {
							com.lumina.music.ImageLoader.loadAlbumArt(
							context,
							albumId,
							vh.holder.img_music,
							R.drawable.default_music_image
							);
						}
					}
					
					if (vh.holder.linearm != null) {
						android.util.TypedValue typedValue = new android.util.TypedValue();
						vh.itemView.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
						vh.holder.linearm.setBackgroundResource(typedValue.resourceId);
					}
					
					android.view.View clickTarget = vh.holder.linearm != null ? vh.holder.linearm : vh.itemView;
					clickTarget.setOnClickListener(new android.view.View.OnClickListener() {
						@Override
						public void onClick(android.view.View v) {
							android.content.Context context = v.getContext();
							if (context != null && _data != null && pos < _data.size()) {
								com.lumina.music.MusicService.songList = new java.util.ArrayList<>(_data);
								com.lumina.music.MusicService.currentIndex = pos;
								
								android.content.Intent playIntent = new android.content.Intent(context, com.lumina.music.MusicService.class);
								playIntent.setAction("ACTION_PLAY");
								playIntent.putExtra("index", pos);
								
								String mediaPath = "";
								if (_data.get(pos).containsKey("stream_url") && _data.get(pos).get("stream_url") != null) {
									mediaPath = String.valueOf(_data.get(pos).get("stream_url"));
								} else if (_data.get(pos).containsKey("path") && _data.get(pos).get("path") != null) {
									mediaPath = String.valueOf(_data.get(pos).get("path"));
								} else if (_data.get(pos).containsKey("audio") && _data.get(pos).get("audio") != null) {
									mediaPath = String.valueOf(_data.get(pos).get("audio"));
								}
								playIntent.putExtra("path", mediaPath);
								
								if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
									context.startForegroundService(playIntent);
								} else {
									context.startService(playIntent);
								}
							}
						}
					});
				}
				
				@Override
				public int getItemCount() {
					return _data != null ? _data.size() : 0;
				}
			}
			
			final MainFlowAdapter adapter = new MainFlowAdapter(displayList);
			if (rvMainFlow != null) {
				rvMainFlow.setAdapter(adapter);
			}
			
			final android.content.SharedPreferences prefs = getContext().getSharedPreferences("MFLOW_PREFS", android.content.Context.MODE_PRIVATE);
			
			final Runnable applyFilterLogic = new Runnable() {
				@Override
				public void run() {
					displayList.clear();
					displayList.addAll(allSongs);
					
					int mode = prefs.getInt("KEY_ACTIVE_FILTER", 1);
					if (mode == 1) {
						java.util.Collections.sort(displayList, new java.util.Comparator<java.util.HashMap<String, Object>>() {
							@Override
							public int compare(java.util.HashMap<String, Object> o1, java.util.HashMap<String, Object> o2) {
								String t1 = o1.get("title") != null ? String.valueOf(o1.get("title")) : "";
								String t2 = o2.get("title") != null ? String.valueOf(o2.get("title")) : "";
								return t1.compareToIgnoreCase(t2);
							}
						});
						if (btnFilterA != null) btnFilterA.setBackgroundResource(R.drawable.bg_glass_button_primary);
						if (btnFilterB != null) btnFilterB.setBackgroundResource(R.drawable.bg_glass_card);
						if (btnFilterC != null) btnFilterC.setBackgroundResource(R.drawable.bg_glass_card);
					} else if (mode == 2) {
						java.util.Collections.sort(displayList, new java.util.Comparator<java.util.HashMap<String, Object>>() {
							@Override
							public int compare(java.util.HashMap<String, Object> o1, java.util.HashMap<String, Object> o2) {
								String a1 = o1.get("artist") != null ? String.valueOf(o1.get("artist")) : "";
								String a2 = o2.get("artist") != null ? String.valueOf(o2.get("artist")) : "";
								return a1.compareToIgnoreCase(a2);
							}
						});
						if (btnFilterA != null) btnFilterA.setBackgroundResource(R.drawable.bg_glass_card);
						if (btnFilterB != null) btnFilterB.setBackgroundResource(R.drawable.bg_glass_button_primary);
						if (btnFilterC != null) btnFilterC.setBackgroundResource(R.drawable.bg_glass_card);
					} else if (mode == 3) {
						java.util.Collections.shuffle(displayList);
						if (btnFilterA != null) btnFilterA.setBackgroundResource(R.drawable.bg_glass_card);
						if (btnFilterB != null) btnFilterB.setBackgroundResource(R.drawable.bg_glass_card);
						if (btnFilterC != null) btnFilterC.setBackgroundResource(R.drawable.bg_glass_button_primary);
					}
					
					if (rvMainFlow != null) {
						rvMainFlow.setAdapter(new MainFlowAdapter(displayList));
					}
				}
			};
			
			final Runnable updateBentoHeroArtwork = new Runnable() {
				@Override
				public void run() {
					if (!isAdded() || getActivity() == null || getActivity().isFinishing()) return;
					
					int index = com.lumina.music.MusicService.currentIndex;
					java.util.ArrayList<java.util.HashMap<String, Object>> list = com.lumina.music.MusicService.songList;
					
					if (list != null && index >= 0 && index < list.size()) {
						java.util.HashMap<String, Object> song = list.get(index);
						
						if (imgBentoCover != null) {
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
							
							if (!artworkUrl.isEmpty() && (artworkUrl.startsWith("http://") || artworkUrl.startsWith("https://"))) {
								com.lumina.music.ImageLoader.loadOnlineAlbumArt(
								getContext(),
								artworkUrl,
								imgBentoCover,
								R.drawable.default_music_image
								);
							} else {
								com.lumina.music.ImageLoader.loadAlbumArt(
								getContext(),
								albumId,
								imgBentoCover,
								R.drawable.default_music_image
								);
							}
						}
					}
				}
			};
			
			Runnable loadSongsRunnable = new Runnable() {
				@Override
				public void run() {
					android.content.Context context = getContext();
					java.util.ArrayList<java.util.HashMap<String, Object>> localSongs = null;
					if (context != null) {
						localSongs = com.lumina.music.MusicCache.getSongs(context);
					}
					
					allSongs.clear();
					if (localSongs != null && !localSongs.isEmpty()) {
						allSongs.addAll(localSongs);
					}
					
					if (tvTrackCount != null) {
						tvTrackCount.setText(allSongs.size() + " Tracks");
					}
					
					applyFilterLogic.run();
					updateBentoHeroArtwork.run();
				}
			};
			
			android.content.IntentFilter filter = new android.content.IntentFilter("MFLOW_UPDATE_PLAYER_UI");
			final android.content.BroadcastReceiver bentoReceiver = new android.content.BroadcastReceiver() {
				@Override
				public void onReceive(android.content.Context context, android.content.Intent intent) {
					updateBentoHeroArtwork.run();
				}
			};
			
			if (getContext() != null) {
				try {
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
						getContext().registerReceiver(bentoReceiver, filter, android.content.Context.RECEIVER_EXPORTED);
					} else {
						getContext().registerReceiver(bentoReceiver, filter);
					}
				} catch (Exception ignored) {}
			}
			
			if (cardBentoHero != null) {
				cardBentoHero.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(android.view.View v) {
						if (!allSongs.isEmpty()) {
							java.util.ArrayList<java.util.HashMap<String, Object>> shuffled = new java.util.ArrayList<>(allSongs);
							java.util.Collections.shuffle(shuffled);
							
							com.lumina.music.MusicService.songList = shuffled;
							com.lumina.music.MusicService.currentIndex = 0;
							
							android.content.Context ctx = v.getContext();
							android.content.Intent intent = new android.content.Intent(ctx, com.lumina.music.MusicService.class);
							intent.setAction("ACTION_PLAY");
							intent.putExtra("index", 0);
							if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
								ctx.startForegroundService(intent);
							} else {
								ctx.startService(intent);
							}
						}
					}
				});
			}
			
			if (btnBentoPlay != null) {
				btnBentoPlay.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(android.view.View v) {
						if (!allSongs.isEmpty()) {
							java.util.ArrayList<java.util.HashMap<String, Object>> shuffled = new java.util.ArrayList<>(allSongs);
							java.util.Collections.shuffle(shuffled);
							
							com.lumina.music.MusicService.songList = shuffled;
							com.lumina.music.MusicService.currentIndex = 0;
							
							android.content.Context ctx = v.getContext();
							android.content.Intent intent = new android.content.Intent(ctx, com.lumina.music.MusicService.class);
							intent.setAction("ACTION_PLAY");
							intent.putExtra("index", 0);
							if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
								ctx.startForegroundService(intent);
							} else {
								ctx.startService(intent);
							}
						}
					}
				});
			}
			
			if (cardBentoFavs != null) {
				cardBentoFavs.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(android.view.View v) {
						android.content.Context ctx = v.getContext();
						if (ctx != null) {
							android.content.Intent intent = new android.content.Intent(ctx, com.lumina.music.ShoeActivity.class);
							ctx.startActivity(intent);
						}
					}
				});
			}
			
			if (cardBentoArtists != null) {
				cardBentoArtists.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(android.view.View v) {
						android.content.Context ctx = v.getContext();
						if (ctx != null) {
							android.content.Intent intent = new android.content.Intent(ctx, com.lumina.music.ArtmActivity.class);
							ctx.startActivity(intent);
						}
					}
				});
			}
			
			if (btnFilterA != null) {
				btnFilterA.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(android.view.View v) {
						prefs.edit().putInt("KEY_ACTIVE_FILTER", 1).apply();
						applyFilterLogic.run();
					}
				});
			}
			
			if (btnFilterB != null) {
				btnFilterB.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(android.view.View v) {
						prefs.edit().putInt("KEY_ACTIVE_FILTER", 2).apply();
						applyFilterLogic.run();
					}
				});
			}
			
			if (btnFilterC != null) {
				btnFilterC.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(android.view.View v) {
						prefs.edit().putInt("KEY_ACTIVE_FILTER", 3).apply();
						applyFilterLogic.run();
					}
				});
			}
			
			root.addOnAttachStateChangeListener(new android.view.View.OnAttachStateChangeListener() {
				@Override
				public void onViewAttachedToWindow(android.view.View v) {
					updateBentoHeroArtwork.run();
				}
				@Override
				public void onViewDetachedFromWindow(android.view.View v) {
					try {
						if (getContext() != null) {
							getContext().unregisterReceiver(bentoReceiver);
						}
					} catch (Exception ignored) {}
				}
			});
			
			loadSongsRunnable.run();
		}
		
	}
	public class RvMainFlowAdapter extends RecyclerView.Adapter<RvMainFlowAdapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public RvMainFlowAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getActivity().getLayoutInflater();
			View _v = _inflater.inflate(R.layout.music, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			MusicBinding binding = MusicBinding.bind(_view);
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