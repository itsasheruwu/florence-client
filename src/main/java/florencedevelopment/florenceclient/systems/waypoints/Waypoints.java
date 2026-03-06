/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.waypoints;

import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.game.GameJoinedEvent;
import florencedevelopment.florenceclient.events.game.GameLeftEvent;
import florencedevelopment.florenceclient.systems.System;
import florencedevelopment.florenceclient.systems.Systems;
import florencedevelopment.florenceclient.systems.waypoints.events.WaypointAddedEvent;
import florencedevelopment.florenceclient.systems.waypoints.events.WaypointRemovedEvent;
import florencedevelopment.florenceclient.utils.Utils;
import florencedevelopment.florenceclient.utils.files.StreamUtils;
import florencedevelopment.florenceclient.utils.misc.NbtUtils;
import florencedevelopment.florenceclient.utils.player.PlayerUtils;
import florencedevelopment.florenceclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Waypoints extends System<Waypoints> implements Iterable<Waypoint> {
    private static final String PNG = ".png";

    public static final String[] BUILTIN_ICONS = {"square", "circle", "triangle", "star", "diamond", "skull"};

    public final Map<String, AbstractTexture> icons = new ConcurrentHashMap<>();

    private final List<Waypoint> waypoints = new CopyOnWriteArrayList<>();

    public Waypoints() {
        super(null);
    }

    public static Waypoints get() {
        return Systems.get(Waypoints.class);
    }

    @Override
    public void init() {
        File iconsFolder = new File(new File(FlorenceClient.FOLDER, "waypoints"), "icons");
        iconsFolder.mkdirs();

        for (String builtinIcon : BUILTIN_ICONS) {
            File iconFile = new File(iconsFolder, builtinIcon + PNG);
            if (!iconFile.exists()) copyIcon(iconFile);
        }

        File[] files = iconsFolder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.getName().endsWith(PNG)) {
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    String name = Strings.CS.removeEnd(file.getName(), PNG);
                    AbstractTexture texture = new NativeImageBackedTexture(() -> name, NativeImage.read(inputStream));
                    icons.put(name, texture);
                }
                catch (Exception e) {
                    FlorenceClient.LOG.error("Failed to read a waypoint icon", e);
                }
            }
        }
    }

    /**
     * Adds a waypoint or saves it if it already exists
     * @return {@code true} if waypoint already exists
     */
    public boolean add(Waypoint waypoint) {
        if (waypoints.contains(waypoint)) {
            save();
            return true;
        }

        waypoints.add(waypoint);
        save();

        FlorenceClient.EVENT_BUS.post(new WaypointAddedEvent(waypoint));

        return false;
    }

    public boolean remove(Waypoint waypoint) {
        boolean removed = waypoints.remove(waypoint);
        if (removed) {
            save();
            FlorenceClient.EVENT_BUS.post(new WaypointRemovedEvent(waypoint));
        }

        return removed;
    }

    public void removeAll(Collection<Waypoint> c) {
        boolean removed = waypoints.removeAll(c);
        if (removed) save();
    }

    public Waypoint get(String name) {
        for (Waypoint waypoint : waypoints) {
            if (waypoint.name.get().equalsIgnoreCase(name)) return waypoint;
        }

        return null;
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        load();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onGameDisconnected(GameLeftEvent event) {
        waypoints.clear();
    }

    public static boolean checkDimension(Waypoint waypoint) {
        Dimension playerDim = PlayerUtils.getDimension();
        Dimension waypointDim = waypoint.dimension.get();

        if (playerDim == waypointDim) return true;
        if (!waypoint.opposite.get()) return false;

        boolean playerOpp = playerDim == Dimension.Overworld || playerDim == Dimension.Nether;
        boolean waypointOpp = waypointDim == Dimension.Overworld || waypointDim == Dimension.Nether;

        return playerOpp && waypointOpp;
    }

    @Override
    public File getFile() {
        if (!Utils.canUpdate()) return null;
        return new File(new File(FlorenceClient.FOLDER, "waypoints"), Utils.getFileWorldName() + ".nbt");
    }

    public boolean isEmpty() {
        return waypoints.isEmpty();
    }

    @Override
    public @NotNull Iterator<Waypoint> iterator() {
        return new WaypointIterator();
    }

    private void copyIcon(File file) {
        String path = "/assets/" + FlorenceClient.MOD_ID + "/textures/icons/waypoints/" + file.getName();
        InputStream in = Waypoints.class.getResourceAsStream(path);

        if (in == null) {
            FlorenceClient.LOG.error("Failed to read a resource: {}", path);
            return;
        }

        StreamUtils.copy(in, file);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("waypoints", NbtUtils.listToTag(waypoints));
        return tag;
    }

    @Override
    public Waypoints fromTag(NbtCompound tag) {
        waypoints.clear();

        for (NbtElement waypointTag : tag.getListOrEmpty("waypoints")) {
            waypoints.add(new Waypoint(waypointTag));
        }

        return this;
    }

    private final class WaypointIterator implements Iterator<Waypoint> {
        private final Iterator<Waypoint> it = waypoints.iterator();

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Waypoint next() {
            return it.next();
        }

        @Override
        public void remove() {
            it.remove();
            save();
        }
    }
}
