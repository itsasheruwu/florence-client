/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.accounts;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import florencedevelopment.florenceclient.mixin.FileCacheAccessor;
import florencedevelopment.florenceclient.mixin.MinecraftClientAccessor;
import florencedevelopment.florenceclient.mixin.PlayerSkinProviderAccessor;
import florencedevelopment.florenceclient.utils.misc.ISerializable;
import florencedevelopment.florenceclient.utils.misc.NbtException;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Util;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public abstract class Account<T extends Account<?>> implements ISerializable<T> {
    protected AccountType type;
    protected String name;

    protected final AccountCache cache;

    protected Account(AccountType type, String name) {
        this.type = type;
        this.name = name;
        this.cache = new AccountCache();
    }

    public abstract boolean fetchInfo();

    public boolean login() {
        YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(mc.getNetworkProxy());
        applyLoginEnvironment(authenticationService);

        return true;
    }

    public String getUsername() {
        if (cache.username.isEmpty()) return name;
        return cache.username;
    }

    public AccountType getType() {
        return type;
    }

    public AccountCache getCache() {
        return cache;
    }

    public static void setSession(Session session) {
        MinecraftClientAccessor mca = (MinecraftClientAccessor) mc;
        mca.florence$setSession(session);

        YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(mc.getNetworkProxy());

        UserApiService apiService = yggdrasilAuthenticationService.createUserApiService(session.getAccessToken());
        mca.florence$setUserApiService(apiService);
        mca.florence$setSocialInteractionsManager(new SocialInteractionsManager(mc, apiService));
        mca.florence$setProfileKeys(ProfileKeys.create(apiService, session, mc.runDirectory.toPath()));
        mca.florence$setAbuseReportContext(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
        mca.florence$setGameProfileFuture(CompletableFuture.supplyAsync(() -> mc.getApiServices().sessionService().fetchProfile(mc.getSession().getUuidOrNull(), true), Util.getIoWorkerExecutor()));
    }

    public static void applyLoginEnvironment(YggdrasilAuthenticationService authService) {
        MinecraftClientAccessor mca = (MinecraftClientAccessor) mc;
        SignatureVerifier.create(authService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
        PlayerSkinProvider.FileCache skinCache = ((PlayerSkinProviderAccessor) mc.getSkinProvider()).florence$getSkinCache();
        Path skinCachePath = ((FileCacheAccessor) skinCache).florence$getDirectory();
        mca.florence$setApiServices(ApiServices.create(authService, mc.runDirectory));
        mca.florence$setSkinProvider(new PlayerSkinProvider(skinCachePath, mc.getApiServices(), new PlayerSkinTextureDownloader(mc.getNetworkProxy(), mc.getTextureManager(), mc), mc));
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("type", type.name());
        tag.putString("name", name);
        tag.put("cache", cache.toTag());

        return tag;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T fromTag(NbtCompound tag) {
        if (tag.getString("name").isEmpty() || tag.getCompound("cache").isEmpty()) throw new NbtException();

        name = tag.getString("name").get();
        cache.fromTag(tag.getCompound("cache").get());

        return (T) this;
    }
}
