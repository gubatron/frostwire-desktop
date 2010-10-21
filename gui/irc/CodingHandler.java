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

import java.io.*;

/**
 * MyPushbackStream, because of a bug in JDK1.1.
 */
class MyPushbackStream extends InputStream
{
  private InputStream _is;
  private int _back;
  private boolean _closed;

  /**
   * Create a new MyPushbackStream
   * @param is source input stream.
   */
  public MyPushbackStream(InputStream is)
  {
    _is=is;
    _back=-1;
    _closed=false;
  }

  public void close() throws IOException
  {
    _is.close();
    _back=-1;
    _closed=true;
  }

  public int read() throws IOException
  {
    if(_back!=-1)
    {
      int res=_back;
      _back=-1;
      return res;
    }
    return _is.read();
  }

  public int read(byte[] b) throws IOException
  {
    return read(b,0,b.length);
  }

  public int read(byte[] b,int offset,int length) throws IOException
  {
    if(length==0) return 0;
    if(_back!=-1)
    {
      b[offset]=(byte)_back;
      _back=-1;
      return 1;
    }
    return _is.read(b,offset,length);
  }

  public int available() throws IOException
  {
    if(_back!=-1) return 1+_is.available();
    return _is.available();
  }

  /**
   * Unread the last read byte.
   * @param b last read byte value.
   */
  public void unread(byte b)
  {
    if(_closed) return;
    _back=b;
  }
}

/**
 * Coding handler for unicode to ascii transfert via socket streams.
 */
public class CodingHandler extends IRCObject
{
  private MyPushbackStream _is;
  private OutputStream _os;
  private BufferedReader _reader;
  private BufferedWriter _writer;
  private int _coding;

  /**
   * ASCII coding.
   */
  public static final int CODING_ASCII=0;
  /**
   * PJIRC proprietary coding format.
   */
  public static final int CODING_PUAP=1;
  /**
   * UTF-8 coding.
   */
  public static final int CODING_UTF_8=2;
  /**
   * Local charset coding.
   */
  public static final int CODING_LOCAL_CHARSET=3;

  /**
   * Create a new Coding handler, using the given input and output stream.
   * @param config IRCConfiguration objet.
   * @param is inputstream for reading.
   * @param os outputstream for writing.
   */
  public CodingHandler(IRCConfiguration config,InputStream is,OutputStream os)
  {
    super(config);
    _coding=config.getI("coding");
    if(_coding!=CODING_LOCAL_CHARSET)
    {
      _is=new MyPushbackStream(is);
      _os=os;
      _reader=null;
      _writer=null;
    }
    else
    {
      _is=null;
      _os=null;
      _reader=new BufferedReader(new InputStreamReader(is));
      _writer=new BufferedWriter(new OutputStreamWriter(os));
    }
  }

  /**
   * Close the handler, and associated streams.
   * @throws IOException
   */
  public void close() throws IOException
  {
    if(_is!=null) _is.close();
    if(_os!=null) _os.close();
    if(_reader!=null) _reader.close();
    if(_writer!=null) _writer.close();
    _is=null;
    _os=null;
    _reader=null;
    _writer=null;
  }

  /**
   * Read a single line from the input stream.
   * @return the read line.
   * @throws IOException
   */
  public String read() throws IOException
  {
    if(_coding!=CODING_LOCAL_CHARSET)
    {
      String ans=readUTF();
      return asciiToWide(ans);
    }
    return _reader.readLine();
  }

  /**
   * Write a single line to the output stream.
   * @param s the line to write.
   * @throws IOException
   */
  public void write(String s) throws IOException
  {
    if(_coding==CODING_ASCII)
      writeASCII(s);
    else if(_coding==CODING_PUAP)
      writePUAP(s);
    else if(_coding==CODING_UTF_8)
      writeUTF(s);
    else if(_coding==CODING_LOCAL_CHARSET)
      writeCHARSET(s);
    else
      writePUAP(s);

    if(_os!=null) _os.flush();
    if(_writer!=null) _writer.flush();
  }

  private void writeCHARSET(String s) throws IOException
  {
    _writer.write(s,0,s.length());
    _writer.newLine();
  }

  private void writeASCII(String s) throws IOException
  {
    for(int i=0;i<s.length();i++) _os.write((byte)s.charAt(i));
    _os.write(13);
    _os.write(10);
  }

  private void writePUAP(String s) throws IOException
  {
    writeASCII(wideToAscii(s));
  }

  private String readUTF() throws IOException
  {
    String ans="";
    String nonUTFans="";
    boolean utf=true;
    int c;
    char ch=0;
    int expect=0;
    boolean terminated=false;
    while(!terminated)
    {
      c=_is.read();
      if(((c==10) || (c==13)) && (nonUTFans.length()==0)) continue;
      if(c==-1)
      {
        if(nonUTFans.length()==0) throw new IOException("EOF reached");
        if(expect!=0) utf=false;
        if(utf) return ans;
        return nonUTFans;
      }

      if((c==10) || (c==13))
      {
        if(expect!=0) utf=false;
        if(_is.available()>=1) c=_is.read();
        if((c!=10) && (c!=13)) _is.unread((byte)c);
        if(utf) return ans;
        return nonUTFans;
       }

      nonUTFans+=(char)c;

      if(!utf) continue;

      if(c<128)
      {
        if(expect!=0)
        {
          utf=false;
        }
        else
        {
          ans+=(char)c;
        }
      }
      else if(c<192)
      {
        if(expect==0)
        {
          utf=false;
        }
        else
        {
          ch=(char)((ch<<6)|((c-128)&63));
          expect--;
          if(expect==0)
          {
            ans+=ch;
            ch=0;
          }
        }
      }
      else if(c<224)
      {
        if(expect!=0) utf=false;
        else
        {
          expect=1;
          ch=(char)(c-192);
        }
      }
      else
      {
        if(expect!=0) utf=false;
        else
        {
          expect=2;
          ch=(char)(c-224);
        }
      }
    }
    return null;
  }

  private void writeUTF(String str) throws IOException
  {
    for(int i=0;i<str.length();i++)
    {
      char ch=str.charAt(i);
      if(ch<0x007F) _os.write((byte)ch);
      else if(ch<0x07FF)
      {
        _os.write((byte)(192+(ch>>6)));
        _os.write((byte)(128+(ch&63)));
      }
      else
      {
        _os.write((byte)(224+(ch>>12)));
        _os.write((byte)(128+((ch>>6)&63)));
        _os.write((byte)(128+(ch&63)));
      }
    }
    _os.write(10);
    _os.write(13);
  }

  /**
   * Convert an ascii string to a wide string, as defined in the PJIRC unicode transfert
   * protocol.
   * @param str ascii string.
   * @return wide string.
   */
  private static String asciiToWide(String str)
  {
    try
    {
      String res="";
      for(int i=0;i<str.length();i++)
      {
        if(str.charAt(i)==(char)30)
        {
          String hex=str.substring(i+1,i+5);
          i+=4;
          int code=Integer.parseInt(hex,16);
          res+=(char)code;
        }
        else
        {
          res+=str.charAt(i);
        }
      }
      return res;
    }
    catch(Exception ex)
    {
      return str;
    }
  }

  /**
   * Convert a wide string to an ascii string, as defined in the PJIRC unicode transfert
   * protocol.
   * @param str wide string.
   * @return ascii string.
   */
  private static String wideToAscii(String str)
  {
    String res="";
    for(int i=0;i<str.length();i++)
    {
      int c=str.charAt(i);
      if(c>255)
      {
        res+=(char)30;
        String v=Integer.toHexString(c);
        while(v.length()<4) v="0"+v;
        res+=v;
      }
      else
      {
        res+=(char)c;
      }
    }
    return res;
  }
}
