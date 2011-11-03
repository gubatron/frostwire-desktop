package com.frostwire.gui.android;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.limewire.util.CommonUtils;
import org.limewire.util.OSUtils;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbItem;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbPathEvent;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbPathListener;
import org.pushingpixels.flamingo.api.bcb.core.BreadcrumbFileSelector;

import com.frostwire.gui.GuiFrostWireUtils;
import com.frostwire.gui.android.LocalFileListModel.OnRootListener;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

public class DesktopExplorer extends JPanel implements ThemeObserver {

    private static final long serialVersionUID = 7362861227107918643L;

    private JToolBar _toolBar;
    private JButton _buttonUp;
    private JButton _buttonNew;
    private JButton _buttonRefresh;
    private JButton _buttonViewThumbnail;
    private JButton _buttonViewList;
    private JLabel _labelSort;
    private JComboBox _comboBoxSort;
    private BreadcrumbFileSelector _breadcrumb;
    private JList _list;
    private JScrollPane _scrollPane;
    private SkinPopupMenu _popupList;
    private SkinPopupMenu _deletePopupMenu;
    private SkinMenuItem _menuOpen;
    private SkinMenuItem _menuRename;
    private SkinMenuItem _menuDelete;
    private SkinMenuItem _menuDeleteMultiple;
    private SkinMenuItem _menuRefresh;
    private JTextArea _textName;
    private JScrollPane _scrollName;

    private LocalFileListModel _model;
    private int _selectedIndexToRename;
    
    private File _savedFolder;
    private File _documentsFolder;
    private File _musicFolder;
    private File _picturesFolder;
    private File _videosFolder;
    
    public DesktopExplorer() {

        _model = new LocalFileListModel();
        _model.setOnRootListener(new OnRootListener() {
            public void onRoot(LocalFileListModel localFileListModel, File path) {
                cancelEdit();
                _breadcrumb.setPath(path);
            }
        });

        _selectedIndexToRename = -1;
        
        File root = CommonUtils.getUserHomeDir();
        _savedFolder = FrostWireUtils.getFrostWireRootFolder();
        _documentsFolder = new File(root, "Documents");
        _musicFolder = new File(root, "Music");
        _picturesFolder = new File(root, "Pictures");
        _videosFolder = new File(root, OSUtils.isMacOSX() ? "Movies" : "Videos");

        setupUI();
        setRootFolder(SharingSettings.DIRECTORY_FOR_OPEN_DESKTOP_EXPLORER.getValue());
        
        ThemeMediator.addThemeObserver(this);
    }

    public File getRootFolder() {
        cancelEdit();
        return _model.getRoot();
    }

    public void setRootFolder(final File path) {
        cancelEdit();
        _model.setRoot(path);
    }
    
    public File getSelectedFolder() {
        cancelEdit();
        LocalFile localFile = (LocalFile) _list.getSelectedValue();
        return localFile != null && localFile.getFile().isDirectory() ? localFile.getFile() : null; 
    }

    public void refresh() {
        cancelEdit();
        _model.refresh();
    }

    protected void setupUI() {
        setLayout(new GridBagLayout());

        setupTop();
        setupList();
    }

    protected void breadcrumb_pathEvent(BreadcrumbPathEvent event) {
        cancelEdit();
        List<BreadcrumbItem<File>> items = _breadcrumb.getModel().getItems();

        if (items.size() > 0) {
            File path = items.get(items.size() - 1).getData();
            OnRootListener listener = _model.getOnRootListener();
            _model.setOnRootListener(null); // avoid infinite recursion
            _model.setRoot(path);
            _model.setOnRootListener(listener);
            SharingSettings.DIRECTORY_FOR_OPEN_DESKTOP_EXPLORER.setValue(path);
        }
    }

    protected void buttonUp_mousePressed(MouseEvent e) {
        actionGotoParentFolder();
    }

    protected void buttonNew_mousePressed(MouseEvent e) {
        cancelEdit();
        LocalFile localFile = _model.createNewFolder();
        if (localFile != null) {
            _list.setSelectedValue(localFile, true);
            startEdit(_list.getSelectedIndex());
        }
    }

    protected void buttonViewThumbnail_mousePressed(MouseEvent e) {
        cancelEdit();
        _list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        _list.setPrototypeCellValue(new LocalFile(_savedFolder));
    }

    protected void buttonViewList_mousePressed(MouseEvent e) {
        cancelEdit();
        _list.setLayoutOrientation(JList.VERTICAL);
        _list.setPrototypeCellValue(new LocalFile(_savedFolder));
    }

    protected void comboBoxSort_actionPerformed(ActionEvent e) {
        cancelEdit();
        JComboBox comboBox = (JComboBox) e.getSource();
        SortByItem item = (SortByItem) comboBox.getSelectedItem();
        _model.sortBy(item.sortBy);
    }

    protected void textName_keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (_selectedIndexToRename != -1 && key == KeyEvent.VK_ENTER) {
            renameSelectedItem(_selectedIndexToRename);
        } else if (key == KeyEvent.VK_ESCAPE) {
            _scrollName.setVisible(false);
        }
    }

    protected void list_mouseClicked(MouseEvent e) {
        cancelEdit();
        if (SwingUtilities.isRightMouseButton(e) && !_list.isSelectionEmpty()) {
            int numSelected = _list.getSelectedIndices().length;
            if (numSelected == 1) {
                _popupList.show(_list, e.getX(), e.getY());
            } else if (numSelected > 1) {
                _deletePopupMenu.show(_list, e.getX(), e.getY());
            }
        } else if (e.getClickCount() >= 2 && !e.isConsumed()) {
            actionOpenFile();
        }
    }

    protected void list_keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            cancelEdit();
        } else if (key == KeyEvent.VK_F5) {
            refresh();
        } else if (key == KeyEvent.VK_ENTER) {
            if (OSUtils.isMacOSX()) {
                actionStartRename();
            } else {
                actionOpenFile();
            }
        } else if (key == KeyEvent.VK_F2) {
            if (!OSUtils.isMacOSX()) {
                actionStartRename();
            }
        } else if (key == KeyEvent.VK_SPACE) {
            if (OSUtils.isMacOSX() || OSUtils.isLinux()) {
                actionOpenFile();
            }
        } else if ((!OSUtils.isMacOSX() && key == KeyEvent.VK_BACK_SPACE) || (OSUtils.isMacOSX() && (key == KeyEvent.VK_UP && e.isMetaDown()))) {
            actionGotoParentFolder();
        }
    }

    private void setupTop() {

        GridBagConstraints c;

        _toolBar = new JToolBar();
        _toolBar.setFloatable(false);
        _toolBar.setRollover(true);
        _toolBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                _scrollName.setVisible(false);
            }
        });

        _toolBar.addSeparator();

        Dimension toolBarButtonSize = new Dimension(28, 28);

        _buttonUp = new JButton();
        _buttonUp.setIcon(new ImageIcon(new UITool().loadImage("folder_up")));
        _buttonUp.setPreferredSize(toolBarButtonSize);
        _buttonUp.setMinimumSize(toolBarButtonSize);
        _buttonUp.setMaximumSize(toolBarButtonSize);
        _buttonUp.setSize(toolBarButtonSize);
        _buttonUp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                buttonUp_mousePressed(e);
            }
        });
        _toolBar.add(_buttonUp);

        _buttonNew = new JButton();
        _buttonNew.setIcon(new ImageIcon(new UITool().loadImage("folder_new")));
        _buttonNew.setPreferredSize(toolBarButtonSize);
        _buttonNew.setMinimumSize(toolBarButtonSize);
        _buttonNew.setMaximumSize(toolBarButtonSize);
        _buttonNew.setSize(toolBarButtonSize);
        _buttonNew.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                buttonNew_mousePressed(e);
            }
        });
        _toolBar.add(_buttonNew);

        _buttonRefresh = new JButton();
        _buttonRefresh.setIcon(new ImageIcon(new UITool().loadImage("refresh")));
        _buttonRefresh.setPreferredSize(toolBarButtonSize);
        _buttonRefresh.setMinimumSize(toolBarButtonSize);
        _buttonRefresh.setMaximumSize(toolBarButtonSize);
        _buttonRefresh.setSize(toolBarButtonSize);
        _buttonRefresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                DesktopExplorer.this.refresh();
            }
        });
        _toolBar.add(_buttonRefresh);

        _toolBar.addSeparator();

        _buttonViewThumbnail = new JButton();
        _buttonViewThumbnail.setIcon(new ImageIcon(new UITool().loadImage("view_thumbnail")));
        _buttonViewThumbnail.setPreferredSize(toolBarButtonSize);
        _buttonViewThumbnail.setMinimumSize(toolBarButtonSize);
        _buttonViewThumbnail.setMaximumSize(toolBarButtonSize);
        _buttonViewThumbnail.setSize(toolBarButtonSize);
        _buttonViewThumbnail.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                buttonViewThumbnail_mousePressed(e);
            }
        });
        _toolBar.add(_buttonViewThumbnail);

        _buttonViewList = new JButton();
        _buttonViewList.setIcon(new ImageIcon(new UITool().loadImage("view_list")));
        _buttonViewList.setPreferredSize(toolBarButtonSize);
        _buttonViewList.setMinimumSize(toolBarButtonSize);
        _buttonViewList.setMaximumSize(toolBarButtonSize);
        _buttonViewList.setSize(toolBarButtonSize);
        _buttonViewList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                buttonViewList_mousePressed(e);
            }
        });
        _toolBar.add(_buttonViewList);

        _toolBar.addSeparator();

        setupButtonFavorite(_toolBar, _savedFolder, I18n.tr("Saved"), "folder_32");
        setupButtonFavorite(_toolBar, DeviceConstants.FILE_TYPE_DOCUMENTS, _documentsFolder, I18n.tr("Documents"));
        setupButtonFavorite(_toolBar, DeviceConstants.FILE_TYPE_AUDIO, _musicFolder, I18n.tr("Music"));
        setupButtonFavorite(_toolBar, DeviceConstants.FILE_TYPE_PICTURES, _picturesFolder, I18n.tr("Pictures"));
        setupButtonFavorite(_toolBar, DeviceConstants.FILE_TYPE_VIDEOS, _videosFolder, I18n.tr("Videos"));
        
        _toolBar.addSeparator();

        _labelSort = new JLabel();
        _labelSort.setText(I18n.tr("Sort:"));
        _toolBar.add(_labelSort);

        _comboBoxSort = new JComboBox();
        _comboBoxSort.addItem(new SortByItem(LocalFileListModel.SORT_BY_NONE, I18n.tr("None")));
        _comboBoxSort.addItem(new SortByItem(LocalFileListModel.SORT_BY_NAME_ASC, I18n.tr("Name Asc")));
        _comboBoxSort.addItem(new SortByItem(LocalFileListModel.SORT_BY_NAME_DESC, I18n.tr("Name Desc")));
        _comboBoxSort.addItem(new SortByItem(LocalFileListModel.SORT_BY_DATE_ASC, I18n.tr("Date Asc")));
        _comboBoxSort.addItem(new SortByItem(LocalFileListModel.SORT_BY_DATE_DESC, I18n.tr("Date Desc")));
        _comboBoxSort.addItem(new SortByItem(LocalFileListModel.SORT_BY_KIND_ASC, I18n.tr("Kind Asc")));
        _comboBoxSort.addItem(new SortByItem(LocalFileListModel.SORT_BY_KIND_DESC, I18n.tr("Kind Desc")));
        _comboBoxSort.addItem(new SortByItem(LocalFileListModel.SORT_BY_SIZE_ASC, I18n.tr("Size Asc")));
        _comboBoxSort.addItem(new SortByItem(LocalFileListModel.SORT_BY_SIZE_DESC, I18n.tr("Size Desc")));
        _comboBoxSort.setSelectedIndex(5);
        _comboBoxSort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                comboBoxSort_actionPerformed(e);
            }
        });
        _toolBar.add(_comboBoxSort);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        add(_toolBar, c);

        _breadcrumb = new BreadcrumbFileSelector();
        _breadcrumb.getModel().addPathListener(new BreadcrumbPathListener() {
            public void breadcrumbPathEvent(BreadcrumbPathEvent event) {
                breadcrumb_pathEvent(event);
            }
        });
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridwidth = 2; // this put a extra column and perform a nice fill at
                         // the end in the top row
        add(_breadcrumb, c);
    }

    private void setupList() {

        GridBagConstraints c;

        _list = new JList(_model);
        _list.setCellRenderer(new LocalFileRenderer());
        _list.addMouseListener(new RedispatchMouseListener(_list));
        _list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        _list.setDragEnabled(true);
        _list.setTransferHandler(new DesktopListTransferHandler());
        _list.setPrototypeCellValue(new LocalFile(_savedFolder));
        _list.setVisibleRowCount(-1);

        _popupList = new SkinPopupMenu();
        _deletePopupMenu = new SkinPopupMenu();

        _menuOpen = new SkinMenuItem(I18n.tr("Open"));
        _menuOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionOpenFile();
            }
        });
        _popupList.add(_menuOpen);

        _menuRename = new SkinMenuItem(I18n.tr("Rename"));
        _menuRename.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionStartRename();
            }
        });
        _popupList.add(_menuRename);

        _menuDelete = new SkinMenuItem(I18n.tr("Delete"));
        _menuDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionStartDelete();
            }
        });
        _popupList.add(_menuDelete);

        _menuDeleteMultiple = new SkinMenuItem(I18n.tr("Delete Files"));
        _menuDeleteMultiple.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionStartDelete();
            }
        });

        _deletePopupMenu.add(_menuDeleteMultiple);

        _popupList.addSeparator();

        _menuRefresh = new SkinMenuItem(I18n.tr("Refresh"));
        _menuRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        _popupList.add(_menuRefresh);

        _list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                cancelEdit();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                cancelEdit();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                list_mouseClicked(e);
            }
        });
        _list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                list_keyPressed(e);
            }
        });

        _scrollPane = new JScrollPane(_list);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 8;

        add(_scrollPane, c);

        _textName = new JTextArea();
        _textName.setLineWrap(true);
        _textName.setWrapStyleWord(true);
        _textName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                textName_keyPressed(e);
            }
        });

        _scrollName = new JScrollPane(_textName);
        _scrollName.setVisible(false);
        _scrollName.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        _scrollName.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        _list.add(_scrollName);
    }

    private JButton setupButtonFavorite(JToolBar toolbar, int type, final File path, String tooltip) {
        return setupButtonFavorite(toolbar, path, tooltip, new UITool().getImageNameByFileType(type));
    }
    
    private JButton setupButtonFavorite(JToolBar toolbar, final File path, String tooltip, String imageName) {
        if (!path.exists()) {
            return null;
        }
        Image image = new UITool().loadImage(imageName).getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        Dimension size = new Dimension(28, 28);
        JButton button = new JButton();
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setSize(size);
        button.setToolTipText(tooltip);
        button.setIcon(new ImageIcon(image));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                cancelEdit();
                setRootFolder(path);
            }
        });
        toolbar.add(button);
        return button;
    }

    private void startEdit(int index) {
        
        if (index < 0) {
            return;
        }

        _selectedIndexToRename = index;
        LocalFile localFile = (LocalFile) _model.getElementAt(index);
        String text = localFile.getName();

        if (_list.getLayoutOrientation() == JList.VERTICAL) { // list mode
            LocalFileRenderer renderer = (LocalFileRenderer) _list.getCellRenderer().getListCellRendererComponent(_list, _list.getModel().getElementAt(index),
                    index, false, false);
            Dimension lsize = renderer.getLabelNameSize();
            Point llocation = renderer.getLabelNameLocation();
            lsize.setSize(lsize.getWidth() - 3, lsize.getHeight() - 8);
            Point p = _list.indexToLocation(index);
            p.translate(llocation.x, llocation.y + 4);
            _textName.setSize(lsize);
            _scrollName.setSize(lsize);
            _scrollName.setLocation(p);
        } else { // thumbnail mode
            Point p = _list.indexToLocation(index);
            p.translate(5, 64);
            _textName.setSize(130, 33);
            _scrollName.setSize(130, 33);
            _scrollName.setLocation(p);
        }

        _textName.setText(text);
        _textName.setSelectionStart(0);
        _textName.setSelectionEnd(localFile.getFile().isFile() ? text.lastIndexOf(".") : text.length());

        _scrollName.setVisible(true);

        _scrollName.requestFocusInWindow();
        _scrollName.requestFocus();
        _textName.requestFocusInWindow();
        _textName.requestFocus();
    }

    private void actionStartRename() {
        cancelEdit();
        int index = _list.getSelectedIndex();
        if (index != -1) {
            startEdit(index);
        }
    }

    private void actionStartDelete() {
    	
        int[] selectedIndices = _list.getSelectedIndices();

        if (selectedIndices.length > 0) {
        	JComponent dialogParent = AndroidMediator.instance().getComponent();
        	
        	int r = JOptionPane.showConfirmDialog(dialogParent, I18n.tr("Are you sure you want to delete the selected files?"),I18n.tr("Are you sure?"),JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        	if (r!=JOptionPane.YES_OPTION) {
        		return;
        	}
        	
            for (int index = 0; index < selectedIndices.length; index++) {
                LocalFile localFile = (LocalFile) _model.getElementAt(selectedIndices[index]);
                if (localFile != null) {
                    
                    if (localFile.getFile().isDirectory() && localFile.getFile().list().length > 0) {
                        JOptionPane.showMessageDialog(dialogParent, I18n.tr("Can't delete not empty folder"), I18n.tr("System"), JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                    
                    boolean success;
                    try {
                        success = localFile.getFile().delete();
                    } catch (Exception e) {
                        success = false;
                    }

                    if (!success) {
                        JOptionPane.showMessageDialog(dialogParent, I18n.tr("Error deleting file"), I18n.tr("System"), JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            }
            _list.clearSelection();
            refresh();
        }
    }

    private void renameSelectedItem(int index) {
        if (!_scrollName.isVisible()) {
            return;
        }
        String text = _textName.getText();
        if (text != null && text.length() > 0) {
            if (text.indexOf('.') == -1) { // no extension? put the old
                                           // extension
                LocalFile localFile = (LocalFile) _model.getElementAt(index);
                if (localFile != null && localFile.getFile().isFile() && localFile.getExt() != null) {
                    text += "." + localFile.getExt();
                }
            }
            _model.rename(index, text);
        }
        _scrollName.setVisible(false);
    }

    private void cancelEdit() {
        _selectedIndexToRename = -1;
        _scrollName.setVisible(false);
    }

    private void actionOpenFile() {
        cancelEdit();
        int index = _list.getSelectedIndex();
        if (index != -1) {
            LocalFile localFile = (LocalFile) _model.getElementAt(index);
            if (localFile != null) {
                if (localFile.getFile().isDirectory()) {
                    setRootFolder(localFile.getFile());
                } else {
                    GuiFrostWireUtils.launchFile(localFile.getFile());
                }
            }
        }
    }

    private void actionGotoParentFolder() {
        cancelEdit();
        File path = _model.getRoot().getParentFile();
        if (path != null) {
            setRootFolder(path);
        }
    }

    private final class SortByItem {
        public int sortBy;
        public String text;

        public SortByItem(int sortBy, String text) {
            this.sortBy = sortBy;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public void updateTheme() {
        removeAll();
        setupUI();
    }
}
