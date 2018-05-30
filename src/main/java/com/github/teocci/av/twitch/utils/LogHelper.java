package com.github.teocci.av.twitch.utils;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

import static com.github.teocci.av.twitch.utils.Config.LOG_PREFIX;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Nov-14
 */
public class LogHelper
{
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 30;
    private static final int RESERVED_LENGTH = MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 2;

    private static final PrintStream Log = System.out;

    public static String makeLogTag(String str)
    {
        return LOG_PREFIX
                + '['
                + (str.length() > RESERVED_LENGTH ? str.substring(0, RESERVED_LENGTH - 1) : str)
                + ']';
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls)
    {
        return makeLogTag(cls.getSimpleName());
    }

    public static void v(String tag, Object... messages)
    {
        // Only log VERBOSE if build type is DEBUG
        log(tag, null, messages);
    }

    public static void d(String tag, Object... messages)
    {
        // Only log DEBUG if build type is DEBUG
        log(tag, null, messages);
    }

    public static void i(String tag, Object... messages)
    {
        log(tag, null, messages);
    }

    public static void w(String tag, Object... messages)
    {
        log(tag, null, messages);
    }

    public static void w(String tag, Throwable t, Object... messages)
    {
        log(tag, t, messages);
    }

    public static void e(String tag, Object... messages)
    {
        log(tag, null, messages);
    }

    public static void e(String tag, Throwable t, Object... messages)
    {
        log(tag, t, messages);
    }

    public static void log(String tag, Throwable t, Object... messages)
    {
        String message;
        if (t == null && messages != null && messages.length == 1) {
            // Handle this common case without the extra cost of creating a StringBuffer:
            message = messages[0].toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (messages != null) for (Object m : messages) {
                sb.append(m);
            }
            if (t != null) {
                sb.append("\n").append(LogHelper.getStackTraceString(t));
            }
            message = sb.toString();
        }

        Log.println(tag + ": " + message);
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr)
    {
        if (tr == null) {
            return "";
        }
        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, false);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}