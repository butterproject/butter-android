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

package butter.droid.base.providers.media;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import butter.droid.base.R;
import butter.droid.base.providers.BaseProvider;
import butter.droid.base.providers.media.models.Genre;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.nav.NavItem;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * MediaProvider.java
 * <p/>
 * Base class for all media providers. Any media providers has to extend this class and use the callback defined here.
 */
public abstract class MediaProvider extends BaseProvider {

    @Nullable private final SubsProvider subsProvider;

    public MediaProvider(OkHttpClient client, Gson gson, @Nullable SubsProvider subsProvider) {
        super(client, gson);
        this.subsProvider = subsProvider;
    }

    /**
     * Get a list of Media items from the provider
     *
     * @param filters  Filters the provider can use to sort or search
     * @param callback MediaProvider callback
     */
    public Call getList(Filters filters, Callback callback) {
        return getList(null, filters, callback);
    }

    /**
     * Get a list of Media items from the provider
     *
     * @param currentList Input the current list so it can be extended
     * @param filters     Filters the provider can use to sort or search
     * @param callback    MediaProvider callback
     * @return Call
     */
    public abstract Call getList(ArrayList<Media> currentList, Filters filters, Callback callback);

    public abstract Call getDetail(ArrayList<Media> currentList, Integer index, Callback callback);

    public abstract List<NavInfo> getNavigation();

    public int getDefaultNavigationIndex() {
        return 1;
    }

    public List<Genre> getGenres() {
        return new ArrayList<>();
    }

    public SubsProvider getSubsProvider() {
        return subsProvider;
    }

    public boolean hasSubsProvider() {
        return subsProvider != null;
    }

    public interface Callback {
        void onSuccess(Filters filters, ArrayList<Media> items, boolean changed);

        void onFailure(Exception e);
    }

    public static class Filters {
        public enum Order {ASC, DESC}
        public enum Sort {POPULARITY, YEAR, DATE, RATING, ALPHABET, TRENDING}

        public String keywords = null;
        public String genre = null;
        public Order order = Order.DESC;
        public Sort sort = Sort.POPULARITY;
        public Integer page = null;
        public String langCode = "en";

        public Filters() { }

        public Filters(Filters filters) {
            keywords = filters.keywords;
            genre = filters.genre;
            order = filters.order;
            sort = filters.sort;
            page = filters.page;
            langCode = filters.langCode;
        }
    }

    public static class NavInfo {

        @IdRes private final int id;
        @DrawableRes private final int icon;
        @StringRes private final int label;
        @Nullable private final Filter filter;
        private final int providerId;

        public NavInfo(@NonNull NavItem item, final int providerId) {
            this.id = R.id.nav_item_filter;
            this.icon = item.getIcon();
            this.label = item.getLabel();
            this.providerId = providerId;
            this.filter = item.getFilter();
        }

        public NavInfo(@IdRes final int id, @DrawableRes final int icon, @StringRes final int label, final int providerId) {
            if (id == R.id.nav_item_filter) {
                throw new IllegalStateException("Filter items have to have filter parameter set");
            }

            this.id = id;
            this.icon = icon;
            this.label = label;
            this.providerId = providerId;
            this.filter = null;
        }

        public int getId() {
            return id;
        }

        @DrawableRes
        public int getIcon() {
            return icon;
        }

        @StringRes
        public int getLabel() {
            return label;
        }

        public int getProviderId() {
            return providerId;
        }

        @Nullable public Filter getFilter() {
            return filter;
        }
    }

}
