package com.github.teocci.av.twitch.views;

import com.github.teocci.av.twitch.controllers.ChannelSearchController;
import com.github.teocci.av.twitch.gui.WorkIndicatorDialog;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-24
 */
public class ChannelSearchView
{
    private ScrollPane root = new ScrollPane();
    private TilePane tile = new TilePane();

    private Button btn = new Button("Add More");
    private Pane addMore = new Pane();

    private ChannelSearchController controller;

    public ChannelSearchView(ChannelSearchController controller)
    {
        this.controller = controller;

        initUIElements();
        initEventHandlers();
    }

    private void initUIElements()
    {
        tile.setPadding(new Insets(5));
        tile.setId("content-tile");
        tile.getChildren().clear();
        tile.setHgap(5); // row gap
        tile.setVgap(5); // column gap

        addMore.setId("add-more-pane");
        addMore.getChildren().add(btn);

        root.setFitToHeight(true);
        root.setFitToWidth(true);

        root.setContent(tile);
    }

    private void initEventHandlers()
    {
        btn.setOnAction(e -> {
            btn.setDisable(true);
            controller.loadMore();
        });
    }

    public void updateView()
    {
        tile.getChildren().clear();
        tile.getChildren().addAll(controller.getVideoViews());
        if (controller.hasMore()) {
            btn.setDisable(false);
            tile.getChildren().add(addMore);
        }
    }

    public void clear()
    {
        tile.getChildren().clear();
    }

    public Node asNode()
    {
        return root;
    }

    public Parent asParent()
    {
        return root;
    }
}
