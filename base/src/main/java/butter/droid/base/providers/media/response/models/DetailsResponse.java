package butter.droid.base.providers.media.response.models;

import android.content.Context;

import java.util.ArrayList;

import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.subs.SubsProvider;

public abstract class DetailsResponse<T extends ResponseItem> {

    public DetailsResponse() {
    }

    public abstract ArrayList<Media> formatDetailForPopcorn(Context context, T responseItem, MediaProvider mediaProvider, SubsProvider subsProvider);
}