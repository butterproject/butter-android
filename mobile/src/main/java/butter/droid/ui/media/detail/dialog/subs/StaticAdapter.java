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

package butter.droid.ui.media.detail.dialog.subs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public abstract class StaticAdapter<D, V extends View> {

    private final ViewGroup containerViewGroup;
    private final LayoutInflater layoutInflater;

    private final List<V> views;
    private List<D> data;

    private final Deque<V> unusedViewCache;

    public StaticAdapter(ViewGroup containerViewGroup) {
        this.containerViewGroup = containerViewGroup;
        this.layoutInflater = LayoutInflater.from(containerViewGroup.getContext());
        this.views = new ArrayList<>();
        this.unusedViewCache = new ArrayDeque<>();
    }

    public void setData(List<D> data) {
        this.data = data;

        if (data == null || data.isEmpty()) {
            prepareItemViews(0);
        } else {
            prepareItemViews(data.size());
            updateItemViews(data);
        }
    }

    public List<D> getData() {
        return data;
    }

    private void updateItemViews(List<D> data) {
        for (int i = 0; i < data.size(); i++) {
            D item = data.get(i);
            V view = views.get(i);
            bindView(view, item, i);
        }
    }

    private void prepareItemViews(int count) {
        ensureItemViews(count);

        for (int i = views.size() - 1; i >= count; i--) {
            V view = views.remove(i);
            containerViewGroup.removeView(view);
            unusedViewCache.push(view);
        }
    }

    private void ensureItemViews(int count) {
        if (views.size() < count) {
            for (int i = views.size(); i < count; i++) {
                V view;
                if (!unusedViewCache.isEmpty()) {
                    view = unusedViewCache.pop();
                } else {
                    view = createView(layoutInflater, containerViewGroup);
                }

                views.add(view);
                containerViewGroup.addView(view);
            }
        }
    }

    protected abstract V createView(LayoutInflater inflater, ViewGroup parent);

    protected abstract void bindView(V view, D item, int position);
}
