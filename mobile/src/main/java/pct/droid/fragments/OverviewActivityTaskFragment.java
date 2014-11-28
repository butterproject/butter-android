package pct.droid.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;

import pct.droid.providers.media.MediaProvider;

/**
 * Created by Sebastiaan on 10-11-14.
 */
public class OverviewActivityTaskFragment extends Fragment implements MediaProvider.Callback {

    public final static String TAG = "pct.droid.overviewactivitytaskfragment";

    private MediaProvider.Callback mCallback;
    private Boolean mLoaded = false;
    private ArrayList<MediaProvider.Video> mItems;
    private int mPage = 1;
    private HashMap<String, String> mFilters = new HashMap<String, String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (MediaProvider.Callback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public ArrayList<MediaProvider.Video> getExistingItems() {
        return mItems;
    }

    public int getCurrentPage() {
        return mPage;
    }

    public void setCurrentPage(int page) {
        mPage = page;
    }

    public HashMap<String, String> getFilters() {
        return mFilters;
    }

    public void setFilters(HashMap<String, String> filters) {
        mFilters = filters;
    }

    @Override
    public void onSuccess(ArrayList<MediaProvider.Video> items) {
        mPage++;
        mItems = items;
        if(mCallback != null) mCallback.onSuccess(items);
    }

    @Override
    public void onFailure(Exception e) {
        if(mCallback != null) mCallback.onFailure(e);
    }
}
