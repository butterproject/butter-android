package com.popcorn.tv.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;
import pct.droid.base.providers.media.types.Media;

public class MainMedia implements Parcelable
{
    //region Attributes

    public String videoId;
    public String title;
    public String year;
    public String genre;
    public String rating;
    public String type = "media";
    public String image;
    public String fullImage;
    public String headerImage;
    public Map<String, String> subtitles;

    //endregion

    public MainMedia(Media media) {
        this.videoId = media.videoId;
        this.title = media.title;
        this.year = media.year;
        this.genre = media.genre;
        this.rating = media.rating;
        this.type = media.type;
        this.image = media.image;
        this.image = media.image;
        this.fullImage = media.fullImage;
        this.headerImage = media.headerImage;
        this.subtitles = media.subtitles;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
