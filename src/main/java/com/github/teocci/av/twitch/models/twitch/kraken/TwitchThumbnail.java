package com.github.teocci.av.twitch.models.twitch.kraken;

import com.google.gson.annotations.SerializedName;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Jun-15
 */
public class TwitchThumbnail
{
    @SerializedName("type")
    private String type;

    @SerializedName("url")
    private String url;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}
