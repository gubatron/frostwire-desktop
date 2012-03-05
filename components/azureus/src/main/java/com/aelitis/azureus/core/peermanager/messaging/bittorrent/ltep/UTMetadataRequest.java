package com.aelitis.azureus.core.peermanager.messaging.bittorrent.ltep;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gudy.azureus2.core3.util.BEncoder;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.DirectByteBuffer;

import com.aelitis.azureus.core.peermanager.messaging.Message;
import com.aelitis.azureus.core.peermanager.messaging.MessageException;
import com.aelitis.azureus.core.peermanager.messaging.MessagingUtil;

public class UTMetadataRequest implements LTMessage {
    
    private final byte version;
    
    private DirectByteBuffer buffer = null;
    private String description = null;
    
    public UTMetadataRequest(byte version) {
        this.version = version;
    }

    @Override
    public String getID() {
        return LTMessage.ID_UT_METADATA;
    }

    @Override
    public byte[] getIDBytes() {
        return LTMessage.ID_UT_METADATA_BYTES;
    }

    @Override
    public String getFeatureID() {
        return LTMessage.LT_FEATURE_ID;
    }

    @Override
    public int getFeatureSubID() {
        return LTMessage.SUBID_UT_METADATA; 
    }

    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public int getType() {
        return Message.TYPE_PROTOCOL_PAYLOAD;
    }

    @Override
    public String getDescription() {
        if (description == null) {
            description = getID().toUpperCase() + " with " + "Testing";
        }
        return description;
    }

    @Override
    public DirectByteBuffer[] getData() {
        if( buffer == null ) {
            Map payload_map = new HashMap();
            // bencoded_buffer = payload_map;
            payload_map.put("msg_type", 0);
            payload_map.put("piece", 0);
            buffer = MessagingUtil.convertPayloadToBencodedByteStream(payload_map, DirectByteBuffer.AL_MSG_UT_METADATA);
          }
          
          return new DirectByteBuffer[] {buffer};
    }

    @Override
    public Message deserialize(DirectByteBuffer data, byte version) throws MessageException {
        Map root = MessagingUtil.convertBencodedByteStreamToPayload(data, 2, getID());
        
        return null;
    }

    @Override
    public void destroy() {
        if( buffer != null )  buffer.returnToPool();
    }
    
    public static void main(String[] args) throws Exception, Exception {
        UTMetadataRequest m = new UTMetadataRequest((byte)2);
        Map data_dic = new HashMap();
        data_dic.put("msg_type", 0);
        data_dic.put("piece", 0);
       
        System.out.println(new String( BEncoder.encode(data_dic), Constants.BYTE_ENCODING));
       
    }
}
