package butter.droid.base.providers.media.response.models;

import java.util.ArrayList;
import java.util.List;

import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.subs.SubsProvider;

public abstract class Response<T extends ResponseItem> {

    protected List<T> responseItems;

    public Response(List<T> responseItems) {
        this.responseItems = responseItems;
    }

    public abstract ArrayList<Media> formatListForPopcorn(ArrayList<Media> existingList, MediaProvider mediaProvider, SubsProvider subsProvider);

    //public abstract ArrayList<Media> formatDetailForPopcorn();
}