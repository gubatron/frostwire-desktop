package com.frostwire.bittorrent;

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.Icon;
/**
 * Generates the tree with checkboxes
 */
public class TreeCheckBox extends DefaultMutableTreeNode {

  public final static int SINGLE_SELECTION = 0;
  public final static int DIG_IN_SELECTION = 4;
  protected int selectionMode;
  protected boolean isSelected;
  private String _path;
  private Long _filesize;

  
  // Icon Attributes
  protected Icon _icon;
  protected String _iconName;

  public TreeCheckBox() {
    this(null);
  }

  public TreeCheckBox(Object userObject) {
    this(userObject, true, false);
  }

  public TreeCheckBox(Object userObject, boolean allowsChildren
                                    , boolean isSelected) {
    super(userObject, allowsChildren);
    this.isSelected = isSelected;
    setSelectionMode(DIG_IN_SELECTION);
  }


  public void setSelectionMode(int mode) {
    selectionMode = mode;
  }

  public int getSelectionMode() {
    return selectionMode;
  }

  public void setSelected(boolean isSelected) {
    this.isSelected = isSelected;
    
    if ((selectionMode == DIG_IN_SELECTION)
        && (children != null)) {
      Enumeration myitem = children.elements();      
      while (myitem.hasMoreElements()) {
        TreeCheckBox node = (TreeCheckBox)myitem.nextElement();
        node.setSelected(isSelected);
      }
    }
  }
  
  public boolean isSelected() {
    return isSelected;
  }

  public String getFilePath() {
	return _path;
  }

  public void setFilePath(String filepath) {
	this._path = filepath;
  }

  public Long getFileSize() {
	return _filesize;
  }

  public void setFileSize(Long filesize) {	
	this._filesize = filesize;	
  }

  public void setIcon(Icon icon) {
    this._icon = icon;
  }

  public Icon getIcon() {
    return _icon;
  }

  public void setIconName(String name) {
    _iconName = name;
  }

  public String getIconName() {
    String str = userObject.toString();
    int index = str.lastIndexOf(".");
    if (index != -1) {
      return str.substring(++index);
    } else {
      return _iconName;
    }
  }

}


