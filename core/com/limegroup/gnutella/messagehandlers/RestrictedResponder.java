package com.limegroup.gnutella.messagehandlers;

import java.net.InetSocketAddress;

import org.limewire.io.NetworkInstanceUtils;
import org.limewire.setting.StringArraySetting;

import com.limegroup.gnutella.ReplyHandler;
import com.limegroup.gnutella.messages.Message;

/**
 * A message handler that responds to messages only to hosts
 * contained in a simppable whitelist.
 */
abstract class RestrictedResponder implements MessageHandler {
    /** setting to check for updates to the host list */
    private final StringArraySetting setting;
    
    private final NetworkInstanceUtils networkInstanceUtils;
    
    public RestrictedResponder(StringArraySetting setting) {
        this(setting, null);
    }
    
    /**
     * @param setting the setting containing the list of allowed
     * hosts to respond to.
     * @param verifier the <tt>SignatureVerifier</tt> to use.  Null if we
     * want to process all messages.
     */
    // TODO cleanup: SimmpManager registration should be done in extra initialize method
    // and also cleaned up
    public RestrictedResponder(StringArraySetting setting, 
            NetworkInstanceUtils networkInstanceUtils) {
        this.setting = setting;
        this.networkInstanceUtils = networkInstanceUtils;
    }
    
    public void simppUpdated(int newVersion) {
    }
    
    public final void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
//	System.out.print("Handling message: " + msg);
//        if (msg instanceof RoutableGGEPMessage) {
//            // if we have a verifier, verify
//            if (verifier != null && msg instanceof SecureMessage)
//                verifier.verify((SecureMessage)msg, new SecureCallback(addr, handler));
//            else
//                processRoutableMessage((RoutableGGEPMessage)msg, addr, handler);
//        } else {
//            // just check the return address.
//            if (!allowed.contains(new IP(handler.getAddress())))
//                return;
//            processAllowedMessage(msg, addr, handler);
//        }
    }
    
    /**
     * Process the specified message because it has been approved.
     */
    protected abstract void processAllowedMessage(Message msg, InetSocketAddress addr, ReplyHandler handler);
}
