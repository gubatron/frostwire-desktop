package com.limegroup.gnutella.connection;


public interface MessageReaderFactory {

    public MessageReader createMessageReader(MessageReceiver receiver);

}