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
import android.support.annotation.NonNull;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.base.model.Movie;
import butter.droid.provider.base.model.Season;
import butter.droid.provider.base.model.Show;
import org.parceler.Parcels;

public class MediaWrapper implements Parcelable {

    public static final int COLOR_NONE = Color.TRANSPARENT;

    @NonNull private final Media media;
    private final int providerId;

    @ColorInt private int color = COLOR_NONE;

    public MediaWrapper(final Media media, final int providerId) {
        this.media = media;
        this.providerId = providerId;
    }

    public MediaWrapper(final Media media, final int providerId, final int color) {
        this.media = media;
        this.providerId = providerId;
        this.color = color;
    }

    private MediaWrapper(Parcel in) {
        this.media = Parcels.unwrap(in.readParcelable(Media.class.getClassLoader()));
        this.providerId = in.readInt();
        this.color = in.readInt();
    }

    public Media getMedia() {
        return media;
    }

    public int getProviderId() {
        return providerId;
    }

    public int getColor() {
        return color;
    }

    public void setColor(final int color) {
        this.color = color;
    }

    public boolean isMovie() {
        return media instanceof Movie;
    }

    public boolean isShow() {
        return media instanceof Show;
    }

    public boolean isSeason() {
        return media instanceof Season;
    }

    public boolean hasColor() {
        return color != COLOR_NONE;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(Parcels.wrap(media), flags);
        dest.writeInt(providerId);
        dest.writeInt(color);
    }

    public static final Creator<MediaWrapper> CREATOR = new Creator<MediaWrapper>() {
        @Override
        public MediaWrapper createFromParcel(Parcel in) {
            return new MediaWrapper(in);
        }

        @Override
        public MediaWrapper[] newArray(int size) {
            return new MediaWrapper[size];
        }
    };

}
