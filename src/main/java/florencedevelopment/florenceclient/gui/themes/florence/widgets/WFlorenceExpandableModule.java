/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

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
import florencedevelopment.florenceclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import static florencedevelopment.florenceclient.FlorenceClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WFlorenceExpandableModule extends WContainer implements FlorenceWidget {
    private final Module module;
    private final String title;
    private final boolean canExpand;
    
    private WModuleHeader header;
    private WSettingsContainer settingsContainer;
    private boolean expanded = false;
    private double expandProgress = 0;
    private double chevronRotation = -1; // -1 means uninitialized
    
    private double titleWidth;
    
    public WFlorenceExpandableModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.canExpand = hasSettings(module);
        this.tooltip = module.description;
    }

    private boolean hasSettings(Module module) {
        for (SettingGroup group : module.settings.groups) {
            for (Setting<?> setting : group) {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public void init() {
        header = add(new WModuleHeader()).expandX().widget();
        settingsContainer = add(new WSettingsContainer()).expandX().widget();
    }
    
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        // Render border between modules (rgba(255, 255, 255, 0.05) from HTML example)
        Color borderColor = new Color(255, 255, 255, (int)(255 * 0.05));
        renderer.quad(x, y + height - 1, width, 1, borderColor);
        
        super.onRender(renderer, mouseX, mouseY, delta);
    }
    
    @Override
    protected void onCalculateSize() {
        if (titleWidth == 0) titleWidth = theme.textWidth(title);
        
        // Width should match parent (window)
        if (parent != null) {
            width = parent.width;
        } else {
            double pad = theme.scale(12);
            width = pad + titleWidth + pad + theme.scale(100); // Space for keybind, dot, chevron
        }
        
        // Calculate header size first
        if (header != null) {
            header.onCalculateSize();
        }
        
        // Calculate height based on expansion
        // Always calculate settings container size, but only include it in height when expanded
        double settingsHeight = 0;
        if (settingsContainer != null) {
            settingsContainer.onCalculateSize();
            // Use full settings height when expanded (not animated) so it properly pushes other modules down
            if (expanded) {
                settingsHeight = settingsContainer.height;
            }
        }
        
        height = (header != null ? header.height : theme.scale(20)) + settingsHeight;
    }
    
    @Override
    protected void onCalculateWidgetPositions() {
        // Calculate positions for all children
        double currentY = y;
        
        // Position header at the top
        if (header != null && cells.size() > 0) {
            Cell<?> headerCell = cells.get(0);
            headerCell.x = x + headerCell.padLeft();
            headerCell.y = currentY + headerCell.padTop();
            headerCell.width = width - headerCell.padLeft() - headerCell.padRight();
            headerCell.height = header.height;
            headerCell.alignWidget();
            header.x = headerCell.x;
            header.y = headerCell.y;
            header.width = headerCell.width;
            header.height = headerCell.height;
            currentY += header.height + headerCell.padTop() + headerCell.padBottom();
        }
        
        // Position settings container below header (only when expanded)
        if (settingsContainer != null && cells.size() > 1) {
            Cell<?> settingsCell = cells.get(1);
            if (expanded) {
                settingsCell.x = x + settingsCell.padLeft();
                settingsCell.y = currentY + settingsCell.padTop();
                settingsCell.width = width - settingsCell.padLeft() - settingsCell.padRight();
                settingsCell.height = settingsContainer.height;
                settingsCell.alignWidget();
                settingsContainer.x = settingsCell.x;
                settingsContainer.y = settingsCell.y;
                settingsContainer.width = settingsCell.width;
                settingsContainer.height = settingsCell.height;
            } else {
                // Hide settings container when collapsed
                settingsCell.x = x;
                settingsCell.y = y;
                settingsCell.width = 0;
                settingsCell.height = 0;
            }
        }
    }
    
    private class WModuleHeader extends WPressable implements FlorenceWidget {
        private double animationProgress1;
        private double animationProgress2;
        private double hoverProgress = 0;
        
        @Override
        public void init() {
            if (module.isActive()) {
                animationProgress1 = 1;
                animationProgress2 = 1;
            }
        }
        
        @Override
        protected void onCalculateSize() {
            double pad = theme.scale(12); // Matching HTML example padding
            height = pad + theme.textHeight() + pad;
            width = parent != null ? parent.width : 0;
        }
        
        @Override
        protected void onPressed(int button) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                module.toggle();
            } else if (button == GLFW_MOUSE_BUTTON_RIGHT && canExpand) {
                // Right click to expand/collapse settings
                WFlorenceExpandableModule.this.expanded = !WFlorenceExpandableModule.this.expanded;
                // Invalidate this widget and all parents up to the root to trigger layout recalculation
                WWidget widget = WFlorenceExpandableModule.this;
                while (widget != null) {
                    widget.invalidate();
                    widget = widget.parent;
                }
            }
        }
        
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            double pad = theme.scale(12); // Matching HTML example padding
            
            // Smooth animations
            double animSpeed1 = theme.smoothAnimations() ? 4 : 6;
            double animSpeed2 = theme.smoothAnimations() ? 6 : 8;
            
            animationProgress1 += delta * animSpeed1 * ((module.isActive() || mouseOver) ? 1 : -1);
            animationProgress1 = MathHelper.clamp(animationProgress1, 0, 1);
            
            animationProgress2 += delta * animSpeed2 * (module.isActive() ? 1 : -1);
            animationProgress2 = MathHelper.clamp(animationProgress2, 0, 1);
            
            // Hover animation
            if (theme.smoothAnimations()) {
                double target = mouseOver ? 1 : 0;
                hoverProgress += (target - hoverProgress) * delta * 14;
                hoverProgress = Math.max(0, Math.min(1, hoverProgress));
            } else {
                hoverProgress = mouseOver ? 1 : 0;
            }
            
            // Background - dark with subtle hover effect (matching HTML example)
            Color bgColor = new Color(20, 20, 20, 230); // rgba(20, 20, 20, 0.9)
            
            if (mouseOver && !module.isActive()) {
                // Subtle hover effect (rgba(255, 255, 255, 0.03))
                Color hoverOverlay = new Color(255, 255, 255, (int)(255 * 0.03));
                renderer.quad(x, y, width, height, bgColor);
                renderer.quad(x, y, width, height, hoverOverlay);
            } else {
                renderer.quad(x, y, width, height, bgColor);
            }
            
            // Active module accent highlight
            if (module.isActive()) {
                Color accentColor = theme.accentColor.get();
                // Left accent bar
                renderer.quad(x, y, theme.scale(2), height, accentColor);
                // Text color change for active
            }
            
            // Module name on the left
            double textX = x + pad;
            Color textColor = new Color(236, 236, 236); // #ececec from HTML
            if (module.isActive()) {
                textColor = theme.accentColor.get(); // Accent color when active
            }
            renderer.text(title, textX, y + pad, textColor, false);
            
            // Keybind tag (small, dark background)
            String keybindText = module.keybind.toString();
            boolean isBinding = Modules.get().isBinding();
            // Check if this module is the one being bound by checking if it was just set to bind
            // We'll show "..." when binding mode is active (simplified approach)
            if (isBinding) {
                keybindText = "...";
            } else if (keybindText.equals("None")) {
                keybindText = "NONE";
            } else {
                // Convert to uppercase for display
                keybindText = keybindText.toUpperCase();
            }
            
            // Calculate positions from right to left: [chevron] -> dot -> keybind tag
            double chevronSize = theme.scale(14);
            double chevronX = x + width - pad - chevronSize;
            
            // Status dot (6px circle) - positioned between keybind and chevron
            double dotSize = theme.scale(6);
            double dotSpacing = theme.scale(8); // Space between dot and chevron
            double dotX = canExpand ? chevronX - dotSpacing - dotSize : x + width - pad - dotSize;
            double dotY = y + height / 2 - dotSize / 2;
            Color dotColor = module.isActive() 
                ? theme.accentColor.get() 
                : new Color(68, 68, 68); // #444
            
            // Calculate keybind tag position (before status dot)
            double keybindTextWidth = theme.textWidth(keybindText);
            double keybindTagWidth = keybindTextWidth + theme.scale(10); // Padding
            double keybindTagHeight = theme.scale(14);
            double keybindSpacing = theme.scale(8); // Space between keybind and dot
            double keybindTagX = dotX - keybindSpacing - keybindTagWidth;
            double keybindTagY = y + height / 2 - keybindTagHeight / 2;
            
            // Keybind tag background (#333 equivalent)
            Color keybindBgColor = new Color(51, 51, 51); // #333
            renderer.quad(keybindTagX, keybindTagY, keybindTagWidth, keybindTagHeight, keybindBgColor);
            
            // Keybind text
            Color keybindTextColor = isBinding || module.keybind.isSet() 
                ? theme.accentColor.get() 
                : new Color(119, 119, 119); // #777
            double keybindTextX = keybindTagX + theme.scale(5);
            double keybindTextY = keybindTagY + (keybindTagHeight - theme.textHeight()) / 2;
            renderer.text(keybindText, keybindTextX, keybindTextY, keybindTextColor, false);
            
            // Glow effect when active
            if (module.isActive() && theme.enableShadows()) {
                Color glowColor = new Color(dotColor.r, dotColor.g, dotColor.b, 100);
                renderer.quad(dotX - theme.scale(1), dotY - theme.scale(1), dotSize + theme.scale(2), dotSize + theme.scale(2), GuiRenderer.CIRCLE, glowColor);
            }
            renderer.quad(dotX, dotY, dotSize, dotSize, GuiRenderer.CIRCLE, dotColor);
            
            if (canExpand) {
                // Chevron (expand button) - rightmost element
                double chevronY = y + height / 2 - chevronSize / 2;

                // Smooth rotation animation
                // When collapsed: point right (0 degrees)
                // When expanded: point down (-90 degrees)
                double targetRotation = WFlorenceExpandableModule.this.expanded ? -90 : 0;
                double currentRotation = targetRotation;

                if (theme.smoothAnimations()) {
                    // Initialize if needed
                    if (WFlorenceExpandableModule.this.chevronRotation < 0) {
                        WFlorenceExpandableModule.this.chevronRotation = targetRotation;
                    }
                    // Animate towards target
                    double diff = targetRotation - WFlorenceExpandableModule.this.chevronRotation;
                    WFlorenceExpandableModule.this.chevronRotation += diff * delta * 10;
                    currentRotation = WFlorenceExpandableModule.this.chevronRotation;
                } else {
                    WFlorenceExpandableModule.this.chevronRotation = targetRotation;
                    currentRotation = targetRotation;
                }

                Color chevronColor = new Color(236, 236, 236); // #ececec
                boolean chevronHover = mouseX >= chevronX && mouseX <= chevronX + chevronSize &&
                    mouseY >= chevronY && mouseY <= chevronY + chevronSize;
                if (chevronHover) {
                    chevronColor = theme.accentColor.get();
                }

                renderer.rotatedQuad(chevronX, chevronY, chevronSize, chevronSize, currentRotation, GuiRenderer.TRIANGLE, chevronColor);
            }
        }
        
        @Override
        public boolean onMouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
            // Check for Shift+Right Click to bind key
            if (click.button() == GLFW_MOUSE_BUTTON_RIGHT) {
                // Check if shift is pressed using Minecraft client
                if (mc.isShiftPressed()) {
                    // Shift+Right Click to bind key
                    Modules.get().setModuleToBind(module);
                    return true; // Consume the event
                }
                // Regular right click is handled by onPressed to expand/collapse
            }
            
            // Let the parent handle other clicks (toggle on left, expand on right)
            return super.onMouseClicked(click, doubled);
        }
    }
    
    private class WSettingsContainer extends WVerticalList implements FlorenceWidget {
        @Override
        public void init() {
            if (!module.settings.groups.isEmpty()) {
                // Use optimized factory for compact settings in expandable modules
                FlorenceExpandableSettingsWidgetFactory factory = new FlorenceExpandableSettingsWidgetFactory(theme);
                add(factory.create(theme, module.settings, "")).expandX();
            }
            spacing = theme.scale(1); // Reduced from 2 to 1 for more compact layout
        }
        
        @Override
        public double pad() {
            return theme.scale(8); // Horizontal padding matching HTML example
        }
        
        @Override
        protected void onCalculateSize() {
            if (parent != null) {
                width = parent.width;
            }
            super.onCalculateSize();
        }
        
        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            
            // Smooth expand animation
            if (theme.smoothAnimations()) {
                double target = WFlorenceExpandableModule.this.expanded ? 1 : 0;
                expandProgress += (target - expandProgress) * delta * 14;
                expandProgress = Math.max(0, Math.min(1, expandProgress));
            } else {
                expandProgress = WFlorenceExpandableModule.this.expanded ? 1 : 0;
            }
            
            if (expandProgress <= 0) {
                // Not expanded, don't render
                return true;
            }
            
            // Darker background for settings (rgba(0, 0, 0, 0.2) from HTML example)
            Color bgColor = new Color(0, 0, 0, (int)(255 * 0.2));
            // Use animated height for smooth visual transition
            double visibleHeight = height * expandProgress;
            renderer.quad(x, y, width, visibleHeight, bgColor);
            
            // Always use scissor to clip children to the visible area
            // This prevents settings from rendering outside the module bounds
            renderer.scissorStart(x, y, width, visibleHeight);
            
            boolean result = super.render(renderer, mouseX, mouseY, delta);
            
            renderer.scissorEnd();
            
            return result;
        }
    }
    
    public void tick() {
        if (settingsContainer != null && expanded) {
            module.settings.tick(settingsContainer, theme);
        }
    }
}
