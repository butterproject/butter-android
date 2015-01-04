package pct.droid.base.casting.googlecast;

import android.content.Context;
import android.os.Handler;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.CastMediaControlIntent;

import java.util.HashSet;
import java.util.Set;

import pct.droid.base.casting.BaseCastingClient;
import pct.droid.base.casting.CastingDevice;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.utils.LogUtils;

public class GoogleCastClient extends BaseCastingClient {

    private final Set<GoogleDevice> mDiscoveredDevices = new HashSet<>();

    private Context mContext;
    private Handler mHandler;
    private GoogleCastCallback mCallback;
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private GoogleDevice mCurrentDevice;

    public GoogleCastClient(Context context, GoogleCastCallback callback) {
        mContext = context;
        mCallback = callback;
        mHandler = new Handler(context.getApplicationContext().getMainLooper());
        mMediaRouter = MediaRouter.getInstance(context.getApplicationContext());

        mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)).build();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,  MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    /**
     * Call on close
     */
    public void destroy() {
        mHandler.removeCallbacksAndMessages(null);
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    /**
     * @param context New context for future use
     */
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void loadMedia(Media media, String location, float position) {

    }

    @Override
    public void play() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void seek(float position) {

    }

    @Override
    public void stop() {

    }

    /**
     * @param device Device to connect to
     */
    @Override
    public void connect(CastingDevice device) {
        if (mCurrentDevice != device) {
            disconnect();
        }

        mCurrentDevice = (GoogleDevice) device;
        mMediaRouter.selectRoute(mCurrentDevice.routeInfo);
        LogUtils.d("Connecting to google cast device: " + device.getId() + " - " + mCurrentDevice.getName());

        mCallback.onDeviceSelected(mCurrentDevice);
        // initEvents();
    }

    @Override
    public void disconnect() {
        //CastDevice.getFromBundle(info.getExtras())
        mCurrentDevice = null;
        mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
    }

    @Override
    public void setVolume(float volume) {

    }

    @Override
    public boolean canControlVolume() {
        return mCurrentDevice != null && mCurrentDevice.routeInfo.getVolumeHandling() == MediaRouter.RouteInfo.PLAYBACK_VOLUME_VARIABLE;
    }

    private MediaRouter.Callback mMediaRouterCallback = new MediaRouter.Callback() {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteSelected(router, route);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteUnselected(router, route);
            if (mCurrentDevice.equals(new GoogleDevice(route))) {
                mCurrentDevice = null;
                mCallback.onDisconnected();
            }
        }

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteAdded(router, route);
            GoogleDevice device = new GoogleDevice(route);
            mDiscoveredDevices.add(device);
            mCallback.onDeviceDetected(device);
        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteRemoved(router, route);
            GoogleDevice device = new GoogleDevice(route);
            mDiscoveredDevices.remove(device);
            mCallback.onDeviceRemoved(device);
        }

        @Override
        public void onRouteVolumeChanged(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteVolumeChanged(router, route);
            mCallback.onVolumeChanged((double) route.getVolume(), route.getVolume() == 0);
        }
    };

}
