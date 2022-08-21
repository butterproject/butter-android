<h1 align="center">
  <br>
  <a href="https://popcorntime.app"><img src="https://avatars2.githubusercontent.com/u/7267937?s=200" alt="Popcorn Time" width="200"></a>
  <br>
  Popcorn Time
  <br>
  <br>
</h1>

<h4 align="center">A multi-platform, free software BitTorrent client that includes an integrated media player.</h4>

<p align="center">
  <a href="https://github.com/popcorn-official/popcorn-android/releases/latest"><img src="https://img.shields.io/github/v/release/popcorn-official/popcorn-android?color=brightgreen&label=latest%20release"></a>
  <a href="https://github.com/popcorn-official/popcorn-android/releases/latest"><img src="https://img.shields.io/github/release-date/popcorn-official/popcorn-android?label="></a>
  <a href="https://github.com/popcorn-official/popcorn-android/compare/master...development"><img src="https://img.shields.io/github/commits-since/popcorn-official/popcorn-android/latest?label=commits%20since"></a>
  <a href="https://github.com/popcorn-official/popcorn-android/commit/development"><img src="https://img.shields.io/github/last-commit/popcorn-official/popcorn-android?label=latest%20commit"></a>
  <a href="https://github.com/popcorn-official/popcorn-android/actions"><img src="https://img.shields.io/github/workflow/status/popcorn-official/popcorn-android/CI?label=latest%20build"></a>
  <a href="https://david-dm.org/popcorn-official/popcorn-android"><img src="https://img.shields.io/david/popcorn-official/popcorn-android?label=deps"></a><br>
  <a href="https://popcorntime.app"><img src="https://img.shields.io/website?down_color=red&down_message=offline&label=popcorntime.app&up_color=brightgreen&up_message=online&url=https%3A%2F%2Fpopcorntime.app"></a>
  <a href="https://www.reddit.com/r/PopCornTimeApp"><img src="https://img.shields.io/reddit/subreddit-subscribers/PopCornTimeApp?color=e84722&label=reddit&style=flat"></a>
  <a href="https://discuss.popcorntime.app"><img src="https://img.shields.io/website?down_color=red&down_message=offline&label=forum&up_color=brightgreen&up_message=online&url=https%3A%2F%2Fdiscuss.popcorntime.app"></a>
  <a href="https://github.com/popcorn-official/popcorn-android/wiki"><img src="https://img.shields.io/website?down_color=red&down_message=offline&label=wiki&up_color=brightgreen&up_message=online&url=https%3A%2F%2Fgithub.com%2Fpopcorn-official%2Fpopcorn-android%2Fwiki"></a>
  <a href="https://github.com/popcorn-official/popcorn-desktop/wiki/FAQ"><img src="https://img.shields.io/website?down_color=red&down_message=offline&label=faq&up_color=brightgreen&up_message=online&url=https%3A%2F%2Fgithub.com%2Fpopcorn-official%2Fpopcorn-desktop%2Fwiki%2FFAQ"></a>

<h4 align="center">Visit the project's website at <a href="https://popcorntime.app">popcorntime.app</a></h4>

***

## Build Instructions ##

The [gradle build system](http://tools.android.com/tech-docs/new-build-system/user-guide) will fetch all dependencies and generate
files you need to build the project. You first need to generate the
local.properties (replace YOUR_SDK_DIR by your actual android sdk dir)
file:

    $ echo "sdk.dir=YOUR_SDK_DIR" > local.properties

You can now sync, build and install the project:

    $ ./gradlew assembleDebug # assemble the debug .apk
    $ ./gradlew installDebug  # install the debug .apk if you have an
                              # emulator or an Android device connected

You can use [Android Studio](http://developer.android.com/sdk/installing/studio.html) by importing the project as a Gradle project.

## Directory structure ##

    `|-- base                            # base module (contains providers and streamer)
     |    |-- build.gradle               # base build script
     |    `-- src
     |          |-- main
     |                |-- assets         # base module assets
     |                |-- java           # base module java code
     |                `-- res            # base module resources
    `|-- mobile                          # mobile module (smartphone/tablet application)
     |    |-- build.gradle               # mobile build script
     |    `-- src
     |          |-- main
     |                |-- java           # mobile module java code
     |                `-- res            # mobile module resources
    `|-- tv                              # tv module (Android TV application)
     |    |-- build.gradle               # tv build script
     |    `-- src
     |          |-- main
     |                |-- java           # tv module java code
     |                `-- res            # tv module resources
    `|-- vlc                             # vlc module (VLC mediaplayer library)
     |    |-- build.gradle               # vlc module build script
     |    `-- src
     |          |-- main
     |                |-- jniLibs        # native LibVLC libraries
     |                |-- java           # LibVLC Java code
    `|-- connectsdk                      # connectsdk module
          |-- build.gradle               # connectsdk build script
          `-- src
          |     |-- java                 # connectsdk module java code
          `-- core
          |     |-- src                  # connectsdk module core java code
          `-- modules
                |-- google_cast
                      |-- src            # connectsdk module google cast java code
                |-- firetv
                      |-- src            # connectsdk module google cast java code


## Getting Involved
Want to report a bug, request a feature, contribute to or translate Popcorn Time?  
Check out our in-depth guide to [Contributing to Popcorn Time](CONTRIBUTING.md#contributing-to-popcorn-time). We need all the help we can get!  
You can also join our [community](README.md#community) to keep up-to-date and meet other developers.  


<a name="community"></a>
## Community
Keep track of Popcorn Time development and community activity.
  * Read and contribute to the official [Popcorn Time Wiki](https://github.com/popcorn-official/popcorn-android/wiki/).
  * Join in discussions on the [Popcorn Time Forum](https://discuss.popcorntime.app) and [r/PopCornTimeApp](https://www.reddit.com/r/PopcornTimeApp).


## Versioning
For transparency and insight into our release cycle, and for striving to maintain backward compatibility, Popcorn Time will be maintained according to the [Semantic Versioning](http://semver.org/) guidelines as much as possible.

Releases will be numbered with the following format:

`<major>.<minor>.<patch>-<build>`

Constructed with the following guidelines:

* A new *major* release indicates a large change where backward compatibility is broken.
* A new *minor* release indicates a normal change that maintains backward compatibility.
* A new *patch* release indicates a bugfix or small change which does not affect compatibility.
* A new *build* release indicates this is a pre-release of the version.


***

If you distribute a copy or make a fork of the project, you have to credit this project as the source.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.

***

Copyright © 2022 Popcorn Time Project - Released under the [GPL v3 license](LICENSE.txt).
