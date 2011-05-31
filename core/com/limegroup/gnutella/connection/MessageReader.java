package com.limegroup.gnutella.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.limegroup.gnutella.messages.MessageFactory;

/**
 * Reads messages from a channel.  This class is notified when more of a message
 * can potentially be read by its handleRead() method being called.  To change
 * the channel this reads from, use setReaderChannel(ReadableByteChannel).
 *
 * It is possible to construct this class without an initial source channel.
 * However, before handleRead is called, the channel must be set.
 *
 * The first time the channel returns -1 this will throw an IOException, as it
 * never expects the channel to run out of data.  Upon each read notification,
 * as much data as possible will be read from the source channel.
 */
public class MessageReader {
    
    /** the maximum size of a message payload that we'll accept */
    private static final long MAX_MESSAGE_SIZE = 64 * 1024;
    /** the size of the header */
    private static final int HEADER_SIZE = 23;
    /** where in the header the payload is */
    private static final int PAYLOAD_LENGTH_OFFSET = 19;
    
    /** the constant buffer to use for emtpy payloads. */
    private static final ByteBuffer EMPTY_PAYLOAD = ByteBuffer.allocate(0);
    
    /** the sole buffer for parsing msg headers */
    private final ByteBuffer header;
    /** the buffer used for parsing the payload -- recreated for each message */
    private ByteBuffer payload;
    
    /** the sole receiver of messages */
    private final MessageReceiver receiver;
    
    /** whether or not this reader has been shut down yet. */
    private boolean shutdown = false;
    private final MessageFactory messageFactory;

    
    /**
     * Constructs a new MessageReader with the given source channel & receiver.
     */
    MessageReader(MessageReceiver receiver, MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
        if(receiver == null)
            throw new NullPointerException("null receiver");
            
        this.receiver = receiver;
        this.header = ByteBuffer.allocate(HEADER_SIZE);
        header.order(ByteOrder.LITTLE_ENDIAN);
        this.payload = null;
    }
    
    /**
     * Notification that a read can be performed from the given channel.
     * All messages that can be read without blocking are read & dispatched.
     */
    public void handleRead() throws IOException {
        
    }
    
    /** 
     * Informs the receiver that the message is shutdown.
     */
    public void shutdown() {
        synchronized(this) {
            if(shutdown)
                return;
                
            shutdown = true;
        }
        receiver.messagingClosed();
    }
    
    /** Unused */
    public void handleIOException(IOException iox) {
        throw new RuntimeException("unsupported operation", iox);
    }
    
}
    
    