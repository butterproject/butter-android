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

package com.google.sample.castcompanionlibrary.cast.exceptions;

public interface OnFailedListener {

    /**
     * An interface for reporting back errors in an asynchronous way.
     * 
     * @param resourceId The resource that has a textual description of the problem
     * @param statusCode An additional integer to further specify the error. Value -1 would be
     *            interpreted as no data available.
     */
    public void onFailed(int resourceId, int statusCode);
}
