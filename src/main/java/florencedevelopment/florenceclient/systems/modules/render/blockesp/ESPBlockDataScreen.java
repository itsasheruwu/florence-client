/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.render.blockesp;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WindowScreen;
import florencedevelopment.florenceclient.renderer.ShapeMode;
import florencedevelopment.florenceclient.settings.*;
import florencedevelopment.florenceclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import org.jetbrains.annotations.Nullable;

public class ESPBlockDataScreen extends WindowScreen {
    private final ESPBlockData blockData;
    private final Setting<?> setting;
    private final @Nullable Runnable firstChangeConsumer;

    public ESPBlockDataScreen(GuiTheme theme, ESPBlockData blockData, Block block, BlockDataSetting<ESPBlockData> setting) {
        this(theme, blockData, setting, () -> setting.get().put(block, blockData));
    }

    public ESPBlockDataScreen(GuiTheme theme, ESPBlockData blockData, GenericSetting<ESPBlockData> setting) {
        this(theme, blockData, setting, null);
    }

    private ESPBlockDataScreen(GuiTheme theme, ESPBlockData blockData, Setting<?> setting, @Nullable Runnable firstChangeConsumer) {
        super(theme, "Configure Block");

        this.blockData = blockData;
        this.setting = setting;
        this.firstChangeConsumer = firstChangeConsumer;
    }

    @Override
    public void initWidgets() {
        Settings settings = new Settings();
        SettingGroup sgGeneral = settings.getDefaultGroup();
        SettingGroup sgTracer = settings.createGroup("Tracer");

        sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shape is rendered.")
            .defaultValue(ShapeMode.Lines)
            .onModuleActivated(shapeModeSetting -> shapeModeSetting.set(blockData.shapeMode))
            .onChanged(shapeMode -> {
                if (blockData.shapeMode != shapeMode) {
                    blockData.shapeMode = shapeMode;
                    onChanged();
                }
            })
            .build()
        );

        sgGeneral.add(new ColorSetting.Builder()
            .name("line-color")
            .description("Color of lines.")
            .defaultValue(new SettingColor(0, 255, 200))
            .onModuleActivated(settingColorSetting -> settingColorSetting.get().set(blockData.lineColor))
            .onChanged(settingColor -> {
                if (!blockData.lineColor.equals(settingColor)) {
                    blockData.lineColor.set(settingColor);
                    onChanged();
                }
            })
            .build()
        );

        sgGeneral.add(new ColorSetting.Builder()
            .name("side-color")
            .description("Color of sides.")
            .defaultValue(new SettingColor(0, 255, 200, 25))
            .onModuleActivated(settingColorSetting -> settingColorSetting.get().set(blockData.sideColor))
            .onChanged(settingColor -> {
                if (!blockData.sideColor.equals(settingColor)) {
                    blockData.sideColor.set(settingColor);
                    onChanged();
                }
            })
            .build()
        );

        sgTracer.add(new BoolSetting.Builder()
            .name("tracer")
            .description("If tracer line is allowed to this block.")
            .defaultValue(true)
            .onModuleActivated(booleanSetting -> booleanSetting.set(blockData.tracer))
            .onChanged(aBoolean -> {
                if (blockData.tracer != aBoolean) {
                    blockData.tracer = aBoolean;
                    onChanged();
                }
            })
            .build()
        );

        sgTracer.add(new ColorSetting.Builder()
            .name("tracer-color")
            .description("Color of tracer line.")
            .defaultValue(new SettingColor(0, 255, 200, 125))
            .onModuleActivated(settingColorSetting -> settingColorSetting.get().set(blockData.tracerColor))
            .onChanged(settingColor -> {
                if (!blockData.tracerColor.equals(settingColor)) {
                    blockData.tracerColor.set(settingColor);
                    onChanged();
                }
            })
            .build()
        );

        settings.onActivated();
        add(theme.settings(settings)).expandX();
    }

    private void onChanged() {
        if (!blockData.isChanged() && firstChangeConsumer != null) {
            firstChangeConsumer.run();
        }

        setting.onChanged();
        blockData.changed();
    }
}
