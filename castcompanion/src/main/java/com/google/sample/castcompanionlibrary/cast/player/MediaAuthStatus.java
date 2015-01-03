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

public enum MediaAuthStatus {
    /* Service has not started yet */
    NOT_STARTED,

    /* Service is running but no results is available yet */
    PENDING,

    /* Service has finished its query and results are available */
    FINISHED,

    /* Service has finished but user was not authorized */
    RESULT_AUTHORIZED,

    /* Service has finished and user was authorized */
    RESULT_NOT_AUTHORIZED,

    /* Timeout has reached with no result */
    ABORT_TIMEOUT,

    /* User triggered abort */
    ABORT_USER_CANCELLED,

    /* Abort due to an unknown issue */
    ABORT_UNKNOWN;

}
