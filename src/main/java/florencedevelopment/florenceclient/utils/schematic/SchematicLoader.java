/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.schematic;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.util.math.BlockPos;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class SchematicLoader {

    public static SchematicData load(File file, BlockPos origin) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".litematic")) {
            if (!FabricLoader.getInstance().isModLoaded("litematica")) {
                return null;
            }
            return LitematicLoader.load(file, origin);
        } else if (name.endsWith(".schem")) {
            return SpongeSchematicLoader.load(file, origin);
        }

        return null;
    }

    public static boolean isLitematicWithoutMod(File file) {
        return file.getName().toLowerCase().endsWith(".litematic")
            && !FabricLoader.getInstance().isModLoaded("litematica");
    }

    public static NbtCompound readNbt(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             DataInputStream dis = new DataInputStream(fis)) {
            return NbtIo.readCompressed(dis, NbtSizeTracker.ofUnlimitedBytes());
        } catch (Exception e) {
            return null;
        }
    }
}
