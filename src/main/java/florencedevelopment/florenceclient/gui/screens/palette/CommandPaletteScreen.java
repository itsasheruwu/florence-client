/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens.palette;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WidgetScreen;
import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Spring;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.screens.ModulesScreen;
import florencedevelopment.florenceclient.gui.search.FuzzyMatcher;
import florencedevelopment.florenceclient.gui.tabs.Tab;
import florencedevelopment.florenceclient.gui.tabs.Tabs;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WVerticalList;
import florencedevelopment.florenceclient.gui.widgets.input.WTextBox;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.config.Config;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.utils.Utils;
import florencedevelopment.florenceclient.utils.render.color.Color;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static florencedevelopment.florenceclient.FlorenceClient.mc;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Search for anything in the client: modules, settings and tabs. Opened with Ctrl + K.
 * <p>
 * Up and down pick a result, Enter runs it (Shift + Enter keeps this open) and Escape closes it.
 */
public class CommandPaletteScreen extends WidgetScreen {
    private static final int MAX_ROWS = 8;
    private static final int MAX_MODULES = 8;
    private static final int MAX_SETTINGS = 4;

    private enum Kind {
        MODULE("Module"),
        SETTING("Setting"),
        TAB("Tab");

        private final String label;

        Kind(String label) {
            this.label = label;
        }
    }

    private record Entry(Kind kind, String title, String subtitle, int[] highlight, Module module, Runnable action) {}

    private final List<Entry> entries = new ArrayList<>();
    private int selected;
    private int firstShown;

    private WTextBox input;
    private WPaletteList list;

    public CommandPaletteScreen(GuiTheme theme) {
        super(theme, "Search");

        // Fade in even though another screen was open
        animProgress = 0;
    }

    @Override
    protected boolean canOpenPalette() {
        return false;
    }

    @Override
    public void initWidgets() {
        WPanel panel = add(new WPanel()).centerX().top().marginTop(96).widget();
        panel.minWidth = 560;

        refresh();
    }

    /**
     * Fills in the search field, as if it had been typed.
     */
    public void setQuery(String query) {
        input.set(query);
        refresh();
    }

    // Results

    private void refresh() {
        String query = input != null ? input.get().trim() : "";

        entries.clear();

        if (query.isEmpty()) addSuggestions();
        else addMatches(query);

        selected = 0;
        firstShown = 0;

        if (list != null) list.invalidate();
    }

    private void addSuggestions() {
        int count = 0;

        for (Module module : Modules.get().getAll()) {
            if (module.favorite && !isHidden(module)) {
                entries.add(moduleEntry(module, module.title, new int[0]));
                count++;
            }
        }

        for (Module module : Modules.get().getActive()) {
            if (count >= 8) break;

            if (!module.favorite && !isHidden(module)) {
                entries.add(moduleEntry(module, module.title, new int[0]));
                count++;
            }
        }

        for (Tab tab : Tabs.get()) entries.add(tabEntry(tab, new int[0]));
    }

    private void addMatches(String query) {
        int count = 0;

        for (Pair<Module, String> match : Modules.get().searchTitles(query)) {
            if (count >= MAX_MODULES) break;

            Module module = match.getLeft();
            if (isHidden(module)) continue;

            // Highlight the letters when it is the title that matched
            int[] positions = match.getRight().equals(module.title) ? FuzzyMatcher.match(query, module.title).positions() : new int[0];

            entries.add(moduleEntry(module, match.getRight(), positions));
            count++;
        }

        count = 0;

        for (Module module : Modules.get().searchSettingTitles(query)) {
            if (count >= MAX_SETTINGS) break;
            if (isHidden(module)) continue;

            Setting<?> best = null;
            FuzzyMatcher.Result bestResult = null;

            for (SettingGroup group : module.settings) {
                for (Setting<?> setting : group) {
                    FuzzyMatcher.Result result = FuzzyMatcher.match(query, setting.title);

                    if (result.matched() && (bestResult == null || result.score() > bestResult.score())) {
                        best = setting;
                        bestResult = result;
                    }
                }
            }

            if (best != null) {
                entries.add(new Entry(Kind.SETTING, best.title, module.title, bestResult.positions(), module, () -> openSettings(module)));
                count++;
            }
        }

        for (Tab tab : Tabs.get()) {
            FuzzyMatcher.Result result = FuzzyMatcher.match(query, tab.name);

            if (result.matched()) entries.add(tabEntry(tab, result.positions()));
        }
    }

    private static boolean isHidden(Module module) {
        return Config.get().hiddenModules.get().contains(module);
    }

    private Entry moduleEntry(Module module, String title, int[] positions) {
        return new Entry(Kind.MODULE, title, module.category.name, positions, module, module::toggle);
    }

    private Entry tabEntry(Tab tab, int[] positions) {
        return new Entry(Kind.TAB, tab.name, "Open", positions, null, () -> tab.openScreen(theme));
    }

    private void openSettings(Module module) {
        Tabs.get().getFirst().openScreen(theme);

        if (mc.currentScreen instanceof ModulesScreen screen) screen.expandModule(module);
    }

    // Choosing

    private void move(int direction) {
        if (entries.isEmpty()) return;

        selected = Math.floorMod(selected + direction, entries.size());
        keepSelectedInView();
    }

    private void keepSelectedInView() {
        if (selected < firstShown) firstShown = selected;
        else if (selected >= firstShown + MAX_ROWS) firstShown = selected - MAX_ROWS + 1;
    }

    private void run(int index, boolean keepOpen) {
        if (index < 0 || index >= entries.size()) return;

        Entry entry = entries.get(index);

        if (entry.kind() == Kind.MODULE && keepOpen) {
            // Stay here so several modules can be switched in a row, the result shows the new state
            entry.action().run();
            return;
        }

        if (entry.kind() == Kind.MODULE) {
            close();
            entry.action().run();
        }
        else {
            entry.action().run();
        }
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        switch (keyInput.key()) {
            case GLFW_KEY_DOWN -> {
                move(1);
                return true;
            }
            case GLFW_KEY_UP -> {
                move(-1);
                return true;
            }
            case GLFW_KEY_PAGE_DOWN -> {
                move(MAX_ROWS);
                return true;
            }
            case GLFW_KEY_PAGE_UP -> {
                move(-MAX_ROWS);
                return true;
            }
            case GLFW_KEY_ENTER, GLFW_KEY_KP_ENTER -> {
                run(selected, (keyInput.modifiers() & GLFW_MOD_SHIFT) != 0);
                return true;
            }
        }

        return super.keyPressed(keyInput);
    }

    // Widgets

    private class WPanel extends WVerticalList {
        @Override
        public void init() {
            spacing = 0;

            input = add(theme.textBox("", "Search modules, settings and tabs")).expandX().pad(10).widget();
            input.setFocused(true);
            input.action = CommandPaletteScreen.this::refresh;

            list = add(new WPaletteList()).expandX().widget();
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            // Dim everything behind
            renderer.roundRect(0, 0, Utils.getWindowWidth(), Utils.getWindowHeight(), 0, theme instanceof FlorenceGuiTheme florence ? florence.design().scrim : Colors.argb(0, 0, 0, 120));

            if (!(theme instanceof FlorenceGuiTheme florence)) {
                renderer.quad(x, y, width, height, new Color(20, 20, 24, 240));
                return;
            }

            Design d = florence.design();
            double radius = florence.radiusLarge();

            if (florence.shadows()) renderer.shadow(x, y + florence.scale(10), width, height, radius, florence.scale(40), d.shadow);

            if (florence.glass() && renderer.hasBackdrop()) renderer.glass(x, y, width, height, radius, d.panel, Math.max(1, Math.round(florence.scale(1))), d.outlineHover);
            else renderer.roundRect(x, y, width, height, radius, d.panelSolid, Math.max(1, Math.round(florence.scale(1))), d.outlineHover);
        }
    }

    private class WPaletteList extends WWidget {
        private final AnimatedFloat highlight = new AnimatedFloat(0);
        private boolean highlightPlaced;

        private double rowHeight() {
            return rowSpace() * 2 + theme.textHeight();
        }

        private double rowSpace() {
            return theme instanceof FlorenceGuiTheme florence ? florence.space(8) : theme.scale(8);
        }

        private double hintHeight() {
            return theme.textHeight() + rowSpace() * 1.6;
        }

        @Override
        protected void onCalculateSize() {
            width = theme.scale(560);

            int rows = Math.min(entries.size(), MAX_ROWS);
            height = rows * rowHeight() + hintHeight() + (rows == 0 ? rowHeight() : 0);
        }

        @Override
        public boolean onMouseClicked(Click click, boolean doubled) {
            if (!mouseOver || click.button() != GLFW_MOUSE_BUTTON_LEFT) return false;

            int row = rowAt(click.y());

            if (row >= 0) {
                run(firstShown + row, false);
                return true;
            }

            return false;
        }

        @Override
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (mouseX == lastMouseX && mouseY == lastMouseY) return;

            int row = rowAt(mouseY);
            if (mouseOver && row >= 0) selected = firstShown + row;
        }

        @Override
        public boolean onMouseScrolled(double amount) {
            if (!mouseOver || entries.size() <= MAX_ROWS) return false;

            firstShown = Math.max(0, Math.min(entries.size() - MAX_ROWS, firstShown - (int) Math.signum(amount)));
            return true;
        }

        private int rowAt(double mouseY) {
            int rows = Math.min(entries.size(), MAX_ROWS);
            int row = (int) Math.floor((mouseY - y) / rowHeight());

            return row >= 0 && row < rows ? row : -1;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!(theme instanceof FlorenceGuiTheme florence)) return;

            Design d = florence.design();
            double th = theme.textHeight();
            double rowH = rowHeight();
            double padX = florence.space(16);
            double line = Math.max(1, Math.round(florence.scale(1)));

            // Line between the search field and the results
            renderer.roundRect(x, y, width, line, 0, d.divider);

            int rows = Math.min(entries.size(), MAX_ROWS);

            if (entries.isEmpty()) {
                renderer.text("Nothing found", x + padX, y + (rowH - th) / 2, d.textSecondary, false);
            }

            // Highlight under the chosen row, it slides from row to row
            double targetY = y + (selected - firstShown) * rowH;

            if (!highlightPlaced) {
                highlight.snap(targetY);
                highlightPlaced = true;
            }

            highlight.springTo(targetY, Spring.CRITICAL);
            double highlightY = highlight.update(delta);

            if (rows > 0) {
                double inset = florence.space(6);
                renderer.roundRect(x + inset, highlightY + line + florence.space(1), width - inset * 2, rowH - florence.space(2), florence.radiusMedium(), d.cardActive, line, Colors.withAlpha(d.accent, 90));
            }

            for (int i = 0; i < rows; i++) {
                Entry entry = entries.get(firstShown + i);
                double rowY = y + i * rowH;
                double textY = rowY + (rowH - th) / 2 + line;
                boolean chosen = firstShown + i == selected;

                // Dot that shows whether a module is on
                double dot = Math.max(6, florence.scale(7));
                double dotX = x + padX;
                double dotY = rowY + rowH / 2 + line / 2;

                int dotColor;

                if (entry.kind() == Kind.MODULE) dotColor = entry.module().isActive() ? d.success : d.trackOff;
                else if (entry.kind() == Kind.SETTING) dotColor = d.accent2;
                else dotColor = d.accent;

                renderer.circle(dotX + dot / 2, dotY, dot / 2, dotColor);

                // Title with the matched letters in the accent color
                double tx = dotX + dot + florence.space(12);
                drawHighlighted(renderer, florence, entry.title(), entry.highlight(), tx, textY, chosen ? d.text : Colors.lerp(d.textSecondary, d.text, 0.8), d.accent2);

                // What it is
                String right = entry.kind() == Kind.MODULE ? entry.subtitle() : entry.kind() == Kind.SETTING ? "in " + entry.subtitle() : entry.subtitle();
                renderer.text(right, x + width - padX - theme.textWidth(right), textY, d.textDisabled, false);
            }

            // Keys that work here
            double hintY = y + rows * rowH + (rows == 0 ? rowH : 0);
            renderer.roundRect(x, hintY, width, line, 0, d.divider);

            String hint = "Up and Down to choose    Enter to select    Esc to close";
            double hintTextY = hintY + (hintHeight() - th) / 2;
            renderer.text(hint, x + padX, hintTextY, d.textDisabled, false);
        }

        private void drawHighlighted(GuiRenderer renderer, FlorenceGuiTheme florence, String text, int[] positions, double x, double y, int color, int highlightColor) {
            if (positions.length == 0) {
                renderer.text(text, x, y, color, false);
                return;
            }

            // Draw runs of matched and unmatched letters one after the other
            StringBuilder run = new StringBuilder();
            boolean runHighlighted = false;
            int next = 0;

            for (int i = 0; i < text.length(); i++) {
                boolean highlighted = next < positions.length && positions[next] == i;
                if (highlighted) next++;

                if (i > 0 && highlighted != runHighlighted) {
                    renderer.text(run.toString(), x, y, runHighlighted ? highlightColor : color, false);
                    x += theme.textWidth(run.toString());
                    run.setLength(0);
                }

                run.append(text.charAt(i));
                runHighlighted = highlighted;
            }

            renderer.text(run.toString(), x, y, runHighlighted ? highlightColor : color, false);
        }
    }
}
