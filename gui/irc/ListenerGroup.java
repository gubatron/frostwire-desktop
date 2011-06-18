/*****************************************************/
/*          This java file is a part of the          */
/*                                                   */
/*           -  Plouf's Java IRC Client  -           */
/*                                                   */
/*   Copyright (C)  2002 - 2004 Philippe Detournay   */
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

import java.util.*;

/**
 * Handles a group of listeners.
 */
public class ListenerGroup
{
  private Vector<Object> _listeners;

  /**
   * Create a new ListenerGroup.
   */
  public ListenerGroup()
  {
    _listeners=new Vector<Object>();
  }

  /**
   * Add a listener to the group.
   * @param listener listener to add.
   */
  public synchronized void addListener(Object listener)
  {
    _listeners.insertElementAt(listener,_listeners.size());
  }

  /**
   * Remove a listener from the group.
   * @param listener listener to remove.
   */
  public synchronized void removeListener(Object listener)
  {
    for(int i=0;i<_listeners.size();i++)
    {
      if(_listeners.elementAt(i)==listener)
      {
        _listeners.removeElementAt(i);
        return;
      }
    }
  }

  /**
   * Send an event to all listeners in the group, in the event thread.
   * @param method method to call on each listener.
   * @param params parameters to pass to the called method.
   */
  public synchronized void sendEventAsync(String method,Object[] params)
  {
    Enumeration<Object> e=_listeners.elements();
    while(e.hasMoreElements())
      EventDispatcher.dispatchEventAsync(e.nextElement(),method,params);
  }

  /**
   * Send an event to all listeners in the group, in the event thread.
   * @param method method to call on each listener.
   */
  public synchronized void sendEventAsync(String method)
  {
    sendEventAsync(method,new Object[0]);
  }

  /**
   * Send an event to all listeners in the group, in the event thread.
   * @param method method to call on each listener.
   * @param param1 first parameter to pass to the called method.
   */
  public synchronized void sendEventAsync(String method,Object param1)
  {
    Object[] p={param1};
    sendEventAsync(method,p);
  }

  /**
   * Send an event to all listeners in the group, in the event thread.
   * @param method method to call on each listener.
   * @param param1 first parameter to pass to the called method.
   * @param param2 second parameter to pass to the called method.
   */
  public synchronized void sendEventAsync(String method,Object param1,Object param2)
  {
    Object[] p={param1,param2};
    sendEventAsync(method,p);
  }

  /**
   * Send an event to all listeners in the group, in the event thread.
   * @param method method to call on each listener.
   * @param param1 first parameter to pass to the called method.
   * @param param2 second parameter to pass to the called method.
   * @param param3 third parameter to pass to the called method.
   */
  public synchronized void sendEventAsync(String method,Object param1,Object param2,Object param3)
  {
    Object[] p={param1,param2,param3};
    sendEventAsync(method,p);
  }

  /**
   * Send an event to all listeners in the group. The event will be processed
   * synchrounsly in the current thread.
   * @param method method to call on each listener.
   * @param params parameters to pass to the called method.
   * @return array of returned resuluts for each method call.
   * deprecated use sendEventEx instead.
   */
  public synchronized Object[] sendEvent(String method,Object[] params)
  {
    try
    {
      return sendEventEx(method,params);
    }
    catch(Throwable ex)
    {
      //Lex.printStackTrace();
      return null;
    }
  }

  /**
   * Send an event to all listeners in the group. The event will be processed
   * synchrounsly in the current thread.
   * @param method method to call on each listener.
   * @param params parameters to pass to the called method.
   * @return array of returned resuluts for each method call.
   * @throws Throwable
   */
  public synchronized Object[] sendEventEx(String method,Object[] params) throws Throwable
  {
    Object[] res=new Object[_listeners.size()];
    int i=0;
    Enumeration<Object> e=_listeners.elements();
    while(e.hasMoreElements())
    {
      try
      {
        res[i++]=EventDispatcher.dispatchEventSyncEx(e.nextElement(),method,params);
      }
      catch (Throwable ex)
      {
        //ex.printStackTrace();
      }
    }
    return res;
  }

  /**
   * Send an event to all listeners in the group in the current thread.
   * @param method method to call on each listener.
   * @return array of returned resuluts for each method call.
   */
  public synchronized Object[] sendEvent(String method)
  {
    return sendEvent(method,new Object[0]);
  }

  /**
   * Send an event to all listeners in the group in the current thread.
   * @param method method to call on each listener.
   * @param param1 first parameter to pass to the called method.
   * @return array of returned resuluts for each method call.
   */
  public synchronized Object[] sendEvent(String method,Object param1)
  {
    Object[] p={param1};
    return sendEvent(method,p);
  }

  /**
   * Send an event to all listeners in the group in the current thread.
   * @param method method to call on each listener.
   * @param param1 first parameter to pass to the called method.
   * @param param2 second parameter to pass to the called method.
   * @return array of returned resuluts for each method call.
   */
  public synchronized Object[] sendEvent(String method,Object param1,Object param2)
  {
    Object[] p={param1,param2};
    return sendEvent(method,p);
  }

  /**
   * Send an event to all listeners in the group in the current thread.
   * @param method method to call on each listener.
   * @param param1 first parameter to pass to the called method.
   * @param param2 second parameter to pass to the called method.
   * @param param3 third parameter to pass to the called method.
   * @return array of returned resuluts for each method call.
   */
  public synchronized Object[] sendEvent(String method,Object param1,Object param2,Object param3)
  {
    Object[] p={param1,param2,param3};
    return sendEvent(method,p);
  }

  /**
   * Send an event to all listeners in the group in the current thread.
   * @param method method to call on each listener.
   * @return array of returned resuluts for each method call.
   * @throws Throwable
   */
  public synchronized Object[] sendEventEx(String method) throws Throwable
  {
    return sendEventEx(method,new Object[0]);
  }

  /**
   * Send an event to all listeners in the group in the current thread.
   * @param method method to call on each listener.
   * @param param1 first parameter to pass to the called method.
   * @return array of returned resuluts for each method call.
   * @throws Throwable
   */
  public synchronized Object[] sendEventEx(String method,Object param1) throws Throwable
  {
    Object[] p={param1};
    return sendEventEx(method,p);
  }

  /**
   * Send an event to all listeners in the group in the current thread.
   * @param method method to call on each listener.
   * @param param1 first parameter to pass to the called method.
   * @param param2 second parameter to pass to the called method.
   * @return array of returned resuluts for each method call.
   * @throws Throwable
   */
  public synchronized Object[] sendEventEx(String method,Object param1,Object param2) throws Throwable
  {
    Object[] p={param1,param2};
    return sendEventEx(method,p);
  }

  /**
   * Send an event to all listeners in the group in the current thread.
   * @param method method to call on each listener.
   * @param param1 first parameter to pass to the called method.
   * @param param2 second parameter to pass to the called method.
   * @param param3 third parameter to pass to the called method.
   * @return array of returned resuluts for each method call.
   * @throws Throwable
   */
  public synchronized Object[] sendEventEx(String method,Object param1,Object param2,Object param3) throws Throwable
  {
    Object[] p={param1,param2,param3};
    return sendEventEx(method,p);
  }

}
