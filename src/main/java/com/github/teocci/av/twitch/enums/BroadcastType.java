package com.github.teocci.av.twitch.enums;

public enum BroadcastType {
    ALL,
    ARCHIVE,
    HIGHLIGHT,
    UPLOAD;

    public static BroadcastType toType(String type) {
        switch(type.toLowerCase()) {
            case "all": return ALL;
            case "archive": return ARCHIVE;
            case "highlight": return HIGHLIGHT;
            case "upload": return UPLOAD;
            default:
                return null;
        }
    }
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
