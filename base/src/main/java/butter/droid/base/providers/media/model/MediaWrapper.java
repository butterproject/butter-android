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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import butter.droid.provider.base.model.Episode;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.base.model.Movie;
import butter.droid.provider.base.model.Season;
import butter.droid.provider.base.model.Show;
import butter.droid.provider.base.model.Streamable;

public class MediaWrapper implements Parcelable {

    @NonNull private final Media media;
    @NonNull private final MediaMeta mediaMeta;

    public MediaWrapper(@NonNull final Media media, final int providerId) {
        this.media = media;
        this.mediaMeta = new MediaMeta(providerId);
    }

    public MediaWrapper(@NonNull final Media media, final int providerId, final int color) {
        this(media, new MediaMeta(providerId, color));
    }

    public MediaWrapper(@NonNull final Media media, @NonNull final MediaMeta mediaMeta) {
        this.media = media;
        this.mediaMeta = mediaMeta;
    }

    private MediaWrapper(Parcel in) {
        this.media = in.readParcelable(Media.class.getClassLoader());
        this.mediaMeta = in.readParcelable(MediaMeta.class.getClassLoader());
    }

    @NonNull public Media getMedia() {
        return media;
    }

    @NonNull public MediaMeta getMediaMeta() {
        return mediaMeta;
    }

    public int getProviderId() {
        return mediaMeta.getProviderId();
    }

    @ColorInt public int getColor() {
        return mediaMeta.getColor();
    }

    public void setColor(@ColorInt final int color) {
        this.mediaMeta.setColor(color);
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

    public boolean isEpisode() {
        return media instanceof Episode;
    }

    public boolean isStreamable() {
        return media instanceof Streamable;
    }

    public boolean hasColor() {
        return mediaMeta.hasColor();
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(media, flags);
        dest.writeParcelable(mediaMeta, flags);
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
