/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.addons;

import florencedevelopment.florenceclient.utils.render.color.Color;

public abstract class FlorenceAddon {
    /** This field is automatically assigned from fabric.mod.json file. */
    public String name;

    /** This field is automatically assigned from fabric.mod.json file. */
    public String[] authors;

    /** This field is automatically assigned from the florence-client:color property in fabric.mod.json file. */
    public final Color color = new Color(255, 255, 255);

    public abstract void onInitialize();

    public void onRegisterCategories() {}

    public abstract String getPackage();

    public String getWebsite() {
        return null;
    }

    public GithubRepo getRepo() {
        return null;
    }

    public String getCommit() {
        return null;
    }
}
