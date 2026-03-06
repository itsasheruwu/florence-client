/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.movement.Anchor;
import florencedevelopment.florenceclient.systems.modules.movement.Scaffold;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntity {
    protected ServerPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void dontJump(CallbackInfo ci) {
        if (!getEntityWorld().isClient()) return;

        Anchor module = Modules.get().get(Anchor.class);
        if (module.isActive() && module.cancelJump) ci.cancel();
        else if (Modules.get().get(Scaffold.class).towering()) ci.cancel();
    }
}
