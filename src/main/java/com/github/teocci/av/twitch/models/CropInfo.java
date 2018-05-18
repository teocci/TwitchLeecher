package com.github.teocci.av.twitch.models;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class CropInfo
{
    public boolean cropStart;
    public boolean cropEnd;
    public double start;
    public double length;

    public CropInfo(boolean cropStart, boolean cropEnd, double start, double length)
    {
        this.cropStart = cropStart;
        this.cropEnd = cropEnd;
        this.start = start;
        this.length = length;
    }
}
