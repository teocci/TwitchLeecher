package com.github.teocci.av.twitch.model.twitch;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Diese Klasse representiert Ein VideoObjekt von der Twitch api
 * https://api.twitch.tv/api/videos/a582145870
 * <p>
 * Dies ist die API-Schnitsstelle für den Player der so festellt welches VideoFile von wo bis wo abgespielt werden soll.
 * <p>
 * Highlights stellen nur ausschnitte von BastBroadcasts dar. Das Heisst der startOffset und end Offset (angabe in Sekunden)
 * liegen nicht bei 0 und dem Ende des Broadcasts. Der Twitch PLayer z.B. berechnet sich vermutlich welches File er laden
 * und wohin er in diesem Springen muss.
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class TwitchDownloadInfo
{
    @SerializedName("api_id")
    private String apiId;

    @SerializedName("start_offset")
    private int startOffset;

    @SerializedName("end_offset")
    private int endOffset;

    @SerializedName("play_offset")
    private int playOffset;

    @SerializedName("increment_view_count_url")
    private String incrementViewCountUrl;

    @SerializedName("path")
    private String path;

    @SerializedName("duration")
    private int duration;

    @SerializedName("broadcaster_software")
    private String broadcasterSoftware;

    @SerializedName("channel")
    private String channel;

    @SerializedName("chunks")
    private TwitchChunks chunks;

    @SerializedName("restrictions")
    private TwitchRestrictions restrictions;

    @SerializedName("preview_small")
    private String previewSmall;

    @SerializedName("preview")
    private String preview;

    @SerializedName("vod_ad_frequency")
    private String vodAdFrequency;

    @SerializedName("vod_ad_length")
    private String vodAdLength;

    @SerializedName("redirect_api_id")
    private String redirectApiId;

    @SerializedName("muted_segments")
    private String mutedSegments;

    private final PropertyChangeSupport pcs;
    private List<Observer> observers;

    public void addObserver(Observer observer)
    {
        this.observers.add(observer);
    }

    public void addTwitchBroadcastPart(TwitchVideoPart tbp, String quality)
    {

        if (chunks == null) chunks = new TwitchChunks();
        switch (quality) {
            case "chunked":
                chunks.addSourcePart(tbp);
                break;
            case "high":
                chunks.addHighPart(tbp);
                break;
            case "medium":
                chunks.addMediumPart(tbp);
                break;
            case "low":
                chunks.addLowPart(tbp);
                break;
            case "mobile":
                chunks.addMobilePart(tbp);
                break;
            default:
                break;
        }
    }

    public List<TwitchVideoPart> getTwitchBroadcastParts(String quality)
    {
        if (quality.equals("source")) return chunks.getSource();
        else if (quality.equals("high")) return chunks.getHigh();
        else if (quality.equals("medium")) return chunks.getMid();
        else if (quality.equals("low")) return chunks.getLow();
        else if (quality.equals("mobile")) return chunks.getMobile();
        else return null;
    }


    public TwitchDownloadInfo()
    {
//        System.out.println("DownloadInfoContructor");
        this.pcs = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.pcs.removePropertyChangeListener(listener);
    }

    public List<String> getAvailableQualities()
    {
        List<String> availableQualities = new ArrayList<>();
        if (chunks.getSource() != null && !chunks.getSource().isEmpty())
            availableQualities.add("source");
        if (chunks.getHigh() != null && !chunks.getHigh().isEmpty())
            availableQualities.add("high");
        if (chunks.getMid() != null && !chunks.getMid().isEmpty())
            availableQualities.add("medium");
        if (chunks.getLow() != null && !chunks.getLow().isEmpty())
            availableQualities.add("low");
        if (chunks.getMobile() != null && !chunks.getMobile().isEmpty())
            availableQualities.add("mobile");
        return availableQualities;
    }

    public String getApiId()
    {
        return apiId;
    }

    public int getStartOffset()
    {
        return startOffset;
    }

    public int getEndOffset()
    {
        return endOffset;
    }

    public int getPlayOffset()
    {
        return playOffset;
    }

    public String getIncrementViewCountUrl()
    {
        return incrementViewCountUrl;
    }

    public String getPath()
    {
        return path;
    }

    public int getDuration()
    {
        return duration;
    }

    public String getBroadcasterSoftware()
    {
        return broadcasterSoftware;
    }

    public String getChannel()
    {
        return channel;
    }

    public Map<String, List<TwitchVideoPart>> getAllParts()
    {
        Map<String, List<TwitchVideoPart>> allParts = new HashMap<>();
        if (chunks.getSource() != null)
            allParts.put("source", chunks.getSource());
        if (chunks.getHigh() != null)
            allParts.put("high", chunks.getHigh());
        if (chunks.getMid() != null)
            allParts.put("mid", chunks.getMid());
        if (chunks.getLow() != null)
            allParts.put("low", chunks.getLow());
        if (chunks.getMobile() != null)
            allParts.put("mobile", chunks.getMobile());

        return allParts;
    }

    public String getBestAvailableQuality()
    {
        ArrayList<String> preferredQualitiesDescendingOrder = new ArrayList<>();
        preferredQualitiesDescendingOrder.add("source");
        preferredQualitiesDescendingOrder.add("high");
        preferredQualitiesDescendingOrder.add("medium");
        preferredQualitiesDescendingOrder.add("low");
        preferredQualitiesDescendingOrder.add("mobile");
        return getPreferredQuality(preferredQualitiesDescendingOrder);
    }

    public String getPreferredQuality(List<String> preferredQualitiesDescendingOrder)
    {
        for (String quality : preferredQualitiesDescendingOrder) {
            if (getAvailableQualities().contains(quality))
                return quality;
        }
        return null;
    }

    public String getPreviewSmall()
    {
        return previewSmall;
    }

    public String getPreview()
    {
        return preview;
    }

    public String getVodAdFrequency()
    {
        return vodAdFrequency;
    }

    public String getVodAdLength()
    {
        return vodAdLength;
    }

    public String getRedirectApiId()
    {
        return redirectApiId;
    }

    public String getMutedSegments()
    {
        return mutedSegments;
    }

    public TwitchRestrictions getRestrictions()
    {
        return restrictions;
    }

    public void setApiId(String apiId)
    {
        String oldApId = this.apiId;
        this.apiId = apiId;
        this.pcs.firePropertyChange("apiId", oldApId, this.apiId);
    }

    public void setStartOffset(int startOffset)
    {
        int oldStartOffset = this.startOffset;
        this.startOffset = startOffset;
        this.pcs.firePropertyChange("startOffset", oldStartOffset, this.startOffset);
    }

    public void setEndOffset(int endOffset)
    {
        int oldEndOffset = this.endOffset;
        this.endOffset = endOffset;
        this.pcs.firePropertyChange("endOffset", oldEndOffset, this.endOffset);
    }

    public void setPlayOffset(int playOffset)
    {
        int oldPlayOffset = this.playOffset;
        this.playOffset = playOffset;
        this.pcs.firePropertyChange("playOffset", oldPlayOffset, this.playOffset);
    }

    public void setIncrementViewCountUrl(String incrementViewCountUrl)
    {
        String oldIncrementViewCountUrl = this.incrementViewCountUrl;
        this.incrementViewCountUrl = incrementViewCountUrl;
        this.pcs.firePropertyChange("incrementViewCountUrl", oldIncrementViewCountUrl, this.incrementViewCountUrl);
    }

    public void setPath(String path)
    {
        String oldPath = this.path;
        this.path = path;
        this.pcs.firePropertyChange("path", oldPath, this.path);
    }

    public void setDuration(int duration)
    {
        int oldDuration = this.duration;
        this.duration = duration;
        this.pcs.firePropertyChange("duration", oldDuration, this.duration);
    }

    public void setBroadcasterSoftware(String broadcasterSoftware)
    {
        String oldBroadCasterSoftware = this.broadcasterSoftware;
        this.broadcasterSoftware = broadcasterSoftware;
        this.pcs.firePropertyChange("broadcasterSoftware", oldBroadCasterSoftware, this.broadcasterSoftware);
    }

    public void setChannel(String channel)
    {
        String oldChannel = this.channel;
        this.channel = channel;
        this.pcs.firePropertyChange("channel", oldChannel, this.channel);
    }

    private void setChunks(TwitchChunks chunks)
    {
        Map<String, List<TwitchVideoPart>> oldParts;
        if (!(this.chunks == null)) oldParts = getAllParts();
        else oldParts = null;
        this.chunks = chunks;
        this.pcs.firePropertyChange("chunks", oldParts, getAllParts());
    }

    public void setPreviewSmall(String previewSmall)
    {
        String oldPreviewSmall = this.previewSmall;
        this.previewSmall = previewSmall;
        this.pcs.firePropertyChange("previewSmall", oldPreviewSmall, this.previewSmall);
    }

    public void setPreview(String preview)
    {
        String oldPreview = this.preview;
        this.preview = preview;
        this.pcs.firePropertyChange("preview", oldPreview, this.preview);
    }

    public void setVodAdFrequency(String vodAdFrequency)
    {
        String oldVodAdFrequency = this.vodAdFrequency;
        this.vodAdFrequency = vodAdFrequency;
        this.pcs.firePropertyChange("vodAdFrequency", oldVodAdFrequency, this.vodAdFrequency);
    }

    public void setVodAdLength(String vodAdLength)
    {
        String oldVodAdLength = this.vodAdLength;
        this.vodAdLength = vodAdLength;
        this.pcs.firePropertyChange("vodAdLength", oldVodAdLength, this.vodAdLength);
    }

    public void setRedirectApiId(String redirectApiId)
    {
        String oldRedirectApiId = this.redirectApiId;
        this.redirectApiId = redirectApiId;
        this.pcs.firePropertyChange("redirectApiId", oldRedirectApiId, this.redirectApiId);
    }

    public void setMutedSegments(String mutedSegments)
    {
        String oldMutedSegments = this.mutedSegments;
        this.mutedSegments = mutedSegments;
        this.pcs.firePropertyChange("mutedSegments", oldMutedSegments, this.mutedSegments);
    }

    public void update(String archiveId) throws IOException
    {
        URL apiRequestUrl = new URL("https://api.twitch.tv/api/videos/".concat(archiveId));
        update(apiRequestUrl);
    }

    public void update(URL apiURL) throws IOException
    {
        InputStream is = apiURL.openStream();
        InputStreamReader ir = new InputStreamReader(is);
        TwitchDownloadInfo dlInfo = new Gson().fromJson(ir, TwitchDownloadInfo.class);
        update(dlInfo);
    }

    private void update(TwitchDownloadInfo dlInfo)
    {
        setApiId(dlInfo.apiId);
        setStartOffset(dlInfo.startOffset);
        setPlayOffset(dlInfo.playOffset);
        setIncrementViewCountUrl(dlInfo.incrementViewCountUrl);
        setPath(dlInfo.path);
        setDuration(dlInfo.duration);
        setBroadcasterSoftware(dlInfo.broadcasterSoftware);
        setChannel(dlInfo.channel);
        setChunks(dlInfo.chunks);
        setPreviewSmall(dlInfo.previewSmall);
        setPreview(dlInfo.preview);
        setVodAdFrequency(dlInfo.vodAdFrequency);
        setVodAdLength(dlInfo.vodAdLength);
        setRedirectApiId(dlInfo.redirectApiId);
        setMutedSegments(dlInfo.mutedSegments);

        this.pcs.firePropertyChange("fullUpdate", null, this);
    }
}
