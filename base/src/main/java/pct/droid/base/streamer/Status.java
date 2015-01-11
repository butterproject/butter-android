package pct.droid.base.streamer;

import com.google.gson.Gson;

public class Status {
    public double progress;
    public double downloadSpeed;
    public int peers;
    public int seeds;
    public String filePath;

    public static Status parseJSON(String json) throws IllegalStateException {
        Gson gson = new Gson();
        return gson.fromJson(json, Status.class);
    }

    public static String getJSON(double progress, double downloadSpeed, int peers, int seeds, String filePath) throws IllegalStateException {
        Gson gson = new Gson();
        return gson.toJson(new Status(progress, downloadSpeed, peers, seeds, filePath));
    }

    private Status(double progress, double downloadSpeed, int peers, int seeds, String filePath) {
        this.progress = progress;
        this.downloadSpeed = downloadSpeed;
        this.peers = peers;
        this.seeds = seeds;
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "filePath: " + filePath + ", progress: " + progress + ", downloadSpeed: " + downloadSpeed + ", peers: " + peers + ", seeds: " + seeds;
    }
}
