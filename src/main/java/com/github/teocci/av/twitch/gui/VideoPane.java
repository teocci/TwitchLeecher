package com.github.teocci.av.twitch.gui;

import com.github.teocci.av.twitch.controllers.VideoViewController;
import com.github.teocci.av.twitch.enums.State;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideo;
import com.github.teocci.av.twitch.utils.LogHelper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static com.github.teocci.av.twitch.enums.State.INITIAL;
import static com.github.teocci.av.twitch.enums.State.SELECTED_FOR_DOWNLOAD;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.layout.BorderStrokeStyle.SOLID;

/**
 * Represents one TwitchVideoInfo Object in the Search Result Area in th GUI.
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class VideoPane extends Pane implements /*ItemListener,*/ PropertyChangeListener
{
    private static final String TAG = LogHelper.makeLogTag(VideoPane.class);

    // Borders
    private static int BORDER_SIZE = 5;
    private static int SPACING_SIZE = 2;

    private final boolean debugColors = true;

    private final TwitchVideo video;
    private final VideoViewController controller;

    private final VBox content = new VBox();

    private final StackPane previewPane = new StackPane();

    private final ImageView thumbnail = new ImageView();

    private final HBox darkBar = new HBox(SPACING_SIZE);

    private final Label durationLbl = new Label();
    private final Label viewCountLbl = new Label();

    private final Label titleLbl = new Label();

    private final HBox infoBox = new HBox(SPACING_SIZE);
    private final Label channelName = new Label();
    private final Label recordedDate = new Label();

    private final Hyperlink twitchLink = new Hyperlink("Watch on Twitch");

    private final HBox bottomBox = new HBox(SPACING_SIZE);
    private final CheckBox queued = new CheckBox("Add to queue");

//    private final btnBox = new Panel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
    private final HBox btnBox = new HBox(SPACING_SIZE);
    private final Button downloadBtn = new Button("Download");
    private final Button playBtn = new Button("Play");
    private final Button convertBtn = new Button("Convert");
    private final Button deleteBtn = new Button("Delete");
    private final Button preferencesBtn = new Button("Preferences");

    private static final Color selectedColor = Color.rgb(165, 89, 243, 1);
    private static final Color unselectedColor = Color.rgb(0, 0, 0, 0.5);
    private static final Color downloadedColor = Color.GREEN;
    private static final Color downloadingColor = Color.YELLOW;
    private static final Color convertingColor = Color.ORANGE;
    private static final Color downloadQueuedColor = Color.CYAN;
    private static final Color convertQueuedColor = Color.BLUE;


    private static final Border selectedBorder = new Border(new BorderStroke(selectedColor, SOLID, null, new BorderWidths(BORDER_SIZE)));
    private static final Border unselectedBorder = new Border(new BorderStroke(unselectedColor, SOLID, null, new BorderWidths(BORDER_SIZE)));
    private static final Border downloadedBorder = new Border(new BorderStroke(downloadedColor, SOLID, null, new BorderWidths(BORDER_SIZE)));
    private static final Border downloadingBorder = new Border(new BorderStroke(downloadingColor, SOLID, null, new BorderWidths(BORDER_SIZE)));
    private static final Border convertingBorder = new Border(new BorderStroke(convertingColor, SOLID, null, new BorderWidths(BORDER_SIZE)));


    private static final Border downloadQueuedBorder = new Border(new BorderStroke(
            downloadQueuedColor,
            SOLID,
            null,
            new BorderWidths(BORDER_SIZE)
    ));
    private static final Border convertQueuedBorder = new Border(new BorderStroke(
            convertQueuedColor,
            SOLID,
            null,
            new BorderWidths(BORDER_SIZE)
    ));

    private EventHandler<ActionEvent> downloadHandler;
    private EventHandler<ActionEvent> liveHandler;

    public VideoPane(final TwitchVideo video, final VideoViewController controller) throws IOException
    {
        this.video = video;
        this.video.addPropertyChangeListenern(this);

        this.controller = controller;

        loadPreviewImage();

        initGUIElements();
        initEventHandlers();
        initStyles();
        layoutComponents();
    }

    private void initGUIElements()
    {
//        thumbnail.setTooltip(new Tooltip(video.getTitle()));
        Tooltip.install(thumbnail, new Tooltip(video.getTitle()));

        viewCountLbl.setText(String.format("%d views", video.getViews()));

        int duration = video.getLength();
        int seconds = duration % 60;
        int minutes = (duration / 60) % 60;
        int hours = duration / (60 * 60);

        durationLbl.setText(String.format("%5d:%02d:%02d", hours, minutes, seconds));

        String title = video.getTitle();
        int cutLength = 40;
        if (title.length() > cutLength) {
            title = title.substring(0, cutLength);
            title = title.concat("...");
        }

        titleLbl.setText(title);
        titleLbl.setTooltip(new Tooltip(video.getTitle()));

        channelName.setText(video.getChannelDisplaylName());

        recordedDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(video.getRecordedAt().getTime()));
    }

    private void initEventHandlers()
    {
        downloadHandler = e -> controller.downloadTwitchVideo(video);
        liveHandler = e -> {
            try {
                controller.openUrlInBrowser(new URL("http://twitch.tv/" + video.getChannelName()));
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
        };

        queued.setOnAction((e) -> setViewAsSelected(queued));

        thumbnail.setSmooth(true);
        thumbnail.setOnMousePressed(e -> {
            if (!video.relatedFileExists()) {
                if (isSelected()) {
                    queued.setSelected(false);
                    video.setState(INITIAL);
                } else {
                    queued.setSelected(true);
                    video.setState(SELECTED_FOR_DOWNLOAD);
                }
            }
        });
        thumbnail.setOnMouseEntered(e -> {
            if (video.getState().equals(INITIAL)) {
                if (isSelected()) setBorder(unselectedBorder);
                else setBorder(selectedBorder);
            }
        });
        thumbnail.setOnMouseExited(e -> {
            if (video.getState().equals(INITIAL)) {
                if (isSelected()) setBorder(selectedBorder);
                else setBorder(unselectedBorder);
            }
        });


        twitchLink.setOnAction((e) -> {
            LogHelper.e(TAG, "This link is clicked");
            try {
                controller.openUrlInBrowser(video.getUrl());
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
        });

        queued.managedProperty().bind(queued.visibleProperty());

//        downloadBtn.setActionCommand("download");
        downloadBtn.managedProperty().bind(downloadBtn.visibleProperty());
        downloadBtn.setOnAction(downloadHandler);

//        playBtn.setActionCommand("watchVideo");
        playBtn.managedProperty().bind(playBtn.visibleProperty());
        playBtn.setOnAction((e) -> {
            LogHelper.e(TAG, "Watch Btn pressed opening " + video.getMainRelatedFileOnDisk().getName());
            try {
                Desktop.getDesktop().open(video.getMainRelatedFileOnDisk());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        convertBtn.managedProperty().bind(convertBtn.visibleProperty());
        convertBtn.setOnAction((e) -> controller.convert2mp4(video));

        deleteBtn.managedProperty().bind(deleteBtn.visibleProperty());
        deleteBtn.setOnAction((e) -> {
            Alert alert = new Alert(CONFIRMATION);
            alert.setTitle("Delete?");
            String s = "Delete \"" + video.getTitle() + "\" ?";
            alert.setContentText(s);

            Optional<ButtonType> result = alert.showAndWait();

            if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                controller.delete(video);
            }
        });

//        preferencesBtn.setActionCommand("preferencesBtn");
        preferencesBtn.managedProperty().bind(preferencesBtn.visibleProperty());
        preferencesBtn.setOnAction((e) -> createPreferenceDialog());
    }

    private void initStyles()
    {
//        thumbnail.setBounds(0, 0, 320, 180);
//        thumbnail.setTranslateX(0);
//        thumbnail.setTranslateY(0);
//        thumbnail.setPrefSize(320, 180);
        thumbnail.setId("video-pane-thumbnail");
        thumbnail.getStyleClass().add("preview-full-size");

//        viewCountLbl.setFont(Font.font(original.getFamily(), FontWeight.BOLD, original.getSize()));
//        viewCountLbl.setBounds(5, 0, 310, 25);
//        viewCountLbl.setHorizontalAlignment(SwingConstants.LEFT);
//        viewCountLbl.setTextFill(Color.WHITE);
//        viewCountLbl.setStyle("-fx-background-color: " + Color.rgb(0, 0, 0, 0).toString() + ";");
//        Font original = viewCountLbl.getFont();
        viewCountLbl.setId("video-pane-views");
        viewCountLbl.getStyleClass().add("text-half-size");

//        durationLbl.setBounds(5, 0, 310, 25);
//        durationLbl.setHorizontalAlignment(SwingConstants.RIGHT);
//        durationLbl.setFont(Font.font(original.getFamily(), FontWeight.BOLD, original.getSize()));
//        durationLbl.setTextFill(Color.WHITE);
        durationLbl.setId("video-pane-duration");
        durationLbl.getStyleClass().add("text-half-size");

        //Adding a dark bar to the previewImage to increase the readability of the view, count and duration
//        darkBar.setBounds(0, 0, 320, 25);
//        darkBar.setStyle("-fx-background-color: " + new Color(0, 0, 0, 80).toString() + ";");
//        darkBar.setVerticalAlignment(JLabel.TOP);
//        darkBar.setHorizontalAlignment(JLabel.CENTER);
//        darkBar.setOpaque(true);
        darkBar.setId("video-pane-dark-bar");
        darkBar.getStyleClass().add("dark-bar-normal");

//        previewPane.setPrefSize(315, 180);
//        previewPane.setBackground(Color.yellow);
        previewPane.setId("video-pane-preview-pane");
        previewPane.getStyleClass().add("preview-full-size");

//        titleLbl.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 14));
        titleLbl.setId("video-pane-title");
        titleLbl.getStyleClass().add("text-full-width");

        twitchLink.setId("video-pane-link");
        twitchLink.getStyleClass().add("text-full-width");

        channelName.setId("video-pane-channel-name");
        channelName.getStyleClass().add("text-half-size");
        recordedDate.setId("video-pane-recorded-date");
        recordedDate.getStyleClass().add("text-half-size");
        infoBox.setId("video-pane-info-box");

        queued.setId("video-pane-queued");
        btnBox.setId("video-pane-btn-box");
        bottomBox.setId("video-pane-bottom-box");

        setId("video-pane");
    }


    private void setViewAsSelected(CheckBox checkBox)
    {
        if (checkBox.isSelected()) {
            if (video.getState().equals(INITIAL)) {
                video.setState(SELECTED_FOR_DOWNLOAD);
            }
        } else {
            if (video.getState().equals(SELECTED_FOR_DOWNLOAD)) {
                video.setState(INITIAL);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        if (e.getSource() == video) {
            if (e.getPropertyName().equals("title")) {
                titleLbl.setText(video.getTitle());
            } else if (e.getPropertyName().equals("state")) {
                State currentState = (State) e.getNewValue();
                updateGUIBasedOnState(currentState);
            }
        }
    }

    private void layoutComponents()
    {
//        previewPane.add(durationLbl, 5);
//        previewPane.add(viewCountLbl, 5);
//        previewPane.add(darkBar, 7);
//        previewPane.add(thumbnail, 10);
        Region dbRegion = new Region();
        HBox.setHgrow(dbRegion, Priority.ALWAYS);
        viewCountLbl.setAlignment(Pos.CENTER_LEFT);
        durationLbl.setAlignment(Pos.CENTER_RIGHT);
        darkBar.getChildren().addAll(viewCountLbl, dbRegion, durationLbl);

        StackPane.setAlignment(thumbnail, Pos.TOP_CENTER);
        StackPane.setAlignment(darkBar, Pos.TOP_CENTER);
        previewPane.getChildren().addAll(thumbnail, darkBar);


//        GridBagConstraints c = new GridBagConstraints();
//        c.insets = new Insets(0, 0, 0, 0);
//        c.ipadx = 3;
//        c.ipady = 3;
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = 2;
//        c.anchor = GridBagConstraints.LINE_START;
//        add(previewPane, c);

//        c.insets = new Insets(2, 2, 2, 2);
//        c.gridy++;
//        c.gridwidth = 2;
//        add(titleLbl, c);
//        titleLbl.setMinimumSize(new Dimension(300, 20));
//        titleLbl.setMaximumSize(new Dimension(300, 100));
//        titleLbl.setPreferredSize(new Dimension(300, 20));

//        c.gridwidth = 1;
//        c.gridy++;
//        add(channelName, c);
//
//        c.gridx = 1;
//        c.anchor = GridBagConstraints.LINE_END;
//        add(recordedDate, c);
        infoBox.getChildren().addAll(channelName, recordedDate);

//        c.gridx = 0;
//        c.gridy++;
//        c.anchor = GridBagConstraints.LINE_START;
//        add(twitchLink, c);


//        c.gridy++;
//        add(queued, c);
//
//        c.gridx++;
//        c.anchor = GridBagConstraints.LINE_END;
//
//        btnBox.add(deleteBtn);
//        btnBox.add(convertBtn);
//        btnBox.add(preferencesBtn);
//        btnBox.add(downloadBtn);
//        btnBox.add(playBtn);

        btnBox.getChildren().addAll(deleteBtn, convertBtn, preferencesBtn, downloadBtn, playBtn);
        Region bRegion = new Region();
        HBox.setHgrow(bRegion, Priority.ALWAYS);
        bottomBox.getChildren().addAll(queued, bRegion, btnBox);
        content.getChildren().addAll(previewPane, titleLbl, infoBox, twitchLink, bottomBox);

        getChildren().add(content);

        setInitialLayout();
        updateGUIBasedOnState(video.getState());
    }

    private void setInitialLayout()
    {
        LogHelper.e(TAG, "setInitialLayout()");
        queued.setVisible(true);
        downloadBtn.setVisible(true);
        preferencesBtn.setVisible(false);
        playBtn.setVisible(false);
        deleteBtn.setVisible(false);
        convertBtn.setVisible(false);
        setBorder(unselectedBorder);
    }

    private void setDownloadingLayout()
    {
        setInitialLayout();
        setQueuedLayout();
        setBorder(downloadingBorder);
    }

    private void setDownloadedLayout()
    {
        downloadBtn.setVisible(false);
        preferencesBtn.setVisible(false);
        deleteBtn.setVisible(true);
        deleteBtn.setDisable(false);
        playBtn.setVisible(true);
        convertBtn.setVisible(true);
        convertBtn.setDisable(false);
        queued.setDisable(true);
        queued.setVisible(false);
        setBorder(downloadedBorder);
    }

    private void setConvertedLayout()
    {
        setDownloadedLayout();
        convertBtn.setVisible(false);
        convertBtn.setDisable(true);
    }

    private void setConvertingLayout()
    {
        setDownloadedLayout();
        convertBtn.setDisable(true);
        setBorder(convertingBorder);
    }

    private void setQueuedLayout()
    {
        convertBtn.setDisable(true);
        deleteBtn.setDisable(true);
        downloadBtn.setDisable(true);
        queued.setDisable(true);
        preferencesBtn.setDisable(true);
    }

    private void createPreferenceDialog()
    {
        VideoInfoPreferencesDialog preferencesDialog = new VideoInfoPreferencesDialog();
        preferencesDialog.setVisible(true);
    }


    private boolean isSelected()
    {
        return video.getState().equals(SELECTED_FOR_DOWNLOAD);
    }

    private void updateGUIBasedOnState(State currentState)
    {
        switch (currentState) {
            case SELECTED_FOR_DOWNLOAD:
                setBorder(selectedBorder);
                queued.setSelected(true);
                break;
            case QUEUED_FOR_DOWNLOAD: // Video is in a Queue
                downloadBtn.setDisable(true);
                convertBtn.setDisable(true);
                queued.setDisable(true);
                setBorder(downloadQueuedBorder);
                break;
            case DOWNLOADING:
                setDownloadingLayout();
                break;
            case DOWNLOADED:
                setDownloadedLayout();
                break;
            case QUEUED_FOR_CONVERT:
                setConvertingLayout();
                setBorder(convertQueuedBorder);

                break;
            case CONVERTING:
                setConvertingLayout();
                break;
            case CONVERTED:
                setConvertedLayout();
                break;
            case INITIAL:
                setInitialLayout();
                break;
            case LIVE:
                setInitialLayout();
                int viewers = 0;
                try {
                    viewers = video.getChannel().getStream().getViewers();
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                downloadBtn.setActionCommand("watchLive");
                downloadBtn.setText("Watch Live");
                downloadBtn.setOnAction(liveHandler);
                durationLbl.setText("LIVE");
                viewCountLbl.setText(String.format("%d viewers", viewers));

                darkBar.getStyleClass().clear();
                darkBar.getStyleClass().addAll("video-pane-dark-bar", "dark-bar-live");
//                darkBar.setStyle("-fx-background-color: " + new Color(255, 0, 0, 80).toString() + ";");
                queued.setVisible(false);
                break;
            default:
                break;
        }
    }

    public void loadPreviewImage() throws IOException
    {
        if (video == null) return;
        Image image = new Image(video.getPreviewUrl("medium").toString());
        thumbnail.setImage(image);
    }

    public void updatePreviewImage() throws IOException
    {
        thumbnail.setImage(video.getPreviewImage());
    }
}
