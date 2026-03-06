/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.mojang.authlib.GameProfile;
import florencedevelopment.florenceclient.mixininterface.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ChatHudLine.class)
public abstract class ChatHudLineMixin implements IChatHudLine {
    @Shadow @Final private Text content;
    @Unique private int id;
    @Unique private GameProfile sender;

    @Override
    public String florence$getText() {
        return content.getString();
    }

    @Override
    public int florence$getId() {
        return id;
    }

    @Override
    public void florence$setId(int id) {
        this.id = id;
    }

    @Override
    public GameProfile florence$getSender() {
        return sender;
    }

    @Override
    public void florence$setSender(GameProfile profile) {
        sender = profile;
    }
}
