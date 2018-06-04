package com.github.teocci.av.twitch.views;

import com.github.teocci.av.twitch.controllers.MainController;
import com.github.teocci.av.twitch.enums.BroadcastType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;

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

    private MenuBar menuBar = new MenuBar();
    private ToolBar toolBar = new ToolBar();
    private ToggleGroup group = new ToggleGroup();


    private Label errorLbl = new Label();
    private VBox loader = new VBox();
    private StackPane stackPane = new StackPane();

    private ToolBar tempBar = new ToolBar();
    private ToolBar footBar = new ToolBar();

    private ProgressBar progress = new ProgressBar();
    private Label status = new Label();

    public MainView(MainController controller)
    {
        this.controller = controller;

        initHeader();
        initCenter();
        initFooter();
    }

    private void initHeader()
    {
        initMenuBar();
        initToolBar();

        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menuBar, toolBar);

        root.setTop(topContainer);
    }

    private void initCenter()
    {
        stackPane.getChildren().add(controller.getSearchViewNode());

        errorLbl.setStyle("-fx-background-color:yellow");
        errorLbl.setPadding(new Insets(5, 5, 5, 5));
//        errorLbl.setVisible(false);
        errorLbl.managedProperty().bind(errorLbl.visibleProperty());
        stackPane.getChildren().add(errorLbl);

        Label label = new Label();
        ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);

        loader.setSpacing(5);
        loader.setAlignment(Pos.CENTER);
        loader.setMinSize(330, 120);
        loader.getChildren().addAll(label, progressIndicator);
//        loader.setVisible(false);
        loader.managedProperty().bind(errorLbl.visibleProperty());
        stackPane.getChildren().add(loader);


        root.setCenter(stackPane);
    }

    private void initFooter()
    {
        initTempBar();
        initFootBar();
        VBox botContainer = new VBox();
        botContainer.getChildren().addAll(tempBar, footBar);

        root.setBottom(botContainer);
    }

    private void initMenuBar()
    {
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
    }

    private void initToolBar()
    {
        Label channelLabel = new Label("Channel: ");
        TextField channelInput = new TextField();
        channelInput.setOnAction(e -> searchChannel(channelInput));

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

        // Set Hgrow for TextField
        HBox.setHgrow(channelInput, Priority.ALWAYS);
        toolBar.getItems().addAll(channelLabel, channelInput, all, archive, highlight, upload, tbRegion, search);
    }

    private void initTempBar()
    {
        Region pbRegion = new Region();
        HBox.setHgrow(pbRegion, Priority.ALWAYS);
//        tempBar.setVisible(false);
        tempBar.managedProperty().bind(tempBar.visibleProperty());
        tempBar.getItems().addAll(status, pbRegion, progress);
    }

    private void initFootBar()
    {
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
    }

    public void initSearch()
    {
        showLoader(true);
        hideError();
    }


    private void searchChannel(TextField channelInput)
    {
        if (channelInput.getText() != null && !channelInput.getText().isEmpty() && broadcastType != null) {
            controller.searchChannel(channelInput.getText(), broadcastType);
        }
    }

    public void showLoader(boolean visible)
    {
        loader.setVisible(visible);
    }

    public void showError(String error)
    {
        errorLbl.setVisible(true);
        errorLbl.setText(error);
    }

    public void hideError()
    {
        errorLbl.setVisible(false);
        errorLbl.setText("");
    }

    public void setErrorText(String error)
    {
        errorLbl.setText(error);
    }

    public Label getStatus()
    {
        return status;
    }

    public ProgressBar getProgressBar()
    {
        return progress;
    }

    public void showTempBar(boolean visible)
    {
        tempBar.setVisible(visible);
    }

    public Parent asParent()
    {
        return root;
    }

    public void endSearch(IOException e)
    {
        showLoader(false);
        if (e == null) {
            hideError();
        } else {
            showError(e.getMessage());
        }
    }
}
