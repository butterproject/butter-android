/*
 * MediaPlayer
 * Connect SDK
 * 
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on Jan 19 2014
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

import com.connectsdk.core.MediaInfo;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceSubscription;
import com.connectsdk.service.sessions.LaunchSession;

public interface MediaPlayer extends CapabilityMethods {

    String Any = "MediaPlayer.Any";

    /**
     * This capability is deprecated. Use `MediaPlayer.Play_Video` instead.
     */
    @Deprecated String Display_Video = "MediaPlayer.Play.Video";

    /**
     * This capability is deprecated. Use `MediaPlayer.Play_Audio` instead.
     */
    @Deprecated String Display_Audio = "MediaPlayer.Play.Audio";

    String Display_Image = "MediaPlayer.Display.Image";
    String Play_Video = "MediaPlayer.Play.Video";
    String Play_Audio = "MediaPlayer.Play.Audio";
    String Play_Playlist = "MediaPlayer.Play.Playlist";
    String Close = "MediaPlayer.Close";
    String Loop = "MediaPlayer.Loop";
    String Subtitles_Vtt = "MediaPlayer.Subtitles.Vtt";
    String Subtitles_Srt = "MediaPlayer.Subtitles.Srt";

    String MetaData_Title = "MediaPlayer.MetaData.Title";
    String MetaData_Description = "MediaPlayer.MetaData.Description";
    String MetaData_Thumbnail = "MediaPlayer.MetaData.Thumbnail";
    String MetaData_MimeType = "MediaPlayer.MetaData.MimeType";

    String MediaInfo_Get = "MediaPlayer.MediaInfo.Get";
    String MediaInfo_Subscribe = "MediaPlayer.MediaInfo.Subscribe";

    String[] Capabilities = {
            Display_Image,
            Play_Video,
            Play_Audio,
            Close,
            MetaData_Title,
            MetaData_Description,
            MetaData_Thumbnail,
            MetaData_MimeType,
            MediaInfo_Get,
            MediaInfo_Subscribe
    };

    MediaPlayer getMediaPlayer();

    CapabilityPriorityLevel getMediaPlayerCapabilityLevel();

    void getMediaInfo(MediaInfoListener listener);

    ServiceSubscription<MediaInfoListener> subscribeMediaInfo(MediaInfoListener listener);

    void displayImage(MediaInfo mediaInfo, LaunchListener listener);

    void playMedia(MediaInfo mediaInfo, boolean shouldLoop, LaunchListener listener);

    void closeMedia(LaunchSession launchSession, ResponseListener<Object> listener);

    /**
     * This method is deprecated.
     * Use `MediaPlayer#displayImage(MediaInfo mediaInfo, LaunchListener listener)` instead.
     */
    @Deprecated void displayImage(String url, String mimeType, String title, String description, String iconSrc, LaunchListener listener);

    /**
     * This method is deprecated.
     * Use `MediaPlayer#playMedia(MediaInfo mediaInfo, boolean shouldLoop, LaunchListener listener)`
     * instead.
     */
    @Deprecated void playMedia(String url, String mimeType, String title, String description, String iconSrc, boolean shouldLoop, LaunchListener listener);

    /**
     * Success block that is called upon successfully playing/displaying a media file.
     *
     * Passes a MediaLaunchObject which contains the objects for controlling media playback.
     */
    interface LaunchListener extends ResponseListener<MediaLaunchObject> { }

    /**
     * Helper class used with the MediaPlayer.LaunchListener to return the current media playback.
     */
    class MediaLaunchObject {
        /** The LaunchSession object for the media launched. */
        public LaunchSession launchSession;
        /** The MediaControl object for the media launched. */
        public MediaControl mediaControl;
        /** The PlaylistControl object for the media launched */
        public PlaylistControl playlistControl;

        public MediaLaunchObject(LaunchSession launchSession, MediaControl mediaControl) {
            this.launchSession = launchSession;
            this.mediaControl = mediaControl;
        }

        public MediaLaunchObject(LaunchSession launchSession, MediaControl mediaControl, PlaylistControl playlistControl) {
            this.launchSession = launchSession;
            this.mediaControl = mediaControl;
            this.playlistControl = playlistControl;
        }
    }

    interface MediaInfoListener extends ResponseListener<com.connectsdk.core.MediaInfo> { }

}
