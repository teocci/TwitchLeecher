package com.github.teocci.av.twitch.exceptions;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class UnsupportedOsException extends Exception
{
    public UnsupportedOsException()
    {
        super(System.getProperty("os.name") + " isn't supported yet!");
    }

    public UnsupportedOsException(String message)
    {
        super(message);
    }
}
