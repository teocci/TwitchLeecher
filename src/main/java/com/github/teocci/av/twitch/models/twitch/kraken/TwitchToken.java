package com.github.teocci.av.twitch.models.twitch.kraken;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-08
 */
public class TwitchToken
{
    @SerializedName("chansub")
    private Map<String, List<String>> chansub;

    @SerializedName("expires")
    private String expires;

    @SerializedName("https_required")
    private String httpsRequired;

    @SerializedName("privileged")
    private boolean privileged;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("version")
    private String version;

    @SerializedName("vod_id")
    private String vodId;

    @Override
    public String toString()
    {
        return "TwitchToken{" +
                "vod_id='" + vodId + '\'' +
                '}';
    }

    public Map<String, List<String>> getChansub()
    {
        return chansub;
    }

    public void setChansub(Map<String, List<String>> chansub)
    {
        this.chansub = chansub;
    }

    public String getExpires()
    {
        return expires;
    }

    public void setExpires(String expires)
    {
        this.expires = expires;
    }

    public String getHttpsRequired()
    {
        return httpsRequired;
    }

    public void setHttpsRequired(String httpsRequired)
    {
        this.httpsRequired = httpsRequired;
    }

    public boolean isPrivileged()
    {
        return privileged;
    }

    public void setPrivileged(boolean privileged)
    {
        this.privileged = privileged;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getVodId()
    {
        return vodId;
    }

    public void setVodId(String vodId)
    {
        this.vodId = vodId;
    }
}
