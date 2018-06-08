package com.github.teocci.av.twitch.controllers;

import com.github.teocci.av.twitch.TwitchVodLeecher;
import com.github.teocci.av.twitch.interfaces.ChannelSyncControllerInterface;
import com.github.teocci.av.twitch.gui.vod.channelsync.ChannelSyncLogFrame;
import com.github.teocci.av.twitch.gui.vod.channelsync.ChannelSyncMenuBar;
import com.github.teocci.av.twitch.gui.vod.channelsync.SyncChannelMainPanel;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideoInfo;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideoInfoList;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideoPart;
import com.github.teocci.av.twitch.utils.Utils;
import com.github.teocci.av.twitch.utils.OsValidator;
import com.github.teocci.av.twitch.TwitchLeecherPreferences;
import com.github.teocci.av.twitch.worker.FFmpegConverterWorker;
import com.github.teocci.av.twitch.worker.FFmpegDownloadWorkerOld;
import com.github.teocci.av.twitch.worker.HttpFileDownloadWorker;
import com.github.teocci.av.twitch.worker.TwitchDownloadWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static com.github.teocci.av.twitch.TwitchLeecherPreferences.KEY_APP_BASE_PATH;
import static com.github.teocci.av.twitch.enums.State.*;
import static com.github.teocci.av.twitch.enums.WorkerState.DONE;
import static com.github.teocci.av.twitch.enums.WorkerState.STARTED;
import static com.github.teocci.av.twitch.TwitchLeecherPreferences.KEY_DOWNLOAD_PATH;
import static com.github.teocci.av.twitch.utils.Config.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * This is the main controller for this application. That handls operations between the models and the views.
 * <p/>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class ChannelSyncController implements ChannelSyncControllerInterface {
    public static final String URL_FFMPEG_EXE = "http://trabauer.com/downloads/project_ressources/TwitchTools/ffmpeg.exe";

    public static final String URL_VERSION_INFO = "http://trabauer.com/downloads/TwitchVodLoaderInfo.txt";
    public static final String URL_PROGRAM_DOWNLOAD = "http://trabauer.com/downloads/TwitchVodLoader.jar";

    public static final String URL_PROJECT_PAGE = "http://lordh3lmchen.github.io/TwitchDownloader/";

    private final TwitchVideoInfoList videoInfoList = new TwitchVideoInfoList();

    private final JFrame mainFrame = new JFrame("Twitch Leecher");
    private final SyncChannelMainPanel mainPanel = new SyncChannelMainPanel(this, videoInfoList);
    private final JMenuBar mainMenuBar;

    private ChannelSyncLogFrame progressFrame = new ChannelSyncLogFrame();

    private final LinkedBlockingQueue<TwitchVideoInfo> videoWorkerQueue = new LinkedBlockingQueue<>();

    private TwitchVideoInfo videoInfo, convertingVideoInfo;

    private String baseDirPath = TwitchLeecherPreferences.getInstance().get(KEY_APP_BASE_PATH, Utils.getUserHome());
    private String destinationDirPath = TwitchLeecherPreferences.getInstance().get(KEY_DOWNLOAD_PATH, baseDirPath + APP_DIR);
    private String playlistFolderPath = destinationDirPath + PLAYLIST_DIR;

    private File playlist;
    private File ffmpegFileListFile;

    private final ThreadPoolExecutor ffmpegExecutor;
    private final ThreadPoolExecutor ffmpegDownloader;
    private final ThreadPoolExecutor downloadExecutor;

    private File ffmpegExecutable;
    private String ffmpegCommand;

    private List<TwitchVideoPart> videoParts;

    public ChannelSyncController() {
        mainFrame.getContentPane().add(this.getMainPanel());
        mainFrame.setSize(750, 550);
        mainFrame.setMinimumSize(new Dimension(550, 450));
        mainFrame.setVisible(true);
        mainFrame.setIconImage(new ImageIcon(IMAGE_ICON).getImage());

        mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainMenuBar = new ChannelSyncMenuBar(this, mainFrame);

        initAppConfig();

        ffmpegExecutor = new ThreadPoolExecutor(1, 1, 5000L, MILLISECONDS, new LinkedBlockingQueue<>());
        ffmpegDownloader = new ThreadPoolExecutor(10, 10, 5000L, MILLISECONDS, new LinkedBlockingQueue<>());
        // (15, 15, 5000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())
        downloadExecutor = new ThreadPoolExecutor(10, 10, 5000L, MILLISECONDS, new LinkedBlockingQueue<>());

        initFFmpegConfig();
//        checkForUpdates();
    }

    @Override
    public JPanel getMainPanel() {
        return mainPanel;
    }

    @Override
    public void searchFldText(String text, boolean pastBroadcasts) throws IOException {
        List<TwitchVideoInfo> cachedTVIs = new ArrayList<>();

        // Getting all queued and processed VideoInfoObjects
        if (videoInfo != null) {
            cachedTVIs.add(videoInfo);
        }
        if (convertingVideoInfo != null) {
            cachedTVIs.add(convertingVideoInfo);
        }

        for (TwitchVideoInfo queued : videoWorkerQueue.toArray(new TwitchVideoInfo[0])) {
            cachedTVIs.add(queued);
        }

        for (Runnable runnable : ffmpegExecutor.getQueue()) {
            if (runnable instanceof FFmpegConverterWorker) {
                FFmpegConverterWorker ffmpegConverterWorker = (FFmpegConverterWorker) runnable;
                cachedTVIs.add(ffmpegConverterWorker.getVideoInfo());
            }
        }

        videoInfoList.update(text, pastBroadcasts, 40, 0, cachedTVIs); // Updates Videos Except new
        searchLocalFiles(videoInfoList);

        if (videoInfoList.isEmpty()) throw new IOException("No video found.");

        if (videoInfoList.get(0).getChannel().getStream() != null && videoInfoList.get(0).getChannel().getStream().isOnline() && pastBroadcasts) {
            videoInfoList.get(0).setState(LIVE);
        }
    }

    @Override
    public void openUrlInBrowser(URL url) {
        try {
            Desktop.getDesktop().browse(url.toURI());
        } catch (URISyntaxException | IOException e) {
            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
        }
    }

    @Override
    public void loadMoreSearchResults() {
        videoInfoList.loadMore(null);
        searchLocalFiles(videoInfoList);
    }

    @Override
    public void downloadTwitchVideo(TwitchVideoInfo videoInfo) {
        videoInfo.setState(QUEUED_FOR_DOWNLOAD);
        if (videoWorkerQueue.isEmpty() && (ffmpegDownloader.getActiveCount() == 0)) {
            // Queue is Empty and no running workers
            videoWorkerQueue.add(videoInfo);
//            initializeDownload();
            initFFmpegVideoDownloader();
        } else {
            // Only add it when the previous is done it will be init the next download
            videoWorkerQueue.add(videoInfo);
        }
    }

    @Override
    public void selectMostRecent(Integer age) {
        videoInfoList.selectMostRecentForDownload(age);
    }

    @Override
    public void downloadAllSelectedTwitchVideos() {
//        videoWorkerQueue.resetQueue(videoInfoList.getAllSelected());
        for (TwitchVideoInfo tvi : videoInfoList.getAllSelected()) {
            try {
                videoWorkerQueue.put(tvi);
                tvi.setState(QUEUED_FOR_DOWNLOAD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        progressFrame.setVisible(true); //Is displayed in the main window now
//        initializeDownload();
        initFFmpegVideoDownloader();
    }

    @Override
    public void convert2mp4(TwitchVideoInfo videoInfo) {
        ffmpegFileListFile = new File(playlistFolderPath + videoInfo.getId() + ".ffmpeglist");

        String dateTimeStr = Utils.getSimpleDateString(videoInfo.getRecordedAt().getTime());
        File destinationVideoFile = new File(destinationDirPath + "/" +
                Utils.getValidFilename(videoInfo.getChannelName()) + "/" +
                Utils.getValidFilename(videoInfo.getTitle()) + "_" +
                dateTimeStr + ".mp4"
        );

        List<String> ffmpegOptions = null;
        try {
            ffmpegOptions = Utils.getFFmpegParameters(videoInfo.getUrl());
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
        }

        FFmpegConverterWorker converter;
        converter = new FFmpegConverterWorker(destinationVideoFile, ffmpegFileListFile, ffmpegCommand, ffmpegOptions);
        converter.addPropertyChangeListener(this);
        converter.addPropertyChangeListener(mainPanel.getConvertProgressPanel());
        converter.setVideoLength(videoInfo.getLength());
        converter.setRelatedTwitchVideoInfo(videoInfo);
        LinkedBlockingQueue queue = (LinkedBlockingQueue) ffmpegExecutor.getQueue();
        mainPanel.getConvertProgressPanel().setQueue(queue);
        videoInfo.setState(QUEUED_FOR_CONVERT);
        ffmpegExecutor.execute(converter);
    }

    @Override
    public void delete(TwitchVideoInfo videoInfo) {
        videoInfo.deleteAllRelatedFiles();
        videoInfo.setState(INITIAL);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof TwitchDownloadWorker) {
            if (evt.getPropertyName().equals("state")) {
                if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                    if (downloadExecutor.getActiveCount() == 0) { // if (Download of video is done)
                        videoInfo.setMainRelatedFileOnDisk(playlist);
                        videoInfo.setState(DOWNLOADED);
                        concatVideoParts(videoInfo);
                        mainPanel.getDownloadProgressPanel().setVisible(false);
//                        initializeDownload(); //try init next Download
                        initFFmpegVideoDownloader();
                    }
                } else if (evt.getNewValue().equals(SwingWorker.StateValue.STARTED)) {
                    TwitchDownloadWorker source = (TwitchDownloadWorker) evt.getSource();
                    TwitchVideoPart videoPart = source.getVideoPart();
//                    System.out.println(String.format("downloading Nr. %6d %s", videoPart.getPartNumber(), videoPart.getUrl()));
                    progressFrame.addOutputText(String.format("Downloading Part %d/%d %s\n",
                            videoPart.getPartNumber() + 1,
                            videoParts.size(),
                            videoPart.getUrl()));
                }
            }
        } else if (evt.getSource() instanceof FFmpegConverterWorker) {
            FFmpegConverterWorker runningFFmpegWorker = (FFmpegConverterWorker) evt.getSource();
            if (evt.getPropertyName().equals("outputline")) {
                String output = evt.getNewValue().toString();
                progressFrame.addOutputText(output);
            } else if (evt.getPropertyName().equals("state")) {
                if (evt.getNewValue().equals(SwingWorker.StateValue.STARTED)) {
                    progressFrame.addOutputText("Starting to Convert Video");
                    convertingVideoInfo = runningFFmpegWorker.getVideoInfo();
                } else if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                    convertingVideoInfo = null;
                }
            }
        } else if (evt.getSource() instanceof FFmpegDownloadWorkerOld) {
            FFmpegDownloadWorkerOld runningFFmpegWorker = (FFmpegDownloadWorkerOld) evt.getSource();
            if (evt.getPropertyName().equals("outputline")) {
                String output = evt.getNewValue().toString();
                progressFrame.addOutputText(output);
            } else if (evt.getPropertyName().equals("state")) {
                if (evt.getNewValue().equals(STARTED)) {
                    progressFrame.addOutputText("Starting to Convert Video");
                    convertingVideoInfo = runningFFmpegWorker.getVideoInfo();
                } else if (evt.getNewValue().equals(DONE)) {
                    videoInfo.setState(CONVERTED);
                    mainPanel.getDownloadProgressPanel().setVisible(false);
                }
            }
        }
    }

    private void initAppConfig() {
        Utils.checkDir(destinationDirPath);
        playlistFolderPath = TwitchLeecherPreferences.getInstance().get(KEY_DOWNLOAD_PATH, Utils.getUserHome()) + PLAYLIST_DIR;
    }

    private void initFFmpegConfig() {
        try {
            if (OsValidator.isWindows()) {
                ffmpegExecutable = new File(Utils.getFFmpegPath(TwitchVodLeecher.class.getProtectionDomain()));
                if (!ffmpegExecutable.exists()) {
                    int choice = JOptionPane.showConfirmDialog(
                            mainFrame,
                            "FFMPEG not found! Do you want to download it? FFMPEG is required to convert videos",
                            "FFMPEG not found! Download it?",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice == 0) { //YES
                        downloadFFMPEG();
                    } //else if(choice == 0) { //NO
                    // Nothing right now
                    //}
                }
                ffmpegCommand = ffmpegExecutable.getAbsolutePath();
            } else if (OsValidator.isUnix() || OsValidator.isMac()) {
                System.out.println("Running on a Unix System");
                ffmpegCommand = "ffmpeg";
            } else {
                System.out.println("unknown OS assuming ffmpeg is installed and can be accessed via path-variable");
                ffmpegCommand = "ffmpeg";
            }
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
        }
    }

    private void checkForUpdates() {
        try {
            URL VersionInfoUrl = new URL(URL_VERSION_INFO);
            InputStream is = VersionInfoUrl.openStream();
            Scanner sc = new Scanner(is);
            String line = null;
            if (sc.hasNextLine()) line = sc.nextLine();
            if (line != null) {
                if (!line.equals(PROGRAM_VERSION)) {
                    int choice = JOptionPane.showConfirmDialog(
                            mainFrame,
                            "Update Available! Download latest Version?",
                            "Update Available!",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice == 0) { //YES
                        openUrlInBrowser(new URL(URL_PROJECT_PAGE));
                    } //else if(choice == 0) { //NO
                    // Nothing right now
                    //}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void searchLocalFiles(TwitchVideoInfoList videoList) {
        for (TwitchVideoInfo videoInfo : videoList.getVideos()) {
            // Search related file on disk
            searchLocalFiles(videoInfo);
        }
    }


    /**
     * Updates the State of a new TwitchVideoInfoObjects based on the Stored Videos
     *
     * @param videoInfo the Twitch Video Info Object that should be modified
     */
    private void searchLocalFiles(TwitchVideoInfo videoInfo) {
        if (videoInfo.getState().equals(INITIAL)) {
            File playlist = new File(playlistFolderPath + videoInfo.getId() + ".m3u");
            if (playlist.exists() && playlist.isFile() && playlist.canRead()) {
                videoInfo.setMainRelatedFileOnDisk(playlist);
                videoInfo.putRelatedFile("playlist", playlist);

                try {
                    InputStream is = new FileInputStream(playlist);
                    Scanner sc = new Scanner(is);
                    int i = 0;
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        File file = new File(line);
                        if (file.exists()) {
                            i++;
                            String key = String.format("playlist_item_%04d", i);
                            videoInfo.putRelatedFile(key, file);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                videoInfo.setState(DOWNLOADED);
            }

            ffmpegFileListFile = new File(playlistFolderPath + videoInfo.getId() + ".ffmpeglist");
            if (ffmpegFileListFile.exists()) {
                videoInfo.putRelatedFile("ffmpegFileListFile", ffmpegFileListFile);
            }

            File mp4Video = getVideoFile(videoInfo, true);

            if (mp4Video.exists() && mp4Video.isFile() && mp4Video.canRead()) {
                videoInfo.setMainRelatedFileOnDisk(mp4Video);
                videoInfo.putRelatedFile("mp4Video", mp4Video);
                videoInfo.setState(CONVERTED);
            }
        }
    }

    /**
     * Prepares the Download and creates needed files in the destination folder
     */
    private void initFFmpegVideoDownloader() {
        if (isQueueEmpty()) return;

        videoInfo = videoWorkerQueue.poll(); // get the next Video
        try {
            videoInfo.getDownloadInfo();
            videoInfo.setState(DOWNLOADING);

            File destinationVideoFile = getVideoFile(videoInfo, true);

            List<String> ffmpegOptions = Utils.getFFmpegParameters(videoInfo.getUrl());

//            mainPanel.getDownloadProgressPanel().setMaximum(videoInfo.getLength() * 100);
//            mainPanel.getDownloadProgressPanel().setValue(0);
            mainPanel.getDownloadProgressPanel().setTitle(videoInfo.getTitle());

            FFmpegDownloadWorkerOld downloader = new FFmpegDownloadWorkerOld(
                    destinationVideoFile,
                    videoInfo,
                    ffmpegCommand,
                    ffmpegOptions
            );

            downloader.addPropertyChangeListener(this);
            downloader.addPropertyChangeListener(mainPanel.getDownloadProgressPanel());
//            LinkedBlockingQueue queue = (LinkedBlockingQueue) ffmpegDownloader.getQueue();
//            mainPanel.getDownloadProgressPanel().setQueue(queue);
//            videoInfo.setState(QUEUED_FOR_CONVERT);
            ffmpegDownloader.execute(downloader);

            mainPanel.getDownloadProgressPanel().setQueue(videoWorkerQueue);
            mainPanel.getDownloadProgressPanel().setVisible(true);
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
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

    /**
     * Prepares the Download and creates needed files in the destination folder
     */
    private void initializeDownload() {
        if (videoWorkerQueue.isEmpty()) {
            return;
        }

        mainPanel.getDownloadProgressPanel().setQueue(videoWorkerQueue);
        mainPanel.getDownloadProgressPanel().setVisible(true);

//        TwitchVideoInfo tvi = videoWorkerQueue.pop();

        videoInfo = videoWorkerQueue.poll(); // get the next Video
        videoInfo.setState(DOWNLOADING);
        try {
            // Select quality based on TwitchToolsPreferences
            String quality = videoInfo.getDownloadInfo().getPreferredQuality(TwitchLeecherPreferences.getQualityOrder());
            videoParts = videoInfo.getDownloadInfo().getTwitchBroadcastParts(quality); // get the Parts of a Video
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
        }

        mainPanel.getDownloadProgressPanel().setMaximum(videoParts.size() * 100);
        mainPanel.getDownloadProgressPanel().setValue(0);
        mainPanel.getDownloadProgressPanel().setTitle(videoInfo.getTitle());

        //Add Part numbers for the WorkerThread
        int i = 0;

        File destinationFilenameTemplate = getVideoFile(videoInfo, false);

        List<File> destinationFiles = new ArrayList<>();

        for (TwitchVideoPart videoPart : videoParts) {
            videoPart.getFileExtension();
            videoPart.setPartNumber(i++);
            String destinationFilePath = String.format(
                    "%s_%04d%s",
                    destinationFilenameTemplate.getAbsolutePath(),
                    videoPart.getPartNumber(),
                    videoPart.getFileExtension()
            );

            File destinationFile = new File(destinationFilePath);
//            File destinationFile = new File(destinationFilenameTemplate.getAbsolutePath() + "_" +
//                    String.valueOf(videoPart.getPartNumber() + videoPart.getFileExtension()));
            destinationFiles.add(destinationFile);
            TwitchDownloadWorker tdw = new TwitchDownloadWorker(destinationFile, videoPart);
            tdw.addPropertyChangeListener(this);
            tdw.addPropertyChangeListener(mainPanel.getDownloadProgressPanel());
            downloadExecutor.execute(tdw);
        }

        createPlaylistsFolder();

        playlist = new File(playlistFolderPath + videoInfo.getId() + ".m3u");
        createM3uPlaylist(playlist, destinationFiles);
        ffmpegFileListFile = new File(playlistFolderPath + videoInfo.getId() + ".ffmpeglist");
        createFFmpegFileList(ffmpegFileListFile, destinationFiles);
    }

    private void createPlaylistsFolder() {
        File playlistsFolder = new File(playlistFolderPath);

        if (!playlistsFolder.exists()) {
            boolean mkdirs = playlistsFolder.mkdirs();
            if (!mkdirs) {
                JOptionPane.showConfirmDialog(
                        mainFrame,
                        "Unable to create folder for playlists in " + playlistsFolder.getParent() + " make sure you have write access to that directory.",
                        "Unable to create playlist folder!",
                        ERROR_MESSAGE
                );
            }
        }
    }

    private void concatVideoParts(TwitchVideoInfo tvi) {
        File playlist = tvi.getMainRelatedFileOnDisk();
        FileOutputStream outputStream = null;
        File outfile = null;
        try {
            Scanner sc = new Scanner(playlist);
            while (sc.hasNextLine()) {
                File videoPart = new File(sc.nextLine());
                if (!videoPart.canRead()) {
                    System.err.printf("Unable to read %s, skipping file", videoPart);
                    continue;
                }
                if (outputStream == null) {
                    if (!videoPart.getPath().endsWith(".ts")) {
                        return;
                    }
//                    if(video is not a ts-Stream) return;
                    outfile = new File(videoPart.getPath().replaceFirst("_\\d+\\.ts$", ".ts"));
                    tvi.putRelatedFile("concatenated_file", outfile);
                    outputStream = new FileOutputStream(outfile, true);
                }
                FileInputStream inputStream = new FileInputStream(videoPart);
                byte[] buffer = new byte[1024];
                while ((inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer);
                }

                inputStream.close();
                videoPart.delete();
            }
            outputStream.close();

            // Update playlist and list for ffmpeg.
            List<File> outFiles = new ArrayList<>();
            outFiles.add(outfile);
            createM3uPlaylist(playlist, outFiles);
            ffmpegFileListFile = new File(playlistFolderPath + videoInfo.getId() + ".ffmpeglist");
            createFFmpegFileList(ffmpegFileListFile, outFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createM3uPlaylist(File fileName, List<File> files) {
        // return createFileList("", "", fileName, twitchDownloadWorkerQueue, ".m3u", List<File>files);
        try {
            createFileList(fileName, "", "", files);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
        }
    }

    private void createFFmpegFileList(File fileName, List<File> files) {
        try {
            createFileList(fileName, "file '", "'", files);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", ERROR_MESSAGE);
        }
    }

    private void createFileList(File playlistFile, String prefix, String postfix, List<File> files) throws IOException {
        FileWriter fileWriter = new FileWriter(playlistFile);
        for (File file : files) {
            fileWriter.append(prefix)
                    .append(file.getAbsolutePath())
                    .append(postfix)
                    .append(System.getProperty("line.separator"));
        }
        fileWriter.close();
    }

    public void progressFrameSetVisible(boolean x) {
        this.progressFrame.setVisible(x);
    }

    private void downloadFFMPEG() {
        try {
            if (OsValidator.isWindows()) {
                URL ffmpegExeUrl = new URL(URL_FFMPEG_EXE);

                HttpFileDownloadWorker httpFileDownloadWorker = new HttpFileDownloadWorker(ffmpegExeUrl, ffmpegExecutable);
                httpFileDownloadWorker.addPropertyChangeListener(mainPanel.getDownloadProgressPanel());
                mainPanel.getConvertProgressPanel().setTitle("FFMPEG");
                httpFileDownloadWorker.execute();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private File getVideoFile(TwitchVideoInfo videoInfo, boolean withExt) {
        String dateTime = Utils.getSimpleDateString(videoInfo.getRecordedAt().getTime());
        File channelFile = Utils.getDirAsFile(destinationDirPath + "/" + Utils.getValidFilename(videoInfo.getChannelName()) + "/");
        if (channelFile == null) return null;
        return new File( channelFile.getAbsolutePath() + "/" +
                Utils.getValidFilename(videoInfo.getTitle()) + "_" +  dateTime + (withExt ? ".mp4" : ""));
    }

    private boolean isQueueEmpty() {
        return videoWorkerQueue.isEmpty();
    }
}
