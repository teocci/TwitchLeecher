package com.github.teocci.av.twitch.gui.vod.channelsync;

import javax.swing.*;
import java.awt.*;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class ChannelSyncLogFrame extends JFrame
{

    private final JTextArea consoleOutputTextArea;
    private final JScrollPane consoleOutputScrollPane;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            ChannelSyncLogFrame channelSyncLogFrame = new ChannelSyncLogFrame();
            channelSyncLogFrame.setVisible(true);
        });
    }


    public ChannelSyncLogFrame() throws HeadlessException
    {
        super("Log ");
        setSize(750, 550);

        JPanel mainPanel = (JPanel) getContentPane();
        mainPanel.setLayout(new GridBagLayout());

        consoleOutputTextArea = new JTextArea();
        consoleOutputScrollPane = new JScrollPane(consoleOutputTextArea);

        configureComponents();
        layoutComponents();
    }


    private void configureComponents()
    {
        consoleOutputTextArea.setEditable(false);
        consoleOutputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    public void addOutputText(String text)
    {
        consoleOutputTextArea.append(text);
    }


    private void layoutComponents()
    {
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;

        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;

        c.fill = GridBagConstraints.BOTH;

        add(consoleOutputScrollPane, c);
    }
}
