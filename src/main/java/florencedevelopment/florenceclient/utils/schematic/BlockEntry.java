/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.schematic;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public record BlockEntry(BlockPos worldPos, BlockState state) {}
