package com.github.teocci.av.twitch.managers;

import com.github.teocci.av.twitch.TwitchLeecher;
import com.github.teocci.av.twitch.controllers.MainController;
import com.github.teocci.av.twitch.utils.OsUtils;
import com.github.teocci.av.twitch.utils.OsValidator;
import com.github.teocci.av.twitch.worker.FileDownloadWorker;
import com.github.teocci.av.twitch.worker.HttpFileDownloadWorker;
import javafx.scene.control.Alert;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static com.github.teocci.av.twitch.utils.Config.URL_FFMPEG_32_EXE;
import static com.github.teocci.av.twitch.utils.Config.URL_FFMPEG_64_EXE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Jun-04
 */
public class FFmpegManager
{
    private final ThreadPoolExecutor ffmpegExecutor = new ThreadPoolExecutor(1, 1, 5000L, MILLISECONDS, new LinkedBlockingQueue<>());
    private final ThreadPoolExecutor ffmpegDownloader = new ThreadPoolExecutor(10, 10, 5000L, MILLISECONDS, new LinkedBlockingQueue<>());

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    private File ffmpegExecutable;
    private String ffmpegCommand;

    private MainController mainController;

    public FFmpegManager(MainController mainController)
    {
        this.mainController = mainController;
        initFFmpegConfig();
    }

    private void initFFmpegConfig()
    {
        try {
            ffmpegCommand = "ffmpeg";
            if (OsValidator.isWindows()) {
                ffmpegExecutable = new File(OsUtils.getFFmpegPath(TwitchLeecher.class.getProtectionDomain()));
                if (!ffmpegExecutable.exists()) {
                    Alert alert = new Alert(CONFIRMATION, "FFmpeg not found! Do you want to download it? FFmpeg is required to download and convert the videos.", YES, NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == YES) {
                            downloadFFmpeg();
                        }
                    });
                }
                ffmpegCommand = ffmpegExecutable.getAbsolutePath();
            } else if (OsValidator.isUnix() || OsValidator.isMac()) {
                System.out.println("Running on a Unix System");
            } else {
                System.out.println("unknown OS assuming ffmpeg is installed and can be accessed via path-variable");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void downloadFFmpeg()
    {
        try {
            URL ffmpegExeUrl = new URL(OsUtils.is64Arch() ? URL_FFMPEG_64_EXE : URL_FFMPEG_32_EXE);
            FileDownloadWorker downloader = new FileDownloadWorker(ffmpegExeUrl, ffmpegExecutable);
            mainController.getProgressBar().progressProperty().unbind();
            mainController.getProgressBar().progressProperty().bind(downloader.progressProperty());
            mainController.getStatusLabel().textProperty().bind(downloader.messageProperty());
            executor.submit(downloader);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
