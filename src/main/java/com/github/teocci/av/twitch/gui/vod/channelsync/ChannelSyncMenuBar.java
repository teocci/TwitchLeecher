package com.github.teocci.av.twitch.gui.vod.channelsync;

import com.github.teocci.av.twitch.controllers.ChannelSyncController;
import com.github.teocci.av.twitch.utils.Utils;
import com.github.teocci.av.twitch.TwitchLeecherPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.List;
import java.util.prefs.Preferences;

import static com.github.teocci.av.twitch.utils.Config.IMAGE_ICON;
import static com.github.teocci.av.twitch.utils.Config.PROGRAM_VERSION;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class ChannelSyncMenuBar extends JMenuBar implements ActionListener
{
    //    private final JMenu fileMenu;
    private final JMenu settingsMenu;
    private final JMenuItem destinationFolderMenuItem;
    //    private final JMenuItem qualityPriorityMenuItem;
    private final ChannelSyncController controller;
    private final Preferences prefs;
    private final SettingsFolderDialog settingsFolderDialog;
    private final AboutThisProgramDialog aboutThisProgramDialog;
    private final JFrame mainFrame;
    private final JMenu viewMenu, helpMenu;
    private final JMenuItem showProgressWindowsMenuItem;
    private final JMenuItem showAboutDialogMenuItem;


    private class SettingsFolderDialog extends JDialog
    {
        private final JList qualitiesJList;

        public SettingsFolderDialog(Frame owner)
        {
            super(owner, "Select Destination Folder");
            JLabel destinationFolderLbl = new JLabel("Destination Folder:");
            final JTextField destinationFolderTextField = new JTextField();
            destinationFolderTextField.setText(prefs.get(TwitchLeecherPreferences.KEY_DOWNLOAD_PATH, ""));

            JButton selectFolderBtn = new JButton("...");
            selectFolderBtn.addActionListener(e -> {
                String currentDestinationDirectory = prefs.get(TwitchLeecherPreferences.KEY_DOWNLOAD_PATH, Utils.getUserHome());
                File destinationDirectory = showDestinationDirChooser(currentDestinationDirectory);
                destinationFolderTextField.setText(destinationDirectory.getPath());
            });

            JLabel helpTextLbl = new JLabel("<html>You are able to move the qualities up and down. " +
                    "TwitchLeecher tries to download the video qualities in that order. " +
                    "If the quality at the top isn't available, it will try to download the " +
                    "second, third ... and so on.</html>");


            JButton okBtn = new JButton("OK");
            okBtn.addActionListener(e -> {
                prefs.put(TwitchLeecherPreferences.KEY_DOWNLOAD_PATH, destinationFolderTextField.getText());
                settingsFolderDialog.setVisible(false);
            });


            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.addActionListener(e -> settingsFolderDialog.setVisible(false));

            JButton upBtn = new JButton("Up");
            upBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int index = qualitiesJList.getSelectedIndex();
                    moveQuality(index, index - 1);
                }
            });


            JButton downBtn = new JButton("Down");
            downBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int index = qualitiesJList.getSelectedIndex();
                    moveQuality(index, index + 1);
                }
            });

            qualitiesJList = new JList(TwitchLeecherPreferences.getQualityOrder().toArray());
            qualitiesJList.setSelectedIndex(ListSelectionModel.SINGLE_SELECTION);
            qualitiesJList.setBorder(BorderFactory.createTitledBorder("Quality Priority"));

            GridBagLayout formLayout = new GridBagLayout();

            JPanel formPanel = new JPanel(formLayout);
            JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JPanel contentPane = new JPanel(new BorderLayout());
            this.setContentPane(contentPane);

            contentPane.add(formPanel, BorderLayout.CENTER);
            contentPane.add(controlsPanel, BorderLayout.PAGE_END);

            controlsPanel.add(cancelBtn);
            controlsPanel.add(okBtn);


            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 5, 5, 5);


            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.LINE_END;
            formPanel.add(destinationFolderLbl, c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.gridx++;
            c.anchor = GridBagConstraints.CENTER;
            formPanel.add(destinationFolderTextField, c);


            c.gridx++;
            c.weightx = 0.0;
            c.anchor = GridBagConstraints.LINE_END;
            c.fill = GridBagConstraints.NONE;
            formPanel.add(selectFolderBtn, c);

            c.gridy++;
            c.gridx = 0;
            c.gridwidth = 3;
            c.anchor = GridBagConstraints.CENTER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;

            formPanel.add(qualitiesJList, c);


            JPanel upDownPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            upDownPanel.add(downBtn);
            upDownPanel.add(upBtn);

            c.gridy++;
            formPanel.add(upDownPanel, c);

            c.gridy++;


            helpTextLbl.setPreferredSize(new Dimension(400, 100));
            formPanel.add(helpTextLbl, c);

            //setSize(400, 500);
            pack();
        }

        private void moveQuality(int oldIndex, int newIndex)
        {
            List<String> qualities = TwitchLeecherPreferences.getQualityOrder();
            if (newIndex < 0 || newIndex > (qualities.size() - 1))
                return;
            String quality = qualities.get(oldIndex);
            qualities.remove(oldIndex);
            qualities.add(newIndex, quality);
            TwitchLeecherPreferences.setQualityOrder(qualities);
            qualitiesJList.setListData(TwitchLeecherPreferences.getQualityOrder().toArray());
            qualitiesJList.updateUI();
            qualitiesJList.setSelectedIndex(newIndex);
        }
    }

    private class AboutThisProgramDialog extends JDialog implements ActionListener
    {
        private final JLabel visitProjectSiteLbl;
        private JButton closeBtn, licenseBtn;

        public AboutThisProgramDialog(Frame owner)
        {
            super(owner, "About Twitch Leecher");

            GridBagLayout layoutManager = new GridBagLayout();
            GridBagConstraints gc = new GridBagConstraints();
            this.setLayout(layoutManager);

            ImageIcon logoIcon = new ImageIcon(IMAGE_ICON);
            JLabel programNameVersionLabel = new JLabel(PROGRAM_VERSION, logoIcon, JLabel.CENTER);
            Font origFont = programNameVersionLabel.getFont().deriveFont(Font.BOLD);
            programNameVersionLabel.setFont(origFont.deriveFont(30.0F));
            programNameVersionLabel.setVerticalTextPosition(JLabel.BOTTOM);
            programNameVersionLabel.setHorizontalTextPosition(JLabel.CENTER);

            gc.gridx = 0;
            gc.gridy = 0;
            gc.insets = new Insets(5, 5, 5, 5);
            gc.gridwidth = 2;
            add(programNameVersionLabel, gc);

            JLabel copyrightLbl = new JLabel("Copyright 2018 Jorge Frisancho");
            gc.gridy++;
            add(copyrightLbl, gc);
            visitProjectSiteLbl = new JLabel("Visit the TwitchDownloader website");
            Font visitProjectSiteFont = visitProjectSiteLbl.getFont();
            Map visitProjectSiteFontAttributes = visitProjectSiteFont.getAttributes();
            visitProjectSiteFontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            visitProjectSiteLbl.setForeground(Color.BLUE);
            visitProjectSiteLbl.setFont(visitProjectSiteFont.deriveFont(visitProjectSiteFontAttributes));
            visitProjectSiteLbl.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    super.mouseClicked(e);
                    try {
                        controller.openUrlInBrowser(new URL("https://github.com/teocci/TwitchLeecher"));
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            gc.gridy++;
            add(visitProjectSiteLbl, gc);
            licenseBtn = new JButton("License");
            licenseBtn.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    super.mouseClicked(e);
                    try {
                        controller.openUrlInBrowser(new URL("https://raw.githubusercontent.com/teocci/TwitchLeecher/master/LICENSE"));
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            gc.gridy++;
            gc.gridwidth = 1;
            add(licenseBtn, gc);
            closeBtn = new JButton("Close");
            closeBtn.addActionListener(this);
            gc.gridx++;
            gc.anchor = GridBagConstraints.LINE_END;
            add(closeBtn, gc);

            pack();
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == closeBtn) {
                this.setVisible(false);
            } else if (e.getSource() == licenseBtn) {
                this.setVisible(false);
            }
        }
    }

//    private class licenseDialog extends JDialog { //STUB not used default browser is used instead.
//        public licenseDialog(Frame owner) {
//            super(owner, "License");
//            GridBagLayout layout = new GridBagLayout();
//            setLayout(layout);
//            GridBagConstraints gc = new GridBagConstraints();
//
//
//        }
//    }

    private File showDestinationDirChooser(String path)
    {
        JFileChooser fileChooser = null;
        File file = null;
        fileChooser = new JFileChooser(path);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser != null) {
            int returnVal = fileChooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
            }
        }
        return file;
    }


    public ChannelSyncMenuBar(ChannelSyncController controller, JFrame mainFrame)
    {
        this.controller = controller;
        this.prefs = TwitchLeecherPreferences.getInstance();
        this.mainFrame = mainFrame;

        mainFrame.setJMenuBar(this);

        settingsMenu = new JMenu("Edit");
        this.settingsFolderDialog = new SettingsFolderDialog(mainFrame);
        destinationFolderMenuItem = new JMenuItem("Settings");
        destinationFolderMenuItem.addActionListener(this);
        settingsMenu.add(destinationFolderMenuItem);
        add(settingsMenu);

        viewMenu = new JMenu("View");
        showProgressWindowsMenuItem = new JMenuItem("Log Window");
        showProgressWindowsMenuItem.addActionListener(this);
        viewMenu.add(showProgressWindowsMenuItem);
        add(viewMenu);

        helpMenu = new JMenu("Help");
        showAboutDialogMenuItem = new JMenuItem("About");
        showAboutDialogMenuItem.addActionListener(this);
        helpMenu.add(showAboutDialogMenuItem);
        this.aboutThisProgramDialog = new AboutThisProgramDialog(mainFrame);
        add(helpMenu);
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == destinationFolderMenuItem) {
            settingsFolderDialog.setVisible(true);
//            } else if (e.getSource() == qualityPriorityMenuItem) {
//                videoQualityDialog.setVisible(true);
        } else if (e.getSource() == showProgressWindowsMenuItem) {
            controller.progressFrameSetVisible(true);
        } else if (e.getSource() == showAboutDialogMenuItem) {
            aboutThisProgramDialog.setVisible(true);
        }
    }
}
