package pct.droid.base.casting.server;

public interface WebServerPluginInfo {
    String[] getMimeTypes();

    String[] getIndexFilesForMimeType(String mime);

    WebServerPlugin getWebServerPlugin(String mimeType);
}
