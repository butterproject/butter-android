package com.popcorn.tv.models;

import java.util.Map;

import pct.droid.base.providers.media.types.Media;

public class MainMedia
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
}
