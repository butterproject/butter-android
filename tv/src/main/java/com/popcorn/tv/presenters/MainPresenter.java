package com.popcorn.tv.presenters;

import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import com.popcorn.tv.Movie;
import com.popcorn.tv.MovieList;
import com.popcorn.tv.R;
import com.popcorn.tv.interactors.MainInteractor;
import com.popcorn.tv.interfaces.main.MainInteractorInputInterface;
import com.popcorn.tv.interfaces.main.MainInteractorOutputInterface;
import com.popcorn.tv.interfaces.main.MainPresenterInputInterface;
import com.popcorn.tv.interfaces.main.MainRouterInputInterface;
import com.popcorn.tv.interfaces.main.MainViewInputInterface;
import com.popcorn.tv.routers.MainRouter;
import com.popcorn.tv.utils.Capitalize;

import java.util.Collections;
import java.util.List;

public class MainPresenter implements MainPresenterInputInterface, MainInteractorOutputInterface
{
    //region Attributes
    private MainViewInputInterface view;
    private MainRouterInputInterface router;
    private MainInteractorInputInterface interactor;
    private ArrayObjectAdapter mediaAdapter;
    private MediaRowPresenter mediaRowPresenter;

    //endregion

    //region Constructors
    public MainPresenter(MainViewInputInterface view) {
        this.view = view;
        this.router = new MainRouter();
        this.interactor = new MainInteractor(this);
    }
    //endregion

    //region Setup
    private void setupAdapters() {
        List<Movie> list = MovieList.setupMovies();
        mediaAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mediaRowPresenter = new MediaRowPresenter();
        int i;
        for (i = 0; i < view.getContext().getResources().getStringArray(R.array.categories).length; i++) {
            if (i != 0) {
                Collections.shuffle(list);
            }
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mediaRowPresenter);
            for (int j = 0; j < view.getContext().getResources().getInteger(R.integer.max_columns); j++) {
                listRowAdapter.add(list.get(j % 5));
            }
            HeaderItem header = new HeaderItem(i, Capitalize.capitalize(view.getContext().getResources().getStringArray(R.array.categories)[i]), null);
            mediaAdapter.add(new ListRow(header, listRowAdapter));
        }

        HeaderItem gridHeader = new HeaderItem(i, "PREFERENCES", null);

        BasicRowPresenter mGridPresenter = new BasicRowPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(view.getContext().getResources().getString(R.string.grid_view));
        gridRowAdapter.add(view.getContext().getResources().getString(R.string.send_feeback));
        gridRowAdapter.add(view.getContext().getResources().getString(R.string.personal_settings));
        mediaAdapter.add(new ListRow(gridHeader, gridRowAdapter));
        view.setAdapter(mediaAdapter);
    }
    //endregion

    //region MainPresenterInputInterface
    @Override
    public void onViewCreated() {
        setupAdapters();
    }

    @Override
    public void userDidSelectMedia(Object object)
    {
//        if (item instanceof Movie) {
//            //TODO - Decide what to do here too
//            // backgroundUri = ((Movie) item).getBackgroundImageURI();
//        }
//        startBackgroundTimer();
    }

    @Override
    public void userDidClickMedia(Object object) {
        //TODO - Decide what to do here
//                if (item instanceof Movie) {
//                    Movie movie = (Movie) item;
//                    Log.d(TAG, "Item: " + item.toString());
//                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
//                    intent.putExtra(getString(R.string.movie), movie);
//                    startActivity(intent);
//                } else if (item instanceof String) {
//                    Toast.makeText(getActivity(), (String) item, Toast.LENGTH_SHORT)
//                            .show();
//                }
    }
    //endregion
}
