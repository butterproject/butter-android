/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.ui.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import butter.droid.R;
import butter.droid.base.Constants;
import butter.droid.base.content.preferences.PrefItem;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.content.preferences.Prefs.PrefKey;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.updater.ButterUpdateManager;
import butter.droid.base.manager.vlc.PlayerManager;
import butter.droid.base.utils.LocaleUtils;
import timber.log.Timber;

public class PreferencesPresenterImpl implements PreferencesPresenter,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final PreferencesView view;
    private final PrefManager prefManager;
    private final PreferencesHandler preferencesHandler;
    private final Resources resources;
    private final PlayerManager playerManager;
    private final ButterUpdateManager updateManager;

    private String[] keys;
    private String[] providers;
    private String[] qualities;
    private String[] languages;

    public PreferencesPresenterImpl(PreferencesView view, PrefManager prefManager,
            PreferencesHandler preferencesHandler, Resources resources, PlayerManager playerManager,
            ButterUpdateManager updateManager) {
        this.view = view;
        this.prefManager = prefManager;
        this.preferencesHandler = preferencesHandler;
        this.resources = resources;
        this.playerManager = playerManager;
        this.updateManager = updateManager;
    }

    @Override public void onCreate() {
        providers = resources.getStringArray(R.array.prefs_providers);
        keys = preferencesHandler.getPreferencesOrder(false);
        Map<String, PrefItem> items = preferencesHandler.getPreferenceItems(keys);
        view.displayItems(keys, items);
        qualities = resources.getStringArray(R.array.video_qualities);
        languages = resources.getStringArray(R.array.subtitle_languages);

        prefManager.registerOnSharedPreferenceChangeListener(this);
    }

    @Override public void onDestroy() {
        prefManager.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override public void itemSelected(PrefItem item) {
        updateItem(item);
    }

    @Override public void onSimpleChaiseItemSelected(@PrefKey String key, int position) {
        switch (key) {
            case Prefs.DEFAULT_PROVIDER:
                prefManager.save(key, position);
                break;
            case Prefs.DEFAULT_PLAYER:
                if (position == 0) {
                    playerManager.set(null, null);
                } else {
                    final Map<String, String> players = playerManager.getVideoPlayerApps();
                    final String[] playerDatas = players.keySet().toArray(new String[players.size()]);
                    String playerData = playerDatas[position - 1];
                    playerManager.set(players.get(playerData), playerData);
                }
                break;
            case Prefs.QUALITY_DEFAULT:
                prefManager.save(key, qualities[position]);
                break;
            case Prefs.LOCALE:
                if (position == 0) {
                    prefManager.remove(key);
                } else {
                    prefManager.save(key, languages[position - 1]);
                }
                view.showMessage(R.string.restart_effect);
                break;
            case Prefs.HW_ACCELERATION:
                prefManager.save(key, position - 1);
                break;
        }
    }

    @Override public void onColorSelected(@PrefKey String key, int color) {
        switch (key) {
            case Prefs.SUBTITLE_COLOR:
            case Prefs.SUBTITLE_STROKE_COLOR:
                prefManager.save(key, color);
                break;
        }
    }

    @Override public void onNumberSelected(@PrefKey String key, int value) {
        switch (key) {
            case Prefs.SUBTITLE_SIZE:
            case Prefs.SUBTITLE_STROKE_WIDTH:
            case Prefs.LIBTORRENT_CONNECTION_LIMIT:
            case Prefs.LIBTORRENT_DOWNLOAD_LIMIT:
            case Prefs.LIBTORRENT_UPLOAD_LIMIT:
            case Prefs.LIBTORRENT_LISTENING_PORT:
                prefManager.save(key, value);
                break;
        }
    }

    @Override public void clearPreference(@PrefKey String key) {
        prefManager.remove(key);
    }

    @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        int keyPosition = getKeyPosition(key);
        if (keyPosition > -1) {
            view.updateItem(keyPosition, preferencesHandler.getPreferenceItem(key));
        }
    }

    private int getKeyPosition(String key) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private void updateItem(@NonNull PrefItem item) {
        switch (item.getPrefKey()) {
            case Prefs.DEFAULT_PROVIDER:
                view.openSimpleChoiceSelector(Prefs.DEFAULT_PROVIDER, item.getTitleRes(), providers,
                        (Integer) item.getValue());
                break;
            case Prefs.DEFAULT_PLAYER:
                updateDefaultPlayer(item);
                break;
            case Prefs.QUALITY_DEFAULT:
                int selectedItem = Arrays.asList(qualities).indexOf(item.getValue());
                view.openSimpleChoiceSelector(Prefs.QUALITY_DEFAULT, item.getTitleRes(), qualities, selectedItem);
                break;
            case Prefs.LOCALE:
                updateLocale(item);
                break;
            case Prefs.WIFI_ONLY:
            case Prefs.REMOVE_CACHE:
            case Prefs.LIBTORRENT_AUTOMATIC_PORT:
            case Prefs.SHOW_VPN:
            case Prefs.AUTOMATIC_UPDATES:
                prefManager.save(item.getPrefKey(), !(boolean) item.getValue());
                break;
            case Prefs.SUBTITLE_COLOR:
                view.openColorSelector(Prefs.SUBTITLE_COLOR, item.getTitleRes(), (Integer) item.getValue());
                break;
            case Prefs.SUBTITLE_SIZE:
                view.openPreciseSmallNumberSelector(Prefs.SUBTITLE_SIZE, item.getTitleRes(), (Integer) item.getValue(),
                        10, 60);
                break;
            case Prefs.SUBTITLE_STROKE_COLOR:
                view.openColorSelector(Prefs.SUBTITLE_STROKE_COLOR, item.getTitleRes(), (Integer) item.getValue());
                break;
            case Prefs.SUBTITLE_STROKE_WIDTH:
                view.openPreciseSmallNumberSelector(Prefs.SUBTITLE_STROKE_WIDTH, item.getTitleRes(),
                        (Integer) item.getValue(), 0, 5);
                break;
            case Prefs.SUBTITLE_DEFAULT:
                updateSubtitleDefault(item);
                break;
            case Prefs.LIBTORRENT_CONNECTION_LIMIT:
                view.openPreciseSmallNumberSelector(Prefs.LIBTORRENT_CONNECTION_LIMIT, item.getTitleRes(),
                        (Integer) item.getValue(), 0, 200);
                break;
            case Prefs.LIBTORRENT_DOWNLOAD_LIMIT:
                view.openNumberSelector(Prefs.LIBTORRENT_DOWNLOAD_LIMIT, item.getTitleRes(), (Integer) item.getValue(),
                        0, 3000);
                break;
            case Prefs.LIBTORRENT_UPLOAD_LIMIT:
                view.openNumberSelector(Prefs.LIBTORRENT_UPLOAD_LIMIT, item.getTitleRes(), (Integer) item.getValue(), 0,
                        3000);
                break;
            case Prefs.STORAGE_LOCATION:
                view.openDirectorySelector(Prefs.STORAGE_LOCATION, item.getTitleRes(), (String) item.getValue());
//                if (value != null) {
//                    item.saveValue(value);
//                } else {
//                    item.clearValue();
//                }
                break;
            case Prefs.LIBTORRENT_LISTENING_PORT:
                view.openPreciseNumberSelector(Prefs.LIBTORRENT_LISTENING_PORT, item.getTitleRes(),
                        (Integer) item.getValue(), 1024, 65534);
                break;
            case Prefs.HW_ACCELERATION:
                final String[] hwItems = resources.getStringArray(R.array.prefs_hw);
                view.openSimpleChoiceSelector(Prefs.HW_ACCELERATION, item.getTitleRes(), hwItems,
                        (Integer) item.getValue() + 1);
                break;
            case Prefs.PIXEL_FORMAT:
                updatePixelFormat(item);
                break;
            case Prefs.CHECK_UPDATE:
                updateManager.checkUpdatesManually();
                break;
            case Prefs.REPORT_BUG:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Constants.REPORT_ISSUE_URL));
                view.openActivity(i);
                break;
            case Prefs.CHANGE_LOG:
                view.openChangelog();
                break;
            case Prefs.NOTICE:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Constants.NOTICE_URL));
                view.openActivity(intent);
                break;
            case Prefs.ABOUT:
                view.showAboutScreen();
                break;
            default:
                Timber.i("Key %s has no action", item.getPrefKey());
                break;
        }
    }

    private void updateDefaultPlayer(@NonNull PrefItem item) {
        int currentPosition = 0;
        String currentValue = item.getValue().toString();

        final Map<String, String> players = playerManager.getVideoPlayerApps();
        final String[] playerDatas = players.keySet().toArray(new String[players.size()]);
        String[] items = new String[players.size() + 1];
        items[0] = resources.getString(butter.droid.base.R.string.internal_player);
        for (int i = 0; i < playerDatas.length; i++) {
            String playerData = playerDatas[i];
            String playerName = players.get(playerData);

            items[i + 1] = playerName;
            if (playerData.equals(currentValue)) {
                currentPosition = i + 1;
            }
        }

        view.openSimpleChoiceSelector(Prefs.DEFAULT_PLAYER, item.getTitleRes(), items, currentPosition);
    }

    private void updateLocale(@NonNull PrefItem item) {
        int currentPosition = 0;
        String currentValue = item.getValue().toString();

        final String[] languages = resources.getStringArray(R.array.translation_languages);
        Arrays.sort(languages);

        String[] items = new String[languages.length + 1];
        items[0] = resources.getString(R.string.device_language);
        for (int i = 0; i < languages.length; i++) {
            Locale locale = LocaleUtils.toLocale(languages[i]);
            items[i + 1] = locale.getDisplayName(locale);
            if (languages[i].equals(currentValue)) {
                currentPosition = i + 1;
            }
        }

        view.openSimpleChoiceSelector(Prefs.LOCALE, item.getTitleRes(), items, currentPosition);

//        if (position == 0) {
//            item.clearValue();
//        } else {
//            item.saveValue(languages[position - 1]);
//        }

    }

    private void updateSubtitleDefault(@NonNull PrefItem item) {
        int currentPosition = 0;
        String currentValue = (String) item.getValue();

        String[] items = new String[languages.length + 1];
        items[0] = resources.getString(R.string.no_default_set);

        if (currentValue != null) {
            for (int i = 0; i < languages.length; i++) {
                Locale locale = LocaleUtils.toLocale(languages[i]);
                items[i + 1] = locale.getDisplayName(locale);
                if (languages[i].equals(currentValue)) {
                    currentPosition = i + 1;
                }
            }
        }

        view.openSimpleChoiceSelector(Prefs.SUBTITLE_DEFAULT, item.getTitleRes(), items, currentPosition);

//        if (position == 0) {
//            item.clearValue();
//        } else {
//            item.saveValue(languages[position - 1]);
//        }
    }

    private void updatePixelFormat(@NonNull PrefItem item) {
        final String[] pixelFormats = resources.getStringArray(R.array.prefs_pixel_format);

        String currentValue = (String) item.getValue();
        int current = 1;
        if (currentValue.equals("YV12")) {
            current = 2;
        } else if (currentValue.equals("RV16")) {
            current = 0;
        }

        view.openSimpleChoiceSelector(Prefs.PIXEL_FORMAT, item.getTitleRes(), pixelFormats, current);
//        if (position == 2) {
//            item.saveValue("YV12");
//        } else if (position == 0) {
//            item.saveValue("RV16");
//        } else {
//            item.saveValue("RV32");
//        }
    }

}
