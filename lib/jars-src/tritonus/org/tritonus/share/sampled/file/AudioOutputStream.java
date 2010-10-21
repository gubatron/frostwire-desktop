package org.tritonus.share.sampled.file;

import java.io.IOException;
import javax.sound.sampled.AudioFormat;

public abstract interface AudioOutputStream
{
  public abstract AudioFormat getFormat();

  public abstract long getLength();

  public abstract int write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;

  public abstract void close()
    throws IOException;
}
