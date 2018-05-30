package com.github.teocci.av.twitch.models.twitch.kraken;

import com.github.teocci.av.twitch.controllers.VideoViewController;
import com.google.gson.annotations.SerializedName;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
 * <li>broadcast_type=archive</li>
 * </ul>
 * <p>
 * https://api.twitch.tv/kraken/teocci/taketv/videos?broadcasts=true&limit=20&offset=20
 * https://api.twitch.tv/kraken/teocci/taketv/videos?broadcasts=true&limit=20
 * <p>
 * Gets the past broadcats 1-20.
 * <p>
 * You are able to update a TwitchVideo list with its update methods. S
 * So you don't have to worry about the TwitchAPI
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchVideoList
{
    @SerializedName("_total")
    private int total;

    @SerializedName("videos")
    private List<TwitchVideo> videos = new ArrayList<>();

    private final PropertyChangeSupport pcs;

    /**
     * Creates a empty TwitchVideoInfoList
     */
    public TwitchVideoList()
    {
        pcs = new PropertyChangeSupport(this);
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

    public void clear()
    {
        total = 0;
        videos.clear();
    }

    public List<TwitchVideo> getVideos()
    {
        return videos;
    }

    public int getTotal()
    {
        return total;
    }

    private void setVideos(List<TwitchVideo> videos)
    {
        List<TwitchVideo> oldTwitchVideoInfos = this.videos;
        this.videos = videos;
        this.pcs.firePropertyChange("videos", oldTwitchVideoInfos, this.videos);
    }

    public void addTwitchVideo(TwitchVideo tvi)
    {
        if (this.videos == null) this.videos = new ArrayList<>();
        this.videos.add(tvi);
        this.pcs.firePropertyChange("twitchVideoInfoAdded", null, tvi);
    }

    public void loadMore(List<TwitchVideoInfo> cachedTVIs)
    {
        TwitchVideoList tempVideoInfoList = new TwitchVideoList();
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

    public TwitchVideoList getMostRecent(int ageInDays)
    {
        TwitchVideoList mostRecentList = new TwitchVideoList();

        for (TwitchVideo tvi : videos) {
            int age = 0 - ageInDays;
            Calendar dateLimit = Calendar.getInstance();
            dateLimit.add(Calendar.DATE, age);
            if (tvi.getRecordedAt().compareTo(dateLimit) > 0) {
                mostRecentList.addTwitchVideo(tvi);
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
     * Takes AllSelecte
     *
     * @return
     */
    public List<TwitchVideo> getAllSelected()
    {
        List<TwitchVideo> selectedVideos = new ArrayList<>();
        for (TwitchVideo tvi : videos) {
            if (tvi.getState().equals(SELECTED_FOR_DOWNLOAD)) {
                selectedVideos.add(tvi);
            }
        }
        return selectedVideos;
    }

    public TwitchVideo get(int index)
    {
        return videos.get(index);
    }

    public boolean isEmpty()
    {
        return videos == null || videos.size() < 1;
    }
}


