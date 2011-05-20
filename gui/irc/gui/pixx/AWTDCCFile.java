package irc.gui.pixx;

import irc.EventDispatcher;
import irc.dcc.DCCFile;
import irc.dcc.DCCFileListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * The AWT dcc file interface.
 */
public class AWTDCCFile implements DCCFileListener,WindowListener
{
  /**
   * The file.
   */
  protected DCCFile _file;
  /**
   * The displayed frame.
   */
  protected JFrame _frame;
  /**
   * The progress bar.
   */
  protected AWTProgressBar _bar;
  private PixxConfiguration _pixxConfiguration;

  /**
   * Create a new AWTDCCFile.
   * @param config the global irc configuration.
   * @param file the source DCCFile.
   */
  public AWTDCCFile(PixxConfiguration config,DCCFile file)
  {
    _pixxConfiguration=config;
    _file=file;
    _file.addDCCFileListener(this);

    String str="";
    if(file.isDownloading())
      str=_pixxConfiguration.getText(PixxTextProvider.GUI_RETREIVING_FILE,_file.getSize()+"");
    else
      str=_pixxConfiguration.getText(PixxTextProvider.GUI_SENDING_FILE,_file.getSize()+"");


    JLabel label=new JLabel(str);

    _frame=new JFrame();
    _frame.setBackground(Color.white);

    _frame.setLayout(new BorderLayout());
    _frame.addWindowListener(this);

    _bar=new AWTProgressBar();
    _frame.add(label,BorderLayout.NORTH);
    _frame.add(_bar,BorderLayout.CENTER);

    _frame.setTitle(_file.getName());
    _frame.setSize(400,80);
    _frame.setVisible(true);
  }

  /**
   * Release this object.
   */
  public void release()
  {
    _frame.removeAll();
    _file.removeDCCFileListener(this);
    _file=null;
    _frame.removeWindowListener(this);
    _frame.dispose();
    _frame=null;
  }

	/**
	 * Get the source DCCFile.
	 * @return source DCCFile.
	 */
  public DCCFile getFile()
  {
    return _file;
  }

  /**
   * Close this transfert.
   */
  public void close()
  {
    _frame.setVisible(false);
  }

  public void transmitted(Integer icount,DCCFile file)
  {
    //activate();
    int count=icount.intValue();
    if((count&32767)==0)
    {
      double pc=count;
      pc/=_file.getSize();
      _bar.setColor(Color.blue);
      _bar.setValue(pc);
      _bar.repaint();
    }
  }

  public void finished(DCCFile file)
  {
    _frame.setTitle(_pixxConfiguration.getText(PixxTextProvider.GUI_TERMINATED,_file.getName()));
    _bar.setColor(Color.green);
    _bar.setValue(1);
    _bar.repaint();
  }

  public void failed(DCCFile file)
  {
    _frame.setTitle(_pixxConfiguration.getText(PixxTextProvider.GUI_FAILED,_file.getName()));
    _bar.setColor(Color.red);
    _bar.repaint();

  }

  public void windowActivated(WindowEvent e) {}
  public void windowClosed(WindowEvent e) {}
  public void windowClosing(WindowEvent e)
  {
    EventDispatcher.dispatchEventAsync(_file,"leave",new Object[0]);
  }

  public void windowDeactivated(WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {}
  public void windowOpened(WindowEvent e) {}

}

