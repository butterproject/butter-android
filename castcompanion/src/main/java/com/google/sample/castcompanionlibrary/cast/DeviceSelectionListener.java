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

package com.google.sample.castcompanionlibrary.cast;

import com.google.android.gms.cast.CastDevice;

import android.support.v7.media.MediaRouter.RouteInfo;

/**
 * An interface that will be used to inform clients that a {@link CastDevice} is discovered by the
 * system or selected by the user.
 */
public interface DeviceSelectionListener {

    /**
     * Called when a {@link CastDevice} is extracted from the {@link RouteInfo}. This is where all
     * the fun starts!
     *
     * @param device
     */
    public void onDeviceSelected(CastDevice device);

    /**
     * Called as soon as a non-default {@link RouteInfo} is discovered. The main usage for this is
     * to provide a hint to clients that the cast button is going to become visible/available soon.
     * A client, for example, can use this to show a quick help screen to educate the user on the
     * cast concept and the usage of the cast button.
     *
     * @param route
     */
    public void onCastDeviceDetected(RouteInfo route);

}
