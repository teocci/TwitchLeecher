package com.github.teocci.av.twitch.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchToolPreferences
{
    private static Preferences prefs;
    private static List<String> qualities;

    public static final String KEY_DESTINATION_DIR = "destinationDir";
    public static final String KEY_FILENAME_PATTERN = "filenamePattern";

    private TwitchToolPreferences() {}

    public static Preferences getInstance()
    {
        if (prefs == null) {
            prefs = Preferences.userRoot().node("/com/trabauer/twitchtools");
        }
        return prefs;
    }

    public static void setQualityOrder(List<String> qualities)
    {
        String key = "QualityPriority";
        String value = "";
        for (String quality : qualities) {
            value = value + quality + ";";
        }

        prefs.put(key, value);
    }

    public static List<String> getQualityOrder()
    {
        String value = getInstance().get("QualityPriority", "source;high;medium;low;mobile");
        String values[] = value.split(";");
        List<String> qualities = new ArrayList<>();
        Collections.addAll(qualities, values);

        return qualities;
    }
}
