/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.util.ApiServices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("currentFps")
    static int florence$getFps() {
        return 0;
    }

    @Mutable
    @Accessor("session")
    void florence$setSession(Session session);

    @Accessor("resourceReloadLogger")
    ResourceReloadLogger florence$getResourceReloadLogger();

    @Accessor("attackCooldown")
    int florence$getAttackCooldown();

    @Accessor("attackCooldown")
    void florence$setAttackCooldown(int attackCooldown);

    @Invoker("doAttack")
    boolean florence$leftClick();

    @Mutable
    @Accessor("profileKeys")
    void florence$setProfileKeys(ProfileKeys keys);

    @Mutable
    @Accessor("userApiService")
    void florence$setUserApiService(UserApiService apiService);

    @Mutable
    @Accessor("skinProvider")
    void florence$setSkinProvider(PlayerSkinProvider skinProvider);

    @Mutable
    @Accessor("socialInteractionsManager")
    void florence$setSocialInteractionsManager(SocialInteractionsManager socialInteractionsManager);

    @Mutable
    @Accessor("abuseReportContext")
    void florence$setAbuseReportContext(AbuseReportContext abuseReportContext);

    @Mutable
    @Accessor("gameProfileFuture")
    void florence$setGameProfileFuture(CompletableFuture<ProfileResult> future);

    @Mutable
    @Accessor("apiServices")
    void florence$setApiServices(ApiServices apiServices);
}
