/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens.settings;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WindowScreen;
import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.widgets.WLabel;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WTable;
import florencedevelopment.florenceclient.gui.widgets.containers.WView;
import florencedevelopment.florenceclient.gui.widgets.input.WDropdown;
import florencedevelopment.florenceclient.gui.widgets.input.WTextBox;
import florencedevelopment.florenceclient.gui.widgets.pressable.WButton;
import florencedevelopment.florenceclient.renderer.Fonts;
import florencedevelopment.florenceclient.renderer.text.FontFamily;
import florencedevelopment.florenceclient.renderer.text.FontInfo;
import florencedevelopment.florenceclient.settings.FontFaceSetting;
import org.apache.commons.lang3.Strings;

import java.util.List;

public class FontFaceSettingScreen extends WindowScreen {
    private final FontFaceSetting setting;

    private WTable table;

    private WTextBox filter;
    private String filterText = "";

    public FontFaceSettingScreen(GuiTheme theme, FontFaceSetting setting) {
        super(theme, "Select Font");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        filter = add(theme.textBox("")).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        window.view.hasScrollBar = false;

        enterAction = () -> {
            List<Cell<?>> row = table.getRow(0);
            if (row == null) return;

            WWidget widget = row.get(2).widget();
            if (widget instanceof WButton button) {
                button.action.run();
            }
        };

        WView view = add(theme.view()).expandX().widget();
        view.scrollOnlyWhenMouseOver = false;
        table = view.add(theme.table()).expandX().widget();

        initTable();
    }

    private void initTable() {
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            String name = fontFamily.getName();

            WLabel item = theme.label(name);
            if (!filterText.isEmpty() && !Strings.CI.contains(name, filterText)) continue;
            table.add(item);

            WDropdown<FontInfo.Type> dropdown = table.add(theme.dropdown(FontInfo.Type.Regular)).right().widget();

            WButton select = table.add(theme.button("Select")).expandCellX().right().widget();
            select.action = () -> {
                setting.set(fontFamily.get(dropdown.get()));
                close();
            };

            table.row();
        }
    }
}
