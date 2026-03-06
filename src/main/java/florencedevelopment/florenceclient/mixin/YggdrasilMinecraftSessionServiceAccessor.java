/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.mojang.authlib.Environment;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.net.Proxy;

@Mixin(YggdrasilMinecraftSessionService.class)
public interface YggdrasilMinecraftSessionServiceAccessor {
    @Invoker("<init>")
    static YggdrasilMinecraftSessionService florence$createYggdrasilMinecraftSessionService(final ServicesKeySet servicesKeySet, final Proxy proxy, final Environment env) {
        throw new UnsupportedOperationException();
    }
}
