/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixininterface;

import com.mojang.authlib.GameProfile;

public interface IChatHudLine {
    String florence$getText();

    int florence$getId();

    void florence$setId(int id);

    GameProfile florence$getSender();

    void florence$setSender(GameProfile profile);
}
