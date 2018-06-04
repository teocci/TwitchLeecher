package com.github.teocci.av.twitch.utils;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-30
 */
public class Config
{
    public static final String LOG_PREFIX = "[TLeecher]";

    public static final String PROGRAM_VERSION = "TwitchLeecher 0.8";

    public static final String IMAGE_ICON = "/images/twitchTool.png";
    public static final String IMAGE_DELETE = "/images/delete.png.png";
    public static final String IMAGE_PLAY = "/images/play.gif";

    public static final String APP_DIR = "/TwitchLeecher/";
    public static final String DOWNLOAD_DIR = "/Downloads/";
    public static final String PLAYLIST_DIR = "/Playlists/";
    public static final String STREAM_DIR = "/TwitchStreams/";


    public static final String USER_HOME_PATH = System.getProperty("user.home");
    public static final String APP_BASE_PATH = USER_HOME_PATH + APP_DIR;
    public static final String DOWNLOAD_PATH = APP_BASE_PATH + DOWNLOAD_DIR;
    public static final String PLAYLIST_PATH = APP_BASE_PATH + PLAYLIST_DIR;
    public static final String STREAM_PATH = APP_BASE_PATH + STREAM_DIR;


    public static final String PREFERENCES_FILE = APP_BASE_PATH + "preferences.json";

    public static final String URL_FFMPEG_BASE = "https://raw.githubusercontent.com/teocci/FFmpegBuilds/master";
    public static final String URL_FFMPEG_32_EXE = URL_FFMPEG_BASE + "/win32-static/bin/ffmpeg.exe";
    public static final String URL_FFMPEG_64_EXE = URL_FFMPEG_BASE + "/win64-static/bin/ffmpeg.exe";
}
