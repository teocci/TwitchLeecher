package com.github.teocci.av.twitch.model.twitch;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchVodAccessToken
{
    @SerializedName("token")
    private String token;

    @SerializedName("sig")
    private String sig;

    public TwitchVodAccessToken() {}

    @Override
    public String toString()
    {
        return "TwitchVodAccessToken{" +
                "token='" + token + '\'' +
                '}';
    }

    public String getToken()
    {
        return token;
    }

    public String getSig()
    {
        return sig;
    }

    public String getVodId() {
        TwitchToken twitchToken = new Gson().fromJson(token, TwitchToken.class);
        return twitchToken.getVodId();
    }
}
