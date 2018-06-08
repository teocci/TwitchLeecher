package com.github.teocci.av.twitch.controllers;

import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideo;
import com.github.teocci.av.twitch.utils.Utils;
import com.github.teocci.av.twitch.views.VideoView;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;

import static com.github.teocci.av.twitch.enums.State.INITIAL;
import static com.github.teocci.av.twitch.enums.State.QUEUED_FOR_DOWNLOAD;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-29
 */
public class VideoViewController
{
    private VideoView view;
    private ChannelSearchController controller;

    private TwitchVideo video;

    public VideoViewController(ChannelSearchController controller, TwitchVideo video)
    {
        this.video = video;
        this.controller = controller;
        try {
            view = new VideoView(video, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString()
    {
        return "VideoViewController{" +
                "title='" + video.getTitle() + '\'' +
                '}';
    }

    public void downloadTwitchVideo(TwitchVideo video)
    {
        controller.getMainController().downloadTwitchVideo(video);
    }

    public void openUrlInBrowser(URL url)
    {
//        try {
            Utils.open(url.toString());
//            Runtime.getRuntime().exec("gvfs-open " + url);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void convert2mp4(TwitchVideo video)
    {

    }

    public void delete(TwitchVideo video)
    {
        video.deleteAllRelatedFiles();
        video.setState(INITIAL);
    }

    public VideoView getView()
    {
        return view;
    }

    public Pane getPane()
    {
        return view.getPane();
    }

    public TwitchVideo getVideo()
    {
        return video;
    }
}
