package javazoom.spi.mpeg.sampled.file.tag;

import java.util.EventListener;

public abstract interface TagParseListener extends EventListener
{
  public abstract void tagParsed(TagParseEvent paramTagParseEvent);
}

