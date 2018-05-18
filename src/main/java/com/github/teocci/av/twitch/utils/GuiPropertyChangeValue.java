package com.github.teocci.av.twitch.utils;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Is a containerClass to get user input to the controller. It can contain a List of PropertyChangeListner references
 * that could be used for SwingWorkers to update a GUI.
 * <p>
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Apr-26
 */
public class GuiPropertyChangeValue extends HashMap<String, String>
{
    private PropertyChangeListener propertyChangeListener;

    public GuiPropertyChangeValue(Map<? extends String, ? extends String> m, PropertyChangeListener propertyChangeListener)
    {
        super(m);
        this.propertyChangeListener = propertyChangeListener;
    }

    public GuiPropertyChangeValue(PropertyChangeListener propertyChangeListener)
    {
        this.propertyChangeListener = propertyChangeListener;
    }

    public GuiPropertyChangeValue()
    {
    }

    public String putValue(String key, String value)
    {
        return super.put(key, value);
    }

    public PropertyChangeListener getPropertyChangeListener()
    {
        return propertyChangeListener;
    }
}
