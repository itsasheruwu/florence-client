/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Easing;
import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.animation.Spring;
import florencedevelopment.florenceclient.gui.design.CategoryColors;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceExpandableSettingsWidgetFactory;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WContainer;
import florencedevelopment.florenceclient.gui.widgets.containers.WVerticalList;
import florencedevelopment.florenceclient.gui.widgets.pressable.WPressable;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import net.minecraft.client.gui.Click;

import static florencedevelopment.florenceclient.FlorenceClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * A module in the click GUI: a row with a switch to turn it on and off that opens up to show its settings.
 * <p>
 * Left click turns the module on or off, right click (or the arrow) shows the settings and Shift + right click binds it
 * to a key.
 */
public class WFlorenceModule extends WContainer implements FlorenceWidget {
    private final Module module;
    private final String title;
    private final boolean canExpand;

    private FlorenceExpandableSettingsWidgetFactory settingsFactory;
    private WModuleHeader header;
    private WSettingsContainer settingsContainer;

    private boolean expanded;
    private final AnimatedFloat expand = new AnimatedFloat(0);
    private double lastExpand;

    // Whether any setting of the module has been changed from its default
    private boolean modified;

    public WFlorenceModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.canExpand = hasSettings(module);
        this.tooltip = module.description;
    }

    private static boolean hasSettings(Module module) {
        for (SettingGroup group : module.settings.groups) {
            for (Setting<?> ignored : group) {
                return true;
            }
        }

        return false;
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void init() {
        settingsFactory = new FlorenceExpandableSettingsWidgetFactory(theme);

        header = add(new WModuleHeader()).expandX().widget();
        settingsContainer = add(new WSettingsContainer()).expandX().widget();

        updateModified();
    }

    // Layout

    private double inset() {
        return theme().space(4);
    }

    @Override
    protected void onCalculateSize() {
        header.onCalculateSize();

        double settingsHeight = 0;

        if (settingsContainer.isBuilt()) {
            settingsContainer.onCalculateSize();

            settingsHeight = settingsContainer.height * expand.get();
        }

        // The settings fit in the width the window already has
        width = header.width;
        height = header.height + settingsHeight;
    }

    @Override
    protected void onCalculateWidgetPositions() {
        Cell<?> headerCell = cells.get(0);
        headerCell.x = x;
        headerCell.y = y;
        headerCell.width = width;
        headerCell.height = header.height;
        headerCell.alignWidget();

        Cell<?> settingsCell = cells.get(1);

        if (settingsContainer.isBuilt() && expand.get() > 0) {
            double inset = inset();

            settingsCell.x = x + inset;
            settingsCell.y = y + header.height;
            settingsCell.width = width - inset * 2;
            settingsCell.height = settingsContainer.height;
            settingsCell.alignWidget();

            settingsContainer.x = settingsCell.x;
            settingsContainer.y = settingsCell.y;
            settingsContainer.width = settingsCell.width;
            settingsContainer.height = settingsCell.height;
        }
        else {
            settingsCell.x = x;
            settingsCell.y = y + header.height;
            settingsCell.width = 0;
            settingsCell.height = 0;
        }
    }

    public void setExpanded(boolean expanded) {
        if (!canExpand || this.expanded == expanded) return;

        this.expanded = expanded;

        if (expanded) settingsContainer.build();
        expand.animateTo(expanded ? 1 : 0, 0.24);

        relayout();
    }

    private void relayout() {
        WWidget widget = this;

        while (widget != null) {
            widget.invalidate();
            widget = widget.parent;
        }
    }

    // Rendering

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        // Animate the settings opening, the rows below slide along because the height changes
        double amount = expand.update(delta);

        if (amount != lastExpand) {
            lastExpand = amount;
            relayout();
        }

        boolean active = module.isActive();
        double activeAmount = header.active.get();
        double hover = header.ui.hover();

        double inset = inset();
        double vInset = theme.space(1.5);

        double cx = x + inset;
        double cy = y + vInset;
        double cw = width - inset * 2;
        double ch = height - vInset * 2;

        // Row background
        int fill = Colors.lerp(d.card, d.cardHover, hover);
        fill = Colors.lerp(fill, d.cardPressed, header.ui.press());

        int border = Colors.lerp(0, d.outline, Math.max(amount, hover * 0.6));

        // Every category has a color of its own
        int categoryColor = CategoryColors.of(module.category.name);

        if (activeAmount > 0.01) {
            // Active modules get a tint of the category color that fades out to the right
            int left = Colors.lerp(fill, Colors.withAlpha(categoryColor, 64), activeAmount);
            int right = Colors.lerp(fill, Colors.withAlpha(categoryColor, 14), activeAmount);

            renderer.roundRectHorizontal(cx, cy, cw, ch, theme.radiusMedium(), left, right, lineWidth(), Colors.lerp(border, Colors.withAlpha(categoryColor, 110), activeAmount));
        }
        else {
            renderer.roundRect(cx, cy, cw, ch, theme.radiusMedium(), fill, lineWidth(), border);
        }

        // Bar on the left edge of active modules
        if (activeAmount > 0.01) {
            double barHeight = header.height * 0.5 * activeAmount;
            double barWidth = Math.max(3, theme.scale(3));

            renderer.roundRect(cx + theme.scale(1), y + header.height / 2 - barHeight / 2, barWidth, barHeight, barWidth / 2, categoryColor);
        }
    }

    // Updates

    public void tick() {
        updateModified();

        if (settingsContainer != null && settingsContainer.isBuilt() && expanded) {
            boolean needsRefresh = false;

            for (SettingGroup group : module.settings.groups) {
                for (Setting<?> setting : group) {
                    boolean visible = setting.isVisible();
                    if (visible != setting.lastWasVisible) {
                        needsRefresh = true;
                    }

                    setting.lastWasVisible = visible;
                }
            }

            if (needsRefresh) {
                settingsContainer.rebuildSettings();
                relayout();
            }
        }
    }

    private void updateModified() {
        boolean changed = false;

        for (SettingGroup group : module.settings.groups) {
            for (Setting<?> setting : group) {
                if (setting.wasChanged()) {
                    changed = true;
                    break;
                }
            }

            if (changed) break;
        }

        modified = changed;
    }

    // Widgets

    private class WModuleHeader extends WPressable {
        private final Interaction ui = new Interaction();
        private final AnimatedFloat active = new AnimatedFloat(0);
        private final AnimatedFloat starHover = new AnimatedFloat(0);

        // Title cut to fit, recalculated when the room for it changes
        private String shownTitle = "";
        private double shownTitleSpace = -1;

        // Positions of the things on the right, worked out by layoutRight()
        private double chevronX, chevronSize;
        private double switchX, switchWidth, switchHeight;
        private double starX, starSize;
        private double chipX, chipWidth;
        private double titleX, titleSpace;
        private String chipText;

        @Override
        public void init() {
            active.snap(module.isActive() ? 1 : 0);
        }

        @Override
        protected void onCalculateSize() {
            FlorenceGuiTheme theme = theme();

            height = theme.space(8) + theme.textHeight() + theme.space(8);

            // Room for most of the title and everything on the right of it, longer titles are cut with dots rather than
            // making the window wider. The row is stretched to the width of the window, which is at least this wide.
            double th = theme.textHeight();
            String chip = currentChipText();

            width = inset() * 2 + theme.space(14) + Math.min(theme.textWidth(title), theme.scale(140)) + theme.space(16)
                + (chip != null ? theme.textWidth(chip) + theme.space(12) + theme.space(8) : 0)
                + th * 0.9 + theme.space(4) + theme.space(8)
                + th * 0.82 * 1.8 + theme.space(8)
                + (canExpand ? th * 0.9 + theme.space(6) : 0)
                + theme.space(10);
        }

        private String currentChipText() {
            if (Modules.get().isBinding(module)) return "...";
            if (module.keybind.isSet()) return module.keybind.toString().toUpperCase();

            return null;
        }

        private void layoutRight() {
            FlorenceGuiTheme theme = theme();
            double th = theme.textHeight();

            double edge = x + width - inset() - theme.space(10);
            // The switch is at the far right, the arrow for the settings sits to the left of it
            switchHeight = th * 0.82;
            switchWidth = switchHeight * 1.8;
            switchX = edge - switchWidth;
            edge = switchX - theme.space(8);

            if (canExpand) {
                chevronSize = th * 0.9;
                chevronX = edge - chevronSize;
                edge = chevronX - theme.space(6);
            }
            else {
                chevronSize = 0;
                chevronX = edge;
            }

            starSize = th * 0.9;
            starX = edge - starSize;
            edge = starX - theme.space(4);

            chipText = currentChipText();
            chipWidth = 0;

            if (chipText != null) {
                chipWidth = theme.textWidth(chipText) + theme.space(12);
                chipX = edge - chipWidth;
                edge = chipX - theme.space(8);
            }

            titleX = x + inset() + theme.space(14);
            titleSpace = edge - titleX;
        }

        private boolean inside(double mouseX, double mouseY, double rx, double ry, double rw, double rh) {
            return mouseX >= rx && mouseX <= rx + rw && mouseY >= ry && mouseY <= ry + rh;
        }

        private boolean isOverChevron(double mouseX, double mouseY) {
            layoutRight();
            double pad = theme().space(5);

            return canExpand && inside(mouseX, mouseY, chevronX - pad, y, chevronSize + pad * 2, height);
        }

        private boolean isOverStar(double mouseX, double mouseY) {
            layoutRight();
            return inside(mouseX, mouseY, starX, y + (height - starSize) / 2, starSize, starSize);
        }

        private boolean isOverChip(double mouseX, double mouseY) {
            layoutRight();
            return chipText != null && inside(mouseX, mouseY, chipX, y, chipWidth, height);
        }

        @Override
        public boolean onMouseClicked(Click click, boolean doubled) {
            double mx = click.x();
            double my = click.y();

            if (mouseOver && !doubled) {
                int button = click.button();

                if (button == GLFW_MOUSE_BUTTON_LEFT || button == GLFW_MOUSE_BUTTON_RIGHT) {
                    if (isOverStar(mx, my)) {
                        module.setFavorite(!module.favorite);
                        if (mc.currentScreen instanceof florencedevelopment.florenceclient.gui.screens.ModulesScreen screen) screen.refreshFavorites();
                        return true;
                    }

                    if (isOverChevron(mx, my)) {
                        setExpanded(!expanded);
                        return true;
                    }

                    if (button == GLFW_MOUSE_BUTTON_LEFT && isOverChip(mx, my)) {
                        Modules.get().setModuleToBind(module);
                        return true;
                    }
                }

                if (button == GLFW_MOUSE_BUTTON_RIGHT && mc.isShiftPressed()) {
                    Modules.get().setModuleToBind(module);
                    return true;
                }
            }

            return super.onMouseClicked(click, doubled);
        }

        @Override
        protected void onPressed(int button) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                module.toggle();
            }
            else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                setExpanded(!expanded);
            }
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            Design d = design();

            ui.update(mouseOver, pressed, delta);

            boolean on = module.isActive();
            active.springTo(on ? 1 : 0, Spring.SNAPPY);
            double activeAmount = Math.max(0, Math.min(1, active.update(delta)));

            layoutRight();

            double th = theme.textHeight();
            double centerY = y + height / 2;

            // Title
            if (titleSpace != shownTitleSpace) {
                shownTitleSpace = titleSpace;
                shownTitle = ellipsize(title, titleSpace);
            }

            int titleColor = Colors.lerp(d.textSecondary, d.text, Math.max(activeAmount, ui.hover()));
            renderer.text(shownTitle, titleX, centerY - th / 2, titleColor, false);

            // Dot after the title if settings have been changed
            if (modified && !shownTitle.isEmpty()) {
                double dot = Math.max(4, theme.scale(5));
                double dotX = titleX + theme.textWidth(shownTitle) + theme.space(6);

                if (dotX + dot <= titleX + titleSpace) {
                    renderer.circle(dotX + dot / 2, centerY, dot / 2, Colors.withAlpha(d.accent2, 200));
                }
            }

            // Keybind
            if (chipText != null) {
                boolean binding = Modules.get().isBinding(module);
                double chipH = th * 0.95;
                double chipY = centerY - chipH / 2;

                int chipFill = binding ? Colors.withAlpha(d.accent, 60) : d.field;
                int chipBorder = binding ? d.accent : d.outline;

                renderer.roundRect(chipX, chipY, chipWidth, chipH, theme.radiusSmall(), chipFill, lineWidth(), chipBorder);
                renderer.text(chipText, chipX + (chipWidth - theme.textWidth(chipText)) / 2, centerY - th / 2, binding ? d.text : d.textSecondary, false);
            }

            // Favorite, only shown when it is set or the mouse is over the row
            boolean overStar = mouseOver && isOverStar(mouseX, mouseY);
            starHover.animateTo(overStar ? 1 : 0, 0.12);
            double sh = starHover.update(delta);

            double starAlpha = module.favorite ? 1 : ui.hover() * 0.9;

            if (starAlpha > 0.02) {
                int starColor = module.favorite
                    ? Colors.lerp(d.favorite, Colors.lighten(d.favorite, 0.35), sh)
                    : Colors.lerp(d.textDisabled, d.textSecondary, sh);

                renderer.icon(module.favorite ? GuiRenderer.FAVORITE_YES : GuiRenderer.FAVORITE_NO, starX, centerY - starSize / 2, starSize, starSize, Colors.mulAlpha(starColor, starAlpha));
            }

            // Switch
            double switchY = centerY - switchHeight / 2;
            int track = Colors.lerp(d.trackOff, Colors.lerp(d.accent, d.accentHover, ui.hover()), activeAmount);

            renderer.roundRect(switchX, switchY, switchWidth, switchHeight, switchHeight / 2, track);

            double gap = Math.max(2, switchHeight * 0.16);
            double knob = switchHeight - gap * 2;
            double travel = switchWidth - gap * 2 - knob;

            renderer.roundRect(switchX + gap + travel * active.get(), switchY + gap, knob, knob, knob / 2, d.thumb);

            // Arrow that turns down when the settings are open
            if (canExpand) {
                boolean overChevron = mouseOver && isOverChevron(mouseX, mouseY);
                int chevronColor = overChevron ? d.accent : Colors.lerp(d.textSecondary, d.text, ui.hover());

                chevron(renderer, chevronX + chevronSize / 2, centerY, chevronSize * 0.6, (expand.get() - 1) * 90, Math.max(1.5, theme.scale(2)), chevronColor);
            }
        }

        private String ellipsize(String text, double space) {
            FlorenceGuiTheme theme = theme();

            if (space <= 0) return "";
            if (theme.textWidth(text) <= space) return text;

            String dots = "...";
            double dotsWidth = theme.textWidth(dots);

            int end = text.length();
            while (end > 0 && theme.textWidth(text.substring(0, end)) + dotsWidth > space) end--;

            return end <= 0 ? dots : text.substring(0, end).stripTrailing() + dots;
        }
    }

    private class WSettingsContainer extends WVerticalList implements FlorenceWidget {
        private boolean built;

        boolean isBuilt() {
            return built;
        }

        void build() {
            if (built) return;

            built = true;
            rebuildSettings();
            spacing = theme.scale(1);
        }

        void rebuildSettings() {
            clear();

            if (!module.settings.groups.isEmpty()) {
                add(settingsFactory.create(theme, module.settings, "")).expandX().padHorizontal(10).padVertical(6);
            }
        }

        @Override
        public double pad() {
            return theme().space(8);
        }

        @Override
        protected void onCalculateSize() {
            super.onCalculateSize();
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double amount = expand.get();

            if (!built || amount <= 0.001) return true;

            FlorenceGuiTheme theme = theme();
            Design d = design();

            double visibleHeight = height * amount;
            double radius = theme.radiusMedium();

            // The settings sit in a darker pocket below the row
            renderer.roundRect(x, y, width, visibleHeight, 0, 0, radius, radius, Colors.withAlpha(0, d.light ? 14 : 70), 0, 0);

            renderer.scissorStart(x, y, width, visibleHeight);

            boolean result = super.render(renderer, mouseX, mouseY, delta);

            renderer.scissorEnd();

            return result;
        }
    }
}
