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

import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.MediaProvider.Callback;
import butter.droid.base.providers.media.MediaProvider.Filters;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.utils.ThreadUtils;
import java.util.ArrayList;
import okhttp3.Call;

public class TVBaseDetailsPresenterImpl implements TVBaseDetailsPresenter, Callback {

        protected static final int ACTION_TRAILER = -1;

    private final TVBaseDetailView view;
    private final ProviderManager providerManager;

    private Media item;

    @Nullable private Call detailsCall;

    public TVBaseDetailsPresenterImpl(final TVBaseDetailView view, final ProviderManager providerManager) {
        this.view = view;
        this.providerManager = providerManager;
    }


    @CallSuper protected void onCreate(final Media item) {
        this.item = item;

        view.initData(item);
        loadDetails();
    }

    @Override @CallSuper public void onDestroy() {
        if (detailsCall != null) {
            detailsCall.cancel();
        }
    }

    @Override public void actionClicked(final long actionId) {
        // override if needed
    }

    @Override public void onSuccess(final Filters filters, final ArrayList<Media> items, final boolean changed) {
        if (null == items || items.size() == 0) {
            return;
        }

        final Media itemDetail = items.get(0);

        item = itemDetail;

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override public void run() {
                detailsLoaded(itemDetail);
            }
        });
    }

    @Override public void onFailure(final Exception e) {
        // TODO: 5/25/17 Show error message
    }

    @CallSuper @MainThread protected void detailsLoaded(Media media) {
        view.updateOverview(media);
    }

    private void loadDetails() {
        ArrayList<Media> mediaList = new ArrayList<>();
        mediaList.add(item);

        detailsCall = providerManager.getCurrentMediaProvider().getDetail(mediaList, 0, this);
    }

}
