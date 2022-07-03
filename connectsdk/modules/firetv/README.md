# Connect-SDK-Android-FireTV
The module extends Connect SDK to add FireTV support. This repository is included as a submodule in
the main project, and has some manual setup before the main project will compile. It provides the
following functionality:

    * Media playback
    * Media control
Using Connect SDK for discovery/control of Fire TV devices will result in your app complying with the [Amazon Fling SDK terms of service](https://developer.amazon.com/public/support/pml.html).

##General Information
For more information about Connect SDK, visit the [main repository](https://github.com/ConnectSDK/Connect-SDK-Android). This project can be built in Android Studio or directly with Gradle. Eclipse IDE is not supported.

##Setup
###Connect SDK Integration
1. Setup [Connect-SDK-Android](https://github.com/ConnectSDK/Connect-SDK-Android)
2. Download AmazonFling.jar and WhisperPlay.jar from [the Amazon website](https://developer.amazon.com/public/apis/experience/fling/docs/amazon-fling-sdk-download) and put them into ```Connect-SDK-Android/modules/firetv/libs/``` folder
3. Add these lines into dependency section of build.gradle file:

    ```groovy
    compile files('modules/firetv/libs/AmazonFling.jar')
    compile files('modules/firetv/libs/WhisperPlay.jar')
    ```
4. Synchronize your project with Gradle files

###Connect SDK Lite Integration
1. Setup [Connect-SDK-Android-Lite](https://github.com/ConnectSDK/Connect-SDK-Android-Lite)
2. Clone this repository into a subfolder of the Connect SDK Lite project (e.g. modules/firetv)
3. Go to the [Amazon Developer site](https://developer.amazon.com/) download the AmazonFling.jar and WhisperPlay.jar and put them into ```Connect-SDK-Android/modules/firetv/libs/``` folder
4. Add these lines into dependency section of build.gradle file:

    ```groovy
    compile files('modules/firetv/libs/AmazonFling.jar')
    compile files('modules/firetv/libs/WhisperPlay.jar')
    ```
5. Add sources files for FireTV in your build.gradle, it should looks similar to this (here we have FireTV module in modules/firetv folder):
    ```groovy
        sourceSets {
            main {
                manifest.srcFile 'AndroidManifest.xml'
                java.srcDirs = [
                        'src',
                        'core/src',
                        'modules/firetv/src',
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
                        'modules/firetv/test/src',
                ]
            }
        }
    ```

6. In Connect SDK Lite's `DefaultPlatforms.java` file add this line
    ```groovy
    devicesList.put("com.connectsdk.service.FireTVService", "com.connectsdk.discovery.provider.FireTVDiscoveryProvider");
    ```
    inside `getDeviceServiceMap()` method.


##License
Copyright (c) 2015 LG Electronics.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
