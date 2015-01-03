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

import android.view.View;
import android.widget.SeekBar;

import com.google.android.gms.cast.MediaTrack;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;


import com.google.sample.castcompanionlibrary.cast.tracks.ui.TracksChooserDialog;

import java.util.List;

public interface OnVideoCastControllerListener extends TracksChooserDialog.OnTracksSelectedListener{

    /**
     * Called when seeking is stopped by user.
     *
     * @param seekBar
     */
    public void onStopTrackingTouch(SeekBar seekBar);

    /**
     * Called when seeking starts by user
     *
     * @param seekBar
     */
    public void onStartTrackingTouch(SeekBar seekBar);

    /**
     * Called while seeking is happening by the user
     *
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

    /**
     * Notification that user has clicked on the Play/Pause button
     *
     * @param v
     * @throws TransientNetworkDisconnectionException
     * @throws NoConnectionException
     * @throws CastException
     */
    public void onPlayPauseClicked(View v) throws CastException,
            TransientNetworkDisconnectionException, NoConnectionException;

    /**
     * Called when a configuration change happens (for example device is rotated)
     */
    public void onConfigurationChanged();

}
