/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.world;

import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.settings.BoolSetting;
import florencedevelopment.florenceclient.settings.DoubleSetting;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.utils.player.FindItemResult;
import florencedevelopment.florenceclient.utils.player.InvUtils;
import florencedevelopment.florenceclient.utils.player.PlayerUtils;
import florencedevelopment.florenceclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class AutoShearer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("distance")
        .description("The maximum distance the sheep have to be to be sheared.")
        .min(0.0)
        .defaultValue(5.0)
        .build()
    );

    private final Setting<Boolean> antiBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-break")
        .description("Prevents shears from being broken.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Automatically faces towards the animal being sheared.")
        .defaultValue(true)
        .build()
    );

    private Entity entity;
    private Hand hand;

    public AutoShearer() {
        super(Categories.World, "auto-shearer", "Automatically shears sheep.");
    }

    @Override
    public void onDeactivate() {
        entity = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        entity = null;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof SheepEntity) || ((SheepEntity) entity).isSheared() || ((SheepEntity) entity).isBaby() || !PlayerUtils.isWithin(entity, distance.get())) continue;

            FindItemResult findShear = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == Items.SHEARS && (!antiBreak.get() || itemStack.getDamage() < itemStack.getMaxDamage() - 1));
            if (!InvUtils.swap(findShear.slot(), true)) return;

            this.hand = findShear.getHand();
            this.entity = entity;

            if (rotate.get()) Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, this::interact);
            else interact();

            return;
        }
    }

    private void interact() {
        EntityHitResult location = new EntityHitResult(entity, entity.getBoundingBox().getCenter());
        mc.interactionManager.interactEntityAtLocation(mc.player, entity, location, hand);
        mc.interactionManager.interactEntity(mc.player, entity, hand);
        InvUtils.swapBack();
    }
}
