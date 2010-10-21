package irc;

import java.awt.Image;

/**
 * SmileyItem.
 */
public class SmileyItem
{
  /**
   * Matching string.
   */
  public String match;
  /**
   * Matching image.
   */
  public Image img;

  /**
   * Create a new SmileyItem
   * @param amatch matching string.
   * @param aimg matching image.
   */
  public SmileyItem(String amatch,Image aimg)
	{
	  this.match=amatch;
		this.img=aimg;
	}
}