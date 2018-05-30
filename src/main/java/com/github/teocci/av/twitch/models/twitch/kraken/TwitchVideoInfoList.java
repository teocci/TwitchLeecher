package com.github.teocci.av.twitch.models.twitch.kraken;

import com.github.teocci.av.twitch.utils.Network;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.github.teocci.av.twitch.enums.State.INITIAL;
import static com.github.teocci.av.twitch.enums.State.SELECTED_FOR_DOWNLOAD;

/**
 * This class represents a Search Result from the folowing api
 * https://api.twitch.tv/kraken/channels/taketv/videos
 * <p>
 * Valid Arguments are
 * <ul>
 * <li>limit=10     (0-100)</li>
 * <li>offset=10</li>
 * <li>broadcast_type=true (or false)</li>
 * </ul>
 * <p>
 * https://api.twitch.tv/kraken/teocci/taketv/videos?broadcasts=true&limit=20&offset=20
 * https://api.twitch.tv/kraken/teocci/taketv/videos?broadcasts=true&limit=20
 * <p>
 * Gets the past broadcats 1-20.
 * <p>
 * You are abel to update a TwitchVideoInfo list with its  update methods. S
 * So you don't have to worry about the TwitchAPI
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchVideoInfoList
{
    @SerializedName("_total")
    private int total;

    @SerializedName("videos")
    private List<TwitchVideoInfo> videos;

    private final PropertyChangeSupport pcs;

    /**
     * Creates a empty TwitchVideoInfoList
     */
    public TwitchVideoInfoList()
    {
        pcs = new PropertyChangeSupport(this);
        videos = new ArrayList<>();
    }

    /**
     * Adds a PropertyChangeListener to the TwitchVideoInfoList
     * <p>
     * The following methods are able to fire a <code>PropertyChangeEvent</code>
     * <p>
     * <p>
     * <ul>
     * <li><code>setSize(int total)</code></li>
     * <li><code>setNextUrl(String url)</code></li>
     * <li><code>setSelfUrl(String url)</code></li>
     * <li><code>setVideos(ArrayList<TwitchVideoInfo></></code></li>
     * <li><code>loadMore()</code></li>
     * </ul>
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Remove the given PropertyChangeListener from the TwitchVideoInfoList
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.removePropertyChangeListener(listener);
    }


    @Override
    public String toString()
    {
        return "TwitchVideoInfoList{" +
                "total=" + getTotal() +
                ", videos=" + Arrays.toString(videos.toArray()) +
                '}';
    }


    /**
     * This TwitchVideoInfoList updates all its data from the source-TwitchVideoInfoList
     * It's like a Copy Constructor but the Object remains still the same. Listeners getting informed of the update.
     * <p>
     * This Method fires a PropertyChangeEvent with the PropertyName="contentUpdate" to inform the listeners
     *
     * @param source
     */
    public void update(TwitchVideoInfoList source, List<TwitchVideoInfo> cachedTVIs)
    {
        List<TwitchVideoInfo> oldVideos = this.videos;
        this.setVideos(source.getVideos());

        if (cachedTVIs != null) {
            for (TwitchVideoInfo cachedVideo : cachedTVIs) {
                int index = this.videos.indexOf(cachedVideo);
                if (index >= 0) { // Replace it with the cached instance of the Object
                    this.videos.remove(index);
                    this.videos.add(index, cachedVideo);
                    try {
                        cachedVideo.loadPreviewImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        pcs.firePropertyChange("contentUpdate", oldVideos, videos);
    }

    /**
     * Updates the <code>TwitchVideoInfoList</code> with the result of the given <code>URL</code>
     *
     * @param apiUrl
     * @throws IOException
     */
    private void update(URL apiUrl, List<TwitchVideoInfo> cachedTVIs) throws IOException
    {
        String data = Network.getJSON(apiUrl);
        JsonObject p = new Gson().fromJson(data, JsonObject.class);
        System.out.println(p);

        if (p == null) return;
        if (p.has("videos")  && !(p.get("videos") instanceof JsonNull)) {
            System.out.println("Has videos: " + p.has("videos"));

            List<TwitchVideoInfo> videos = new Gson().fromJson(p.getAsJsonArray("videos"), new TypeToken<List<TwitchVideoInfo>>() {}.getType());
            System.out.println(videos);

            TwitchVideoInfoList twitchVideoInfoList = new Gson().fromJson(p, TwitchVideoInfoList.class); //deserialize
            System.out.println(twitchVideoInfoList);

            update(twitchVideoInfoList, cachedTVIs);
        }
    }


    /**
     * Updates this list of videos ordered by time of creation, starting with the most recent from :channel.
     * <p>
     * It uese the Folowing API
     * <a href="https://github.com/justintv/Twitch-API/blob/master/v3_resources/videos.md#get-channelschannelvideos">api.twitch.tv/kraken/channels/:channel/videos</a>
     *
     * @param channelName Channels's name
     * @param broadcasts  Returns only broadcasts when true. Otherwise only highlights are returned. Default is false.
     * @param limit       Maximum number of objects in array. Default is 10. Maximum is 100. If limit<=0 the Twitch API default is used
     * @param offset      Object offset for pagination. Default is 0.
     * @param cachedTVIs
     * @throws MalformedURLException
     */
    public void update(String channelName, boolean broadcasts, int limit, int offset, List<TwitchVideoInfo> cachedTVIs) throws IOException
    {
        String urlStr = TwitchUserInfo.getTwitchChannelVideosURL(channelName);
        if (urlStr == null) throw new IOException("Channel not found");

        List<String> parameters = new ArrayList<>();
        if (broadcasts) parameters.add("broadcasts=true");
        if (limit > 0) parameters.add(String.format("limit=%d", limit));
        if (offset >= 0) parameters.add(String.format("offset=%d", offset));
        if (!parameters.isEmpty()) {
            urlStr = urlStr.concat("?");
            String joinedParameters = Joiner.on('&').join(parameters);
            urlStr = urlStr.concat(joinedParameters);
        }
        update(new URL(urlStr), cachedTVIs);
    }

    /**
     * Works like <code>update(String channelName, boolean broadcasts, int limit, int offset)</code>
     * <p>
     * The default values from <a href="https://github.com/justintv/Twitch-API/blob/master/v3_resources/videos.md#get-channelschannelvideos">Twitch</a>
     * are used for offset and limit
     *
     * @param channelName Channels's name
     * @param broadcasts  Returns only broadcasts when true. Otherwise only highlights are returned. Default is false.
     * @throws MalformedURLException
     */
    public void update(String channelName, boolean broadcasts) throws IOException
    {
        update(channelName, broadcasts, -1, -1, null);
    }

    public void update(String channelName) throws IOException
    {
        update(channelName, false, -1, -1, null);
    }



    public List<TwitchVideoInfo> getVideos()
    {
        return videos;
    }

    public int getTotal()
    {
        return total;
    }

    private void setVideos(List<TwitchVideoInfo> videos)
    {
        List<TwitchVideoInfo> oldTwitchVideoInfos = this.videos;
        this.videos = videos;
        this.pcs.firePropertyChange("videos", oldTwitchVideoInfos, this.videos);
    }

    public void addTwitchVideoInfo(TwitchVideoInfo tvi)
    {
        if (this.videos == null) this.videos = new ArrayList<>();
        this.videos.add(tvi);
        this.pcs.firePropertyChange("twitchVideoInfoAdded", null, tvi);
    }

    public void loadMore(List<TwitchVideoInfo> cachedTVIs)
    {
        TwitchVideoInfoList tempVideoInfoList = new TwitchVideoInfoList();
        //try {
        //    tempVideoInfoList.update(getNextUrl(), cachedTVIs);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        //setNextUrl(tempVideoInfoList.getNextUrlString());
        //for (TwitchVideoInfo videoInfo : tempVideoInfoList.getVideos()) {
        //    addTwitchVideo(videoInfo);
        //}
        this.pcs.firePropertyChange("moreLoaded", null, videos);
    }

    public TwitchVideoInfoList getMostRecent(int ageInDays)
    {
        TwitchVideoInfoList mostRecentList = new TwitchVideoInfoList();

        for (TwitchVideoInfo tvi : videos) {
            int age = 0 - ageInDays;
            Calendar dateLimit = Calendar.getInstance();
            dateLimit.add(Calendar.DATE, age);
            if (tvi.getRecordedAt().compareTo(dateLimit) > 0) {
                mostRecentList.addTwitchVideoInfo(tvi);
            }
        }
        return mostRecentList;
    }

    public void selectMostRecentForDownload(int ageInDays)
    {
        List<TwitchVideoInfo> mostRecentList = this.getMostRecent(ageInDays).videos;
        for (TwitchVideoInfo tvi : this.videos) {
            if (tvi.getState().equals(SELECTED_FOR_DOWNLOAD)) { //Reset old selection
                tvi.setState(INITIAL);
            }
        }
        for (TwitchVideoInfo tvi : mostRecentList) { //Select most recent videos
            if (tvi.getState().equals(INITIAL)) {
                tvi.setState(SELECTED_FOR_DOWNLOAD);
            }
        }
    }

    /**
     * Takes AllSelecte
     *
     * @return
     */
    public List<TwitchVideoInfo> getAllSelected()
    {
        List<TwitchVideoInfo> selectedVideos = new ArrayList<>();
        for (TwitchVideoInfo tvi : videos) {
            if (tvi.getState().equals(SELECTED_FOR_DOWNLOAD)) {
                selectedVideos.add(tvi);
            }
        }
        return selectedVideos;
    }

    public TwitchVideoInfo get(int index)
    {
        return videos.get(index);
    }

    public boolean isEmpty()
    {
        return videos == null || videos.size() < 1;
    }
}


