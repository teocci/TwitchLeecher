package com.github.teocci.av.twitch.models.twitch.kraken;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-08
 */
public class TwitchUserInfo
{
    public static final String API_URL = "https://api.twitch.tv/kraken/users";

    @SerializedName("_id")
    private String id;

    private String name;

    @SerializedName("display_name")
    private String displayName;

    private String logo;

    private String type;

    private String bio;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("created_at")
    private Date createdAt;


    @Override
    public String toString()
    {
        return "TwitchUserInfo{" +
                "_id='" + id + '\'' +
                "display_name='" + displayName + '\'' +
                '}';
    }

    public static TwitchUserInfo getTwitchUserInfo(String channelName)
    {
        return TwitchUserInfoList.getFirst(channelName);
    }

    public static String getTwitchChannelVideosURL(String channelName)
    {
        TwitchUserInfo userInfo = getTwitchUserInfo(channelName);
        if (userInfo == null) return null;
        return String.format("https://api.twitch.tv/kraken/channels/%s/videos", userInfo.getId());
    }

    public static String getTwitchChannelStreamTwitchStreamVideosURL(String channelName)
    {
        TwitchUserInfo userInfo = getTwitchUserInfo(channelName);
        if (userInfo == null) return null;
        return String.format("https://api.twitch.tv/kraken/streams/%s", userInfo.getId());
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getLogo()
    {
        return logo;
    }

    public void setLogo(String logo)
    {
        this.logo = logo;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getBio()
    {
        return bio;
    }

    public void setBio(String bio)
    {
        this.bio = bio;
    }

    public Date getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }
}
