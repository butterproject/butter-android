/*
 * PlaylistControl
 * Connect SDK
 * 
 * Copyright (c) 2015 LG Electronics.
 * Created by Oleksii Frolov on 15 Jan 2015
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

package com.connectsdk.service.capability;

import com.connectsdk.service.capability.listeners.ResponseListener;

public interface PlaylistControl extends CapabilityMethods {
    public final static String Any = "PlaylistControl.Any";
    public final static String JumpToTrack = "PlaylistControl.JumpToTrack";
    public final static String SetPlayMode = "PlaylistControl.SetPlayMode";
    public final static String Previous = "PlaylistControl.Previous";
    public final static String Next = "PlaylistControl.Next";


    public final static String[] Capabilities = {
        Previous,
        Next,
        JumpToTrack,
        SetPlayMode,
        JumpToTrack,
    };

    public static enum PlayMode {
        Normal,
        Shuffle,
        RepeatOne,
        RepeatAll,
    }

    public PlaylistControl getPlaylistControl();

    public CapabilityPriorityLevel getPlaylistControlCapabilityLevel();

    /**
     * Jump to previous track in the playlist
     * @param listener
     */
    public void previous(ResponseListener<Object> listener);

    /**
     * Jump to next track in the playlist
     * @param listener
     */
    public void next(ResponseListener<Object> listener);

    /**
     * This method is used for playlist only and it allows to switch to another track by it's position
     *
     * @param index index in the playlist, it starts from zero like index of array
     * @param listener
     */
    public void jumpToTrack(long index, ResponseListener<Object> listener);

    /**
     * Set order of playing tracks
     *
     * @param playMode
     */
    public void setPlayMode(PlayMode playMode, ResponseListener<Object> listener);

}
