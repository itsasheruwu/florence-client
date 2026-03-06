/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.commands.Commands;
import florencedevelopment.florenceclient.systems.config.Config;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.movement.GUIMove;
import florencedevelopment.florenceclient.systems.modules.render.NoRender;
import florencedevelopment.florenceclient.utils.Utils;
import florencedevelopment.florenceclient.utils.misc.text.FlorenceClickEvent;
import florencedevelopment.florenceclient.utils.misc.text.RunnableClickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.minecraft.client.util.InputUtil.*;

@Mixin(value = Screen.class, priority = 500) // needs to be before baritone
public abstract class ScreenMixin {
    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void onRenderInGameBackground(CallbackInfo info) {
        if (Utils.canUpdate() && Modules.get().get(NoRender.class).noGuiBackground())
            info.cancel();
    }

    @Inject(method = "handleClickEvent", at = @At(value = "HEAD"))
    private static void onHandleClickEvent(ClickEvent clickEvent, MinecraftClient client, Screen screenAfterRun, CallbackInfo ci) {
        if (!(clickEvent instanceof RunnableClickEvent runnableClickEvent)) return;

        runnableClickEvent.runnable.run();
    }

    @Inject(method = "handleBasicClickEvent", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private static void onHandleBasicClickEvent(ClickEvent clickEvent, MinecraftClient client, Screen screen, CallbackInfo ci) {
        if (clickEvent instanceof FlorenceClickEvent FlorenceClickEvent && FlorenceClickEvent.value.startsWith(Config.get().prefix.get())) {
            try {
                Commands.dispatch(FlorenceClickEvent.value.substring(Config.get().prefix.get().length()));
            } catch (CommandSyntaxException e) {
                FlorenceClient.LOG.error("Failed to run command", e);
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) (this) instanceof ChatScreen) return;
        GUIMove guiMove = Modules.get().get(GUIMove.class);
        List<Integer> arrows = List.of(GLFW_KEY_RIGHT, GLFW_KEY_LEFT, GLFW_KEY_DOWN,  GLFW_KEY_UP);
        if ((guiMove.disableArrows() && arrows.contains(input.key())) || (guiMove.disableSpace() && input.key() == GLFW_KEY_SPACE)) {
            cir.setReturnValue(true);
        }
    }
}
