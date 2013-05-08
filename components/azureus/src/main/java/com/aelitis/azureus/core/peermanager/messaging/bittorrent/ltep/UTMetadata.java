/*
 * Created by  Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aelitis.azureus.core.peermanager.messaging.bittorrent.ltep;

import java.util.HashMap;
import java.util.Map;

import org.gudy.azureus2.core3.logging.LogEvent;
import org.gudy.azureus2.core3.logging.LogIDs;
import org.gudy.azureus2.core3.logging.Logger;
import org.gudy.azureus2.core3.util.BEncoder;
import org.gudy.azureus2.core3.util.DirectByteBuffer;
import org.gudy.azureus2.core3.util.DirectByteBufferPool;

import com.aelitis.azureus.core.peermanager.messaging.Message;
import com.aelitis.azureus.core.peermanager.messaging.MessageException;
import com.aelitis.azureus.core.peermanager.messaging.MessagingUtil;

// ut_metadata
public class UTMetadata implements LTMessage {

    public static final boolean ENABLED = true; // put this in a configuration

    private static final LogIDs LOGID = LogIDs.NET;

    public static final int REQUEST_MESSAGE_TYPE_ID = 0;
    public static final int DATA_MESSAGE_TYPE_ID = 1;
    public static final int REJECT_MESSAGE_TYPE_ID = 2;

    private final int msg_type;
    private final int piece;
    private final int total_size;
    private final byte[] metadata;
    private final byte version;

    private DirectByteBuffer buffer = null;
    private String description = null;

    public UTMetadata(int msg_type, int piece, int total_size, byte[] metadata, byte version) {
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
            description = getID().toUpperCase() + "(msg_type=" + msg_type + ", piece=" + piece + ")";
        }
        return description;
    }

    @Override
    public DirectByteBuffer[] getData() {
        try {
            if (buffer == null) {
                if (msg_type == REQUEST_MESSAGE_TYPE_ID) {
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put("msg_type", msg_type);
                    map.put("piece", piece);
                    buffer = MessagingUtil.convertPayloadToBencodedByteStream(map, DirectByteBuffer.AL_MSG_UT_METADATA);
                } else if (msg_type == DATA_MESSAGE_TYPE_ID) {
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put("msg_type", msg_type);
                    map.put("piece", piece);
                    map.put("total_size", total_size);
                    byte[] mapPayload = BEncoder.encode(map);
                    buffer = DirectByteBufferPool.getBuffer(DirectByteBuffer.AL_MSG_UT_METADATA, mapPayload.length + metadata.length);
                    buffer.put(DirectByteBuffer.SS_MSG, mapPayload);
                    buffer.put(DirectByteBuffer.SS_MSG, metadata);
                    buffer.flip(DirectByteBuffer.SS_MSG);
                } else if (msg_type == REJECT_MESSAGE_TYPE_ID) {
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    map.put("msg_type", msg_type);
                    map.put("piece", piece);
                    buffer = MessagingUtil.convertPayloadToBencodedByteStream(map, DirectByteBuffer.AL_MSG_UT_METADATA);
                } else {
                    if (Logger.isEnabled()) {
                        Logger.log(new LogEvent(LOGID, LogEvent.LT_ERROR, "METADATA (UT): No valid msg_type=" + msg_type));
                    }
                    return null;
                }
            }

            return new DirectByteBuffer[] { buffer };
        } catch (Throwable e) {
            // what is the best way to handle this error?
            if (Logger.isEnabled()) {
                Logger.log(new LogEvent(LOGID, LogEvent.LT_ERROR, "METADATA (UT)", e));
            }
            return new DirectByteBuffer[0];
        }
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
            int piece = ((Long) map.get("piece")).intValue();
            if (msg_type == REQUEST_MESSAGE_TYPE_ID) {
                return new UTMetadata(msg_type, piece, 0, null, (byte) 1);
            } else if (msg_type == DATA_MESSAGE_TYPE_ID) {
                int total_size = ((Long) map.get("total_size")).intValue();
                int offset = BEncoder.encode(map).length;
                byte[] metadata = new byte[raw.length - offset];
                System.arraycopy(raw, offset, metadata, 0, metadata.length);
                return new UTMetadata(msg_type, piece, total_size, metadata, (byte) 1);
            } else if (msg_type == REJECT_MESSAGE_TYPE_ID) {
                return new UTMetadata(msg_type, piece, 0, null, (byte) 1);
            }

            // what is the best way to handle this error?
            if (Logger.isEnabled()) {
                Logger.log(new LogEvent(LOGID, LogEvent.LT_ERROR, "METADATA (UT): No valid msg_type=" + msg_type));
            }

            return null;
        } catch (Throwable e) {
            // what is the best way to handle this error?
            if (Logger.isEnabled()) {
                Logger.log(new LogEvent(LOGID, LogEvent.LT_ERROR, "METADATA (UT)", e));
            }
            return null;
        }
    }

    @Override
    public void destroy() {
        if (buffer != null) {
            buffer.returnToPool();
        }
    }
}