/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base.beaming.server;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.HttpDate;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import pct.droid.base.PopcornApplication;
import timber.log.Timber;

public class BeamServer {

    private static FileType
            MP4 = new FileType("mp4", "video/mp4", "DLNA.ORG_PN=AVC_MP4_BL_L3L_SD_AAC;DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01700000000000000000000000000000", "Streaming"),
            AVI = new FileType("avi", "video/x-msvideo", "DLNA.ORG_PN=AVC_MP4_BL_L3L_SD_AAC;DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01700000000000000000000000000000", "Streaming"),
            MKV = new FileType("mkv", "video/x-matroska", "DLNA.ORG_PN=AVC_MKV_MP_HD_AC3;DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01700000000000000000000000000000", "Streaming"),
            SRT = new FileType("srt", "application/x-subrip", "*", "");
    private static FileType[] FILE_TYPES = {MP4, AVI, MKV, SRT};
    private static HashMap<String, FileType> EXTENSIONS, CONTENT_TYPES;
    private static String sHost;
    private static Integer sPort;
    private static File sCurrentVideo = null, sCurrentSubs = null;
    private AsyncServer mAsyncServer = new AsyncServer();
    private AsyncHttpServer mHttpServer = new AsyncHttpServer() {
        protected boolean onRequest(final AsyncHttpServerRequest httpServerRequest, final AsyncHttpServerResponse httpServerResponse) {
            Timber.d(httpServerRequest.toString());
            httpServerResponse.getHeaders().set("Access-Control-Allow-Origin", "*");
            return super.onRequest(httpServerRequest, httpServerResponse);
        }
    };
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;

    static {
        EXTENSIONS = new HashMap<>();
        CONTENT_TYPES = new HashMap<>();
        for (FileType localFileType : FILE_TYPES) {
            EXTENSIONS.put(localFileType.extension, localFileType);
            CONTENT_TYPES.put(localFileType.mimeType, localFileType);
        }
        CONTENT_TYPES.put("video/mpeg4", MP4);
        EXTENSIONS.put("3gp", MP4);
        EXTENSIONS.put("mov", MP4);
    }

    public BeamServer(String host, int port) {
        sHost = host;
        sPort = port;

        StringBuilder extBuilder = new StringBuilder();
        extBuilder.append("(");
        for (FileType localFileType : FILE_TYPES) {
            FileReponse localFileReponse = new FileReponse(localFileType);
            mHttpServer.get("/video." + localFileType.extension, localFileReponse);
            mHttpServer.addAction("HEAD", "/video." + localFileType.extension, localFileReponse);
            extBuilder.append(localFileType.extension);
            extBuilder.append("|");
        }
        extBuilder.deleteCharAt(extBuilder.length() - 1);
        extBuilder.append(")");

        mHttpServer.get("/video.srt", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest httpServerRequest, AsyncHttpServerResponse httpServerResponse) {
                if (sCurrentSubs != null && sCurrentSubs.exists()) {
                    SRT.setHeaders(httpServerResponse);
                    httpServerResponse.sendFile(sCurrentSubs);
                } else {
                    httpServerResponse.code(404);
                }
            }
        });

        mHttpServer.get("/(.*?)", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.send("Empty");
            }
        });
    }

    public static void setCurrentVideo(File file) {
        sCurrentVideo = file;
    }

    public static void setCurrentSubs(File file) {
        sCurrentSubs = file;
    }

    public static void setCurrentVideo(String file) {
        setCurrentVideo(new File(file));
    }

    public static void setCurrentSubs(String file) {
        setCurrentSubs(new File(file));
    }

    public static String getHost() {
        return "http://" + sHost + ":" + sPort;
    }

    public static String getVideoURL() {
        return "http://" + sHost + ":" + sPort + "/video.mp4";
    }

    public static String getSubsURL() {
        return "http://" + sHost + ":" + sPort + "/video.srt";
    }

    public void start() {
        mHttpServer.listen(mAsyncServer, sPort);

        PowerManager powerManager = (PowerManager) PopcornApplication.getAppContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CastingServer");
        mWakeLock.acquire();
        WifiManager wifiManager = (WifiManager) PopcornApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "CastingServer");
        mWifiLock.acquire();
    }

    public void stop() {
        try {
            mHttpServer.stop();
            mAsyncServer.stop();
            mWifiLock.release();
            mWakeLock.release();
        } catch (Exception e) {}
    }

    public static class FileType {
        public final String dlnaContentFeatures;
        public final String dlnaTransferMode;
        public final String extension;
        public final String mimeType;

        public FileType(String extension, String mimeType, String dlnaContentFeatures, String dlnaTransferMode) {
            this.extension = extension;
            this.mimeType = mimeType;
            this.dlnaContentFeatures = dlnaContentFeatures;
            this.dlnaTransferMode = dlnaTransferMode;
        }

        public final boolean isImage() {
            return this.mimeType.startsWith("image/");
        }

        public boolean isVideo() {
            return this.mimeType.startsWith("video/");
        }

        public void setHeaders(Headers paramHeaders) {
            paramHeaders.set("contentFeatures.dlna.org", this.dlnaContentFeatures);
            paramHeaders.set("TransferMode.DLNA.ORG", this.dlnaTransferMode);
            paramHeaders.set("DAAP-Server", "iTunes/11.0.5 (OS X)");
            paramHeaders.set("Date", HttpDate.format(new Date()));
            paramHeaders.set("Last-Modified", "2015-01-01T10:00:00Z");
            paramHeaders.set("Content-Type", this.mimeType);
        }

        public void setHeaders(AsyncHttpServerResponse httpServerResponse) {
            setHeaders(httpServerResponse.getHeaders());
        }
    }

    class FileReponse implements HttpServerRequestCallback {
        FileType mFileType;

        public FileReponse(FileType fileType) {
            mFileType = fileType;
        }

        public void onRequest(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse httpServerResponse) {
            if (sCurrentVideo != null && sCurrentVideo.exists()) {
                mFileType.setHeaders(httpServerResponse);
                if (!asyncHttpServerRequest.getMethod().equals("HEAD"))
                    httpServerResponse.sendFile(sCurrentVideo);
            } else {
                httpServerResponse.code(404);
            }

            Timber.i(httpServerResponse.toString());
        }
    }
}
