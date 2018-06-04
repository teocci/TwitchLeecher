package com.github.teocci.av.twitch;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static com.github.teocci.av.twitch.utils.Config.IMAGE_ICON;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TestPreloader extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("TestPreloader");

        Pane pane = new Pane();
        ImageView imageView = new ImageView(new Image("https://i.imgur.com/VT1J8EW.jpg"));
        imageView.setPreserveRatio(true);
        imageView.setPickOnBounds(true);
        pane.getChildren().addAll(imageView);
        Scene scene = new Scene(pane, 960, 600);

        primaryStage.getIcons().add(new Image(IMAGE_ICON));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
