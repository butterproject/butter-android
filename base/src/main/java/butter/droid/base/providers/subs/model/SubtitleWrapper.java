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

package butter.droid.base.providers.subs.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butter.droid.provider.subs.model.Subtitle;

public class SubtitleWrapper implements Parcelable {

    // If null default subtitle language should be loaded
    @Nullable private final Subtitle subtitle;
    @Nullable private Uri fileUri;

    public SubtitleWrapper() {
        subtitle = null;
    }

    public SubtitleWrapper(@NonNull final Subtitle subtitle) {
        this.subtitle = subtitle;
    }

    protected SubtitleWrapper(Parcel in) {
        subtitle = in.readParcelable(Subtitle.class.getClassLoader());
        fileUri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(subtitle, flags);
        dest.writeParcelable(fileUri, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Nullable public Subtitle getSubtitle() {
        return subtitle;
    }

    @Nullable public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(@Nullable final Uri fileUri) {
        this.fileUri = fileUri;
    }

    public static final Creator<SubtitleWrapper> CREATOR = new Creator<SubtitleWrapper>() {
        @Override
        public SubtitleWrapper createFromParcel(Parcel in) {
            return new SubtitleWrapper(in);
        }

        @Override
        public SubtitleWrapper[] newArray(int size) {
            return new SubtitleWrapper[size];
        }
    };

}
