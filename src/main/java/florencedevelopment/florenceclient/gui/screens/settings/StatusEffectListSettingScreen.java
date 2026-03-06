/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens.settings;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.screens.settings.base.CollectionListSettingScreen;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.utils.misc.Names;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.List;
import java.util.Optional;

public class StatusEffectListSettingScreen extends CollectionListSettingScreen<StatusEffect> {
    public StatusEffectListSettingScreen(GuiTheme theme, Setting<List<StatusEffect>> setting) {
        super(theme, "Select Effects", setting, setting.get(), Registries.STATUS_EFFECT);
    }

    @Override
    protected WWidget getValueWidget(StatusEffect value) {
        return theme.itemWithLabel(getPotionStack(value), Names.get(value));
    }

    @Override
    protected String[] getValueNames(StatusEffect value) {
        return new String[]{
            Names.get(value),
            Registries.STATUS_EFFECT.getId(value).toString()
        };
    }

    private ItemStack getPotionStack(StatusEffect effect) {
        ItemStack potion = Items.POTION.getDefaultStack();

        potion.set(
            DataComponentTypes.POTION_CONTENTS,
            new PotionContentsComponent(
                potion.get(DataComponentTypes.POTION_CONTENTS).potion(),
                Optional.of(effect.getColor()),
                potion.get(DataComponentTypes.POTION_CONTENTS).customEffects(),
                Optional.empty()
            )
        );

        return potion;
    }
}
