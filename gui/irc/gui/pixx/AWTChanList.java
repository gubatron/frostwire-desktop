package irc.gui.pixx;

import irc.*;
import irc.style.*;

import java.awt.*;

/**
 * The AWT channel list.
 */
public class AWTChanList extends BaseAWTSource implements ChanListListener
{
  /**
   * Horizontal scrollbar.
   */
  protected PixxHorizontalScrollBar _hscroll;

  /**
   * Create a new AWTChanList.
   * @param config the global irc configuration.
   * @param list the source channel list.
   */
  public AWTChanList(PixxConfiguration config,ChanList list)
  {
    super(config,list,true);

    list.addChanListListener(this);

    _hscroll=new PixxHorizontalScrollBar(_pixxConfiguration,0,0,0.1);
    _hscroll.addPixxScrollBarListener(this);

    _list.setWrap(false);

    remove(_textField);
    add(_hscroll,BorderLayout.SOUTH);

    setTitle("Available Chat Rooms... Double click the chatroom to join it or you can type /join <roomname>.");
    //setTitle(getText(PixxTextProvider.SOURCE_CHANLIST,getChanList().getName()));
    _list.clear(1024);
  }

  public void release()
  {
    ((ChanList)_source).removeChanListListeners(this);
    _hscroll.removePixxScrollBarListener(this);
    _hscroll.release();
    _hscroll=null;
    super.release();
  }

  public String getShortTitle()
  {
    return "Chat Rooms";
    //return getText(PixxTextProvider.GUI_CHANNELS);
  }

  public void setFieldText(String txt)
  {
  }

  public String getFieldText()
  {
    return "";
  }

  public void validateText()
  {
  }

  /**
   * Get the source chanlist.
   * @return source chanlist.
   */
  public ChanList getChanList()
  {
    return (ChanList)getSource();
  }

  public void channelBegin(ChanList list)
  {
    clear(getSource());
    print("Retrieving available rooms...");
    //print(_pixxConfiguration.getText(PixxTextProvider.SOURCE_CHANLIST_RETREIVING));
    _list.setFirst(0);
  }

  private void sort(ChannelInfo[] info,int begin,int end,int deep)
  {
    if(deep<50)
    {
      if(begin<end)
      {
        ChannelInfo tmp;
  
        int f=(begin+end)/2;
        tmp=info[f];
        info[f]=info[begin];
        info[begin]=tmp;
  
        int p_pos=begin;
        ChannelInfo pivot=info[p_pos];
        for(int i=begin;i<=end;i++)
        {
          if(info[i].userCount>pivot.userCount)
          {
            p_pos++;
            tmp=info[p_pos];
            info[p_pos]=info[i];
            info[i]=tmp;
          }
        }
        tmp=info[p_pos];
        info[p_pos]=info[begin];
        info[begin]=tmp;
  
        sort(info,begin,p_pos-1,deep+1);
        sort(info,p_pos+1,end,deep+1);
      }
    }
    else
    {    
      for(int i=begin;i<=end;i++)
      {
        ChannelInfo little=info[i];
        int littleindex=i;
        int littleuser=little.userCount;
        for(int j=i+1;j<=end;j++)
        {
          if(info[j].userCount>littleuser)
          {
            little=info[j];
            littleindex=j;
            littleuser=little.userCount;
          }
        } 
        
        ChannelInfo tmp=info[i];
        info[i]=info[littleindex];
        info[littleindex]=tmp;
      }
    }
  }

  private void sort(ChannelInfo[] info)
  {
    sort(info,0,info.length-1,0);
  }

  public void channelEnd(ChanList list)
  {
    ChannelInfo[] info=getChanList().getChannels();
    sort(info);
    int count=info.length;
    if(count>1024) count=1024;
    String[] lines=new String[count];
    for(int i=0;i<count;i++) lines[i]=format(info[i]);
    
    clear(getSource());
    _list.addLines(lines);
    _list.setFirst(0);
    _scroll.setMaximum(_list.getLineCount()-1);
    _scroll.setValue(_list.getLast());
    _hscroll.setMaximum(_list.getLogicalWidth()/10);
  }

  private String format(ChannelInfo item)
  {
    String msg=item.name;
    String count=""+item.userCount;
    for(int i=0;i<20-item.name.length();i++) msg+=" ";
    msg+="   "+item.userCount;
    for(int i=0;i<5-count.length();i++) msg+=" ";
    msg+="   "+item.topic;
    return msg;
  }

  public void channelAdded(ChannelInfo item,ChanList list)
  {
    int count=getChanList().getChannelCount();
    int total=getChanList().getIgnoredChannelCount()+count;
    if(total%100==0)
    {
      clear(getSource());
      print("Retrieving available rooms... ("+count+"/"+total+")");
      //print(_pixxConfiguration.getText(PixxTextProvider.SOURCE_CHANLIST_RETREIVING)+" ("+count+"/"+total+")");
      _list.setFirst(0);
    }
  }

  public void valueChanged(PixxScrollBar pixScrollBar)
  {
    if(pixScrollBar==_hscroll)
      _list.setLeft(_hscroll.getValue()*10);
    else super.valueChanged(pixScrollBar);
  }

  public void virtualSizeChanged(StyledList lis)
  {
    _hscroll.setMaximum(_list.getLogicalWidth()/10);
    super.virtualSizeChanged(lis);
  }
}
