package irc.gui.common;

import irc.EventDispatcher;
import irc.ListenerGroup;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JTextField;

/**
 * The special input text field used in AWTSource.
 */
public class AWTIrcTextField extends Container implements ActionListener,KeyListener,FocusListener
{
  private int _index;
  private int _tabCount;
  private String _completing;
  private String[] _completeList;
  private Vector _historic;
  private ListenerGroup _listeners;
  private JTextField _field;
  private boolean _useEnterTextHere;
  private String _enterTextHere;
  
  /**
   * Create a new AWTIrcTextField.
   */
  public AWTIrcTextField()
  {
    super();
    _useEnterTextHere=false;
    setLayout(new GridLayout(1,1));
    _field=new JTextField();
    add(_field);
    _completeList=new String[0];
    _field.setFont(new Font("SanSerif",Font.PLAIN,13));
    _tabCount=0;
    _completing="";
    _index=0;
    _listeners=new ListenerGroup();
    _historic=new Vector();
    _field.addActionListener(this);
    _field.addKeyListener(this);

    try
    {
      Class c=_field.getClass();
      java.lang.reflect.Method m=c.getMethod("setFocusTraversalKeysEnabled",new Class[] {boolean.class});
      m.invoke(_field,new Object[] {new Boolean(false)});
    }
    catch(Exception ex)
    {
      //ignore it...
    }

    addFocusListener(this);
   
  }

  /**
   * Release this object.
   */
  public void release()
  {
    removeFocusListener(this);
    _field.removeActionListener(this);
    _field.removeKeyListener(this);
    _historic=new Vector();
    _field=null;
    removeAll();
  }

  /**
   * Configure the "enter text here" feature.
   * @param b true if feature enabled, false otherwise.
   * @param text text to be displayed.
   */
  public void setEnterTextHere(boolean b,String text)
  {
    _useEnterTextHere=b;
    _enterTextHere=text;
    
    if(_useEnterTextHere)
    {
      _field.setText(_enterTextHere);
      _field.setSelectionStart(0);
      _field.setSelectionEnd(_field.getText().length()+1);
    }
  }
  
  public void setBackground(Color c)
  {
    super.setBackground(c);
    _field.setBackground(c);
  }

  public void setForeground(Color c)
  {
    super.setForeground(c);
    _field.setForeground(c);
  }

  public void focusGained(FocusEvent e)
  {
    _field.requestFocus();
  }

  public void focusLost(FocusEvent e)
  {
    //nothing here
  }

  /**
   * Add an action listener on the field.
   * @param lis the action listener to add.
   */
  public void addActionListener(ActionListener lis)
  {
    _listeners.addListener(lis);
  }

  /**
   * Remove an action lsitener from the field.
   * @param lis action listener to remove.
   */
  public void removeActionListener(ActionListener lis)
  {
    _listeners.removeListener(lis);
  }

  /**
   * Set the list of known words to be auto completed if the type key is pressed.
   * @param list array of known words.
   */
  public void setCompleteList(String[] list)
  {
    _completeList=new String[list.length];
    for(int i=0;i<list.length;i++) _completeList[i]=list[i];
  }

  /**
   * Set this field content.
   * @param txt new text content.
   */
  public void setText(String txt)
  {
    _field.setText(txt);
  }

  /**
   * Return text content.
   * @return text content.
   */
  public String getText()
  {
    return _field.getText();
  }

  /**
   * Get the current caret position.
   * @return caret position.
   */
  public int getCaretPosition()
  {
    return _field.getCaretPosition();
  }

  /**
   * Set the caret position.
   * @param pos new caret position.
   */
  public void setCaretPosition(int pos)
  {
    _field.setCaretPosition(pos);
  }

  private void type(int c)
  {
    int selA=_field.getSelectionStart();
    int selB=_field.getSelectionEnd();
    String t=_field.getText();
    if(selA!=selB)
    {
      t=t.substring(0,selA)+t.substring(selB);
      _field.setCaretPosition(selA);
    }
    int p=_field.getCaretPosition();
    String before=t.substring(0,p);
    String after=t.substring(p);
    _field.setText(before+((char)c)+after);
    _field.setCaretPosition(p+1);
  }

  private void getCompleting()
  {
    _completing="";
    String t=_field.getText();

    if((_field.getCaretPosition()==t.length()) || (t.charAt(_field.getCaretPosition())==' '))
    {
      for(int i=_field.getCaretPosition()-1;i>=0;i--)
      {
        if(t.charAt(i)==' ') break;
        _completing=t.charAt(i)+_completing;
      }
    }
  }

  private void complete()
  {
    if(_completing.length()==0) return;
    String begin=_completing.toLowerCase(java.util.Locale.ENGLISH);
    Vector match=new Vector();
    for(int i=0;i<_completeList.length;i++)
    {
      if(_completeList[i].toLowerCase(java.util.Locale.ENGLISH).startsWith(begin))
      {
        match.insertElementAt(_completeList[i],match.size());
      }
    }
    if(match.size()>0)
    {
      String completeItem=(String)match.elementAt(_tabCount%match.size());
      int p=_field.getCaretPosition();
      String t=_field.getText();
      String before=t.substring(0,p);
      String after=t.substring(p);
      //supprimer le dernier mot de before (garder l'espace)
      int space=before.lastIndexOf(' ');
      if(space==-1)
        before="";
      else
        before=before.substring(0,space+1);
      before+=completeItem;
      _field.setText(before+after);
      _field.setCaretPosition(before.length());
    }
  }

  public void keyPressed(KeyEvent e)
  {
    if((e.getKeyCode()==KeyEvent.VK_TAB) || (e.getKeyCode()==KeyEvent.VK_PAGE_DOWN))
    {
      if(_tabCount==0) getCompleting();
      complete();
      _tabCount++;
      e.consume();
    }
    else
    {
      _tabCount=0;
    }
    if(e.getKeyCode()==KeyEvent.VK_UP)
    {
      if(_historic.size()>0)
      {
        _index--;
        if(_index==-1) _index=0;
        _field.setText((String)_historic.elementAt(_index));
        setCaretPosition(getText().length());
      }
      e.consume();
    }
    else if(e.getKeyCode()==KeyEvent.VK_DOWN)
    {
      if(_historic.size()>0)
      {
        _index++;
        if(_index>_historic.size()) _index=_historic.size();
        if(_index<_historic.size())
        {
          _field.setText((String)_historic.elementAt(_index));
        }
        else
        {
          _field.setText("");
        }
        setCaretPosition(getText().length());
      }
      e.consume();
    }
    else if((e.getKeyCode()==KeyEvent.VK_K) && e.isControlDown())
    {
      type(3);
      e.consume();
    }
    else if((e.getKeyCode()==KeyEvent.VK_B) && e.isControlDown())
    {
      type(2);
      e.consume();
    }
    else if((e.getKeyCode()==KeyEvent.VK_U) && e.isControlDown())
    {
      type(31);
      e.consume();
    }
    else if((e.getKeyCode()==KeyEvent.VK_R) && e.isControlDown())
    {
      type(22);
      e.consume();
    }
    else if((e.getKeyCode()==KeyEvent.VK_O) && e.isControlDown())
    {
      type(15);
      e.consume();
    }
    else if((e.getKeyCode()==KeyEvent.VK_W) && e.isControlDown())
    {
      type(30);
      e.consume();
    }
  }

  public void keyReleased(KeyEvent e)
  {
    //nothing here...
  }

  public void keyTyped(KeyEvent e)
  {
    //nothing here...
  }

  /**
   * Validate the curren text, as if user pressed enter key.
   */
  public void validateText()
  {
    if(getText().length()>0)
    {
      _historic.insertElementAt(getText(),_historic.size());
      _index=_historic.size();
    }
    _listeners.sendEvent("actionPerformed",new Object[] {new ActionEvent(this,0,"validate")});
  }

  public void actionPerformed(ActionEvent e)
  {
    EventDispatcher.dispatchEventAsync(this,"validateText",new Object[0]);
  }
}

