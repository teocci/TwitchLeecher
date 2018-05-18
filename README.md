## TwitchLeecher for Java

[![Twitter][1]][2]

TwitchLeecher for Java is a very simple and straight-forward JAVA app implementation that download VOD from Twitch Broadcasts using [Twitch API v5][4] to retrieve all twitch relation information, and  [FFmpeg Android Java][3] to download the `.ts` parts.

### Disclaimer

This repository contains a simple sample code intended to demonstrate the capabilities of [FFmpeg Android Java][3] and [Twitch API v5][4]. It is not intended to be used as-is in applications as a library dependency, and will not be maintained as such. Bug fix contributions are welcome, but issues and feature requests will not be addressed.

### Summary

TwitchLeecher for Java is a simple API client for the Twitch API V5. TwitchLeecher for Java uses FFMPEG for download tasks. But also can downloads thousands of `.ts` files (small video chunks) in parallel while using all of the available bandwidth of your internet connection. As soon as all video chunks are downloaded, FFMPEG is only used to merge those chunks together in order to create a single video file again.

### Features
* Create mp4 Videos using FFMPEG
* A m3u-playlist is created to make watching the past broadcast easier.
* Downloading Multiple Videos

### Pre-requisites
    
- Install FFMPEG for Linux and Mac users

## Credits

* [FFmpeg Android Java][3] is an Android java library for FFmpeg binary compiled with x264, libass, fontconfig, freetype, fribidi and LAME.
* [Twitch API v5][4]

## License

The code supplied here is covered under the MIT Open Source License.


  [1]: https://img.shields.io/badge/Twitter-@Teocci-blue.svg?style=flat
  [2]: http://twitter.com/teocci
  [3]: http://writingminds.github.io/ffmpeg-android-java/
  [4]: https://dev.twitch.tv/docs/v5/