package com.github.teocci.av.twitch.model;

import com.github.teocci.av.twitch.model.twitch.TwitchVideoInfo;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class VideoQualityComboBoxModel implements ComboBoxModel<String>, Observer
{
    private ArrayList<ListDataListener> listDataListeners;
    private ArrayList<String> qualities;
    private int selectedItem = 0;


    public VideoQualityComboBoxModel(TwitchVideoInfo twitchVideo) throws IOException
    {
        this();
        fillQualities(twitchVideo);
    }

    private void fillQualities(TwitchVideoInfo twitchVideo) throws IOException
    {
        if (twitchVideo.getDownloadInfo().getAvailableQualities().isEmpty()) qualities.add("None");
        qualities.addAll(twitchVideo.getDownloadInfo().getAvailableQualities());
        //this.selectedItem = qualities.indexOf(twitchVideo.getBestAvailableQuality());
    }

    public VideoQualityComboBoxModel()
    {
        this.listDataListeners = new ArrayList<>();
        this.qualities = new ArrayList<>();
    }

    @Override
    public void setSelectedItem(Object anItem)
    {
        selectedItem = qualities.indexOf(anItem);
    }

    @Override
    public Object getSelectedItem()
    {
        return qualities.get(selectedItem);
    }

    @Override
    public int getSize()
    {
        return qualities.size();
    }

    @Override
    public String getElementAt(int index)
    {
        return qualities.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l)
    {
        listDataListeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l)
    {
        listDataListeners.remove(l);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (o.getClass().equals(TwitchVideoInfo.class)) {
            TwitchVideoInfo twitchVideo = (TwitchVideoInfo) o;
            this.qualities = new ArrayList<>();
            try {
                fillQualities(twitchVideo);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (ListDataListener listener : listDataListeners) {
                listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize() - 1));
            }
        }
    }
}



