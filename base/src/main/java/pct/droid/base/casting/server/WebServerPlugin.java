package pct.droid.base.casting.server;

import java.io.File;
import java.util.Map;

public interface WebServerPlugin {
    void initialize(Map<String, String> commandLineOptions);

    boolean canServeUri(String uri, File rootDir);

    NanoHTTPD.Response serveFile(String uri, Map<String, String> headers, NanoHTTPD.IHTTPSession session, File file, String mimeType);
}
