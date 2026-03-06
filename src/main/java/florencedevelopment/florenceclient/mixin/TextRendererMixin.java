/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.render.NoRender;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {
    @ModifyExpressionValue(method = "getGlyph", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isObfuscated()Z"))
    private boolean onRenderObfuscatedStyle(boolean original) {
        if (Modules.get() == null || Modules.get().get(NoRender.class) == null) {
            return original;
        }
        return !Modules.get().get(NoRender.class).noObfuscation() && original;
    }
}
