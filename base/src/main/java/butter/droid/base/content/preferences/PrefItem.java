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

package butter.droid.base.content.preferences;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import butter.droid.base.content.preferences.Prefs.PrefKey;

public class PrefItem {

    @DrawableRes private final int iconRes;
    @StringRes private final int titleRes;
    @PrefKey private final String prefKey;
    private final Object value;
    private final SubtitleGenerator subtitleGenerator;
    private final boolean hasNext;
    private final boolean clickable;

    public static Builder newBuilder() {
        return new Builder();
    }

    protected PrefItem(int iconRes, int titleRes, String prefKey, Object value, SubtitleGenerator subtitleGenerator,
            boolean hasNext, boolean clickable) {
        this.iconRes = iconRes;
        this.titleRes = titleRes;
        this.prefKey = prefKey;
        this.value = value;
        this.subtitleGenerator = subtitleGenerator;
        this.hasNext = hasNext;
        this.clickable = clickable;
    }

    public int getIconResource() {
        return iconRes;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public Object getValue() {
        return value;
    }

    public String getSubtitle() {
        if (subtitleGenerator != null) {
            return subtitleGenerator.get(this);
        } else {
            return null;
        }
    }

    public boolean hasNext() {
        return hasNext;
    }

    public boolean isTitle() {
        return prefKey == null;
    }

    public boolean isClickable() {
        return clickable;
    }

    public interface OnClickListener {
        void onClick(PrefItem item);
    }

    public interface SubtitleGenerator {
        String get(PrefItem item);
    }

    public static class Builder {

        @DrawableRes private int iconRes;
        @StringRes private int titleRes;
        @PrefKey private String prefKey;
        private Object value;
        private SubtitleGenerator subtitleGenerator;
        private boolean hasNext = false;
        private boolean clickable = true;

        public Builder() {
        }

        public Builder setIconResource(@DrawableRes int iconRes) {
            this.iconRes = iconRes;
            return this;
        }

        public Builder setTitleResource(@StringRes int titleRes) {
            this.titleRes = titleRes;
            return this;
        }

        public Builder setPreferenceKey(@PrefKey String prefKey) {
            this.prefKey = prefKey;
            return this;
        }

        public Builder setValue(Object value) {
            this.value = value;
            return this;
        }

        public Builder setSubtitleGenerator(SubtitleGenerator subtitleGenerator) {
            this.subtitleGenerator = subtitleGenerator;
            return this;
        }

        public Builder hasNext(boolean hasNext) {
            this.hasNext = hasNext;
            return this;
        }

        public Builder setClickable(boolean clickable) {
            this.clickable = clickable;
            return this;
        }

        public PrefItem build() {
            return new PrefItem(iconRes, titleRes, prefKey, value, subtitleGenerator, hasNext, clickable);
        }

    }

}
