/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.notebot.decoder;

import florencedevelopment.florenceclient.systems.modules.Modules;
import florencedevelopment.florenceclient.systems.modules.misc.Notebot;
import florencedevelopment.florenceclient.utils.notebot.song.Song;

import java.io.File;

public abstract class SongDecoder {
    protected Notebot notebot = Modules.get().get(Notebot.class);

    /**
     * Parse file to a {@link Song} object
     *
     * @param file Song file
     * @return A {@link Song} object
     */
    public abstract Song parse(File file) throws Exception;
}
