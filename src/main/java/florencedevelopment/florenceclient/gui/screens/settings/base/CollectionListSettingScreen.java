/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens.settings.base;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WindowScreen;
import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WTable;
import florencedevelopment.florenceclient.gui.widgets.input.WTextBox;
import florencedevelopment.florenceclient.gui.widgets.pressable.WPressable;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.systems.config.Config;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class CollectionListSettingScreen<T> extends WindowScreen {
    protected final Setting<?> setting;
    protected final Collection<T> collection;
    private final Iterable<T> registry;

    private WTable table;
    private String filterText = "";

    public CollectionListSettingScreen(GuiTheme theme, String title, Setting<?> setting, Collection<T> collection, Iterable<T> registry) {
        super(theme, title);

        this.registry = registry;
        this.setting = setting;
        this.collection = collection;
    }

    @Override
    public void initWidgets() {
        // Filter
        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();

        initTable();
    }

    private void initTable() {
        // Left (all)
        WTable left = abc(registry, true, t -> {
            addValue(t);

            T v = getAdditionalValue(t);
            if (v != null) addValue(v);
        });

        if (Config.get().syncListSettingWidths.get() || !left.cells.isEmpty()) {
            table.add(theme.verticalSeparator()).expandWidgetY();
        }

        // Right (selected)
        WTable right = abc(collection, false, t -> {
            removeValue(t);

            T v = getAdditionalValue(t);
            if (v != null) removeValue(v);
        });

        postWidgets(left, right);
    }

    private WTable abc(Iterable<T> iterable, boolean isLeft, Consumer<T> buttonAction) {
        // Create
        Cell<WTable> cell = this.table.add(theme.table()).top();
        if (Config.get().syncListSettingWidths.get()) cell.group("sync-width");
        WTable table = cell.widget();

        // Sort
        Predicate<T> predicate = isLeft
            ? value -> this.includeValue(value) && !collection.contains(value)
            : this::includeValue;

        Iterable<T> sorted = SortingHelper.sort(iterable, predicate, this::getValueNames, filterText);

        sorted.forEach(t -> {
            table.add(getValueWidget(t));

            WPressable button = table.add(isLeft ? theme.plus() : theme.minus()).expandCellX().right().widget();
            button.action = () -> buttonAction.accept(t);

            table.row();
        });

        if (!table.cells.isEmpty()) cell.expandX();

        return table;
    }

    protected void invalidateTable() {
        table.clear();
        initTable();
    }

    protected void addValue(T value) {
        if (!collection.contains(value)) {
            collection.add(value);
            setting.onChanged();
            invalidateTable();
        }
    }

    protected void removeValue(T value) {
        if (collection.remove(value)) {
            setting.onChanged();
            invalidateTable();
        }
    }

    protected void postWidgets(WTable left, WTable right) {}

    protected boolean includeValue(T value) {
        return true;
    }

    protected abstract WWidget getValueWidget(T value);

    protected abstract String[] getValueNames(T value);

    protected T getAdditionalValue(T value) {
        return null;
    }
}
