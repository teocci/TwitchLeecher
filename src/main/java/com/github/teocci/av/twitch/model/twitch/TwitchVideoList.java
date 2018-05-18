package com.github.teocci.av.twitch.model.twitch;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.teocci.av.twitch.enums.State.INITIAL;
import static com.github.teocci.av.twitch.enums.State.SELECTED_FOR_DOWNLOAD;

/**
 * This class represents a Search Result from the following api
 * https://api.twitch.tv/helix/videos
 * <p>
 * Valid Arguments are
 * <ul>
 * <li>id=ID of the video being queried (Limit: 100)</li>
 * <li>user_id=ID of the user who owns the video. (Limit 1)</li>
 * <li>game_id=ID of the game the video is of. Limit 1.</li>
 * </ul>
 * <p>
 * https://api.twitch.tv/helix/videos?id=134233
 * https://api.twitch.tv/helix/videos?user_id=1234123
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
public class TwitchVideoList
{    // API URL used by this class
    public static final String API_URL = "https://api.twitch.tv/helix/videos";

    private final PropertyChangeSupport pcs;

    @SerializedName("_links")
    private HashMap<String, String> links;


    @SerializedName("videos")
    List<TwitchVideo> videos;


    /**
     * Creates a empty TwitchVideoInfoList
     */
    public TwitchVideoList()
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
     * <li><code>setSize(int size)</code></li>
     * <li><code>setNextUrl(String url)</code></li>
     * <li><code>setSelfUrl(String url)</code></li>
     * <li><code>setTwitchVideo(ArrayList<TwitchVideoInfo></></code></li>
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
        return "TwitchVideoList{" +
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
     * @param videos
     */
    public void update(TwitchVideoList source, List<TwitchVideo> videos)
    {
        List<TwitchVideo> previousVideos = this.videos;
        this.setLinks(source.getLinks());
        this.setTwitchVideo(source.getVideos());
        if (videos != null) {
            for (TwitchVideo video : videos) {
                int index = this.videos.indexOf(video);
                if (index >= 0) { // replace it with the cached instance of the Object
                    this.videos.remove(index);
                    this.videos.add(index, video);
                    try {
                        video.loadThumbnail();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        this.pcs.firePropertyChange("contentUpdate", previousVideos, this.videos);
    }


    /**
     * Updates this list of videos ordered by time of creation, starting with the most recent from :channel.
     * <p>
     * It uese the Folowing API
     * <a href="https://github.com/justintv/Twitch-API/blob/master/v3_resources/videos.md#get-channelschannelvideos">api.twitch.tv/kraken/channels/:channel/videos</a>
     *
     * @param apiUrl    url for the query
     * @param videos    list of previous videos.
     * * @throws IOException as a MalformedURLException
     */
    public void update(URL apiUrl, List<TwitchVideo> videos) throws IOException
    {
        InputStream is = apiUrl.openStream();
        InputStreamReader ir = new InputStreamReader(is);
        TwitchVideoList source = new Gson().fromJson(ir, TwitchVideoList.class); //deserialize
        ir.close();
        is.close();
        update(source, videos);
    }

    /**
     * More information in the  <a href="https://dev.twitch.tv/docs/api/reference/#get-videos">Twitch</a>
     * reference for Required and Optional Query String Parameters
     *
     * @param parameters Channels's name
     * @throws MalformedURLException
     */
    public void update(ArrayList<String> parameters) throws IOException
    {
        String urlStr = API_URL;
        if (parameters.isEmpty()) {
            urlStr = urlStr.concat("?");
            String joinedParameters = Joiner.on('&').join(parameters);
            urlStr = urlStr.concat(joinedParameters);
        }
        URL apiUrl = new URL(String.format(urlStr));
        update(apiUrl, null);
    }

    public void update(String id) throws IOException
    {
        URL apiUrl = new URL(String.format(API_URL + "?id=%s", id));
        update(apiUrl, null);
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


    public List<TwitchVideo> getVideos()
    {
        return videos;
    }

    public int getSize()
    {
        return videos.size();
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

    private void setTwitchVideo(List<TwitchVideo> twitchVideos)
    {
        List<TwitchVideo> oldTwitchVideoInfos = this.videos;
        this.videos = twitchVideos;
        this.pcs.firePropertyChange("twitchVideos", oldTwitchVideoInfos, this.videos);
    }

    public HashMap<String, String> getLinks()
    {
        return links;
    }

    public void setLinks(HashMap<String, String> links)
    {
        this.links = links;
    }

    public void addTwitchVideoInfo(TwitchVideo tvi)
    {
        if (this.videos == null) this.videos = new ArrayList<>();
        this.videos.add(tvi);
        this.pcs.firePropertyChange("twitchVideoInfoAdded", null, tvi);
    }

    public void loadMore(List<TwitchVideoInfo> cachedTVIs)
    {
        TwitchVideoList tempVideoInfoList = new TwitchVideoList();
//        try {
////            tempVideoInfoList.update(getNextUrl(), cachedTVIs);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        setNextUrl(tempVideoInfoList.getNextUrlString());
        for (TwitchVideo videoInfo : tempVideoInfoList.getVideos()) {
            addTwitchVideoInfo(videoInfo);
        }
        this.pcs.firePropertyChange("moreLoaded", null, videos);

    }

    public TwitchVideoList getMostRecent(int ageInDays)
    {
        TwitchVideoList mostRecentList = new TwitchVideoList();

        int age = 0 - ageInDays;
        Calendar dateLimit = Calendar.getInstance();
        dateLimit.add(Calendar.DATE, age);

        for (TwitchVideo video : videos) {
            if (video.getCreatedAt().compareTo(dateLimit) > 0) {
                mostRecentList.addTwitchVideoInfo(video);
            }
        }
        return mostRecentList;
    }

    public void selectMostRecentForDownload(int ageInDays)
    {
        List<TwitchVideo> mostRecentList = this.getMostRecent(ageInDays).videos;
        for (TwitchVideo tvi : this.videos) {
            if (tvi.getState().equals(SELECTED_FOR_DOWNLOAD)) { //Reset old selection
                tvi.setState(INITIAL);
            }
        }
        for (TwitchVideo tvi : mostRecentList) { //Select most recent videos
            if (tvi.getState().equals(INITIAL)) {
                tvi.setState(SELECTED_FOR_DOWNLOAD);
            }
        }
    }

    /**
     * Takes AllSelected
     *
     * @return
     */
    public List<TwitchVideo> getAllSelected()
    {
        List<TwitchVideo> selectedVideos = videos.stream().filter(
                video -> video.getState().equals(SELECTED_FOR_DOWNLOAD)).collect(Collectors.toList()
        );
        return selectedVideos;
    }

    public TwitchVideo get(int index)
    {
        return videos.get(index);
    }
}


