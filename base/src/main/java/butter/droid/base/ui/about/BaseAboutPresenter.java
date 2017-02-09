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

package butter.droid.base.ui.about;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface BaseAboutPresenter {

    // region IntDef

    @IntDef({ABOUT_BUTTER, ABOUT_FB, ABOUT_GIT, ABOUT_BLOG, ABOUT_DISCUSS, ABOUT_TWITTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AboutLinks {
    }

    int ABOUT_BUTTER = 0;
    int ABOUT_FB = 1;
    int ABOUT_GIT = 2;
    int ABOUT_BLOG = 3;
    int ABOUT_DISCUSS = 4;
    int ABOUT_TWITTER = 5;

    // endregion IntDef

}
