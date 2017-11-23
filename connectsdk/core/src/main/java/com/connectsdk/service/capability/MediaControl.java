/*
 * MediaControl
 * Connect SDK
 * 
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 19 Jan 2014
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
import com.connectsdk.service.command.ServiceSubscription;

public interface MediaControl extends CapabilityMethods {

    String Any = "MediaControl.Any";

    String Play = "MediaControl.Play";
    String Pause = "MediaControl.Pause";
    String Stop = "MediaControl.Stop";
    String Rewind = "MediaControl.Rewind";
    String FastForward = "MediaControl.FastForward";
    String Seek = "MediaControl.Seek";
    String Duration = "MediaControl.Duration";
    String PlayState = "MediaControl.PlayState";
    String PlayState_Subscribe = "MediaControl.PlayState.Subscribe";
    String Position = "MediaControl.Position";

    /**
     * This capability is deprecated. Use `PlaylistControl.Previous` instead.
     */
    @Deprecated String Previous = "MediaControl.Previous";

    /**
     * This capability is deprecated. Use `PlaylistControl.Next` instead.
     */
    @Deprecated String Next = "MediaControl.Next";


    int PLAYER_STATE_UNKNOWN = 0;
    int PLAYER_STATE_IDLE = 1;
    int PLAYER_STATE_PLAYING = 2;
    int PLAYER_STATE_PAUSED = 3;
    int PLAYER_STATE_BUFFERING = 4;

    String[] Capabilities = {
            Play,
            Pause,
            Stop,
            Rewind,
            FastForward,
            Seek,
            Previous,
            Next,
            Duration,
            PlayState,
            PlayState_Subscribe,
            Position,
    };

    /**
     * Enumerates possible playback status
     */
    enum PlayStateStatus {
        /**
         * Unknown state
         */
        Unknown,

        /**
         * Media source is not set.
         */
        Idle,

        /**
         * Media is playing.
         */
        Playing,

        /**
         * Media is paused.
         */
        Paused,

        /**
         * Media is buffering on the first screen device (e.g. on the TV)
         */
        Buffering,

        /**
         * Playback is finished.
         */
        Finished;

        /**
         * Converts int value into PlayStateStatus
         *
         * @param playerState int value
         * @return PlayStateStatus
         */
        public static PlayStateStatus convertPlayerStateToPlayStateStatus(int playerState) {
            PlayStateStatus status;

            switch (playerState) {
                case PLAYER_STATE_BUFFERING:
                    status = PlayStateStatus.Buffering;
                    break;
                case PLAYER_STATE_IDLE:
                    status = PlayStateStatus.Finished;
                    break;
                case PLAYER_STATE_PAUSED:
                    status = PlayStateStatus.Paused;
                    break;
                case PLAYER_STATE_PLAYING:
                    status = PlayStateStatus.Playing;
                    break;
                case PLAYER_STATE_UNKNOWN:
                default:
                    status = PlayStateStatus.Unknown;
                    break;
            }

            return status;
        }

        /**
         * Converts String value into PlayStateStatus
         *
         * @param transportState String value
         * @return PlayStateStatus
         */
        public static PlayStateStatus convertTransportStateToPlayStateStatus(String transportState) {
            PlayStateStatus status = PlayStateStatus.Unknown;

            if (transportState.equals("STOPPED")) {
                status = PlayStateStatus.Finished;
            } else if (transportState.equals("PLAYING")) {
                status = PlayStateStatus.Playing;
            } else if (transportState.equals("TRANSITIONING")) {
                status = PlayStateStatus.Buffering;
            } else if (transportState.equals("PAUSED_PLAYBACK")) {
                status = PlayStateStatus.Paused;
            } else if (transportState.equals("PAUSED_RECORDING")) {

            } else if (transportState.equals("RECORDING")) {

            } else if (transportState.equals("NO_MEDIA_PRESENT")) {

            }
            return status;
        }
    }

    /**
     * Get MediaControl implementation
     *
     * @return MediaControl
     */
    MediaControl getMediaControl();

    /**
     * Get a capability priority for current implementation
     *
     * @return CapabilityPriorityLevel
     */
    CapabilityPriorityLevel getMediaControlCapabilityLevel();

    void play(ResponseListener<Object> listener);

    void pause(ResponseListener<Object> listener);

    void stop(ResponseListener<Object> listener);

    void rewind(ResponseListener<Object> listener);

    void fastForward(ResponseListener<Object> listener);

    /**
     * This method is deprecated.
     * Use `PlaylistControl#previous(ResponseListener<Object> listener)` instead.
     */
    @Deprecated void previous(ResponseListener<Object> listener);

    /**
     * This method is deprecated.
     * Use `PlaylistControl#next(ResponseListener<Object> listener)` instead.
     */
    @Deprecated void next(ResponseListener<Object> listener);

    /**
     * @param position The new position, in milliseconds from the beginning of the stream
     * @param listener (optional) ResponseListener< Object > with methods to be called on success or failure
     */
    void seek(long position, ResponseListener<Object> listener);

    /**
     * Get the current media duration in milliseconds
     */
    void getDuration(DurationListener listener);

    /**
     * Get the current playback position in milliseconds
     */
    void getPosition(PositionListener listener);

    /**
     * Get the current state of playback
     */
    void getPlayState(PlayStateListener listener);

    /**
     * Subscribe for playback state changes
     *
     * @param listener receives play state notifications
     * @return ServiceSubscription<PlayStateListener>
     */
    ServiceSubscription<PlayStateListener> subscribePlayState(PlayStateListener listener);

    /**
     * Success block that is called upon any change in a media file's play state.
     *
     * Passes a PlayStateStatus enum of the current media file
     */
    interface PlayStateListener extends ResponseListener<PlayStateStatus> {

    }

    /**
     * Success block that is called upon successfully getting the media file's current playhead position.
     *
     * Passes the position of the current playhead position of the current media file, in seconds
     */
    interface PositionListener extends ResponseListener<Long> {

    }

    /**
     * Success block that is called upon successfully getting the media file's duration.
     *
     * Passes the duration of the current media file, in seconds
     */
    interface DurationListener extends ResponseListener<Long> {

    }
}
