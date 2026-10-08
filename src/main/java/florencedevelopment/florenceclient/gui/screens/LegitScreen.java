/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.CategoryColors;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import net.minecraft.client.gui.Click;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.screens.settings.ModuleListSettingScreen;
import florencedevelopment.florenceclient.gui.search.FuzzyMatcher;
import florencedevelopment.florenceclient.gui.tabs.Tab;
import florencedevelopment.florenceclient.gui.tabs.TabScreen;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.WFlorenceLegitCard;
import florencedevelopment.florenceclient.gui.themes.florence.widgets.WFlorenceSidebarButton;
import florencedevelopment.florenceclient.gui.widgets.WLabel;
import florencedevelopment.florenceclient.gui.widgets.containers.WHorizontalList;
import florencedevelopment.florenceclient.gui.widgets.containers.WVerticalList;
import florencedevelopment.florenceclient.gui.widgets.containers.WView;
import florencedevelopment.florenceclient.gui.widgets.input.WTextBox;
import florencedevelopment.florenceclient.gui.widgets.pressable.WButton;
import florencedevelopment.florenceclient.gui.widgets.pressable.WCheckbox;
import florencedevelopment.florenceclient.settings.ModuleListSetting;
import florencedevelopment.florenceclient.systems.modules.Category;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static florencedevelopment.florenceclient.FlorenceClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * The Legit tab: the modules that are fine to use anywhere (visual and quality of life ones). A list of categories down
 * the left side and a grid of modules next to it, in the style of Lunar Client. Options of a module open as a page in
 * the same window. What is in here is not in the Modules tab, and can be changed with the Edit button.
 */
public class LegitScreen extends TabScreen {
    private WTextBox search;
    private WLabel count;
    private WVerticalList content;
    private WVerticalList settingsContainer;

    // What is shown: null is all categories
    private String selectedCategory;
    // The module whose options are open, null for the grid
    private Module opened;

    // Set when the list of legit modules was changed in the Edit screen
    private boolean changed;

    public LegitScreen(GuiTheme theme, Tab tab) {
        super(theme, tab);
    }

    @Override
    public void initWidgets() {
        if (!(theme instanceof FlorenceGuiTheme)) {
            addDirect(theme.label("The Legit tab needs the Florence theme.")).center();
            return;
        }

        addDirect(new WPanel()).centerX().top().marginTop(64);

        rebuildContent();
    }

    @Override
    protected void init() {
        super.init();

        // Back from the Edit screen
        if (changed) {
            changed = false;
            reload();
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Settings that depend on others show and hide
        if (opened != null && settingsContainer != null) opened.settings.tick(settingsContainer, theme);
    }

    // Actions

    private void edit() {
        List<Module> current = new ArrayList<>();
        for (Module module : Modules.get().getAll()) if (module.legit) current.add(module);

        ModuleListSetting setting = new ModuleListSetting.Builder()
            .name("legit-modules")
            .description("The modules shown in the Legit tab.")
            .onChanged(list -> {
                for (Module module : Modules.get().getAll()) module.legit = list.contains(module);
                changed = true;
            })
            .build();

        setting.get().addAll(current);

        mc.setScreen(new ModuleListSettingScreen(theme, setting));
    }

    private void showCategory(String category) {
        selectedCategory = category;
        opened = null;

        taskAfterRender = this::rebuildContent;
    }

    private void openOptions(Module module) {
        opened = module;

        taskAfterRender = this::rebuildContent;
    }

    private void closeOptions() {
        opened = null;

        taskAfterRender = this::rebuildContent;
    }

    // Content

    private int columns() {
        double sidebar = theme.scale(150);
        double card = theme.scale(184);
        double gap = theme.scale(10);
        double available = Utils.getWindowWidth() * 0.92 - sidebar - theme.scale(70);

        return Math.max(2, Math.min(4, (int) ((available + gap) / (card + gap))));
    }

    private List<Module> visibleModules() {
        String query = search != null ? search.get().trim() : "";
        List<Module> modules = new ArrayList<>();

        for (Module module : Modules.get().getAll()) {
            if (!module.legit) continue;
            if (selectedCategory != null && !module.category.name.equals(selectedCategory)) continue;
            if (!query.isEmpty() && FuzzyMatcher.score(query, module.title) == FuzzyMatcher.NO_MATCH) continue;

            modules.add(module);
        }

        if (query.isEmpty()) modules.sort(Comparator.comparing((Module m) -> m.title));
        else modules.sort(Comparator.comparingInt((Module m) -> FuzzyMatcher.score(query, m.title)).reversed());

        return modules;
    }

    private void rebuildContent() {
        if (content == null) return;

        content.clear();
        settingsContainer = null;

        if (opened != null) buildOptions(opened);
        else buildGrid();
    }

    private void buildGrid() {
        List<Module> modules = visibleModules();
        String query = search != null ? search.get().trim() : "";

        count.set(modules.size() + (modules.size() == 1 ? " module" : " modules"));

        WView view = content.add(theme.view()).expandX().widget();
        view.maxHeight = Utils.getWindowHeight() * 0.62;

        WVerticalList grid = view.add(theme.verticalList()).widget();
        grid.spacing = 10;

        if (modules.isEmpty()) {
            grid.add(theme.label(query.isEmpty() ? "Nothing here yet, press Edit to add modules." : "No modules match.")).pad(12);
            return;
        }

        int columns = columns();

        for (int i = 0; i < modules.size(); i += columns) {
            WHorizontalList row = grid.add(theme.horizontalList()).widget();
            row.spacing = 10;

            for (int j = i; j < Math.min(i + columns, modules.size()); j++) {
                Module module = modules.get(j);
                row.add(new WFlorenceLegitCard(module, () -> openOptions(module)));
            }
        }
    }

    // Width of the grid in the units the GUI is laid out in, the options page is as wide
    private double contentUnits() {
        int columns = columns();

        return columns * 184 + (columns - 1) * 10;
    }

    private void buildOptions(Module module) {
        count.set(module.title);

        content.add(new WOptionsHeader(module));

        // The settings sit in a rounded pocket
        WPocket pocket = content.add(new WPocket()).widget();
        pocket.minWidth = contentUnits();

        WView view = pocket.add(theme.view()).expandX().pad(8).widget();
        view.maxHeight = Utils.getWindowHeight() * 0.44;

        module.settings.onActivated();

        settingsContainer = view.add(theme.verticalList()).expandX().widget();
        settingsContainer.add(theme.settings(module.settings)).expandX();
    }

    private class WPocket extends WVerticalList {
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme florence = (FlorenceGuiTheme) theme;
            double line = Math.max(1, Math.round(florence.scale(1)));

            renderer.roundRect(x, y, width, height, florence.radiusMedium() * 1.3, florence.design().card, line, florence.design().outline);
        }
    }

    /**
     * The top of a module's options: back button, icon, name, description and the bar that turns the module on and off.
     */
    private class WOptionsHeader extends WWidget {
        private final Module module;
        private final Interaction backUi = new Interaction();
        private final Interaction barUi = new Interaction();

        WOptionsHeader(Module module) {
            this.module = module;
        }

        private FlorenceGuiTheme florence() {
            return (FlorenceGuiTheme) theme;
        }

        @Override
        protected void onCalculateSize() {
            width = theme.scale(contentUnits());
            height = theme.scale(60);
        }

        private double buttonHeight() {
            return theme.textHeight() + florence().space(10);
        }

        private double backWidth() {
            return theme.textWidth("Back") + florence().space(34);
        }

        private double barWidth() {
            return theme.scale(130);
        }

        private boolean over(double mx, double my, double rx, double ry, double rw, double rh) {
            return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
        }

        private double buttonY() {
            return y + (height - buttonHeight()) / 2;
        }

        @Override
        public boolean onMouseClicked(Click click, boolean doubled) {
            if (!mouseOver || click.button() != GLFW_MOUSE_BUTTON_LEFT) return false;

            if (over(click.x(), click.y(), x, buttonY(), backWidth(), buttonHeight())) {
                closeOptions();
                return true;
            }

            if (over(click.x(), click.y(), x + width - barWidth(), buttonY(), barWidth(), buttonHeight())) {
                module.toggle();
                return true;
            }

            return false;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme florence = florence();
            Design d = florence.design();

            backUi.update(mouseOver && over(mouseX, mouseY, x, buttonY(), backWidth(), buttonHeight()), false, delta);
            barUi.update(mouseOver && over(mouseX, mouseY, x + width - barWidth(), buttonY(), barWidth(), buttonHeight()), false, delta);

            double th = theme.textHeight();
            double line = Math.max(1, Math.round(florence.scale(1)));
            double bh = buttonHeight();
            double by = buttonY();
            int category = CategoryColors.of(module.category.name);
            boolean on = module.isActive();

            // Back
            double bw = backWidth();
            int backColor = Colors.lerp(d.textSecondary, d.text, backUi.hover());

            renderer.roundRect(x, by, bw, bh, florence.radiusMedium(), Colors.lerp(d.field, d.fieldHover, backUi.hover()), line, Colors.lerp(d.outline, Colors.withAlpha(d.accent, 170), backUi.hover()));
            chevron(renderer, x + florence.space(14), by + bh / 2, th * 0.5, 90, Math.max(1.5, florence.scale(2)), backColor);
            renderer.text("Back", x + florence.space(26), by + (bh - th) / 2, backColor, false);

            // Icon
            double tile = theme.scale(44);
            double tx = x + bw + florence.space(14);
            double ty = y + (height - tile) / 2;

            renderer.roundRect(tx, ty, tile, tile, florence.radiusMedium() * 1.3, Colors.withAlpha(category, on ? 70 : 38), line, Colors.withAlpha(category, 120));

            int iconColor = Colors.lerp(Colors.lighten(category, 0.3), Colors.rgb(255, 255, 255), on ? 0.55 : 0);

            if (!LegitIconPainter.draw(renderer, module.name, tx + tile / 2, ty + tile / 2, tile * 0.6, iconColor)) {
                String letter = module.title.isEmpty() ? "?" : module.title.substring(0, 1).toUpperCase();
                renderer.text(letter, tx + (tile - theme.textWidth(letter)) / 2, ty + (tile - th) / 2, iconColor, false);
            }

            // Name and description
            double textX = tx + tile + florence.space(14);
            double textSpace = x + width - barWidth() - florence.space(16) - textX;

            renderer.text(fit(module.title, textSpace), textX, y + height / 2 - th * 1.05, d.text, false);
            renderer.text(fit(module.description, textSpace), textX, y + height / 2 + th * 0.05, d.textSecondary, false);

            // Enabled / disabled bar
            int barColor = on ? d.success : d.danger;
            double barX = x + width - barWidth();

            renderer.roundRect(barX, by, barWidth(), bh, florence.radiusMedium(), Colors.lerp(Colors.withAlpha(barColor, 205), barColor, barUi.hover()), line, Colors.withAlpha(Colors.lighten(barColor, 0.3), 120));

            String state = on ? "ENABLED" : "DISABLED";
            renderer.text(state, barX + (barWidth() - theme.textWidth(state)) / 2, by + (bh - th) / 2, Colors.rgb(255, 255, 255), false);
        }

        private void chevron(GuiRenderer renderer, double cx, double cy, double size, double rotation, double thickness, int color) {
            double rad = Math.toRadians(rotation);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);
            double hw = size / 2;
            double hh = size / 4;

            double lx = cx + (-hw) * cos - (-hh) * sin;
            double ly = cy + (-hw) * sin + (-hh) * cos;
            double tipX = cx - hh * sin;
            double tipY = cy + hh * cos;
            double rx = cx + hw * cos - (-hh) * sin;
            double ry = cy + hw * sin + (-hh) * cos;

            renderer.line(lx, ly, tipX, tipY, thickness, color);
            renderer.line(tipX, tipY, rx, ry, thickness, color);
        }

        private String fit(String text, double space) {
            if (space <= 0) return "";
            if (theme.textWidth(text) <= space) return text;

            double dots = theme.textWidth("...");
            int end = text.length();

            while (end > 0 && theme.textWidth(text.substring(0, end)) + dots > space) end--;

            return end <= 0 ? "..." : text.substring(0, end).stripTrailing() + "...";
        }
    }

    // Widgets

    private class WPanel extends WVerticalList {
        @Override
        public void init() {
            spacing = 8;

            // Header
            WHorizontalList header = add(theme.horizontalList()).expandX().pad(12).widget();
            header.spacing = 10;

            header.add(theme.label("Legit", true)).centerY();
            count = header.add(theme.label("")).centerY().widget();
            count.color = theme.textSecondaryColor();

            search = header.add(theme.textBox("", "Search")).expandCellX().right().centerY().widget();
            search.minWidth = 200;
            search.action = () -> {
                opened = null;
                rebuildContent();
            };

            WButton edit = header.add(theme.button("Edit")).centerY().widget();
            edit.action = LegitScreen.this::edit;

            add(theme.horizontalSeparator()).expandX().padHorizontal(12);

            // Categories on the left, modules on the right
            WHorizontalList body = add(theme.horizontalList()).expandX().padHorizontal(12).padBottom(12).widget();
            body.spacing = 12;

            WVerticalList sidebar = body.add(theme.verticalList()).top().widget();
            sidebar.spacing = 4;

            buildSidebar(sidebar);

            body.add(theme.verticalSeparator()).expandWidgetY();

            content = body.add(theme.verticalList()).top().widget();
            content.spacing = 8;
        }

        // The panel keeps the size it has with all the modules showing, whatever is in it
        private double baseWidth, baseHeight, baseScreenWidth, baseScreenHeight;

        @Override
        protected void onCalculateSize() {
            super.onCalculateSize();

            if (baseScreenWidth != Utils.getWindowWidth() || baseScreenHeight != Utils.getWindowHeight()) {
                baseScreenWidth = Utils.getWindowWidth();
                baseScreenHeight = Utils.getWindowHeight();
                baseWidth = baseHeight = 0;
            }

            baseWidth = Math.max(baseWidth, width);
            baseHeight = Math.max(baseHeight, height);

            width = baseWidth;
            height = baseHeight;
        }

        private void buildSidebar(WVerticalList sidebar) {
            // How many legit modules every category has
            Map<String, Integer> counts = new LinkedHashMap<>();
            int total = 0;

            for (Category category : Modules.loopCategories()) {
                int n = 0;
                for (Module module : Modules.get().getGroup(category)) if (module.legit) n++;

                if (n > 0) counts.put(category.name, n);
                total += n;
            }

            int all = total;
            sidebar.add(new WFlorenceSidebarButton("All", all, florencedevelopment.florenceclient.gui.design.Colors.rgb(160, 168, 200), () -> selectedCategory == null, () -> showCategory(null)));

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                String name = entry.getKey();

                sidebar.add(new WFlorenceSidebarButton(name, entry.getValue(), CategoryColors.of(name), () -> name.equals(selectedCategory), () -> showCategory(name)));
            }
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme florence = (FlorenceGuiTheme) theme;

            double radius = florence.radiusLarge();
            double line = Math.max(1, Math.round(florence.scale(1)));

            if (florence.shadows()) renderer.shadow(x, y + florence.scale(6), width, height, radius, florence.scale(28), florence.design().shadow);

            if (florence.glass() && renderer.hasBackdrop()) renderer.glass(x, y, width, height, radius, florence.design().panel, line, florence.design().outline);
            else renderer.roundRect(x, y, width, height, radius, florence.design().panelSolid, line, florence.design().outline);
        }
    }
}
