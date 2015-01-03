/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
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

package com.google.sample.castcompanionlibrary.cast.player;

import android.graphics.Bitmap;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;
import com.google.sample.castcompanionlibrary.cast.tracks.ui.TracksChooserDialog;
import com.google.sample.castcompanionlibrary.widgets.MiniController.OnMiniControllerChangedListener;

public interface IVideoCastController {

    public static final int CC_ENABLED = 1;
    public static final int CC_DISABLED = 2;
    public static final int CC_HIDDEN = 3;

    /**
     * Sets the bitmap for the album art
     *
     * @param bitmap
     */
    public void setImage(Bitmap bitmap);

    /**
     * Sets the title
     *
     * @param text
     */
    public void setLine1(String text);

    /**
     * Sets the subtitle
     *
     * @param text
     */
    public void setLine2(String text);

    /**
     * Sets the playback state, and the idleReason (this is only reliable when the state is idle).
     * Values that can be passed to this method are from {@link MediaStatus}
     *
     * @param state
     * @param idleReason
     */
    public void setPlaybackStatus(int state);

    /**
     * Assigns a {@link OnMiniControllerChangedListener} listener to be notified of the changes in
     * the mini controller
     *
     * @param listener
     */
    public void setOnVideoCastControllerChangedListener(OnVideoCastControllerListener listener);

    /**
     * Sets the type of stream. <code>streamType</code> can be {@link MediaInfo.STREAM_TYPE_LIVE} or
     * {@link MediaInfo.STREAM_TYPE_BUFFERED}
     *
     * @param streamType
     */
    public void setStreamType(int streamType);

    public void updateSeekbar(int position, int duration);

    public void updateControllersStatus(boolean enabled);

    public void showLoading(boolean visible);

    public void closeActivity();

    public void adjustControllersForLiveStream(boolean isLive);

    /**
     * Updates the visual status of the Closed Caption icon. Possible states are provided by
     * <code>CC_ENABLED, CC_DISABLED, CC_HIDDEN</code>
     * @param status
     */
    public void updateClosedCaption(int status);
}
