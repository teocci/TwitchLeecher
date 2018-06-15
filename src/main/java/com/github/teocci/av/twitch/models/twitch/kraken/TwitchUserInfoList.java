package com.github.teocci.av.twitch.models.twitch.kraken;

import com.github.teocci.av.twitch.utils.LogHelper;
import com.github.teocci.av.twitch.utils.Network;
import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-08
 */
public class TwitchUserInfoList
{
    private static final String TAG = LogHelper.makeLogTag(TwitchUserInfoList.class);

    public static final String API_URL = "https://api.twitch.tv/kraken/users";

    @SerializedName("_total")
    private int total;

    private List<TwitchUserInfo> users;


    @Override
    public String toString()
    {
        return "TwitchUserInfoList{" +
                "_total='" + total + '\'' +
                '}';
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public List<TwitchUserInfo> getUsers()
    {
        return users;
    }

    public void setUsers(List<TwitchUserInfo> users)
    {
        this.users = users;
    }


    public static TwitchUserInfoList getTwitchUserList(String channelName)
    {
        String data = Network.getJSON(String.format(API_URL + "?login=%s", channelName));
        JsonObject p = new Gson().fromJson(data, JsonObject.class);
        LogHelper.e(TAG, p);

        if (p == null) return null;
        if (p.get("_total").getAsInt() < 1) return null;

        if (p.has("users") && !(p.get("users") instanceof JsonNull)) {
//            LogHelper.e(TAG, "Has users: " + p.getAsJsonArray().get(0));
            TwitchUserInfoList users = new Gson().fromJson(p, TwitchUserInfoList.class);
//            LogHelper.e(TAG, users);
            return users;
        }

        return null;
    }


    public static TwitchUserInfo getFirst(String channelName)
    {
        TwitchUserInfoList users = getTwitchUserList(channelName);

        if (users == null || users.isEmpty()) return null;
        return users.getUsers().get(0);
    }

    public boolean isEmpty()
    {
        return users == null || users.size() < 1;
    }
}
