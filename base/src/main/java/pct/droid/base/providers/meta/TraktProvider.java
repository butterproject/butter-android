package pct.droid.base.providers.meta;


import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class TraktProvider extends MetaProvider {
    //http://api.trakt.tv/movie/summary.json/8ad497c5baf8ce8ce7d61040db5e7289/tt1285016 -> movies
    //http://api.trakt.tv/show/summary.json/8ad497c5baf8ce8ce7d61040db5e7289/the-walking-dead -> shows
    private String mApiUrl = "https://api.trakt.tv/";
    private String mApiKey = "515a27ba95fbd83f20690e5c22bceaff0dfbde7c";

    /**
     * Get metadata from Trakt
     *
     * @param imdbId IMDb ids to get metadata for
     * @param type   Type of item
     * @return MetaData
     */
    public MetaData getSummary(String imdbId, String type) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(mApiUrl + type + "/summary.json/" + mApiKey + "/" + imdbId);
        requestBuilder.tag(META_CALL);

        Call call = enqueue(requestBuilder.build());
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                String responseStr = response.body().string();
                MetaData result = mGson.fromJson(responseStr, MetaData.class);
                return result;
            }
        } catch (IOException e) {
            // eat exception for now TODO
            e.printStackTrace();
        }

        return new MetaData();
    }

    /**
     * Get metadata from Trakt
     *
     * @param ids      IMDb ids to get metadata for
     * @param type     Type of item
     * @param extended Type of data to get
     * @return MetaData
     */
    public MetaData[] getSummaries(String[] ids, String type, String extended) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.tag(META_CALL);

        String idString = "";
        for (int i = 0; i < ids.length; i++) {
            idString += ids[i];
            if (i != ids.length - 1) {
                idString += ",";
            }
        }

        requestBuilder.url(mApiUrl + type + "/summaries.json/" + mApiKey + "/" + idString + "/" + extended);

        Call call = enqueue(requestBuilder.build());
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                String responseStr = response.body().string();
                MetaData[] result = mGson.fromJson(responseStr, MetaData[].class);
                return result;
            }
        } catch (IOException e) {
            // eat exception for now TODO
            e.printStackTrace();
        }

        return new MetaData[0];
    }

}