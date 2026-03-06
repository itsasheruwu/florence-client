/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixininterface;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

public interface IMessageHandler {
    /** Only valid inside of {@link net.minecraft.client.gui.hud.ChatHud#addMessage(Text, MessageSignatureData, MessageIndicator)} call */
    GameProfile florence$getSender();
}
