/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui;

import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.gui.renderer.GuiDebugRenderer;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.tabs.TabScreen;
import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.widgets.WRoot;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WContainer;
import florencedevelopment.florenceclient.gui.widgets.input.WTextBox;
import florencedevelopment.florenceclient.utils.Utils;
import florencedevelopment.florenceclient.utils.misc.CursorStyle;
import florencedevelopment.florenceclient.utils.misc.input.Input;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static florencedevelopment.florenceclient.FlorenceClient.mc;
import static florencedevelopment.florenceclient.utils.Utils.getWindowHeight;
import static florencedevelopment.florenceclient.utils.Utils.getWindowWidth;
import static org.lwjgl.glfw.GLFW.*;

public abstract class WidgetScreen extends Screen {
    private static final GuiRenderer RENDERER = new GuiRenderer();
    private static final GuiDebugRenderer DEBUG_RENDERER = new GuiDebugRenderer();

    public Runnable taskAfterRender;
    protected Runnable enterAction;

    public Screen parent;
    private final WContainer root;

    protected final GuiTheme theme;

    public boolean locked, lockedAllowClose;
    private boolean closed;
    private boolean onClose;
    private boolean debug;

    private boolean closing;

    private double lastMouseX, lastMouseY;

    public double animProgress;

    private List<Runnable> onClosed;

    protected boolean firstInit = true;

    public WidgetScreen(GuiTheme theme, String title) {
        super(Text.literal(title));

        this.parent = mc.currentScreen;
        this.root = new WFullScreenRoot();
        this.theme = theme;

        root.theme = theme;

        if (parent != null) {
            animProgress = 1;

            if (this instanceof TabScreen && parent instanceof TabScreen) {
                parent = ((TabScreen) parent).parent;
            }
        }
    }

    public <W extends WWidget> Cell<W> add(W widget) {
        return root.add(widget);
    }

    public void clear() {
        root.clear();
    }

    public void invalidate() {
        root.invalidate();
    }

    @Override
    protected void init() {
        FlorenceClient.EVENT_BUS.subscribe(this);

        closed = false;

        if (firstInit) {
            firstInit = false;
            initWidgets();
        }
    }

    public abstract void initWidgets();

    public void reload() {
        clear();
        initWidgets();
    }

    public void onClosed(Runnable action) {
        if (onClosed == null) onClosed = new ArrayList<>(2);
        onClosed.add(action);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (locked) return false;

        double mouseX = click.x();
        double mouseY = click.y();
        double s = mc.getWindow().getScaleFactor();

        mouseX *= s;
        mouseY *= s;

        return root.mouseClicked(new Click(mouseX, mouseY, click.buttonInfo()), doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (locked) return false;

        double mouseX = click.x();
        double mouseY = click.y();
        double s = mc.getWindow().getScaleFactor();

        mouseX *= s;
        mouseY *= s;

        if (debug && click.button() == GLFW_MOUSE_BUTTON_RIGHT) DEBUG_RENDERER.mouseReleased(root, new Click(mouseX, mouseY, click.buttonInfo()), 0);

        return root.mouseReleased(new Click(mouseX, mouseY, click.buttonInfo()));
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (locked) return;

        double s = mc.getWindow().getScaleFactor();
        mouseX *= s;
        mouseY *= s;

        root.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (locked) return false;

        root.mouseScrolled(verticalAmount);

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        if (locked) return false;

        if ((input.modifiers() == GLFW_MOD_CONTROL || input.modifiers() == GLFW_MOD_SUPER) && input.key() == GLFW_KEY_9) {
            debug = !debug;
            return true;
        }

        if ((input.key() == GLFW_KEY_ENTER || input.key() == GLFW_KEY_KP_ENTER) && enterAction != null) {
            enterAction.run();
            return true;
        }

        return super.keyReleased(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (locked) return false;

        boolean shouldReturn = root.keyPressed(input) || super.keyPressed(input);
        if (shouldReturn) return true;

        // Select next text box if TAB was pressed
        if (input.key() == GLFW_KEY_TAB) {
            AtomicReference<WTextBox> firstTextBox = new AtomicReference<>(null);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean foundFocused = new AtomicBoolean(false);

            loopWidgets(root, wWidget -> {
                if (done.get() || !(wWidget instanceof WTextBox textBox)) return;

                if (foundFocused.get()) {
                    textBox.setFocused(true);
                    textBox.setCursorMax();

                    done.set(true);
                } else {
                    if (textBox.isFocused()) {
                        textBox.setFocused(false);
                        foundFocused.set(true);
                    }
                }

                if (firstTextBox.get() == null) firstTextBox.set(textBox);
            });

            if (!done.get() && firstTextBox.get() != null) {
                firstTextBox.get().setFocused(true);
                firstTextBox.get().setCursorMax();
            }

            return true;
        }

        boolean control = MacWindowUtil.IS_MAC ? input.modifiers() == GLFW_MOD_SUPER : input.modifiers() == GLFW_MOD_CONTROL;

        return (control && input.key() == GLFW_KEY_C && toClipboard())
            || (control && input.key() == GLFW_KEY_V && fromClipboard());
    }

    public void keyRepeated(KeyInput input) {
        if (locked) return;

        root.keyRepeated(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (locked) return false;

        return root.charTyped(input);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.client.world == null) {
            this.renderPanoramaBackground(context, deltaTicks);
        }
    }

    public void renderCustom(DrawContext context, int mouseX, int mouseY, float delta) {
        int s = mc.getWindow().getScaleFactor();
        mouseX *= s;
        mouseY *= s;

        animProgress += (delta / 20 * 14) * (closing ? -1 : 1);
        animProgress = MathHelper.clamp(animProgress, 0, 1);

        if (closing && (animProgress == 0 || parent != null)) {
            closeInternal();
        }

        GuiKeyEvents.canUseKeys = true;

        // Apply projection without scaling
        Utils.unscaledProjection();

        onRenderBefore(context, delta);

        RENDERER.theme = theme;
        theme.beforeRender();

        RENDERER.begin(context);
        RENDERER.setAlpha(animProgress);
        root.render(RENDERER, mouseX, mouseY, delta / 20);
        RENDERER.setAlpha(1);
        RENDERER.end();

        boolean tooltip = RENDERER.renderTooltip(context, mouseX, mouseY, delta / 20);

        if (debug) {
            DEBUG_RENDERER.render(root);
            if (tooltip) DEBUG_RENDERER.render(RENDERER.tooltipWidget);
        }

        Utils.scaledProjection();

        runAfterRenderTasks();
    }

    protected void runAfterRenderTasks() {
        if (taskAfterRender != null) {
            taskAfterRender.run();
            taskAfterRender = null;
        }
    }

    protected void onRenderBefore(DrawContext drawContext, float delta) {}

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        root.invalidate();
    }

    @Override
    public void close() {
        if (!locked || lockedAllowClose) {
            closing = true;
        }
    }

    @Override
    public void removed() {
        if (!closed || lockedAllowClose) {
            closed = true;
            onClosed();

            Input.setCursorStyle(CursorStyle.Default);

            loopWidgets(root, widget -> {
                if (widget instanceof WTextBox textBox && textBox.isFocused()) textBox.setFocused(false);
            });

            FlorenceClient.EVENT_BUS.unsubscribe(this);
            GuiKeyEvents.canUseKeys = true;

            if (onClosed != null) {
                for (Runnable action : onClosed) action.run();
            }

            if (onClose) {
                taskAfterRender = () -> {
                    locked = true;
                    mc.setScreen(parent);
                };
            }
        }
    }

    private void closeInternal() {
        boolean preOnClose = onClose;
        onClose = true;

        super.close();
        removed();

        onClose = preOnClose;
    }

    private void loopWidgets(WWidget widget, Consumer<WWidget> action) {
        action.accept(widget);

        if (widget instanceof WContainer) {
            for (Cell<?> cell : ((WContainer) widget).cells) loopWidgets(cell.widget(), action);
        }
    }

    protected void onClosed() {}

    public boolean toClipboard() {
        return false;
    }

    public boolean fromClipboard() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !locked || lockedAllowClose;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class WFullScreenRoot extends WContainer implements WRoot {
        private boolean valid;

        @Override
        public void invalidate() {
            valid = false;
        }

        @Override
        protected void onCalculateSize() {
            width = getWindowWidth();
            height = getWindowHeight();
        }

        @Override
        protected void onCalculateWidgetPositions() {
            for (Cell<?> cell : cells) {
                cell.x = 0;
                cell.y = 0;

                cell.width = width;
                cell.height = height;

                cell.alignWidget();
            }
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!valid) {
                calculateSize();
                calculateWidgetPositions();

                valid = true;
                mouseMoved(mc.mouse.getX(), mc.mouse.getY(), mc.mouse.getX(), mc.mouse.getY());
            }

            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}
