package irc.gui.pixx;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import irc.gui.*;
import irc.*;
import irc.dcc.*;
import irc.gui.common.*;

/**
 * MDILayout.
 */
class MDILayout implements LayoutManager
{
  private Hashtable _components;

  /**
   * Create a new MDILayout
   */
  public MDILayout()
  {
    _components=new Hashtable();
  }

  public void addLayoutComponent(String name, Component comp)
  {
    _components.put(comp,comp);
  }

  private Component getVisible(Container parent)
  {
    Component[] c=parent.getComponents();
    for(int i=0;i<c.length;i++) if(c[i].isVisible()) return c[i];
    return null;
  }

  public void layoutContainer(Container parent)
  {
    Component c=getVisible(parent);
    if(c==null) return;
    int w=parent.getSize().width;
    int h=parent.getSize().height;
    c.setBounds(0,0,w,h);
  }

  public Dimension minimumLayoutSize(Container parent)
  {
    return new Dimension(0,0);
  }

  public Dimension preferredLayoutSize(Container parent)
  {
    Component visible=getVisible(parent);
    if(visible!=null) return visible.getPreferredSize();
    return new Dimension(0,0);
  }

  public void removeLayoutComponent(Component comp)
  {
    _components.remove(comp);
  }
}

/**
 * The multiplie document interface.
 */
public class PixxMDIInterface extends IRCInterface implements PixxTaskBarListener,PixxMenuBarListener,ActionListener,MouseWheelPanelListener,BaseAWTSourceListener,DockablePanelListener
{
  private PixxPanel _panel;
  private PixxMenuBar _menu;
  private PixxTaskBar _task;
  private Panel _mdi;
  private PopupMenu _popMenu;
	private TextField _nickField;
	private Hashtable _awt2Dock;
	private AWTInterpretor _interpretor;
	private PixxConfiguration _pixxConfiguration;

  private Hashtable _status;
  private Hashtable _channels;
  private Hashtable _queries;
  private Hashtable _dccChats;
  private Hashtable _dccFiles;
  private Hashtable _lists;
  private DefaultSource _defaultSource;
  private AWTDefaultSource _awtDefaultSource;


  /**
   * Create a new PixxMDIInterface.
   * @param config global irc configuration.
   * @throws Exception
   */
  public PixxMDIInterface(IRCConfiguration config) throws Exception
  {
    super(config);
  }

  public void load()
  {
    super.load();
    try
    {
      _ircConfiguration.setGUIInfoString("Pixx's designed interface");
      _pixxConfiguration=new PixxConfigurationLoader(_ircConfiguration).loadPixxConfiguration();
      _panel=new PixxPanel(_pixxConfiguration);
      _defaultSource=null;
      _task=new PixxTaskBar(_pixxConfiguration);
      _interpretor=new AWTInterpretor(_pixxConfiguration,this);
      _awt2Dock=new Hashtable();
      _popMenu=new PopupMenu();
      _panel.setLayout(new BorderLayout());
      _mdi=new Panel();
      _mdi.setLayout(new MDILayout());
      _mdi.setBackground(Color.white);
      _task.add(_popMenu);
      _popMenu.addActionListener(this);
      _task.addPixxTaskBarListener(this);
      _menu=new PixxMenuBar(_pixxConfiguration,true);
      _menu.addPixxMenuBarListener(this);
      _panel.add(_menu,BorderLayout.NORTH);
      _panel.add(_mdi,BorderLayout.CENTER);
  		_nickField=new NonFocusableTextField("");
  		_nickField.addActionListener(this);
      _nickField.setBackground(_pixxConfiguration.getColor(PixxColorModel.COLOR_WHITE));
      
      if(!_pixxConfiguration.getB("nickfield"))
  		{
        _panel.add(_task,BorderLayout.SOUTH);
  		}
  		else
  		{
  		  Panel bottom=new Panel();
  			bottom.setLayout(new BorderLayout());
  			bottom.add(_task,BorderLayout.CENTER);
  			Panel nickConfig=new Panel();
  			nickConfig.setLayout(new BorderLayout());
  			Label label=new Label("Change nickname to:");
  			//Label label=new Label(_panel.getText(PixxTextProvider.GUI_CHANGE_NICK));
  		  label.setBackground(_pixxConfiguration.getColor(PixxColorModel.COLOR_BACK));
  		  label.setForeground(_pixxConfiguration.getColor(PixxColorModel.COLOR_WHITE));
  			Panel outerNickLabel=new Panel();
  			outerNickLabel.setLayout(new BorderLayout());
  			outerNickLabel.add(label,BorderLayout.CENTER);
        outerNickLabel.add(new PixxSeparator(PixxSeparator.BORDER_LEFT),BorderLayout.WEST);
        outerNickLabel.add(new PixxSeparator(PixxSeparator.BORDER_RIGHT),BorderLayout.EAST);
        outerNickLabel.add(new PixxSeparator(PixxSeparator.BORDER_UP),BorderLayout.NORTH);
        outerNickLabel.add(new PixxSeparator(PixxSeparator.BORDER_DOWN),BorderLayout.SOUTH);

  			nickConfig.add(outerNickLabel,BorderLayout.NORTH);
  			nickConfig.add(_nickField,BorderLayout.CENTER);

        bottom.add(nickConfig,(BorderLayout.EAST));
  			_panel.add(bottom,BorderLayout.SOUTH);
  		}

      _channels=new Hashtable();
      _queries=new Hashtable();
      _dccChats=new Hashtable();
      _dccFiles=new Hashtable();
      _lists=new Hashtable();
      _status=new Hashtable();

      _panel.validate();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      throw new Error(ex.getMessage());
    }
  }

  public Component getComponent()
  {
    return _panel;
  }

  public void unload()
  {
    removeAWTDefault();
    _popMenu.removeActionListener(this);
    _task.removePixxTaskBarListener(this);
    _menu.removePixxMenuBarListener(this);
    _nickField.removeActionListener(this);
    _task=null;
    _menu=null;
    _popMenu=null;
    _awt2Dock=null;
    _panel.release();
    _panel=null;
    _pixxConfiguration=null;
    super.unload();
  }

  private void addAWTDefault()
  {
    if(_defaultSource==null) return;
    if(_awtDefaultSource==null)
    {
      _awtDefaultSource=new AWTDefaultSource(_pixxConfiguration,_defaultSource);
      _task.addDefaultSource(_awtDefaultSource,true);
    }
  }

  private void removeAWTDefault()
  {
    if(_awtDefaultSource==null) return;
    _task.removeDefaultSource(_awtDefaultSource);
    _awtDefaultSource.release();
    _awtDefaultSource=null;
  }

  private void channelCreated(Channel chan,Boolean bring)
  {
    AWTChannel awt=new AWTChannel(_pixxConfiguration,chan);
    awt.addBaseAWTSourceListener(this);
    _task.addChannel(awt,bring.booleanValue());
    _channels.put(chan,awt);
  }

  private void channelRemoved(Channel chan)
  {
    AWTChannel s=(AWTChannel)_channels.get(chan);
    s.removeBaseAWTSourceListener(this);
    _task.removeChannel(s);
    _channels.remove(chan);
  }

  private void queryCreated(Query query,Boolean bring)
  {
    AWTQuery awt=new AWTQuery(_pixxConfiguration,query);
    awt.addBaseAWTSourceListener(this);
    _task.addQuery(awt,bring.booleanValue());
    _queries.put(query,awt);
    if(!bring.booleanValue()) _pixxConfiguration.getIRCConfiguration().getAudioConfiguration().onQuery();
  }

  private void queryRemoved(Query query)
  {
    AWTQuery q=(AWTQuery)_queries.get(query);
    q.removeBaseAWTSourceListener(this);
    _task.removeQuery(q);
    _queries.remove(query);
  }

  private void statusCreated(Status status,Boolean bring)
  {
    AWTStatus awt=new AWTStatus(_pixxConfiguration,status);
    _task.addStatus(awt,bring.booleanValue());
    awt.addBaseAWTSourceListener(this);
    _status.put(status,awt);
  }

  private void statusRemoved(Status status)
  {
    AWTStatus s=(AWTStatus)_status.get(status);
    s.removeBaseAWTSourceListener(this);
    _task.removeStatus(s);
    _status.remove(status);
  }

  private void chanListCreated(ChanList list,Boolean bring)
  {
    AWTChanList cl=new AWTChanList(_pixxConfiguration,list);
    _task.addChanList(cl,bring.booleanValue());
    cl.addBaseAWTSourceListener(this);
    _lists.put(list,cl);
  }

  private void chanListRemoved(ChanList list)
  {
    AWTChanList l=(AWTChanList)_lists.get(list);
    l.removeBaseAWTSourceListener(this);
    _task.removeChanList(l);
    _lists.remove(list);
  }

  private void DCCChatCreated(DCCChat chat,Boolean bbring)
  {
    boolean bring=bbring.booleanValue();
    AWTDCCChat awt=new AWTDCCChat(_pixxConfiguration,chat);
    awt.addBaseAWTSourceListener(this);
    _task.addDCCChat(awt,bring);
    _dccChats.put(chat,awt);
  }

  private void DCCChatRemoved(DCCChat chat)
  {
    AWTDCCChat c=(AWTDCCChat)_dccChats.get(chat);
    c.removeBaseAWTSourceListener(this);
    _task.removeDCCChat(c);
    _dccChats.remove(chat);
  }

  /**
   * A DCCFile has been created.
   * @param file created file.
   * @param bring true if source should be brang.
   */
  public void DCCFileCreated(DCCFile file,Boolean bring)
  {
    _dccFiles.put(file,new AWTDCCFile(_pixxConfiguration,file));
  }

  /**
   * A DCCFile has been removed.
   * @param file removed file.
   */
  public void DCCFileRemoved(DCCFile file)
  {
    AWTDCCFile f=(AWTDCCFile)_dccFiles.get(file);
    _dccFiles.remove(file);
    f.close();
    f.release();
  }

  public void sourceCreated(Source source,Boolean bring)
  {
    if(source instanceof DefaultSource)
    {
      _defaultSource=(DefaultSource)source;
      if(_pixxConfiguration.getIRCConfiguration().getB("multiserver")) addAWTDefault();
    }

    if(source instanceof Channel) channelCreated((Channel)source,bring);
    if(source instanceof Query) queryCreated((Query)source,bring);
    if(source instanceof Status)
    {
       if(_pixxConfiguration.getB("showstatus")) statusCreated((Status)source,bring);
    }
    if(source instanceof ChanList) chanListCreated((ChanList)source,bring);
    if(source instanceof DCCChat) DCCChatCreated((DCCChat)source,bring);
    if(source instanceof DCCFile) DCCFileCreated((DCCFile)source,bring);
  }

  public void sourceRemoved(Source source)
  {
    if(source instanceof DefaultSource)
    {
      _defaultSource=null;
      removeAWTDefault();
    }
    if(source instanceof Channel) channelRemoved((Channel)source);
    if(source instanceof Query) queryRemoved((Query)source);
    if(source instanceof Status)
    {
       if(_pixxConfiguration.getB("showstatus")) statusRemoved((Status)source);
    }
    if(source instanceof ChanList) chanListRemoved((ChanList)source);
    if(source instanceof DCCChat) DCCChatRemoved((DCCChat)source);
    if(source instanceof DCCFile) DCCFileRemoved((DCCFile)source);
  }

  public void serverCreated(Server s)
  {
  }

  public void serverConnected(Server s)
  {
    updateConnect();
  }

  public void serverDisconnected(Server s)
  {
    updateConnect();
  }

  public void serverRemoved(Server s)
  {
  }


  /**
   * Set the title.
   * @param title title string.
   * @param context title stle context.
   */
  public void setTitle(String title,StyleContext context)
  {
    _menu.setTitle(title,context);
  }

  /**
   * Set the connected state.
   * @param b true if connected to server, false otherwise.
   */
  public void setConnected(boolean b)
  {
    _menu.setConnected(b);
  }

  private void test()
  {
    updateConnect();
    if(_task.getActive()!=null)
    {
      setTitle(_task.getActive().getTitle(),_task.getActive().getStyleContext());
    }
    else
    {
      setTitle("",_pixxConfiguration.getIRCConfiguration().getDefaultStyleContext());
    }

    triggerActiveChanged(_task.getActive());
  }

  /**
   * Get active awt source.
   * @return the current active awt source.
   */
  public GUISource getActive()
  {
    return _task.getActive();
  }
  
  public void setActive(GUISource source)
  {
    _task.activate((BaseAWTSource)source);
  }

  public GUISource getGUISource(Source source)
  {
    if(source instanceof DefaultSource) return _awtDefaultSource;
    if(source instanceof Channel) return (GUISource)_channels.get(source);
    if(source instanceof Query) return (GUISource)_queries.get(source);
    if(source instanceof Status) return (GUISource)_status.get(source);
    if(source instanceof ChanList) return (GUISource)_lists.get(source);
    if(source instanceof DCCChat) return (GUISource)_dccChats.get(source);
    if(source instanceof DCCFile) return (GUISource)_dccFiles.get(source);
    return null;
  }
  
  private DockablePanel createSource(BaseAWTSource asource)
  {
    DockablePanel source=new DockablePanel(asource,_panel.getColor(PixxPanel.COLOR_BACK));
    source.addDockablePanelListener(this);
    source.setClosingBehaviour(DockablePanel.DO_NOTHING_ON_CLOSE);
    _awt2Dock.put(asource,source);
    return source;
  }

  private DockablePanel getSource(BaseAWTSource source)
  {
    return (DockablePanel)_awt2Dock.get(source);
  }

  private void deleteSource(BaseAWTSource source)
  {
    DockablePanel panel=getSource(source);
    if(panel!=null) panel.removeDockablePanelListener(this);
    _awt2Dock.remove(source);
  }

  /**
   * Dock the specified source.
   * @param source the source to be docked.
   */
  public void dock(BaseAWTSource source)
  {
    getSource(source).dock();
    source.requestFocus();
    if(_pixxConfiguration.getB("hideundockedsources")) _task.show(source);
  }

  /**
   * Undock the specified source.
   * @param source the source to be undocked.
   */
  public void undock(BaseAWTSource source)
  {
    DockablePanel todock=getSource(source);
    BaseAWTSource[] srcs=_task.getZOrderedSources();
    for(int i=0;i<srcs.length;i++)
    {
      DockablePanel dock=getSource(srcs[i]);
      if((dock.isDocked()) && (dock!=todock))
      {
        _task.activate(srcs[i]);
        break;
      }
    }
    todock.undock(source.getShortTitle());
    source.requestFocus();
    if(_pixxConfiguration.getB("hideundockedsources")) _task.hide(source);
  }

  /**
   * Check whether the given source is docked or not.
   * @param source the source to be checked.
   * @return true if the source is docked, false otherwise.
   */
  public boolean isDocked(BaseAWTSource source)
  {
    return getSource(source).isDocked();
  }

  /**
   * Get the interpretor for this interface. Any unrecognized command will be passed
   * to this interpretor.
   * @return the interpretor for this interface, or null if no default interface
   * interpretor should be used.
   */
  public Interpretor getInterpretor()
  {
    return _interpretor;
  }

  /**
   * Find the AWTSource relative to the given source.
   * @param src source to find AWTSource from.
   * @return AWTSource relative to the given source, or null if not found.
   */
  public BaseAWTSource findBaseAWTSource(Source src)
  {
    Enumeration e=_awt2Dock.keys();
    while(e.hasMoreElements())
    {
      BaseAWTSource asrc=(BaseAWTSource)e.nextElement();
      if(asrc.getSource()==src) return asrc;
    }
    return null;
  }

  public void AWTSourceAdded(PixxTaskBar bar,BaseAWTSource asource)
  {
    DockablePanel source=createSource(asource);
    _mdi.add(source);
    source.setVisible(false);
    _panel.validate();
    test();
    if(_pixxConfiguration.getDockingPolicy(asource.getSource().getType().toLowerCase(java.util.Locale.ENGLISH),asource.getSource().getName().toLowerCase(java.util.Locale.ENGLISH))) undock(asource);
  }

  public void AWTSourceRemoved(PixxTaskBar bar,BaseAWTSource asource)
  {
    DockablePanel source=getSource(asource);
    source.dock();
    deleteSource(asource);
    _mdi.remove(source);
    source.release();
    _panel.validate();
    test();
  }

  public void AWTSourceDesactivated(PixxTaskBar bar,BaseAWTSource asource)
  {
    DockablePanel source=getSource(asource);
    _panel.setBackground(_ircConfiguration.getStyleColors((asource.getStyleContext()))[0]);
    source.setVisible(false);
    _panel.validate();
    test();
  }

  public void AWTSourceActivated(PixxTaskBar bar,BaseAWTSource asource)
  {
    if(asource!=null)
    {
      DockablePanel source=getSource(asource);
      source.setVisible(true);
      _panel.validate();
      source.requestFocus();
      source.bring();
    }
    test();
  }

  public void eventOccured(PixxTaskBar bar,BaseAWTSource asource,MouseEvent e)
  {
    DockablePanel source=getSource(asource);
    if(_pixxConfiguration.matchMouseConfiguration("taskbarpopup",e))
    {

      //_selectedSource=asource;
      bar.activate(asource);

      _popMenu.removeAll();
      _popMenu.add(new MenuItem("Close"));
      //_popMenu.add(new MenuItem(_panel.getText(PixxTextProvider.GUI_CLOSE)));

      _popMenu.show(_task,e.getX(),e.getY());
    }
    source.requestFocus();
  }

  public void actionPerformed(ActionEvent e)
  {
    EventDispatcher.dispatchEventAsync(this,"actionPerformedEff",new Object[] {e});
  }

  /**
   * Internally used.
   * @param e
   */
  public void actionPerformedEff(ActionEvent e)
  {
    if(e.getActionCommand().equals("Close"))
    //if(e.getActionCommand().equals(_panel.getText(PixxTextProvider.GUI_CLOSE)))
    {
      //_selectedSource.leave();

      BaseAWTSource src=_task.getActive();
      if(src!=null) src.leave();
    }
		else if(e.getSource()==_nickField)
		{
      BaseAWTSource src=_task.getActive();
      if(src==null) return;
		  src.getSource().sendString("/nick "+_nickField.getText());
		}
  }

  public void connectionClicked(PixxMenuBar bar)
  {
    GUISource current=getActiveSource();
    if(current==null) return;
    Server currentServer=current.getSource().getServer();
    if(currentServer==null) return;

    if(currentServer.isConnected())
      currentServer.disconnect();
    else {
      try {
          com.frostwire.gnutella.gui.chat.ChatMediator.instance().ensureValidNickname();
          currentServer.connect();
      } catch (Exception e) {
        e.printStackTrace();
      } //try/catch
    }
  }

  public void chanListClicked(PixxMenuBar bar)
  {
    GUISource src=_task.getActive();
    if(src==null) return;
    src.getSource().sendString("/list");
  }

  public void aboutClicked(PixxMenuBar bar)
  {
    _pixxConfiguration.getIRCConfiguration().displayAboutPage();
  }

  public void helpClicked(PixxMenuBar bar)
  {
    IRCConfiguration cfg=_pixxConfiguration.getIRCConfiguration();
    if(_pixxConfiguration.getS("helppage")!=null) cfg.getURLHandler().openURL(_pixxConfiguration.getS("helppage"));
  }

  public void closeClicked(PixxMenuBar bar)
  {
    BaseAWTSource src=_task.getActive();
    if(src==null) return;
    src.leave();
  }

  public void dockClicked(PixxMenuBar bar)
  {
    BaseAWTSource src=_task.getActive();
    if(src==null) return;
    if(isDocked(src))
      undock(src);
    else
      dock(src);
  }

  public void mouseWheelMoved(Integer amount)
  {
    BaseAWTSource src=_task.getActive();
    if(src==null) return;
    src.mouseWheelMoved(amount);
  }

  private void updateConnect()
  {
    BaseAWTSource active=_task.getActive();
    if(active==null) return;
    Server currentServer=active.getSource().getServer();
    setConnected(currentServer.isConnected());
  }

  /**
   * Get the active source, or null if none is active.
   * @return active source, or null.
   */
  public GUISource getActiveSource()
  {
    return _task.getActive();
  }

  public void titleChanged(BaseAWTSource source)
  {
    if(source!=getActive()) return;
    setTitle(source.getTitle(),source.getStyleContext());
  }

  public void eventOccured(BaseAWTSource source)
  {
  }

  public void DockablePanelWindowClosing(DockablePanel panel)
  {
    BaseAWTSource source;
    Enumeration e=_awt2Dock.keys();
    while(e.hasMoreElements())
    {
      source=(BaseAWTSource)e.nextElement();
      if(_awt2Dock.get(source)==panel)
      {
        if(_pixxConfiguration.getB("leaveonundockedwindowclose"))
          source.leave();
        else
          panel.dock();
        _task.show(source);
      } 
    }
  }

  public void DockablePanelWindowClosed(DockablePanel panel)
  {
  }

}

