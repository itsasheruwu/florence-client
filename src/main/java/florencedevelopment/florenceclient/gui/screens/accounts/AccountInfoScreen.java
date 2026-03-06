/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens.accounts;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WindowScreen;
import florencedevelopment.florenceclient.gui.widgets.containers.WHorizontalList;
import florencedevelopment.florenceclient.gui.widgets.pressable.WButton;
import florencedevelopment.florenceclient.systems.accounts.Account;
import florencedevelopment.florenceclient.systems.accounts.AccountType;
import florencedevelopment.florenceclient.systems.accounts.TokenAccount;
import florencedevelopment.florenceclient.utils.render.color.Color;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class AccountInfoScreen extends WindowScreen {
    private final Account<?> account;

    public AccountInfoScreen(GuiTheme theme, Account<?> account) {
        super(theme, account.getUsername() + " details");
        this.account = account;
    }

    @Override
    public void initWidgets() {
        TokenAccount e = (TokenAccount) account;
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        String tokenLabel = account.getType() + " token:";
        if (account.getType() == AccountType.Session) tokenLabel = "";

        WButton copy = theme.button("Copy");
        copy.action = () -> mc.keyboard.setClipboard(e.getToken());

        l.add(theme.label(tokenLabel));
        l.add(theme.label(account.getType() == AccountType.Session ? "Click to copy Token" : e.getToken()).color(Color.GRAY)).pad(5);
        l.add(copy);
    }
}
