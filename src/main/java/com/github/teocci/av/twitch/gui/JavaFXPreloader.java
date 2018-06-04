package com.github.teocci.av.twitch.gui;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Jun-01
 */
public class JavaFXPreloader extends Preloader
{
    private ProgressBar bar;
    private Stage stage;

    private Scene createPreloaderScene() throws IOException
    {
        bar = new ProgressBar();
        ProgressIndicator pi = new ProgressIndicator();
        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(bar, pi);
        return new Scene(hb, 300, 150);
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        this.stage = stage;
        stage.setScene(createPreloaderScene());
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification scn)
    {
        if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn)
    {
        bar.setProgress(pn.getProgress());
    }
}