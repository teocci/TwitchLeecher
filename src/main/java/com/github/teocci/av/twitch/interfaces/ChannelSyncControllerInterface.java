package com.github.teocci.av.twitch.interfaces;

import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideoInfo;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public interface ChannelSyncControllerInterface extends ActionListener, PropertyChangeListener
{
    JPanel getMainPanel();

    void searchFldText(String text, boolean pastBroadcasts) throws IOException;

    void openUrlInBrowser(URL url);

    void loadMoreSearchResults();

    void downloadTwitchVideo(TwitchVideoInfo videoInfo);

    void selectMostRecent(Integer value);

    void downloadAllSelectedTwitchVideos();

    void convert2mp4(TwitchVideoInfo relatedTwitchVideoInfoObject);

    void delete(TwitchVideoInfo relatedTwitchVideoInfoObject);
}
