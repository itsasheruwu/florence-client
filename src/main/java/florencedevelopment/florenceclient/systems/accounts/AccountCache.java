/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.accounts;

import com.mojang.util.UndashedUuid;
import florencedevelopment.florenceclient.utils.misc.ISerializable;
import florencedevelopment.florenceclient.utils.misc.NbtException;
import florencedevelopment.florenceclient.utils.render.PlayerHeadTexture;
import florencedevelopment.florenceclient.utils.render.PlayerHeadUtils;
import net.minecraft.nbt.NbtCompound;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class AccountCache implements ISerializable<AccountCache> {
    public String username = "";
    public String uuid = "";
    private PlayerHeadTexture headTexture;

    public PlayerHeadTexture getHeadTexture() {
        return headTexture != null ? headTexture : PlayerHeadUtils.STEVE_HEAD;
    }

    public void loadHead() {
        if (uuid == null || uuid.isBlank()) return;
        mc.execute(() -> headTexture = PlayerHeadUtils.fetchHead(UndashedUuid.fromStringLenient(uuid)));
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("username", username);
        tag.putString("uuid", uuid);

        return tag;
    }

    @Override
    public AccountCache fromTag(NbtCompound tag) {
        if (tag.getString("username").isEmpty() || tag.getString("uuid").isEmpty()) throw new NbtException();

        username = tag.getString("username").get();
        uuid = tag.getString("uuid").get();
        loadHead();

        return this;
    }
}
