package com.github.teocci.av.twitch.managers;

import com.github.teocci.av.twitch.TwitchLeecher;
import com.github.teocci.av.twitch.controllers.MainController;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideo;
import com.github.teocci.av.twitch.utils.LogHelper;
import com.github.teocci.av.twitch.utils.Utils;
import com.github.teocci.av.twitch.utils.OsValidator;
import com.github.teocci.av.twitch.worker.FFmpegDownloadWorker;
import com.github.teocci.av.twitch.worker.FileDownloadWorker;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static com.github.teocci.av.twitch.enums.State.CONVERTED;
import static com.github.teocci.av.twitch.enums.State.DOWNLOADING;
import static com.github.teocci.av.twitch.enums.State.QUEUED_FOR_DOWNLOAD;
import static com.github.teocci.av.twitch.utils.Config.APP_BASE_PATH;
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
    private static final String TAG = LogHelper.makeLogTag(FFmpegManager.class);

    private final ThreadPoolExecutor ffmpegExecutor = new ThreadPoolExecutor(1, 1, 5000L, MILLISECONDS, new LinkedBlockingQueue<>());
    private final ThreadPoolExecutor ffmpegDownloader = new ThreadPoolExecutor(10, 10, 5000L, MILLISECONDS, new LinkedBlockingQueue<>());

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LinkedBlockingQueue<TwitchVideo> videoWorkerQueue = new LinkedBlockingQueue<>();

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
                ffmpegExecutable = new File(Utils.getFFmpegPath(TwitchLeecher.class.getProtectionDomain()));
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
                LogHelper.e(TAG, "Running on a Unix System");
            } else {
                LogHelper.e(TAG, "unknown OS assuming ffmpeg is installed and can be accessed via path-variable");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void downloadFFmpeg()
    {
        try {
            URL ffmpegExeUrl = new URL(Utils.is64Arch() ? URL_FFMPEG_64_EXE : URL_FFMPEG_32_EXE);
            FileDownloadWorker downloader = new FileDownloadWorker(ffmpegExeUrl, ffmpegExecutable);
            mainController.getProgressBar().progressProperty().unbind();
            mainController.getProgressBar().progressProperty().bind(downloader.progressProperty());
            mainController.getStatusLabel().textProperty().bind(downloader.messageProperty());
            executor.submit(downloader);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void downloadTwitchVideo(TwitchVideo video)
    {
        video.setState(QUEUED_FOR_DOWNLOAD);
        if (videoWorkerQueue.isEmpty() && (ffmpegDownloader.getActiveCount() == 0)) {
            // Queue is Empty and no running workers
            videoWorkerQueue.add(video);
            initFFmpegVideoDownloader();
        } else {
            // Only add it when the previous is done it will be init the next download
            videoWorkerQueue.add(video);
        }
    }

    /**
     * Prepares the Download and creates needed files in the destination folder
     */
    private void initFFmpegVideoDownloader()
    {
        if (isEmpty()) return;

        TwitchVideo video = videoWorkerQueue.poll(); // get the next Video
        try {
            video.getDownloadInfo();
            video.setState(DOWNLOADING);

            File destinationVideoFile = Utils.getVideoFile(video, true);

            List<String> ffmpegOptions = Utils.getFFmpegParameters(video.getUrl());

//            mainPanel.getDownloadProgressPanel().setTitle(video.getTitle());

            FFmpegDownloadWorker downloader = new FFmpegDownloadWorker(
                    destinationVideoFile,
                    video,
                    ffmpegCommand,
                    ffmpegOptions
            );

            downloader.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    LogHelper.e(TAG, "Result = " + newValue);
                    switch (newValue) {
                        case STARTED:
                            mainController.showTempBar(true);
                            break;
                        case DONE:
                            mainController.showTempBar(false);
                            video.setState(CONVERTED);
                            break;
                    }
                }
            });


            mainController.getProgressBar().progressProperty().unbind();
            mainController.getProgressBar().progressProperty().bind(downloader.progressProperty());
            mainController.getStatusLabel().textProperty().bind(downloader.messageProperty());
//            downloader.addPropertyChangeListener(this);
//            downloader.addPropertyChangeListener(mainPanel.getDownloadProgressPanel());
//            LinkedBlockingQueue queue = (LinkedBlockingQueue) ffmpegDownloader.getQueue();
//            mainPanel.getDownloadProgressPanel().setQueue(queue);
//            videoInfo.setState(QUEUED_FOR_CONVERT);
            ffmpegDownloader.execute(downloader);

//            mainPanel.getDownloadProgressPanel().setQueue(videoWorkerQueue);
//            mainPanel.getDownloadProgressPanel().setVisible(true);
        } catch (MalformedURLException e) {
//            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
        } catch (IOException e) {
//            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
        }


//        try {
//            // Select quality based on TwitchToolsPreferences
//            String quality = videoInfo.getDownloadInfo().getPreferredQuality(TwitchLeecherPreferences.getQualityOrder());
//            // get the Parts of a Video
//            videoParts = videoInfo.getDownloadInfo().getTwitchBroadcastParts(quality);
//        } catch (IOException e) {
//            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
//        }
//
//        mainPanel.getDownloadProgressPanel().setMaximum(videoParts.size() * 100);
//        mainPanel.getDownloadProgressPanel().setValue(0);
//        mainPanel.getDownloadProgressPanel().setTitle(videoInfo.getTitle());
//
//        //Add Part numbers for the WorkerThread
//        int i = 0;
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
//        String dateTimeStr = sdf.format(videoInfo.getRecordedAt().getTime());
//        String destinationDir = TwitchLeecherPreferences.getInstance().get(KEY_DOWNLOAD_PATH, OsUtils.getUserHome());
//        File destinationFilenameTemplate = new File(destinationDir + "/" +
//                OsUtils.getValidFilename(videoInfo.getChannelName()) + "/" +
//                OsUtils.getValidFilename(videoInfo.getTitle()) + "_" +
//                dateTimeStr
//        );
//
//        List<File> destinationFiles = new ArrayList<>();
//
//        for (TwitchVideoPart videoPart : videoParts) {
//            videoPart.getFileExtension();
//            videoPart.setPartNumber(i++);
//            String destinationFilePath = String.format(
//                    "%s_%04d%s",
//                    destinationFilenameTemplate.getAbsolutePath(),
//                    videoPart.getPartNumber(),
//                    videoPart.getFileExtension()
//            );
//
//            File destinationFile = new File(destinationFilePath);
////            File destinationFile = new File(destinationFilenameTemplate.getAbsolutePath() + "_" +
////                    String.valueOf(videoPart.getPartNumber() + videoPart.getFileExtension()));
//            destinationFiles.add(destinationFile);
//            TwitchDownloadWorker tdw = new TwitchDownloadWorker(destinationFile, videoPart);
//            tdw.addPropertyChangeListener(this);
//            tdw.addPropertyChangeListener(mainPanel.getDownloadProgressPanel());
//            downloadExecutor.execute(tdw);
//        }
//
//        createPlaylistsFolder();
//        playlist = new File(playlistFolderPath + videoInfo.getId() + ".m3u");
//        createM3uPlaylist(playlist, destinationFiles);
//        ffmpegFileListFile = new File(playlistFolderPath + videoInfo.getId() + ".ffmpeglist");
//        createFFmpegFileList(ffmpegFileListFile, destinationFiles);
    }

    private boolean isEmpty()
    {
        return videoWorkerQueue == null || videoWorkerQueue.isEmpty();
    }
}
