package com.github.teocci.av.twitch.exceptions;

import com.github.teocci.av.twitch.model.twitch.TwitchVideoPart;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchDownloadException extends Exception
{
    private TwitchVideoPart videoPart;

    public TwitchDownloadException() { }

    public TwitchDownloadException(String message)
    {
        super(message);
    }

    public TwitchDownloadException(TwitchVideoPart videoPart)
    {
        this.videoPart = videoPart;
    }

    public TwitchDownloadException(String message, TwitchVideoPart videoPart)
    {
        super(message);
        this.videoPart = videoPart;
    }

    public TwitchVideoPart getVideoPart()
    {
        return videoPart;
    }

    public void setVideoPart(TwitchVideoPart videoPart)
    {
        this.videoPart = videoPart;
    }
}
