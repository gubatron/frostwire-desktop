/*****************************************************/
/*          This java file is a part of the          */
/*                                                   */
/*           -  Plouf's Java IRC Client  -           */
/*                                                   */
/*   Copyright (C)  2002 - 2005 Philippe Detournay   */
/*                                                   */
/*         All contacts : theplouf@yahoo.com         */
/*                                                   */
/*  PJIRC is free software; you can redistribute     */
/*  it and/or modify it under the terms of the GNU   */
/*  General Public License as published by the       */
/*  Free Software Foundation; version 2 or later of  */
/*  the License.                                     */
/*                                                   */
/*  PJIRC is distributed in the hope that it will    */
/*  be useful, but WITHOUT ANY WARRANTY; without     */
/*  even the implied warranty of MERCHANTABILITY or  */
/*  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU   */
/*  General Public License for more details.         */
/*                                                   */
/*  You should have received a copy of the GNU       */
/*  General Public License along with PJIRC; if      */
/*  not, write to the Free Software Foundation,      */
/*  Inc., 59 Temple Place, Suite 330, Boston,        */
/*  MA  02111-1307  USA                              */
/*                                                   */
/*****************************************************/

package irc;

import java.lang.reflect.*;
import java.util.*;

/**
 * EventItem.
 */
class EventItem
{
  /**
   * Target object.
   */
  public Object target;
  /**
   * Method name to call on target.
   */
  public String method;
  /**
   * Method parameters.
   */
  public Object[] params;
  /**
   * Lock that will be signaled upon completion.
   */
  public Object endLock;
  /**
   * Method call result, if resultException is null.
   */
  public Object result;
  /**
   * Method call exception, or null if all went well.
   */
  public Throwable resultException;
  /**
   * True if call has completed.
   */
  public boolean resultAvailable;

  /**
   * Create a new EventItem
   * @param atarget
   * @param amethod
   * @param aparams
   */
  public EventItem(Object atarget,String amethod,Object[] aparams)
  {
    this.target=atarget;
    this.method=amethod;
    this.params=aparams;
    endLock=new Object();
    resultAvailable=false;
    result=null;
  }

}

/**
 * DispatchThread.
 */
class DispatchThread extends Thread
{
  private irc.LinkedList _list;

  private Object _manageLock;
  private boolean _terminated;
  private boolean _processing;

  /**
   * Create a new DispatchThread
   * @param type thread type : will be added in the name.
   */
  public DispatchThread(String type)
  {
    super(type+" event dispatch thread");
    _manageLock=new Object();
    _list=new LinkedList();
    _terminated=false;
    _processing=false;
    setDaemon(true);
    start();
  }

  /**
   * Add an event in the dispatch thread queue.
   * @param target target object.
   * @param method method name to call.
   * @param params call parameters.
   * @return the newly created event item.
   */
  public EventItem addEvent(Object target,String method,Object[] params)
  {
    if(_terminated) return null;
    EventItem item=new EventItem(target,method,params);
    synchronized(_manageLock)
    {
      _list.addLast(item);
      _manageLock.notify();
    }
    return item;
  }

  public void run()
  {
    int size=0;
    do
    {
      EventItem item;
      synchronized(_manageLock)
      {
        if(_list.size()>0)
          item=(EventItem)_list.removeFirst();
        else
        {
          item=null;
          try
          {
            _manageLock.wait();
          }
          catch(InterruptedException ex)
          {
            //ignore...
          }
        }
        size=_list.size();
      }
      if(item!=null)
      {
        _processing=true;
        item.resultException=null;
        try
        {
          item.result=EventDispatcher.dispatchEventSyncEx(item.target,item.method,item.params);
        }
        catch (Throwable e)
        {
          item.resultException=e;
        }
        _processing=false;
        synchronized(item.endLock)
        {
          item.resultAvailable=true;
          item.endLock.notify();
        }
      }
    }
    while(!(_terminated && (size==0)));
  }

  /**
   * Terminate the event thread processing.
   */
  public void terminate()
  {
    _terminated=true;
    if(!_processing) interrupt();
  }
}

/**
 * Event dispatcher, using reflection and PJIRC threading model.
 * The PJIRC threading model states that, unless specified otherwise, any
 * call to any method of any object should be performed in the event
 * dispatcher thread. This can be ensured via isEventThread. Any call
 * to dispatchEventSync in an other thread will lead to an error message
 * displayed on the error output.
 */
public class EventDispatcher
{
  private static final int USER=0;
  private static final int SECURITY=1;
  
  private static final String[] _names={"User","Security"};
  
  private static Hashtable _cache=new Hashtable();
  private static DispatchThread[] _thread=new DispatchThread[2];
  private static boolean _warning=true;

  private static void ensureAlive(int index)
  {
    if((_thread[index]==null) || (!_thread[index].isAlive()))
    {
      _thread[index]=new DispatchThread(_names[index]);
    }
  }
  
  private static boolean match(Class[] t1,Class[] t2)
  {
    if(t1.length!=t2.length) return false;
    for(int i=0;i<t1.length;i++)
    {
      if(t2[i]!=null)
        if(!t1[i].isAssignableFrom(t2[i])) return false;
    }
    return true;
  }

  /**
   * Clear the internal EventDispatcher method cache.
   */
  public static void clearCache()
  {
    synchronized(_cache)
    {
      _cache.clear();
    }
  }

  /**
   * Disabe the synchroneous call thread check.
   */
  public static void disableBadThreadWarning()
  {
    _warning=false;
  }
  
  /**
   * Enable the synchroneous call thread check.
   */
  public static void enableBadThreadWarning()
  {
    _warning=true;
  }
  
  /**
   * Dispatch the given event in the current thread, ignoring any thrown exception.
   * @param target event target.
   * @param method event method name.
   * @param params event parameters.
   * @return the method result.
   * @deprecated Use dispatchEventSyncEx instead.
   */
  public static Object dispatchEventSync(Object target,String method,Object[] params)
  {
    try
    {
      return dispatchEventSyncEx(target,method,params);
    }
    catch(Throwable ex)
    {
      //ex.printStackTrace();
      return null;
    }
  }

  /**
   * Dispatch the given event in the current thread, not ignoring any thrown exception.
   * @param target event target.
   * @param method event method name.
   * @param params event parameters.
   * @return the method result.
   * @throws Throwable
   */
  public static Object dispatchEventSyncEx(Object target,String method,Object[] params) throws Throwable
  {
    ensureAlive(USER);

    if (method.startsWith("AWTSource")) disableBadThreadWarning();
    if(_warning && !isEventThread())
    {
      System.err.println("Event dispatch in wrong thread for IRC method: "+ method);
      System.err.println("expected thread was "+_thread);
      System.err.println("current thread is "+Thread.currentThread());
      System.err.println("please submit a bug report to bugs@frostwire.com with the following information :");
      Thread.dumpStack();
    }
    
    try
    {
      Class c=target.getClass();
      
      Method m[];
      synchronized(_cache)
      {
        m=(Method[])_cache.get(c);
        if(m==null)
        {
          m=c.getMethods();
          _cache.put(c,m);
        }
      }

      Class types[]=new Class[params.length];
      for(int i=0;i<params.length;i++)
      {
        if(params[i]!=null)
          types[i]=params[i].getClass();
        else
          types[i]=null;
      }
      for(int i=0;i<m.length;i++)
      {
        if(m[i].getName().equals(method))
        {
          if(match(m[i].getParameterTypes(),types))
          {
            return m[i].invoke(target,params);
          }
        }
      }
      throw new NoSuchMethodException(method);
    }
    catch(InvocationTargetException ex)
    {
      throw ex.getTargetException();
    }
    catch(Throwable ex)
    {
      System.err.println("internal error");
      System.err.println("please submit a bug report to bugs@frostwire.com with the following information :");
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Dispatch a new event to the given target in the event thread. The method result
   * is discarded.
   * @param target target event listener.
   * @param method method name to call.
   * @param params parameters to pass to the called method.
   */
  public static void dispatchEventAsync(Object target,String method,Object[] params)
  {
    ensureAlive(USER);
    _thread[USER].addEvent(target,method,params);
  }

  private static void checkStack()
  {
    //we want to avoid code other that irc.security.* calls this method
    //unable to implement on 1.1...
  }
  
  private static void checkDeadLock(int index)
  {
    ensureAlive(index);
    if(Thread.currentThread()==_thread[index])
    {
      try
      {
        throw new RuntimeException("Deadlock protection");
      }
      catch(RuntimeException ex)
      {
        ex.printStackTrace();
        throw ex;
      }
    }
  }
  
  /**
   * Dispatch a new event to the given target in the security event thread. The method result
   * is discarded.
   * @param target target event listener.
   * @param method method name to call.
   * @param params parameters to pass to the called method.
   */
  public static void dispatchEventAsyncSecurity(Object target,String method,Object[] params)
  {
    checkStack();
    ensureAlive(SECURITY);
    _thread[SECURITY].addEvent(target,method,params);
  }
  
  private static Object dispatchEventAsyncAndWaitExImp(Object target,String method,Object[] params,int index) throws InterruptedException,Throwable
  {
    checkDeadLock(index);
    ensureAlive(index);
    EventItem item=_thread[index].addEvent(target,method,params);
    synchronized(item.endLock)
    {
      if(item.resultAvailable)
      {
        if(item.resultException!=null) throw item.resultException;
        return item.result;
      }
      item.endLock.wait();
      if(item.resultException!=null) throw item.resultException;
      return item.result;
    }
  }

  /**
   * Dispatch a new event in the event thread, waiting for the result and returning
   * it.
   * @param target target event listener.
   * @param method method name to call.
   * @param params parameters to pass to the called method.
   * @return method result.
   * @throws InterruptedException if the wait is interrupted.
   * @throws Throwable
   */
  public static Object dispatchEventAsyncAndWaitEx(Object target,String method,Object[] params) throws InterruptedException,Throwable
  {
    return dispatchEventAsyncAndWaitExImp(target,method,params,USER);
  }
  
  /**
   * Dispatch a new event in the security event thread, waiting for the result and returning
   * it.
   * @param target target event listener.
   * @param method method name to call.
   * @param params parameters to pass to the called method.
   * @return method result.
   * @throws InterruptedException if the wait is interrupted.
   * @throws Throwable
   */
  public static Object dispatchEventAsyncAndWaitExSecurity(Object target,String method,Object[] params) throws InterruptedException,Throwable
  {
    checkStack();
    return dispatchEventAsyncAndWaitExImp(target,method,params,SECURITY);
  }
  
  /**
   * Dispatch a new event in the event thread, waiting for the result and returning
   * it.
   * @param target target event listener.
   * @param method method name to call.
   * @param params parameters to pass to the called method.
   * @return method result.
   * @throws InterruptedException if the wait is interrupted.
   * @deprecated Use dispatchEventAsyncAndWaitEx instead.
   */
  public static Object dispatchEventAsyncAndWait(Object target,String method,Object[] params) throws InterruptedException
  {
    checkDeadLock(USER);
    ensureAlive(USER);
    EventItem item=_thread[USER].addEvent(target,method,params);
    synchronized(item.endLock)
    {
      if(item.resultAvailable)
      {
        return item.result;
      }
      item.endLock.wait();
      return item.result;
    }
  }

  /**
   * Check if the calling thread is the event thread.
   * @return true if calling thread is event thread, false otherwise.
   */
  public static boolean isEventThread()
  {
    ensureAlive(USER);
    return Thread.currentThread()==_thread[USER] || Thread.currentThread()==_thread[SECURITY];
  }
}
