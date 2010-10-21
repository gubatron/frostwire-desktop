package irc.style;

/**
 * This interface is used to notify that some drawn content has been updated.
 */
public interface FormattedStringDrawerListener
{
  /**
   * Image data is updated.
   */
  public static final int DATA=1;
  /**
   * Size is updated.
   */
  public static final int SIZE=2;
  /**
   * Frame is updated. 
   */
  public static final int FRAME=4;
  
  /**
   * The given handle has been updated.
   * @param handle updated handle.
   * @param what type of update, bitfield.
   * @return true if future handle update should be notified, false otherwise.
   */
  public Boolean displayUpdated(Object handle,Integer what);
}
