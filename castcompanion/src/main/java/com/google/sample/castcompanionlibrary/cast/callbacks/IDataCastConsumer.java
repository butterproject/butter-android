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
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.Status;

public interface IDataCastConsumer extends IBaseCastConsumer {
    /**
     * Called when the application is successfully launched or joined. Upon successful connection, a
     * session ID is returned. <code>wasLaunched</code> indicates if the application was launched or
     * joined.
     * 
     * @param appMetadata
     * @param applicationStatus
     * @param sessionId
     * @param wasLaunched
     */
    public void onApplicationConnected(ApplicationMetadata appMetadata,
            String applicationStatus, String sessionId, boolean wasLaunched);

    /**
     * Called when the current application has stopped
     * 
     * @param errorCode
     */
    public void onApplicationDisconnected(int errorCode);

    /**
     * Called when an attempt to stop a receiver application has failed.
     * 
     * @param errorCode
     */
    public void onApplicationStopFailed(int errorCode);

    /**
     * Called when an application launch has failed. Failure reason is captured in the
     * <code>errorCode</code> argument. Here is a list of possible values:
     * <ul>
     * <li>4 : Application not found
     * <li>5 : Application not currently running
     * <li>6 : Application already running
     * </ul>
     * If this method returns <code>true</code>, then the library will provide an error dialog to
     * inform the user. Clients can extend this method and return <code>false</code> to handle the
     * error message themselves.
     * 
     * @param errorCode
     * @return <code>true</code> if you want the library handle the error message
     */
    public boolean onApplicationConnectionFailed(int errorCode);

    /**
     * Called when application status changes. The argument is built by the receiver
     * 
     * @param appStatus
     */
    public void onApplicationStatusChanged(String appStatus);

    /**
     * Called when the device's volume is changed. Note not to mix that with the stream's volume
     * 
     * @param value
     * @param isMute
     */
    public void onVolumeChanged(double value, boolean isMute);

    /**
     * Called when a message is received from a given {@link CastDevice} for a given
     * <code>namespace</code>.
     * 
     * @param castDevice
     * @param namespace
     * @param message
     */
    public void onMessageReceived(CastDevice castDevice, String namespace, String message);

    /**
     * Called when there is an error sending a message.
     * 
     * @param status The status of the result
     */
    public void onMessageSendFailed(Status status);

    /**
     * Called when this callback is removed from the Cast object.
     * 
     * @param castDevice The castDevice from where the message originated.
     * @param namespace The associated namespace of the removed listener.
     */
    public void onRemoved(CastDevice castDevice, String namespace);
}
