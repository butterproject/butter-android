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

package butter.droid.ui.loading.fragment;

import android.content.Intent;
import androidx.annotation.NonNull;

import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragmentView;

public interface StreamLoadingFragmentView extends BaseStreamLoadingFragmentView {

    void loadBackgroundImage(String url);

    void pickTorrentFile(String[] fileNames);

    void startBeamActivity(StreamInfo streamInfo, int resumePosition);

    void closeSelf();

    void startExternalPlayer(@NonNull Intent intent);

    void startPlayerActivity(StreamInfo streamInfo, int resumePosition);

    void showExternalPlayerButton();
}
