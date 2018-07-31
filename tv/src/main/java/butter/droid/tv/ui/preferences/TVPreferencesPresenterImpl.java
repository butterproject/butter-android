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

package butter.droid.tv.ui.preferences;

import android.content.Context;
import android.content.res.Resources;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.leanback.widget.GuidedAction;
import butter.droid.base.content.preferences.PrefItem;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.ui.preferences.BasePreferencesPresenterImpl;

public class TVPreferencesPresenterImpl extends BasePreferencesPresenterImpl implements TVPreferencesPresenter {

    private final TVPreferencesView view;
    private final Context context;
    private final PreferencesHandler preferencesHandler;

    public TVPreferencesPresenterImpl(TVPreferencesView view, Context context, PreferencesHandler preferencesHandler,
            Resources resources, PrefManager prefManager, PlayerManager playerManager) {
        super(view, prefManager, playerManager, preferencesHandler, resources, true);
        this.view = view;
        this.context = context;
        this.preferencesHandler = preferencesHandler;
    }

    @Override public void createActions(@NonNull List<GuidedAction> actions) {
        for (int i = 0; i < keys.length; i++) {
            //noinspection WrongConstant
            PrefItem item = preferencesHandler.getPreferenceItem(keys[i]);
            actions.add(generateAction(i, item));
        }

    }

    @Override public void itemSelected(int position) {
        updateItem(preferencesHandler.getPreferenceItem(keys[position]));
    }

    private GuidedAction generateAction(long id, PrefItem item) {
        GuidedAction.Builder builder = new GuidedAction.Builder(context)
                .id(id)
                .hasNext(item.hasNext())
                .enabled(item.isClickable())
                .focusable(item.isClickable())
                .infoOnly(item.isTitle() || !item.isClickable())
                .title(item.getTitleRes());

        if (!item.isTitle()) {
            builder.description(item.getSubtitle());
            if (item.getValue() instanceof Boolean) {
                builder.checked((Boolean) item.getValue());
            }
        }

        return builder.build();
    }

    @Override protected void updateDisplayItem(int position, PrefItem prefItem) {
        view.updateAction(position, generateAction(position, prefItem));
    }
}
