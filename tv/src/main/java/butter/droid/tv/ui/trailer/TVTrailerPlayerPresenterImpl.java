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

package butter.droid.tv.ui.trailer;

import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.phone.PhoneManager;
import butter.droid.base.manager.youtube.YouTubeManager;
import butter.droid.base.ui.trailer.BaseTrailerPlayerPresenterImpl;
import butter.droid.base.ui.trailer.BaseTrailerPlayerView;

public class TVTrailerPlayerPresenterImpl extends BaseTrailerPlayerPresenterImpl implements TVTrailerPlayerPresenter {

    public TVTrailerPlayerPresenterImpl(final BaseTrailerPlayerView view, final YouTubeManager youTubeManager, final NetworkManager
            networkManager, final PhoneManager phoneManager) {
        super(view, youTubeManager, networkManager, phoneManager);
    }
}
