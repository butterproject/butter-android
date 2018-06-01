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

package butter.droid.ui.media.detail.streamable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;

import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.provider.base.model.Torrent;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.ui.media.detail.dialog.quality.model.UiQuality;

public interface StreamableDetailView {
    void initLayout(MediaWrapper movie);

    void renderHealth(Torrent torrent);

    void updateMagnet(Torrent torrent);

    void showReadMoreDialog(String synopsis);

    void hideRating();

    void displayRating(int rating);

    void displayMetaData(CharSequence metaData);

    void displaySynopsis(String synopsis);

    void hideSynopsis();

    void setSubtitleText(@StringRes int subtitleText);

    void setSubtitleText(String subtitleText);

    void displaySubsPicker(@NonNull MediaWrapper mediaWrapper, @Nullable Subtitle subtitle);

    void displayQuality(String quality);

    void hideDialog();

    void displayQualityPicker(ArrayList<UiQuality> qualities);

    void subtitleVisibility(boolean visible);
}
