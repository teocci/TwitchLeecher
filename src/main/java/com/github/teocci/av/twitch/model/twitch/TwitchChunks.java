package com.github.teocci.av.twitch.model.twitch;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-10
 */
public class TwitchChunks
{
    // Maybe someone has a better solution to replace that with a Hashmap (Twitch could change the names
    // of qualities from time to time)

    // no need for a better solution. Twitch changed VODs entirely. This ist still remaining to be compatible
    // to old VODs

    @SerializedName("live")
    private List<TwitchVideoPart> source;

    @SerializedName("240p")
    private List<TwitchVideoPart> mobile;

    @SerializedName("360p")
    private List<TwitchVideoPart> low;

    @SerializedName("480p")
    private List<TwitchVideoPart> mid;

    @SerializedName("720p")
    private List<TwitchVideoPart> high;

    public TwitchChunks()
    {
        source = new ArrayList<>();
        mobile = new ArrayList<>();
        low = new ArrayList<>();
        mid = new ArrayList<>();
        high = new ArrayList<>();
    }

    public List<TwitchVideoPart> getSource()
    {
        return source;
    }

    public List<TwitchVideoPart> getMobile()
    {
        return mobile;
    }

    public List<TwitchVideoPart> getLow()
    {
        return low;
    }

    public List<TwitchVideoPart> getMid()
    {
        return mid;
    }

    public List<TwitchVideoPart> getHigh()
    {
        return high;
    }


    public void addSourcePart(TwitchVideoPart tbp)
    {
        source.add(tbp);
    }

    public void addHighPart(TwitchVideoPart tbp)
    {
        high.add(tbp);
    }

    public void addMediumPart(TwitchVideoPart tbp)
    {
        mid.add(tbp);
    }

    public void addLowPart(TwitchVideoPart tbp)
    {
        low.add(tbp);
    }

    public void addMobilePart(TwitchVideoPart tbp)
    {
        mobile.add(tbp);
    }

    @Override
    public String toString()
    {
        return source.toString();
    }
}
