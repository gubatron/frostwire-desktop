package irc;

import java.util.*;
import java.io.*;

/**
 * Provides text from langage file.
 */
public class FileTextProvider implements TextProvider
{
  private Hashtable<Integer, String> _mainlist;
  private Hashtable<Integer, String> _backlist;

	/**
	 * Create a new file text provider.
	 * @param fname langage file name.
	 * @param encoding langage file encoding. Empty string for default encoding.
   * @param backname backup langage file name.
   * @param backencoding backup langage file encoding. Empty string for default encoding.
   * @param file a file handler to load file.
	 */
  public FileTextProvider(String fname,String encoding,String backname,String backencoding,FileHandler file)
	{
	  _mainlist=new Hashtable<Integer, String>();
    _backlist=new Hashtable<Integer, String>();
    load(_mainlist,fname,encoding,file);
    load(_backlist,backname,backencoding,file);
  }

	private void parse(Hashtable<Integer, String> dest,String line)
	{
	  try
		{
	    int pos=line.indexOf(' ');
		  if(pos==-1) return;
		  String id=line.substring(0,pos);
		  String end=line.substring(pos+1).trim();
		  if(line.indexOf('[')!=-1)
		  {
		    pos=line.indexOf(']');
			  if(pos==-1) return;
			  end=line.substring(pos+1).trim();
		  }
		  int iid=Integer.parseInt(id,16);
			end=replace(end,"\\s"," ");
		  dest.put(new Integer(iid),end);
		}
		catch(Exception ex)
		{
      throw new RuntimeException(ex.toString());
		}
	}

	private void load(Hashtable<Integer, String> dest,String fname,String encoding,FileHandler handler)
	{
	  InputStream stream=handler.getInputStream(fname);
		if(stream==null) return;

		BufferedReader reader=null;
    try
		{
		  if(encoding.length()>0)
        reader=new BufferedReader(new InputStreamReader(stream,encoding));
      else
        reader=new BufferedReader(new InputStreamReader(stream));
  	}
		catch(Exception ex)
		{
		  return;
    }

    try
    {
		  String line=reader.readLine();
			while(line!=null)
			{
        line=line.trim();
        if(line.length()>0)
				{
				  if(line.charAt(0)!='#')
				  {
					  parse(dest,line);
					}
				}
			  line=reader.readLine();
			}
		  reader.close();
		}
		catch(IOException ex)
		{
		  ex.printStackTrace();
		}


	}

	private String replace(String on,String what,String with)
	{
	  int pos=on.indexOf(what);
		while(pos>=0)
		{
		  String before=on.substring(0,pos);
			String after=on.substring(pos+what.length());
			on=before+with+after;
		  pos=on.indexOf(what);
		}
		return on;
	}

	public String getString(int code,String[] params)
	{
	  String ans=(String)_mainlist.get(new Integer(code));
	  if(ans==null) ans=(String)_backlist.get(new Integer(code));
    if(ans==null) ans=(String)_mainlist.get(new Integer(ERROR_NOT_DEFINED));
    if(ans==null) ans=(String)_backlist.get(new Integer(ERROR_NOT_DEFINED));
    if(ans==null) return getStringP(ERROR_NOT_DEFINED);
    for(int i=params.length-1;i>=0;i--) ans=replace(ans,"%"+(i+1),params[i]);
		return ans;
	}

	public String getString(int code)
	{
		return getString(code,new String[0]);
	}

	public String getString(int code,String p1)
	{
    String p[]={p1};
  	return getString(code,p);
	}

	public String getString(int code,String p1,String p2)
	{
    String p[]={p1,p2};
    return getString(code,p);
	}

	public String getString(int code,String p1,String p2,String p3)
	{
	  String p[]={p1,p2,p3};
		return getString(code,p);
	}

  private String getStringP(int code)
  {
    switch(code)
    {
    case ERROR_NOT_DEFINED:return "Undefined string";
		default:return null;
    }
  }

}

