package pct.droid.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import org.videolan.libvlc.LibVLC;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.adapters.PreferencesListAdapter;
import pct.droid.base.Constants;
import pct.droid.base.JiraClient;
import pct.droid.base.preferences.DefaultPlayer;
import pct.droid.base.preferences.PrefItem;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.updater.PopcornUpdater;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.ResourceUtil;
import pct.droid.base.utils.StorageUtils;
import pct.droid.fragments.ChangeLogDialogFragment;
import pct.droid.fragments.ColorPickerDialogFragment;
import pct.droid.fragments.NumberPickerDialogFragment;
import pct.droid.fragments.StringArraySelectorDialogFragment;
import pct.droid.utils.ToolbarUtils;

public class PreferencesActivity extends BaseActivity
		implements SharedPreferences.OnSharedPreferenceChangeListener, DirectoryChooserFragment.OnFragmentInteractionListener {

	private List<Object> mPrefItems = new ArrayList<>();
	private LinearLayoutManager mLayoutManager;
	private DirectoryChooserFragment mDirectoryChooserFragment;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;
	@InjectView(R.id.recyclerView)
	RecyclerView recyclerView;
	@InjectView(R.id.rootLayout)
	ViewGroup rootLayout;

	public static Intent startActivity(Activity activity) {
		Intent intent = new Intent(activity, PreferencesActivity.class);
		activity.startActivity(intent);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_preferences);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.settings);

		ToolbarUtils.updateToolbarHeight(this,toolbar);

		mLayoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(mLayoutManager);

		PrefUtils.getPrefs(this).registerOnSharedPreferenceChangeListener(this);

		refreshItems();
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		PrefUtils.getPrefs(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	private void refreshItems() {
		mPrefItems = new ArrayList<>();
		mPrefItems.add(getResources().getString(R.string.general));

		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_default_view, R.string.default_view, Prefs.DEFAULT_VIEW, 0,
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(final PrefItem item) {
						String[] items = {getString(R.string.title_movies), getString(R.string.title_shows)};

						openListSelectionDialog(item.getTitle(), items, StringArraySelectorDialogFragment.SINGLE_CHOICE,
								(int) item.getValue(), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int position) {
										item.saveValue(position);
										dialog.dismiss();
									}
								});
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						if ((Integer) item.getValue() == 1) {
							return getString(R.string.title_shows);
						}
						return getString(R.string.title_movies);
					}
				}));

		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_default_player, R.string.default_player, Prefs.DEFAULT_PLAYER, "",
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(final PrefItem item) {
						int currentPosition = 0;
						String currentValue = item.getValue().toString();

						final Map<String, String> players = DefaultPlayer.getVideoPlayerApps();
						final String[] playerDatas = players.keySet().toArray(new String[players.size()]);
						String[] items = new String[players.size() + 1];
						items[0] = getString(R.string.internal_player);
						for (int i = 0; i < playerDatas.length; i++) {
							String playerData = playerDatas[i];
							String playerName = players.get(playerData);

							items[i + 1] = playerName;
							if (playerData.equals(currentValue)) {
								currentPosition = i + 1;
							}
						}

						openListSelectionDialog(item.getTitle(), items, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int position) {
										if (position == 0) {
											DefaultPlayer.set("", "");
										} else {
											String playerData = playerDatas[position - 1];
											DefaultPlayer.set(players.get(playerData), playerData);
										}
										dialog.dismiss();
									}
								});
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						return PrefUtils.get(PreferencesActivity.this, Prefs.DEFAULT_PLAYER_NAME, getString(R.string.internal_player));
					}
				}));
		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_app_language, R.string.i18n_language, Prefs.LOCALE, "",
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(final PrefItem item) {
						int currentPosition = 0;
						String currentValue = item.getValue().toString();

						final String[] languages = getResources().getStringArray(R.array.translation_languages);
						Arrays.sort(languages);

						String[] items = new String[languages.length + 1];
						items[0] = getString(R.string.device_language);
						for (int i = 0; i < languages.length; i++) {
							Locale locale = LocaleUtils.toLocale(languages[i]);
							items[i + 1] = locale.getDisplayName(locale);
							if (languages[i].equals(currentValue)) {
								currentPosition = i + 1;
							}
						}

						openListSelectionDialog(item.getTitle(), items, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int position) {
										if (position == 0) {
											item.clearValue();
										} else {
											item.saveValue(languages[position - 1]);
										}

										dialog.dismiss();

										Toast.makeText(PreferencesActivity.this, R.string.restart_effect, Toast.LENGTH_LONG).show();
									}
								});
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						String langCode = item.getValue().toString();
						if (langCode.isEmpty())
							return getString(R.string.device_language);

						Locale locale = LocaleUtils.toLocale(langCode);
						return locale.getDisplayName(locale);

					}
				}));
		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_wifi_only, R.string.stream_over_wifi_only, Prefs.WIFI_ONLY, true,
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(PrefItem item) {
						item.saveValue(!(boolean) item.getValue());
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						boolean enabled = (boolean) item.getValue();
						return enabled ? getString(R.string.enabled) : getString(R.string.disabled);
					}
				}));

		mPrefItems.add(getResources().getString(R.string.subtitles));

		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_subtitle_color, R.string.subtitle_color, Prefs.SUBTITLE_COLOR, Color.WHITE,
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(final PrefItem item) {
						Bundle args = new Bundle();
						args.putString(NumberPickerDialogFragment.TITLE, item.getTitle());
						args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, (int) item.getValue());

						ColorPickerDialogFragment dialogFragment = new ColorPickerDialogFragment();
						dialogFragment.setArguments(args);
						dialogFragment.setOnResultListener(new ColorPickerDialogFragment.ResultListener() {
							@Override
							public void onNewValue(int value) {
								item.saveValue(value);
							}
						});
						dialogFragment.show(getFragmentManager(), "pref_fragment");
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						return String.format("#%06X", 0xFFFFFF & (int) item.getValue());
					}
				}));
		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_subtitle_size, R.string.subtitle_size, Prefs.SUBTITLE_SIZE, 16,
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(final PrefItem item) {
						Bundle args = new Bundle();
						args.putString(NumberPickerDialogFragment.TITLE, item.getTitle());
						args.putInt(NumberPickerDialogFragment.MAX_VALUE, 30);
						args.putInt(NumberPickerDialogFragment.MIN_VALUE, 10);
						args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, (int) item.getValue());

						NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
						dialogFragment.setArguments(args);
						dialogFragment.setOnResultListener(new NumberPickerDialogFragment.ResultListener() {
							@Override
							public void onNewValue(int value) {
								item.saveValue(value);
							}
						});
						dialogFragment.show(getFragmentManager(), "pref_fragment");
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						return Integer.toString((int) item.getValue());
					}
				}));
		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_subtitle_lang, R.string.subtitle_language, Prefs.SUBTITLE_DEFAULT, "",
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(final PrefItem item) {
						int currentPosition = 0;
						String currentValue = item.getValue().toString();

						final String[] languages = getResources().getStringArray(R.array.subtitle_languages);
						String[] items = new String[languages.length + 1];
						items[0] = getString(R.string.no_default_set);
						for (int i = 0; i < languages.length; i++) {
							Locale locale = LocaleUtils.toLocale(languages[i]);
							items[i + 1] = locale.getDisplayName(locale);
							if (languages[i].equals(currentValue)) {
								currentPosition = i + 1;
							}
						}

						openListSelectionDialog(item.getTitle(), items, StringArraySelectorDialogFragment.SINGLE_CHOICE, currentPosition,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int position) {
										if (position == 0) {
											item.clearValue();
										} else {
											item.saveValue(languages[position - 1]);
										}
										dialog.dismiss();
									}
								});
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						String langCode = item.getValue().toString();
						if (langCode.isEmpty())
							return getString(R.string.no_default_set);

						Locale locale = LocaleUtils.toLocale(langCode);
						return locale.getDisplayName(locale);
					}
				}));
		mPrefItems.add(getResources().getString(R.string.updates));
		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_auto_update, R.string.auto_updates, Prefs.AUTOMATIC_UPDATES, true,
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(PrefItem item) {
						item.saveValue(!(boolean) item.getValue());
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						boolean enabled = (boolean) item.getValue();
						return enabled ? getString(R.string.enabled) : getString(R.string.disabled);
					}
				}));
		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_check_update, R.string.check_for_updates, PopcornUpdater.LAST_UPDATE_CHECK, 1,
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(PrefItem item) {
						PopcornUpdater.getInstance(PreferencesActivity.this).checkUpdatesManually();
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						long timeStamp = Long.parseLong(PrefUtils.get(PreferencesActivity.this, PopcornUpdater.LAST_UPDATE_CHECK, "0"));
						Calendar cal = Calendar.getInstance(Locale.getDefault());
						cal.setTimeInMillis(timeStamp);
						String time = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.getDefault()).format(timeStamp);
						String date = DateFormat.format("dd-MM-yyy", cal).toString();
						return getString(R.string.last_check) + " :" + date + " " + time;
					}
				}));

		mPrefItems.add(getResources().getString(R.string.advanced));
		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_storage_location, R.string.storage_location, Prefs.STORAGE_LOCATION,
				StorageUtils.getIdealCacheDirectory(this),
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(final PrefItem item) {
						String[] items = {getString(R.string.storage_automatic), getString(R.string.storage_choose)};

						openListSelectionDialog(item.getTitle(), items, StringArraySelectorDialogFragment.NORMAL, -1,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int position) {
										if (position == 0) {
											item.clearValue();
										} else {
											mDirectoryChooserFragment = DirectoryChooserFragment.newInstance("pct.droid", null);
											mDirectoryChooserFragment.show(getFragmentManager(), "pref_fragment");
											dialog.dismiss();
										}
									}
								});
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						return item.getValue().toString();
					}
				}));
		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_remove_cache, R.string.remove_cache, Prefs.REMOVE_CACHE, true,
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(PrefItem item) {
						PrefUtils.save(PreferencesActivity.this, Prefs.REMOVE_CACHE, !(boolean) item.getValue());
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						boolean enabled = (boolean) item.getValue();
						return enabled ? getString(R.string.enabled) : getString(R.string.disabled);
					}
				}));
		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_hw_accel, R.string.hw_acceleration, Prefs.HW_ACCELERATION,
				LibVLC.HW_ACCELERATION_AUTOMATIC,
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(final PrefItem item) {
						String[] items = {getString(R.string.hw_automatic), getString(R.string.disabled), getString(R.string.hw_decoding),
								getString(R.string.hw_full)};

						openListSelectionDialog(item.getTitle(), items, StringArraySelectorDialogFragment.SINGLE_CHOICE,
								(int) item.getValue() + 1, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int position) {
										item.saveValue(position - 1);
										dialog.dismiss();
									}
								});
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						switch ((int) item.getValue()) {
							case LibVLC.HW_ACCELERATION_DECODING:
								return getString(R.string.hw_decoding);
							case LibVLC.HW_ACCELERATION_DISABLED:
								return getString(R.string.disabled);
							case LibVLC.HW_ACCELERATION_FULL:
								return getString(R.string.hw_full);
							default:
							case LibVLC.HW_ACCELERATION_AUTOMATIC:
								return getString(R.string.hw_automatic);
						}
					}
				}));

		mPrefItems.add(getResources().getString(R.string.about));

		if (!Constants.DEBUG_ENABLED) {
			mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_report_bug, R.string.report_a_bug, "", "",
					new PrefItem.OnClickListener() {
						@Override
						public void onClick(PrefItem item) {
							JiraClient.getVersionId(new JiraClient.VersionCallback() {
								@Override
								public void onResult(String result) {
									Intent i = new Intent(Intent.ACTION_VIEW);
									i.setData(Uri.parse(
											"https://git.popcorntime.io/jira/CreateIssueDetails!init.jspa?pid=10200&issuetype=1&priority=4&versions=" +
													result));
									startActivity(i);
								}
							});
						}
					},
					new PrefItem.SubTitleGenerator() {
						@Override
						public String get(PrefItem item) {
							return getString(R.string.tap_to_open);
						}
					}));
		}

		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_changelog, R.string.changelog, "", "",
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(PrefItem item) {
						ChangeLogDialogFragment changeLogDialogFragment = new ChangeLogDialogFragment();
						changeLogDialogFragment.show(getSupportFragmentManager(), "prefs_fragment");
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						return getString(R.string.tap_to_open);
					}
				}));

		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_open_source, R.string.open_source_licenses, "", "",
				new PrefItem.OnClickListener() {
					@Override
					public void onClick(PrefItem item) {
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(Constants.GIT_URL + "stash/projects/PA/repos/popcorn-android/browse/NOTICE.md"));
						startActivity(i);
					}
				},
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						return getString(R.string.tap_to_open);
					}
				}));

		mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_version, R.string.version, "", "",
				new PrefItem.SubTitleGenerator() {
					@Override
					public String get(PrefItem item) {
						try {
							PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
							return packageInfo.versionName + " - " + Build.CPU_ABI;
						} catch (PackageManager.NameNotFoundException e) {
							e.printStackTrace();
						}
						return "?.? (?) - ?";
					}
				}));

		if (recyclerView.getAdapter() != null && mLayoutManager != null) {
			int position = mLayoutManager.findFirstVisibleItemPosition();
			View v = mLayoutManager.findViewByPosition(position);
			recyclerView.setAdapter(new PreferencesListAdapter(mPrefItems));
			if (v != null) {
				int offset = v.getTop();
				mLayoutManager.scrollToPositionWithOffset(position, offset);
			} else {
				mLayoutManager.scrollToPosition(position);
			}
		} else {
			recyclerView.setAdapter(new PreferencesListAdapter(mPrefItems));
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		refreshItems();
		toolbar.setMinimumHeight((int) ResourceUtil.getAttributeDimension(this,this.getTheme(),R.attr.actionBarSize));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (isUseChangeablePref(key)) {
			refreshItems();
		}
	}

	private boolean isUseChangeablePref(String key) {
		boolean b = false;
		for (Object item : mPrefItems) {
			if (item instanceof PrefItem) {
				PrefItem pref = (PrefItem) item;
				if (pref.getPrefKey().equals(key))
					b = true;
			}
		}
		return b;
	}

	private void openListSelectionDialog(String title, String[] items, int mode, int defaultPosition,
			DialogInterface.OnClickListener onClickListener) {
		if (mode == StringArraySelectorDialogFragment.NORMAL) {
			StringArraySelectorDialogFragment.show(getFragmentManager(), title, items, defaultPosition, onClickListener);
		} else if (mode == StringArraySelectorDialogFragment.SINGLE_CHOICE) {
			StringArraySelectorDialogFragment.showSingleChoice(getFragmentManager(), title, items, defaultPosition, onClickListener);
		}
	}

	@Override
	public void onSelectDirectory(@NonNull String s) {
		File f = new File(s);
		if (f.canWrite()) {
			PrefUtils.save(this, Prefs.STORAGE_LOCATION, s + "/pct.droid");
		} else {
			Toast.makeText(this, R.string.not_writable, Toast.LENGTH_SHORT).show();
		}
		mDirectoryChooserFragment.dismiss();
	}

	@Override
	public void onCancelChooser() {
		mDirectoryChooserFragment.dismiss();
	}
}
