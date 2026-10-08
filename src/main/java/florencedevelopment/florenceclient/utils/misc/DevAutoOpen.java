/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.misc;

import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.gui.GuiThemes;
import florencedevelopment.florenceclient.gui.WidgetScreen;
import florencedevelopment.florenceclient.events.gui.NotificationEvent.Severity;
import florencedevelopment.florenceclient.gui.design.Preset;
import florencedevelopment.florenceclient.gui.notifications.NotificationManager;
import florencedevelopment.florenceclient.gui.screens.ModulesScreen;
import florencedevelopment.florenceclient.gui.screens.palette.CommandPaletteScreen;
import florencedevelopment.florenceclient.gui.tabs.Tab;
import florencedevelopment.florenceclient.gui.tabs.Tabs;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.utils.PostInit;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.TitleScreen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static florencedevelopment.florenceclient.FlorenceClient.mc;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

/**
 * Development helper that makes it possible to look at the GUI without any input. Only does anything in a development
 * environment with the {@code FLORENCE_DEV_OPEN_GUI} environment variable set.
 * <p>
 * Setting the variable opens the click GUI on the title screen, setting it to {@code world} loads the save called
 * "New World" first so the GUI is on top of a world. After that, commands can be written to the file
 * {@code florence-dev-command.txt} in the game folder, one per line, and are run (and the file deleted) on the next tick:
 * <pre>
 * gui                  opens the click GUI
 * close                closes the screen
 * tab Config           opens a tab
 * expand Auto Eat      shows the settings of a module
 * toggle Auto Eat      turns a module on or off
 * hover 400 300        moves the mouse, in window coordinates
 * preset Light         changes the look of the GUI
 * scale 1.5            changes the scale of the GUI
 * </pre>
 */
public final class DevAutoOpen {
    private static final Path COMMAND_FILE = FabricLoader.getInstance().getGameDir().resolve("florence-dev-command.txt");

    private static boolean loadWorld;
    private static boolean worldRequested;
    private static boolean opened;
    private static int ticks;

    private DevAutoOpen() {}

    @PostInit
    public static void init() {
        String mode = System.getenv("FLORENCE_DEV_OPEN_GUI");
        if (!FabricLoader.getInstance().isDevelopmentEnvironment() || mode == null) return;

        loadWorld = mode.equalsIgnoreCase("world");

        FlorenceClient.EVENT_BUS.subscribe(new ConsumerListener<>(TickEvent.Post.class, event -> tick()));
    }

    private static void tick() {
        if (!opened) {
            openGui();
            return;
        }

        runCommands();
    }

    private static void openGui() {
        if (loadWorld) {
            if (mc.world == null) {
                if (!worldRequested && mc.currentScreen instanceof TitleScreen && ++ticks >= 80) {
                    worldRequested = true;
                    mc.createIntegratedServerLoader().start("New World", () -> {});
                }

                return;
            }

            // In the world, give it a moment to load
            if (mc.player == null || mc.currentScreen != null || ++ticks < 400) return;
        }
        else {
            // Give the title screen a moment to finish loading
            if (!(mc.currentScreen instanceof TitleScreen) || ++ticks < 80) return;
        }

        opened = true;
        Tabs.get().getFirst().openScreen(GuiThemes.get());
    }

    private static void runCommands() {
        if (!Files.exists(COMMAND_FILE)) return;

        String content;

        try {
            content = Files.readString(COMMAND_FILE);
            Files.delete(COMMAND_FILE);
        } catch (IOException e) {
            return;
        }

        for (String line : content.split("\\R")) {
            line = line.trim();
            if (line.isEmpty()) continue;

            try {
                run(line);
            } catch (Exception e) {
                FlorenceClient.LOG.error("Dev command '{}' failed", line, e);
            }
        }
    }

    private static void run(String line) {
        String[] parts = line.split("\\s+", 2);
        String argument = parts.length > 1 ? parts[1] : "";

        switch (parts[0]) {
            case "gui" -> Tabs.get().getFirst().openScreen(GuiThemes.get());
            case "close" -> mc.setScreen(null);
            case "tab" -> {
                for (Tab tab : Tabs.get()) {
                    if (tab.name.equalsIgnoreCase(argument)) tab.openScreen(GuiThemes.get());
                }
            }
            case "expand" -> {
                Module module = findModule(argument);

                if (module != null && mc.currentScreen instanceof ModulesScreen screen) screen.expandModule(module);
            }
            case "toggle" -> {
                Module module = findModule(argument);
                if (module != null) module.toggle();
            }
            case "click" -> {
                String[] xy = argument.split(" +");
                double cx = Double.parseDouble(xy[0]), cy = Double.parseDouble(xy[1]);
                try {
                    long handle = mc.getWindow().getHandle();
                    var move = net.minecraft.client.Mouse.class.getDeclaredMethod("onCursorPos", long.class, double.class, double.class);
                    var button = net.minecraft.client.Mouse.class.getDeclaredMethod("onMouseButton", long.class, net.minecraft.client.input.MouseInput.class, int.class);
                    move.setAccessible(true);
                    button.setAccessible(true);
                    move.invoke(mc.mouse, handle, cx, cy);
                    button.invoke(mc.mouse, handle, new net.minecraft.client.input.MouseInput(0, 0), 1);
                    button.invoke(mc.mouse, handle, new net.minecraft.client.input.MouseInput(0, 0), 0);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
            case "shot" -> net.minecraft.client.util.ScreenshotRecorder.saveScreenshot(mc.runDirectory, argument + ".png", mc.getFramebuffer(), 1, text -> {});
            case "palette" -> mc.setScreen(new CommandPaletteScreen(GuiThemes.get()));
            case "query" -> {
                if (mc.currentScreen instanceof CommandPaletteScreen palette) palette.setQuery(argument);
            }
            case "notify" -> {
                NotificationManager.push("key:" + argument, "Notification", argument, Severity.INFO, 4);
                NotificationManager.push("Module enabled", "Auto Eat", Severity.ENABLED);
                NotificationManager.push("Something went wrong", "Could not load the profile", Severity.ERROR);
                NotificationManager.push("Careful", "This module is experimental", Severity.WARNING);
            }
            case "hover" -> {
                String[] xy = argument.split("\\s+");
                glfwSetCursorPos(mc.getWindow().getHandle(), Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
            }
            case "preset" -> {
                if (GuiThemes.get() instanceof FlorenceGuiTheme theme) {
                    for (Preset preset : Preset.values()) {
                        if (preset.name().equalsIgnoreCase(argument) || preset.toString().equalsIgnoreCase(argument)) theme.preset.set(preset);
                    }
                }
            }
            case "scale" -> {
                if (GuiThemes.get() instanceof FlorenceGuiTheme theme) theme.scale.set(Double.parseDouble(argument));
            }
            default -> FlorenceClient.LOG.warn("Unknown dev command '{}'", line);
        }

        if (mc.currentScreen instanceof WidgetScreen screen) screen.invalidate();
    }

    private static Module findModule(String name) {
        for (Module module : Modules.get().getAll()) {
            if (module.title.equalsIgnoreCase(name) || module.name.equalsIgnoreCase(name)) return module;
        }

        return null;
    }
}
