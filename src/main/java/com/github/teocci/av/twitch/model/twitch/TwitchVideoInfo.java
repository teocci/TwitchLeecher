package com.github.teocci.av.twitch.model.twitch;

import com.github.teocci.av.twitch.enums.State;
import com.github.teocci.av.twitch.utils.Network;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import javax.imageio.ImageIO;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Diese Klasse representiert Ein Video Objekt von der Twitch api
 * https://api.twitch.tv/teocci/videos/a582145870
 * <p>
 * Sie enthaelt Informationen zum PastBroadcast allerdings keine Informationen zu den VideoFiles auf den Twitch Servern
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchVideoInfo extends Observable
{
    public static final String API_URL = "https://api.twitch.tv";

    public static final String BASIC_TS_FILE = "^(\\d+\\.ts)";
    public static final String RANGE_TS_FILE = "^(\\d+\\.ts)\\?start_offset=(\\d+)&end_offset=(\\d+)$";


    @SerializedName("title")
    private String title;
    private String description;
    @SerializedName("broadcast_id")
    private String broadcastId;
    @SerializedName("tag_list")
    private String tagList;
    @SerializedName("_id")
    private String id;
    @SerializedName("recorded_at")
    private String recordedAt;
    private String game;
    private double length; //Twitch changed that to double.  
    private HashMap<String, String> preview;
    private String url;
    private int views;

    @SerializedName("_links")
    private HashMap<String, String> links;

    @SerializedName("channel")
    private TwitchChannel channel;

    private Image image;

    private TwitchDownloadInfo dlInfo;
    private boolean dlInfoNeedsUpdate = false;
    private State state;
    private HashMap<String, File> relatedFiles;

    private int startOffset;
    private int endOffset;


    private URL playlistUrl;

    protected PropertyChangeSupport pcs;


    public TwitchVideoInfo()
    {
        this.pcs = new PropertyChangeSupport(this);
        dlInfoNeedsUpdate = false;
        this.state = State.INITIAL;
        this.relatedFiles = new HashMap<>();
    }

    public static TwitchVideoInfo getTwitchVideoInfo(String id) throws IOException
    {
        URL infoApiUrl = new URL(API_URL + "/kraken/videos/" + id);
        InputStream is = infoApiUrl.openStream();
        InputStreamReader ir = new InputStreamReader(is);
        TwitchVideoInfo tvi = new Gson().fromJson(ir, TwitchVideoInfo.class);
        ir.close();
        is.close();
        return tvi;
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        State oldState = this.state;
        this.state = state;
        pcs.firePropertyChange("state", oldState, this.state);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TwitchVideoInfo that = (TwitchVideoInfo) o;

        if (length != that.length) return false;
        if (views != that.views) return false;
        if (broadcastId != null ? !broadcastId.equals(that.broadcastId) : that.broadcastId != null) return false;
        if (channel != null ? !channel.equals(that.channel) : that.channel != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (game != null ? !game.equals(that.game) : that.game != null) return false;
        if (!id.equals(that.id)) return false;
        if (links != null ? !links.equals(that.links) : that.links != null) return false;
        if (preview != null ? !preview.equals(that.preview) : that.preview != null) return false;
        if (recordedAt != null ? !recordedAt.equals(that.recordedAt) : that.recordedAt != null) return false;
        if (!title.equals(that.title)) return false;
        return url != null ? url.equals(that.url) : that.url == null;

    }

    @Override
    public int hashCode()
    {
        int result = title.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (broadcastId != null ? broadcastId.hashCode() : 0);
        result = 31 * result + id.hashCode();
        result = 31 * result + (recordedAt != null ? recordedAt.hashCode() : 0);
        result = 31 * result + (game != null ? game.hashCode() : 0);
        result = 31 * result + (preview != null ? preview.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + views;
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        return result;
    }


    @Override
    public String toString()
    {
        return "TwitchVideoInfo{" +
                "title='" + title + '\'' +
                '}';
    }

    public boolean relatedFileExists()
    {
        if (relatedFiles.isEmpty()) {
            return false;
        } else {
            if (relatedFiles.containsKey("default")) {
                return relatedFiles.get("default").exists();
            } else return false;
        }
    }

    public File getMainRelatedFileOnDisk()
    {
        if (relatedFiles.containsKey("default")) {
            return relatedFiles.get("default");
        } else {
            return null;
        }
    }

    public void setMainRelatedFileOnDisk(File relatedFileOnDisk)
    {
        File oldRelatedFile;
        if (relatedFiles.containsKey("default")) oldRelatedFile = relatedFiles.get("default");
        else oldRelatedFile = null;
        relatedFiles.put("default", relatedFileOnDisk);
        pcs.firePropertyChange("relatedFile", oldRelatedFile, relatedFiles.get("default"));
    }

    public ArrayList<File> getRelatedFiles()
    {
        return new ArrayList<>(relatedFiles.values());
    }

    public void putRelatedFile(String key, File value)
    {
        relatedFiles.put(key, value);
    }

    public void removeRelatedFile(String key)
    {
        relatedFiles.remove(key);
    }

    public void deleteAllRelatedFiles()
    {
        for (File file : relatedFiles.values()) {
            if (file.exists() && file.canWrite()) {
                file.delete();
            }
        }
        relatedFiles.clear();
    }

    public void addPropertyChangeListenern(PropertyChangeListener listener)
    {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removeProbertyChangeListenern(PropertyChangeListener listener)
    {
        this.pcs.removePropertyChangeListener(listener);
    }


    public void update(URL twitchUrl) throws IOException
    {
        if (Pattern.matches("http://www.twitch.tv/\\w+/b/\\d+", twitchUrl.toString())) {
            String id = twitchUrl.toString().split("/")[5];
            update("a".concat(id));
        } else if (Pattern.matches("http://www.twitch.tv/\\w+/c/\\d+", twitchUrl.toString())) {
            String id = twitchUrl.toString().split("/")[5];
            update("c".concat(id));
        } else if (Pattern.matches("http://www.twitch.tv/\\w+/v/\\d+", twitchUrl.toString())) {
            String id = twitchUrl.toString().split("/")[5];
            update("v".concat(id));
        }
    }

    public void update(String id) throws IOException
    {
        update(getTwitchVideoInfo(id));
    }

    public void update(TwitchVideoInfo tvi)
    {
        if (this.channel == null) this.channel = new TwitchChannel();
        if (this.links == null) this.links = new HashMap<>();
        setTitle(tvi.title);
        setDescription(tvi.description);
        setBroadcastId(tvi.broadcastId);
        setTagList(tvi.tagList);
        setId(tvi.id);
        setRecordedAt(tvi.recordedAt);
        setGame(tvi.game);
        setLength(tvi.getLength());
        setPreview(tvi.preview);
        setUrl(tvi.url);
        setViews(tvi.views);
        setChannelLink(tvi.getChannelLink());
        setSelfLink(tvi.getSelfLink());
        setChannelName(tvi.getChannelName());
        setChannelDisplayname(tvi.getChannelDisplaylName());
        setImage(tvi.image);
        dlInfoNeedsUpdate = true;
        pcs.firePropertyChange("fullUpdate", null, this);
    }


    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public String getBroadcastId()
    {
        return broadcastId;
    }

    public String getTagList()
    {
        return tagList;
    }

    public String getId()
    {
        return id;
    }

    public Calendar getRecordedAt()
    {
        String date = recordedAt.split("T")[0];
        String time = recordedAt.split("T")[1];
        int year = new Integer(date.split("-")[0]);
        int month = new Integer(date.split("-")[1]);
        int day = new Integer(date.split("-")[2]);
        int hourOfDay = new Integer(time.split(":")[0]);
        int minute = new Integer(time.split(":")[1]);
        int secound = new Integer(time.split(":")[2].substring(0, 2));

        Calendar recordedAtCalendar = GregorianCalendar.getInstance();
        recordedAtCalendar.set(year, month - 1, day, hourOfDay, minute, secound);
        return recordedAtCalendar;
    }

    public String getGame()
    {
        return game;
    }

    public int getLength()
    {
        return (int) length;
    }

    public URL getPreviewUrl(String key) throws MalformedURLException
    {
        return new URL(preview.get(key));
    }

    public URL getUrl() throws MalformedURLException
    {
        return new URL(url);
    }

    public int getViews()
    {
        return views;
    }

    public String getSelfLink()
    {
        return links.get("self");
    }


    public String getChannelLink()
    {
        return links.get("channel");
    }

    public String getChannelDisplaylName()
    {
        return channel.getDisplayName();
    }

    public String getChannelName()
    {
        return channel.getName();
    }

    public TwitchDownloadInfo getDownloadInfo() throws IOException
    {
        if (id.matches("^v\\d+")) { //VODs with that kind of Id are stored in the new TwitchVodSystem. The old API requests is empty
            updateDLinfoNewVodSystem();
        } else {
            updateDlInfoOldVodSystem();
        }
        return dlInfo;
    }

    private void updateDlInfoOldVodSystem() throws IOException
    {
        if (this.dlInfo == null || this.dlInfoNeedsUpdate) {
            if (this.dlInfo == null) this.dlInfo = new TwitchDownloadInfo();
            dlInfo.update(this.id);
            dlInfoNeedsUpdate = false;
        }
    }


    /**
     * The new VOD System uses m3u PLaylist instead. The REST-API returns empty lists.
     * <p>
     * This Method fetches the relevant Information and stores them in the same way as the old-VOD-System
     *
     * @throws IOException
     */
    private void updateDLinfoNewVodSystem() throws IOException
    {
        if (this.dlInfo == null || this.dlInfoNeedsUpdate) {
            if (this.dlInfo == null) this.dlInfo = new TwitchDownloadInfo();

            String idNr = this.id.substring(1);
            URL tokenUrl = new URL("https://api.twitch.tv/api/vods/" + idNr + "/access_token");
//            URL tokenUrl = new URL(String.format("http://api.twitch.tv/api/channels/%s/access_token", channel.getName()));

            String data = Network.getJSON(tokenUrl);
//            JsonObject p = new Gson().fromJson(data, JsonObject.class);
//            System.out.println(p);

            TwitchVodAccessToken vodAccessToken = new Gson().fromJson(data, TwitchVodAccessToken.class);
            System.out.println(vodAccessToken);

            // URLEncoder is used to encode the Token(JSON) to a valid URL
//            URL qualityPlaylistUrl = new URL("http://usher.twitch.tv/vod/" + idNr + "?nauth=" + URLEncoder.encode(vodAccessToken.getToken(), "UTF-8") + "&nauthsig=" + vodAccessToken.getSig()); Twitch changed something source download doesn work with that request
            URL qualityPlaylistUrl = new URL("http://usher.twitch.tv/vod/" + vodAccessToken.getVodId() +
                    "?player=twitchweb&allow_source=true&nauth=" + URLEncoder.encode(vodAccessToken.getToken(), "UTF-8") +
                    "&nauthsig=" + vodAccessToken.getSig()
            );

            InputStream qualityPlaylistIs = qualityPlaylistUrl.openStream();
            Scanner qualityPlaylistSc = new Scanner(qualityPlaylistIs);
            while (qualityPlaylistSc.hasNextLine()) {
                String line = qualityPlaylistSc.nextLine();
//                System.out.println(line);
                if (!Pattern.matches("^#.*$", line)) { //filter Out comment lines
                    System.out.println(line);
                    String quality = line.split("/")[4];
                    playlistUrl = new URL(line);
                    
                    String m3uFilename = new File(playlistUrl.getFile()).getName();

                    InputStream playlistIs = playlistUrl.openStream();
                    Scanner playlistSc = new Scanner(playlistIs);
                    Pattern partFileNameStringPattern = Pattern.compile(BASIC_TS_FILE);

                    while (playlistSc.hasNextLine()) {
                        String partLine = playlistSc.nextLine();
//                        System.out.println(partLine);
                        if (partLine.isEmpty())
                            continue;
                        Matcher m = partFileNameStringPattern.matcher(partLine);
                        if (m.matches()) {
                            String partURL = String.format("%s%s", line.replace(m3uFilename, ""), m.group(1));
                            System.out.println(partURL);
                            TwitchVideoPart tbp = new TwitchVideoPart(partURL, -1, null, null);
                            dlInfo.addTwitchBroadcastPart(tbp, quality);
                        }
                    }
                }
            }
            dlInfoNeedsUpdate = false;
        }
    }

    public void setTitle(String title)
    {
        String oldTitle = this.title;
        this.title = title;
        pcs.firePropertyChange("title", oldTitle, this.title);
    }

    public void setDescription(String description)
    {
        String oldDescription = this.description;
        this.description = description;
        pcs.firePropertyChange("description", oldDescription, this.description);
    }

    public void setBroadcastId(String broadcastId)
    {
        String oldBroadcastId = this.broadcastId;
        this.broadcastId = broadcastId;
        pcs.firePropertyChange("broadcastId", oldBroadcastId, this.broadcastId);
    }

    public void setTagList(String tagList)
    {
        String oldTaglist = this.tagList;
        this.tagList = tagList;
        pcs.firePropertyChange("tagList", oldTaglist, this.tagList);
    }

    public void setId(String id)
    {
        String oldId = this.id;
        this.id = id;
        pcs.firePropertyChange("id", oldId, this.id);
    }

    public void setRecordedAt(String recordedAt)
    {
        String oldRecordedAt = this.recordedAt;
        this.recordedAt = recordedAt;
        pcs.firePropertyChange("recordedAt", oldRecordedAt, recordedAt);
    }

    public void setGame(String game)
    {
        String oldGame = this.game;
        this.game = game;
        pcs.firePropertyChange("game", oldGame, this.game);
    }

    public void setLength(int length)
    {
        double oldLength = this.length;
        this.length = length;
        pcs.firePropertyChange("length", oldLength, length);
    }

    public void setPreview(HashMap<String, String> preview)
    {
        HashMap<String, String> oldPreview = this.preview;
        this.preview = preview;
        pcs.firePropertyChange("preview", oldPreview, preview);
    }

    public void setUrl(String url)
    {
        String oldUrl = this.url;
        this.url = url;
        pcs.firePropertyChange("url", oldUrl, this.url);
    }

    public void setChannelName(String channelName)
    {
        String oldChannelName = this.channel.getName();
        this.channel.setName(channelName);
        pcs.firePropertyChange("channelName", oldChannelName, this.channel.getName());
    }

    public void setChannelDisplayname(String channelDisplayname)
    {
        String oldChannelDisplayname = this.channel.getDisplayName();
        this.channel.setDisplayName(channelDisplayname);
        pcs.firePropertyChange("channelDisplayname", oldChannelDisplayname, channelDisplayname);
    }

    public void setViews(int views)
    {
        int oldViews = this.views;
        this.views = views;
        pcs.firePropertyChange("views", oldViews, this.views);
    }

    public void setSelfLink(String selfLink)
    {
        String oldSelfLink = this.links.get("self");
        this.links.put("self", selfLink);
        pcs.firePropertyChange("selfLink", oldSelfLink, this.links.get("self"));
    }

    public void setChannelLink(String channelLink)
    {
        String oldChannelLink = this.links.get("channel");
        this.links.put("channel", channelLink);
        pcs.firePropertyChange("channelLink", oldChannelLink, this.links.get("channel"));
    }

    public void setImage(Image image)
    {
        Image oldImage = this.image;
        this.image = image;
        pcs.firePropertyChange("image", oldImage, this.image);
    }

    public Image loadPreviewImage() throws IOException
    {
        if (image == null) {
            InputStream is = getPreviewUrl("medium").openStream();
            Image image = ImageIO.read(is);
            this.image = image;
            pcs.firePropertyChange("previewImage", null, image);
            return image;
        } else {
            return this.image;
        }
    }

    public Image getPreviewImage()
    {
        return image;
    }

    public HashMap<String, String> getVideoInformation() throws IOException
    {
        HashMap<String, String> videoInformation = new HashMap<>();

        videoInformation.put("PreviewURL", getPreviewUrl("medium").toString());
        videoInformation.put("URL", getUrl().toString());
        videoInformation.put("ChannelName", getChannelName());
        videoInformation.put("ChannelDisplayName", getChannelDisplaylName());
        videoInformation.put("BroadcastId", getBroadcastId());
        videoInformation.put("TagList", getTagList());
        videoInformation.put("Id", getId());
        videoInformation.put("recordedAt", Integer.toString(getRecordedAt().get(Calendar.YEAR)));
        videoInformation.put("Game", getGame());
        videoInformation.put("BestAvailableQuality", getDownloadInfo().getBestAvailableQuality());
        videoInformation.put("Title", getTitle());
        videoInformation.put("Description", getDescription());
        videoInformation.put("Length", Integer.toString(getLength()));

        return videoInformation;
    }


    public LinkedHashMap<String, String> getStreamInformation() throws IOException
    {
        //LinkedHashMap<String, String> streamInfo = super.getStreamInformation();
        LinkedHashMap<String, String> streamInformation = new LinkedHashMap<>();
        streamInformation.put("Title", getTitle());
        streamInformation.put("Description", getDescription());
        streamInformation.put("Channel", getChannelName());
        streamInformation.put("ChannelDisplayName", getChannelDisplaylName());
        //streamInformation.put("RecordedAt", getRecordedAt().getDisplayName(Calendar.YEAR, Calendar.SHORT, Locale.ENGLISH));
        streamInformation.put("Game", getGame());
        streamInformation.put("BestQuality", getDownloadInfo().getBestAvailableQuality());
        streamInformation.put("broadcastId", getBroadcastId());
        streamInformation.put("tagList", getTagList());
        streamInformation.put("id", getId());
        streamInformation.put("previewImageURL", getPreviewUrl("medium").toString());
        streamInformation.put("url", getUrl().toString());
        streamInformation.put("views", String.valueOf(views));
        streamInformation.put("channelLink", getChannelLink());
        streamInformation.put("selfLink", getChannelLink());

        if (getRecordedAt() != null) {
            String timestamp = String.format("%tF_%tT", getRecordedAt(), getRecordedAt());
            streamInformation.put("recorded-at", timestamp);
            streamInformation.put("date", String.format("%tF", getRecordedAt()));
            streamInformation.put("time", String.format("%tT", getRecordedAt()));
        }

        return streamInformation;
    }

    public int getStartOffset()
    {
        return startOffset;
    }

    public void setStartOffset(int startOffset)
    {
        this.startOffset = startOffset;
    }

    public int getEndOffset()
    {
        return endOffset;
    }

    public URL getPlaylistUrl()
    {
        return playlistUrl;
    }

    public void setPlaylistUrl(URL playlistUrl)
    {
        this.playlistUrl = playlistUrl;
    }

    public void setEndOffset(int endOffset)
    {
        this.endOffset = endOffset;
    }

    public TwitchChannel getChannel()
    {
        return channel;
    }

    public void setChannel(TwitchChannel channel)
    {
        this.channel = channel;
    }
}
