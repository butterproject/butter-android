package pct.droid.base.updater;

import java.util.Map;

public class UpdaterData {
    public Map<String, Map<String, Arch>> mobile;
    public Map<String, Map<String, Arch>> tv;

    public class Arch {
        public int versionCode;
        public String versionName;
        public String checksum;
        public String updateUrl;
    }
}