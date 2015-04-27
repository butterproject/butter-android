package pct.droid.tv.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.text.format.DateFormat;
import android.view.View;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pct.droid.base.preferences.Prefs;
import pct.droid.base.updater.PopcornUpdater;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.R;

public class PTVSettingsFragment extends GuidedStepFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Field f = null; //NoSuchFieldException
//        try {
//            f = this.getClass().getDeclaredField("mAdapter");
//            f.setAccessible(true);
//            adapter = (RecyclerView.Adapter) f.get(this); //IllegalAccessException
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }

    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.preferences), null, getString(R.string.app_name), null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {


//        GuidedAction generalSection = new GuidedAction.Builder().id(R.id.action_general).infoOnly(true).title(getString(R.string.genera)).build();

//
        //subtitle
//        actions.add(new GuidedAction.Builder().id(R.id.action_subtitles).infoOnly(true).title(getString(R.string.subtitles)).build());
//        actions.add(new GuidedAction.Builder().id(R.id.action_subtitle_color).description(StringUtils.colorToString(Color.WHITE)).icon(getActivity().getDrawable(R.drawable.ic_prefs_subtitle_color)).title(getString(R.string.subtitle_color)).build());

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

//        mPrefItems.add(new PrefItem(this, R.drawable.ic_prefs_check_update, R.string.check_for_updates, PopcornUpdater.LAST_UPDATE_CHECK, 1,
//                new PrefItem.OnClickListener() {
//                    @Override
//                    public void onClick(PrefItem item) {
//                        PopcornUpdater.getInstance(PreferencesActivity.this).checkUpdatesManually();
//                    }
//                },
//                new PrefItem.SubTitleGenerator() {
//                    @Override
//                    public String get(PrefItem item) {
//                        long timeStamp = Long.parseLong(PrefUtils.get(PreferencesActivity.this, PopcornUpdater.LAST_UPDATE_CHECK, "0"));
//                        Calendar cal = Calendar.getInstance(Locale.getDefault());
//                        cal.setTimeInMillis(timeStamp);
//                        String time = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.getDefault()).format(timeStamp);
//                        String date = DateFormat.format("dd-MM-yyy", cal).toString();
//                        return getString(R.string.last_check) + " :" + date + " " + time;
//                    }
//                }));


        super.onCreateActions(actions, savedInstanceState);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        switch ((int) action.getId()) {
            case R.id.action_auto_updates:
                Boolean autoUpdates = PrefUtils.get(getActivity(), Prefs.AUTOMATIC_UPDATES, true);
                action.setChecked(!autoUpdates);
                action.setLabel2(getString(!autoUpdates ? R.string.enabled : R.string.disabled));
                PrefUtils.save(getActivity(), Prefs.AUTOMATIC_UPDATES, !autoUpdates);
                break;
            case R.id.action_check_updates:
                PopcornUpdater.getInstance(getActivity()).checkUpdatesManually();
                //todo: update value
                break;
        }
//        if (action.getId() == ACTION_ACCEPT) {
//            //set first run flag to false, don't show welcome again
//            PrefUtils.save(getActivity(), Prefs.FIRST_RUN, false);
//            //start main activity
//
//            PTVMainActivity.startActivity(getActivity());
//            getActivity().finish();
//            return;
//        } else if (action.getId() == ACTION_DECLINE) {
//            getActivity().finish();
//            return;
//        }
        super.onGuidedActionClicked(action);
    }

    private String getLastUpdateCheck() {
        long timeStamp = Long.parseLong(PrefUtils.get(getActivity(), PopcornUpdater.LAST_UPDATE_CHECK, "0"));
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timeStamp);
        String time = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.getDefault()).format(timeStamp);
        String date = DateFormat.format("dd-MM-yyy", cal).toString();
        return getString(R.string.last_check) + " :" + date + " " + time;
    }

}
