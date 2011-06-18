package irc.plugin.buttons;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;

// This appears in Core Web Programming from
// Prentice Hall Publishers, and may be freely used
// or adapted. 1997 Marty Hall, hall@apl.jhu.edu.

//======================================================
/**
 * A class for displaying smileys. It places the Smiley
 * into a canvas so that it can moved around by layout
 * managers, will get repainted automatically, etc.
 * No mouseXXX or action events are defined, so it is
 * most similar to the Label Component.
 * <P>
 * By default, with FlowLayout the SmileyLabel takes
 * its minimum size (just enclosing the smiley). The
 * default with BorderLayout is to expand to fill
 * the region in width (North/South), height
 * (East/West) or both (Center). This is the same
 * behavior as with the builtin Label class. If you
 * give an explicit setSize or
 * reshape call <B>before</B> adding the
 * SmileyLabel to the Container, this size will
 * override the defaults.
 * <P>
 * Here is an example of its use:
 * <P>
 * <PRE>
 * public class ShowSmileys extends Applet {
 *   private SmileyLabel smiley1, smiley2;
 *
 *   public void init() {
 *     smiley1 = new SmileyLabel(getCodeBase(),
 *                             "some-smiley.gif");
 *     smiley2 = new SmileyLabel(getCodeBase(),
 *                             "other-smiley.jpg");
 *     add(smiley1);
 *     add(smiley2);
 *   }
 * }
 * </PRE>
 *
 * @author Marty Hall (hall@apl.jhu.edu)
 * @see Icon
 * @see SmileyButton
 * @version 1.0 (1997)
 */

public class SmileyLabel extends Canvas
{
	//----------------------------------------------------
	// Instance variables.
	
	/**
     * 
     */
    private static final long serialVersionUID = -6659942014558080823L;

    // The actual Smiley drawn on the canvas. 
	private Image smiley;
	
	// A String corresponding to the URL of the smiley
	// you will get if you call the constructor with
	// no arguments.
	private static String defaultSmileyString
		= "http://localhost/" +
		"logo.java.color-transp.55x60.gif";
	
	// The URL of the smiley. But sometimes we will use
	// an existing smiley object (e.g. made by
	// createSmiley) for which this info will not be
	// available, so a default string is used here.
	private String smileyString = "<Existing Smiley>";
	
	// Turn this on to get verbose debugging messages. 
	private boolean debug = false;
	/** Amount of extra space around the smiley. */
	
	private int border = 0;
	
	/** If there is a non-zero border, what color should
	 *  it be? Default is to use the background color
	 *  of the Container.
	 */
	private Color borderColor = null;
	
	// Width and height of the Canvas. This is the
	//  width/height of the smiley plus twice the border.
	private int width, height;
	
	/** Determines if it will be sized automatically.
	 *  If the user issues a setSize() or setBounds()
	 *  call before adding the label to the Container,
	 *  or if the LayoutManager setSizes before
	 *  drawing (as with BorderLayout), then those sizes
	 *  override the default, which is to make the label
	 *  the same size as the smiley it holds (after
	 *  reserving space for the border, if any).
	 *  This flag notes this, so subclasses that
	 *  override SmileyLabel need to check this flag, and
	 *  if it is true, and they draw modified smiley,
	 *  then they need to draw them based on the width
	 *  height variables, not just blindly drawing them
	 *  full size.
	 */
	private boolean explicitSize = false;
	private int explicitWidth=0, explicitHeight=0;
	
	// The MediaTracker that can tell if smiley has been
	// loaded before trying to paint it or resize
	// based on its size.
	private MediaTracker tracker;
	
	// Used by MediaTracker to be sure smiley is loaded
	// before paint & resize, since you can't find out
	// the size until it is done loading.
	private static int lastTrackerID=0;
	private int currentTrackerID;
	private boolean doneLoading = false;
	
	private Container parentContainer;
	
	//----------------------------------------------------
	/** Create an SmileyLabel with the default smiley.
	 *
	 * @see #getDefaultSmileyString
	 * @see #setDefaultSmileyString
	 */
	// Remember that the funny "this()" syntax calls
	// constructor of same class
	public SmileyLabel() 
	{
		this(defaultSmileyString);
	}
	
	/** Create an SmileyLabel using the smiley at URL
	 *  specified by the string.
	 *
	 * @param smileyURLString A String specifying the
	 *   URL of the smiley.
	*/
	public SmileyLabel(String smileyURLString) 
	{
		this(makeURL(smileyURLString));
	}
	
	/** Create an SmileyLabel using the smiley at URL
	 *  specified.
	 *
	 * @param smileyURL The URL of the smiley.
	 */
	public SmileyLabel(URL smileyURL) 
	{
		this(loadSmiley(smileyURL));
		smileyString = smileyURL.toExternalForm();
	}
	
	/** Create an SmileyLabel using the smiley in the file
	 *  in the specified directory.
	 *
	 * @param smileyDirectory Directory containing smiley
	 * @param file Filename of smiley
	 */
	public SmileyLabel(URL smileyDirectory, String file) 
	{
		this(makeURL(smileyDirectory, file));
		smileyString = file;
	}
	
	/** Create an SmileyLabel using the smiley specified.
	 *  The other constructors eventually call this one,
	 *  but you may want to call it directly if you
	 *  already have an smiley (e.g. created via
	 *  createSmiley).
	 *
	 * @param smiley The smiley
	 */
	public SmileyLabel(Image smiley) 
	{
		this.smiley = smiley;
		tracker = new MediaTracker(this);
		currentTrackerID = lastTrackerID++;
		tracker.addImage(smiley, currentTrackerID);
	}
	
	//----------------------------------------------------
	/** Makes sure that the Smiley associated with the
	 *  Canvas is done loading before returning, since
	 *  loadSmiley spins off a separate thread to do the
	 *  loading. Once you get around to drawing the
	 *  smiley, this will make sure it is loaded,
	 *  waiting if not. The user does not need to call
	 *  this at all, but if several SmileyLabels are used
	 *  in the same Container, this can cause
	 *  several repeated layouts, so users might want to
	 *  explicitly call this themselves before adding
	 *  the SmileyLabel to the Container. Another
	 *  alternative is to start asynchronous loading by
	 *  calling prepareImage on the SmileyLabel's
	 *  smiley (see getImage). 
	 *
	 * @param doLayout Determines if the Container
	 *   should be re-laid out after you are finished
	 *    waiting. <B>This should be true when called
	 *   from user functions</B>, but is set to false
	 *   when called from preferredSize to avoid an
	 *   infinite loop. This is needed when
	 *   using BorderLayout, which calls preferredSize
	 *   <B>before</B> calling paint.
	 */
	public void waitForSmiley(boolean doLayout) 
	{
		if (!doneLoading) 
		{
			debug("[waitForSmiley] - Resizing and waiting for "
			+ smileyString);
			try { tracker.waitForID(currentTrackerID); } 
			catch (InterruptedException ie) {} 
			catch (Exception e) 
			{ 
				System.out.println("Error loading "
				+ smileyString + ": "
				+ e.getMessage()); 
				e.printStackTrace(); 
			} 
			if (tracker.isErrorID(0)) 
				new Throwable("Error loading smiley "
			+ smileyString).printStackTrace();
			doneLoading = true;
			if (explicitWidth != 0)
				width = explicitWidth;
			else
				width = smiley.getWidth(this) + 2*border;
			if (explicitHeight != 0)
				height = explicitHeight;
			else
				height = smiley.getHeight(this) + 2*border;
	
		setSize(width, height);
			debug("[waitForSmiley] - " + smileyString + " is "
			+ width + "x" + height + ".");
			
			// If no parent, you are OK, since it will have
			// been resized before being added. But if
			// parent exists, you have already been added,
			// and the change in size requires re-layout. 
			if (((parentContainer = getParent()) != null) && doLayout) 
			{
				setBackground(parentContainer.getBackground());
				parentContainer.doLayout();
			}
		}
	}
	
	//----------------------------------------------------
	/** Moves the smiley so that it is <I>centered</I> at
	 *  the specified location, as opposed to the move
	 *  method of Component which places the top left
	 *  corner at the specified location.
	 *  <P>
	 *  <B>Note:</B> The effects of this could be undone
	 *  by the LayoutManager of the parent Container, if
	 *  it is using one. So this is normally only used
	 *  in conjunction with a null LayoutManager.
	 

	 * @param x The X coord of center of the smiley
	 *          (in parent's coordinate system)
	 * @param y The Y coord of center of the smiley
	 *          (in parent's coordinate system)
	 * @see java.awt.Component#move
	 */
	 
	public void centerAt(int x, int y) 
	{
		debug("Placing center of " + smileyString + " at ("
		+ x + "," + y + ")");
		setLocation(x - width/2, y - height/2); 
	}
	
	
	//----------------------------------------------------
	/** Draws the smiley. If you override this in a
	 *  subclass, be sure to call super.paint.
	 */
	public void paint(Graphics g) 
	{
		if (!doneLoading)
			waitForSmiley(true);
		else 
		{
			if (explicitSize)
				g.drawImage(smiley, border, border,
				width-2*border, height-2*border,
				this);
			else
				g.drawImage(smiley, border, border, this);
			drawRect(g, 0, 0, width-1, height-1,
				border, borderColor);
		}
	}
	
	//----------------------------------------------------
	/** Used by layout managers to calculate the usual
	 *  size allocated for the Component. Since some
	 *  layout managers (e.g. BorderLayout) may
	 *  call this before paint is called, you need to
	 *  make sure that the smiley is done loading, which
	 *  will force a resize, which determines the values
	 *  returned.
	 */
	public Dimension getPreferredSize() 
	{
		if (!doneLoading)
			waitForSmiley(false);
		return(super.getPreferredSize());
	}
	
	//----------------------------------------------------
	/** Used by layout managers to calculate the smallest
	 *  size allocated for the Component. Since some
	 *  layout managers (e.g. BorderLayout) may
	 *  call this before paint is called, you need to
	 *  make sure that the smiley is done loading, which
	 *  will force a resize, which determines the values
	 *  returned.
	 */
	public Dimension getMinimumSize() 
	{
		if (!doneLoading)
			waitForSmiley(false);
		return(super.getMinimumSize());
	}
	
	//----------------------------------------------------
	// LayoutManagers (such as BorderLayout) might call
	// resize or setBounds with only 1 dimension of
	// width/height non-zero. In such a case, you still
	// want the other dimension to come from the smiley
	// itself.
	/** SetSizes the SmileyLabel. If you don't resize the
	 *  label explicitly, then what happens depends on
	 *  the layout manager. With FlowLayout, as with
	 *  FlowLayout for Labels, the SmileyLabel takes its
	 *  minimum size, just enclosing the smiley. With
	 *  BorderLayout, as with BorderLayout for Labels,
	 *  the SmileyLabel is expanded to fill the
	 *  section. Stretching GIF/JPG files does not always
	 *  result in clear looking smileys. <B>So just as
	 *  with builtin Labels and Buttons, don't
	 *  use FlowLayout if you don't want the Buttons to
	 *  get resized.</B> If you don't use any
	 *  LayoutManager, then the SmileyLabel will also
	 *  just fit the smiley.
	 *  <P>
	 *  Note that if you resize explicitly, you must do
	 *  it <B>before</B> the SmileyLabel is added to the
	 *  Container. In such a case, the explicit size
	 *  overrides the smiley dimensions.
	 *
	 * @see #setBounds
	 */
	public void setSize(int width, int height) 
	{
		if (!doneLoading) 
		{
			explicitSize=true;
			if (width > 0)
				explicitWidth=width;
			if (height > 0)
				explicitHeight=height;
		}
		super.setSize(width, height);
	}
	
	/** Resizes the SmileyLabel. If you don't resize the
	 *  label explicitly, then what happens depends on
	 *  the layout manager. With FlowLayout, as with
	 *  FlowLayout for Labels, the SmileyLabel takes its
	 *  minimum size, just enclosing the smiley. With
	 *  BorderLayout, as with BorderLayout for Labels,
	 *  the SmileyLabel is expanded to fill the
	 *  section. Stretching GIF/JPG files does not always
	 *  result in clear looking smileys. <B>So just as
	 *  with builtin Labels and Buttons, don't
	 *  use FlowLayout if you don't want the Buttons to
	 *  get setSized.</B> If you don't use any
	 *  LayoutManager, then the SmileyLabel will also   
	 *  just fit the smiley.
	 *  <P>
	 *  Note that if you resize explicitly, you must do
	 *  it <B>before</B> the SmileyLabel is added to the
	 *  Container. In such a case, the explicit size
	 *  overrides the smiley dimensions.
	 *
	 * @see #resize
	 */
	public void setBounds(int x, int y, int width, int height) 
	{
		if (!doneLoading) 
		{
			explicitSize=true;
			if (width > 0)
				explicitWidth=width;
			if (height > 0)
				explicitHeight=height;
		}
		super.setBounds(x, y, width, height);
	}
	
	//----------------------------------------------------
	// You can't just set the background color to
	// the borderColor and skip drawing the border,
	// since it messes up transparent gifs. You
	// need the background color to be the same as
	// the container.
	
	/** Draws a rectangle with the specified OUTSIDE
	 *  left, top, width, and height.
	 *  Used to draw the border.
	 */
	protected void drawRect(Graphics g, int left, int top, int width, int height, int lineThickness, Color rectangleColor) 
	{
		g.setColor(rectangleColor);
		for(int i=0; i<lineThickness; i++) 
		{
			g.drawRect(left, top, width, height);
			if (i < lineThickness-1) 
			{  
				left = left + 1;
				top = top + 1;
				width = width - 2;
				height = height - 2;
			}
		}
	}
	
	public boolean isLoaded()
	{
		return doneLoading;
	}

	//----------------------------------------------------
	/** Calls System.out.println if the debug variable
	 *  is true; does nothing otherwise.
	 *
	 * @param message The String to be printed.
	 */
	protected void debug(String message) 
	{
		if (debug)
			System.out.println(message);
	}
	
	//----------------------------------------------------
	// Creates the URL with some error checking.
	private static URL makeURL(String s) 
	{
		URL u = null;
		try { u = new URL(s); }
		catch (MalformedURLException mue) 
		{
			System.out.println("Bad URL " + s + ": " + mue);
			mue.printStackTrace();
		}
		return(u);
	}
	
	private static URL makeURL(URL directory, String file)
	{
		URL u = null;
		try { u = new URL(directory, file); }
		catch (MalformedURLException mue) 
		{
			System.out.println("Bad URL " +
				directory.toExternalForm() +
				", " + file + ": " + mue);
			mue.printStackTrace();
		}
		return(u);
	}
	
	//----------------------------------------------------
	// Loads the smiley. Needs to be static since it is
	// called by the constructor.
	private static Image loadSmiley(URL url) 
	{
		return(Toolkit.getDefaultToolkit().getImage(url));
	}
	
	//----------------------------------------------------  
	/** The Smiley associated with the SmileyLabel. */
	public Image getSmiley() 
	{
		return(smiley);
	}
	
	//----------------------------------------------------
	/** Gets the border width. */
	public int getBorder() 
	{
		return(border);
	}
	
	/** Sets the border thickness. */
	public void setBorder(int border) 
	{
		this.border = border;
	}
	
	//----------------------------------------------------
	/** Gets the border color. */
	public Color getBorderColor() 
	{
		return(borderColor);
	}
	
	/** Sets the border color. */
	public void setBorderColor(Color borderColor) 
	{
		this.borderColor = borderColor;
	}
	
	//----------------------------------------------------
	// You could just call size().width and size().height,
	// but since we've overridden resize to record
	// this, we might as well use it.
	/** Gets the width (smiley width plus twice border). */
	public int getWidth() 
	{
		return(width);
	}
	
	/** Gets the height (smiley height plus 2x border). */
	public int getHeight() 
	{
		return(height);
	}
	
	//----------------------------------------------------
	/** Has the SmileyLabel been given an explicit size?
	 *  This is used to decide if the smiley should be
	 *  stretched or not. This will be true if you
	 *  call resize or setBounds on the SmileyLabel before
	 *  adding it to a Container. It will be false
	 *  otherwise.
	 */
	protected boolean hasExplicitSize() 
	{
		return(explicitSize);
	}
	
	//----------------------------------------------------
	/** Returns the string representing the URL that
	 *  will be used if none is supplied in the
	 *  constructor.
	 */
	public static String getDefaultSmileyString() 
	{
		return(defaultSmileyString);
	}
	
	/** Sets the string representing the URL that
	 *  will be used if none is supplied in the
	 *  constructor. Note that this is static,
	 *  so is shared by all SmileyLabels. Using this
	 *  might be convenient in testing, but "real"
	 *  applications should avoid it.
	 */
	public static void setDefaultSmileyString(String file) 
	{
		defaultSmileyString = file;
	}
	
	//----------------------------------------------------
	/** Returns the string representing the URL
	 *  of smiley.
	 */
	protected String getSmileyString() 
	{
		return(smileyString);
	}
	
	//----------------------------------------------------
	/** Is the debugging flag set? */
	public boolean isDebugging() 
	{
		return(debug);
	}
	
	/** Set the debugging flag. Verbose messages
	 *  will be printed to System.out if this is true.
	 */
	public void setIsDebugging(boolean debug) 
	{
		this.debug = debug;
	}
	
	//----------------------------------------------------
}
