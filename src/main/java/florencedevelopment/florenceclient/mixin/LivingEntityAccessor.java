/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("swimUpward")
    void florence$swimUpwards(TagKey<Fluid> fluid);

    @Accessor("jumping")
    boolean florence$isJumping();

    @Accessor("jumpingCooldown")
    int florence$getJumpCooldown();

    @Accessor("jumpingCooldown")
    void florence$setJumpCooldown(int cooldown);
}
