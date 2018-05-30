package com.github.teocci.av.twitch.controllers;

import com.github.teocci.av.twitch.enums.BroadcastType;
import com.github.teocci.av.twitch.interfaces.ChannelSearchCallback;
import com.github.teocci.av.twitch.managers.VideoListManager;
import com.github.teocci.av.twitch.utils.LogHelper;
import com.github.teocci.av.twitch.views.ChannelSearchView;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.io.IOException;

import static com.github.teocci.av.twitch.enums.BroadcastType.ALL;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-24
 */
public class ChannelSearchController implements ChannelSearchCallback
{
    private static final String TAG = LogHelper.makeLogTag(ChannelSearchController.class);

    private MainController mainController;

    private ChannelSearchView view = new ChannelSearchView(this);

    private VideoListManager videoListManager = new VideoListManager(this);

    public ChannelSearchController(MainController mainController)
    {
        this.mainController = mainController;
    }

    @Override
    public void onResult()
    {
        view.updateView();
    }

    @Override
    public void onError(IOException e)
    {
        e.printStackTrace();
        view.clear();
    }

    public void searchChannel(String keyword, BroadcastType broadcastType)
    {
        LogHelper.e(TAG, "keyword: " + keyword + " | broadcastType: " + broadcastType);
        if (broadcastType.equals(ALL)) {
            videoListManager.search(keyword);
        } else {
            videoListManager.search(keyword, broadcastType.toString());
        }
    }

    public void loadMore()
    {
        videoListManager.loadMore();
    }

    public ChannelSearchView getView()
    {
        return view;
    }

    public Node getViewNode()
    {
        return view.asNode();
    }

    public ObservableList<Pane> getVideoViews()
    {
        return videoListManager.getVideoViews();
    }

    public boolean hasMore()
    {
        return videoListManager.hasMore();
    }

    public MainController getMainController()
    {
        return mainController;
    }
}
