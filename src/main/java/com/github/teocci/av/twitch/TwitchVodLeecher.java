package com.github.teocci.av.twitch;

import com.github.teocci.av.twitch.controllers.ChannelSyncController;

import javax.swing.*;
import java.net.URISyntaxException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchVodLeecher
{
    public static void main(String[] args) throws URISyntaxException
    {
        // Set look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(ChannelSyncController::new);
    }
}
