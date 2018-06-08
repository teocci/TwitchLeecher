package com.github.teocci.av.twitch;

import com.github.teocci.av.twitch.controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static com.github.teocci.av.twitch.utils.Config.IMAGE_ICON;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchLeecher extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Twitch Leecher");

        MainController controller = new MainController();
        Scene scene = new Scene(controller.getViewAsParent(), 1030, 600);
        scene.getStylesheets().add("css/style.css");
        controller.setScene(scene);

//        primaryStage.setOnCloseRequest(e -> {
//            Platform.exit();
//            System.exit(0);
//        });

        primaryStage.getIcons().add(new Image(IMAGE_ICON));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
