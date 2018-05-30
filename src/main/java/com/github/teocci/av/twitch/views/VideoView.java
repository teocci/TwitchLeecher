package com.github.teocci.av.twitch.views;

import com.github.teocci.av.twitch.controllers.VideoViewController;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideo;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-29
 */
public class VideoView
{
    private Pane pane;

    private VideoViewController controller;

    public VideoView(final TwitchVideo video, VideoViewController controller) throws IOException
    {
        this.pane = new VideoPane(video, controller);
        this.controller = controller;
    }

    public void setPane(Pane pane)
    {
        this.pane = pane;
    }

    public Pane getPane()
    {
        return pane;
    }

    public VideoViewController getController()
    {
        return controller;
    }

    public void setController(VideoViewController controller)
    {
        this.controller = controller;
    }
}
