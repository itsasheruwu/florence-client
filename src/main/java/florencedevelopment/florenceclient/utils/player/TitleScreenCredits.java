/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.player;

import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.addons.AddonManager;
import florencedevelopment.florenceclient.addons.GithubRepo;
import florencedevelopment.florenceclient.addons.FlorenceAddon;
import florencedevelopment.florenceclient.gui.GuiThemes;
import florencedevelopment.florenceclient.gui.screens.CommitsScreen;
import florencedevelopment.florenceclient.mixininterface.IText;
import florencedevelopment.florenceclient.utils.network.Http;
import florencedevelopment.florenceclient.utils.network.FlorenceExecutor;
import florencedevelopment.florenceclient.utils.render.FlorenceToast;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class TitleScreenCredits {
    private static final List<Credit> credits = new ArrayList<>();

    private TitleScreenCredits() {
    }

    private static void init() {
        // Add addons
        for (FlorenceAddon addon : AddonManager.ADDONS) add(addon);

        // Sort by width (Meteor always first)
        credits.sort(Comparator.comparingInt(value -> value.addon == FlorenceClient.ADDON ? Integer.MIN_VALUE : -mc.textRenderer.getWidth(value.text)));

        // Check for latest commits
        FlorenceExecutor.execute(() -> {
            for (Credit credit : credits) {
                if (credit.addon.getRepo() == null || credit.addon.getCommit() == null) continue;

                GithubRepo repo = credit.addon.getRepo();
                Http.Request request = Http.get("https://api.github.com/repos/%s/branches/%s".formatted(repo.getOwnerName(), repo.branch()));
                request.exceptionHandler(e -> FlorenceClient.LOG.error("Could not fetch repository information for addon '{}'.", credit.addon.name, e));
                repo.authenticate(request);
                HttpResponse<Response> res = request.sendJsonResponse(Response.class);

                switch (res.statusCode()) {
                    case Http.UNAUTHORIZED -> {
                        String message = "Invalid authentication token for repository '%s'".formatted(repo.getOwnerName());
                        FlorenceToast toast = new FlorenceToast.Builder("GitHub: Unauthorized").icon(Items.BARRIER).text(message).build();
                        mc.getToastManager().add(toast);
                        FlorenceClient.LOG.warn(message);
                        if (System.getenv("meteor.github.authorization") == null) {
                            FlorenceClient.LOG.info("Consider setting an authorization " +
                                "token with the 'meteor.github.authorization' environment variable.");
                            FlorenceClient.LOG.info("See: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens");
                        }
                    }
                    case Http.FORBIDDEN -> FlorenceClient.LOG.warn("Could not fetch updates for addon '{}': Rate-limited by GitHub.", credit.addon.name);
                    case Http.NOT_FOUND -> FlorenceClient.LOG.warn("Could not fetch updates for addon '{}': GitHub repository '{}' not found.", credit.addon.name, repo.getOwnerName());
                    case Http.SUCCESS -> {
                        if (!credit.addon.getCommit().equals(res.body().commit.sha)) {
                            synchronized (credit.text) {
                                credit.text.append(Text.literal("*").formatted(Formatting.RED));
                                ((IText) ((Text) credit.text)).florence$invalidateCache(); // ???
                            }
                        }
                    }
                }
            }
        });
    }

    private static void add(FlorenceAddon addon) {
        Credit credit = new Credit(addon);

        credit.text.append(Text.literal(addon.name).styled(style -> style.withColor(addon.color.getPacked())));
        credit.text.append(Text.literal(" by ").formatted(Formatting.GRAY));

        for (int i = 0; i < addon.authors.length; i++) {
            if (i > 0) {
                credit.text.append(Text.literal(i == addon.authors.length - 1 ? " & " : ", ").formatted(Formatting.GRAY));
            }

            credit.text.append(Text.literal(addon.authors[i]).formatted(Formatting.WHITE));
        }

        credits.add(credit);
    }

    public static void render(DrawContext context) {
        if (credits.isEmpty()) init();

        int y = 3;
        for (Credit credit : credits) {
            synchronized (credit.text) {
                int x = mc.currentScreen.width - 3 - mc.textRenderer.getWidth(credit.text);

                context.drawTextWithShadow(mc.textRenderer, credit.text, x, y, -1);
            }

            y += mc.textRenderer.fontHeight + 2;
        }
    }

    public static boolean onClicked(double mouseX, double mouseY) {
        int y = 3;
        for (Credit credit : credits) {
            int width;
            synchronized (credit.text) {
                width = mc.textRenderer.getWidth(credit.text);
            }

            int x = mc.currentScreen.width - 3 - width;

            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + mc.textRenderer.fontHeight + 2) {
                if (credit.addon.getRepo() != null && credit.addon.getCommit() != null) {
                    mc.setScreen(new CommitsScreen(GuiThemes.get(), credit.addon));
                    return true;
                }
            }

            y += mc.textRenderer.fontHeight + 2;
        }

        return false;
    }

    private static class Credit {
        public final FlorenceAddon addon;
        public final MutableText text = Text.empty();

        public Credit(FlorenceAddon addon) {
            this.addon = addon;
        }
    }

    private static class Response {
        public Commit commit;
    }

    private static class Commit {
        public String sha;
    }
}
