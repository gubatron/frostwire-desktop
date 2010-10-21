package com.frostwire.gnutella.gui.chat;

//import com.limegroup.gnutella.Downloader;
//import com.limegroup.gnutella.Downloader.DownloadStatus;

/**
 * This class provides access to the <tt>ArrayList</tt> that stores all of the
 * downloads displayed in the download window.
 */
//final class ChatModel extends BasicDataLineModel<DownloadDataLine, Downloader> {
//public final class ChatModel extends BasicDataLineModel<ChatDataLine, ChatItem> {
public final class ChatModel implements Comparable<ChatDataLine> {

    /**
     * The currently item *debug purposes*.
     */
    private ChatItem _currentItem;

    /**
     * Initialize the model by setting the class of its DataLines.
     */
    ChatModel() {
	//System.out.println("initialized chat!");
      //  super(ChatDataLine.class);
    }

    /**
     * Creates a new ChatDataLine
     */
    public ChatDataLine createDataLine() {
        return new ChatDataLine();
    }    
	
	/**
	 * Returns a test
	 *
	 * @return debug text
	 */
	void testing() {
	//System.out.println("Testing...");
	}


	/**
	 * Over-ride the default refresh so that we can
	 * set the CLEAR_BUTTON as appropriate.
	 */
/*
	public Object refresh() {
		int size = getRowCount();
		boolean inactiveDownloadPresent = false;
		for(int i=0; i<size; i++) {
			DownloadDataLine ud = get(i);
			ud.update();
			inactiveDownloadPresent |= ud.isInactive();
		}
        fireTableRowsUpdated(0, size);
        return inactiveDownloadPresent ? Boolean.TRUE : Boolean.FALSE;
	}
*/
    public int compareTo(ChatDataLine o) {
	return 0;
        //return o.getURI().compareTo(uri);
    }

}








