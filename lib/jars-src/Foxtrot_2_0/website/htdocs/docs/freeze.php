<?php include 'header.php';?>

<tr><td align="center" colspan="3">
<a href="http://foxtrot.sourceforge.net"><img class="nav-btn-en" hspace="10" src="../images/cnvhome.gif"/></a>
<a href="toc.php"><img class="nav-btn-en" hspace="10" src="../images/cnvup.gif"/></a>
<img class="nav-btn-dis" hspace="10" src="../images/cnvprev.gif"/>
<img class="nav-btn-dis" hspace="10" src="../images/cnvnext.gif"/>
</td></tr>

<tr><td class="date" colspan="3">Last Updated: $Date: 2002-08-14 14:16:47 -0400 (Wed, 14 Aug 2002) $</td></tr>

<tr><td class="documentation">

<h2>The GUI freeze problem</h2>

<p>When you write Swing applications, you show a GUI to a user; the user clicks on some components (buttons, menus, etc.)
to perform the desired action.<br>
The code that executes the action is written in event listeners methods, and event listeners are always executed in the
<b>Event Dispatch Thread</b>.<br>
The Event Dispatch Thread is responsible for taking one event after another and processing it; the processing involves calling
the event listeners's method, which are then executed. If an event listener requires a long time to be executed, then the
Event Dispatch Thread cannot process the next event, which will then be waiting in the Event Queue.<br>
If the pending event is a repaint event, the GUI cannot be repainted, so it appears to be <b>frozen</b>.<br>
So resizing your window, overlapping it with another window, clicking on other components, all these
events are queued but not processed until the time-consuming listener has finished. <br>
The user feels the application has hung.<br>
When the time-consuming listener finishes, all pending events are processed, and if they are quick to execute (like
repainting events) it appears they're are executed like a storm.</p>
<p>Take a look at the following code.</p>
<p>Let's concentrate on the button's listener (the <code>actionPerformed()</code> method): the first statement changes the text of the button. This
causes the JButton to post a repaint event to the Event Queue, which is not be executed until the method finishes.<br>
But the listener is waiting for 10 seconds, so what happens is that the button remains pressed, and its text doesn't
change. After 10 seconds have passed, another button-text-change method is called, thus posting another repaint
event to the Event Queue, but still, this listener has not finished so the two events posted by the button-text-change methods
are not dequeued and processed.<br>
When the listener finishes, on the way back, Swing can take care of repainting the button according to the two events previously posted,
and the other pending
events in the Event Queue finally get a chance to be executed, too.<br>
Being repaint events, the two button events are executed quickly. So first the button text is changed to "Sleeping...", and immediately
after to "Slept !", too quick for the eye to see.<br>
Before that, during 10 seconds, the GUI was <b>frozen</b>.</p>
<table width="100%" cellspacing="0" cellpadding="0">
<tr><td width="60%">
<pre><span class="code">
public class FreezeExample extends JFrame
{
   public static void main(String[] args)
   {</span><span class="main">
      FreezeExample example = new FreezeExample();
      example.setVisible(true);</span><span class="code">
   }

   public FreezeExample()
   {</span><span class="main">
      super("Freeze Example");

      final JButton button = new JButton("Take a nap !");
      button.addActionListener(new ActionListener()</span><span class="code">
      {
         public void actionPerformed(ActionEvent e)
         {</span><span class="event">
            button.setText("Sleeping...");
            try {Thread.sleep(10000);}
            catch (Exception ignored) {}
            button.setText("Slept !");</span><span class="code">
         }
      }</span><span class="main">);

      setDefaultCloseOperation(EXIT_ON_CLOSE);

      Container c = getContentPane();
      c.setLayout(new GridBagLayout());
      c.add(button);

      setSize(300,200);

      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      int x = (screen.width - size.width) >> 1;
      int y = (screen.height - size.height) >> 1;
      setLocation(x, y);</span><span class="code">
   }
}</span>
</pre>
</td>
<td valign="top" align="left">
<table class="legend" width="50%" cellspacing="0" cellpadding="0">
<tr><td class="legend">Legend</td></tr>
<tr><td class="legend-entry"><span class="main">Main Thread</span></td></tr>
<tr><td class="legend-entry"><span class="event">Event Dispatch Thread</span></td></tr>
</table>
</td></tr>
</table>

</td></tr>

<?php include 'footer.php';?>
