package com.github.teocci.av.twitch.worker;

import com.github.teocci.av.twitch.gui.OverallProgressPanel;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideoInfo;

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
import static com.github.teocci.av.twitch.enums.State.CONVERTING;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class FFmpegConverterWorker implements Runnable
{
    private final String TIME_PATTERN = "time=\\d{2,}:\\d{2}:\\d{2}";
    private final String PREFIX = "[FFMPEG] ";

    private File destinationVideoFile, fileListForFfmpeg;
    private String ffmpegCommand;
    private List<String> ffmpegOptions;
    private String outputLine;
    private int videoLength;

    private PropertyChangeListener callback;

    private TwitchVideoInfo videoInfo;

    public FFmpegConverterWorker(File destinationVideoFile, File fileListForFFmpeg, String ffmpegCommand)
    {
        this.destinationVideoFile = destinationVideoFile;
        this.fileListForFfmpeg = fileListForFFmpeg;
        this.ffmpegCommand = ffmpegCommand;
        this.ffmpegOptions = new ArrayList<>();
        ffmpegOptions.add("-c:v");
        ffmpegOptions.add("libx264");
        ffmpegOptions.add("-c:a");
        ffmpegOptions.add("copy");
        ffmpegOptions.add("-bsf:a");
        ffmpegOptions.add("aac_adtstoasc");
    }

    public FFmpegConverterWorker(File destinationVideoFile, File fileListForFFmpeg, String ffmpegCommand, List<String> ffmpegOptions)
    {
        this.destinationVideoFile = destinationVideoFile;
        this.fileListForFfmpeg = fileListForFFmpeg;
        this.ffmpegCommand = ffmpegCommand;
        this.ffmpegOptions = ffmpegOptions;
    }

    public void setVideoLength(int videoLength)
    {
        int oldVideoLength = this.videoLength;
        this.videoLength = videoLength;
//        pcs.firePropertyChange("videoLength", oldVideoLength, this.videoLength);
        PropertyChangeEvent event = new PropertyChangeEvent(this, "videoLength", oldVideoLength, this.videoLength);
        this.callback.propertyChange(event);
    }

    public TwitchVideoInfo getVideoInfo()
    {
        return videoInfo;
    }

    public void setRelatedTwitchVideoInfo(TwitchVideoInfo relatedTvi)
    {
        this.videoInfo = relatedTvi;
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
        PropertyChangeEvent event = new PropertyChangeEvent(this, "outputline", oldOutputLine, line);
        this.callback.propertyChange(event);
    }

    @Override
    public void run()
    {
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
        command.add("-f");
        command.add("concat");
        command.add("-safe 0");
        command.add("-i");
        command.add(fileListForFfmpeg.getAbsolutePath());
        command.addAll(ffmpegOptions);
        command.add(destinationVideoFile.getAbsolutePath());

        System.out.println("command: " + String.join(", ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
//        System.out.println("Starting to convert Video to " + destinationVideoFile.getAbsolutePath());
//        System.out.println(pb.command());
//        pcs.firePropertyChange("videoLength", 0, videoLength);
        PropertyChangeEvent event = new PropertyChangeEvent(this, "videoLength", 0, videoLength);
        callback.propertyChange(event);
        videoInfo.setState(CONVERTING);

        pb.directory(new File(destinationVideoFile.getParent()));

        try {
            Process p = pb.start();
            Scanner pSc = new Scanner(p.getErrorStream());
            while (pSc.hasNextLine()) {
                String line = pSc.nextLine();
                System.out.println(PREFIX + line);

                outputLine = PREFIX + line + "\n";
                printToPropertyChangeListeners(outputLine);

                Matcher matcher = Pattern.compile(TIME_PATTERN).matcher(line);
                if (matcher.find()) {
                    System.out.println(PREFIX + line);
                    String timeStr = matcher.group().replace("time=", "");
                    String timeStrParts[] = timeStr.split(":");
                    try {
                        int progress = Integer.parseInt(timeStrParts[0]) * 3600;
                        progress += Integer.parseInt(timeStrParts[1]) * 60;
                        progress += Integer.parseInt(timeStrParts[2]);
                        if (videoLength > 0) {
                            int percent = (progress * 100) / videoLength;
                            ((OverallProgressPanel) callback).setProgress(Math.min(100, percent));
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
//            Scanner fileListSc = new Scanner(fileListForFfmpeg);
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
//        if (fileListForFfmpeg.delete()) {
//            System.out.println("deleting " + fileListForFfmpeg.getName());
//            printToPropertyChangeListeners("deleting " + fileListForFfmpeg.getName() + "\n");
//        }

        if (videoInfo != null) {
            videoInfo.setState(CONVERTED);
            videoInfo.setMainRelatedFileOnDisk(destinationVideoFile);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener callback)
    {
        this.callback = callback;
    }
}
