/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.schematic;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class SchematicData {
    public final List<BlockEntry> blocks;
    public final Map<Block, Integer> totalRequired;
    public final BlockPos origin;

    public SchematicData(List<BlockEntry> blocks, Map<Block, Integer> totalRequired, BlockPos origin) {
        this.blocks = blocks;
        this.totalRequired = totalRequired;
        this.origin = origin;
    }
}
