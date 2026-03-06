/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("KEYS_BY_ID")
    static Map<String, KeyBinding> getKeysById() { return null; }

    @Accessor("boundKey")
    InputUtil.Key florence$getKey();

    @Accessor("timesPressed")
    int florence$getTimesPressed();

    @Accessor("timesPressed")
    void florence$setTimesPressed(int timesPressed);

    @Invoker("reset")
    void florence$invokeReset();
}
