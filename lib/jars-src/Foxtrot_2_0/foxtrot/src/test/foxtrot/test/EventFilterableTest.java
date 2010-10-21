/**
 * Copyright (c) 2002, Simone Bordet
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package foxtrot.test;

import java.awt.AWTEvent;

import foxtrot.Worker;
import foxtrot.Job;
import foxtrot.EventPump;
import foxtrot.pumps.JDK13QueueEventPump;
import foxtrot.pumps.EventFilterable;
import foxtrot.pumps.EventFilter;

/**
 *
 * @author <a href="mailto:biorn_steedom@users.sourceforge.net">Simone Bordet</a>
 * @version $Revision: 155 $
 */
public class EventFilterableTest extends FoxtrotTestCase
{
   public EventFilterableTest(String s)
   {
      super(s);
   }

   public void testEventFiltering() throws Exception
   {
       if (!isJRE14())
       {
          invokeTest(new Runnable()
          {
             public void run()
             {
                Worker.setEventPump(new JDK13QueueEventPump());

                EventPump pump = Worker.getEventPump();

                final MutableInteger count = new MutableInteger(0);
                EventFilterable filterable = null;
                EventFilter oldFilter = null;
                try
                {
                   if (pump instanceof EventFilterable)
                   {
                      filterable = (EventFilterable)pump;
                      oldFilter = filterable.getEventFilter();
                      filterable.setEventFilter(new EventFilter()
                      {
                         public boolean accept(AWTEvent event)
                         {
                            count.set(count.get() + 1);
                            return true;
                         }
                      });
                   }

                   Worker.post(new Job()
                   {
                      public Object run()
                      {
                         sleep(5000);
                         return null;
                      }
                   });
                }
                finally
                {
                   filterable.setEventFilter(oldFilter);
                }

                // Ensure that at least we have one event, the one posted at the end of the Task to wakeup the queue
                if (count.get() != 1) fail();

                // Be sure that after everything is again ok
                Worker.post(new Job()
                {
                   public Object run()
                   {
                      sleep(5000);
                      return null;
                   }
                });

                // Should not have been called again
                if (count.get() != 1) fail();
             }
          });
       }
   }
}
