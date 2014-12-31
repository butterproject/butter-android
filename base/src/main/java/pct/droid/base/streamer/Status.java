package pct.droid.base.streamer;

import com.google.gson.Gson;

public class Status {
    public double downloaded;
    public double progress;
    public double downloadSpeed;
    public double eta;
    public int peers;
    public int seeds;
    public int connections;
    public double uploadSpeed;
    public String filePath;

    public static Status parseJSON(String json) throws IllegalStateException {
        Gson gson = new Gson();
        return gson.fromJson(json, Status.class);
    }

    @Override
    public String toString() {
        return "eta: " + eta + ", downloaded: " + downloaded + ", progress: " + progress + ", downloadSpeed: " + downloadSpeed + ", peers: " + peers + ", seeds: " + seeds + ", uploadSpeed: " + uploadSpeed + ", connections: " + connections;
    }
}
