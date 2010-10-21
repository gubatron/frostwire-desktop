<?php include 'header.php';?>

<tr><td align="center" colspan="3">
<a href="http://foxtrot.sourceforge.net"><img class="nav-btn-en" hspace="10" src="../images/cnvhome.gif"/></a>
<a href="toc.php"><img class="nav-btn-en" hspace="10" src="../images/cnvup.gif"/></a>
<img class="nav-btn-dis" hspace="10" src="../images/cnvprev.gif"/>
<img class="nav-btn-dis" hspace="10" src="../images/cnvnext.gif"/>
</td></tr>

<tr><td class="date" colspan="3">Last Updated: $Date: 2002-12-25 16:54:50 -0500 (Wed, 25 Dec 2002) $</td></tr>

<tr><td class="documentation">

<h2>Foxtrot API</h2>
<p>The Foxtrot API is very small and simple, and consists of 3 main classes:</p>
<ul>
<li><code>class foxtrot.Worker</code></li>
<li><code>class foxtrot.Task</code></li>
<li><code>class foxtrot.Job</code></li>
</ul>
<p>From Foxtrot 2.x, the API has been extended to allow customization of the part that handles event pumping
and of the part that handles execution of <code>Task</code>s and <code>Job</code>s in a worker thread, via
the following classes:</p>
<ul>
<li><code>interface foxtrot.EventPump</code></li>
<li><code>interface foxtrot.WorkerThread</code></li>
<li><code>class foxtrot.AbstractWorkerThread</code></li>
</ul>
<p>Normally users do not need to deal with these classes to use Foxtrot in their Swing applications, since
Foxtrot will configure itself with the most suitable implementations; however, if
a specific customization of the event pumping mechanism or of the worker thread mechanism is needed, the APIs
provided by these classes allow fine grained control on Foxtrot's behavior.</p>

<h2>Foxtrot API Details</h2>
<p>The <code>Worker</code> class is used to post <code>Task</code>s or <code>Job</code>s that will be executed
in the Foxtrot Worker Thread.</p>
<p>The <code>Task</code> class is subclassed by the user to perform heavy tasks that throw checked exceptions.</p>
<p>The <code>Job</code> class, conversely, is subclassed by the user to perform heavy tasks that do not throw
checked exceptions, but only RuntimeExceptions (or Errors).</p>
<p>The <code>Worker</code> class has the following 2 public methods that can be used to post
<code>Task</code>s or <code>Job</code>s:</p>
<ul>
<li><code>public static Object post(Task task) throws Exception</code></li>
<li><code>public static Object post(Job job)</code></li>
</ul>
<p>The <code>Task</code> class has a single abstract method that must be implemented by the user, with the
time-consuming code that may throw checked exceptions:</p>
<ul>
<li><code>public abstract Object run() throws Exception</code></li>
</ul>
<p>The <code>Job</code> class, conversely, has a single abstract method that must be implemented by the user,
with the time-consuming code that does not throw checked exceptions:</p>
<ul>
<li><code>public abstract Object run()</code></li>
</ul>
<p>The exceptions or errors thrown inside the <code>Task.run()</code> or <code>Job.run()</code> methods are
re-thrown by the corrispondent <code>Worker.post(...)</code> method <b>as is</b>, i.e. without being wrapped into,
for example, an InvocationTargetException.</p>
<p>The usage is very simple; here's an example with the <code>Job</code> class:</p>
<pre><span class="code">
Worker.post(new Job()
{
   public Object run()
   {
      // Here write the time-consuming code
      // that does not throw checked exceptions
   }
});
</span></pre>
<p>and here's an example with the <code>Task</code> class:</p>
<pre><span class="code">
try
{
   Worker.post(new Task()
   {
      public Object run() throws Exception
      {
         // Here write the time-consuming code
         // that may throw checked exceptions
      }
   });
}
catch (Exception x)
{
   // Handle the exception thrown by the Task
}
</span></pre>
<p>It is possible to narrow the throws clause of the <code>Task</code> class, but unfortunately not the one of
the <code>Worker</code> class. <br>
So, when using the <code>Worker.post(Task task)</code> method, you have to surround it in a
<code>try...catch(Exception x)</code> block (unless the method that contains <code>Worker.post(Task task)</code>
throws <code>Exception</code> itself).</p>
<pre><span class="code">
try
{
   Worker.post(new Task()
   {
      public Object run() throws FileNotFoundException
      {
         // Here write the time-consuming code
         // that accesses the file system
      }
   });
}
catch (FileNotFoundException x)
{
   // Handle the exception or rethrow.
}
catch (RuntimeException x)
{
   // RuntimeExceptions are always possible.
   // Catch them here to prevent they are
   // ignored by the catch(Exception ignored)
   // block below.
   throw x;
}
catch (Exception ignored)
{
   // No other checked exceptions are thrown
   // by the Task (the compiler will enforce this),
   // so we can safely ignore it, but we're forced
   // to write this catch block: Worker.post(Task t)
   // requires it.
}
</span></pre>

<p>The <code>Worker</code> class, from Foxtrot 2.x, has the following public methods to deal with the
<code>EventPump</code> and <code>WorkerThread</code> components:
<ul>
<li><code>public static EventPump getEventPump()</code></li>
<li><code>public static void setEventPump(EventPump pump)</code></li>
<li><code>public static WorkerThread getWorkerThread()</code></li>
<li><code>public static void setWorkerThread(WorkerThread worker)</code></li>
</ul>
<p>Foxtrot configures itself automatically with the most suitable implementation of
<code>EventPump</code> and <code>WorkerThread</code>.
Some implementations of <code>EventPump</code> or <code>WorkerThread</code> allow an even further customization
of the component.</p>
<p>For example, implementations of <code>EventPump</code> that also implement the
<code>foxtrot.pumps.EventFilterable</code> interface may allow the user to filter events that are being dispatched
by the <code>java.awt.EventQueue</code>. See also the bundled Javadocs for further details. <br>
However, it is recommended not to exploit these features unless knowing <strong>exactly</strong> what one is doing:
Foxtrot's defaults may change from version to version to suit better implementations, and these defaults may depend
on the Java Runtime Environment version Foxtrot is running on, so that features working in JDK 1.3.x may not work
in JDK 1.4.x or viceversa. <br>
Playing with AWT events too badly is normally looking for troubles, so consider you warned :)</p>
<p>The same holds for <code>WorkerThread</code> implementations, that should extend the abstract class
<code>AbstractWorkerThread</code>: Foxtrot uses a <strong>synchronous</strong> model, so replacing (for example)
the default <code>WorkerThread</code> implementation (that uses a single worker thread) with an implementation
that uses multiple pooled threads does not lead (in the common case) to a great speed-up of <code>Task</code>s
execution. <br>
To be more precise, with a multiple thread implementation of <code>WorkerThread</code>, <code>Tasks</code> posted
from pumped events will return control to the initial caller after a time that is (roughly) the time of the
longest <code>Task</code>, while with the default implementation will be after a time that is (roughly) the sum
of the times of all <code>Task</code>s posted from pumped events. <br>
<code>Task</code>s posted from pumped events are normally a rare case, and may not be worth the effort of
a multiple thread implementation of <code>WorkerThread</code>.</p>

<p>Refer to the bundled Javadoc documentation for further information, and to the bundled examples for
further details on how to use the Foxtrot classes with Swing. And do not forget the
<a href="tips.php">Tip 'n' Tricks</a> section !<p>

</td></tr>

<?php include 'footer.php';?>
