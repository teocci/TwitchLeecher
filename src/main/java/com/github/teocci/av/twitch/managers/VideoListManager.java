package com.github.teocci.av.twitch.managers;

import com.github.teocci.av.twitch.controllers.ChannelSearchController;
import com.github.teocci.av.twitch.controllers.VideoViewController;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchUserInfo;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideo;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideoList;
import com.github.teocci.av.twitch.utils.LogHelper;
import com.github.teocci.av.twitch.utils.Network;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-24
 */
public class VideoListManager
{
    private static final String TAG = LogHelper.makeLogTag(VideoListManager.class);

    private Map<String, VideoViewController> videos = new ConcurrentHashMap<>();
    private ObservableList<Pane> videoViews = FXCollections.observableArrayList(extractVideoViews());

    private TwitchVideoList twitchVideoList = new TwitchVideoList();

    private String keyword;
    private String broadcastType;
    private int limit, offset;

    private String channelURL;
    private URL queryURL;

    private final Object lock = new Object();

    private ChannelSearchController searchController;

    public VideoListManager(ChannelSearchController searchController)
    {
        this.searchController = searchController;
    }


    public void search(String channelName)
    {
        try {
            search(channelName, "", -1, -1);
        } catch (IOException e) {
            searchController.onError(e);
        }
    }

    /**
     * Works like <code>update(String channelName, String broadcastType, int limit, int offset)</code>
     * <p>
     * The default values from <a href="https://dev.twitch.tv/docs/v5/reference/channels/#get-channel-videos">Optional Query String Parameters</a>
     * are used for offset and limit
     *
     * @param channelName   Channels's name
     * @param broadcastType Constrains the type of videos returned. Valid values: (any combination of) archive, highlight, upload. Default: all types (no filter).
     * @throws MalformedURLException
     */
    public void search(String channelName, String broadcastType)
    {
        try {
            search(channelName, broadcastType, -1, -1);
        } catch (IOException e) {
            searchController.onError(e);
        }
    }


    /**
     * Updates this list of videos ordered by time of creation, starting with the most recent from :channel.
     * <p>
     * It uses the following <a href="https://dev.twitch.tv/docs/v5/reference/channels/#get-channel-videos">Twitch API</a>
     *
     * @param keyword       Channels's name
     * @param broadcastType Constrains the type of videos returned. Valid values: (any combination of) archive, highlight, upload. Default: all types (no filter).
     * @param limit         Maximum number of objects in array. Default is 10. Maximum is 100. If limit<=0 the Twitch API default is used
     * @param offset        Object offset for pagination. Default is 0.
     * @throws MalformedURLException
     */
    public void search(String keyword, String broadcastType, int limit, int offset) throws IOException
    {
        LogHelper.e(TAG, "keyword: " + keyword + " | broadcastType: " + broadcastType);
        this.keyword = keyword;
        this.broadcastType = broadcastType;
        this.limit = limit;
        this.offset = offset;

        initDataContainers();

        generateChannelURL();
        generateQuery();

        update();
    }

    private void initDataContainers()
    {
        synchronized (lock) {
            videos.clear();
            videoViews.clear();
            twitchVideoList.clear();
        }
    }

    private void generateChannelURL() throws IOException
    {
        if (keyword == null || keyword.isEmpty()) throw new IOException("Channel name not entered");
        channelURL = TwitchUserInfo.getTwitchChannelVideosURL(keyword);
    }

    private String generateQuery() throws IOException
    {
        if (channelURL == null) throw new IOException("Channel not found");

        String urlStr = channelURL;
        List<String> parameters = new ArrayList<>();
        if (!broadcastType.isEmpty()) parameters.add(String.format("broadcast_type=%s", broadcastType));
        if (limit > 0) parameters.add(String.format("limit=%d", limit));
        if (offset >= 0) parameters.add(String.format("offset=%d", offset));
        if (!parameters.isEmpty()) {
            urlStr = urlStr.concat("?");
            String joinedParameters = Joiner.on('&').join(parameters);
            urlStr = urlStr.concat(joinedParameters);
        }
        queryURL = new URL(urlStr);
        return urlStr;
    }


    private void update() throws IOException
    {
        update(queryURL);
    }

    /**
     * Updates the <code>TwitchVideoInfoList</code> with the result of the given <code>URL</code>
     *
     * @param requestURL This URL has the API request constructed in the search function.
     * @throws IOException
     */
    private void update(URL requestURL) throws IOException
    {
        String data = Network.getJSON(requestURL);
        JsonObject p = new Gson().fromJson(data, JsonObject.class);
        System.out.println(p);

        if (p == null) return;
        if (p.has("videos") && !(p.get("videos") instanceof JsonNull)) {
            System.out.println("Has videos: " + p.has("videos"));

            List<TwitchVideo> videos = new Gson().fromJson(p.getAsJsonArray("videos"), new TypeToken<List<TwitchVideo>>() {}.getType());
            System.out.println(videos);

            synchronized (lock) {
                twitchVideoList = new Gson().fromJson(p, TwitchVideoList.class); //deserialize
                System.out.println(twitchVideoList);
                addTwitchVideoList();
            }
            searchController.onResult();
        }
    }

    public void loadMore()
    {
        offset = offset > 0 ? offset : 0;
        offset += (limit > 0 ? limit : 10);
        try {
            generateQuery();
            update();
        } catch (IOException e) {
            searchController.onError(e);
        }
    }

    private void addTwitchVideoList()
    {
        synchronized (lock) {
            for (TwitchVideo video : twitchVideoList.getVideos()) {
                VideoViewController controller = new VideoViewController(video);
                add(video.getId(), controller);
            }

            videoViews = FXCollections.observableArrayList(extractVideoViews());
        }
    }

    public boolean add(String videoId, VideoViewController controller)
    {
        if (controller == null) return false;
        if (contains(videoId)) return false;

        videos.put(videoId, controller);
        LogHelper.e(TAG, "Controller added: " + controller);
        return true;
    }

    private List<Pane> extractVideoViews()
    {
        List<Pane> viewList = new ArrayList<>();
        if (!isEmpty()) {
            for (VideoViewController controller : videos.values()) {
                viewList.add(controller.getView().getPane());
            }
        }

        return viewList;
    }

    public boolean hasMore()
    {
        LogHelper.e(TAG, "videos.size() = " + videos.size() +
                " twitchVideoList.getTotal() = " + twitchVideoList.getTotal() +
                " offset = " + offset
        );
        return videos != null && videos.size() < twitchVideoList.getTotal();
    }


    public ObservableList<Pane> getVideoViews()
    {
        return videoViews;
    }


    public boolean contains(String serviceName)
    {
        return !isEmpty() && videos.containsKey(serviceName);
    }

    public boolean isEmpty()
    {
        return videos != null && videos.isEmpty();
    }
}
