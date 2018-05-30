package com.github.teocci.av.twitch.models;

import java.util.List;
import java.util.Vector;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public final class WorkerQueue<T extends Object>
{
    private Vector<T> content;
    private int initialSize;

    public WorkerQueue(List<T> content)
    {
        this.content = new Vector<T>(content);
        this.initialSize = content.size();
    }

    public WorkerQueue()
    {
        this.content = new Vector<T>();
        this.initialSize = 0;
    }

    public T pop()
    {
        if (!content.isEmpty()) {
            T item = content.get(0);
            content.remove(item);
            return item;
        }
        return null;
    }

    public T peek()
    {
        if (!content.isEmpty()) {
            T item = content.get(0);
            return item;
        }
        return null;
    }

    public void append(T item)
    {
        content.add(item);
        initialSize++;
    }

    public void append(List<T> items)
    {
        content.addAll(items);
        initialSize += items.size();
    }

    public void resetQueue(List<T> content)
    {
        this.content = new Vector<T>(content);
        this.initialSize = content.size();
    }

    public int size()
    {
        return content.size();
    }

    public boolean isEmpty()
    {
        return content.isEmpty();
    }

    public int getInitialSize()
    {
        return initialSize;
    }
}
