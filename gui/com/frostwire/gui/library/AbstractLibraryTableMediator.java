package com.frostwire.gui.library;

import com.frostwire.gui.player.AudioSource;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.DataLine;
import com.limegroup.gnutella.gui.tables.DataLineModel;

public abstract class AbstractLibraryTableMediator<T extends DataLineModel<E, I>, E extends DataLine<I>, I> extends AbstractTableMediator<T, E, I> {

    protected AbstractLibraryTableMediator(String id) {
        super(id);
    }
    
    public abstract AudioSource getNextRandomSong();

    public abstract AudioSource getNextContinuousSong(AudioSource currentSong);

    public abstract AudioSource getNextSong(AudioSource currentSong);
}
