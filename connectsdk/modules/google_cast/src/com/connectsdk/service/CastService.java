/*
 * CastService
 * Connect SDK
 *
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 23 Feb 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.connectsdk.service;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.connectsdk.core.ImageInfo;
import com.connectsdk.core.MediaInfo;
import com.connectsdk.core.SubtitleInfo;
import com.connectsdk.core.Util;
import com.connectsdk.discovery.DiscoveryFilter;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.capability.WebAppLauncher;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;
import com.connectsdk.service.command.URLServiceSubscription;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;
import com.connectsdk.service.sessions.CastWebAppSession;
import com.connectsdk.service.sessions.LaunchSession;
import com.connectsdk.service.sessions.LaunchSession.LaunchSessionType;
import com.connectsdk.service.sessions.WebAppSession;
import com.connectsdk.service.sessions.WebAppSession.WebAppPinStatusListener;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.LaunchOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

public class CastService extends DeviceService implements MediaPlayer, MediaControl, VolumeControl, WebAppLauncher {
    private static final long MEDIA_TRACK_ID = 1;

    interface ConnectionListener {
        void onConnected();
    }

    public interface LaunchWebAppListener{
        void onSuccess(WebAppSession webAppSession);
        void onFailure(ServiceCommandError error);
    }

    // @cond INTERNAL

    public static final String ID = "Chromecast";

    public final static String PLAY_STATE = "PlayState";
    public final static String CAST_SERVICE_VOLUME_SUBSCRIPTION_NAME = "volume";
    public final static String CAST_SERVICE_MUTE_SUBSCRIPTION_NAME = "mute";

    /**
     * Checked exception for CastApi wrapper
     */
    static class CastClientException extends Exception {

        CastClientException(String message, Exception e) {
            super(message, e);
        }
    }

    /*
     * CastApi wrapper. It catches all possible cast exceptions and rethrows a checked
     * CastClientException.
     */
    static class CastClient {

        public void leaveApplication(GoogleApiClient mApiClient) throws CastClientException {
            try {
                Cast.CastApi.leaveApplication(mApiClient);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public void setMessageReceivedCallbacks(
                GoogleApiClient mApiClient, String namespace,
                RemoteMediaPlayer mMediaPlayer) throws CastClientException {
            try {
                Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mMediaPlayer.getNamespace(),
                        mMediaPlayer);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            } catch (IOException e) {
                throw createCastClientException(e);
            }
        }

        public void removeMessageReceivedCallbacks(GoogleApiClient mApiClient,
                                                   String namespace) throws CastClientException {
            try {
                Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, namespace);
            } catch (IOException e) {
                throw createCastClientException(e);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public Object getApplicationStatus(GoogleApiClient mApiClient) throws CastClientException {
            try {
                return Cast.CastApi.getApplicationStatus(mApiClient);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public PendingResult<ApplicationConnectionResult> launchApplication(
                GoogleApiClient mApiClient, String mediaAppId, LaunchOptions options)
                throws CastClientException {
            try {
                return Cast.CastApi.launchApplication(mApiClient, mediaAppId, options);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public PendingResult<Status> stopApplication(GoogleApiClient mApiClient, String sessionId)
                throws CastClientException {
            try {
                return Cast.CastApi.stopApplication(mApiClient, sessionId);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public PendingResult<ApplicationConnectionResult> joinApplication(
                GoogleApiClient mApiClient) throws CastClientException {
            try {
                return Cast.CastApi.joinApplication(mApiClient);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public PendingResult<ApplicationConnectionResult> joinApplication(
                GoogleApiClient mApiClient, String appId) throws CastClientException {
            try {
                return Cast.CastApi.joinApplication(mApiClient, appId);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public PendingResult<Status> stopApplication(GoogleApiClient mApiClient)
                throws CastClientException {
            try {
                return Cast.CastApi.stopApplication(mApiClient);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public void setVolume(GoogleApiClient mApiClient, float volume)
                throws IOException, CastClientException {
            try {
                Cast.CastApi.setVolume(mApiClient, volume);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            } catch (IOException e) {
                throw createCastClientException(e);
            }
        }

        public void setMute(GoogleApiClient mApiClient, boolean isMute)
                throws IOException, CastClientException {
            try {
                Cast.CastApi.setMute(mApiClient, isMute);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            } catch (IOException e) {
                throw createCastClientException(e);
            }
        }

        public ApplicationMetadata getApplicationMetadata(GoogleApiClient mApiClient)
                throws CastClientException {
            try {
                return Cast.CastApi.getApplicationMetadata(mApiClient);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public double getVolume(GoogleApiClient mApiClient) throws CastClientException {
            try {
                return Cast.CastApi.getVolume(mApiClient);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        public boolean isMute(GoogleApiClient mApiClient) throws CastClientException {
            try {
                return Cast.CastApi.isMute(mApiClient);
            } catch (RuntimeException e) {
                throw createCastClientException(e);
            }
        }

        private CastClientException createCastClientException(Exception e) {
            return new CastClientException("CastClient error", e);
        }
    }

    // @endcond

    String currentAppId;
    String launchingAppId;

    CastClient mCastClient;
    GoogleApiClient mApiClient;
    CastListener mCastClientListener;
    ConnectionCallbacks mConnectionCallbacks;
    ConnectionFailedListener mConnectionFailedListener;

    CastDevice castDevice;
    RemoteMediaPlayer mMediaPlayer;

    Map<String, CastWebAppSession> sessions;
    List<URLServiceSubscription<?>> subscriptions;

    float currentVolumeLevel;
    boolean currentMuteStatus;
    boolean mWaitingForReconnect;
    
    static String applicationID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;

    // Queue of commands that should be sent once register is complete
    CopyOnWriteArraySet<ConnectionListener> commandQueue = new CopyOnWriteArraySet<ConnectionListener>();

    public CastService(ServiceDescription serviceDescription, ServiceConfig serviceConfig) {
        super(serviceDescription, serviceConfig);

        mCastClient = new CastClient();
        mCastClientListener = new CastListener();
        mConnectionCallbacks = new ConnectionCallbacks();
        mConnectionFailedListener = new ConnectionFailedListener();

        sessions = new HashMap<String, CastWebAppSession>();
        subscriptions = new ArrayList<URLServiceSubscription<?>>();

        mWaitingForReconnect = false;
    }

    @Override
    public String getServiceName() {
        return ID;
    }

    public static DiscoveryFilter discoveryFilter() {
        return new DiscoveryFilter(ID, "Chromecast");
    }

    public static void setApplicationID(String id) {
        applicationID = id;
    }
    
    public static String getApplicationID() {
        return applicationID;
    }

    @Override
    public CapabilityPriorityLevel getPriorityLevel(Class<? extends CapabilityMethods> clazz) {
        if (clazz.equals(MediaPlayer.class)) {
            return getMediaPlayerCapabilityLevel();
        }
        else if (clazz.equals(MediaControl.class)) {
            return getMediaControlCapabilityLevel();
        }
        else if (clazz.equals(VolumeControl.class)) {
            return getVolumeControlCapabilityLevel();
        }
        else if (clazz.equals(WebAppLauncher.class)) {
            return getWebAppLauncherCapabilityLevel();
        }
        return CapabilityPriorityLevel.NOT_SUPPORTED;
    }

    @Override
    public void connect() {
        if (castDevice == null) {
            castDevice = (CastDevice) getServiceDescription().getDevice();
        }

        if (mApiClient == null) {
            mApiClient = createApiClient();
        }

        if (!mApiClient.isConnecting() && !mApiClient.isConnected()) {
            mApiClient.connect();
        }
    }

    protected GoogleApiClient createApiClient() {
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder(castDevice, mCastClientListener);

        return new GoogleApiClient.Builder(DiscoveryManager.getInstance().getContext())
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();
    }

    @Override
    public void disconnect() {
        mWaitingForReconnect = false;
        detachMediaPlayer();
        if (!commandQueue.isEmpty()) {
            commandQueue.clear();
        }
        if (mApiClient != null && mApiClient.isConnected()) {
            try {
                mCastClient.leaveApplication(mApiClient);
            } catch (CastClientException e) {
                Log.e(Util.T, "Closing application error", e);
            }
            mApiClient.disconnect();
        }
        if (connected) {
            Util.runOnUI(new Runnable() {

                @Override
                public void run() {
                    if (getListener() != null) {
                        getListener().onDisconnect(CastService.this, null);
                    }
                }
            });
        }

        connected = false;
        mApiClient = null;
    }

    @Override
    public MediaControl getMediaControl() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getMediaControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void play(final ResponseListener<Object> listener) {
        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mMediaPlayer.play(mApiClient);
                    Util.postSuccess(listener, null);
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to play", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void pause(final ResponseListener<Object> listener) {
        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mMediaPlayer.pause(mApiClient);

                    Util.postSuccess(listener, null);
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to pause", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void stop(final ResponseListener<Object> listener) {
        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mMediaPlayer.stop(mApiClient);

                    Util.postSuccess(listener, null);
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to stop", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void rewind(ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void fastForward(ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void previous(ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void next(ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void seek(final long position, final ResponseListener<Object> listener) {
        if (mMediaPlayer == null || mMediaPlayer.getMediaStatus() == null) {
            Util.postError(listener, new ServiceCommandError(0, "There is no media currently available", null));
            return;
        }

        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mMediaPlayer.seek(mApiClient, position, RemoteMediaPlayer.RESUME_STATE_UNCHANGED).setResultCallback(
                            new ResultCallback<MediaChannelResult>() {

                                @Override
                                public void onResult(MediaChannelResult result) {
                                    Status status = result.getStatus();

                                    if (status.isSuccess()) {
                                        Util.postSuccess(listener, null);
                                    } else {
                                        Util.postError(listener, new ServiceCommandError(status.getStatusCode(), status.getStatusMessage(), status));
                                    }
                                }
                            });
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to seek", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void getDuration(final DurationListener listener) {
        if (mMediaPlayer != null && mMediaPlayer.getMediaStatus() != null) {
            Util.postSuccess(listener, mMediaPlayer.getStreamDuration());
        }
        else {
            Util.postError(listener, new ServiceCommandError(0, "There is no media currently available", null));
        }
    }

    @Override
    public void getPosition(final PositionListener listener) {
        if (mMediaPlayer != null && mMediaPlayer.getMediaStatus() != null) {
            Util.postSuccess(listener, mMediaPlayer.getApproximateStreamPosition());
        }
        else {
            Util.postError(listener, new ServiceCommandError(0, "There is no media currently available", null));
        }
    }

    @Override
    public MediaPlayer getMediaPlayer() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getMediaPlayerCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void getMediaInfo(MediaInfoListener listener) {
        if (mMediaPlayer == null)
            return;

        if (mMediaPlayer.getMediaInfo() != null) {
            String url = mMediaPlayer.getMediaInfo().getContentId();
            String mimeType = mMediaPlayer.getMediaInfo().getContentType();

            MediaMetadata metadata = mMediaPlayer.getMediaInfo().getMetadata();
            String title = null;
            String description = null;
            ArrayList<ImageInfo> list = null;

            if (metadata != null) {
                title = metadata.getString(MediaMetadata.KEY_TITLE);
                description =  metadata.getString(MediaMetadata.KEY_SUBTITLE);

                if (metadata.getImages() != null && metadata.getImages().size() > 0) {
                    String iconUrl = metadata.getImages().get(0).getUrl().toString();
                    list = new ArrayList<ImageInfo>();
                    list.add(new ImageInfo(iconUrl));
                }
            }

            MediaInfo info = new MediaInfo(url, mimeType, title, description, list);

            Util.postSuccess(listener, info);
        }
        else {
            Util.postError(listener, new ServiceCommandError(0, "Media Info is null", null));
        }
    }

    @Override
    public ServiceSubscription<MediaInfoListener> subscribeMediaInfo(
            MediaInfoListener listener) {
        URLServiceSubscription<MediaInfoListener> request = new URLServiceSubscription<MediaInfoListener>(this, "info", null, null);
        request.addListener(listener);
        addSubscription(request);

        return request;
    }

    private void attachMediaPlayer() {
        if (mMediaPlayer != null) {
            return;
        }

        mMediaPlayer = createMediaPlayer();
        mMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {

            @Override
            public void onStatusUpdated() {
                if (subscriptions.size() > 0) {
                    for (URLServiceSubscription<?> subscription : subscriptions) {
                        if (subscription.getTarget().equalsIgnoreCase(PLAY_STATE)) {
                            for (int i = 0; i < subscription.getListeners().size(); i++) {
                                @SuppressWarnings("unchecked")
                                ResponseListener<Object> listener = (ResponseListener<Object>) subscription.getListeners().get(i);
                                if (mMediaPlayer != null && mMediaPlayer.getMediaStatus() != null) {
                                    PlayStateStatus status = PlayStateStatus.convertPlayerStateToPlayStateStatus(mMediaPlayer.getMediaStatus().getPlayerState());
                                    Util.postSuccess(listener, status);
                                }
                            }
                        }
                    }
                }
            }
        });

        mMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
            @Override
            public void onMetadataUpdated() {
                if (subscriptions.size() > 0) {
                    for (URLServiceSubscription<?> subscription : subscriptions) {
                        if (subscription.getTarget().equalsIgnoreCase("info")) {
                            for (int i = 0; i < subscription.getListeners().size(); i++) {
                                MediaInfoListener listener = (MediaInfoListener) subscription.getListeners().get(i);
                                getMediaInfo(listener);
                            }
                        }
                    }
                }
            }
        });

        if (mApiClient != null) {
            try {
                mCastClient.setMessageReceivedCallbacks(mApiClient, mMediaPlayer.getNamespace(), mMediaPlayer);
            } catch (Exception e) {
                Log.w(Util.T, "Exception while creating media channel", e);
            }
        }
    }

    protected RemoteMediaPlayer createMediaPlayer() {
        return new RemoteMediaPlayer();
    }

    private void detachMediaPlayer() {
        if ((mMediaPlayer != null) && (mApiClient != null)) {
            try {
                mCastClient.removeMessageReceivedCallbacks(mApiClient, mMediaPlayer.getNamespace());
            } catch (CastClientException e) {
                Log.w(Util.T, "Exception while launching application", e);
            }
        }
        mMediaPlayer = null;
    }

    @Override
    public void displayImage(String url, String mimeType, String title,
                             String description, String iconSrc, LaunchListener listener) {
        MediaMetadata mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        mMediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        mMediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, description);

        if (iconSrc != null) {
            Uri iconUri = Uri.parse(iconSrc);
            WebImage image = new WebImage(iconUri, 100, 100);
            mMediaMetadata.addImage(image);
        }

        com.google.android.gms.cast.MediaInfo mediaInformation = new com.google.android.gms.cast.MediaInfo.Builder(url)
                .setContentType(mimeType)
                .setStreamType(com.google.android.gms.cast.MediaInfo.STREAM_TYPE_NONE)
                .setMetadata(mMediaMetadata)
                .setStreamDuration(0)
                .setCustomData(null)
                .build();

        playMedia(mediaInformation, applicationID, listener);
    }

    @Override
    public void displayImage(MediaInfo mediaInfo, LaunchListener listener) {
        String mediaUrl = null;
        String mimeType = null;
        String title = null;
        String desc = null;
        String iconSrc = null;

        if (mediaInfo != null) {
            mediaUrl = mediaInfo.getUrl();
            mimeType = mediaInfo.getMimeType();
            title = mediaInfo.getTitle();
            desc = mediaInfo.getDescription();

            if (mediaInfo.getImages() != null && mediaInfo.getImages().size() > 0) {
                ImageInfo imageInfo = mediaInfo.getImages().get(0);
                iconSrc = imageInfo.getUrl();
            }
        }

        displayImage(mediaUrl, mimeType, title, desc, iconSrc, listener);
    }

    private void playMedia(String url, SubtitleInfo subtitleInfo, String mimeType, String title,
                          String description, String iconSrc, boolean shouldLoop,
                          LaunchListener listener) {
        MediaMetadata mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mMediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        mMediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, description);

        if (iconSrc != null) {
            Uri iconUri = Uri.parse(iconSrc);
            WebImage image = new WebImage(iconUri, 100, 100);
            mMediaMetadata.addImage(image);
        }

        List<MediaTrack> mediaTracks = new ArrayList<>();
        if(subtitleInfo != null) {
            MediaTrack subtitle = new MediaTrack.Builder(MEDIA_TRACK_ID, MediaTrack.TYPE_TEXT)
                    .setName(subtitleInfo.getLabel())
                    .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    .setContentId(subtitleInfo.getUrl())
                    .setContentType(subtitleInfo.getMimeType())
                    .setLanguage(subtitleInfo.getLanguage())
                    .build();

            mediaTracks.add(subtitle);
        }

        com.google.android.gms.cast.MediaInfo mediaInformation = new com.google.android.gms.cast.MediaInfo.Builder(url)
                .setContentType(mimeType)
                .setStreamType(com.google.android.gms.cast.MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mMediaMetadata)
                .setStreamDuration(1000)
                .setCustomData(null)
                .setMediaTracks(mediaTracks)
                .build();

        playMedia(mediaInformation, applicationID, listener);
    }

    @Override
    public void playMedia(String url, String mimeType, String title,
                          String description, String iconSrc, boolean shouldLoop,
                          LaunchListener listener) {
        playMedia(url, null, mimeType, title, description, iconSrc, shouldLoop, listener);
    }

    @Override
    public void playMedia(MediaInfo mediaInfo, boolean shouldLoop, LaunchListener listener) {
        try {
            mCastClient.getApplicationStatus(mApiClient);
        } catch (CastClientException e) {
            Util.postError(listener, new ServiceCommandError(e.getMessage()));
        }
        String mediaUrl = null;
        SubtitleInfo subtitle = null;
        String mimeType = null;
        String title = null;
        String desc = null;
        String iconSrc = null;

        if (mediaInfo != null) {
            mediaUrl = mediaInfo.getUrl();
            subtitle = mediaInfo.getSubtitleInfo();
            mimeType = mediaInfo.getMimeType();
            title = mediaInfo.getTitle();
            desc = mediaInfo.getDescription();

            if (mediaInfo.getImages() != null && mediaInfo.getImages().size() > 0) {
                ImageInfo imageInfo = mediaInfo.getImages().get(0);
                iconSrc = imageInfo.getUrl();
            }
        }

        playMedia(mediaUrl, subtitle, mimeType, title, desc, iconSrc, shouldLoop, listener);
    }

    private void playMedia(final com.google.android.gms.cast.MediaInfo mediaInformation, final String mediaAppId, final LaunchListener listener) {
        final ApplicationConnectionResultCallback webAppLaunchCallback =
                new ApplicationConnectionResultCallback(new LaunchWebAppListener() {

            @Override
            public void onSuccess(final WebAppSession webAppSession) {
                ConnectionListener connectionListener = new ConnectionListener() {

                    @Override
                    public void onConnected() {
                        loadMedia(mediaInformation, webAppSession, listener);
                    }
                };

                runCommand(connectionListener);
            }

            @Override
            public void onFailure(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        });

        launchingAppId = mediaAppId;

        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                boolean relaunchIfRunning = false;

                try {
                    if (mCastClient.getApplicationStatus(mApiClient) == null || (!mediaAppId.equals(currentAppId))) {
                        relaunchIfRunning = true;
                    }

                    LaunchOptions options = new LaunchOptions();
                    options.setRelaunchIfRunning(relaunchIfRunning);
                    mCastClient.launchApplication(mApiClient, mediaAppId, options).setResultCallback(webAppLaunchCallback);
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to launch", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    private void loadMedia(com.google.android.gms.cast.MediaInfo mediaInformation,
                           final WebAppSession webAppSession, final LaunchListener listener) {
        try {
            mMediaPlayer.load(mApiClient, mediaInformation, true).setResultCallback(new ResultCallback<MediaChannelResult>() {

                @Override
                public void onResult(MediaChannelResult result) {
                    Status status = result.getStatus();

                    if (status.isSuccess()) {
                        webAppSession.launchSession.setSessionType(LaunchSessionType.Media);
                        mMediaPlayer.setActiveMediaTracks(mApiClient, new long[] { MEDIA_TRACK_ID });
                        Util.postSuccess(listener, new MediaLaunchObject(webAppSession.launchSession, CastService.this));
                    }
                    else {
                        Util.postError(listener, new ServiceCommandError(status.getStatusCode(), status.getStatusMessage(), status));
                    }
                }
            });
        } catch (Exception e) {
            Util.postError(listener, new ServiceCommandError(0, "Unable to load", null));
        }
    }

    @Override
    public void closeMedia(final LaunchSession launchSession, final ResponseListener<Object> listener) {
        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mCastClient.stopApplication(mApiClient, launchSession.getSessionId()).setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status result) {
                            if (result.isSuccess()) {
                                Util.postSuccess(listener, result);
                            } else {
                                Util.postError(listener, new ServiceCommandError(result.getStatusCode(), result.getStatusMessage(), result));
                            }
                        }
                    });
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to stop", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public WebAppLauncher getWebAppLauncher() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getWebAppLauncherCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void launchWebApp(String webAppId, WebAppSession.LaunchListener listener) {
        launchWebApp(webAppId, true, listener);
    }

    @Override
    public void launchWebApp(final String webAppId, final boolean relaunchIfRunning, final WebAppSession.LaunchListener listener) {
        launchingAppId = webAppId;

        final LaunchWebAppListener launchWebAppListener = new LaunchWebAppListener() {
            @Override
            public void onSuccess(WebAppSession webAppSession) {
                Util.postSuccess(listener, webAppSession);
            }

            @Override
            public void onFailure(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                // TODO Workaround, for some reason, if relaunchIfRunning is false, launchApplication returns 2005 error and cannot launch.
                try {
                    if (relaunchIfRunning == false) {
                        mCastClient.joinApplication(mApiClient).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {

                            @Override
                            public void onResult(ApplicationConnectionResult result) {
                                if (result.getStatus().isSuccess() &&
                                        result.getApplicationMetadata() != null &&
                                        result.getApplicationMetadata().getName() != null &&
                                        result.getApplicationMetadata().getApplicationId().equals(webAppId)) {
                                    ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
                                    currentAppId = applicationMetadata.getApplicationId();

                                    LaunchSession launchSession = LaunchSession.launchSessionForAppId(applicationMetadata.getApplicationId());
                                    launchSession.setAppName(applicationMetadata.getName());
                                    launchSession.setSessionId(result.getSessionId());
                                    launchSession.setSessionType(LaunchSessionType.WebApp);
                                    launchSession.setService(CastService.this);

                                    CastWebAppSession webAppSession = new CastWebAppSession(launchSession, CastService.this);
                                    webAppSession.setMetadata(applicationMetadata);

                                    sessions.put(applicationMetadata.getApplicationId(), webAppSession);

                                    Util.postSuccess(listener, webAppSession);
                                }
                                else {
                                    LaunchOptions options = new LaunchOptions();
                                    options.setRelaunchIfRunning(true);

                                    try {
                                        mCastClient.launchApplication(mApiClient, webAppId, options).setResultCallback(
                                                new ApplicationConnectionResultCallback(launchWebAppListener));
                                    } catch (Exception e) {
                                        Util.postError(listener, new ServiceCommandError(0, "Unable to launch", null));
                                    }
                                }
                            }
                        });
                    }
                    else {
                        LaunchOptions options = new LaunchOptions();
                        options.setRelaunchIfRunning(relaunchIfRunning);

                        mCastClient.launchApplication(mApiClient, webAppId, options).setResultCallback(
                                new ApplicationConnectionResultCallback(launchWebAppListener)
                        );
                    }
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to launch", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void launchWebApp(String webAppId, JSONObject params, WebAppSession.LaunchListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void launchWebApp(String webAppId, JSONObject params, boolean relaunchIfRunning, WebAppSession.LaunchListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }
    
    public void requestStatus(final ResponseListener<Object> listener) {
        try {
            mMediaPlayer
            .requestStatus(mApiClient)
            .setResultCallback(
                    new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

                        @Override
                        public void onResult(MediaChannelResult result) {
                            if (result.getStatus().isSuccess()) {
                                Util.postSuccess(listener, result);
                            }
                            else {
                                Util.postError(listener, new ServiceCommandError(0, "Failed to request status", result));
                            }
                        }
                    });
        } catch (Exception e) {
            Util.postError(listener, new ServiceCommandError(0, "There is no media currently available", null));
        }
    }

    public void joinApplication(final ResponseListener<Object> listener) {
        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mCastClient.joinApplication(mApiClient).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {

                        @Override
                        public void onResult(ApplicationConnectionResult result) {
                            if (result.getStatus().isSuccess()) {
                                // TODO: Maybe there is better way to check current cast device is showing backdrop, but for now, if chromecast is showing backdrop, then requestStatus would never response.
                                if (result.getApplicationMetadata() != null &&
                                    result.getApplicationMetadata().getName() != null &&
                                    !result.getApplicationMetadata().getName().equals("Backdrop") &&
                                    mMediaPlayer != null && mApiClient != null) {

                                    mMediaPlayer.requestStatus(mApiClient).setResultCallback(
                                        new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

                                            @Override
                                            public void onResult(MediaChannelResult result) {
                                                Util.postSuccess(listener, result);
                                            }
                                    });
                                }
                                else {
                                    Util.postSuccess(listener, result);
                                }
                            }
                            else {
                                Util.postError(listener, new ServiceCommandError(0, "Failed to join application", result));
                            }
                        }
                    });
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to join", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void joinWebApp(final LaunchSession webAppLaunchSession, final WebAppSession.LaunchListener listener) {
        final ApplicationConnectionResultCallback webAppLaunchCallback = new ApplicationConnectionResultCallback(new LaunchWebAppListener() {

            @Override
            public void onSuccess(final WebAppSession webAppSession) {
                webAppSession.connect(new ResponseListener<Object>() {

                    @Override
                    public void onSuccess(Object object) {
                        requestStatus(new ResponseListener<Object>() {
                            @Override
                            public void onSuccess(Object object) {
                                Util.postSuccess(listener, webAppSession);
                            }

                            @Override
                            public void onError(ServiceCommandError error) {
                                // we sent success, because join is already succeeded.
                                Util.postSuccess(listener, webAppSession);
                            }
                        });
                    }

                    @Override
                    public void onError(ServiceCommandError error) {
                        Util.postError(listener, error);
                    }
                });
            }

            @Override
            public void onFailure(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        });

        launchingAppId = webAppLaunchSession.getAppId();

        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mCastClient.joinApplication(mApiClient, webAppLaunchSession.getAppId()).setResultCallback(webAppLaunchCallback);
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to join", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void joinWebApp(String webAppId, WebAppSession.LaunchListener listener) {
        LaunchSession launchSession = LaunchSession.launchSessionForAppId(webAppId);
        launchSession.setSessionType(LaunchSessionType.WebApp);
        launchSession.setService(this);

        joinWebApp(launchSession, listener);
    }

    @Override
    public void closeWebApp(LaunchSession launchSession, final ResponseListener<Object> listener) {
        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mCastClient.stopApplication(mApiClient).setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Util.postSuccess(listener, null);
                            }
                            else {
                                Util.postError(listener, new ServiceCommandError(status.getStatusCode(), status.getStatusMessage(), status));
                            }
                        }
                    });
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "Unable to stop", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void pinWebApp(String webAppId, ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void unPinWebApp(String webAppId, ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void isWebAppPinned(String webAppId, WebAppPinStatusListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public ServiceSubscription<WebAppPinStatusListener> subscribeIsWebAppPinned(
            String webAppId, WebAppPinStatusListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
        return null;
    }

    @Override
    public VolumeControl getVolumeControl() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getVolumeControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void volumeUp(final ResponseListener<Object> listener) {
        getVolume(new VolumeListener() {

            @Override
            public void onSuccess(final Float volume) {
                if (volume >= 1.0) {
                    Util.postSuccess(listener, null);
                }
                else {
                    float newVolume = (float)(volume + 0.01);

                    if (newVolume > 1.0)
                        newVolume = (float)1.0;

                    setVolume(newVolume, listener);

                    Util.postSuccess(listener, null);
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        });
    }

    @Override
    public void volumeDown(final ResponseListener<Object> listener) {
        getVolume(new VolumeListener() {

            @Override
            public void onSuccess(final Float volume) {
                if (volume <= 0.0) {
                    Util.postSuccess(listener, null);
                }
                else {
                    float newVolume = (float)(volume - 0.01);

                    if (newVolume < 0.0)
                        newVolume = (float)0.0;

                    setVolume(newVolume, listener);

                    Util.postSuccess(listener, null);
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        });
    }

    @Override
    public void setVolume(final float volume, final ResponseListener<Object> listener) {
        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mCastClient.setVolume(mApiClient, volume);
                    Util.postSuccess(listener, null);
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "setting volume level failed", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void getVolume(VolumeListener listener) {
        Util.postSuccess(listener, currentVolumeLevel);
    }

    @Override
    public void setMute(final boolean isMute, final ResponseListener<Object> listener) {
        ConnectionListener connectionListener = new ConnectionListener() {

            @Override
            public void onConnected() {
                try {
                    mCastClient.setMute(mApiClient, isMute);
                    Util.postSuccess(listener, null);
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(0, "setting mute status failed", null));
                }
            }
        };

        runCommand(connectionListener);
    }

    @Override
    public void getMute(final MuteListener listener) {
        Util.postSuccess(listener, currentMuteStatus);
    }

    @Override
    public ServiceSubscription<VolumeListener> subscribeVolume(VolumeListener listener) {
        URLServiceSubscription<VolumeListener> request = new URLServiceSubscription<VolumeListener>(this, CAST_SERVICE_VOLUME_SUBSCRIPTION_NAME, null, null);
        request.addListener(listener);
        addSubscription(request);

        return request;
    }

    @Override
    public ServiceSubscription<MuteListener> subscribeMute(MuteListener listener) {
        URLServiceSubscription<MuteListener> request = new URLServiceSubscription<MuteListener>(this, CAST_SERVICE_MUTE_SUBSCRIPTION_NAME, null, null);
        request.addListener(listener);
        addSubscription(request);

        return request;
    }

    @Override
    protected void updateCapabilities() {
        List<String> capabilities = new ArrayList<String>();

        Collections.addAll(capabilities, MediaPlayer.Capabilities);
        capabilities.add(Subtitle_WebVTT);


        Collections.addAll(capabilities, VolumeControl.Capabilities);

        capabilities.add(Play);
        capabilities.add(Pause);
        capabilities.add(Stop);
        capabilities.add(Duration);
        capabilities.add(Seek);
        capabilities.add(Position);
        capabilities.add(PlayState);
        capabilities.add(PlayState_Subscribe);

        capabilities.add(WebAppLauncher.Launch);
        capabilities.add(Message_Send);
        capabilities.add(Message_Receive);
        capabilities.add(Message_Send_JSON);
        capabilities.add(Message_Receive_JSON);
        capabilities.add(WebAppLauncher.Connect);
        capabilities.add(WebAppLauncher.Disconnect);
        capabilities.add(WebAppLauncher.Join);
        capabilities.add(WebAppLauncher.Close);

        setCapabilities(capabilities);
    }

    private class CastListener extends Cast.Listener {
        @Override
        public void onApplicationDisconnected(int statusCode) {
            Log.d(Util.T, "Cast.Listener.onApplicationDisconnected: " + statusCode);

            if (currentAppId == null)
                return;

            CastWebAppSession webAppSession = sessions.get(currentAppId);

            if (webAppSession == null)
                return;

            webAppSession.handleAppClose();

            currentAppId = null;
        }

        @Override
        public void onApplicationStatusChanged() {
            ConnectionListener connectionListener = new ConnectionListener() {

                @Override
                public void onConnected() {
                    if (mApiClient != null) {
                        ApplicationMetadata applicationMetadata = null;
                        try {
                            applicationMetadata = mCastClient.getApplicationMetadata(mApiClient);
                            if (applicationMetadata != null) {
                                currentAppId = applicationMetadata.getApplicationId();
                            }
                        } catch (CastClientException e) {
                            Log.e(Util.T, "Error in onApplicationStatusChanged", e);
                        }
                    }
                }
            };

            runCommand(connectionListener);
        }

        @Override
        public void onVolumeChanged() {
            ConnectionListener connectionListener = new ConnectionListener() {

                @Override
                public void onConnected() {
                    try {
                        currentVolumeLevel = (float) mCastClient.getVolume(mApiClient);
                        currentMuteStatus = mCastClient.isMute(mApiClient);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (subscriptions.size() > 0) {
                        for (URLServiceSubscription<?> subscription: subscriptions) {
                            if (subscription.getTarget().equals(CAST_SERVICE_VOLUME_SUBSCRIPTION_NAME)) {
                                for (int i = 0; i < subscription.getListeners().size(); i++) {
                                    @SuppressWarnings("unchecked")
                                    ResponseListener<Object> listener = (ResponseListener<Object>) subscription.getListeners().get(i);

                                    Util.postSuccess(listener, currentVolumeLevel);
                                }
                            }
                            else if (subscription.getTarget().equals(CAST_SERVICE_MUTE_SUBSCRIPTION_NAME)) {
                                for (int i = 0; i < subscription.getListeners().size(); i++) {
                                    @SuppressWarnings("unchecked")
                                    ResponseListener<Object> listener = (ResponseListener<Object>) subscription.getListeners().get(i);

                                    Util.postSuccess(listener, currentMuteStatus);
                                }
                            }
                        }
                    }
                }
            };

            runCommand(connectionListener);
        }
    }

    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnectionSuspended(final int cause) {
            Log.d(Util.T, "ConnectionCallbacks.onConnectionSuspended");

            mWaitingForReconnect = true;
            detachMediaPlayer();
        }

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(Util.T, "ConnectionCallbacks.onConnected, wasWaitingForReconnect: " + mWaitingForReconnect);

            attachMediaPlayer();

            if (mApiClient != null && mApiClient.isConnected()) {
                try {
                    mCastClient.joinApplication(mApiClient)
                            .setResultCallback(new ResultCallback<ApplicationConnectionResult>() {

                                @Override
                                public void onResult(ApplicationConnectionResult result) {
                                    onJoinApplicationResult(result);
                                }
                            });
                } catch (CastClientException e) {
                    Log.e(Util.T, "join application error", e);
                }
            }
        }

        private void onJoinApplicationResult(ApplicationConnectionResult result) {
            if (result.getStatus().isSuccess()) {
                // TODO: Maybe there is better way to check current cast device is
                // showing backdrop, but for now, if chromecast is showing backdrop,
                // then requestStatus would never response.
                if (result.getApplicationMetadata() != null &&
                    result.getApplicationMetadata().getName() != null &&
                    !result.getApplicationMetadata().getName().equals("Backdrop") &&
                    mMediaPlayer != null && mApiClient != null) {

                    mMediaPlayer.requestStatus(mApiClient).setResultCallback(
                        new ResultCallback<MediaChannelResult>() {

                            @Override
                            public void onResult(MediaChannelResult result) {
                                joinFinished();
                            }
                    });
                }
                else {
                    joinFinished();
                }
            }
            else {
                joinFinished();
            }
        }

        private void joinFinished() {
            if (mWaitingForReconnect) {
                mWaitingForReconnect = false;
            }
            else {
                connected = true;

                reportConnected(true);
            }

            if (!commandQueue.isEmpty()) {
                for (ConnectionListener listener : commandQueue) {
                    listener.onConnected();
                    commandQueue.remove(listener);
                }
            }
        }
    }

    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(final ConnectionResult result) {
            Log.d(Util.T, "ConnectionFailedListener.onConnectionFailed " + (result != null ? result: ""));

            detachMediaPlayer();
            connected = false;
            mWaitingForReconnect = false;
            mApiClient = null;


            Util.runOnUI(new Runnable() {

                @Override
                public void run() {
                    if (listener != null) {
                        ServiceCommandError error = new ServiceCommandError(result.getErrorCode(), "Failed to connect to Google Cast device", result);

                        listener.onConnectionFailure(CastService.this, error);
                    }
                }
            });
        }
    }

    private class ApplicationConnectionResultCallback implements
            ResultCallback<Cast.ApplicationConnectionResult> {
        LaunchWebAppListener listener;

        public ApplicationConnectionResultCallback(LaunchWebAppListener listener) {
            this.listener = listener;
        }

        @Override
        public void onResult(ApplicationConnectionResult result) {
            Status status = result.getStatus();

            if (status.isSuccess()) {
                ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
                currentAppId = applicationMetadata.getApplicationId();

                LaunchSession launchSession = LaunchSession.launchSessionForAppId(applicationMetadata.getApplicationId());
                launchSession.setAppName(applicationMetadata.getName());
                launchSession.setSessionId(result.getSessionId());
                launchSession.setSessionType(LaunchSessionType.WebApp);
                launchSession.setService(CastService.this);

                CastWebAppSession webAppSession = new CastWebAppSession(launchSession, CastService.this);
                webAppSession.setMetadata(applicationMetadata);

                sessions.put(applicationMetadata.getApplicationId(), webAppSession);

                if (listener != null) {
                    listener.onSuccess(webAppSession);
                }

                launchingAppId = null;
            }
            else {
                if (listener != null) {
                    listener.onFailure(new ServiceCommandError(status.getStatusCode(), status.getStatusMessage(), status));
                }
            }
        }
    }

    @Override
    public void getPlayState(PlayStateListener listener) {
        if (mMediaPlayer != null && mMediaPlayer.getMediaStatus() != null) {
            PlayStateStatus status = PlayStateStatus.convertPlayerStateToPlayStateStatus(mMediaPlayer.getMediaStatus().getPlayerState());
            Util.postSuccess(listener, status);
        }
        else {
            Util.postError(listener, new ServiceCommandError(0, "There is no media currently available", null));
        }
    }

    public GoogleApiClient getApiClient() {
        return mApiClient;
    }

    //////////////////////////////////////////////////
    //      Device Service Methods
    //////////////////////////////////////////////////
    @Override
    public boolean isConnectable() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public ServiceSubscription<PlayStateListener> subscribePlayState(PlayStateListener listener) {
        URLServiceSubscription<PlayStateListener> request = new URLServiceSubscription<PlayStateListener>(this, PLAY_STATE, null, null);
        request.addListener(listener);
        addSubscription(request);

        return request;
    }

    private void addSubscription(URLServiceSubscription<?> subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public void unsubscribe(URLServiceSubscription<?> subscription) {
        subscriptions.remove(subscription);
    }

    public List<URLServiceSubscription<?>> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<URLServiceSubscription<?>> subscriptions) {
        this.subscriptions = subscriptions;
    }

    private void runCommand(ConnectionListener connectionListener) {
        if (mApiClient != null && mApiClient.isConnected()) {
            connectionListener.onConnected();
        }
        else {
            connect();
            commandQueue.add(connectionListener);
        }
    }

}
