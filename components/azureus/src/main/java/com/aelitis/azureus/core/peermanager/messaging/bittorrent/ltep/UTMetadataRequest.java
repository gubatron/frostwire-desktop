package com.aelitis.azureus.core.peermanager.messaging.bittorrent.ltep;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
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

    private final int msg_type;
    private final int piece;
    private final int total_size;
    private final byte[] metadata;
    private final byte version;

    private DirectByteBuffer buffer = null;
    private String description = null;

    public UTMetadataRequest(int msg_type, int piece, int total_size, byte[] metadata, byte version) {
        this.msg_type = msg_type;
        this.piece = piece;
        this.total_size = total_size;
        this.metadata = metadata;
        this.version = version;
    }
    
    public int getMessageType() {
        return msg_type;
    }
    
    public int getPiece() {
        return piece;
    }
    
    public int getTotalSize() {
        return total_size;
    }
    
    public byte[] getMetadata() {
        return metadata;
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
        if (buffer == null) {
            if (msg_type == 0) {
                Map payload_map = new HashMap();
                // bencoded_buffer = payload_map;
                payload_map.put("msg_type", 0);
                payload_map.put("piece", piece);
                buffer = MessagingUtil.convertPayloadToBencodedByteStream(payload_map, DirectByteBuffer.AL_MSG_UT_METADATA);
            }
        }

        return new DirectByteBuffer[] { buffer };
    }

    @Override
    public Message deserialize(DirectByteBuffer data, byte version) throws MessageException {
        try {
            int pos = data.position(DirectByteBuffer.SS_MSG);
            byte[] raw = new byte[data.remaining(DirectByteBuffer.SS_MSG)];
            data.get(DirectByteBuffer.SS_MSG, raw);
            data.position(DirectByteBuffer.SS_MSG, pos);
            Map<?, ?> map = MessagingUtil.convertBencodedByteStreamToPayload(data, 2, getID());

            int msg_type = ((Long) map.get("msg_type")).intValue();
            if (msg_type == 0) {
                int piece = ((Long) map.get("piece")).intValue();
                return new UTMetadataRequest(msg_type, piece, 0, null, (byte)1);
            } else if (msg_type == 1) {
                int piece = ((Long) map.get("piece")).intValue();
                int total_size = ((Long) map.get("total_size")).intValue();
                int offset = BEncoder.encode(map).length;
                byte[] metadata = new byte[raw.length - offset];
                System.arraycopy(raw, offset, metadata, 0, metadata.length);
                return new UTMetadataRequest(msg_type, piece, total_size, metadata, (byte)1);
            } else if (msg_type == 2) {
                int piece = ((Long) map.get("piece")).intValue();
                return new UTMetadataRequest(msg_type, piece, 0, null, (byte)1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void destroy() {
        if (buffer != null)
            buffer.returnToPool();
    }

    public static void main(String[] args) throws Exception, Exception {
        //        UTMetadataRequest m = new UTMetadataRequest((byte) 2);
        //        Map data_dic = new HashMap();
        //        data_dic.put("msg_type", 0);
        //        data_dic.put("piece", 0);
        //
        //        System.out.println(new String(BEncoder.encode(data_dic), Constants.BYTE_ENCODING));

    }
}
