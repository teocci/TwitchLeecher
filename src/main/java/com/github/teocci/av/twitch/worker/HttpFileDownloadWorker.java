package com.github.teocci.av.twitch.worker;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class HttpFileDownloadWorker extends SwingWorker<Void, Void>
{
    private URL sourceUrl;
    private File destinationFile;

    public HttpFileDownloadWorker(URL source, File destination)
    {
        this.sourceUrl = source;
        this.destinationFile = destination;
    }

    @Override
    protected Void doInBackground() throws Exception
    {

        URLConnection urlConnection = null;
        InputStream sourceInputStream = null;
        FileOutputStream fos = null;
        String outputLine = "Downloading " + sourceUrl.getPath() + "\n";
        firePropertyChange("outputline", null, outputLine);
        try {
            urlConnection = sourceUrl.openConnection();
            int filesize = urlConnection.getContentLength();
            sourceInputStream = urlConnection.getInputStream();
            fos = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[16384];

            int len;
            long done = 0;
            while ((len = sourceInputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                done += len;
                float percent = (done * 100) / filesize;
                setProgress((int) percent);
                firePropertyChange("outputline", outputLine, outputLine = String.format("%3d%% \n", (int) percent));
            }
            firePropertyChange("outpuline", outputLine, outputLine = "Download finished\n");
            setProgress(100);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sourceInputStream != null) sourceInputStream.close();
            if (fos != null) fos.close();
        }
        return null;
    }

    public URL getSourceUrl()
    {
        return sourceUrl;
    }

    public File getDestinationFile()
    {
        return destinationFile;
    }

    @Override
    protected void done()
    {
        super.done();
    }
}
