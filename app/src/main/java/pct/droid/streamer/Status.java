package pct.droid.streamer;

import com.google.gson.Gson;

public class Status {
    public double downloaded;
    public double progress;
    public double downloadSpeed;
    public double eta;
    public double peers;
    public double seeds;
    public double connections;
    public double uploadSpeed;

    public static Status parseJSON(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Status.class);
    }

    @Override
    public String toString() {
        return  "eta: " + eta + ", downloaded: " + downloaded + ", progress: " + progress + ", downloadSpeed: " + downloadSpeed + ", peers: " + peers + ", seeds: " + seeds  + ", uploadSpeed: " + uploadSpeed + ", connections: " + connections;
    }
}
