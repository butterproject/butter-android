package pct.droid.base.casting.airplay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.widget.EditText;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.PropertyListParser;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import pct.droid.base.casting.BaseCastingClient;
import pct.droid.base.casting.CastingDevice;
import pct.droid.base.casting.CastingListener;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.utils.LogUtils;

/**
 * AirPlayClient.java
 *
 * CastingClient for AirPlay. Using {@link pct.droid.base.casting.airplay.AirPlayDiscovery} to discover devices.
 */
public class AirPlayClient extends BaseCastingClient implements ServiceListener {

    private static final String USER_AGENT = "MediaControl/1.0";
    private static final MediaType TYPE_PARAMETERS = MediaType.parse("text/parameters");
    private static final MediaType TYPE_PLIST = MediaType.parse("text/x-apple-plist+xml");
    public static final String AUTH_USERNAME = "Airplay";

    private Map<String, ServiceInfo> mDiscoveredServices = new HashMap<String, ServiceInfo>();

    private Context mContext;
    private AirPlayDiscovery mDiscovery;
    private Handler mPingHandler;
    private Handler mHandler;
    private CastingListener mListener;
    private OkHttpClient mHttpClient = new OkHttpClient();
    private AirPlayDevice mCurrentDevice;
    private String mCurrentState = "stopped", mSessionId = null, mPassword = null;

    public AirPlayClient(Context context, CastingListener listener) {
        mListener = listener;
        mHandler = new Handler(context.getApplicationContext().getMainLooper());
        mPingHandler = new Handler(context.getApplicationContext().getMainLooper());
        mDiscovery = AirPlayDiscovery.getInstance(context, this);
        mDiscovery.start();

        mHttpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                if (response.request().header("Authorization") != null && (mPassword == null || mPassword.isEmpty())) {
                    openAuthorizationDialog();
                }

                String responseHeader = response.header("WWW-Authenticate");
                Map<String, String> params = getAuthParams(responseHeader);
                String credentials = makeAuthorizationHeader(params, response.request().method(), response.request().uri().toString());
                return response.request().newBuilder().header("Authorization", credentials).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null; // not needed
            }
        });
    }

    /**
     * Call on close
     */
    public void destroy() {
        mHandler.removeCallbacksAndMessages(null);
        mDiscovery.stop();
    }

    /**
     * @param context New context for future use
     */
    public void setContext(Context context) {
        mContext = context;
    }

    /**
     * @return {@link pct.droid.base.casting.airplay.AirPlayDiscovery} used by this client
     */
    public AirPlayDiscovery getDiscovery() {
        return mDiscovery;
    }

    /**
     * @param device Device to connect to
     */
    public void connect(CastingDevice device) {
        if (mCurrentDevice != device) {
            disconnect();
        }
        mCurrentDevice = (AirPlayDevice) device;
        LogUtils.d("Connecting to airplay device: " + device.getId() + " - " + mCurrentDevice.getIpAddress());

        mSessionId = UUID.randomUUID().toString();
        LogUtils.d("Session ID: " + mSessionId);

        mListener.onDeviceSelected(mCurrentDevice);
        mPingRunnable.run();
        // initEvents();
    }

    /**
     * Disconnect from device and stop all runnables
     */
    public void disconnect() {
        if(mCurrentDevice == null) return;

        stop();
        mPingHandler.removeCallbacks(mPingRunnable);
        mHandler.removeCallbacks(mPlaybackRunnable);
        mListener.onDisconnected();
    }

    /**
     * Not used code for AirPlay events (not working)
     */
    /*
     * Init events connection, uses reverse HTTP
     *//*
    public void initEvents() {
        RequestBody body = RequestBody.create(TYPE_PLIST, "");

        Request playRequest = requestBuilder("reverse")
                .addHeader("Upgrade", "PTTH/1.0")
                .addHeader("Connection", "Upgrade")
                .addHeader("X-Apple-Purpose", "event")
                .addHeader("X-Apple-Session-ID", mSessionId)
                .post(body)
                .build();

        mHttpClient.newCall(playRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                response.headers();
            }
        });
    }
    */

    /**
     * Load media to device and start playing
     * @param media Media object used for MetaData
     * @param location Location of media that is supposed to be played
     * @param position Start position of playback
     */
    @Override
    public void loadMedia(Media media, String location, float position) {
        if(mCurrentDevice == null) return;
        LogUtils.d("Load media: " + location);

        stop();

        RequestBody body = RequestBody.create(TYPE_PARAMETERS, "Content-Location: " + location + "\n" + "Start-Position: " + position);

        Request playRequest = requestBuilder("play")
                .post(body)
                .build();

        mPlaybackRunnable.run();

        mHttpClient.newCall(playRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                LogUtils.d("Failed to load media: " + e.getMessage());
                mListener.onCommandFailed("play", e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String body = response.body().string();
                LogUtils.d("Load media response: " + body);
                if (response.isSuccessful()) {
                    LogUtils.d("Load media successful");
                } else {
                    LogUtils.d("Failed to play media");
                    mListener.onCommandFailed("play", "Failed to play media");
                }
            }
        });
    }

    /**
     * Pause playback when paused
     */
    @Override
    public void play() {
        if (mCurrentState.equals("paused")) {
            Request playRequest = requestBuilder("rate?value=1.000000")
                    .build();

            mHttpClient.newCall(playRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    // Ignore, playback info will be obtained and so will the result
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    // Ignore, playback info will be obtained and so will the result
                }
            });
        }
    }

    /**
     * Pause playback when playing
     */
    @Override
    public void pause() {
        if (mCurrentState.equals("playing")) {
            Request playRequest = requestBuilder("scrub?position=0.000000")
                    .build();

            mHttpClient.newCall(playRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    // Ignore, playback info will be obtained and so will the result
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    // Ignore, playback info will be obtained and so will the result
                }
            });
        }
    }

    /**
     * Seek to position in playback
     * @param position Relative seek position
     */
    @Override
    public void seek(float position) {
        Request playRequest = requestBuilder("scrub?position=" + position)
                .build();

        mHttpClient.newCall(playRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                // Ignore, playback info will be obtained and so will the result
            }

            @Override
            public void onResponse(Response response) throws IOException {
                // Ignore, playback info will be obtained and so will the result
            }
        });
    }

    /**
     * Completely stop playback
     */
    @Override
    public void stop() {
        Request stopRequest = requestBuilder("stop").build();

        mHttpClient.newCall(stopRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mListener.onCommandFailed("stop", e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mListener.onCommandFailed("stop", "Cannot stop");
                } else {
                    mHandler.removeCallbacks(mPlaybackRunnable);
                }
            }
        });
    }

    @Override
    public void setVolume(float volume) {
        // Can't control volume (yet), so do nothing
    }

    @Override
    public boolean canControlVolume() {
        return false;
    }

    private Request.Builder requestBuilder(String method) {
        Request.Builder builder = new Request.Builder()
                .addHeader("User-Agent", USER_AGENT)
                .url(mCurrentDevice.getUrl() + method);

        if (mSessionId != null) {
            builder.addHeader("X-Apple-Session-ID", mSessionId);
        }

        return builder;
    }

    Runnable mPingRunnable = new Runnable() {
        @Override
        public void run() {
            Request pingRequest = requestBuilder("server-info")
                    .build();

            mHttpClient.newCall(pingRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    mSessionId = null;
                    mListener.onCommandFailed("server-info", e.getMessage());
                    mListener.onDisconnected();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        mListener.onConnected(mCurrentDevice);
                        LogUtils.d("Ping successful to " + mCurrentDevice.getIpAddress());
                        mPingHandler.postDelayed(mPingRunnable, 30000);
                    }
                }
            });
        }
    };

    Runnable mPlaybackRunnable = new Runnable() {
        @Override
        public void run() {
            Request infoRequest = requestBuilder("playback-info").build();

            mHttpClient.newCall(infoRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    mListener.onCommandFailed("playback-info", e.getMessage());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(response.body().byteStream());
                            float position = ((NSNumber) rootDict.objectForKey("position")).floatValue();
                            float duration = ((NSNumber) rootDict.objectForKey("duration")).floatValue();

                            float rate = ((NSNumber) rootDict.objectForKey("rate")).floatValue();
                            boolean playing = false;
                            if (rate > 0) {
                                playing = true;
                            }
                            boolean readyToPlay = false;
                            if (rootDict.containsKey("readyToPlay")) {
                                readyToPlay = ((NSNumber) rootDict.objectForKey("readyToPlay")).boolValue();
                            }

                            LogUtils.d("PlaybackInfo: playing: " + playing + ", rate: " + rate + ", position: " + position + ", ready: " + readyToPlay);

                            if(readyToPlay) {
                                mListener.onReady();
                            }

                            mListener.onPlayBackChanged(playing, position);

                            if (position == duration) return;

                            mHandler.postDelayed(mPlaybackRunnable, 200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mListener.onCommandFailed("playback-info", "Cannot get playback info");
                    }
                }
            });
        }
    };

    @Override
    public void serviceAdded(final ServiceEvent event) {
        LogUtils.d("Found AirPlay service: " + event.getName());
        mDiscoveredServices.put(event.getInfo().getKey(), event.getInfo());
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        LogUtils.d("Removed AirPlay service: " + event.getName());
        mDiscoveredServices.remove(event.getInfo().getKey());
        AirPlayDevice removedDevice = new AirPlayDevice(event.getInfo());
        mListener.onDeviceRemoved(removedDevice);
        if (mCurrentDevice != null && mCurrentDevice.getId().equals(removedDevice.getId())) {
            mPingHandler.removeCallbacks(mPingRunnable);
            mListener.onDisconnected();
            mCurrentDevice = null;
        }
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        LogUtils.d("Resolved AirPlay service: " + event.getName() + " @ " + event.getInfo().getURL());
        AirPlayDevice device = new AirPlayDevice(event.getInfo());
        mListener.onDeviceDetected(device);
        mDiscoveredServices.put(event.getInfo().getKey(), event.getInfo());
    }

    /**
     * Open Dialog to enter password or pin used for AirPlay
     */
    public void openAuthorizationDialog() {
        final Boolean[] wait = {true};

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: better layout
                final EditText editText = new EditText(mContext);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext)
                        .setTitle("Enter pincode")
                        .setView(editText)
                        .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPassword = editText.getText().toString();
                                wait[0] = false;
                                dialog.dismiss();
                            }
                        });

                dialogBuilder.show();
            }
        });

        while (wait[0]) {
            // Block network thread to wait for input
        }
    }

    /**
     * Generate MD5 for Digest Authentication
     * @param input {@link java.lang.String}
     * @return {@link java.lang.String}
     */
    private String md5Digest(String input) {
        byte[] source;
        try {
            source = input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            source = input.getBytes();
        }

        String result = null;
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source);

            byte temp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = temp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }

            result = new String(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Make Authorization header for HTTP request
     * @param params {@link java.util.Map}
     * @param method {@link java.lang.String}
     * @param uri {@link java.lang.String}
     * @return {@link java.lang.String}
     */
    private String makeAuthorizationHeader(Map params, String method, String uri) {
        String realm = (String) params.get("realm");
        String nonce = (String) params.get("nonce");
        String ha1 = md5Digest(AUTH_USERNAME + ":" + realm + ":" + mPassword);
        String ha2 = md5Digest(method + ":" + uri);
        String response = md5Digest(ha1 + ":" + nonce + ":" + ha2);
        return "Digest username=\"" + AUTH_USERNAME + "\", "
                + "realm=\"" + realm + "\", "
                + "nonce=\"" + nonce + "\", "
                + "uri=\"" + uri + "\", "
                + "response=\"" + response + "\"";
    }

    private Map<String, String> getAuthParams(String authString) {
        Map<String, String> params = new HashMap<>();
        int firstSpace = authString.indexOf(' ');
        String rest = authString.substring(firstSpace + 1).replaceAll("\r\n", " ");
        String[] lines = rest.split("\", ");
        for (int i = 0; i < lines.length; i++) {
            int split = lines[i].indexOf("=\"");
            String key = lines[i].substring(0, split);
            String value = lines[i].substring(split + 2);
            if (value.charAt(value.length() - 1) == '"') {
                value = value.substring(0, value.length() - 1);
            }
            params.put(key, value);
        }
        return params;
    }

}
