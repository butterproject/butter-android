package pct.droid.streamer;

import com.google.gson.Gson;

public class Ready {
    public String streamUrl;
    public String filePath;

    public static Ready parseJSON(String json) throws IllegalStateException {
        Gson gson = new Gson();
        return gson.fromJson(json, Ready.class);
    }

    @Override
    public String toString() {
        return  "url: " + streamUrl + ", path: " + filePath;
    }
}
