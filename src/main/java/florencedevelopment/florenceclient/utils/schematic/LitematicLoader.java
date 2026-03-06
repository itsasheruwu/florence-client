/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.schematic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.*;

/**
 * Parses .litematic files using raw NBT, without depending on any Litematica mod classes.
 *
 * Litematic packing uses a no-cross-boundary scheme:
 *   bitsPerEntry = max(2, ceil(log2(paletteSize)))
 *   entriesPerLong = floor(64 / bitsPerEntry)
 *   For block i:
 *     longIdx = i / entriesPerLong
 *     bitIdx  = (i % entriesPerLong) * bitsPerEntry
 *     paletteIdx = (blockStates[longIdx] >> bitIdx) & mask
 */
public class LitematicLoader {

    public static SchematicData load(File file, BlockPos origin) {
        NbtCompound root = SchematicLoader.readNbt(file);
        if (root == null) return null;

        if (!root.contains("Regions")) return null;
        NbtCompound regions = root.getCompoundOrEmpty("Regions");

        List<BlockEntry> allBlocks = new ArrayList<>();
        Map<Block, Integer> totalRequired = new HashMap<>();

        for (String regionName : regions.getKeys()) {
            NbtCompound region = regions.getCompoundOrEmpty(regionName);
            parseRegion(region, origin, allBlocks, totalRequired);
        }

        allBlocks.sort(Comparator.comparingInt(e -> e.worldPos().getY()));

        return new SchematicData(allBlocks, totalRequired, origin);
    }

    private static void parseRegion(NbtCompound region, BlockPos origin,
                                     List<BlockEntry> allBlocks, Map<Block, Integer> totalRequired) {
        if (!region.contains("BlockStatePalette") || !region.contains("BlockStates")) return;

        NbtList paletteNbt = region.getListOrEmpty("BlockStatePalette");
        List<BlockState> palette = new ArrayList<>();
        for (NbtElement element : paletteNbt) {
            if (element instanceof NbtCompound nbt) {
                palette.add(parseBlockState(nbt));
            }
        }

        if (palette.isEmpty()) return;

        long[] blockStates = region.getLongArray("BlockStates").orElse(new long[0]);

        NbtCompound sizeNbt = region.getCompoundOrEmpty("Size");
        int sizeX = Math.abs(sizeNbt.getInt("x", 0));
        int sizeY = Math.abs(sizeNbt.getInt("y", 0));
        int sizeZ = Math.abs(sizeNbt.getInt("z", 0));

        NbtCompound posNbt = region.getCompoundOrEmpty("Position");
        int posX = posNbt.getInt("x", 0);
        int posY = posNbt.getInt("y", 0);
        int posZ = posNbt.getInt("z", 0);

        if (sizeX == 0 || sizeY == 0 || sizeZ == 0) return;

        int paletteSize = palette.size();
        int bitsPerEntry = Math.max(2, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
        int entriesPerLong = 64 / bitsPerEntry;
        long mask = (1L << bitsPerEntry) - 1L;

        int totalBlocks = sizeX * sizeY * sizeZ;

        for (int i = 0; i < totalBlocks; i++) {
            int longIdx = i / entriesPerLong;
            int bitIdx = (i % entriesPerLong) * bitsPerEntry;

            if (longIdx >= blockStates.length) break;

            int paletteIdx = (int) ((blockStates[longIdx] >> bitIdx) & mask);
            if (paletteIdx >= palette.size()) continue;

            BlockState state = palette.get(paletteIdx);
            if (state == null) continue;

            // Skip air
            if (state.isAir()) continue;

            // Compute local coords from linear index (litematic: x + y*sizeX*sizeZ + z*sizeX)
            int localX = i % sizeX;
            int localZ = (i / sizeX) % sizeZ;
            int localY = i / (sizeX * sizeZ);

            int worldX = origin.getX() + posX + localX;
            int worldY = origin.getY() + posY + localY;
            int worldZ = origin.getZ() + posZ + localZ;

            BlockPos worldPos = new BlockPos(worldX, worldY, worldZ);
            allBlocks.add(new BlockEntry(worldPos, state));

            Block block = state.getBlock();
            totalRequired.merge(block, 1, Integer::sum);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static BlockState parseBlockState(NbtCompound nbt) {
        String name = nbt.getString("Name", "");
        if (name.isEmpty()) return null;

        Block block = Registries.BLOCK.get(Identifier.of(name));
        if (block == null) return null;

        BlockState state = block.getDefaultState();

        if (nbt.contains("Properties")) {
            NbtCompound props = nbt.getCompoundOrEmpty("Properties");
            for (String key : props.getKeys()) {
                String value = props.getString(key, "");
                if (value.isEmpty()) continue;
                Property property = block.getStateManager().getProperty(key);
                if (property != null) {
                    Optional<Comparable> parsed = property.parse(value);
                    if (parsed.isPresent()) {
                        state = state.with(property, parsed.get());
                    }
                }
            }
        }

        return state;
    }
}
