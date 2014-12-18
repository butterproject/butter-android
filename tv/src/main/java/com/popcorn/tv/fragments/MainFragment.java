package com.popcorn.tv.fragments;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseSupportFragment;
import android.support.v17.leanback.widget.OnItemClickedListener;
import android.support.v17.leanback.widget.OnItemSelectedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.popcorn.tv.interfaces.main.MainPresenterInputInterface;
import com.popcorn.tv.interfaces.main.MainViewInputInterface;
import com.popcorn.tv.presenters.MainPresenter;
import com.popcorn.tv.utils.PicassoBackgroundManagerTarget;
import com.popcorn.tv.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MainFragment extends BrowseSupportFragment implements MainViewInputInterface, OnItemViewSelectedListener {
    //region Attributes
    private static final String TAG = "MainFragment";
    private DisplayMetrics metrics;
    private Drawable backgroundDefaultDrawable;
    private Target backgroundImageTarget;
    private Timer backgroundTimer;
    private URI backgroundUri;
    private final Handler mainThreadHandler = new Handler();
    private MainPresenterInputInterface presenter;
    //endregion

    //region Fragment Lifecycle
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);
        setupUIElements();
        setupBackgroundManager();
        setupPresenter();
        setupEventListeners();
        presenter.onViewCreated();
    }
    //endregion

    //region Setup
    private void setupUIElements() {
        setBadgeDrawable(getActivity().getResources().getDrawable(R.drawable.header_logo)); //TODO - Add the Popcorn Time icon
        setTitle(getString(R.string.browse_title));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setOnItemViewSelectedListener(this);
        setBrandColor(getResources().getColor(R.color.bg));
        getView().setBackgroundColor(getResources().getColor(R.color.default_background));
        setSearchAffordanceColor(getResources().getColor(R.color.primary));
    }

    private void setupBackgroundManager() {

        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        backgroundImageTarget = new PicassoBackgroundManagerTarget(backgroundManager);
        backgroundDefaultDrawable = getResources().getDrawable(R.drawable.default_background);
        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    private void setupPresenter() {
        presenter = new  MainPresenter(this);
    }

    private void setupEventListeners() {
        setOnItemSelectedListener(getDefaultItemSelectedListener());
        setOnItemClickedListener(getDefaultItemClickedListener());
        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO - Decide what to do once the search button is selected
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
    //endregion

    //region MainViewInputInterface
    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void setBackgroundWithUri(String uri) {

    }
    //endregion

    //region Listeners
    protected OnItemSelectedListener getDefaultItemSelectedListener() {
        return new OnItemSelectedListener() {
            @Override
            public void onItemSelected(Object item, Row row) {
                presenter.userDidSelectMedia(item);
            }
        };
    }

    protected OnItemClickedListener getDefaultItemClickedListener() {
        return new OnItemClickedListener() {
            @Override
            public void onItemClicked(Object item, Row row) {
                presenter.userDidClickMedia(item);
            }
        };
    }
    //endregion

    //region Background Image
    protected void setDefaultBackground(Drawable background) {
        backgroundDefaultDrawable = background;
    }

    protected void setDefaultBackground(int resourceId) {
        backgroundDefaultDrawable = getResources().getDrawable(resourceId);
    }

    protected void updateBackground(URI uri) {
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(metrics.widthPixels, metrics.heightPixels)
                .centerCrop()
                .error(backgroundDefaultDrawable)
                .into(backgroundImageTarget);
    }

    protected void updateBackground(Drawable drawable) {
        BackgroundManager.getInstance(getActivity()).setDrawable(drawable);
    }

    protected void clearBackground() {
        BackgroundManager.getInstance(getActivity()).setDrawable(backgroundDefaultDrawable);
    }

    private void startBackgroundTimer() {
        if (null != backgroundTimer) {
            backgroundTimer.cancel();
        }
        backgroundTimer = new Timer();
        backgroundTimer.schedule(new UpdateBackgroundTask(), 300);
    }

    private class UpdateBackgroundTask extends TimerTask {
        @Override
        public void run() {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (backgroundUri != null) {
                        updateBackground(backgroundUri);
                    }
                }
            });

        }
    }
    //endregion

    //region - OnItemViewSelectedListener
    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        presenter.userDidSelectItem(item, row);
    }
    //endregion
}
