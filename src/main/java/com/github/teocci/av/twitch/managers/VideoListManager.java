package com.github.teocci.av.twitch.managers;

import com.github.teocci.av.twitch.controllers.ChannelSearchController;
import com.github.teocci.av.twitch.controllers.VideoViewController;
import com.github.teocci.av.twitch.gui.VideoPaneComparator;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchUserInfo;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideo;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideoList;
import com.github.teocci.av.twitch.utils.LogHelper;
import com.github.teocci.av.twitch.utils.Network;
import com.github.teocci.av.twitch.utils.Utils;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.teocci.av.twitch.enums.State.*;
import static com.github.teocci.av.twitch.utils.Config.PLAYLIST_PATH;

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
        parameters.add(String.format("sort=%s", "time"));
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
        LogHelper.e(TAG, p);

        if (p == null) return;
        if (p.has("videos") && !(p.get("videos") instanceof JsonNull)) {
//            LogHelper.e(TAG, "Has videos: " + p.has("videos"));

//            List<TwitchVideo> videos = new Gson().fromJson(p.getAsJsonArray("videos"), new TypeToken<List<TwitchVideo>>() {}.getType());
//            LogHelper.e(TAG, videos);
            twitchVideoList = new Gson().fromJson(p, TwitchVideoList.class); //deserialize
//            LogHelper.e(TAG, twitchVideoList);
            if (twitchVideoList.getTotal() < 1) throw new IOException("Channel does not have videos.");

            synchronized (lock) {
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
                VideoViewController controller = new VideoViewController(searchController, video);
                searchLocalFiles(video);
                add(video.getId(), controller);
            }

            videoViews = FXCollections.observableArrayList(extractVideoViews());

            FXCollections.sort(videoViews, new VideoPaneComparator());
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


    /**
     * Updates the State of a new TwitchVideoInfoObjects based on the Stored Videos
     *
     * @param video the Twitch Video Info Object that should be modified
     */
    private void searchLocalFiles(TwitchVideo video)
    {
        if (video.getState().equals(INITIAL)) {
            File playlist = new File(PLAYLIST_PATH + video.getId() + ".m3u");
            if (playlist.exists() && playlist.isFile() && playlist.canRead()) {
                LogHelper.e(TAG, "playlist exists.");
                video.setMainRelatedFileOnDisk(playlist);
                video.putRelatedFile("playlist", playlist);
                try {
                    InputStream is = new FileInputStream(playlist);
                    Scanner sc = new Scanner(is);
                    int i = 0;
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        LogHelper.e(TAG, "line: " + line);
                        File file = new File(line);
                        if (file.exists()) {
                            i++;
                            String key = String.format("playlist_item_%04d", i);
                            video.putRelatedFile(key, file);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                video.setState(DOWNLOADED);
            }

//            ffmpegFileListFile = new File(playlistFolderPath + video.getId() + ".ffmpeglist");
//            if (ffmpegFileListFile.exists()) {
//                video.putRelatedFile("ffmpegFileListFile", ffmpegFileListFile);
//            }

            File mp4Video = Utils.getVideoFile(video, true);
            if (mp4Video == null) return;

            if (mp4Video.exists() && mp4Video.isFile() && mp4Video.canRead()) {
                video.setMainRelatedFileOnDisk(mp4Video);
                video.putRelatedFile("mp4Video", mp4Video);
                video.setState(CONVERTED);
            }
        }
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
