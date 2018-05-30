package com.github.teocci.av.twitch.views;

import com.github.teocci.av.twitch.controllers.MainController;
import com.github.teocci.av.twitch.enums.BroadcastType;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import static com.github.teocci.av.twitch.enums.BroadcastType.ALL;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-24
 */
public class MainView
{
    private BorderPane root = new BorderPane();

    private MainController controller;

    private BroadcastType broadcastType = ALL;

    public MainView(MainController controller)
    {
        this.controller = controller;

        initHeader();
        initCenter();
        initFooter();
    }

    private void initHeader()
    {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem settingMenuItem = new MenuItem("Settings");
        MenuItem logMenuItem = new MenuItem("Log");
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(actionEvent -> Platform.exit());

        fileMenu.getItems().addAll(settingMenuItem, logMenuItem, new SeparatorMenuItem(), exitMenuItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutMenuItem = new MenuItem("About");
        helpMenu.getItems().add(aboutMenuItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);

        VBox topContainer = new VBox();
        ToolBar toolBar = new ToolBar();

        Label channelLabel = new Label("Channel: ");
        TextField channelInput = new TextField();
        channelInput.setOnAction(e -> searchChannel(channelInput));


        ToggleGroup group = new ToggleGroup();

        RadioButton all = new RadioButton("All");
        RadioButton archive = new RadioButton("Archive");
        RadioButton highlight = new RadioButton("Highlight");
        RadioButton upload = new RadioButton("Upload");

        all.setToggleGroup(group);
        all.setUserData("all");

        archive.setToggleGroup(group);
        archive.setUserData("archive");

        highlight.setToggleGroup(group);
        highlight.setUserData("highlight");

        upload.setToggleGroup(group);
        upload.setUserData("upload");

        group.selectedToggleProperty().addListener((obs, oldText, newText) -> {
                if (group.getSelectedToggle() != null) {
                    System.out.println(group.getSelectedToggle().getUserData().toString());
                    broadcastType = BroadcastType.toType(group.getSelectedToggle().getUserData().toString());
                    // Do something here with the userData of newly selected radioButton
                }
        });

        all.setSelected(true);

        Button search = new Button("Search VOD's");
        search.setOnAction(e -> searchChannel(channelInput));


        Region tbRegion = new Region();
        HBox.setHgrow(tbRegion, Priority.ALWAYS);

        toolBar.getItems().addAll(channelLabel, channelInput, all, archive, highlight, upload, tbRegion, search);

        topContainer.getChildren().add(menuBar);
        topContainer.getChildren().add(toolBar);

        root.setTop(topContainer);
    }

    private void searchChannel(TextField channelInput)
    {
        if (channelInput.getText() != null && !channelInput.getText().isEmpty() && broadcastType != null) {
            controller.searchChannel(channelInput.getText(), broadcastType);
        }
    }

    private void initCenter()
    {
        root.setCenter(controller.getSearchViewNode());
    }

    private void initFooter()
    {
        ToolBar footBar = new ToolBar();

        Region fbRegion = new Region();
        HBox.setHgrow(fbRegion, Priority.ALWAYS);


        Button selectRecent = new Button("Select most recent");
        final Spinner<Integer> spinner = new Spinner<>();
        Label recentLabel = new Label(" days.");
        Button downloadAll = new Button("Download All");

        final int initialValue = 30;

        // Value factory.
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, initialValue);
        spinner.setValueFactory(valueFactory);
        spinner.setMaxWidth(80);
        spinner.setOnScroll(e -> {
            if (e.getDeltaY() > 0) {
                spinner.decrement();
            } else if (e.getDeltaY() < 0) {
                spinner.increment();
            }
        });
        footBar.getItems().addAll(selectRecent, spinner, recentLabel, fbRegion, downloadAll);
        root.setBottom(footBar);
    }

    public Parent asParent()
    {
        return root;
    }
}
