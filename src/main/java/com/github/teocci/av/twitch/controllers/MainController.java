package com.github.teocci.av.twitch.controllers;

import com.github.teocci.av.twitch.TwitchLeecherPreferences;
import com.github.teocci.av.twitch.enums.BroadcastType;
import com.github.teocci.av.twitch.managers.FFmpegManager;
import com.github.teocci.av.twitch.utils.LogHelper;
import com.github.teocci.av.twitch.utils.OsUtils;
import com.github.teocci.av.twitch.views.MainView;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;

import static com.github.teocci.av.twitch.TwitchLeecherPreferences.KEY_APP_BASE_PATH;
import static com.github.teocci.av.twitch.TwitchLeecherPreferences.KEY_DOWNLOAD_PATH;
import static com.github.teocci.av.twitch.utils.Config.APP_BASE_PATH;
import static com.github.teocci.av.twitch.utils.Config.DOWNLOAD_PATH;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-24
 */
public class MainController
{
    private static final String TAG = LogHelper.makeLogTag(MainController.class);

    private ChannelSearchController searchController;

    private MainView view;

    private String basePath = TwitchLeecherPreferences.getInstance().get(KEY_APP_BASE_PATH, APP_BASE_PATH);
    private String downloadPath = TwitchLeecherPreferences.getInstance().get(KEY_DOWNLOAD_PATH, DOWNLOAD_PATH);
    private Scene scene;

    private FFmpegManager ffmpegManager;

    public MainController()
    {
        searchController = new ChannelSearchController(this);
        view = new MainView(this);

        ffmpegManager = new FFmpegManager(this);
        initPreferences();
    }

    private void initPreferences()
    {
        if (!OsUtils.checkDir(basePath) || !OsUtils.checkDir(downloadPath)) {
            LogHelper.e(TAG, "Base or Download directories do not exist");
        }

        showLoader(false);
        showTempBar(false);

        hideError();
    }

    public void searchChannel(String keyword, BroadcastType broadcastType)
    {
        LogHelper.e(TAG, "keyword: " + keyword + " | broadcastType: " + broadcastType);
        searchController.searchChannel(keyword, broadcastType);
    }

    public MainView getView()
    {
        return view;
    }

    public Parent getViewAsParent()
    {
        return view.asParent();
    }

    public Node getSearchViewNode()
    {
        return searchController.getViewNode();
    }

    public void setScene(Scene scene)
    {
        this.scene = scene;
    }

    public Scene getScene()
    {
        return scene;
    }

    public void initSearch()
    {
        view.initSearch();
    }

    public void endSearch(IOException e)
    {
        view.endSearch(e);
    }

    public void showLoader(boolean visible)
    {
        view.showLoader(visible);
    }

    private void showTempBar(boolean visible)
    {
        view.showTempBar(visible);
    }

    public void showError(String error)
    {
        view.showError(error);
    }

    public void hideError()
    {
        view.hideError();
    }

    public ProgressBar getProgressBar()
    {
        return view.getProgressBar();
    }

    public Label getStatusLabel()
    {
        return view.getStatus();
    }
}
