package com.github.teocci.av.twitch.enums;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-04
 */
public enum State
{
    INITIAL,
    SELECTED_FOR_DOWNLOAD,
    QUEUED_FOR_DOWNLOAD,
    DOWNLOADING,
    DOWNLOADED,
    SELECTED_FOR_CONVERT,
    QUEUED_FOR_CONVERT,
    CONVERTING,
    CONVERTED,
    LIVE // this broadcast is currently live
}
