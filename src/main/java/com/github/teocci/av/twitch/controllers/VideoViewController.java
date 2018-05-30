package com.github.teocci.av.twitch.controllers;

import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideo;
import com.github.teocci.av.twitch.views.VideoView;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;

import static com.github.teocci.av.twitch.enums.State.QUEUED_FOR_DOWNLOAD;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-29
 */
public class VideoViewController
{
    private VideoView view;

    private TwitchVideo video;

    public VideoViewController(TwitchVideo video)
    {
        this.video = video;
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
        video.setState(QUEUED_FOR_DOWNLOAD);
    }

    public void openUrlInBrowser(URL url)
    {

    }

    public void convert2mp4(TwitchVideo video)
    {

    }

    public void delete(TwitchVideo video)
    {

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
