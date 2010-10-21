/**
 * Copyright (c) 2002-2008, Simone Bordet
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package foxtrot.workers;

import foxtrot.FoxtrotTestCase;
import foxtrot.Job;
import foxtrot.MutableInteger;
import foxtrot.MutableReference;

/**
 * @version $Revision: 263 $
 */
public class MultiWorkerThreadTest extends FoxtrotTestCase
{
    public void testThreads() throws Exception
    {
        final MutableReference thread = new MutableReference(null);
        final MultiWorkerThread worker = new MultiWorkerThread()
        {
            public void run()
            {
                thread.set(Thread.currentThread());
                super.run();
            }
        };
        invokeTest(worker, new Runnable()
        {
            public void run()
            {
                worker.start();

                final MutableReference runner = new MutableReference(null);
                worker.postTask(new Job()
                {
                    public Object run()
                    {
                        runner.set(Thread.currentThread());
                        return null;
                    }
                });

                sleep(1000);

                if (thread.get() == runner.get()) fail();
            }
        }, null);
    }

    public void testLongBeforeShort() throws Exception
    {
        final MultiWorkerThread worker = new MultiWorkerThread();
        invokeTest(worker, new Runnable()
        {
            public void run()
            {
                worker.start();

                // A long Task followed by a short one.
                final long longDelay = 5000;
                final MutableInteger longer = new MutableInteger(0);
                worker.postTask(new Job()
                {
                    public Object run()
                    {
                        longer.set(1);
                        sleep(longDelay);
                        longer.set(2);
                        return null;
                    }
                });
                final long shortDelay = 2000;
                final MutableInteger shorter = new MutableInteger(0);
                worker.postTask(new Job()
                {
                    public Object run()
                    {
                        shorter.set(1);
                        sleep(shortDelay);
                        shorter.set(2);
                        return null;
                    }
                });

                sleep(shortDelay / 2);
                if (shorter.get() != 1) fail();
                if (longer.get() != 1) fail();

                sleep(shortDelay);
                if (shorter.get() != 2) fail();
                if (longer.get() != 1) fail();

                sleep(longDelay);
                if (longer.get() != 2) fail();
            }
        }, null);
    }
}
