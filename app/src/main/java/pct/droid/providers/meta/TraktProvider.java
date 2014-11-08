package pct.droid.providers.meta;


import android.accounts.NetworkErrorException;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import pct.droid.providers.BaseProvider;

public class TraktProvider extends BaseProvider {
    //http://api.trakt.tv/movie/summary.json/8ad497c5baf8ce8ce7d61040db5e7289/tt1285016 -> movies
    //http://api.trakt.tv/show/summary.json/8ad497c5baf8ce8ce7d61040db5e7289/the-walking-dead -> shows
    private String mApiUrl = "https://api.trakt.tv/";
    private String mApiKey = "515a27ba95fbd83f20690e5c22bceaff0dfbde7c";

    public MetaData getSummary(String imdbId, String type) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(mApiUrl + type + "/summary.json/" + mApiKey + "/" + imdbId);

        Call call = enqueue(requestBuilder.build());
        try {
            Response response = call.execute();
            if(response.isSuccessful()) {
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

    public MetaData[] getSummaries(String[] ids, String type, String extended) {
        Request.Builder requestBuilder = new Request.Builder();

        String idString = "";
        for(int i = 0; i < ids.length; i++) {
            idString += ids[i];
            if(i != ids.length - 1) {
                idString += ",";
            }
        }

        requestBuilder.url(mApiUrl + type + "/summaries.json/" + mApiKey + "/" + idString + "/" + extended);

        Call call = enqueue(requestBuilder.build());
        try {
            Response response = call.execute();
            if(response.isSuccessful()) {
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

    public class MetaData {
        public String title;
        public Integer year;
        public Integer released;
        public String trailer;
        public Integer runtime;
        public String tagline;
        public String overview;
        public String certification;
        public HashMap<String, String> images;
        public String[] genres;
    }
}