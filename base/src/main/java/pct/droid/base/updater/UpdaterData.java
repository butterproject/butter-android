package pct.droid.base.updater;

import java.util.HashMap;
import java.util.Map;

public class UpdaterData {
    public Variant mobile;
    public Variant tv;

    public class Variant {
        public Map<String, Arch> release;
        public Map<String, Arch> development;
    }

    public class Arch {
        public int versionCode;
        public String versionName;
        public String checksum;
        public String updateUrl;
    }
}