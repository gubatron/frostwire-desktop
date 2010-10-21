<?php include 'header.php';?>

<tr><td align="center" colspan="3">
<a href="http://foxtrot.sourceforge.net"><img class="nav-btn-en" hspace="10" src="../images/cnvhome.gif"/></a>
<a href="toc.php"><img class="nav-btn-en" hspace="10" src="../images/cnvup.gif"/></a>
<img class="nav-btn-dis" hspace="10" src="../images/cnvprev.gif"/>
<img class="nav-btn-dis" hspace="10" src="../images/cnvnext.gif"/>
</td></tr>

<tr><td class="date" colspan="3">Last Updated: $Date: 2002-08-14 14:16:47 -0400 (Wed, 14 Aug 2002) $</td></tr>

<tr><td class="documentation">

<h2>SwingWorker: asynchronous solution</h2>

<p>Solutions have been proposed for the <a href="freeze.php">GUI freeze problem</a>; asynchronous solutions
rely on the combined usage of a worker thread and the <code>SwingUtilities.invokeLater()</code> method. We will see in few lines why
they're called asynchronous.</p>
<p>The main idea behind asynchronous solution is to return quickly from the time-consuming listener, after having
delegated the time-consuming task to a worker thread. The worker thread has to do 2 things:
<ul>
<li>Execute the time-consuming task
<li>Post an event to the Event Queue using <code>SwingUtilities.invokeLater()</code>
</ul>
</p>
<p>Take a look at the code below which uses the <b>SwingWorker</b>, which is an asynchronous solution.</p>
<p>Let's concentrate on the button's listener (the <code>actionPerformed()</code> method): the first statement, as in the freeze example, changes
the text of the button and thus posts a repaint event to the Event Queue.<br>
The next statement creates a SwingWorker object and starts it. This operation is quick, and non blocking.
When a SwingWorker is started, a worker thread is also started for executing the code contained in <code>construct()</code>;
when the <code>construct()</code> method ends, the <code>finished()</code> method is called (using
<code>SwingUtilities.invokeLater()</code>) and executed in the Event Dispatch Thread.<br>
So we create the SwingWorker, we start it, the listener finishes and returns; the Event Dispatch Thread
can thus dequeue the next event and process it (very likely this event is the one posted by the first statement,
that changes the button's text to "Sleeping...") .<br>
When the worker thread finishes, the <code>finished()</code> method is posted as event in the Event Queue, and again the
Event Dispatch Thread can dequeue it and process it, finally calling <code>finished()</code>.<br>
This is why these solutions are called asynchronous: they let the event listener return immediately, and the code the
listener is supposed to execute is run asynchronously, while the listener still returns.
</p>
<p>This solution, while resolving the freeze problem, has several drawbacks:
<ul>
<li>Note the ugly exception handling. Even in this simple example, 3 chained if-else statements are required. Furthermore,
the exception handling is done inside the SwingWorker, not inside the listener: there is no simple way to rethrow exceptions
(it is possible but needs more coding).
<li>Note the asymmetry: the first <code>setText()</code> is made outside the SwingWorker, the second inside of it
<li>Note the confusing <code>get()</code> method: if <code>construct()</code> returns null (because the operation had a void return
value - like <code>Thread.sleep()</code>), it is easy to forget to call <code>get()</code> to see if any exception was thrown by the
time-consuming code.
<li>What happens if the time-consuming code stays the same, but we want to execute 2 different <code>finished()</code> methods
depending on the place from where we want to execute the time-consuming task ?
<li>If some code is written after <code>SwingWorker.start()</code>, it will be always executed <em>before</em> <code>finished()</code>.
Thus looking at the code, we see:
<ul>
<li><code>setText("Sleeping...")</code>
<li><code>Thread.sleep()</code>
<li><code>setText("Slept !");</code>
<li><code>somethingElse()</code>
</ul>
but the real order of execution is:
<ul>
<li><code>setText("Sleeping...")</code>
<li><code>somethingElse()</code> [before, concurrently or after <code>Thread.sleep()</code>]
<li><code>Thread.sleep()</code>
<li><code>setText("Slept !");</code>
</ul>
making debugging and code readability very difficult.<br>
This is why a golden rule of the SwingWorker is to never put code after <code>SwingWorker.start()</code>.
<li>If the code inside <code>finished()</code> requires a new time-consuming operation, a new <em>nested</em> SwingWorker should be used,
making the code complex and obscure, especially with respect to the sequence order of the operations executed.
</ul>
</p>
<p>Fortunately, <a href="foxtrot.php">Foxtrot</a> solves these issues.</p>
<table width="100%" cellspacing="0" cellpadding="0">
<tr><td width="60%">
<pre><span class="code">
public class AsyncExample extends JFrame
{
   public static void main(String[] args)
   {</span><span class="main">
      AsyncExample example = new AsyncExample();
      example.setVisible(true);</span><span class="code">
   }

   public AsyncExample()
   {</span><span class="main">
      super("SwingWorker Example");

      final JButton button = new JButton("Take a nap !");
      button.addActionListener(new ActionListener()</span><span class="code">
      {
         public void actionPerformed(ActionEvent e)
         {</span><span class="event">
            button.setText("Sleeping...");

            new SwingWorker()</span><span class="code">
            {
               protected Object construct() throws Exception
               {</span><span class="worker">
                  Thread.sleep(10000);
                  return "Slept !";</span><span class="code">
               }
               protected void finished()
               {
                  try
                  {</span><span class="event">
                     String text = (String)get();
                     button.setText(text);</span><span class="code">
                  }
                  catch (InterruptedException ignored) {}
                  catch (InvocationTargetException x)
                  {</span><span class="event">
                     // Do exception handling
                     Throwable t = x.getException();
                     if (t instanceof InterruptedException) ...
                     else if (t instanceof RuntimeException) ...
                     else if (t instanceof Error) ...</span><span class="code">
                  }
               }
            }</span><span class="event">.start();

            somethingElse();</span><span class="code">
         }
      });</span><span class="main">

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
<tr><td class="legend-entry"><span class="worker">SwingWorker Thread</span></td></tr>
</table>
</td></tr>
</table>

</td></tr>

<?php include 'footer.php';?>
