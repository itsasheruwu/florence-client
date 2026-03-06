/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.network;

public class OnlinePlayers {
    private static long lastPingTime;

    private OnlinePlayers() {
    }

    public static void update() {
        long time = System.currentTimeMillis();

        if (time - lastPingTime > 5 * 60 * 1000) {
            FlorenceExecutor.execute(() -> Http.post("https://FlorenceClient.com/api/online/ping").ignoreExceptions().send());

            lastPingTime = time;
        }
    }

    public static void leave() {
        FlorenceExecutor.execute(() -> Http.post("https://FlorenceClient.com/api/online/leave").ignoreExceptions().send());
    }
}
