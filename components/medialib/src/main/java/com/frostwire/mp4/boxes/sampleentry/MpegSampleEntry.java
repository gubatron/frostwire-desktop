package com.frostwire.mp4.boxes.sampleentry;

import com.frostwire.mp4.BoxParser;
import com.frostwire.mp4.boxes.Box;
import com.frostwire.mp4.boxes.ContainerBox;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MpegSampleEntry extends SampleEntry implements ContainerBox {

    @SuppressWarnings("unused")
    private BoxParser boxParser;

    public MpegSampleEntry(String type) {
        super(type);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        _parseChildBoxes(content);

    }

    @Override
    protected long getContentSize() {
        long contentSize = 8;
        for (Box boxe : boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    @SuppressWarnings("unchecked")
    public String toString() {
        return "MpegSampleEntry" + Arrays.asList(getBoxes());
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        _writeChildBoxes(byteBuffer);
    }
}
