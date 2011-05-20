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

package irc.security;

import irc.EventDispatcher;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The security provider. Provides an interface for accessing protected data or operation.
 */
public class SecurityProvider implements ActionListener
{
  private SecuredProvider _provider;
  //private Object _lock=new Object();
  private boolean _answer;

  /**
   * Create a new SecurityProvider.
   */
  public SecurityProvider()
  {
    //if(tryProvider("MS")) return;
	
    _provider=new DefaultSecuredProvider();
  }

  private boolean tryProvider(String name)
  {
    SecuredProvider old=_provider;
    try
    {
      Class cl=Class.forName("irc.security.prv.Specific"+name+"SecuredProvider");
      _provider=(SecuredProvider)cl.newInstance();
      if(!_provider.tryProvider()) throw new Exception();
      return true;
    }
    catch(Exception ex)
    {
      _provider=old;
      return false;
    }
  }

  /**
   * Get the used provider name.
   * @return used provider name.
   */
  public String getProviderName()
  {
    return _provider.getName();
  }

  /**
   * Get a new Socket.
   * @param host server host.
   * @param port server port.
   * @throws UnknownHostException is host is not found.
   * @throws IOException if an error occurs.
   * @return a new Socket.
   */
  public Socket getSocket(String host,int port) throws UnknownHostException,IOException
  {
    try
    {
      Socket ans=(Socket)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"getSocket",new Object[] {host,new Integer(port)});
      return ans;
    }
    catch(InterruptedException ex)
    {
      throw new IOException("Interrupted");
    }
    catch(Throwable ex)
    {
      throw new IOException(ex.getClass().getName()+" : "+ex.getMessage());
    }
  }

  /**
   * Get a new ServerSocket.
   * @param port local port.
   * @return the created server socket.
   * @throws IOException if an error occurs.
   */
  public ServerSocket getServerSocket(int port) throws IOException
  {
    try
    {
      return (ServerSocket)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"getServerSocket",new Object[] {new Integer(port)});
    }
    catch(InterruptedException ex)
    {
      throw new IOException("Interrupted");
    }
    catch(Throwable ex)
    {
      throw new IOException(ex.getClass().getName()+" : "+ex.getMessage());
    }
  }

  /**
   * Display a request for confirmation.
   * @param parent parent frame, or null if headless.
   * @param title the message title.
   * @param msg the message.
   * @return true if the user replied yes, false otherwise.
   */
  public boolean confirm(JFrame parent,String title,String msg)
  {
    JDialog f;
    
    JFrame tmp=null;
    
    if(parent==null)
    {
      tmp=new JFrame();
      f=new JDialog(tmp,title,true);
    }
    else
    {
      f=new JDialog(parent,title,true);
    }

    f.setLayout(new BorderLayout());
    f.add(new JLabel(msg),BorderLayout.CENTER);
    JButton b1=new JButton("Yes");
    JButton b2=new JButton("No");
    JPanel p=new JPanel();
    f.setResizable(false);
    f.add(p,BorderLayout.SOUTH);
    p.add(b1);
    p.add(b2);
    b1.addActionListener(this);
    b2.addActionListener(this);
    f.pack();
    _answer=false;
    //f.show();
    f.setVisible(true);
    /*synchronized(_lock)
    {
      try
      {
        _lock.wait();
      }
      catch(InterruptedException ex)
      {
      }
    }*/

    b1.removeActionListener(this);
    b2.removeActionListener(this);

    f.setVisible(false);
    f.dispose();
    f=null;
    
    if(tmp!=null)
    {
      tmp.setVisible(false);
      tmp.dispose();
      tmp=null;
    }
    
    return _answer;
  }

  public void actionPerformed(ActionEvent e)
  {
    JButton b=(JButton)e.getSource();
    _answer=false;
    if(b.getLabel().equals("Yes")) _answer=true;
    /*synchronized(_lock)
    {
      _lock.notifyAll();
    }*/
    ((Window)(b.getParent().getParent())).setVisible(false);
  }

  /**
   * Get an FileInputStream from a local file.
   * @param file the local file.
   * @throws IOException if an error occurs.
   * @return an FileInputStream from the file.
   */
  public FileInputStream getFileInputStream(File file) throws IOException
  {
    if(!confirm(null,"Security warning","Authorize file read action on "+file+"?")) throw new IOException("User denied access");

    try
    {
      return (FileInputStream)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"getFileInputStream",new Object[] {file});
    }
    catch(InterruptedException ex)
    {
      throw new IOException("Interrupted");
    }
    catch(Throwable ex)
    {
      throw new IOException(ex.getMessage());
    }
  }

  /**
   * Get an FileOutputStream to a local file.
   * @param file the local file.
   * @throws IOException if an error occurs.
   * @return an FileOutputStream from the file.
   */
  public FileOutputStream getFileOutputStream(File file) throws IOException
  {
    if(!confirm(null,"Security warning","Authorize file write action on "+file+"?")) throw new IOException("User denied access");

    try
    {
      return (FileOutputStream)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"getFileOutputStream",new Object[] {file});
    }
    catch(InterruptedException ex)
    {
      throw new IOException("Interrupted");
    }
    catch(Throwable ex)
    {
      throw new IOException(ex.getMessage());
    }
  }

  /**
   * Get the file size.
   * @param file the file to get size.
   * @return the file size, of a negative value if not able.
   */
  public int getFileSize(File file)
  {
    try
    {
      return ((Integer)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"getFileSize",new Object[] {file})).intValue();
    }
    catch(Throwable ex)
    {
      return -1;
    }
  }

  /**
   * Open a load file dialog with the given title and return the user choice.
   * @param title dialog title.
   * @return user choice.
   */
  public File getLoadFile(String title)
  {
    try
    {
      return (File)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"getLoadFile",new Object[] {title});
    }
    catch(Throwable ex)
    {
      return null;
    }
  }

  /**
   * Open a save file dialog with the given title and return the user choice.
   * @param title dialog title.
   * @return user choice.
   */
  public File getSaveFile(String title)
  {
    try
    {
      return (File)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"getSaveFile",new Object[] {title});
    }
    catch(Throwable ex)
    {
      return null;
    }
  }

  /**
   * Open a save file dialog with the given title and return the user choice.
   * @param file default target file.
   * @param title dialog title.
   * @return user choice.
   */
  public File getSaveFile(String file,String title)
  {
    try
    {
      return (File)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"getSaveFile",new Object[] {file,title});
    }
    catch(Throwable ex)
    {
      return null;
    }
  }
  
  /**
   * Get the local host address.
   * @return local host address.
   * @throws UnknownHostException if error occurs.
   */
  public InetAddress getLocalHost() throws UnknownHostException
  {
    try
    {
      return (InetAddress)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"getLocalHost",new Object[] {});
    }
    catch(InterruptedException ex)
    {
      throw new UnknownHostException("Unable to resolve");
    }
    catch(Throwable ex)
    {
      throw new UnknownHostException(ex.getMessage());
    }
  }

  /**
   * Perform a dns resolve of the given address.
   * @param addr address to resolve.
   * @return resolved address.
   */
  public String resolve(InetAddress addr)
  {
    try
    {
      return (String)EventDispatcher.dispatchEventAsyncAndWaitExSecurity(_provider,"resolve",new Object[] {addr});
    }
    catch(Throwable ex)
    {
      return null;
    }
  }


}

