package com.github.teocci.av.twitch.gui.vod.channelsync;

import com.github.teocci.av.twitch.controllers.ChannelSyncController;
import com.github.teocci.av.twitch.interfaces.ChannelSyncControllerInterface;
import com.github.teocci.av.twitch.gui.OverallProgressPanel;
import com.github.teocci.av.twitch.gui.VideoInfoPanel;
import com.github.teocci.av.twitch.model.twitch.TwitchVideoInfo;
import com.github.teocci.av.twitch.model.twitch.TwitchVideoInfoList;
import com.github.teocci.av.twitch.utils.WrapLayout;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

/**
 * The MainPanel of the program. with a browser like look and feel
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class SyncChannelMainPanel extends JPanel implements PropertyChangeListener
{
    // Controller
    private final ChannelSyncControllerInterface controller;

    // Models
    private final TwitchVideoInfoList videoList;

    // localStuff = List to manage ResultPanelItems
    private List<JPanel> searchResultItemPanels;

    // GUI Components
    private final JPanel channelInputPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel();
    private final JPanel searchResultPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));

    private final JTextField channelInputFld = new JTextField();
    private final JButton channelInputBtn = new JButton("Get VOD's");
    private final JButton downloadAllBtn = new JButton("Download All");
    private final JButton loadMoreBtn = new JButton("load more ... ");
    private final JButton selectMostRecentBtn = new JButton("Select most Recent");

    private final JRadioButton highlightRadioBtn = new JRadioButton("Highlights");
    private final JRadioButton pastBroadcastsRadioBtn = new JRadioButton("Past Broadcasts");

    private final JLabel channelInputLabel = new JLabel("Channel: ");
    private final JLabel daysLabel = new JLabel("day's");

    private final JScrollPane searchResultScrollPane = new JScrollPane(searchResultPanel);
    private final JSpinner recentDaysSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));

    private OverallProgressPanel downloadProgressPanel = new OverallProgressPanel("Downloading");
    private OverallProgressPanel convertProgressPanel = new OverallProgressPanel("Converting");


    public SyncChannelMainPanel(ChannelSyncController controller, TwitchVideoInfoList videoList)
    {
        setName("SyncChannel");
        this.controller = controller;
        this.videoList = videoList;
        videoList.addPropertyChangeListener(this);
        this.searchResultItemPanels = new ArrayList<>();
        BorderLayout layout = new BorderLayout();
        setLayout(layout);

        channelInputFld.addActionListener(e -> searchChannel());
        channelInputBtn.addActionListener(e -> searchChannel());

        downloadAllBtn.addActionListener(e -> controller.downloadAllSelectedTwitchVideos());
        loadMoreBtn.addActionListener(e -> controller.loadMoreSearchResults());

        ButtonGroup vodTypeGroup = new ButtonGroup();
        vodTypeGroup.add(highlightRadioBtn);
        vodTypeGroup.add(pastBroadcastsRadioBtn);
        pastBroadcastsRadioBtn.setSelected(true);

//        selectMostRecentBtn.setEnabled(false);
        selectMostRecentBtn.addActionListener(e -> controller.selectMostRecent((Integer) recentDaysSpinner.getValue()));

        searchResultScrollPane.getVerticalScrollBar().setUnitIncrement(30);

        downloadProgressPanel.setIncreasingProgressEvts(true);
        convertProgressPanel.setIncreasingProgressEvts(false);

        layoutComponents();

        downloadProgressPanel.setVisible(false);
        convertProgressPanel.setVisible(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        switch (evt.getPropertyName()) {
            case "contentUpdate":
                updateResultContentPanel();
                break;
            case "twitchVideoInfoAdded":
                addResultPanel((TwitchVideoInfo) evt.getNewValue());
                break;
        }
    }

    private void layoutComponents()
    {
        channelInputPanel.setLayout(new GridBagLayout());
        add(channelInputPanel, BorderLayout.PAGE_START);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        channelInputPanel.add(channelInputLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 1;
        channelInputPanel.add(channelInputFld, c);

        c.weightx = 0.0;
        c.gridx = 2;
        channelInputPanel.add(highlightRadioBtn, c);

        c.gridx++;
        channelInputPanel.add(pastBroadcastsRadioBtn, c);

        c.gridx++;
        channelInputPanel.add(channelInputBtn, c);

        searchResultScrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        searchResultScrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);

        add(searchResultScrollPane, BorderLayout.CENTER);

        bottomPanel.setLayout(new GridBagLayout());
        add(bottomPanel, BorderLayout.PAGE_END);

        c.gridy = 0;
        c.gridx = 0;
        c.gridwidth = 4;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        bottomPanel.add(downloadProgressPanel, c);

        c.gridy++;
        bottomPanel.add(convertProgressPanel, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LINE_START;
        bottomPanel.add(selectMostRecentBtn, c);

        c.gridx = 1;
        bottomPanel.add(recentDaysSpinner, c);

        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 3;
        bottomPanel.add(downloadAllBtn, c);

        c.gridx = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        bottomPanel.add(daysLabel, c);

//        searchResultPanel.setBackground(Color.yellow);
//        add(searchResultPanel, BorderLayout.CENTER);
    }

    private void updateResultContentPanel()
    {
        //removeOldResults
        if (!searchResultItemPanels.isEmpty()) {
            for (JPanel searchResultItemPanel : searchResultItemPanels) {
                searchResultPanel.remove(searchResultItemPanel);
            }
            this.searchResultItemPanels = new ArrayList<>();
        }

        for (TwitchVideoInfo twitchVideoInfo : videoList.getVideos()) {
            VideoInfoPanel videoInfoPanel = null;
            try {
                videoInfoPanel = new VideoInfoPanel(twitchVideoInfo, controller);
            } catch (IOException e) {
                e.printStackTrace();
            }
            searchResultPanel.add(videoInfoPanel);
            searchResultItemPanels.add(videoInfoPanel);
            searchResultPanel.validate();
            searchResultPanel.repaint();
            validate();
            repaint();
        }
        searchResultPanel.add(loadMoreBtn);
        searchResultPanel.validate();
        searchResultPanel.repaint();
        validate();
        repaint();
    }

    private void searchChannel()
    {
        try {
            controller.searchFldText(channelInputFld.getText(), pastBroadcastsRadioBtn.isSelected());
        } catch (MalformedURLException e1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Weird Channel Input " + channelInputFld.getText() + " isn't a valid channel name",
                    "Invalid channel name",
                    JOptionPane.ERROR_MESSAGE
            );
            e1.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Channel not found!",
                    "Channel not found",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void addResultPanel(TwitchVideoInfo twitchVideoInfo)
    {
        VideoInfoPanel videoInfoPanel = null;
        try {
            videoInfoPanel = new VideoInfoPanel(twitchVideoInfo, controller);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to load PreviewImage for " + twitchVideoInfo.getTitle(), "IO Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        searchResultPanel.add(videoInfoPanel);
        searchResultItemPanels.add(videoInfoPanel);
        searchResultPanel.add(loadMoreBtn);
        searchResultPanel.validate();
        searchResultPanel.repaint();
        validate();
        repaint();
    }

    public OverallProgressPanel getDownloadProgressPanel()
    {
        return downloadProgressPanel;
    }

    public OverallProgressPanel getConvertProgressPanel()
    {
        return convertProgressPanel;
    }
}
