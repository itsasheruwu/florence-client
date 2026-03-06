/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules.player;

import florencedevelopment.florenceclient.systems.modules.Categories;
import florencedevelopment.florenceclient.systems.modules.Module;

public class Portals extends Module {
    public Portals() {
        super(Categories.Player, "portals", "Allows you to use GUIs normally while in a Nether Portal.");
    }
}
