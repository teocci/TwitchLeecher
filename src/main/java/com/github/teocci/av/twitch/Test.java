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
public class Test extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Test");

        Pane pane = new Pane();
        Image image = new Image(IMAGE_ICON);
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitHeight(81);
        imageView.setFitWidth(108);
        imageView.setPreserveRatio(true);
        imageView.setPickOnBounds(true);
        Tooltip.install(imageView, new Tooltip("Testessssssssssssssssssssssssss"));
        pane.getChildren().addAll(imageView);
        Scene scene = new Scene(pane, 960, 600);
        scene.getStylesheets().add("css/style.css");

        primaryStage.getIcons().add(new Image(IMAGE_ICON));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
