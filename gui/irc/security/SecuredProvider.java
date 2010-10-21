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

package irc.security;


import java.io.*;
import java.net.*;

/**
 * Provide methods for accessing protected operations.
 */
public interface SecuredProvider
{
  /**
   * Get a new Socket.
   * @param host server host.
   * @param port server port.
   * @throws UnknownHostException is host is not found.
   * @throws IOException if an error occurs.
   * @return a new Socket.
   */
  public Socket getSocket(String host,Integer port) throws UnknownHostException,IOException;

  /**
   * Get a new ServerSocket.
   * @param port local port.
   * @return the created server socket.
   * @throws IOException if an error occurs.
   */
  public ServerSocket getServerSocket(Integer port) throws IOException;

  /**
   * Get an FileInputStream from a local file.
   * @param file the local file.
   * @throws IOException if an error occurs.
   * @return an FileInputStream from the file.
   */
  public FileInputStream getFileInputStream(File file) throws IOException;

  /**
   * Get an FileOutputStream to a local file.
   * @param file the local file.
   * @throws IOException if an error occurs.
   * @return an FileOutputStream from the file.
   */
  public FileOutputStream getFileOutputStream(File file) throws IOException;

  /**
   * Get the file size.
   * @param file the file to get size.
   * @return the file size, of a negative value if not able.
   */
  public Integer getFileSize(File file);

  /**
   * Open a load file dialog with the given title and return the user choice.
   * @param title dialog title.
   * @return user choice.
   */
  public File getLoadFile(String title);

  /**
   * Open a save file dialog with the given title and return the user choice.
   * @param title dialog title.
   * @return user choice.
   */
  public File getSaveFile(String title);

  /**
   * Open a save file dialog with the given title and return the user choice.
   * @param file default file.
   * @param title dialog title.
   * @return user choice.
   */
  public File getSaveFile(String file,String title);
  
  /**
   * Get the local host address.
   * @return local host address.
   * @throws UnknownHostException if error occurs.
   */
  public InetAddress getLocalHost() throws UnknownHostException;

  /**
   * Perform a dns resolve of the given address.
   * @param addr address to resolve.
   * @return resolved address.
   */
  public String resolve(InetAddress addr);

  /**
   * Try to use this provider. Return false if this provider is not able to perform its
   * task.
   * @return true if this provider can be used, false otherwise.
   */
  public boolean tryProvider();

  /**
   * Get this provider's name.
   * @return the provder name.
   */
  public String getName();
}

