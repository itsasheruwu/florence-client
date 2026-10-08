/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.gui;

/**
 * Posted to show a small message in the corner of the screen. Use {@code NotificationManager.push} to post one.
 */
public class NotificationEvent {
    private static final NotificationEvent INSTANCE = new NotificationEvent();

    public enum Severity {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        ENABLED,
        DISABLED
    }

    /** Notifications with the same key replace each other instead of piling up, null to always add a new one. */
    public String key;
    public String title;
    public String body;
    public Severity severity;
    public double seconds;

    public static NotificationEvent get(String key, String title, String body, Severity severity, double seconds) {
        INSTANCE.key = key;
        INSTANCE.title = title;
        INSTANCE.body = body;
        INSTANCE.severity = severity;
        INSTANCE.seconds = seconds;
        return INSTANCE;
    }
}
