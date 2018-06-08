package com.github.teocci.av.twitch.controllers;

import com.github.teocci.av.twitch.enums.BroadcastType;
import com.github.teocci.av.twitch.interfaces.ChannelSearchCallback;
import com.github.teocci.av.twitch.managers.VideoListManager;
import com.github.teocci.av.twitch.utils.LogHelper;
import com.github.teocci.av.twitch.views.ChannelSearchView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

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
        LogHelper.e(TAG, "onResult(): start");
        Platform.runLater(() -> {
            view.updateView();
            getMainController().endSearch(null);
        });
        LogHelper.e(TAG, "onResult(): end");
    }

    @Override
    public void onError(IOException e)
    {
        LogHelper.e(TAG, "onError(): start");
        Platform.runLater(() -> {
            view.clear();
            getMainController().endSearch(e);
        });
        LogHelper.e(TAG, "onError(): end");
    }

    public void searchChannel(String keyword, BroadcastType broadcastType)
    {
        LogHelper.e(TAG, "keyword: " + keyword + " | broadcastType: " + broadcastType);
        LogHelper.e(TAG, "searchChannel(): start");

        Task searchWorker = searchWorker(keyword, broadcastType);
        new Thread(searchWorker).start();

        LogHelper.e(TAG, "searchChannel(): end");
    }

    public void loadMore()
    {
        LogHelper.e(TAG, "loadMore(): start");

        Task loadMoreWorker = loadMoreWorker();
        new Thread(loadMoreWorker).start();

        LogHelper.e(TAG, "loadMore(): end");
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

    public MainController getMainController()
    {
        return mainController;
    }

    public Window getWindow()
    {
        if (mainController == null) return null;
        return mainController.getScene().getWindow();
    }

    public Task searchWorker(String keyword, BroadcastType broadcastType)
    {
        return new Task()
        {
            @Override
            protected Object call() throws Exception
            {
                Platform.runLater(() -> {
                    view.clear();
                    getMainController().initSearch();
                });

                if (broadcastType.equals(ALL)) {
                    videoListManager.search(keyword);
                } else {
                    videoListManager.search(keyword, broadcastType.toString());
                }
                return true;
            }
        };
    }

    public Task loadMoreWorker()
    {
        return new Task()
        {
            @Override
            protected Object call() throws Exception
            {
//                Platform.runLater(() -> {
//                    view.clear();
//                    getMainController().initSearch();
//                });

                videoListManager.loadMore();
                return true;
            }
        };
    }

    public boolean hasMore()
    {
        return videoListManager.hasMore();
    }
}
