package com.limegroup.gnutella.connection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.limegroup.gnutella.messages.MessageFactory;

@Singleton
public class MessageReaderFactoryImpl implements MessageReaderFactory {

    private final MessageFactory messageFactory;

    @Inject
    public MessageReaderFactoryImpl(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }
    
    public MessageReader createMessageReader(MessageReceiver receiver) {
        return null;
    }

}
