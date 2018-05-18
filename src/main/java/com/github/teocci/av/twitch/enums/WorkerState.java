package com.github.teocci.av.twitch.enums;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-May-16
 */
public enum WorkerState {
    /**
     * Initial {@code Worker} state.
     */
    PENDING,
    /**
     * {@code Worker} is {@code STARTED}
     * before invoking {@code doInBackground}.
     */
    STARTED,

    /**
     * {@code Worker} is {@code DONE}
     * after {@code doInBackground} method
     * is finished.
     */
    DONE
}
