package com.github.teocci.av.twitch.worker;

import com.github.teocci.av.twitch.gui.OverallProgressPanel;
import com.github.teocci.av.twitch.model.twitch.TwitchVideoInfo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.teocci.av.twitch.enums.State.CONVERTED;
import static com.github.teocci.av.twitch.enums.State.DOWNLOADED;
import static com.github.teocci.av.twitch.enums.State.DOWNLOADING;
import static com.github.teocci.av.twitch.enums.WorkerState.DONE;
import static com.github.teocci.av.twitch.enums.WorkerState.STARTED;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class FFmpegDownloadWorker implements Runnable
{
    private final String TIME_PATTERN = "time=\\d{2,}:\\d{2}:\\d{2}";
    private final String PREFIX = "[FFMPEG] ";

    private File destinationVideoFile;

    private String ffmpegCommand;
    private List<String> ffmpegOptions;

    private String outputLine;
    private int videoLength;

    private final List<PropertyChangeListener> callbacks = new ArrayList<>();

    private TwitchVideoInfo videoInfo;

    public FFmpegDownloadWorker(File destinationVideoFile, TwitchVideoInfo videoInfo, String ffmpegCommand)
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

    public FFmpegDownloadWorker(File destinationVideoFile, TwitchVideoInfo videoInfo, String ffmpegCommand, List ffmpegOptions)
    {
        this.destinationVideoFile = destinationVideoFile;
        this.videoInfo = videoInfo;
        this.ffmpegCommand = ffmpegCommand;
        this.ffmpegOptions = ffmpegOptions;
    }

    @Override
    public void run()
    {
        if (videoInfo == null) return;
        setVideoLength(videoInfo.getLength());

        List<String> command = new ArrayList<>();
        if (destinationVideoFile.exists()) {
            printToPropertyChangeListeners("Destination file " +
                    destinationVideoFile.getAbsolutePath() +
                    " exists. It will be overwritten.\n"
            );
            System.out.println(outputLine);
            destinationVideoFile.delete();
        }

        System.out.println("Destination file: " + destinationVideoFile.getAbsolutePath());

        command.add(ffmpegCommand);
        command.add("-i");
        command.add(videoInfo.getPlaylistUrl().toString());
        command.addAll(ffmpegOptions);
        command.add(destinationVideoFile.getAbsolutePath());

        System.out.println("command: " + String.join(", ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
//        System.out.println("Starting to convert Video to " + destinationVideoFile.getAbsolutePath());
//        System.out.println(pb.command());
//        pcs.firePropertyChange("videoLength", 0, videoLength);
        firePropertyChange("videoLength", 0, videoLength);
        videoInfo.setState(DOWNLOADING);

        pb.directory(new File(destinationVideoFile.getParent()));

        try {
            Process p = pb.start();
            Scanner pSc = new Scanner(p.getErrorStream());
            while (pSc.hasNextLine()) {
                String line = pSc.nextLine();
//                System.out.println(PREFIX + line);

                outputLine = PREFIX + line + "\n";
                printToPropertyChangeListeners(outputLine);

                Matcher matcher = Pattern.compile(TIME_PATTERN).matcher(line);
                if (matcher.find()) {
                    try {
                        System.out.println(PREFIX + line);
                        if (videoLength > 0) {
                            int progress = getProgress(matcher.group(0).replace("time=", ""));
                            int percent = (progress * 100) / videoLength;
                            getOverallProgressPanel().setProgress(Math.min(100, percent));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        printToPropertyChangeListeners(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("command exec has ended");

//        try {
//            Scanner fileListSc = new Scanner(fileListForFFmpeg);
//            while (fileListSc.hasNextLine()) {
//                String line = fileListSc.nextLine();
//                line = line.replace("file '", "").replace("'", "");
//                File partFile = new File(line);
//                if (partFile.delete()) {
//                    System.out.println("deleting " + partFile.getPath());
//                    printToPropertyChangeListeners("deleting " + partFile.getPath() + "\n");
//                }
//            }
//            fileListSc.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        if (fileListForFFmpeg.delete()) {
//            System.out.println("deleting " + fileListForFFmpeg.getName());
//            printToPropertyChangeListeners("deleting " + fileListForFFmpeg.getName() + "\n");
//        }

        if (videoInfo != null) {
            videoInfo.setState(DOWNLOADED);
            videoInfo.setMainRelatedFileOnDisk(destinationVideoFile);
            firePropertyChange("state", STARTED, DONE);
        }
    }

    public void setVideoLength(int videoLength)
    {
        int oldVideoLength = this.videoLength;
        this.videoLength = videoLength;
//        pcs.firePropertyChange("videoLength", oldVideoLength, this.videoLength);
        firePropertyChange("videoLength", oldVideoLength, videoLength);
    }

    public TwitchVideoInfo getRelatedTwitchVideoInfo()
    {
        return videoInfo;
    }

    public void setVideoInfo(TwitchVideoInfo videoInfo)
    {
        this.videoInfo = videoInfo;
    }

    public TwitchVideoInfo getVideoInfo()
    {
        return videoInfo;
    }

    public File getDestinationVideoFile()
    {
        return destinationVideoFile;
    }

    protected void printToPropertyChangeListeners(String line)
    {
        String oldOutputLine = this.outputLine;
        this.outputLine = line;
//        pcs.firePropertyChange("outputline", oldOutputLine, line);
        firePropertyChange("outputline", oldOutputLine, line);
    }

    private int getProgress(String time)
    {
        String[] timeStrParts = time.split(":");
        int progress = Integer.parseInt(timeStrParts[0]) * 3600;
        progress += Integer.parseInt(timeStrParts[1]) * 60;
        progress += Integer.parseInt(timeStrParts[2]);

        return progress;
    }

    private OverallProgressPanel getOverallProgressPanel()
    {
        synchronized (callbacks) {
            if (!callbacks.isEmpty()) {
                for (PropertyChangeListener cl : callbacks) {
                    if (cl instanceof OverallProgressPanel) return (OverallProgressPanel) cl;
                }
            }
        }
        return null;
    }

    /**
     * See {@link PropertyChangeListener} to check out what events will be fired once you set up a listener.
     *
     * @param callback The callback
     */
    public void addPropertyChangeListener(PropertyChangeListener callback)
    {
        synchronized (callbacks) {
            if (!callbacks.isEmpty()) {
                for (PropertyChangeListener cl : callbacks) {
                    if (cl == callback) return;
                }
            }
            callbacks.add(callback);
        }
    }

    /**
     * Removes the callback.
     *
     * @param callback The callback
     */
    public void removePropertyChangeListener(PropertyChangeListener callback)
    {
        synchronized (callbacks) {
            callbacks.remove(callback);
        }
    }

//    protected void postError(Exception exception, int id) {
//        synchronized (callbacks) {
//            if (!callbacks.isEmpty()) {
//                for (PropertyChangeListener cl : callbacks) {
//                    cl.onError(this, exception, id);
//                }
//            }
//        }
//    }

    protected void firePropertyChange(PropertyChangeEvent event)
    {
        synchronized (callbacks) {
            if (!callbacks.isEmpty()) {
                for (PropertyChangeListener cl : callbacks) {
                    cl.propertyChange(event);
                }
            }
        }
    }

    protected void firePropertyChange(String key, Object oldValue, Object newValue)
    {
        synchronized (callbacks) {
            if (!callbacks.isEmpty()) {
                PropertyChangeEvent event = new PropertyChangeEvent(this, key, oldValue, newValue);
                for (PropertyChangeListener cl : callbacks) {
                    cl.propertyChange(event);
                }
            }
        }
    }
}
