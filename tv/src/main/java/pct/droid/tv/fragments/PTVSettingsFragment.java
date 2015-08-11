package pct.droid.tv.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedActionsStylist;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.videolan.libvlc.LibVLC;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pct.droid.base.preferences.Prefs;
import pct.droid.base.updater.PopcornUpdater;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.R;

public class PTVSettingsFragment extends GuidedStepFragment {

    String[] DEFAULT_VIEW_ITEMS;
    String[] QUALITIES;
    String[] PIXEL_FORMATS;
    String[] HW_ACCEL;

    private List<GuidedAction> mActions;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSelectedActionPosition(0);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.preferences), null, getString(R.string.app_name), null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        DEFAULT_VIEW_ITEMS = new String[]{ getString(R.string.title_movies), getString(R.string.title_shows), getString(R.string.title_anime) };
        QUALITIES = getResources().getStringArray(R.array.video_qualities);
        PIXEL_FORMATS = new String[]{ getString(R.string.rgb16), getString(R.string.rgb32), getString(R.string.yuv) };
        HW_ACCEL = new String[]{ getString(R.string.hw_automatic), getString(R.string.disabled), getString(R.string.hw_decoding), getString(R.string.hw_full) };

        actions.add(new GuidedAction.Builder().id(R.id.action_general).infoOnly(true).title(getString(R.string.general)).build());

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_default_view)
                .title(getString(R.string.default_view))
                .description(getCurrentDefaultView())
                .iconResourceId(R.drawable.ic_prefs_default_view, getActivity())
                .build());

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_quality)
                .title(getString(R.string.quality))
                .description(getCurrentDefaultQuality())
                .iconResourceId(R.drawable.ic_action_quality, getActivity())
                .build());

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_app_language)
                .title(getString(R.string.i18n_language))
                .description(getCurrentAppLanguage())
                .iconResourceId(R.drawable.ic_prefs_app_language, getActivity())
                .build());

        actions.add(new GuidedAction.Builder().id(R.id.action_subtitles).infoOnly(true).title(getString(R.string.subtitles)).build());

        actions.add(new GuidedAction.Builder().id(R.id.action_torrents).infoOnly(true).title(getString(R.string.torrents)).build());

        actions.add(new GuidedAction.Builder().id(R.id.action_advanced).infoOnly(true).title(getString(R.string.advanced)).build());

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_hardware_acceleration)
                .title(getString(R.string.hw_acceleration))
                .description(getCurrentHardwareAccel())
                .iconResourceId(R.drawable.ic_prefs_hw_accel, getActivity()).build());

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_pixel_format)
                .title(getString(R.string.pixel_format))
                .description(getCurrentPixelFormat())
                .iconResourceId(R.drawable.ic_prefs_pixel_format, getActivity()).build());

        actions.add(new GuidedAction.Builder().id(R.id.action_updates).infoOnly(true).title(getString(R.string.updates)).build());

        Boolean autoUpdates = PrefUtils.get(getActivity(), Prefs.AUTOMATIC_UPDATES, true);

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_auto_updates)
                .title(getString(R.string.auto_updates))
                .description(getString(autoUpdates ? R.string.enabled : R.string.disabled))
                .checked(autoUpdates)
                .iconResourceId(R.drawable.ic_prefs_auto_update, getActivity()).build());

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_check_updates)
                .title(getString(R.string.check_for_updates))
                .description(getLastUpdateCheck())
                .iconResourceId(R.drawable.ic_prefs_check_update, getActivity()).build());

        actions.add(new GuidedAction.Builder().id(R.id.action_about_section).infoOnly(true).title(getString(R.string.about)).build());

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_changelog)
                .title(getString(R.string.changelog))
                .description(getString(R.string.tap_to_open))
                .hasNext(true)
                .iconResourceId(R.drawable.ic_prefs_changelog, getActivity()).build());

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_licenses)
                .title(getString(R.string.open_source_licenses))
                .description(getString(R.string.tap_to_open))
                .hasNext(true)
                .iconResourceId(R.drawable.ic_prefs_open_source, getActivity()).build());

        actions.add(new GuidedAction.Builder()
                .title(getString(R.string.version_pref))
                .description(getCurrentAppVersion())
                .iconResourceId(R.drawable.ic_prefs_version, getActivity()).build());

        actions.add(new GuidedAction.Builder()
                .id(R.id.action_about)
                .title(getString(R.string.about))
                .description(getString(R.string.tap_to_open))
                .iconResourceId(R.drawable.ic_prefs_about, getActivity())
                .hasNext(true).build());

        mActions = actions;

        super.onCreateActions(actions, savedInstanceState);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        super.onGuidedActionClicked(action);
        int currentPos = getSelectedActionPosition();

        switch ((int) action.getId()) {
            case R.id.action_default_view:
                int defaultView = PrefUtils.get(getActivity(), Prefs.DEFAULT_VIEW, 0);
                if(defaultView == DEFAULT_VIEW_ITEMS.length - 1) {
                    defaultView = 0;
                } else {
                    defaultView++;
                }
                PrefUtils.save(getActivity(), Prefs.DEFAULT_VIEW, defaultView);
                action.setLabel2(DEFAULT_VIEW_ITEMS[defaultView]);
                break;
            case R.id.action_quality:
                int quality = PrefUtils.get(getActivity(), Prefs.QUALITY_DEFAULT, 0);
                if(quality == QUALITIES.length - 1) {
                    quality = 0;
                } else {
                    quality++;
                }
                PrefUtils.save(getActivity(), Prefs.QUALITY_DEFAULT, quality);
                action.setLabel2(QUALITIES[quality]);
                break;
            case R.id.action_hardware_acceleration:
                int hardwareaccel = PrefUtils.get(getActivity(), Prefs.HW_ACCELERATION, 0);
                if(hardwareaccel == HW_ACCEL.length - 1) {
                    hardwareaccel = 0;
                } else {
                    hardwareaccel++;
                }
                PrefUtils.save(getActivity(), Prefs.HW_ACCELERATION, hardwareaccel);
                action.setLabel2(getCurrentHardwareAccel());
                break;
            case R.id.action_pixel_format:
                String pixelFormat = PrefUtils.get(getActivity(), Prefs.PIXEL_FORMAT, "");
                int pixelFormatInt = 1;
                if(pixelFormat.equals("YV12")) {
                    pixelFormatInt = 2;
                } else if(pixelFormat.equals("RV16")) {
                    pixelFormatInt = 0;
                }

                if(pixelFormatInt == PIXEL_FORMATS.length - 1) {
                    pixelFormatInt = 0;
                } else {
                    pixelFormatInt++;
                }

                String result = "";
                if(pixelFormatInt == 2) {
                    result = "YV12";
                } else if (pixelFormatInt == 0) {
                    result = "RV16";
                }

                PrefUtils.save(getActivity(), Prefs.PIXEL_FORMAT, result);
                action.setLabel2(getCurrentPixelFormat());
                break;
            case R.id.action_auto_updates:
                Boolean autoUpdates = PrefUtils.get(getActivity(), Prefs.AUTOMATIC_UPDATES, true);
                action.setChecked(!autoUpdates);
                action.setLabel2(getString(!autoUpdates ? R.string.enabled : R.string.disabled));
                PrefUtils.save(getActivity(), Prefs.AUTOMATIC_UPDATES, !autoUpdates);
                break;
            case R.id.action_check_updates:
                PopcornUpdater.getInstance(getActivity()).checkUpdatesManually();
                action.setLabel2(getLastUpdateCheck());
                break;
        }
        mActions.set(currentPos, action);
        setActions(mActions);
        setSelectedActionPosition(currentPos);
    }

    private String getLastUpdateCheck() {
        long timeStamp = Long.parseLong(PrefUtils.get(getActivity(), PopcornUpdater.LAST_UPDATE_CHECK, "0"));
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timeStamp);
        String time = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.getDefault()).format(timeStamp);
        String date = DateFormat.format("dd-MM-yyy", cal).toString();
        return getString(R.string.last_check) + ": " + date + " " + time;
    }

    private String getCurrentAppLanguage() {
        String language = getString(R.string.device_language);
        String langCode = PrefUtils.get(getActivity(), Prefs.LOCALE, "");

        if(!langCode.isEmpty()) {
            Locale locale = LocaleUtils.toLocale(langCode);
            language = locale.getDisplayName(locale);
        }
        return language;
    }

    private String getCurrentDefaultView() {
        int defaultView = PrefUtils.get(getActivity(), Prefs.DEFAULT_VIEW, 0);
        return DEFAULT_VIEW_ITEMS[defaultView];
    }

    private String getCurrentDefaultQuality() {
        int quality = PrefUtils.get(getActivity(), Prefs.QUALITY_DEFAULT, 0);
        return QUALITIES[quality];
    }

    private String getCurrentAppVersion() {
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            return packageInfo.versionName + " - " + Build.CPU_ABI;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "?.? (?) - ?";
    }

    private String getCurrentHardwareAccel() {
        switch (PrefUtils.get(getActivity(), Prefs.HW_ACCELERATION, LibVLC.HW_ACCELERATION_AUTOMATIC)) {
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

    private String getCurrentPixelFormat() {
        String currentValue = PrefUtils.get(getActivity(), Prefs.PIXEL_FORMAT, "");
        int current = 1;
        if (currentValue.equals("YV12")) {
            current = 2;
        } else if (currentValue.equals("RV16")) {
            current = 0;
        }

        return PIXEL_FORMATS[current];
    }

}
