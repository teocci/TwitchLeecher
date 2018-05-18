package com.github.teocci.av.twitch.utils;

import com.github.teocci.av.twitch.exceptions.UnsupportedOsException;

import java.io.File;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class OsUtils
{
    private static final String PATTERN_VALID_FILE = "[^a-zA-Z0-9\\.\\-\\\\_ ]";
    private static final String PATTERN_FILE_EXT = "\\?.*$";

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
}
