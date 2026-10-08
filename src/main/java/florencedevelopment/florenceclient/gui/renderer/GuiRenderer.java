/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.renderer;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.renderer.operations.TextOperation;
import florencedevelopment.florenceclient.gui.renderer.packer.GuiTexture;
import florencedevelopment.florenceclient.gui.renderer.packer.TexturePacker;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import florencedevelopment.florenceclient.renderer.BackdropBlur;
import florencedevelopment.florenceclient.renderer.Renderer2D;
import florencedevelopment.florenceclient.renderer.ShapeRenderer;
import florencedevelopment.florenceclient.renderer.Texture;
import florencedevelopment.florenceclient.utils.PostInit;
import florencedevelopment.florenceclient.utils.misc.Pool;
import florencedevelopment.florenceclient.utils.render.RenderUtils;
import florencedevelopment.florenceclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

import static florencedevelopment.florenceclient.FlorenceClient.mc;
import static florencedevelopment.florenceclient.utils.Utils.getWindowHeight;
import static florencedevelopment.florenceclient.utils.Utils.getWindowWidth;

public class GuiRenderer {
    private static final Color WHITE = new Color(255, 255, 255);

    // The mesh copies the color straight away so one instance can be reused
    private final Color scratchColor = new Color();

    private static final TexturePacker TEXTURE_PACKER = new TexturePacker();
    private static Texture TEXTURE;

    public static GuiTexture CIRCLE;
    public static GuiTexture TRIANGLE;
    public static GuiTexture EDIT;
    public static GuiTexture RESET;
    public static GuiTexture FAVORITE_NO, FAVORITE_YES;
    public static GuiTexture COPY, PASTE;

    public GuiTheme theme;

    private final Renderer2D r = new Renderer2D(false);
    private final Renderer2D rTex = new Renderer2D(true);
    private final ShapeRenderer shapes = new ShapeRenderer();

    private final Pool<Scissor> scissorPool = new Pool<>(Scissor::new);
    private final Stack<Scissor> scissorStack = new ObjectArrayList<>();

    private final Pool<TextOperation> textPool = new Pool<>(TextOperation::new);
    private final List<TextOperation> texts = new ObjectArrayList<>();

    private final List<Runnable> postTasks = new ObjectArrayList<>();

    public String tooltip, lastTooltip;
    public WWidget tooltipWidget;
    private double tooltipAnimProgress;

    private DrawContext drawContext;

    public static GuiTexture addTexture(Identifier id) {
        return TEXTURE_PACKER.add(id);
    }

    @PostInit
    public static void init() {
        CIRCLE = addTexture(FlorenceClient.identifier("textures/icons/gui/circle.png"));
        TRIANGLE = addTexture(FlorenceClient.identifier("textures/icons/gui/triangle.png"));
        EDIT = addTexture(FlorenceClient.identifier("textures/icons/gui/edit.png"));
        RESET = addTexture(FlorenceClient.identifier("textures/icons/gui/reset.png"));
        FAVORITE_NO = addTexture(FlorenceClient.identifier("textures/icons/gui/favorite_no.png"));
        FAVORITE_YES = addTexture(FlorenceClient.identifier("textures/icons/gui/favorite_yes.png"));

        COPY = addTexture(FlorenceClient.identifier("textures/icons/gui/copy.png"));
        PASTE = addTexture(FlorenceClient.identifier("textures/icons/gui/paste.png"));

        TEXTURE = TEXTURE_PACKER.pack();
    }

    public void begin(DrawContext drawContext) {
        this.drawContext = drawContext;
        this.drawContext.createNewRootLayer();

        var matrices = drawContext.getMatrices();
        matrices.pushMatrix();
        matrices.scale(1.0f / mc.getWindow().getScaleFactor());

        scissorStart(0, 0, getWindowWidth(), getWindowHeight());
    }

    public void end() {
        scissorEnd();

        for (Runnable task : postTasks) task.run();
        postTasks.clear();

        drawContext.getMatrices().popMatrix();
        drawContext.createNewRootLayer();
    }

    public void beginRender() {
        r.begin();
        shapes.begin();
        rTex.begin();
    }

    public void endRender() {
        endRender(null);
    }

    public void endRender(Scissor scissor) {
        if (scissor != null) scissor.push();

        r.end();
        shapes.end();
        rTex.end();

        // Plain quads, then rounded shapes, then icons and images. Text is drawn last.
        r.render();

        GpuTextureView backdrop = BackdropBlur.get().getTexture();
        if (backdrop != null) shapes.render(backdrop, RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
        else shapes.render(TEXTURE.getGlTextureView(), TEXTURE.getSampler());

        rTex.render("u_Texture", TEXTURE.getGlTextureView(), TEXTURE.getSampler());

        // Normal text
        theme.textRenderer().begin(theme.scale(1));
        for (TextOperation text : texts) {
            if (!text.title) text.run(textPool);
        }
        theme.textRenderer().end();

        // Title text
        theme.textRenderer().begin(theme.scale(1.25));
        for (TextOperation text : texts) {
            if (text.title) text.run(textPool);
        }
        theme.textRenderer().end();

        texts.clear();

        if (scissor != null) scissor.pop();
    }

    public void scissorStart(double x, double y, double width, double height) {
        if (!scissorStack.isEmpty()) {
            Scissor parent = scissorStack.top();

            if (x < parent.x) x = parent.x;
            else if (x + width > parent.x + parent.width) width -= (x + width) - (parent.x + parent.width);

            if (y < parent.y) y = parent.y;
            else if (y + height > parent.y + parent.height) height -= (y + height) - (parent.y + parent.height);

            endRender(parent);
        }

        scissorStack.push(scissorPool.get().set(x, y, width, height));
        drawContext.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));

        beginRender();
    }

    public void scissorEnd() {
        Scissor scissor = scissorStack.pop();

        endRender(scissor);

        scissor.push();
        for (Runnable task : scissor.postTasks) task.run();
        scissor.pop();

        drawContext.disableScissor();
        if (!scissorStack.isEmpty()) beginRender();

        scissorPool.free(scissor);
    }

    public boolean renderTooltip(DrawContext drawContext, double mouseX, double mouseY, double delta) {
        tooltipAnimProgress += (tooltip != null ? 1 : -1) * delta * 14;
        tooltipAnimProgress = MathHelper.clamp(tooltipAnimProgress, 0, 1);

        boolean toReturn = false;

        if (tooltipAnimProgress > 0) {
            if (tooltip != null && !tooltip.equals(lastTooltip)) {
                tooltipWidget = theme.tooltip(tooltip);
                tooltipWidget.init();
            }

            double deltaX = -tooltipWidget.x + mouseX + 12;
            double deltaY = -tooltipWidget.y + mouseY + 12;

            if (mouseX + 12 + tooltipWidget.width > getWindowWidth()) deltaX = -tooltipWidget.x + getWindowWidth() - tooltipWidget.width;
            if (mouseY + 12 + tooltipWidget.height > getWindowHeight()) deltaY = -tooltipWidget.y + getWindowHeight() - tooltipWidget.height;

            tooltipWidget.move(deltaX, deltaY);

            setAlpha(tooltipAnimProgress);

            begin(drawContext);
            tooltipWidget.render(this, mouseX, mouseY, delta);
            end();

            setAlpha(1);

            lastTooltip = tooltip;
            toReturn = true;
        }

        tooltip = null;
        return toReturn;
    }

    public void setAlpha(double a) {
        r.setAlpha(a);
        shapes.setAlpha(a);
        rTex.setAlpha(a);

        theme.textRenderer().setAlpha(a);
    }

    public void tooltip(String text) {
        tooltip = text;
    }

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        r.quad(x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }
    public void quad(double x, double y, double width, double height, Color colorLeft, Color colorRight) {
        quad(x, y, width, height, colorLeft, colorRight, colorRight, colorLeft);
    }
    public void quad(double x, double y, double width, double height, Color color) {
        quad(x, y, width, height, color, color);
    }
    public void quad(WWidget widget, Color color) {
        quad(widget.x, widget.y, widget.width, widget.height, color);
    }

    // Rounded shapes, colors are packed as 0xAARRGGBB (see Colors)

    /**
     * Whether the world behind the GUI is blurred this frame, so {@link #glass} shows it. Without it glass is drawn as
     * a plain tinted box.
     */
    public boolean hasBackdrop() {
        return BackdropBlur.get().getTexture() != null;
    }

    public void roundRect(double x, double y, double w, double h, double radius, int color) {
        shapes.box(x, y, w, h, radius, radius, radius, radius, color, color, color, color, 0, 0);
    }

    public void roundRect(double x, double y, double w, double h, double radius, int color, double borderWidth, int borderColor) {
        shapes.box(x, y, w, h, radius, radius, radius, radius, color, color, color, color, borderWidth, borderColor);
    }

    /**
     * A box with a different radius for every corner.
     */
    public void roundRect(double x, double y, double w, double h, double rTopLeft, double rTopRight, double rBottomRight, double rBottomLeft, int color, double borderWidth, int borderColor) {
        shapes.box(x, y, w, h, rTopLeft, rTopRight, rBottomRight, rBottomLeft, color, color, color, color, borderWidth, borderColor);
    }

    /**
     * A box that fades from the top color to the bottom color.
     */
    public void roundRectVertical(double x, double y, double w, double h, double radius, int top, int bottom, double borderWidth, int borderColor) {
        shapes.box(x, y, w, h, radius, radius, radius, radius, top, top, bottom, bottom, borderWidth, borderColor);
    }

    /**
     * A box that fades from the left color to the right color.
     */
    public void roundRectHorizontal(double x, double y, double w, double h, double radius, int left, int right, double borderWidth, int borderColor) {
        shapes.box(x, y, w, h, radius, radius, radius, radius, left, right, right, left, borderWidth, borderColor);
    }

    public void roundRect(WWidget widget, double radius, int color) {
        roundRect(widget.x, widget.y, widget.width, widget.height, radius, color);
    }

    /**
     * A tint over the blurred world behind the box.
     */
    public void glass(double x, double y, double w, double h, double radius, int tint, double borderWidth, int borderColor) {
        shapes.glass(x, y, w, h, radius, radius, radius, radius, tint, borderWidth, borderColor);
    }

    public void glass(double x, double y, double w, double h, double rTopLeft, double rTopRight, double rBottomRight, double rBottomLeft, int tint, double borderWidth, int borderColor) {
        shapes.glass(x, y, w, h, rTopLeft, rTopRight, rBottomRight, rBottomLeft, tint, borderWidth, borderColor);
    }

    /**
     * A soft shadow for a box with the given bounds, drawn below it.
     */
    public void shadow(double x, double y, double w, double h, double radius, double blur, int color) {
        shapes.shadow(x, y, w, h, radius, blur, color);
    }

    public void circle(double centerX, double centerY, double radius, int color) {
        roundRect(centerX - radius, centerY - radius, radius * 2, radius * 2, radius, color);
    }

    public void line(double x1, double y1, double x2, double y2, double thickness, int color) {
        shapes.line(x1, y1, x2, y2, thickness, color);
    }
    public void quad(double x, double y, double width, double height, GuiTexture texture, Color color) {
        rTex.texQuad(x, y, width, height, texture.get(width, height), color);
    }

    public void rotatedQuad(double x, double y, double width, double height, double rotation, GuiTexture texture, Color color) {
        rTex.texQuad(x, y, width, height, rotation, texture.get(width, height), color);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
        r.triangle(x1, y1, x2, y2, x3, y3 ,color);
    }

    public void text(String text, double x, double y, Color color, boolean title) {
        texts.add(getOp(textPool, x, y, color).set(text, theme.textRenderer(), title));
    }

    /**
     * @param argb the color packed as 0xAARRGGBB
     */
    public void text(String text, double x, double y, int argb, boolean title) {
        TextOperation op = textPool.get();
        op.set(x, y, argb);
        texts.add(op.set(text, theme.textRenderer(), title));
    }

    /**
     * Draws a GUI icon tinted with the color, packed as 0xAARRGGBB.
     */
    public void icon(GuiTexture texture, double x, double y, double width, double height, int argb) {
        scratchColor.set((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, argb >>> 24);
        rTex.texQuad(x, y, width, height, texture.get(width, height), scratchColor);
    }

    public void icon(GuiTexture texture, double x, double y, double width, double height, double rotation, int argb) {
        scratchColor.set((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, argb >>> 24);
        rTex.texQuad(x, y, width, height, rotation, texture.get(width, height), scratchColor);
    }

    public void texture(double x, double y, double width, double height, double rotation, Texture texture) {
        post(() -> {
            rTex.begin();
            rTex.texQuad(x, y, width, height, rotation, 0, 0, 1, 1, WHITE);
            rTex.end();

            rTex.render(texture.getGlTextureView(), texture.getSampler());
        });
    }

    public void post(Runnable task) {
        scissorStack.top().postTasks.add(task);
    }

    public void item(ItemStack itemStack, int x, int y, float scale, boolean overlay) {
        RenderUtils.drawItem(drawContext, itemStack, x, y, scale, overlay, null, false);
    }

    public void absolutePost(Runnable task) {
        postTasks.add(task);
    }

    private <T extends GuiRenderOperation<T>> T getOp(Pool<T> pool, double x, double y, Color color) {
        T op = pool.get();
        op.set(x, y, color);
        return op;
    }
}
