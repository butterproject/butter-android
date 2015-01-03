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

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.ConnectionResult;

import android.support.v7.media.MediaRouter.RouteInfo;

/**
 * A no-op implementation of the {@link IBaseCastConsumer}
 */
public class BaseCastConsumerImpl implements IBaseCastConsumer {

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public boolean onConnectionFailed(ConnectionResult result) {
        return true;
    }

    @Override
    public void onCastDeviceDetected(RouteInfo info) {
    }

    @Override
    public void onCastAvailabilityChanged(boolean castPresent) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectivityRecovered() {
    }

    @Override
    public void onUiVisibilityChanged(boolean visible) {
    }

    @Override
    public void onReconnectionStatusChanged(int status) {
    }

    @Override
    public void onDeviceSelected(CastDevice device) {
    }

    @Override
    public void onFailed(int resourceId, int statusCode) {
    }

}
