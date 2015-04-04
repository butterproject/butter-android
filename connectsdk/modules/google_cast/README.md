#Google Cast module for Connect SDK (Android)
The Google Cast module extends Connect SDK to add Google Cast SDK support. This repository is included as a submodule in the main project, and has some manual setup before the main project will compile.

##General Information
For more information about Connect SDK, visit the [main repository](https://github.com/ConnectSDK/Connect-SDK-Android).

##Setup
###Connect SDK Integration
1. Go to the [Google Cast Developer site](https://developers.google.com/cast/docs/downloads) and download the Android Sender API
2. Set it up following the instructions 

###Connect SDK Lite Integration
1. Clone this repository into a subfolder of the Connect SDK Lite project
2. Import the source files into the Connect SDK Lite Eclipse project
3. Follow the steps above for Connect SDK integration
4. In Connect SDK Lite's `DefaultPlatforms.java` file add line `devicesList.put("com.connectsdk.androidgooglecast.CastService", "com.connectsdk.androidgooglecast.CastDiscoveryProvider");` inside `getDeviceServiceMap()` method

##License
Copyright (c) 2013-2014 LG Electronics.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
