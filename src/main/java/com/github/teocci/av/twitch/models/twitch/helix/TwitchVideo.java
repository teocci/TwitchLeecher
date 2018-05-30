package com.github.teocci.av.twitch.models.twitch.helix;

import com.github.teocci.av.twitch.enums.State;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import javax.imageio.ImageIO;
import java.awt.*;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * https://dev.twitch.tv/docs/api/reference/#get-videos
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchVideo
{
    // API URL used by this class
    public static final String API_URL = "https://api.twitch.tv/helix/videos";

    // Date when the video was created.
    @SerializedName("created_at")
    private String createdAt;

    // Description of the video.
    @SerializedName("description")
    private String description;

    // Length of the video.
    @SerializedName("duration")
    private String duration;

    // User’s ID.
    @SerializedName("id")
    private String id;

    // Language of the video.
    @SerializedName("language")
    private String language;

    // A cursor value, to be used in a subsequent request to specify the starting point of the next set of results.
    @SerializedName("pagination")
    private String pagination;

    // Date when the video was published.
    @SerializedName("published_at")
    private String publishedAt;

    // Template URL for the thumbnail of the video.
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    // Title of the video.
    @SerializedName("title")
    private String title;

    // Type of video. Valid values: "upload", "archive", "highlight".
    @SerializedName("type")
    private String type;

    // URL of the video.
    @SerializedName("url")
    private String url;

    // ID of the user who owns the video.
    @SerializedName("user_id")
    private String userId;

    // Number of times the video has been viewed.
    @SerializedName("view_count")
    private int viewCount;

    // Indicates whether the video is publicly viewable. Valid values: "public", "private".
    @SerializedName("viewable")
    private String viewable;

    private Image thumbnail;
    private State state;

    protected PropertyChangeSupport pcs;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TwitchVideo video = (TwitchVideo) o;

        if (!id.equals(video.id)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public String toString()
    {
        StringBuilder output = new StringBuilder();
        output.append(id).append('\n');
        output.append(title).append('\n');
        output.append(viewCount).append('\n');
        output.append(url).append('\n');
        return output.toString();
    }

    public static TwitchVideo getVideoById(String id) throws IOException
    {
        URL channelApiUrl = new URL(API_URL + "?id=" + id);
        InputStream is = channelApiUrl.openStream();
        InputStreamReader ir = new InputStreamReader(is);
        TwitchVideo channel = new Gson().fromJson(ir, TwitchVideo.class);
        ir.close();
        is.close();
        return channel;
    }

    public void update(String id) throws IOException
    {
        update(getVideoById(id));
    }

    public void update(TwitchVideo video)
    {
        this.id = video.id;
        this.title = video.title;
        this.viewCount = video.viewCount;
        this.url = video.url;
    }

    public Image loadThumbnail() throws IOException
    {
        if (thumbnail == null) {
            InputStream is = getThumbnailUrl().openStream();
            Image image = ImageIO.read(is);
            this.thumbnail = image;
            pcs.firePropertyChange("thumbnail", null, image);
            return image;
        } else {
            return this.thumbnail;
        }
    }

    public Calendar toCalendar(String value)
    {
        String[] date = value.split("T")[0].split("-");
        String[] time = value.split("T")[1].split("-");
        int year = new Integer(date[0]);
        int month = new Integer(date[1]);
        int day = new Integer(date[2]);
        int hours = new Integer(time[0]);
        int minutes = new Integer(time[1]);
        int seconds = new Integer(time[2].substring(0, 2));

        Calendar recordedAtCalendar = GregorianCalendar.getInstance();
        recordedAtCalendar.set(year, month - 1, day, hours, minutes, seconds);
        return recordedAtCalendar;
    }


    /**
     * Reloads the channel Information. This can be used to get additional information of a channel
     * Some other APIs from Twitch don't deliver all fields in a channel object (for example TwitchVideoInfo).
     * This Method reloads the channel using the channels API
     *
     * @throws IOException
     */
    public void reload() throws IOException
    {
        update(getVideoById(getId()));
    }

    // Getters and Setters

    public State getState() {
        return state;
    }

    public void setState(State state) {
        State oldState = this.state;
        this.state = state;
        pcs.firePropertyChange("state", oldState, this.state);
    }

    public Calendar getCreatedAt()
    {
        return toCalendar(createdAt);
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDuration()
    {
        return duration;
    }

    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getPagination()
    {
        return pagination;
    }

    public void setPagination(String pagination)
    {
        this.pagination = pagination;
    }

    public Calendar getPublishedAt()
    {
        return toCalendar(publishedAt);
    }

    public void setPublishedAt(String publishedAt)
    {
        this.publishedAt = publishedAt;
    }

    public URL getThumbnailUrl() throws MalformedURLException
    {
        return new URL(thumbnailUrl);
    }

    public void setThumbnailUrl(String thumbnailUrl)
    {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public URL getUrl() throws MalformedURLException
    {
        return new URL(url);
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public int getViewCount()
    {
        return viewCount;
    }

    public void setViewCount(int viewCount)
    {
        this.viewCount = viewCount;
    }

    public String getViewable()
    {
        return viewable;
    }

    public void setViewable(String viewable)
    {
        this.viewable = viewable;
    }


    public Image getPreviewImage()
    {
        return thumbnail;
    }
}
