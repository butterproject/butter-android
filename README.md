[Butter for Android](https://github.com/butterproject/butter-android)
----

Allow any Android user to watch movies easily streaming from torrents, without any particular knowledge.

Visit the project's website at <http://butterproject.org>.

## Community

Keep track of Butter development and community activity.

* Follow Butter on [Twitter](https://twitter.com/butterproject), [Facebook](https://www.facebook.com/ButterProjectOrg/) and [Google+](https://plus.google.com/communities/111003619134556931561).
* Join in discussions on the [Butter Forum](https://www.reddit.com/r/ButterProject)

## Roadmap

### pre 1.0.0
 - [x] 0.4.0 - Migrating app to MVP arhitecture
 - [x] 0.5.0 - Refactor media providers
 - [ ] 0.6.0 - Update video player and resolve media streaming issues
 - [ ] 0.7.0 - Refactor updater to work on p2p protocol
 - [ ] 0.8.0 - Updating connect SDK
 - [ ] 0.9.0 - Cleaning up project (resources, proguard, ...)
 - [ ] 1.0.0 - Making app stable for release

### pos 1.0.0
 - Make media providers available through Content Providers.
 - Make desktop JS media providers available on Android.
 - Support multiple audio sources.
 - Make media available offline.
 - Adding support for Chromebooks.
 - Picture in picture support.

## Getting Involved

Want to report a bug, request a feature, contribute or translate Butter? Check out our in-depth guide to [Contributing to Butter](.github/CONTRIBUTING.md#contributing-to-butter).

## Build Instructions

[![pipeline status](https://gitlab.com/butterproject/butter-android/badges/develop/pipeline.svg)](https://gitlab.com/butterproject/butter-android/commits/develop)

The [gradle build system](https://developer.android.com/studio/build/index.html) will fetch all dependencies and generate
files you need to build the project. You first need to generate the
local.properties (replace YOUR_SDK_DIR by your actual android sdk dir)
file:

    $ echo "sdk.dir=YOUR_SDK_DIR" > local.properties

You can now sync, build and install the project:

    $ ./gradlew assembleDebug   # assemble the debug .apk
    $ ./gradlew assembleRelease # assemble the release .apk (signing keys are required)
    $ ./gradlew installDebug    # install the debug .apk if you have an
                                # emulator or an Android device connected

You can use [Android Studio](http://developer.android.com/sdk/installing/studio.html) by it as existing project.

## Directory structure ##

    `|-- base                            # base module (contains all the core functionallity)
    `|-- mobile                          # mobile module (smartphone/tablet application)
    `|-- tv                              # tv module (Android TV application)
    `|-- provider                        # includes provider implementations
          `-- media                      # media provider implementation and SDK
                |-- base                 # Contains models for working with providers
                |-- mock                 # Provider implementation with mock data for testing purposes
                |-- provide              # Media provider SDK
                `-- vodo                 # Vodo provider implementation
          `-- subs                       # subtitle provider implementation and SDK
                |-- mock                 # Subs provider implementation with mock data for testing purposes
                |-- provide              # Subs provider SDK
    `|-- connectsdk                      # connectsdk module responsible for streaming on external device such as Chromecas or Apple TV
          |-- core                       # connectsdk module core implementation
          `-- modules
                |-- google_cast          # connectsdk module google cast implementation
                |-- firetv               # connectsdk module fire tv implementation

## Versioning

For transparency and insight into our release cycle, and for striving to maintain backward compatibility, Butter will be maintained according to the [Semantic Versioning](http://semver.org/) guidelines as much as possible.

Releases will be numbered with the following format:

`<major>.<minor>.<patch>`

Constructed with the following guidelines:
* A new *major* release indicates a large change where backwards compatibility is broken.
* A new *minor* release indicates a normal change that maintains backwards compatibility.
* A new *patch* release indicates a bugfix or small change which does not affect compatibility.

## License

If you distribute a copy or make a fork of the project, you have to credit this project as source.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.

Note: some dependencies are external libraries, which might be covered by a different license compatible with the GPLv3. They are mentioned in NOTICE.md.

***

Copyright (c) 2015 Butter Project - Released under the [GPL v3 license](LICENSE.txt).
