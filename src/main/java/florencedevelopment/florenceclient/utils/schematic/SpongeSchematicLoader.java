/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.schematic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.*;

/**
 * Parses .schem (Sponge/WorldEdit schematic) files using raw NBT.
 *
 * Format:
 *   - Width (short), Height (short), Length (short)
 *   - Palette compound: blockstate string -> int ID
 *   - BlockData byte[]: varints encoding palette IDs
 *   - Linear index -> (x, y, z): y*(W*L) + z*W + x
 */
public class SpongeSchematicLoader {

    public static SchematicData load(File file, BlockPos origin) {
        NbtCompound root = SchematicLoader.readNbt(file);
        if (root == null) return null;

        int width  = root.getShort("Width").orElse((short) 0) & 0xFFFF;
        int height = root.getShort("Height").orElse((short) 0) & 0xFFFF;
        int length = root.getShort("Length").orElse((short) 0) & 0xFFFF;

        if (width == 0 || height == 0 || length == 0) return null;

        if (!root.contains("Palette") || !root.contains("BlockData")) return null;

        NbtCompound paletteNbt = root.getCompoundOrEmpty("Palette");
        BlockState[] palette = new BlockState[paletteNbt.getKeys().size()];

        for (String key : paletteNbt.getKeys()) {
            int id = paletteNbt.getInt(key, -1);
            if (id >= 0 && id < palette.length) {
                palette[id] = parseBlockState(key);
            }
        }

        byte[] rawData = root.getByteArray("BlockData").orElse(new byte[0]);
        int[] blockData = decodeVarints(rawData);

        List<BlockEntry> blocks = new ArrayList<>();
        Map<Block, Integer> totalRequired = new HashMap<>();

        for (int i = 0; i < blockData.length; i++) {
            int y = i / (width * length);
            int rem = i % (width * length);
            int z = rem / width;
            int x = rem % width;

            if (y >= height || z >= length || x >= width) continue;

            int paletteIdx = blockData[i];
            if (paletteIdx < 0 || paletteIdx >= palette.length) continue;

            BlockState state = palette[paletteIdx];
            if (state == null || state.isAir()) continue;

            BlockPos worldPos = new BlockPos(
                origin.getX() + x,
                origin.getY() + y,
                origin.getZ() + z
            );

            blocks.add(new BlockEntry(worldPos, state));

            Block block = state.getBlock();
            totalRequired.merge(block, 1, Integer::sum);
        }

        blocks.sort(Comparator.comparingInt(e -> e.worldPos().getY()));

        return new SchematicData(blocks, totalRequired, origin);
    }

    private static int[] decodeVarints(byte[] data) {
        int[] result = new int[data.length];
        int count = 0;
        int i = 0;

        while (i < data.length) {
            int value = 0;
            int shift = 0;
            int b;
            do {
                if (i >= data.length) break;
                b = data[i++] & 0xFF;
                value |= (b & 0x7F) << shift;
                shift += 7;
            } while ((b & 0x80) != 0);
            result[count++] = value;
        }

        return Arrays.copyOf(result, count);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState parseBlockState(String blockStateStr) {
        // Format: "minecraft:stone" or "minecraft:stone_slab[type=double]"
        String name = blockStateStr;
        Map<String, String> properties = new HashMap<>();

        int bracketIdx = blockStateStr.indexOf('[');
        if (bracketIdx != -1) {
            name = blockStateStr.substring(0, bracketIdx);
            String propStr = blockStateStr.substring(bracketIdx + 1, blockStateStr.length() - 1);
            for (String pair : propStr.split(",")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    properties.put(kv[0].trim(), kv[1].trim());
                }
            }
        }

        Block block = Registries.BLOCK.get(Identifier.of(name));
        if (block == null) return null;

        BlockState state = block.getDefaultState();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            Property property = block.getStateManager().getProperty(entry.getKey());
            if (property != null) {
                Optional<Comparable> parsed = property.parse(entry.getValue());
                if (parsed.isPresent()) {
                    state = state.with(property, parsed.get());
                }
            }
        }

        return state;
    }
}
