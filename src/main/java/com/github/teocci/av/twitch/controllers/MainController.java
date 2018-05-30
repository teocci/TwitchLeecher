package com.github.teocci.av.twitch.controllers;

import com.github.teocci.av.twitch.TwitchLeecherPreferences;
import com.github.teocci.av.twitch.enums.BroadcastType;
import com.github.teocci.av.twitch.utils.LogHelper;
import com.github.teocci.av.twitch.utils.OsUtils;
import com.github.teocci.av.twitch.views.MainView;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.io.File;

import static com.github.teocci.av.twitch.TwitchLeecherPreferences.KEY_APP_BASE_PATH;
import static com.github.teocci.av.twitch.TwitchLeecherPreferences.KEY_DOWNLOAD_PATH;
import static com.github.teocci.av.twitch.utils.Config.APP_BASE_PATH;
import static com.github.teocci.av.twitch.utils.Config.DOWNLOAD_PATH;
import static com.github.teocci.av.twitch.utils.Config.PLAYLIST_DIR;

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

    public MainController()
    {
        searchController = new ChannelSearchController(this);
        view = new MainView(this);

        initPreferences();
    }

    private void initPreferences()
    {
        if (!OsUtils.checkDir(basePath) || !OsUtils.checkDir(downloadPath)) {
            LogHelper.e(TAG, "Base or Download directories do not exist");
        }
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
}
