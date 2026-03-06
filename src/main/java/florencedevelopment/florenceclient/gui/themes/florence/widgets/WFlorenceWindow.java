/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WHorizontalList;
import florencedevelopment.florenceclient.gui.widgets.containers.WView;
import florencedevelopment.florenceclient.gui.widgets.containers.WWindow;
import florencedevelopment.florenceclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import static florencedevelopment.florenceclient.utils.Utils.getWindowHeight;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WFlorenceWindow extends WWindow implements FlorenceWidget {
    public WFlorenceWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WFlorenceHeader(icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animProgress > 0) {
            // Dark background matching HTML example (rgba(20, 20, 20, 0.9))
            Color bgColor = new Color(20, 20, 20, (int)(255 * 0.9));
            renderer.quad(x, y + header.height, width, height - header.height, bgColor);
            
            // Border styling (rgba(255, 255, 255, 0.1))
            Color borderColor = new Color(255, 255, 255, (int)(255 * 0.1));
            renderer.quad(x, y + header.height, width, 1, borderColor); // Top border
            renderer.quad(x, y + height - 1, width, 1, borderColor); // Bottom border
            renderer.quad(x, y + header.height, 1, height - header.height, borderColor); // Left border
            renderer.quad(x + width - 1, y + header.height, 1, height - header.height, borderColor); // Right border
        }
    }

    private class WFlorenceHeader extends WHeader {
        private WHorizontalList list;
        
        public WFlorenceHeader(WWidget icon) {
            super(icon);
        }

        @Override
        public void init() {
            // Create horizontal list for header content
            list = add(theme.horizontalList()).expandX().widget();
            list.spacing = 0;
            
            if (icon != null) {
                list.add(icon).centerY();
            }

            if (beforeHeaderInit != null) {
                beforeHeaderInit.accept(list);
            }

            // Uppercase title with letter spacing (matching HTML example)
            String uppercaseTitle = title.toUpperCase();
            list.add(theme.label(uppercaseTitle, true)).expandCellX().center().pad(4);
            
            // Don't add triangle - windows stay expanded
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            // Override render to skip triangle rotation (we don't have a triangle)
            // Update animProgress but don't access triangle
            animProgress += (expanded ? 1 : -1) * delta * 14;
            animProgress = MathHelper.clamp(animProgress, 0, 1);
            
            // Replicate WContainer.render() logic but bypass WHeader.render() that accesses triangle
            // First, call the base WWidget.render() for visibility and tooltip handling
            if (!visible) return true;
            
            if (isOver(mouseX, mouseY)) {
                mouseOverTimer += delta;
                if ((instantTooltips || mouseOverTimer >= 1) && tooltip != null) {
                    WView view = getView();
                    if (view == null || view.mouseOver) renderer.tooltip(tooltip);
                }
            } else {
                mouseOverTimer = 0;
            }
            
            // Render this widget's background/content
            onRender(renderer, mouseX, mouseY, delta);
            
            // Render children using WContainer logic
            WView view = getView();
            double windowHeight = getWindowHeight();
            
            for (Cell<?> cell : cells) {
                WWidget widget = cell.widget();
                
                if (widget.y > windowHeight) break;
                if (widget.y + widget.height <= 0) continue;
                
                // Replicate shouldRenderWidget logic (it's private in WContainer)
                boolean shouldRender = true;
                if (view != null) {
                    if (!view.isWidgetInView(widget)) shouldRender = false;
                    if (widget.mouseOver && !view.mouseOver) {
                        widget.mouseOver = false;
                    }
                }
                
                if (shouldRender) {
                    widget.render(renderer, mouseX, mouseY, delta);
                }
            }
            
            return false;
        }
        
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            
            // Solid accent color background (no gradient) matching HTML example
            Color accentColor = theme.accentColor.get();
            renderer.quad(x, y, width, height, accentColor);
        }
        
        @Override
        public <T extends WWidget> Cell<T> add(T widget) {
            if (list != null) return list.add(widget);
            return super.add(widget);
        }
        
        @Override
        public boolean onMouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
            if (mouseOver && !doubled) {
                if (id != null && click.button() == GLFW_MOUSE_BUTTON_RIGHT) {
                    setExpanded(!expanded);
                    return true;
                }

                dragging = true;
                dragged = false;
                return true;
            }

            return false;
        }
        
        @Override
        public boolean onMouseReleased(net.minecraft.client.gui.Click click) {
            if (dragging) {
                dragging = false;
                // Left click release only ends dragging. Expand/collapse is right-click in header.
            }
            return false;
        }
    }
}
