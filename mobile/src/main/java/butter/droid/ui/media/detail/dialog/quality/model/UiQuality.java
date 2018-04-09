package butter.droid.ui.media.detail.dialog.quality.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UiQuality implements Parcelable {

    private final boolean selected;
    private final String name;

    public UiQuality(boolean selected, String name) {
        this.selected = selected;
        this.name = name;
    }

    protected UiQuality(Parcel in) {
        selected = in.readByte() != 0;
        name = in.readString();
    }

    public boolean isSelected() {
        return selected;
    }

    public String getName() {
        return name;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeString(name);
    }


    public static final Creator<UiQuality> CREATOR = new Creator<UiQuality>() {
        @Override
        public UiQuality createFromParcel(Parcel in) {
            return new UiQuality(in);
        }

        @Override
        public UiQuality[] newArray(int size) {
            return new UiQuality[size];
        }
    };

}
