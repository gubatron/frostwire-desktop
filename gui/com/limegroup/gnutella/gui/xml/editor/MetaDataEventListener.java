package com.limegroup.gnutella.gui.xml.editor;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileEventListener;

public interface MetaDataEventListener extends FileEventListener {

	void metaDataUnchanged(FileDesc fd);
	
}
