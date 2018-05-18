package com.github.teocci.av.twitch.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-08
 */
public class Network
{
    public static String getJSON(String url)
    {
        try {
            return getJSON(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static String getJSON(URL url)
    {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) url.openConnection();
            c.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
            c.setRequestProperty("Client-ID", "jzkbprff40iqj646a697cyrvl0zt2m6");
            c.setRequestMethod("GET");

            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append('\n');
                    }
                    br.close();
                    return sb.toString();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        return null;
    }
}
