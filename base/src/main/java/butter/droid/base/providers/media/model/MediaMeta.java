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

package butter.droid.base.providers.media.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;

public class MediaMeta implements Parcelable {

    public static final int COLOR_NONE = Color.TRANSPARENT;

    private final int providerId;
    @ColorInt private int color = COLOR_NONE;

    public MediaMeta(final int providerId) {
        this(providerId, COLOR_NONE);
    }

    public MediaMeta(final int providerId, @ColorInt final int color) {
        this.providerId = providerId;
        this.color = color;
    }

    private MediaMeta(Parcel in) {
        this(in.readInt(), in.readInt());
    }

    public int getProviderId() {
        return providerId;
    }

    @ColorInt public int getColor() {
        return color;
    }

    public void setColor(@ColorInt final int color) {
        this.color = color;
    }

    public boolean hasColor() {
        return color != COLOR_NONE;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(providerId);
        dest.writeInt(color);
    }

    public static final Creator<MediaMeta> CREATOR = new Creator<MediaMeta>() {
        @Override
        public MediaMeta createFromParcel(android.os.Parcel in) {
            return new MediaMeta(in);
        }

        @Override
        public MediaMeta[] newArray(int size) {
            return new MediaMeta[size];
        }
    };

}
