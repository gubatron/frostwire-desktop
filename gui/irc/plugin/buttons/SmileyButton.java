package irc.plugin.buttons;

import irc.IRCApplication;

import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.net.URL;

import javax.swing.Icon;

//======================================================
/**
 * A button class that uses an smiley instead of a
 * textual label. Clicking and releasing the mouse over
 * the button triggers an ACTION_EVENT, so you can add
 * behavior in the same two ways as you with a normal
 * Button (in Java 1.0):
 * <OL>
 *  <LI>Make an SmileyButton subclass and put the
 *      behavior in the action method of that subclass.
 *  <LI>Use the main SmileyButton class but then catch
 *      the events in the action method of the Container.
 * </OL>
 * <P>
 * Normally, the SmileyButton's preferredSize (used,
 * for instance, by FlowLayout) is just big enough
 * to hold the smiley. However, if you give an explicit
 * resize or reshape call <B>before</B> adding the
 * SmileyButton to the Container, this size will
 * override the defaults.
 * <P>
 * @author Marty Hall (hall@apl.jhu.edu)
 * @see Icon
 * @see SmileyGrayFilter
 * @version 1.0 (1997)
 */

public class SmileyButton extends SmileyLabel implements MouseListener
{
	//----------------------------------------------------
	/** Default width of 3D border around smiley.
	 *  Currently 4.
	 * @see SmileyLabel#setBorder
	 * @see SmileyLabel#getBorder
	 */
	protected static final int defaultBorderWidth = 4;
	protected ActionListener actionListener = null;
	private String _smileyID;
	private IRCApplication _appl;
	
	/** Default color of 3D border around smiley.
	 *  Currently a gray with R/G/B of 160/160/160.
	 *  Light grays look best.
	 * @see SmileyLabel#setBorderColor
	 * @see SmileyLabel#getBorderColor
	 */
	protected static final Color defaultBorderColor =  new Color(160, 160, 160);
	private boolean mouseIsDown = false;
	//----------------------------------------------------
	// Constructors
	/** Create an SmileyButton with the default smiley.
	 * @see SmileyLabel#getDefaultSmileyString
	 */
	public SmileyButton() 
	{
		super();
		setBorders();
		_smileyID=(getDefaultSmileyString());
	}
	
	/** Create an SmileyButton using the smiley at URL
	 *  specified by the string.
	 * @param smileyURLString A String specifying the URL
	 *        of the smiley.
	 */
	public SmileyButton(String smileyURLString) 
	{
		super(smileyURLString);
		setBorders();
		_smileyID=(getDefaultSmileyString());
	}
	
	/** Create an SmileyButton using the smiley at URL
	 *  specified.
	 * @param smileyURL The URL of the smiley.
	 */
	public SmileyButton(URL smileyURL) 
	{
		super(smileyURL);
		setBorders();
		_smileyID=(getDefaultSmileyString());
	}
	
	/** Creates an SmileyButton using the file in
	 *  the directory specified.
	 * @param smileyDirectory The URL of a directory
	 * @param smileyFile File in the above directory
	 */
	public SmileyButton(URL smileyDirectory, String smileyFile) 
	{
		super(smileyDirectory, smileyFile);
		setBorders();
		_smileyID=(getDefaultSmileyString());
	}
	
	/** Create an SmileyButton using the smiley specified.
	 *  You would only want to use this if you already
	 *  have an smiley (e.g. created via createSmiley).
	 * @param smiley The smiley.
	 */
	public SmileyButton(Image smiley) 
	{
		super(smiley);
		setBorders();
		_smileyID=(getDefaultSmileyString());
	}
	
	//----------------------------------------------------
	/** Draws the smiley with the border around it. If you
	 *  override this in a subclass, call super.paint().
	 */
	public void paint(Graphics g) 
	{
		super.paint(g);   
		if (graySmiley == null) 
			createGraySmiley(g);
		drawBorder(true);
	}
	
	public void addActionListener(ActionListener l) 
	{
		actionListener = AWTEventMulticaster.add(actionListener,l);
	}
	
	public void removeActionListener(ActionListener l) 
	{
		actionListener = AWTEventMulticaster.remove(actionListener, l);
	}

	public void processMouseEvent(MouseEvent e) {
		System.out.println("SmileyButton.processMouseEvent()");
	}	
	
	public void mouseClicked(MouseEvent e) {
		System.out.println("SmileyButton.mouseClicked()");
		getIRCApplication().setFieldText(_appl.getFieldText() + ":)");
		
		ActionEvent ae = new ActionEvent(this,0,getSmileyID());
		if (actionListener != null)  {
			System.out.println("it did have an action listener\n");
			actionListener.actionPerformed(ae);
		}
		mouseIsDown = false;
	}
	
	public void mouseEntered(MouseEvent e) {
		System.out.println("SmileyButton.mouseEntered");
		paint(getGraphics());
	}
	
	public void mouseExited(MouseEvent e) {
		System.out.println("SmileyButton.mouseExited");
		paint(getGraphics());
	}
	
	public void mousePressed(MouseEvent e) {
		System.out.println("SmileyButton.mousePressed");
		mouseIsDown = true;
		Graphics g = getGraphics();
		int border = getBorder();
		if (hasExplicitSize())
			g.drawImage(graySmiley, border, border,
			getWidth()-2*border,
			getHeight()-2*border,
			this);
		else
			g.drawImage(graySmiley, border, border, this);
		drawBorder(false);
	}
	
	public void mouseReleased(MouseEvent e) {
		System.out.println("SmileyButton.mouseReleased()");
		paint(getGraphics());
		mouseIsDown = false;
	}
	
	public String getSmileyID()
	{
		return _smileyID;
	}

	public void setSmileyID(String str)
	{
		_smileyID=str;
	}
	
	public IRCApplication getIRCApplication() {
		return _appl;
	}
	
	public void setIRCApplication(IRCApplication appl) {
		_appl = appl;
	}

	//----------------------------------------------------
	/** The darkness value to use for grayed smileys.
	 * @see #setDarkness
	 */
	public int getDarkness() 
	{
		return(darkness);
	}
	
	/** An int whose bits are combined via "and" ("&")
	 *  with the alpha, red, green, and blue bits of the
	 *  pixels of the smiley to produce the grayed-out
	 *  smiley to use when button is depressed.
	 *  Default is 0xffafafaf: af combines with r/g/b
	 *  to darken smiley.
	 */
	public void setDarkness(int darkness) 
	{
		this.darkness = darkness;
	}
	
	// Changing darker is consistent with regular buttons
	private int darkness = 0xffafafaf;
	
	//----------------------------------------------------
	/** The gray smiley used when button is down.
	 * @see #setGraySmiley
	 */
	public Image getGraySmiley() 
	{
		return(graySmiley);
	}
	
	/** Sets gray smiley created automatically from regular
	 *  smiley via an smiley filter to use when button is
	 *  depressed. You won't normally use this directly. 
	 */
	public void setGraySmiley(Image graySmiley) 
	{
		this.graySmiley = graySmiley;
	}
	
	private Image graySmiley = null;
	
	//----------------------------------------------------
	private void drawBorder(boolean isUp) 
	{
		Graphics g = getGraphics();
		g.setColor(getBorderColor());
		int left = 0;
		int top = 0;
		int width = getWidth();
		int height = getHeight();
		int border = getBorder();
		for(int i=0; i<border; i++) {
			g.draw3DRect(left, top, width, height, isUp);
			left++;
			top++;
			width = width - 2;
			height = height - 2;
		}
	}
	
	//----------------------------------------------------
	private void setBorders() 
	{
		setBorder(defaultBorderWidth);
		setBorderColor(defaultBorderColor);
	}
	
	//----------------------------------------------------
	// The first time the smiley is drawn, update() is
	// called, and the result does not come out correctly.
	// So this forces a brief draw on loadup, replaced
	// by real, non-gray smiley.
	
	private void createGraySmiley(Graphics g) 
	{
		ImageFilter filter = new SmileyGrayFilter(darkness);
		ImageProducer producer =
			new FilteredImageSource(getSmiley().getSource(),
			filter);
		graySmiley = createImage(producer);
		int border = getBorder();
		if (hasExplicitSize())
			prepareImage(graySmiley, getWidth()-2*border,
			getHeight()-2*border, this);
		else
			prepareImage(graySmiley, this);
		super.paint(g);
	}
	
	//----------------------------------------------------
}

//======================================================

/** Builds a smiley filter that can be used to gray-out
 *  the smiley.
 * @see SmileyButton
 */
class SmileyGrayFilter extends RGBImageFilter 
{
	//----------------------------------------------------
	private int darkness = 0xffafafaf;
	//----------------------------------------------------
	public SmileyGrayFilter() 
	{
		canFilterIndexColorModel = true;
	}
	
	public SmileyGrayFilter(int darkness) 
	{
		this();
		this.darkness = darkness;
	}
	
	//----------------------------------------------------
	public int filterRGB(int x, int y, int rgb) 
	{
		return(rgb & darkness);
	}
	
	//----------------------------------------------------
}
