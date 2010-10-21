/**
 * Copyright (c) 2002-2008, Simone Bordet
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package foxtrot;

import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

import foxtrot.workers.SingleWorkerThread;
import junit.framework.TestCase;

/**
 * Base class for Foxtrot tests
 *
 * @version $Revision: 263 $
 */
public abstract class FoxtrotTestCase extends TestCase
{
    protected boolean debug = false;

    /**
     * Invokes the given Runnable in the AWT Event Dispatch Thread and waits for its
     * completion, either by returning or by throwing.
     * This is necessary since tests are run in the "main" thread by JUnit and instead
     * we have to start them from the AWT Event Dispatch Thread.
     */
    protected void invokeTest(final WorkerThread workerThread, final Runnable test, final Runnable callback) throws Exception
    {
        if (SwingUtilities.isEventDispatchThread()) fail("Tests cannot be invoked from the Event Dispatch Thread");

        final Object lock = new Object();
        final MutableInteger barrier = new MutableInteger(0);
        final MutableReference throwable = new MutableReference(null);

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

                if (debug) System.out.println("Running test " + this);
                // Run the test and collect exception thrown
                try
                {
                    test.run();
                }
                catch (Throwable x)
                {
                    synchronized (lock)
                    {
                        throwable.set(x);
                    }
                }

                if (debug) System.out.println("Test method completed, waiting for test completion for " + this);
                // Here the test method has returned.
                // However, there may be tasks pending in both the WorkerThread queue
                // and in EventQueue, so here I guarantee that those pending will be
                // executed before starting the next test
                try
                {
                    while (hasPendingTasks(workerThread))
                    {
                        if (debug) System.out.println("Pending tasks for test " + this);
                        sleep(100);
                    }
                }
                catch (Exception x)
                {
                    throwable.set(x);
                }
                if (debug) System.out.println("No more tasks running for test " + this);
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
            }
        });

        // The call above returns immediately.
        // Here we wait for the test to be finished.
        if (debug) System.out.println("Test " + this + " launched");
        synchronized (lock)
        {
            barrier.set(1);
            lock.notifyAll();

            while (barrier.get() > 0) lock.wait();
        }

        if (debug) System.out.println("Test " + this + " consumed all AWT events");

        try
        {
            // Here the test is really finished
            if (callback != null) callback.run();

            synchronized (lock)
            {
                Throwable t = (Throwable)throwable.get();
                if (t instanceof Error) throw (Error)t;
                if (t instanceof Exception) throw (Exception)t;
            }
        }
        finally
        {
            if (debug) System.out.println("Test " + this + " completed");
            if (debug) System.out.println();
        }
    }

    protected void sleep(long ms)
    {
        if (ms <= 0) return;
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException x)
        {
            Thread.currentThread().interrupt();
        }
    }

    protected boolean isJRE13()
    {
        return JREVersion.isJRE13();
    }

    protected boolean isJRE14()
    {
        return JREVersion.isJRE14();
    }

    private boolean hasPendingTasks(WorkerThread workerThread) throws Exception
    {
        if (workerThread == null) return false;
        if (workerThread instanceof SingleWorkerThread)
        {
            Method hasPendingTasks = SingleWorkerThread.class.getDeclaredMethod("hasPendingTasks", null);
            hasPendingTasks.setAccessible(true);
            Boolean result = (Boolean)hasPendingTasks.invoke(workerThread, null);
            return result.booleanValue();
        }
        throw new IllegalArgumentException("Invalid WorkerThread " + workerThread);
    }
}
