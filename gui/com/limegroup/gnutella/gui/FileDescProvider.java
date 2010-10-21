package com.limegroup.gnutella.gui;

import com.limegroup.gnutella.FileDesc;

/**
 * Provides an array of file descs which are currently selected.
 *
 */
public interface FileDescProvider {
	FileDesc[] getFileDescs();
}
