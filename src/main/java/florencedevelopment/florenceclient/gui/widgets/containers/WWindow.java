/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.widgets.containers;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.utils.WindowConfig;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WTriangle;
import net.minecraft.client.gui.Click;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

import static florencedevelopment.florenceclient.utils.Utils.getWindowHeight;
import static florencedevelopment.florenceclient.utils.Utils.getWindowWidth;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public abstract class WWindow extends WVerticalList {
    private static final int RESIZE_LEFT = 1;
    private static final int RESIZE_RIGHT = 1 << 1;
    private static final int RESIZE_TOP = 1 << 2;
    private static final int RESIZE_BOTTOM = 1 << 3;

    public double padding = 8;
    public double fixedWidth = -1;
    public Consumer<WContainer> beforeHeaderInit;
    public String id;

    public final WWidget icon;
    protected final String title;

    protected WHeader header;
    public WView view;

    protected boolean dragging;
    protected boolean expanded = true;
    protected boolean dragged;

    protected double animProgress = 1;

    protected boolean moved = false;
    protected double movedX, movedY;
    protected double dragRawX, dragRawY;
    protected double snapPreviewX, snapPreviewY;
    protected boolean snapPreviewActive;
    protected boolean resizing;
    protected int resizeEdges;
    protected double resizeStartMouseX, resizeStartMouseY;
    protected double resizeStartX, resizeStartY;
    protected double resizeStartWidth, resizeStartHeight;
    protected double userWidth = -1;
    protected double userHeight = -1;
    private double defaultViewMaxHeight;

    private boolean propagateEventsExpanded;

    public WWindow(WWidget icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    @Override
    public void init() {
        header = header(icon);
        header.theme = theme;
        super.add(header).expandWidgetX().widget();

        view = super.add(theme.view()).expandX().pad(padding).widget();
        defaultViewMaxHeight = view.maxHeight;

        if (id != null) {
            WindowConfig config = theme.getWindowConfig(id);
            expanded = config.expanded;
            animProgress = expanded ? 1 : 0;
            userWidth = config.width;
            userHeight = config.height;
        }
    }

    protected abstract WHeader header(WWidget icon);

    @Override
    public void calculateSize() {
        applyUserViewHeight();
        super.calculateSize();

        if (userWidth > 0) {
            width = Math.max(Math.round(theme.scale(minWidth)), Math.round(userWidth));
        }
        else if (fixedWidth > 0) {
            width = Math.round(theme.scale(fixedWidth));
        }
    }

    @Override
    public <T extends WWidget> Cell<T> add(T widget) {
        return view.add(widget);
    }

    @Override
    public void clear() {
        view.clear();
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;

        if (id != null) {
            WindowConfig config = theme.getWindowConfig(id);
            config.expanded = expanded;
        }
    }

    @Override
    protected void onCalculateWidgetPositions() {
        if (id != null) {
            WindowConfig config = theme.getWindowConfig(id);

            if (config.x != -1) {
                x = config.x;

                if (x + width > getWindowWidth()) {
                    x = getWindowWidth() - width;
                }
            }

            if (config.y != -1) {
                y = config.y;

                if (y + height > getWindowHeight()) {
                    y = getWindowHeight() - height;
                }
            }
        }

        super.onCalculateWidgetPositions();

        if (moved) {
            move(movedX - x, movedY - y);
        }
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!visible) return true;

        boolean scissor = (animProgress != 0 && animProgress != 1) || (expanded && animProgress != 1);
        if (scissor) renderer.scissorStart(x, y, width, (height - header.height) * animProgress + header.height);
        boolean toReturn = super.render(renderer, mouseX, mouseY, delta);
        if (scissor) renderer.scissorEnd();

        return toReturn;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!doubled && beginResize(click)) return true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (resizing) {
            finishResizing();
            return true;
        }

        return super.mouseReleased(click);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        if (resizing) {
            mouseOver = isOver(mouseX, mouseY);
            resizeTo(mouseX, mouseY);
            return;
        }

        super.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
    }

    protected void moveTo(double x, double y) {
        dragRawX = x;
        dragRawY = y;
        snapPreviewActive = false;
        setWindowPosition(x, y);
    }

    protected void startDragging() {
        dragging = true;
        dragged = false;
        dragRawX = x;
        dragRawY = y;

        if (theme instanceof FlorenceGuiTheme florenceTheme && florenceTheme.snapToGrid()) {
            updateSnapPreview(florenceTheme);
        }
        else {
            snapPreviewActive = false;
        }
    }

    protected void finishDragging() {
        if (!dragging) return;
        dragging = false;

        if (theme instanceof FlorenceGuiTheme florenceTheme && florenceTheme.snapToGrid()) {
            updateSnapPreview(florenceTheme);
            setWindowPosition(snapPreviewX, snapPreviewY);
        }

        snapPreviewActive = false;
    }

    protected void dragBy(double deltaX, double deltaY) {
        dragRawX += deltaX;
        dragRawY += deltaY;

        if (theme instanceof FlorenceGuiTheme florenceTheme && florenceTheme.snapToGrid()) {
            updateSnapPreview(florenceTheme);

            double gridSize = florenceTheme.gridSizePixels();
            double smoothness = florenceTheme.gridSnapSmoothness();

            double displayX = applySoftSnap(dragRawX, snapPreviewX, gridSize, smoothness);
            double displayY = applySoftSnap(dragRawY, snapPreviewY, gridSize, smoothness);

            setWindowPosition(displayX, displayY);
        }
        else {
            snapPreviewActive = false;
            setWindowPosition(dragRawX, dragRawY);
        }
    }

    private void updateSnapPreview(FlorenceGuiTheme florenceTheme) {
        double gridSize = florenceTheme.gridSizePixels();

        snapPreviewX = snapCoordinate(dragRawX, gridSize);
        snapPreviewY = snapCoordinate(dragRawY, gridSize);
        snapPreviewActive = true;
    }

    private double snapCoordinate(double coordinate, double gridSize) {
        return Math.round(coordinate / gridSize) * gridSize;
    }

    private double applySoftSnap(double raw, double snapped, double gridSize, double smoothness) {
        double maxDistance = Math.max(1, gridSize / 2.0);
        double distance = Math.abs(snapped - raw);
        double closeness = 1.0 - Math.min(1.0, distance / maxDistance);
        double pull = Math.min(1.0, smoothness + (1.0 - smoothness) * closeness * closeness);

        return raw + (snapped - raw) * pull;
    }

    private void setWindowPosition(double x, double y) {
        move(x - this.x, y - this.y);

        moved = true;
        movedX = this.x;
        movedY = this.y;

        if (id != null) {
            WindowConfig config = theme.getWindowConfig(id);
            config.x = this.x;
            config.y = this.y;
        }
    }

    private void setWindowSize(double width, double height) {
        userWidth = width;
        userHeight = height;

        if (id != null) {
            WindowConfig config = theme.getWindowConfig(id);
            config.width = userWidth;
            config.height = userHeight;
        }

        invalidate();
    }

    private boolean beginResize(Click click) {
        if (!(theme instanceof FlorenceGuiTheme florenceTheme)) return false;
        if (!florenceTheme.resizeWindowKeybind().isPressed()) return false;
        if (click.button() != 0) return false;

        int hoveredEdges = getResizeEdges(click.x(), click.y());
        if (hoveredEdges == 0) return false;

        resizing = true;
        resizeEdges = hoveredEdges;
        resizeStartMouseX = click.x();
        resizeStartMouseY = click.y();
        resizeStartX = x;
        resizeStartY = y;
        resizeStartWidth = width;
        resizeStartHeight = height;
        userWidth = width;
        userHeight = height;
        dragged = true;

        return true;
    }

    private void finishResizing() {
        resizing = false;
        resizeEdges = 0;
    }

    private void resizeTo(double mouseX, double mouseY) {
        double dx = mouseX - resizeStartMouseX;
        double dy = mouseY - resizeStartMouseY;

        double newX = resizeStartX;
        double newY = resizeStartY;
        double newWidth = resizeStartWidth;
        double newHeight = resizeStartHeight;

        if ((resizeEdges & RESIZE_LEFT) != 0) {
            newX = resizeStartX + dx;
            newWidth = resizeStartWidth - dx;
        }
        if ((resizeEdges & RESIZE_RIGHT) != 0) {
            newWidth = resizeStartWidth + dx;
        }
        if ((resizeEdges & RESIZE_TOP) != 0) {
            newY = resizeStartY + dy;
            newHeight = resizeStartHeight - dy;
        }
        if ((resizeEdges & RESIZE_BOTTOM) != 0) {
            newHeight = resizeStartHeight + dy;
        }

        double minWidth = getMinResizeWidth();
        double minHeight = getMinResizeHeight();

        if (newWidth < minWidth) {
            if ((resizeEdges & RESIZE_LEFT) != 0) newX = resizeStartX + (resizeStartWidth - minWidth);
            newWidth = minWidth;
        }

        if (newHeight < minHeight) {
            if ((resizeEdges & RESIZE_TOP) != 0) newY = resizeStartY + (resizeStartHeight - minHeight);
            newHeight = minHeight;
        }

        if (newX < 0) {
            newWidth += newX;
            newX = 0;
        }
        if (newY < 0) {
            newHeight += newY;
            newY = 0;
        }
        if (newX + newWidth > getWindowWidth()) {
            newWidth = getWindowWidth() - newX;
        }
        if (newY + newHeight > getWindowHeight()) {
            newHeight = getWindowHeight() - newY;
        }

        if (newWidth < minWidth) {
            newWidth = minWidth;
            newX = Math.max(0, Math.min(newX, getWindowWidth() - newWidth));
        }
        if (newHeight < minHeight) {
            newHeight = minHeight;
            newY = Math.max(0, Math.min(newY, getWindowHeight() - newHeight));
        }

        setWindowPosition(newX, newY);
        setWindowSize(newWidth, newHeight);
    }

    private void applyUserViewHeight() {
        if (view == null) return;

        double maxBodyHeight = Math.max(theme.scale(48), getWindowHeight() - header.height - theme.scale(8));
        if (userHeight > 0) {
            double bodyHeight = Math.max(getMinBodyHeight(), Math.min(maxBodyHeight, userHeight - header.height));
            view.maxHeight = bodyHeight;
        }
        else {
            view.maxHeight = Math.min(defaultViewMaxHeight, maxBodyHeight);
        }
    }

    private double getMinResizeWidth() {
        return Math.max(theme.scale(minWidth), theme.scale(180));
    }

    private double getMinResizeHeight() {
        return header.height + getMinBodyHeight();
    }

    private double getMinBodyHeight() {
        return Math.max(theme.scale(64), padding * 2 + theme.textHeight());
    }

    private int getResizeEdges(double mouseX, double mouseY) {
        double border = theme.scale(6);
        int edges = 0;

        if (mouseX >= x && mouseX <= x + border) edges |= RESIZE_LEFT;
        else if (mouseX >= x + width - border && mouseX <= x + width) edges |= RESIZE_RIGHT;

        if (mouseY >= y && mouseY <= y + border) edges |= RESIZE_TOP;
        else if (mouseY >= y + height - border && mouseY <= y + height) edges |= RESIZE_BOTTOM;

        return edges;
    }

    public boolean isDraggingWindow() {
        return dragging;
    }

    public boolean hasSnapPreview() {
        return snapPreviewActive;
    }

    public double getSnapPreviewX() {
        return snapPreviewX;
    }

    public double getSnapPreviewY() {
        return snapPreviewY;
    }

    @Override
    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animProgress > 0 || widget instanceof WHeader) {
            widget.render(renderer, mouseX, mouseY, delta);
        }

        propagateEventsExpanded = expanded;
    }

    @Override
    protected boolean propagateEvents(WWidget widget) {
        return widget instanceof WHeader || propagateEventsExpanded;
    }

    protected abstract class WHeader extends WContainer {
        private final WWidget icon;
        private WTriangle triangle;
        private WHorizontalList list;

        public WHeader(WWidget icon) {
            this.icon = icon;
        }

        @Override
        public void init() {
            if (icon != null) {
                createList();
                add(icon).centerY();
            }

            if (beforeHeaderInit != null) {
                createList();
                beforeHeaderInit.accept(this);
            }

            add(theme.label(title, true)).expandCellX().center().pad(4);

            triangle = add(theme.triangle()).pad(4).right().centerY().widget();
            triangle.action = () -> setExpanded(!expanded);
        }

        private void createList() {
            list = add(theme.horizontalList()).expandX().widget();
            list.spacing = 0;
        }

        @Override
        public <T extends WWidget> Cell<T> add(T widget) {
            if (list != null) return list.add(widget);
            return super.add(widget);
        }

        @Override
        protected void onCalculateSize() {
            width = 0;
            height = 0;

            for (Cell<?> cell : cells) {
                double w = cell.padLeft() + cell.widget().width + cell.padRight();
                if (cell.widget() instanceof WTriangle) w *= 2;

                width += w;
                height = Math.max(height, cell.padTop() + cell.widget().height + cell.padBottom());
            }
        }

        @Override
        public boolean onMouseClicked(Click click, boolean doubled) {
            if (mouseOver && !doubled) {
                if (click.button() == GLFW_MOUSE_BUTTON_RIGHT) setExpanded(!expanded);
                else {
                    WWindow.this.startDragging();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean onMouseReleased(Click click) {
            if (dragging) {
                WWindow.this.finishDragging();

                if (!dragged) setExpanded(!expanded);
            }

            return false;
        }

        @Override
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (dragging) {
                WWindow.this.dragBy(mouseX - lastMouseX, mouseY - lastMouseY);
                dragged = true;
            }
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            animProgress += (expanded ? 1 : -1) * delta * 14;
            animProgress = MathHelper.clamp(animProgress, 0, 1);

            triangle.rotation = (1 - animProgress) * -90;

            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}
