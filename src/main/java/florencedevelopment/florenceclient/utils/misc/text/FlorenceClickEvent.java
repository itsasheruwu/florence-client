/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.misc.text;

import florencedevelopment.florenceclient.mixin.ScreenMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class does nothing except ensure that {@link ClickEvent}'s containing Florence Client commands can only be executed if they come from the client.
 * @see ScreenMixin#onHandleBasicClickEvent(ClickEvent, MinecraftClient, Screen, CallbackInfo)
 */
public class FlorenceClickEvent implements ClickEvent {
    public final String value;

    public FlorenceClickEvent(String value) {
        this.value = value;
    }

    @Override
    public Action getAction() {
        return Action.RUN_COMMAND;
    }
}
