package javazoom.spi.mpeg.sampled.file.tag;

public abstract interface MP3MetadataParser
{
  public abstract void addTagParseListener(TagParseListener paramTagParseListener);

  public abstract void removeTagParseListener(TagParseListener paramTagParseListener);

  public abstract MP3Tag[] getTags();
}

