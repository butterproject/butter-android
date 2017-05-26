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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import butter.droid.base.providers.media.models.Media;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import javax.inject.Inject;

public abstract class TVBaseDetailsFragment extends DetailsSupportFragment implements TVBaseDetailView, OnActionClickedListener {

    protected static final String EXTRA_ITEM = "butter.droid.tv.ui.detail.base.TVBaseDetailsFragment.item";

    @Inject TVBaseDetailsPresenter presenter;
    @Inject Picasso picasso;

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
        picasso.cancelRequest(detailsImageTarget);
    }

    @Override public void initData(final Media item) {
        presenterSelector = new ClassPresenterSelector();
        createPresenters(presenterSelector);

        adapter = new ArrayObjectAdapter(presenterSelector);

        setupDetailsOverviewRowPresenter(item);

        detailsRow = new DetailsOverviewRow(item);
        adapter.add(detailsRow);

        setAdapter(adapter);
    }

    @Override public void updateOverview(final Media media) {
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

    protected abstract AbstractDetailsDescriptionPresenter getDetailPresenter();

    protected ArrayObjectAdapter getObjectArrayAdapter() {
        return adapter;
    }

    protected abstract ClassPresenterSelector createPresenters(ClassPresenterSelector selector);

    private void setupDetailsOverviewRowPresenter(Media item) {
        // Set detail background and style.
        FullWidthDetailsOverviewRowPresenter detailsPresenter = new FullWidthDetailsOverviewRowPresenter(getDetailPresenter());
        detailsPresenter.setBackgroundColor(item.color);
        detailsPresenter.setOnActionClickedListener(this);

        final FullWidthDetailsOverviewSharedElementHelper sharedElementHelper = new FullWidthDetailsOverviewSharedElementHelper();
        sharedElementHelper.setSharedElementEnterTransition(getActivity(), TVMediaDetailActivity.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(sharedElementHelper);
        detailsPresenter.setParticipatingEntranceTransition(false);

        presenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
        presenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    private void updateDetailImage(final Media item) {
        picasso.load(item.image)
                .into(detailsImageTarget);
    }

    protected void addAction(Action action) {
        ((ArrayObjectAdapter) detailsRow.getActionsAdapter())
                .add(action);
    }

    private final Target detailsImageTarget = new Target() {
        @Override public void onBitmapLoaded(final Bitmap bitmap, final LoadedFrom from) {
            detailsRow.setImageBitmap(getActivity(), bitmap);
        }

        @Override public void onBitmapFailed(final Drawable errorDrawable) {
            // nothing to do
        }

        @Override public void onPrepareLoad(final Drawable placeHolderDrawable) {
            // nothing to do
        }
    };

}
