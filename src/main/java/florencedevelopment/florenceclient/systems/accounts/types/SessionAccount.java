/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.accounts.types;

import com.mojang.util.UndashedUuid;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.systems.accounts.Account;
import florencedevelopment.florenceclient.systems.accounts.AccountType;
import florencedevelopment.florenceclient.systems.accounts.TokenAccount;
import florencedevelopment.florenceclient.utils.network.Http;
import net.minecraft.client.session.Session;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public class SessionAccount extends Account<SessionAccount> implements TokenAccount {
    private String accessToken;

    public SessionAccount(String label) {
        super(AccountType.Session, label);
        accessToken = label;
    }

    @Override
    public SessionAccount fromTag(NbtCompound tag) {
        super.fromTag(tag);

        accessToken = tag.getString("token", "");
        return this;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = super.toTag();
        tag.putString("token", accessToken);
        return tag;
    }

    @Override
    public boolean fetchInfo() {
        if (accessToken == null || accessToken.isBlank()) return false;

        ProfileResponse profile;
        try {
            profile = Http.get("https://api.minecraftservices.com/minecraft/profile")
                .bearer(accessToken)
                .sendJson(ProfileResponse.class);
        } catch (IllegalArgumentException e) {
            FlorenceClient.LOG.error("Invalid session account token", e);
            return false;
        }

        if (profile == null || profile.id == null || profile.name == null) return false;

        cache.username = profile.name;
        cache.uuid = profile.id;

        return true;
    }

    @Override
    public boolean login() {
        if (accessToken == null || accessToken.isBlank()) return false;

        super.login();
        cache.loadHead();

        setSession(new Session(cache.username, UndashedUuid.fromStringLenient(cache.uuid), accessToken, Optional.empty(), Optional.empty()));
        return true;
    }

    @Override
    public String getToken() {
        return accessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SessionAccount account2)) return false;
        return account2.name.equals(this.name);
    }

    private static class ProfileResponse {
        public String id;
        public String name;
    }
}
