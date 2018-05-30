package com.github.teocci.av.twitch.interfaces;

import java.io.IOException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-30
 */
public interface ChannelSearchCallback
{
    void onResult();

    void onError(IOException e);
}
