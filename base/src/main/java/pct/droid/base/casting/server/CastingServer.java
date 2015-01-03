package pct.droid.base.casting.server;

import java.io.File;
import java.util.List;

import pct.droid.base.Constants;

public class CastingServer extends SimpleWebServer {

    private String mHost;
    private Integer mPort;
    private static File mCurrentVideo = null, mCurrentSubs = null;

    public CastingServer(String host, int port, File wwwroot, boolean quiet) {
        super(host, port, wwwroot, quiet);
        mHost = host;
        mPort = port;
    }

    public CastingServer(String host, int port, List<File> wwwroots, boolean quiet) {
        super(host, port, wwwroots, quiet);
        mHost = host;
        mPort = port;
    }

    @Override
    public Response serve(final IHTTPSession session) {
        String uri = session.getUri();

        Response response;
        if(uri.contains("video")) {
            if(mCurrentVideo != null) {
                String mimeTypeForFile = getMimeTypeForFile(mCurrentVideo.getAbsolutePath());
                response =  serveFile(uri, session.getHeaders(), mCurrentVideo, mimeTypeForFile);
            } else {
                response = getNotFoundResponse();
            }
        } else if(uri.contains("subs")) {
            if(mCurrentSubs != null) {
                String mimeTypeForFile = getMimeTypeForFile(mCurrentSubs.getAbsolutePath());
                response =  serveFile(uri, session.getHeaders(), mCurrentSubs, mimeTypeForFile);
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
        if(mCurrentSubs != null) {
            response.addHeader("CaptionInfo.sec", "http://" + mHost + ":" + mPort + "/subs.srt");
        }

        return response;
    }

    public static void setCurrentVideo(File file) {
        mCurrentVideo = file;
    }
    public static void setCurrentSubs(File file) {
        mCurrentSubs = file;
    }

}
