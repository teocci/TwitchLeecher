package com.github.teocci.av.twitch.model.twitch;

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
 * <li>broadcasts=true      (or false)</li>
 * <li>limit=10             (0-100)</li>
 * <li>offset=10</li>
 * </ul>
 * <p>
 * https://api.twitch.tv/teocci/channels/taketv/videos?broadcasts=true&limit=20&offset=20
 * https://api.twitch.tv/teocci/channels/taketv/videos?broadcasts=true&limit=20
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
    private final PropertyChangeSupport pcs;

    @SerializedName("_total")
    private int size;

    @SerializedName("_links")
    private Map<String, String> links;

    @SerializedName("videos")
    List<TwitchVideoInfo> videoList;
//    TwitchVideoInfo[] videos;

    /**
     * Creates a empty TwitchVideoInfoList
     */
    public TwitchVideoInfoList()
    {
        pcs = new PropertyChangeSupport(this);
        videoList = new ArrayList<>();
    }


    /**
     * Adds a PropertyChangeListener to the TwitchVideoInfoList
     * <p>
     * The following methods are able to fire a <code>PropertyChangeEvent</code>
     * <p>
     * <p>
     * <ul>
     * <li><code>setSize(int size)</code></li>
     * <li><code>setNextUrl(String url)</code></li>
     * <li><code>setSelfUrl(String url)</code></li>
     * <li><code>setVideoList(ArrayList<TwitchVideoInfo></></code></li>
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
                "size=" + getSize() +
                ", links=" + links +
                //", videos=" + Arrays.toString(videos) +
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
        List<TwitchVideoInfo> oldVideos = this.videoList;
        this.setLinks(source.getLinks());
        this.setVideoList(source.getVideoList());

        if (cachedTVIs != null) {
            for (TwitchVideoInfo cachedVideo : cachedTVIs) {
                int index = this.videoList.indexOf(cachedVideo);
                if (index >= 0) { // Replace it with the cached instance of the Object
                    this.videoList.remove(index);
                    this.videoList.add(index, cachedVideo);
                    try {
                        cachedVideo.loadPreviewImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        pcs.firePropertyChange("contentUpdate", oldVideos, videoList);
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


    public URL getSelfUrl() throws MalformedURLException
    {
        return new URL(links.get("self"));
    }

    public URL getNextUrl() throws MalformedURLException
    {
        return new URL(links.get("next"));
    }

    public String getNextUrlString()
    {
        return this.links.get("next");
    }

    public String getSelfUrlString()
    {
        return this.links.get("self");
    }


    public List<TwitchVideoInfo> getVideoList()
    {
        return videoList;
    }

    public int getSize()
    {
        return videoList.size();
    }

    public void setNextUrl(String nextUrl)
    {
        if (this.links == null) this.links = new HashMap<>();
        String oldNext = links.get("next");
        links.put("next", nextUrl);
        this.pcs.firePropertyChange("nextUrl", oldNext, links.get("next"));
    }

    public void setSelfUrl(String selfUrl)
    {
        if (this.links == null) this.links = new HashMap<>();
        String oldSelf = links.get("self");
        this.links.put("self", selfUrl);
        this.pcs.firePropertyChange("selfUrl", oldSelf, this.links.get("self"));
    }

    private void setVideoList(List<TwitchVideoInfo> videoList)
    {
        List<TwitchVideoInfo> oldTwitchVideoInfos = this.videoList;
        this.videoList = videoList;
        this.pcs.firePropertyChange("videos", oldTwitchVideoInfos, this.videoList);
    }

    public Map<String, String> getLinks()
    {
        return links;
    }

    public void setLinks(Map<String, String> links)
    {
        this.links = links;
    }

    public void addTwitchVideoInfo(TwitchVideoInfo tvi)
    {
        if (this.videoList == null) this.videoList = new ArrayList<>();
        this.videoList.add(tvi);
        this.pcs.firePropertyChange("twitchVideoInfoAdded", null, tvi);
    }

    public void loadMore(List<TwitchVideoInfo> cachedTVIs)
    {
        TwitchVideoInfoList tempVideoInfoList = new TwitchVideoInfoList();
        try {
            tempVideoInfoList.update(getNextUrl(), cachedTVIs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setNextUrl(tempVideoInfoList.getNextUrlString());
        for (TwitchVideoInfo videoInfo : tempVideoInfoList.getVideoList()) {
            addTwitchVideoInfo(videoInfo);
        }
        this.pcs.firePropertyChange("moreLoaded", null, videoList);

    }

    public TwitchVideoInfoList getMostRecent(int ageInDays)
    {
        TwitchVideoInfoList mostRecentList = new TwitchVideoInfoList();

        for (TwitchVideoInfo tvi : videoList) {
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
        List<TwitchVideoInfo> mostRecentList = this.getMostRecent(ageInDays).videoList;
        for (TwitchVideoInfo tvi : this.videoList) {
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
        for (TwitchVideoInfo tvi : videoList) {
            if (tvi.getState().equals(SELECTED_FOR_DOWNLOAD)) {
                selectedVideos.add(tvi);
            }
        }
        return selectedVideos;
    }

    public TwitchVideoInfo get(int index)
    {
        return videoList.get(index);
    }

    public boolean isEmpty()
    {
        return videoList == null || videoList.size() < 1;
    }
}


