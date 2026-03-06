/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.movement;

import florencedevelopment.florenceclient.events.entity.player.PlayerMoveEvent;
import florencedevelopment.florenceclient.mixininterface.IVec3d;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;

public class Languid extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<LanguidMode> mode = sgGeneral.add(new EnumSetting.Builder<LanguidMode>()
        .name("mode")
        .description("The method of removing soul sand/soul soil slowdown. Motion: Directly modifies movement values. Emulate: Emulates Soul Speed III enchantment. Replace: Replaces soul blocks with stone (like NoSlow).")
        .defaultValue(LanguidMode.Motion)
        .build()
    );

    public final Setting<Boolean> soulSand = sgGeneral.add(new BoolSetting.Builder()
        .name("soul-sand")
        .description("Whether or not soul sand slowdown will be removed.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> soulSoil = sgGeneral.add(new BoolSetting.Builder()
        .name("soul-soil")
        .description("Whether or not soul soil slowdown will be removed.")
        .defaultValue(true)
        .build()
    );

    // Constants
    private static final double SOUL_SAND_VELOCITY_MULTIPLIER = 0.4;
    private static final double COMPENSATION_FACTOR = 1.0 / SOUL_SAND_VELOCITY_MULTIPLIER; // 2.5

    public Languid() {
        super(Categories.Movement, "languid", "Removes the slowdown caused by soul sand and soul soil, inspired by Soul Speed III.");
    }

    /**
     * Check if the player is standing on soul sand.
     */
    public boolean isOnSoulSand() {
        if (!Utils.canUpdate() || mc.player == null) return false;
        BlockPos pos = mc.player.getBlockPos().down();
        Block block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.SOUL_SAND;
    }

    /**
     * Check if the player is standing on soul soil.
     */
    public boolean isOnSoulSoil() {
        if (!Utils.canUpdate() || mc.player == null) return false;
        BlockPos pos = mc.player.getBlockPos().down();
        Block block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.SOUL_SOIL;
    }

    /**
     * Check if the module should activate based on settings and block type.
     */
    public boolean shouldActivate() {
        if (!isActive()) return false;
        return (isOnSoulSand() && soulSand.get()) || (isOnSoulSoil() && soulSoil.get());
    }

    /**
     * Check if Motion mode is active and should apply compensation.
     */
    public boolean motionMode() {
        return isActive() && mode.get() == LanguidMode.Motion;
    }

    /**
     * Check if Emulate mode is active.
     */
    public boolean emulateMode() {
        return isActive() && mode.get() == LanguidMode.Emulate;
    }

    /**
     * Check if Replace mode is active.
     */
    public boolean replaceMode() {
        return isActive() && mode.get() == LanguidMode.Replace;
    }

    @Override
    public String getInfoString() {
        return mode.get().toString();
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.type != MovementType.SELF || !motionMode()) return;
        if (!shouldActivate()) return;

        // Apply compensation based on block type
        if (isOnSoulSand() && soulSand.get()) {
            // Soul sand has velocity multiplier of 0.4, so multiply by 2.5 to compensate
            double velX = event.movement.x * COMPENSATION_FACTOR;
            double velZ = event.movement.z * COMPENSATION_FACTOR;
            
            // Preserve Y velocity
            ((IVec3d) event.movement).florence$setXZ(velX, velZ);
        }
        // Note: Soul soil doesn't slow down (multiplier is 1.0), so no compensation needed
        // The Emulate mode can still apply Soul Speed III bonus to soul soil for speed boost
    }

    public enum LanguidMode {
        Motion,
        Emulate,
        Replace
    }
}
