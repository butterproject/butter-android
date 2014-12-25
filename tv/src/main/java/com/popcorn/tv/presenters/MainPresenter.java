package com.popcorn.tv.presenters;

import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.Row;
import com.popcorn.tv.interactors.MainInteractor;
import com.popcorn.tv.interfaces.main.MainInteractorInputInterface;
import com.popcorn.tv.interfaces.main.MainInteractorOutputInterface;
import com.popcorn.tv.interfaces.main.MainPresenterInputInterface;
import com.popcorn.tv.interfaces.main.MainRouterInputInterface;
import com.popcorn.tv.interfaces.main.MainViewInputInterface;
import com.popcorn.tv.models.MainMedia;
import com.popcorn.tv.routers.MainRouter;
import com.popcorn.tv.utils.MediaListRow;

public class MainPresenter implements MainPresenterInputInterface, MainInteractorOutputInterface
{
    //region Attributes
    private MainViewInputInterface view;
    private MainRouterInputInterface router;
    private MainInteractorInputInterface interactor;
    private ArrayObjectAdapter mediaAdapter;
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
        mediaAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        for (int i=0; i < interactor.getNumberOfSections(view.getContext()); i++) {
            MediaListRow row = new MediaListRow(interactor.getSectionHeaderAtIndex(i, view.getContext()), interactor.getSectionAdapterAtIndex(i, view.getContext()));
            row.setRowIndex(i);
            mediaAdapter.add(row);
        }
        view.setAdapter(mediaAdapter);
    }
    //endregion

    //region MainPresenterInputInterface
    @Override
    public void onViewCreated() {
        setupAdapters();
        interactor.synchronize(view.getContext());
    }

    @Override
    public void userDidSelectItem(Object item, Row row)
    {
        if (!(row instanceof MediaListRow)) return;
        if (item instanceof MainMedia) {
            view.setBackgroundWithUri(((MainMedia)item).headerImage);
        }
        int rightItems = interactor.getRightItemsNextTo(item, (MediaListRow)row);
        if (rightItems > 2) { return; }
        //interactor.getMore((MediaListRow)row); //TODO
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
