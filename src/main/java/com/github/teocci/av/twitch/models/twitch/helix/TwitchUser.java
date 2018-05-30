package com.github.teocci.av.twitch.models.twitch.helix;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * https://dev.twitch.tv/docs/api/reference/#get-users
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchUser
{
    // API URL used by this class
    public static final String API_URL = "https://api.twitch.tv/helix/users";


    // FIELDS returned by the API based on a example request.

    // User’s broadcaster type: "partner", "affiliate", or "".
    @SerializedName("broadcaster_type")
    private String broadcasterType;

    // User’s channel description.
    @SerializedName("description")
    private String description;

    // User’s display name.
    @SerializedName("display_name")
    private String displayName;

    // User’s email address. Returned if the request includes the user:read:email scope.
    @SerializedName("email")
    private String email;

    // User’s ID.
    @SerializedName("id")
    private String id;

    // User’s login name.
    @SerializedName("login")
    private String login;

    // URL of the user’s offline image.
    @SerializedName("offline_image_url")
    private String offlineImageUrl;

    // URL of the user’s profile image.
    @SerializedName("profile_image_url")
    private String profileImageUrl;

    // User’s type: "staff", "admin", "global_mod", or "".
    @SerializedName("type")
    private String type;

    // Total number of views of the user’s channel.
    @SerializedName("view_count")
    private int viewCount;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TwitchUser user = (TwitchUser) o;

        if (!id.equals(user.id)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public String toString()
    {
        String output = super.toString();
        output.concat(id + '\n');
        output.concat(login + '\n');
        output.concat(displayName + '\n');
        output.concat(profileImageUrl + '\n');
        return output;
    }

    public static TwitchUser getTwitchChannel(String loginName) throws IOException
    {
        URL channelApiUrl = new URL(API_URL + "?login=" + loginName);
        InputStream is = channelApiUrl.openStream();
        InputStreamReader ir = new InputStreamReader(is);
        TwitchUser channel = new Gson().fromJson(ir, TwitchUser.class);
        ir.close();
        is.close();
        return channel;
    }

    public void update(String loginName) throws IOException
    {
        update(getTwitchChannel(loginName));
    }

    public void update(TwitchUser user)
    {
        this.id = user.id;
        this.login = user.login;
        this.displayName = user.displayName;
        this.profileImageUrl = user.profileImageUrl;
    }


    /**
     * Reloads the channel Informations. This can be used to get additional information of a channel
     * Some other APIs from Twitch don't deliver all fileds in a channel object (for example TwitchVideoInfo).
     * This Method reloads the channel using the
     * <a href="https://github.com/justintv/Twitch-API/blob/master/v3_resources/channels.md#get-channelschannel">channels API</a>
     *
     * @throws IOException
     */
    public void reload() throws IOException
    {
        update(getTwitchChannel(getLogin()));
    }


    // Getters and Setters
    public String getBroadcasterType()
    {
        return broadcasterType;
    }

    public void setBroadcasterType(String broadcasterType)
    {
        this.broadcasterType = broadcasterType;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getOfflineImageUrl()
    {
        return offlineImageUrl;
    }

    public void setOfflineImageUrl(String offlineImageUrl)
    {
        this.offlineImageUrl = offlineImageUrl;
    }

    public String getProfileImageUrl()
    {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl)
    {
        this.profileImageUrl = profileImageUrl;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public int getViewCount()
    {
        return viewCount;
    }

    public void setViewCount(int viewCount)
    {
        this.viewCount = viewCount;
    }
}
