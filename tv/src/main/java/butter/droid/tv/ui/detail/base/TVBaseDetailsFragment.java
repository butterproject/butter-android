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

package butter.droid.tv.ui.detail.base;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v17.leanback.app.DetailsSupportFragment;
import android.support.v17.leanback.app.DetailsSupportFragmentBackgroundController;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import javax.inject.Inject;

import butter.droid.base.manager.internal.glide.GlideApp;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.tv.presenters.MediaDetailsDescriptionPresenter;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;

public abstract class TVBaseDetailsFragment extends DetailsSupportFragment implements TVBaseDetailView, OnActionClickedListener {

    protected static final String EXTRA_ITEM = "butter.droid.tv.ui.detail.base.TVBaseDetailsFragment.item";

    @Inject TVBaseDetailsPresenter presenter;

    private final DetailsSupportFragmentBackgroundController detailsBackground =
            new DetailsSupportFragmentBackgroundController(this);

    private ArrayObjectAdapter adapter;
    private ClassPresenterSelector presenterSelector;
    private DetailsOverviewRow detailsRow;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        detailsBackground.enableParallax();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        presenter.onDestroy();
        GlideApp.with(this).clear(detailsImageTarget);
    }

    @Override public void initData(final MediaWrapper item) {
        presenterSelector = new ClassPresenterSelector();
        populatePresenterSelector(presenterSelector);

        adapter = new ArrayObjectAdapter(presenterSelector);

        setupDetailsOverviewRowPresenter(item);

        detailsRow = new DetailsOverviewRow(item);
        adapter.add(detailsRow);

        setAdapter(adapter);
    }

    @Override public void updateOverview(final MediaWrapper media) {
        detailsRow.setItem(media);
        adapter.notifyArrayItemRangeChanged(0, 1);
        updateDetailImage(media);
    }

    @Override public void onActionClicked(final Action action) {
        presenter.actionClicked(action.getId());
    }

    @Override public void addAction(final int id, @StringRes final int text1, @StringRes final int text2) {
        addAction(id, text1, getString(text2));
    }

    @Override public void addAction(final int id, @StringRes final int text1, final String text2) {
        addAction(new Action(id, getString(text1), text2));
    }

    protected void addAction(Action action) {
        ((ArrayObjectAdapter) detailsRow.getActionsAdapter())
                .add(action);
    }

    protected AbstractDetailsDescriptionPresenter getDetailPresenter() {
        return new MediaDetailsDescriptionPresenter();
    }

    protected ArrayObjectAdapter getObjectArrayAdapter() {
        return adapter;
    }

    protected void populatePresenterSelector(ClassPresenterSelector selector) {
        // override if needed
    }

    private void setupDetailsOverviewRowPresenter(MediaWrapper item) {
        // Set detail background and style.
        FullWidthDetailsOverviewRowPresenter detailsPresenter = new FullWidthDetailsOverviewRowPresenter(getDetailPresenter());

        if (item.hasColor()) {
            detailsPresenter.setBackgroundColor(item.getColor());
        }

        detailsPresenter.setOnActionClickedListener(this);

        final FullWidthDetailsOverviewSharedElementHelper sharedElementHelper = new FullWidthDetailsOverviewSharedElementHelper();
        sharedElementHelper.setSharedElementEnterTransition(getActivity(), TVMediaDetailActivity.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(sharedElementHelper);
        detailsPresenter.setParticipatingEntranceTransition(false);

        presenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
        presenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    private void updateDetailImage(final MediaWrapper item) {
        GlideApp.with(this)
                .asBitmap()
                .load(item.getMedia().getPoster())
                .into(detailsImageTarget);
    }

    private final Target<Bitmap> detailsImageTarget = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            detailsRow.setImageBitmap(requireActivity(), resource);
        }
    };

}
