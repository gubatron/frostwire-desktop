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

package irc.dcc;

import irc.dcc.prv.*;
import java.io.*;
import irc.*;

/**
 * The DCCFile, used for file transferts.
 */
public class DCCFile extends Source
{
  private OutputStream _os;
  private InputStream _is;
  private File _file;
  private ListenerGroup _listeners;
  private boolean _down=false;
  private int _size;
  private int _count;
  private DCCFileHandler _handler;

  /**
   * Create a new DCCFile.
   * @param config the global configuration.
   * @param f the file to transfert.
   * @param handler the file handler.
   */
  public DCCFile(IRCConfiguration config,File f,DCCFileHandler handler)
  {
    super(config,handler);
    _listeners=new ListenerGroup();
    _handler=handler;
    _count=0;
    _file=f;
  }

  /**
   * Add a listener.
   * @param lis listener to add.
   */
  public void addDCCFileListener(DCCFileListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove a listener.
   * @param lis listener to remove.
   */
  public void removeDCCFileListener(DCCFileListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Prepare to send the file.
   */
  public void prepareSend()
  {
    try
    {
      _size=_ircConfiguration.getSecurityProvider().getFileSize(_file);
   //   _size=(int)_file.length();
      _is=new BufferedInputStream(_ircConfiguration.getSecurityProvider().getFileInputStream(_file));
      _down=false;
    }
    catch(Exception e)
    {
      _ircConfiguration.internalError("prepareSend failure",e,"bugs@frostwire.com");
    }
  }

  /**
   * Read the next bytes while sending.
   * @param buffer the target buffer, where the bytes will be written.
   * @param offset the position of the first written byte in the target buffer.
   * @param length the maximum amount of bytes to read.
   * @return the number of read bytes, or -1 if there is no more bytes to read.
   * @throws IOException in case of error.
   */
  public int readBytes(byte[] buffer,int offset,int length) throws IOException
  {
    int actual=_is.read(buffer,offset,length);
    if(actual>=0)
    {
      _count+=actual; 
      _listeners.sendEventAsync("transmitted",new Integer(_count),this);
    }
    return actual;
  }

  /**
   * Get the file size.
   * @return the file size, in byte.
   */
  public int getSize()
  {
    return _size;
  }

  /**
   * Return true if the transfert is an upload transfert.
   * @return true if uplading, false otherwise.
   */
  public boolean isUploading()
  {
    return !isDownloading();
  }

  /**
   * Return true if the transfert is a download transfert.
   * @return true if downloading, false otherwise.
   */
  public boolean isDownloading()
  {
    return _down;
  }

  /**
   * Notify this file the sending is terminated.
   */
  public void fileSent()
  {
    try
    {
      _listeners.sendEventAsync("finished",this);
      _is.close();
    }
    catch(Exception e)
    {
      _ircConfiguration.internalError("fileSent failure",e,"bugs@frostwire.com");
    }
  }

  /**
   * Notify this file the sending has failed.
   */
  public void fileSentFailed()
  {
    try
    {
      _listeners.sendEventAsync("failed",this);
      _is.close();
    }
    catch(Exception e)
    {
      _ircConfiguration.internalError("fileSentFailed failure",e,"bugs@frostwire.com");
    }
  }

  /**
   * Prepare to receive file.
   * @param size the file size.
   */
  public void prepareReceive(int size)
  {
    _down=true;
    _size=size;
    try
    {
      _os=new BufferedOutputStream(_ircConfiguration.getSecurityProvider().getFileOutputStream(_file));
    }
    catch(Exception e)
    {
      _os=null;
    }
  }

  /**
   * Write new bytes in the destination file.
   * @param buffer the buffer to write.
   * @param offset the first byte of the buffer to write.
   * @param length the number of bytes to write.
   * @throws IOException in case of error.
   */
  public void bytesReceived(byte[] buffer,int offset,int length) throws IOException
  {
    _count+=length;
    _os.write(buffer,offset,length);
    _listeners.sendEventAsync("transmitted",new Integer(_count),this);
  }

  /**
   * Notify this dcc file the file reception is terminated.
   */
  public void fileReceived()
  {
    try
    {
      _listeners.sendEventAsync("finished",this);
      _os.close();
    }
    catch(Exception e)
    {
      _ircConfiguration.internalError("fileReceived failure",e,"bugs@frostwire.com");
    }

  }

  /**
   * Notify this dcc file the file reception has failed.
   */
  public void fileReceiveFailed()
  {
    try
    {
      _listeners.sendEventAsync("failed",this);
      _os.close();
    }
    catch(Exception e)
    {
      _ircConfiguration.internalError("fileReceiveFailed failure",e,"bugs@frostwire.com");
    }
  }

  public String getName()
  {
    return _file.getName();
  }

  public void leave()
  {
    _handler.close();
    _handler.leave();
  }

  public boolean talkable()
  {
    return false;
  }

  public String getType()
  {
    return "DCCFile";
  }
}
