package butter.droid.base.content.preferences;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butter.droid.base.Constants;
import butter.droid.base.R;
import butter.droid.base.content.preferences.PrefItem.SubtitleGenerator;
import butter.droid.base.content.preferences.Prefs.PrefKey;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.manager.updater.ButterUpdateManager;
import butter.droid.base.manager.vlc.VLCOptions;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.StorageUtils;
import dagger.Reusable;

import static butter.droid.base.content.preferences.Prefs.DEFAULT_PROVIDER;

@Reusable
public class PreferencesHandler {

    private final Context context;
    private final PrefManager prefManager;
    private final Resources resources;

    @Inject
    public PreferencesHandler(Context context, PrefManager prefManager, Resources resources) {
        this.context = context;
        this.prefManager = prefManager;
        this.resources = resources;
    }

    /**
     * @return Array of preference keyys that need to be displayed
     */
    @NonNull public String[] getPreferencesOrder(boolean isTV) {
        List<String> keys = new ArrayList<>();

        // general
        if (isTV) {
            keys.add(Prefs.DEFAULT_PROVIDER);
        }

        if (!isTV) {
            keys.add(Prefs.DEFAULT_PLAYER);
        }

        keys.add(Prefs.QUALITY_DEFAULT);
        keys.add(Prefs.LOCALE);

        if (!isTV) {
            keys.add(Prefs.WIFI_ONLY);
        }

        // subtitles
        keys.add(Prefs.SUBTITLE_COLOR);
        keys.add(Prefs.SUBTITLE_SIZE);
        keys.add(Prefs.SUBTITLE_STROKE_COLOR);
        keys.add(Prefs.SUBTITLE_STROKE_WIDTH);
        keys.add(Prefs.SUBTITLE_DEFAULT);

        // torrents
        keys.add(Prefs.LIBTORRENT_CONNECTION_LIMIT);
        keys.add(Prefs.LIBTORRENT_DOWNLOAD_LIMIT);
        keys.add(Prefs.LIBTORRENT_UPLOAD_LIMIT);

        if (!isTV) {
            keys.add(Prefs.STORAGE_LOCATION);
        }
        keys.add(Prefs.REMOVE_CACHE);

        // networking
        keys.add(Prefs.LIBTORRENT_AUTOMATIC_PORT);
        keys.add(Prefs.LIBTORRENT_LISTENING_PORT);

        // advanced
        keys.add(Prefs.HW_ACCELERATION);
        keys.add(Prefs.PIXEL_FORMAT);
        keys.add(Prefs.SHOW_VPN);

        // updates
        keys.add(Prefs.AUTOMATIC_UPDATES);
        keys.add(Prefs.CHECK_UPDATE);

        // about
        if (!Constants.DEBUG_ENABLED && !isTV) {
            keys.add(Prefs.REPORT_BUG);
        }
        keys.add(Prefs.CHANGE_LOG);
        keys.add(Prefs.NOTICE);
        keys.add(Prefs.VERSION);
        keys.add(Prefs.ABOUT);

        return keys.toArray(new String[keys.size()]);
    }

    public Map<String, PrefItem> getPreferenceItems(@NonNull String[] keys) {

        Map<String, PrefItem> prefItems = new HashMap<>();

//        prefItems.add(PrefItem.newBuilder()
//                .setTitleResource(R.string.general)
//                .setClickable(false)
//                .build());

        for (String key : keys) {
            prefItems.put(key, getPreferenceItem(key));
        }

//        prefItems.add(PrefItem.newBuilder()
//                .setTitleResource(R.string.subtitles)
//                .setClickable(false)
//                .build());

//        prefItems.add(PrefItem.newBuilder()
//                .setTitleResource(R.string.torrents)
//                .setClickable(false)
//                .build());

//        prefItems.add(PrefItem.newBuilder()
//                .setTitleResource(R.string.networking)
//                .setClickable(false)
//                .build());

//        prefItems.add(PrefItem.newBuilder()
//                .setTitleResource(R.string.advanced)
//                .setClickable(false)
//                .build());

//        prefItems.add(PrefItem.newBuilder()
//                .setTitleResource(R.string.updates)
//                .setClickable(false)
//                .build());

//        prefItems.add(PrefItem.newBuilder()
//                .setTitleResource(R.string.about_app)
//                .setClickable(false)
//                .build());

        return prefItems;

    }

    @NonNull public PrefItem getPreferenceItem(@PrefKey String key) {
        switch (key) {
            case DEFAULT_PROVIDER:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_default_view)
                        .setTitleResource(R.string.default_view)
                        .setPreferenceKey(DEFAULT_PROVIDER)
                        .setValue(prefManager.get(DEFAULT_PROVIDER, ProviderManager.PROVIDER_TYPE_MOVIE))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                String[] items = resources.getStringArray(R.array.prefs_providers);
                                return items[(Integer) item.getValue()];
                            }
                        })
                        .build();
            case Prefs.DEFAULT_PLAYER:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_default_player)
                        .setTitleResource(R.string.default_player)
                        .setPreferenceKey(Prefs.DEFAULT_PLAYER)
                        .setValue(
                                prefManager.get(Prefs.DEFAULT_PLAYER_NAME, context.getString(R.string.internal_player)))
                        .setSubtitleGenerator(new PrefItem.SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return (String) item.getValue();
                            }
                        })
                        .build();
            case Prefs.QUALITY_DEFAULT:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_action_quality)
                        .setTitleResource(R.string.quality)
                        .setPreferenceKey(Prefs.QUALITY_DEFAULT)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.QUALITY_DEFAULT, "720p"))
                        .setSubtitleGenerator(new PrefItem.SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return (String) item.getValue();
                            }
                        })
                        .build();
            case Prefs.LOCALE:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_app_language)
                        .setTitleResource(R.string.i18n_language)
                        .setPreferenceKey(Prefs.LOCALE)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.LOCALE, resources.getString(R.string.device_language)))
                        .setSubtitleGenerator(new PrefItem.SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                Locale locale = LocaleUtils.toLocale((String) item.getValue());
                                return locale.getDisplayName(locale);
                            }
                        })
                        .build();
            case Prefs.WIFI_ONLY:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_wifi_only)
                        .setTitleResource(R.string.stream_over_wifi_only)
                        .setPreferenceKey(Prefs.WIFI_ONLY)
                        .setValue(prefManager.get(Prefs.WIFI_ONLY, true))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return ((Boolean) item.getValue()) ? resources.getString(
                                        R.string.enabled) : resources.getString(
                                        R.string.disabled);
                            }
                        })
                        .build();
            case Prefs.SUBTITLE_COLOR:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_subtitle_color)
                        .setTitleResource(R.string.subtitle_color)
                        .setPreferenceKey(Prefs.SUBTITLE_COLOR)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.SUBTITLE_COLOR, Color.WHITE))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return String.format("#%06X", 0xFFFFFF & (Integer) item.getValue());
                            }
                        })
                        .build();
            case Prefs.SUBTITLE_SIZE:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_subtitle_size)
                        .setTitleResource(R.string.subtitle_size)
                        .setPreferenceKey(Prefs.SUBTITLE_SIZE)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.SUBTITLE_SIZE,
                                resources.getInteger(R.integer.player_subtitles_default_text_size)))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return Integer.toString((Integer) item.getValue());
                            }
                        })
                        .build();
            case Prefs.SUBTITLE_STROKE_COLOR:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_subtitle_stroke_color)
                        .setTitleResource(R.string.subtitle_stroke_color)
                        .setPreferenceKey(Prefs.SUBTITLE_STROKE_COLOR)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.SUBTITLE_STROKE_COLOR, Color.BLACK))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return String.format("#%06X", 0xFFFFFF & ((Integer) item.getValue()));
                            }
                        })
                        .build();
            case Prefs.SUBTITLE_STROKE_WIDTH:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_subtitle_stroke_width)
                        .setTitleResource(R.string.subtitle_stroke_width)
                        .setPreferenceKey(Prefs.SUBTITLE_STROKE_WIDTH)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.SUBTITLE_STROKE_WIDTH, 2))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return Integer.toString(((Integer) item.getValue()));
                            }
                        })
                        .build();
            case Prefs.SUBTITLE_DEFAULT:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_subtitle_lang)
                        .setTitleResource(R.string.default_subtitle_language)
                        .setPreferenceKey(Prefs.SUBTITLE_DEFAULT)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.SUBTITLE_DEFAULT, null))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                String langCode = (String) item.getValue();

                                if (langCode == null) {
                                    return resources.getString(R.string.no_default_set);
                                } else {
                                    Locale locale = LocaleUtils.toLocale(langCode);
                                    return locale.getDisplayName(locale);
                                }
                            }
                        })
                        .build();
            case Prefs.LIBTORRENT_CONNECTION_LIMIT:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_connections)
                        .setTitleResource(R.string.max_connections)
                        .setPreferenceKey(Prefs.LIBTORRENT_CONNECTION_LIMIT)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.LIBTORRENT_CONNECTION_LIMIT, 200))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return item.getValue() + " connections";
                            }
                        })
                        .build();
            case Prefs.LIBTORRENT_DOWNLOAD_LIMIT:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_download_limit)
                        .setTitleResource(R.string.download_speed)
                        .setPreferenceKey(Prefs.LIBTORRENT_DOWNLOAD_LIMIT)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.LIBTORRENT_DOWNLOAD_LIMIT, 0))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                int limit = (int) item.getValue();
                                if (limit == 0) {
                                    return resources.getString(R.string.unlimited);
                                } else {
                                    return (limit / 1000) + " kB/s";
                                }
                            }
                        })
                        .build();
            case Prefs.LIBTORRENT_UPLOAD_LIMIT:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_upload_limit)
                        .setTitleResource(R.string.upload_speed)
                        .setPreferenceKey(Prefs.LIBTORRENT_UPLOAD_LIMIT)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.LIBTORRENT_UPLOAD_LIMIT, 0))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                int limit = (int) item.getValue();
                                if (limit == 0) {
                                    return resources.getString(R.string.unlimited);
                                } else {
                                    return (limit / 1000) + " kB/s";
                                }
                            }
                        })
                        .build();
            case Prefs.STORAGE_LOCATION:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_storage_location)
                        .setTitleResource(R.string.storage_location)
                        .setPreferenceKey(Prefs.STORAGE_LOCATION)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.STORAGE_LOCATION, StorageUtils.getIdealCacheDirectory(context)
                                .getAbsolutePath()))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return ((String) item.getValue());
                            }
                        })
                        .build();
            case Prefs.REMOVE_CACHE:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_remove_cache)
                        .setTitleResource(R.string.remove_cache)
                        .setPreferenceKey(Prefs.REMOVE_CACHE)
                        .setValue(prefManager.get(Prefs.REMOVE_CACHE, true))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return ((Boolean) item.getValue()) ? resources.getString(
                                        R.string.enabled) : resources.getString(R.string.disabled);
                            }
                        })
                        .build();
            case Prefs.LIBTORRENT_AUTOMATIC_PORT:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_random)
                        .setTitleResource(R.string.automatic_port)
                        .setPreferenceKey(Prefs.LIBTORRENT_AUTOMATIC_PORT)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.LIBTORRENT_AUTOMATIC_PORT, true))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return ((Boolean) item.getValue()) ? resources.getString(
                                        R.string.enabled) : resources.getString(R.string.disabled);
                            }
                        })
                        .build();
            case Prefs.LIBTORRENT_LISTENING_PORT:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_router)
                        .setTitleResource(R.string.listening_port)
                        .setPreferenceKey(Prefs.LIBTORRENT_LISTENING_PORT)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.LIBTORRENT_LISTENING_PORT, 59718))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                int port = (int) item.getValue();
                                if (port == -1 || prefManager.get(Prefs.LIBTORRENT_AUTOMATIC_PORT, true)) {
                                    return "Listening on random port";
                                } else {
                                    return "Listening on port " + port;
                                }
                            }
                        })
                        .build();
            case Prefs.HW_ACCELERATION:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_hw_accel)
                        .setTitleResource(R.string.hw_acceleration)
                        .setPreferenceKey(Prefs.HW_ACCELERATION)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.HW_ACCELERATION, VLCOptions.HW_ACCELERATION_AUTOMATIC))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                int value = ((Integer) item.getValue());
                                switch (value) {
                                    case VLCOptions.HW_ACCELERATION_DECODING:
                                        return resources.getString(R.string.hw_decoding);
                                    case VLCOptions.HW_ACCELERATION_DISABLED:
                                        return resources.getString(R.string.disabled);
                                    case VLCOptions.HW_ACCELERATION_FULL:
                                        return resources.getString(R.string.hw_full);
                                    default:
                                    case VLCOptions.HW_ACCELERATION_AUTOMATIC:
                                        return resources.getString(R.string.hw_automatic);
                                }
                            }
                        })
                        .build();
            case Prefs.PIXEL_FORMAT:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_pixel_format)
                        .setTitleResource(R.string.pixel_format)
                        .setPreferenceKey(Prefs.PIXEL_FORMAT)
                        .hasNext(true)
                        .setValue(prefManager.get(Prefs.PIXEL_FORMAT, "RV32"))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                String value = (String) item.getValue();
                                if ("YV12".equals(value)) {
                                    return resources.getString(R.string.yuv);
                                } else if ("RV16".equals(value)) {
                                    return resources.getString(R.string.rgb16);
                                } else {
                                    return resources.getString(R.string.rgb32);
                                }
                            }
                        })
                        .build();
            case Prefs.SHOW_VPN:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_nav_vpn)
                        .setTitleResource(R.string.show_vpn)
                        .setPreferenceKey(Prefs.SHOW_VPN)
                        .setValue(prefManager.get(Prefs.SHOW_VPN, true))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return ((Boolean) item.getValue()) ? resources.getString(
                                        R.string.enabled) : resources.getString(R.string.disabled);
                            }
                        })
                        .build();
            case Prefs.AUTOMATIC_UPDATES:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_auto_update)
                        .setTitleResource(R.string.auto_updates)
                        .setPreferenceKey(Prefs.AUTOMATIC_UPDATES)
                        .setValue(prefManager.get(Prefs.AUTOMATIC_UPDATES, true))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return ((Boolean) item.getValue()) ? resources.getString(
                                        R.string.enabled) : resources.getString(R.string.disabled);
                            }
                        })
                        .build();
            case Prefs.CHECK_UPDATE:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_check_update)
                        .setTitleResource(R.string.check_for_updates)
                        .setPreferenceKey(Prefs.CHECK_UPDATE)
                        .setValue(prefManager.get(ButterUpdateManager.LAST_UPDATE_CHECK, 1L))
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                long timeStamp = (long) item.getValue();
                                Calendar cal = Calendar.getInstance(Locale.getDefault());
                                cal.setTimeInMillis(timeStamp);
                                String time = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM,
                                        Locale.getDefault()).format(timeStamp);
                                String date = DateFormat.format("dd-MM-yyy", cal).toString();
                                return resources.getString(R.string.last_check) + ": " + date + " " + time;
                            }
                        })
                        .build();
            case Prefs.REPORT_BUG:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_report_bug)
                        .setTitleResource(R.string.report_a_bug)
                        .setPreferenceKey(Prefs.REPORT_BUG)
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return resources.getString(R.string.tap_to_open);
                            }
                        })
                        .build();
            case Prefs.CHANGE_LOG:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_changelog)
                        .setTitleResource(R.string.changelog)
                        .setPreferenceKey(Prefs.CHANGE_LOG)
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return resources.getString(R.string.tap_to_open);
                            }
                        })
                        .build();
            case Prefs.NOTICE:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_open_source)
                        .setTitleResource(R.string.open_source_licenses)
                        .setPreferenceKey(Prefs.NOTICE)
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return resources.getString(R.string.tap_to_open);
                            }
                        })
                        .build();
            case Prefs.VERSION:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_version)
                        .setTitleResource(R.string.version)
                        .setPreferenceKey(Prefs.VERSION)
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                try {
                                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                                            context.getPackageName(), 0);
                                    return packageInfo.versionName + " - " + Build.CPU_ABI;
                                } catch (NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                                return "?.? (?) - ?";
                            }
                        })
                        .build();
            case Prefs.ABOUT:
                return PrefItem.newBuilder()
                        .setIconResource(R.drawable.ic_prefs_about)
                        .setTitleResource(R.string.about_app)
                        .setPreferenceKey(Prefs.ABOUT)
                        .setSubtitleGenerator(new SubtitleGenerator() {
                            @Override
                            public String get(PrefItem item) {
                                return resources.getString(R.string.tap_to_open);
                            }
                        })
                        .build();
            default:
                throw new IllegalArgumentException("Unknown preference key:" + key);
        }
    }

}
