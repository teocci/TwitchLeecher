package com.github.teocci.av.twitch.model.twitch;

import com.github.teocci.av.twitch.utils.Network;
import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-08
 */
public class TwitchUserInfoList
{
    public static final String API_URL = "https://api.twitch.tv/kraken/users";

    @SerializedName("_total")
    private int total;

    private List<TwitchUserInfo> users;


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
        System.out.println(p);

        if (p == null) return null;
        if (p.get("_total").getAsInt() < 1) return null;

        if (p.has("users") && !(p.get("users") instanceof JsonNull)) {
//            System.out.println("Has users: " + p.getAsJsonArray().get(0));
            TwitchUserInfoList users = new Gson().fromJson(p, TwitchUserInfoList.class);
            System.out.println(users);
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
