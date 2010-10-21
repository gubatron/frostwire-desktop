package org.tritonus.share.sampled.file;

import java.io.DataOutput;
import java.io.IOException;

public abstract interface TDataOutputStream extends DataOutput
{
  public abstract boolean supportsSeek();

  public abstract void seek(long paramLong)
    throws IOException;

  public abstract long getFilePointer()
    throws IOException;

  public abstract long length()
    throws IOException;

  public abstract void writeLittleEndian32(int paramInt)
    throws IOException;

  public abstract void writeLittleEndian16(short paramShort)
    throws IOException;

  public abstract void close()
    throws IOException;
}
