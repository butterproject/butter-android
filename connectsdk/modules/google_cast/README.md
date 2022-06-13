#Google Cast module for Connect SDK (Android)
The Google Cast module extends Connect SDK to add Google Cast SDK support. This repository is included as a submodule in the main project, and has some manual setup before the main project will compile.

##General Information
For more information about Connect SDK, visit the [main repository](https://github.com/ConnectSDK/Connect-SDK-Android).

##Setup
###Connect SDK Integration
It's already integrated in Connect-SDK-Android. Set it up following the instructions for Connect-SDK-Android.

###Connect SDK Lite Integration
1. Setup [Connect-SDK-Android-Lite](https://github.com/ConnectSDK/Connect-SDK-Android-Lite)
2. Clone this repository into a subfolder of the Connect SDK Lite project (e.g. `modules/google_cast`)
3. Add sources files for Google Cast module in your `build.gradle`, it should looks similar to this (here we have Google Cast module in `modules/google_cast` folder):
    ```groovy
        sourceSets {
            main {
                manifest.srcFile 'AndroidManifest.xml'
                java.srcDirs = [
                        'src',
                        'core/src',
                        'modules/google_cast/src',
                ]
                resources.srcDirs = ['src']
                aidl.srcDirs = ['src']
                renderscript.srcDirs = ['src']
                res.srcDirs = ['res']
                assets.srcDirs = ['assets']
            }
            test {
                java.srcDirs = [
                        'core/test/src',
                        'modules/google_cast/test/src',
                ]
            }
        }
    ```

4. In Connect SDK Lite's `DefaultPlatforms.java` file add this line
    ```groovy
    devicesList.put("com.connectsdk.service.CastService", "com.connectsdk.discovery.provider.CastDiscoveryProvider");
    ```
    inside `getDeviceServiceMap()` method.

##License
Copyright (c) 2013-2015 LG Electronics.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
