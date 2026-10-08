/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.notifications;

import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.florence.ModuleBindChangedEvent;
import florencedevelopment.florenceclient.events.gui.ModuleToggledEvent;
import florencedevelopment.florenceclient.events.gui.NotificationEvent;
import florencedevelopment.florenceclient.events.gui.NotificationEvent.Severity;
import florencedevelopment.florenceclient.events.render.Render2DEvent;
import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.GuiThemes;
import florencedevelopment.florenceclient.gui.WidgetScreen;
import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Spring;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.design.Preset;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.utils.PostInit;
import florencedevelopment.florenceclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

/**
 * Small messages that slide in at the top right of the screen and fade away on their own. They are drawn on top of the
 * click GUI while it is open and over the game otherwise.
 */
public final class NotificationManager {
    private static final NotificationManager INSTANCE = new NotificationManager();
    private static final int MAX_SHOWN = 5;

    private static final Design FALLBACK = Design.build(Preset.DARK, Colors.rgb(108, 92, 231), Colors.rgb(74, 144, 255), 0.85);

    private final List<Notification> notifications = new ArrayList<>();
    private final GuiRenderer hudRenderer = new GuiRenderer();

    private NotificationManager() {}

    public static NotificationManager get() {
        return INSTANCE;
    }

    @PostInit
    public static void init() {
        FlorenceClient.EVENT_BUS.subscribe(INSTANCE);
    }

    /**
     * Shows a notification.
     */
    public static void push(String key, String title, String body, Severity severity, double seconds) {
        FlorenceClient.EVENT_BUS.post(NotificationEvent.get(key, title, body, severity, seconds));
    }

    public static void push(String title, String body, Severity severity) {
        push(null, title, body, severity, 3);
    }

    // Events

    @EventHandler
    private void onNotification(NotificationEvent event) {
        Notification existing = null;

        if (event.key != null) {
            for (Notification notification : notifications) {
                if (event.key.equals(notification.key) && !notification.leaving) {
                    existing = notification;
                    break;
                }
            }
        }

        if (existing != null) {
            existing.update(event.title, event.body, event.severity, event.seconds);
            return;
        }

        notifications.add(new Notification(event.key, event.title, event.body, event.severity, event.seconds));
    }

    @EventHandler
    private void onModuleToggled(ModuleToggledEvent event) {
        // Only for what is done in the GUI, the module list already shows the rest
        if (!(mc.currentScreen instanceof WidgetScreen)) return;
        if (!(GuiThemes.get() instanceof FlorenceGuiTheme theme) || !theme.notifications.get()) return;

        push("module:" + event.module.name, event.module.title, event.active ? "Enabled" : "Disabled", event.active ? Severity.ENABLED : Severity.DISABLED, 2);
    }

    @EventHandler
    private void onBindChanged(ModuleBindChangedEvent event) {
        if (!(mc.currentScreen instanceof WidgetScreen)) return;
        if (!(GuiThemes.get() instanceof FlorenceGuiTheme theme) || !theme.notifications.get()) return;

        String bind = event.module.keybind.isSet() ? "Bound to " + event.module.keybind : "Keybind removed";
        push("bind:" + event.module.name, event.module.title, bind, Severity.INFO, 2.5);
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        // The click GUI draws them itself
        if (notifications.isEmpty() || mc.currentScreen instanceof WidgetScreen) return;

        GuiTheme theme = GuiThemes.get();
        if (theme == null) return;

        hudRenderer.theme = theme;
        hudRenderer.begin(event.drawContext);
        render(hudRenderer, theme, Utils.frameTime);
        hudRenderer.end();
    }

    // Rendering

    /**
     * Draws and updates the notifications.
     *
     * @param delta time since the last frame in seconds
     */
    public void render(GuiRenderer renderer, GuiTheme theme, double delta) {
        if (notifications.isEmpty()) return;

        FlorenceGuiTheme florence = theme instanceof FlorenceGuiTheme f ? f : null;
        Design d = florence != null ? florence.design() : FALLBACK;

        double th = theme.textHeight();
        double margin = theme.scale(14);
        // Room for the icon on the left and some air on the right
        double width = Math.max(theme.scale(230), Math.min(theme.scale(400), widest(theme) + th * 1.1 + theme.scale(66)));
        double radius = florence != null ? florence.radiusMedium() * 1.3 : theme.scale(8);
        double screenWidth = Utils.getWindowWidth();

        // Newest at the top
        double y = margin;
        int shown = 0;

        Iterator<Notification> it = notifications.iterator();
        List<Notification> visible = new ArrayList<>();

        while (it.hasNext()) {
            Notification notification = it.next();

            notification.step(delta);

            if (notification.isGone()) it.remove();
            else visible.add(notification);
        }

        for (int i = visible.size() - 1; i >= 0 && shown < MAX_SHOWN; i--, shown++) {
            Notification n = visible.get(i);

            double height = th * 2.5 + (n.body != null && !n.body.isEmpty() ? th * 0.85 : 0);
            n.slot.springTo(y, Spring.CRITICAL);

            double enter = Math.max(0, Math.min(1.2, n.enter.get()));
            double x = screenWidth - margin - width + (1 - enter) * (width + margin);
            double top = n.placed ? n.slot.get() : y;

            if (!n.placed) {
                n.slot.snap(y);
                n.placed = true;
            }

            draw(renderer, theme, florence, d, n, x, top, width, height, radius, enter);

            y += height + theme.scale(8);
        }
    }

    private double widest(GuiTheme theme) {
        double widest = 0;

        for (Notification n : notifications) {
            widest = Math.max(widest, theme.textWidth(n.title));
            if (n.body != null) widest = Math.max(widest, theme.textWidth(n.body));
        }

        return widest;
    }

    private void draw(GuiRenderer renderer, GuiTheme theme, FlorenceGuiTheme florence, Design d, Notification n,
                      double x, double y, double width, double height, double radius, double enter) {
        double th = theme.textHeight();
        double line = Math.max(1, Math.round(theme.scale(1)));
        int accent = color(d, n.severity);

        double alpha = Math.min(1, enter);

        if (florence == null || florence.shadows()) {
            renderer.shadow(x, y + theme.scale(4), width, height, radius, theme.scale(18), Colors.mulAlpha(d.shadow, alpha));
        }

        renderer.roundRect(x, y, width, height, radius, Colors.mulAlpha(Colors.withAlpha(d.header, 248), alpha), line, Colors.mulAlpha(d.outlineHover, alpha));

        // Colored strip on the left
        double strip = Math.max(3, theme.scale(3));
        renderer.roundRect(x + theme.scale(6), y + height * 0.2, strip, height * 0.6, strip / 2, Colors.mulAlpha(accent, alpha));

        // Icon
        double icon = th * 1.1;
        double ix = x + theme.scale(18);
        double iy = y + (height - icon) / 2;
        drawIcon(renderer, theme, n.severity, ix, iy, icon, Colors.mulAlpha(accent, alpha), Colors.mulAlpha(d.textOnAccent, alpha));

        // Text
        double tx = ix + icon + theme.scale(12);
        boolean hasBody = n.body != null && !n.body.isEmpty();
        double ty = y + (height - (hasBody ? th * 1.9 : th)) / 2;

        renderer.text(n.title, tx, ty, Colors.mulAlpha(d.text, alpha), false);
        if (hasBody) renderer.text(n.body, tx, ty + th * 1.05, Colors.mulAlpha(d.textSecondary, alpha), false);

        // Time left
        double left = Math.max(0, 1 - n.age / n.seconds);
        double barHeight = Math.max(2, theme.scale(2));
        double barInset = radius * 0.8;

        renderer.roundRect(x + barInset, y + height - barHeight - theme.scale(3), (width - barInset * 2) * left, barHeight, barHeight / 2, Colors.mulAlpha(accent, alpha * 0.8));
    }

    private static void drawIcon(GuiRenderer renderer, GuiTheme theme, Severity severity, double x, double y, double size, int fill, int mark) {
        double cx = x + size / 2;
        double cy = y + size / 2;
        double t = Math.max(1.5, theme.scale(2));

        renderer.circle(cx, cy, size / 2, fill);

        switch (severity) {
            case ENABLED, SUCCESS -> {
                renderer.line(cx - size * 0.22, cy + size * 0.02, cx - size * 0.06, cy + size * 0.18, t, mark);
                renderer.line(cx - size * 0.06, cy + size * 0.18, cx + size * 0.24, cy - size * 0.16, t, mark);
            }
            case DISABLED -> renderer.line(cx - size * 0.2, cy, cx + size * 0.2, cy, t, mark);
            case ERROR -> {
                renderer.line(cx - size * 0.18, cy - size * 0.18, cx + size * 0.18, cy + size * 0.18, t, mark);
                renderer.line(cx - size * 0.18, cy + size * 0.18, cx + size * 0.18, cy - size * 0.18, t, mark);
            }
            case WARNING -> {
                renderer.line(cx, cy - size * 0.22, cx, cy + size * 0.06, t, mark);
                renderer.circle(cx, cy + size * 0.22, t / 2, mark);
            }
            case INFO -> {
                renderer.circle(cx, cy - size * 0.2, t / 2, mark);
                renderer.line(cx, cy - size * 0.04, cx, cy + size * 0.22, t, mark);
            }
        }
    }

    private static int color(Design d, Severity severity) {
        return switch (severity) {
            case INFO -> d.accent;
            case SUCCESS, ENABLED -> d.success;
            case WARNING -> d.warning;
            case ERROR -> d.danger;
            case DISABLED -> d.textSecondary;
        };
    }

    private static final class Notification {
        final String key;
        String title, body;
        Severity severity;
        double seconds;

        double age;
        boolean leaving;
        boolean placed;

        final AnimatedFloat enter = new AnimatedFloat(0);
        final AnimatedFloat slot = new AnimatedFloat(0);

        Notification(String key, String title, String body, Severity severity, double seconds) {
            this.key = key;
            update(title, body, severity, seconds);
        }

        void update(String title, String body, Severity severity, double seconds) {
            this.title = title;
            this.body = body;
            this.severity = severity;
            this.seconds = Math.max(0.5, seconds);

            // Starts over so it stays for the full time again
            age = 0;
            leaving = false;
        }

        void step(double delta) {
            age += delta;

            if (!leaving && age >= seconds) leaving = true;

            enter.springTo(leaving ? 0 : 1, Spring.SNAPPY);
            enter.update(delta);
            slot.update(delta);
        }

        boolean isGone() {
            return leaving && enter.get() < 0.02;
        }
    }
}
