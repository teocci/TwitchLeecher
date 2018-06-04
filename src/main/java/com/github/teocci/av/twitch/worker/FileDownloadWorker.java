package com.github.teocci.av.twitch.worker;

import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class FileDownloadWorker extends Task<Void>
{
    private URL url;
    private File destinationFile;

    public FileDownloadWorker(URL url, File destination)
    {
        this.url = url;
        this.destinationFile = destination;
    }

    public URL getUrl()
    {
        return url;
    }

    public File getDestinationFile()
    {
        return destinationFile;
    }

    @Override
    protected Void call() throws Exception
    {
        URLConnection openConnection = url.openConnection();
        InputStream bis = null;
        FileOutputStream fos = null;
        updateMessage("Downloading " + url.getPath() + ".");
        try {
            bis = new BufferedInputStream(url.openStream());
            int fileSize = openConnection.getContentLength();

            fos = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[16384];

            int len;
            long downloaded = 0;
            while ((len = bis.read(buffer)) > 0) {
                downloaded += len;
                fos.write(buffer, 0, len);
                updateProgress(downloaded, fileSize);
                updateMessage("Downloaded : " + (downloaded / (1024F * 1024F * 8F)) + " MB");
            }
            updateMessage("Download finished.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) bis.close();
            if (fos != null) fos.close();
        }
        return null;
    }
}
