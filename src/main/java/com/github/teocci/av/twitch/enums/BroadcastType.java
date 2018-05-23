package com.github.teocci.av.twitch.enums;

public enum BroadcastType {
    ARCHIVE,
    HIGHLIGHT,
    UPLOAD;

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
