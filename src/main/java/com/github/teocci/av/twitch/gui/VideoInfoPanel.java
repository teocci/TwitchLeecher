package com.github.teocci.av.twitch.gui;

import com.github.teocci.av.twitch.interfaces.ChannelSyncControllerInterface;
import com.github.teocci.av.twitch.enums.State;
import com.github.teocci.av.twitch.models.twitch.kraken.TwitchVideoInfo;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Map;

import static com.github.teocci.av.twitch.enums.State.*;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

/**
 * Represents one TwitchVideoInfo Object in the Search REsult Area in th GUI.
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class VideoInfoPanel extends JPanel implements ItemListener, PropertyChangeListener
{
    private final boolean debugColors = true;

    private final JLabel titleLbl;
    private final JLabel imageLbl;
    private final JLabel durationLbl;
    private final JLabel viewCountLbl;

    private final TwitchVideoInfo relatedTwitchVideoInfoObject;
    private final ChannelSyncControllerInterface controller;

    private final JPanel btnPanel;
    private final JButton downloadBtn;
    private final JButton playBtn;
    private final JButton convertBtn;
    private final JButton deleteBtn;
    private final JButton preferencesBtn;

    private final JLabel channelDisplayNameLbl;
    private final JCheckBox markForBatchCheckbo;
    private final JLabel linkToTwitchLbl;
    private final JLabel dateLbl;
    private final JLayeredPane previewImageLayeredPane;
    private final JLabel darkBarkLbl = new JLabel();


    // Borders
    private static int borderSize = 5;
    private static final Color selectedColor = new Color(165, 89, 243, 255);
    private static final Color unselectedColor = new Color(0, 0, 0, 50);
    private static final Color downloadedColor = Color.GREEN;
    private static final Color downloadingColor = Color.YELLOW;
    private static final Color convertingColor = Color.ORANGE;
    private static final Border selectedBorder = BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, selectedColor);
    private static final Border unselectedBorder = BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, unselectedColor);
    private static final Border downloadedBorder = BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, downloadedColor);
    private static final Border downloadingBorder = BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, downloadingColor);
    private static final Border convertingBorder = BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, convertingColor);


    public VideoInfoPanel(final TwitchVideoInfo relatedTwitchVideoInfoObject, final ChannelSyncControllerInterface controller) throws IOException
    {
        this.relatedTwitchVideoInfoObject = relatedTwitchVideoInfoObject;
        this.relatedTwitchVideoInfoObject.addPropertyChangeListenern(this);

        this.controller = controller;
        setLayout(new GridBagLayout());

        String title = relatedTwitchVideoInfoObject.getTitle();
        int cutlength = 40;
        if (title.length() > cutlength) {
            title = title.substring(0, cutlength);
            title = title.concat("...");
        }
        titleLbl = new JLabel(title);
        titleLbl.setToolTipText(relatedTwitchVideoInfoObject.getTitle());
        Font original = titleLbl.getFont();
        titleLbl.setFont(original.deriveFont(Font.BOLD, 14.0F));
        viewCountLbl = new JLabel(String.format("%d views", relatedTwitchVideoInfoObject.getViews()));
        viewCountLbl.setForeground(Color.WHITE);
        viewCountLbl.setBackground(new Color(0, 0, 0, 0));
        original = viewCountLbl.getFont();
        viewCountLbl.setFont(original.deriveFont(Font.BOLD));

        int duration = relatedTwitchVideoInfoObject.getLength();
        int seconds = duration % 60;
        int minutes = (duration / 60) % 60;
        int hours = duration / (60 * 60);
        durationLbl = new JLabel(String.format("%5d:%02d:%02d", hours, minutes, seconds));
        durationLbl.setFont(original.deriveFont(Font.BOLD));
        durationLbl.setForeground(Color.WHITE);

        imageLbl = new JLabel();
        imageLbl.setToolTipText(relatedTwitchVideoInfoObject.getTitle());

        if (relatedTwitchVideoInfoObject.getPreviewImage() != null) {
            addImageToThis();
        } else { //loadImage in background (improves performance with small connections)
            Runnable getPreviewRunnable = () -> {
                try {
                    relatedTwitchVideoInfoObject.loadPreviewImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Thread t = new Thread(getPreviewRunnable);
            t.start();
        }

        imageLbl.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (!relatedTwitchVideoInfoObject.relatedFileExists()) {
                    if (isSelected()) {
                        markForBatchCheckbo.setSelected(false);
                        relatedTwitchVideoInfoObject.setState(INITIAL);
                    } else {
                        markForBatchCheckbo.setSelected(true);
                        relatedTwitchVideoInfoObject.setState(SELECTED_FOR_DOWNLOAD);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                if (relatedTwitchVideoInfoObject.getState().equals(INITIAL)) {
                    if (isSelected()) setBorder(unselectedBorder);
                    else setBorder(selectedBorder);
                }
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                if (relatedTwitchVideoInfoObject.getState().equals(INITIAL)) {
                    if (isSelected()) setBorder(selectedBorder);
                    else setBorder(unselectedBorder);
                }
            }
        });

        markForBatchCheckbo = new JCheckBox("add to queue");
        markForBatchCheckbo.addItemListener(this);
        channelDisplayNameLbl = new JLabel(relatedTwitchVideoInfoObject.getChannelDisplaylName());
        linkToTwitchLbl = new JLabel("watch on Twitch");
        original = linkToTwitchLbl.getFont();
        Map attributes = original.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        linkToTwitchLbl.setFont(original.deriveFont(attributes));
        linkToTwitchLbl.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                try {
                    controller.openUrlInBrowser(relatedTwitchVideoInfoObject.getUrl());
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        linkToTwitchLbl.setForeground(Color.BLUE);
        dateLbl = new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(relatedTwitchVideoInfoObject.getRecordedAt().getTime()));

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));

        downloadBtn = new JButton("Download");
        downloadBtn.addActionListener((e) -> {
            if (e.getActionCommand().equals("download")) {
                controller.downloadTwitchVideo(relatedTwitchVideoInfoObject);
            } else if (e.getActionCommand().equals("watchLive")) {
                try {
                    controller.openUrlInBrowser(new URL("http://twitch.tv/" + relatedTwitchVideoInfoObject.getChannelName()));
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                }
            }
        });
        downloadBtn.setActionCommand("download");

        playBtn = new JButton("Play");
        playBtn.setActionCommand("watchVideo");
        playBtn.addActionListener((e) -> {
            System.out.println("Watch Btn pressed opening " + relatedTwitchVideoInfoObject.getMainRelatedFileOnDisk().getName());
            try {
                Desktop.getDesktop().open(relatedTwitchVideoInfoObject.getMainRelatedFileOnDisk());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        convertBtn = new JButton("Convert");
        convertBtn.addActionListener((e) -> controller.convert2mp4(relatedTwitchVideoInfoObject));

        deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener((e) -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Delete \"" + relatedTwitchVideoInfoObject.getTitle() + "\" ?",
                    "Delete?",
                    YES_NO_OPTION
            );
            if (choice == YES_OPTION) {
                controller.delete(relatedTwitchVideoInfoObject);
            }
        });

        preferencesBtn = new JButton("Preferences");
        preferencesBtn.setActionCommand("preferencesBtn");
        preferencesBtn.addActionListener((e) -> createPreferenceDialog());

        previewImageLayeredPane = new JLayeredPane();

        layoutComponents();
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getSource() == markForBatchCheckbo) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (relatedTwitchVideoInfoObject.getState().equals(INITIAL)) {
                    relatedTwitchVideoInfoObject.setState(SELECTED_FOR_DOWNLOAD);
                }
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (relatedTwitchVideoInfoObject.getState().equals(SELECTED_FOR_DOWNLOAD)) {
                    relatedTwitchVideoInfoObject.setState(INITIAL);
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getSource() == relatedTwitchVideoInfoObject) {
            if (evt.getPropertyName().equals("previewImage")) {  // Performance improvement
                // Loading the Image is done in a background Thread. This adds the Image Preview when its done.
                try {
                    addImageToThis();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (evt.getPropertyName().equals("state")) {
                State currentState = (State) evt.getNewValue();
                changeLookAndFeelBasedOnState(currentState);
            } else if (evt.getPropertyName().equals("title")) {
                titleLbl.setText(relatedTwitchVideoInfoObject.getTitle());
            }
        }
    }

    private void layoutComponents()
    {
        imageLbl.setBounds(0, 0, 320, 180);
        viewCountLbl.setBounds(5, 0, 310, 25);
        viewCountLbl.setHorizontalAlignment(SwingConstants.LEFT);

        durationLbl.setBounds(5, 0, 310, 25);
        durationLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        //Adding a dark bar to the previewImage to increase the readability of the view, count and duration
        darkBarkLbl.setBounds(0, 0, 320, 25);
        darkBarkLbl.setBackground(new Color(0, 0, 0, 80));
        darkBarkLbl.setOpaque(true);
        darkBarkLbl.setVerticalAlignment(JLabel.TOP);
        darkBarkLbl.setHorizontalAlignment(JLabel.CENTER);

        previewImageLayeredPane.setPreferredSize(new Dimension(315, 180));
        previewImageLayeredPane.add(durationLbl, 5);
        previewImageLayeredPane.add(viewCountLbl, 5);
        previewImageLayeredPane.add(darkBarkLbl, 7);
        previewImageLayeredPane.add(imageLbl, 10);
        previewImageLayeredPane.setBackground(Color.yellow);
        //previewImageLayeredPane.add(viewCountLbl, 1);


        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 0);
        c.ipadx = 3;
        c.ipady = 3;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_START;
        add(previewImageLayeredPane, c);

        c.insets = new Insets(2, 2, 2, 2);
        c.gridy++;
        c.gridwidth = 2;
        add(titleLbl, c);
        titleLbl.setMinimumSize(new Dimension(300, 20));
        titleLbl.setMaximumSize(new Dimension(300, 100));
        titleLbl.setPreferredSize(new Dimension(300, 20));

        c.gridwidth = 1;
        c.gridy++;
        add(channelDisplayNameLbl, c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_END;
        add(dateLbl, c);

        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_START;
        add(linkToTwitchLbl, c);


        c.gridy++;
        add(markForBatchCheckbo, c);

        c.gridx++;
        c.anchor = GridBagConstraints.LINE_END;

        btnPanel.add(deleteBtn);
        btnPanel.add(convertBtn);
        btnPanel.add(preferencesBtn);
        btnPanel.add(downloadBtn);
        btnPanel.add(playBtn);
        add(btnPanel, c);

        setInitialLayout();
        changeLookAndFeelBasedOnState(relatedTwitchVideoInfoObject.getState());
    }

    private void setInitialLayout()
    {
        downloadBtn.setVisible(true);
        preferencesBtn.setVisible(false);
        playBtn.setVisible(false);
        deleteBtn.setVisible(false);
        convertBtn.setVisible(false);
        setBorder(unselectedBorder);
    }

    private void setDownloadingLayout()
    {
        setInitialLayout();
        setQueuedLayout();
        setBorder(downloadingBorder);
    }


    private void setDownloadedLayout()
    {
        downloadBtn.setVisible(false);
        preferencesBtn.setVisible(false);
        deleteBtn.setVisible(true);
        deleteBtn.setEnabled(true);
        playBtn.setVisible(true);
        convertBtn.setVisible(true);
        convertBtn.setEnabled(true);
        markForBatchCheckbo.setEnabled(false);
        markForBatchCheckbo.setVisible(false);
        setBorder(downloadedBorder);
    }

    private void setConvertedLayout()
    {
        setDownloadedLayout();
        convertBtn.setVisible(false);
        convertBtn.setEnabled(false);
    }

    private void setConvertingLayout()
    {
        setDownloadedLayout();
        convertBtn.setEnabled(false);
        setBorder(convertingBorder);
    }

    private void setQueuedLayout()
    {
        convertBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        downloadBtn.setEnabled(false);
        markForBatchCheckbo.setEnabled(false);
        preferencesBtn.setEnabled(false);
    }

    private void createPreferenceDialog()
    {
        VideoInfoPreferencesDialog preferencesDialog = new VideoInfoPreferencesDialog();
        preferencesDialog.setVisible(true);
    }


    private boolean isSelected()
    {
        return relatedTwitchVideoInfoObject.getState().equals(SELECTED_FOR_DOWNLOAD);
    }

    private void changeLookAndFeelBasedOnState(State currentState)
    {
        switch (currentState) {
            case SELECTED_FOR_DOWNLOAD:
                setBorder(selectedBorder);
                markForBatchCheckbo.setSelected(true);
                break;
            case QUEUED_FOR_DOWNLOAD: //Video is in a Queue
                downloadBtn.setEnabled(false);
                convertBtn.setEnabled(false);
                markForBatchCheckbo.setEnabled(false);
                setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Color.CYAN));
                break;
            case DOWNLOADING:
                setDownloadingLayout();
                break;
            case DOWNLOADED:
                setDownloadedLayout();
                break;
            case QUEUED_FOR_CONVERT:
                setConvertingLayout();
                setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Color.blue));

                break;
            case CONVERTING:
                setConvertingLayout();
                break;
            case CONVERTED:
                setConvertedLayout();
                break;
            case INITIAL:
                setInitialLayout();
                break;
            case LIVE:
                setInitialLayout();
                int viewers = 0;
                try {
                    viewers = relatedTwitchVideoInfoObject.getChannel().getStream().getViewers();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                downloadBtn.setText("Watch Live");
                downloadBtn.setActionCommand("watchLive");
                durationLbl.setText("LIVE");
                viewCountLbl.setText(String.format("%d viewers", viewers));
                darkBarkLbl.setBackground(new Color(255, 0, 0, 80));
                markForBatchCheckbo.setVisible(false);
                break;
            default:
                break;
        }
    }

    public void addImageToThis() throws IOException
    {
        imageLbl.setIcon(new ImageIcon(relatedTwitchVideoInfoObject.getPreviewImage()));
        imageLbl.repaint();
    }
}
