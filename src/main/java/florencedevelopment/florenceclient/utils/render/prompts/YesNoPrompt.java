/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.render.prompts;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.GuiThemes;
import florencedevelopment.florenceclient.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.screen.Screen;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class YesNoPrompt extends Prompt<YesNoPrompt> {
    private Runnable onYes = () -> {};
    private Runnable onNo = () -> {};

    private YesNoPrompt(GuiTheme theme, Screen parent) {
        super(theme, parent);
    }

    public static YesNoPrompt create() {
        return new YesNoPrompt(GuiThemes.get(), mc.currentScreen);
    }

    public static YesNoPrompt create(GuiTheme theme, Screen parent) {
        return new YesNoPrompt(theme, parent);
    }

    public YesNoPrompt onYes(Runnable action) {
        this.onYes = action;
        return this;
    }

    public YesNoPrompt onNo(Runnable action) {
        this.onNo = action;
        return this;
    }

    @Override
    protected void initialiseWidgets(PromptScreen screen) {
        WButton yesButton = screen.list.add(theme.button("Yes")).expandX().widget();
        yesButton.action = () -> {
            dontShowAgain(screen);
            onYes.run();
            screen.close();
        };

        WButton noButton = screen.list.add(theme.button("No")).expandX().widget();
        noButton.action = () -> {
            dontShowAgain(screen);
            onNo.run();
            screen.close();
        };
    }
}
