package pct.droid.base.casting.server;

import java.io.File;
import java.util.List;

import pct.droid.base.Constants;

public class CastingServer extends SimpleWebServer {

    private static String sHost;
    private static Integer sPort;
    private static File sCurrentVideo = null, sCurrentSubs = null;

    public CastingServer(String host, int port, File wwwroot, boolean quiet) {
        super(host, port, wwwroot, quiet);
        sHost = host;
        sPort = port;
    }

    public CastingServer(String host, int port, List<File> wwwroots, boolean quiet) {
        super(host, port, wwwroots, quiet);
        sHost = host;
        sPort = port;
    }

    @Override
    public Response serve(final IHTTPSession session) {
        String uri = session.getUri();

        Response response;
        if(uri.contains("video.mp4")) {
            if(sCurrentVideo != null) {
                String mimeTypeForFile = getMimeTypeForFile(sCurrentVideo.getAbsolutePath());
                response =  serveFile(uri, session.getHeaders(), sCurrentVideo, mimeTypeForFile);
            } else {
                response = getNotFoundResponse();
            }
        } else if(uri.contains("video.srt")) {
            if(sCurrentSubs != null) {
                String mimeTypeForFile = getMimeTypeForFile(sCurrentSubs.getAbsolutePath());
                response =  serveFile(uri, session.getHeaders(), sCurrentSubs, mimeTypeForFile);
            } else {
                response = getNotFoundResponse();
            }
        } else if (Constants.DEBUG_ENABLED || uri.contains("torrents") || uri.contains("subs")) {
            response = super.serve(session);
        } else {
            response = getForbiddenResponse("You can't access this location");
        }

        // Extra headers for DLNA
        response.addHeader("transferMode.dlna.org", "Streaming");
        response.addHeader("contentFeatures.dlna.org", "DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=017000 00000000000000000000000000");
        if(sCurrentSubs != null) {
            response.addHeader("CaptionInfo.sec", "http://" + sHost + ":" + sPort + "/subs.srt");
        }

        return response;
    }

    public static void setCurrentVideo(File file) {
        sCurrentVideo = file;
    }
    public static void setCurrentSubs(File file) {
        sCurrentSubs = file;
    }

    public static String getVideoURL() {
        return "http://" + sHost + ":" + sPort + "/video.mp4";
    }

    public static String getSubsURL() {
        return "http://" + sHost + ":" + sPort + "/video.srt";
    }

}
