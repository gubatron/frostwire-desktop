/**
 * Copyright (c) 2002, Simone Bordet
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package foxtrot.test;

import javax.swing.SwingUtilities;

import junit.framework.TestCase;
import foxtrot.Worker;
import foxtrot.Job;
import foxtrot.WorkerThread;

/**
 * Base class for Foxtrot tests
 *
 * @author <a href="mailto:biorn_steedom@users.sourceforge.net">Simone Bordet</a>
 * @version $Revision: 160 $
 */
public class FoxtrotTestCase extends TestCase
{
   protected FoxtrotTestCase(String s)
   {
      super(s);
   }

   /**
    * Invokes the given Runnable in the AWT Event Dispatch Thread and waits for its
    * completion, either by returning or by throwing.
    */
   protected void invokeTest(final Runnable run) throws Exception
   {
      final Object lock = new Object();
      final MutableInteger barrier = new MutableInteger(0);
      final ThrowableHolder throwable = new ThrowableHolder();

      synchronized (lock)
      {
         throwable.set(null);
      }

      // This call returns immediately. It posts on the AWT Event Queue
      // a Runnable that is executed.
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            // Wait to run the test until we're ready
            synchronized (lock)
            {
               while (barrier.get() < 1)
               {
                  try
                  {
                     lock.wait();
                  }
                  catch (InterruptedException ignored)
                  {
                     Thread.currentThread().interrupt();
                     break;
                  }
               }
            }

            // Run the test and collect exception thrown
            try
            {
               run.run();
            }
            catch (Throwable x)
            {
               synchronized (lock)
               {
                  throwable.set(x);
               }
            }

            // Here the test should be finished.
            // However, there may be tasks pending in both the WorkerThread queue
            // and in EventQueue, so here I guarantee that those pending will be
            // executed before starting the next test
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  WorkerThread workerThread = Worker.getWorkerThread();
                  if (!workerThread.isAlive()) workerThread.start();
                  workerThread.postTask(new Job()
                  {
                     public Object run()
                     {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                           public void run()
                           {
                              synchronized (lock)
                              {
                                 barrier.set(0);
                                 lock.notifyAll();
                              }
                           }
                        });
                        return null;
                     }
                  });
               }
            });
         }
      });

      // The call above returns immediately.
      // Here we wait for the test to be finished.
      synchronized (lock)
      {
         barrier.set(1);
         lock.notifyAll();

         while (barrier.get() > 0) lock.wait();

         Throwable t = throwable.get();
         if (t instanceof Error) throw (Error)t;
         if (t instanceof Exception) throw (Exception)t;
      }
   }

   protected void sleep(long ms)
   {
      try
      {
         Thread.sleep(ms);
      }
      catch (InterruptedException ignored)
      {
      }
   }

   protected boolean isJRE14()
   {
      return loadClass("java.awt.SequencedEvent") != null;
   }

   protected boolean isJRE140()
   {
      Class cls = loadClass("java.awt.SequencedEvent");
      if (cls == null) return false;
      try
      {
         cls.getDeclaredMethod("getFirst", null);
         return false;
      }
      catch (NoSuchMethodException x)
      {
      }
      return true;
   }

   protected boolean isJRE141()
   {
      Class cls = loadClass("java.awt.SequencedEvent");
      if (cls == null) return false;
      try
      {
         cls.getDeclaredMethod("getFirst", null);
         return true;
      }
      catch (NoSuchMethodException x)
      {
      }
      return false;
   }

   private Class loadClass(String className)
   {
      // We ask directly to the boot classloader
      try
      {
         return Class.forName(className, false, null);
      }
      catch (ClassNotFoundException ignored)
      {
      }
      return null;
   }

   private class ThrowableHolder
   {
      private Throwable throwable;

      private Throwable get()
      {
         return throwable;
      }

      private void set(Throwable t)
      {
         throwable = t;
      }
   }
}
