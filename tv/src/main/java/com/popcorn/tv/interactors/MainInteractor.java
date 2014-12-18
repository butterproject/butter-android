package com.popcorn.tv.interactors;

import android.content.Context;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.Row;

import com.popcorn.tv.Movie;
import com.popcorn.tv.MovieList;
import com.popcorn.tv.R;
import com.popcorn.tv.interfaces.main.MainInteractorInputInterface;
import com.popcorn.tv.interfaces.main.MainInteractorOutputInterface;
import com.popcorn.tv.presenters.BasicRowPresenter;
import com.popcorn.tv.presenters.MediaRowPresenter;
import com.popcorn.tv.utils.Capitalize;

import java.util.ArrayList;
import java.util.List;

public class MainInteractor implements MainInteractorInputInterface
{
    //region Attributes
    MainInteractorOutputInterface presenter;
    private List<ArrayObjectAdapter> adapters;
    private List<HeaderItem> headers;
    private List<Integer> activeRowsUpdates = new ArrayList<>();
    //endregion

    //region Constructors
    public MainInteractor(MainInteractorOutputInterface presenter)
    {
        this.presenter = presenter;
    }
    //endregion

    //region MainInteractorInputInterface
    @Override
    public int getNumberOfSections(Context context) {
        return context.getResources().getStringArray(R.array.categories).length;
    }

    @Override
    public HeaderItem getSectionHeaderAtIndex(int index, Context context) {

        return getHeaders(context).get(index);
    }

    @Override
    public ArrayObjectAdapter getSectionAdapterAtIndex(int index, Context context) {
        return getAdapters(context).get(index);
    }

    @Override
    public int getRightItemsNextTo(Object item, Row row) {
        //TODO
        return 0;
    }

    @Override
    public void getMore(Row row) {
        //TODO
    }
    //endregion

    //region Custom Getters
    private List<ArrayObjectAdapter> getAdapters(Context context) {
        if (adapters == null) {
            adapters = new ArrayList<>();
            addRowAdapters(context);
        }
        return adapters;
    }

    private List<HeaderItem> getHeaders(Context context) {
        if (headers == null) {
            headers = new ArrayList<>();
            addRowHeaders(context);
        }
        return headers;
    }
    //endregion

    //region Helpers
    private void addRowHeaders(Context context)
    {
        for (int i = 0; i < context.getResources().getStringArray(R.array.categories).length; i++) {
            addRowMediaHeader(context, i);
        }
        addRowSettingsHeader(context);
    }

    private void addRowSettingsHeader(Context context) {
        headers.add(new HeaderItem(context.getResources().getStringArray(R.array.categories).length, Capitalize.capitalize("settings"), null));
    }

    private void addRowMediaHeader(Context context, int index) {
        headers.add(new HeaderItem(index, Capitalize.capitalize(context.getResources().getStringArray(R.array.categories)[index]), null));
    }

    private void addRowAdapters(Context context)
    {
        MediaRowPresenter mediaRowPresenter = new MediaRowPresenter();
        for (int i = 0; i < context.getResources().getStringArray(R.array.categories).length; i++) {
            addRowMediaAdapter(context, i, mediaRowPresenter);
        }
        addRowSettingsAdapter(context);
    }

    private void addRowMediaAdapter(Context context, int index, MediaRowPresenter mediaRowPresenter)
    {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mediaRowPresenter);
        adapters.add(listRowAdapter);
    }

    private void addRowSettingsAdapter(Context context)
    {
        BasicRowPresenter mGridPresenter = new BasicRowPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(context.getResources().getString(R.string.grid_view));
        gridRowAdapter.add(context.getResources().getString(R.string.send_feeback));
        gridRowAdapter.add(context.getResources().getString(R.string.personal_settings));
        adapters.add(gridRowAdapter);
    }
    //end
}
