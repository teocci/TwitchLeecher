package com.github.teocci.av.twitch.gui;

import java.util.Comparator;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Jun-15
 */
public class VideoPaneComparator implements Comparator
{
    @Override
    public int compare(Object pane1, Object pane2)
    {
        String name1 = ((VideoPane) pane1).getPublishedAt();
        String name2 = ((VideoPane) pane2).getPublishedAt();

        // descending order (ascending order would be: name1.compareTo(name2))
        return name2.compareTo(name1);
    }
}
