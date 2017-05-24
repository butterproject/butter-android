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

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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
import android.support.v17.leanback.widget.PresenterSelector;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.utils.ThreadUtils;
import butter.droid.base.utils.VersionUtils;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import javax.inject.Inject;

public abstract class TVBaseDetailsFragment extends DetailsSupportFragment implements MediaProvider.Callback,
        OnActionClickedListener {

    @Inject Picasso picasso;

    private final DetailsSupportFragmentBackgroundController detailsBackground =
            new DetailsSupportFragmentBackgroundController(this);

    public static final String EXTRA_ITEM = "item";

    private ArrayObjectAdapter adapter;
    private ClassPresenterSelector presenterSelector;

    private Media item;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        detailsBackground.enableParallax();
        item = getArguments().getParcelable(EXTRA_ITEM);

        setupAdapter();
        setupDetailsOverviewRowPresenter();

        final DetailsOverviewRow detailRow = createDetailsOverviewRow(item.image);
        adapter.add(detailRow);

        loadDetails();

        setAdapter(adapter);
    }

    protected abstract void loadDetails();

    protected abstract AbstractDetailsDescriptionPresenter getDetailPresenter();

    protected abstract void onDetailLoaded();

    protected ArrayObjectAdapter getObjectArrayAdapter() {
        return adapter;
    }

    protected Media getMediaItem() {
        return item;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupAdapter() {
        presenterSelector = new ClassPresenterSelector();
        createPresenters(presenterSelector);

        adapter = createAdapter(presenterSelector);
    }

    protected abstract ClassPresenterSelector createPresenters(ClassPresenterSelector selector);

    protected ArrayObjectAdapter createAdapter(PresenterSelector selector) {
        return new ArrayObjectAdapter(selector);
    }

    private void setupDetailsOverviewRowPresenter() {
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

    private DetailsOverviewRow createDetailsOverviewRow(String imageUrl) {
        final DetailsOverviewRow detailsRow = new DetailsOverviewRow(item);

        picasso.load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                detailsRow.setImageBitmap(getActivity(), bitmap);
                adapter.notifyArrayItemRangeChanged(0, adapter.size());

            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (VersionUtils.isLollipop()) {
                    getActivity().startPostponedEnterTransition();
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
        return detailsRow;
    }

    protected void addAction(Action action) {
        DetailsOverviewRow detailRow = (DetailsOverviewRow) adapter.get(0);
        detailRow.addAction(action);
    }

    protected abstract void addActions(Media item);

    @Override
    public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
        if (!isAdded()) {
            return;
        }

        if (null == items || items.size() == 0) {
            return;
        }

        final Media itemDetail = items.get(0);

        item = itemDetail;

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override public void run() {
                final DetailsOverviewRow detailRow = createDetailsOverviewRow(itemDetail.image);
                adapter.replace(0, detailRow);
                onDetailLoaded();
            }
        });
    }

    @Override
    public void onFailure(Exception e) {
        //todo: on load failure
    }

}
