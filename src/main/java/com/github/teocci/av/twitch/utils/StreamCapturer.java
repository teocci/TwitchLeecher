package com.github.teocci.av.twitch.utils;

import com.github.teocci.av.twitch.interfaces.Consumer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class StreamCapturer extends OutputStream
{
    private StringBuilder buffer;
    private String prefix;
    private Consumer consumer;
    private PrintStream old;

    public StreamCapturer(String prefix, Consumer consumer, PrintStream old)
    {
        this.prefix = prefix;
        buffer = new StringBuilder(128);
        buffer.append("[").append(prefix).append("] ");
        this.old = old;
        this.consumer = consumer;
    }

    @Override
    public void write(int b) throws IOException
    {
        char c = (char) b;
        String value = Character.toString(c);
        buffer.append(value);
        if (value.equals("\n")) {
            consumer.appendText(buffer.toString());
            buffer.delete(0, buffer.length());
            buffer.append("[").append(prefix).append("] ");
        }
        old.print(c);
    }
}
