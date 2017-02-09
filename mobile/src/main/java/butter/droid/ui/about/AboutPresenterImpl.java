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

package butter.droid.ui.about;

import android.support.annotation.IdRes;

import butter.droid.R;
import butter.droid.base.ui.about.BaseAboutPresenterImpl;

public class AboutPresenterImpl extends BaseAboutPresenterImpl implements AboutPresenter {

    public AboutPresenterImpl(AboutView view) {
        super(view);
    }

    @Override public void aboutButtonClicked(@IdRes int id) {
        @AboutLinks int link;
        switch (id) {
            case R.id.logo_imageview:
            case R.id.butter_button:
                link = ABOUT_BUTTER;
                break;
            case R.id.facebook_button:
                link = ABOUT_FB;
                break;
            case R.id.git_button:
                link = ABOUT_GIT;
                break;
            case R.id.blog_button:
                link = ABOUT_BLOG;
                break;
            case R.id.discuss_button:
                link = ABOUT_DISCUSS;
                break;
            case R.id.twitter_button:
                link = ABOUT_TWITTER;
                break;
            default:
                throw new IllegalArgumentException("Unknown about button");
        }

        displayPage(link);
    }
}
