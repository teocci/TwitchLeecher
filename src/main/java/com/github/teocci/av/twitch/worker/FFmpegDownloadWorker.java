package com.github.teocci.av.twitch.worker;

import com.github.teocci.av.twitch.enums.WorkerState;
import com.github.teocci.av.twitch.gui.OverallProgressPanel;
import com.github.teocci.av.twitch.managers.FFmpegManager;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideo;
import com.github.teocci.av.twitch.utils.LogHelper;
import com.github.teocci.av.twitch.utils.Utils;
import javafx.concurrent.Task;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.teocci.av.twitch.enums.State.DOWNLOADED;
import static com.github.teocci.av.twitch.enums.State.DOWNLOADING;
import static com.github.teocci.av.twitch.enums.WorkerState.DONE;
import static com.github.teocci.av.twitch.enums.WorkerState.STARTED;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class FFmpegDownloadWorker extends Task<WorkerState>
{
    private static final String TAG = LogHelper.makeLogTag(FFmpegDownloadWorker.class);

    private final String TIME_PATTERN = "time=\\d{2,}:\\d{2}:\\d{2}";
    private final String PREFIX = "[FFMPEG] ";

    private File destinationVideoFile;

    private String ffmpegCommand;
    private List<String> ffmpegOptions;

    private String outputLine;
    private int videoLength;

    private TwitchVideo videoInfo;

    public FFmpegDownloadWorker(File destinationVideoFile, TwitchVideo videoInfo, String ffmpegCommand)
    {
        this.destinationVideoFile = destinationVideoFile;
        this.videoInfo = videoInfo;
        this.ffmpegCommand = ffmpegCommand;
        this.ffmpegOptions = new ArrayList<>();
        ffmpegOptions.add("-c:v");
        ffmpegOptions.add("libx264");
        ffmpegOptions.add("-c:a");
        ffmpegOptions.add("copy");
        ffmpegOptions.add("-bsf:a");
        ffmpegOptions.add("aac_adtstoasc");
    }

    public FFmpegDownloadWorker(File destinationVideoFile, TwitchVideo videoInfo, String ffmpegCommand, List ffmpegOptions)
    {
        this.destinationVideoFile = destinationVideoFile;
        this.videoInfo = videoInfo;
        this.ffmpegCommand = ffmpegCommand;
        this.ffmpegOptions = ffmpegOptions;
    }

    @Override
    protected WorkerState call() throws Exception
    {
        if (videoInfo == null) return null;
        setVideoLength(videoInfo.getLength());

        List<String> command = new ArrayList<>();
        if (destinationVideoFile.exists()) {
            LogHelper.e(TAG, "Destination file " +
                    destinationVideoFile.getAbsolutePath() + " exists. It will be overwritten.\n"
            );
            LogHelper.e(TAG, outputLine);
            destinationVideoFile.delete();
        }

        LogHelper.e(TAG, "Destination file: " + destinationVideoFile.getAbsolutePath());

        command.add(ffmpegCommand);
        command.add("-i");
        command.add(videoInfo.getPlaylistUrl().toString());
        command.addAll(ffmpegOptions);
        command.add(destinationVideoFile.getAbsolutePath());

        LogHelper.e(TAG, "command: " + String.join(", ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
//        LogHelper.e(TAG, "Starting to convert Video to " + destinationVideoFile.getAbsolutePath());
//        LogHelper.e(TAG, pb.command());
//        pcs.firePropertyChange("videoLength", 0, videoLength);
//        firePropertyChange("videoLength", 0, videoLength);
        videoInfo.setState(DOWNLOADING);
        updateValue(STARTED);
        updateMessage("Downloading " + destinationVideoFile.getName() + ".");
        pb.directory(new File(destinationVideoFile.getParent()));

        try {
            Process p = pb.start();
            Scanner pSc = new Scanner(p.getErrorStream());
            while (pSc.hasNextLine()) {
                String line = pSc.nextLine();
//                LogHelper.e(TAG, PREFIX + line);

                outputLine = PREFIX + line + "\n";
                LogHelper.e(TAG, outputLine);

                Matcher matcher = Pattern.compile(TIME_PATTERN).matcher(line);
                if (matcher.find()) {
                    try {
                        LogHelper.e(TAG, PREFIX + line);
                        if (videoLength > 0) {
                            int progress = Utils.getProgress(matcher.group(0).replace("time=", ""));
//                            int percent = (progress * 100) / videoLength;
                            updateProgress(progress, videoLength);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogHelper.e(TAG, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogHelper.e(TAG, "command exec has ended");

//        try {
//            Scanner fileListSc = new Scanner(fileListForFFmpeg);
//            while (fileListSc.hasNextLine()) {
//                String line = fileListSc.nextLine();
//                line = line.replace("file '", "").replace("'", "");
//                File partFile = new File(line);
//                if (partFile.delete()) {
//                    LogHelper.e(TAG, "deleting " + partFile.getPath());
//                    printToPropertyChangeListeners("deleting " + partFile.getPath() + "\n");
//                }
//            }
//            fileListSc.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        if (fileListForFFmpeg.delete()) {
//            LogHelper.e(TAG, "deleting " + fileListForFFmpeg.getName());
//            printToPropertyChangeListeners("deleting " + fileListForFFmpeg.getName() + "\n");
//        }

        if (videoInfo != null) {
            videoInfo.setState(DOWNLOADED);
            videoInfo.setMainRelatedFileOnDisk(destinationVideoFile);
//            firePropertyChange("state", STARTED, DONE);
        }
        return DONE;
    }

    public void setVideoLength(int videoLength)
    {
        int oldVideoLength = this.videoLength;
        this.videoLength = videoLength;
//        pcs.firePropertyChange("videoLength", oldVideoLength, this.videoLength);
//        firePropertyChange("videoLength", oldVideoLength, videoLength);
    }

    public TwitchVideo getRelatedTwitchVideoInfo()
    {
        return videoInfo;
    }

    public void setVideoInfo(TwitchVideo videoInfo)
    {
        this.videoInfo = videoInfo;
    }

    public TwitchVideo getVideoInfo()
    {
        return videoInfo;
    }

    public File getDestinationVideoFile()
    {
        return destinationVideoFile;
    }
}
