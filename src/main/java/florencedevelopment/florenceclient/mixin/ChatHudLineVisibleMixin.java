/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.mojang.authlib.GameProfile;
import florencedevelopment.florenceclient.mixininterface.IChatHudLineVisible;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public abstract class ChatHudLineVisibleMixin implements IChatHudLineVisible {
    @Shadow @Final private OrderedText content;
    @Unique private int id;
    @Unique private GameProfile sender;
    @Unique private boolean startOfEntry;

    @Override
    public String florence$getText() {
        StringBuilder sb = new StringBuilder();

        content.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });

        return sb.toString();
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

    @Override
    public boolean florence$isStartOfEntry() {
        return startOfEntry;
    }

    @Override
    public void florence$setStartOfEntry(boolean start) {
        startOfEntry = start;
    }
}
