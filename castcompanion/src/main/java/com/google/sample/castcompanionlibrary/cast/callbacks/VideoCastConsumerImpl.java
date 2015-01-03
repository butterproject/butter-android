/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.google.sample.castcompanionlibrary.cast.callbacks;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.TextTrackStyle;

import java.util.Locale;

/**
 * This is a no-ops implementation of {@link IVideoCastConsumer} so that the clients that like to
 * (partially) implement {@link IVideoCastConsumer} can extend this class and only override the
 * desired methods.
 */
public class VideoCastConsumerImpl extends BaseCastConsumerImpl
        implements IVideoCastConsumer {

    @Override
    public void onApplicationConnected(ApplicationMetadata appMetadata,
            String sessionId, boolean wasLaunched) {
    }

    @Override
    public boolean onApplicationConnectionFailed(int errorCode) {
        return true;
    }

    @Override
    public void onApplicationStatusChanged(String appStatus) {
    }

    @Override
    public void onApplicationDisconnected(int errorCode) {
    }

    @Override
    public void onRemoteMediaPlayerMetadataUpdated() {
    }

    @Override
    public void onRemoteMediaPlayerStatusUpdated() {
    }

    @Override
    public void onVolumeChanged(double value, boolean isMute) {
    }

    @Override
    public void onApplicationStopFailed(int errorCode) {
    }

    @Override
    public void onRemovedNamespace() {
    }

    @Override
    public void onDataMessageSendFailed(int errorCode) {
    }

    @Override
    public void onDataMessageReceived(String message) {
    }

    @Override
    public void onTextTrackStyleChanged(TextTrackStyle style) {
    }

    @Override
    public void onTextTrackEnabledChanged(boolean isEnabled) {
    }

    @Override
    public void onTextTrackLocaleChanged(Locale locale) {
    }

}
