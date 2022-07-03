/*
 * FireTVServiceError
 * Connect SDK
 *
 * Copyright (c) 2015 LG Electronics.
 * Created by Oleksii Frolov on 08 Jul 2015
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

package com.connectsdk.service.command;

/**
 * This class implements an exception for FireTVService
 */
public class FireTVServiceError extends ServiceCommandError {

    public FireTVServiceError(String message) {
        super(message);
    }

    public FireTVServiceError(String message, Throwable e) {
        super(message);
        this.payload = e;
    }
}
