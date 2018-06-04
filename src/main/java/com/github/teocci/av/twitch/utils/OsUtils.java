package com.github.teocci.av.twitch.utils;

import com.github.teocci.av.twitch.TwitchLeecher;
import com.github.teocci.av.twitch.TwitchVodLeecher;
import com.github.teocci.av.twitch.exceptions.UnsupportedOsException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.github.teocci.av.twitch.utils.Config.PREFERENCES_FILE;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class OsUtils
{
    private static final String TAG = LogHelper.makeLogTag(OsUtils.class);

    private static final String PATTERN_VALID_FILE = "[^a-zA-Z0-9\\.\\-\\\\_ ]";
    private static final String PATTERN_FILE_EXT = "\\?.*$";

    private static final int ARCH_64 = 1;
    private static final int ARCH_32 = 2;

    public static String getUserHome()
    {
        if (OsValidator.isWindows()) {
            return System.getenv().get("USERPROFILE");
        } else if (OsValidator.isMac()) {
            return System.getenv().get("HOME");
        } else if (OsValidator.isUnix()) {
            return System.getenv().get("HOME");
        } else {
            try {
                throw new UnsupportedOsException();
            } catch (UnsupportedOsException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    public static int getSystemArch()
    {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

        return arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? ARCH_64 : ARCH_32;
    }

    public static String getValidFilename(String filename)
    {
        String validFilename = filename.replaceAll(PATTERN_VALID_FILE, "");
        return validFilename.replaceAll(" ", "_");
    }

    public static String getFileExtension(File file)
    {
        String fileExtension = "";
        String filename = file.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0) fileExtension = filename.substring(i);
        fileExtension = fileExtension.replaceAll(PATTERN_FILE_EXT, "");
        return fileExtension;
    }


    public boolean saveGroups(String groups)
    {
        if (groups == null) return false;

        try (Writer writer = new FileWriter(PREFERENCES_FILE)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(groups, writer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Calendar convert2Calendar(String dateTime)
    {
        String date = dateTime.split("T")[0];
        String time = dateTime.split("T")[1];
        int year = new Integer(date.split("-")[0]);
        int month = new Integer(date.split("-")[1]);
        int day = new Integer(date.split("-")[2]);
        int hourOfDay = new Integer(time.split(":")[0]);
        int minute = new Integer(time.split(":")[1]);
        int secound = new Integer(time.split(":")[2].substring(0, 2));

        Calendar recordedAtCalendar = GregorianCalendar.getInstance();
        recordedAtCalendar.set(year, month - 1, day, hourOfDay, minute, secound);
        return recordedAtCalendar;
    }

    public static URI getJarURI(ProtectionDomain domain) throws URISyntaxException
    {
        return domain.getCodeSource().getLocation().toURI();
    }

    public static File getDirAsFile(String dirPath)
    {
        File dirFile = new File(dirPath);
        if (!dirFile.exists() && !dirFile.mkdir()) return null;

        return dirFile;
    }

    public static boolean checkDir(String dirPath)
    {
        File destinationFile = OsUtils.getDirAsFile(dirPath);
        if (destinationFile == null || !destinationFile.exists()) {
            if (destinationFile != null && !destinationFile.mkdir()) {
                LogHelper.e(TAG, "destinationFile was not created.");
                return false;
            }
        }

        LogHelper.e(TAG, "destinationFile exists.");
        return true;
    }

    public static String getFFmpegPath(ProtectionDomain domain) throws URISyntaxException
    {
        return new File(getJarURI(domain)).getParent().concat("/ffmpeg.exe");
    }

    public static boolean is64Arch()
    {
        return getSystemArch() == ARCH_64;
    }

    public static boolean is32Arch()
    {
        return getSystemArch() == ARCH_32;
    }
}
