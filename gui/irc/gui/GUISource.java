package irc.gui;

import irc.*;

/**
 * The GUI source.
 */
public interface GUISource
{
  /**
   * Set the current textfield text.
   * @param txt new textfield text.
   */
  public void setFieldText(String txt);

  /**
   * Get the current textfield text.
   * @return the current textfield text.
   */
  public String getFieldText();

  /**
   * Validate the current textfield text, as if user pressed return key.
   */
  public void validateText();

  /**
   * Get the source.
   * @return the source.
   */
  public Source getSource();

  /**
   * Request the keyboard focus on this awt source.
   */
  public void requestFocus();

  /**
   * Get the source title.
   * @return source title.
   */
  public String getTitle();
  
} //GUISource

