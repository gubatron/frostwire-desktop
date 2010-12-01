package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import com.frostwire.gnutella.gui.android.LocalFile.OnOpenListener;
import com.limegroup.gnutella.gui.I18n;

public class LocalFileListModel extends AbstractListModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3669455405023885518L;
	
	public static final int SORT_BY_NONE = 0;
	public static final int SORT_BY_NAME_ASC = 1;
	public static final int SORT_BY_NAME_DESC = 2;
	public static final int SORT_BY_DATE_ASC = 3;
	public static final int SORT_BY_DATE_DESC = 4;
	public static final int SORT_BY_KIND_ASC = 5;
	public static final int SORT_BY_KIND_DESC = 6;
	public static final int SORT_BY_SIZE_ASC = 7;
    public static final int SORT_BY_SIZE_DESC = 8;
	
	private File _root;
	private List<LocalFile> _files;
	
	private int _sortCriteria;
	private OnRootListener _listener;
	private MyOnOpenListener _myOnOpenListener;
	
	public LocalFileListModel() {
	    _sortCriteria = SORT_BY_KIND_ASC;
		_files = new ArrayList<LocalFile>();
		_myOnOpenListener = new MyOnOpenListener();
	}
	
	public void setRoot(File path) {
		if (!path.isDirectory()) {
			return;
		}
		
		_root = path;
		_files.clear();
		
		List<LocalFile> children = getChildren(_root);
		
		if (_sortCriteria != SORT_BY_NONE) {
    		Collections.sort(children, new Comparator<LocalFile>() {
    		    public int compare(LocalFile localFile1, LocalFile localFile2) {
    		        File f1 = localFile1.getFile();
    		        File f2 = localFile2.getFile();
    		        
    		        Long d1 = f1.lastModified();
    		        Long d2 = f2.lastModified();
    		        
                    switch (_sortCriteria) {
                    case SORT_BY_NAME_ASC: return f1.getName().compareTo(f2.getName());
                    case SORT_BY_NAME_DESC: return -1 * f1.getName().compareTo(f2.getName());
                    case SORT_BY_DATE_ASC: return d1.compareTo(d2);
                    case SORT_BY_DATE_DESC: return -1 * d1.compareTo(d2);
                    case SORT_BY_KIND_ASC: return compareByKind(f1, f2);
                    case SORT_BY_KIND_DESC: return -1 * compareByKind(f1, f2);
                    case SORT_BY_SIZE_ASC: return compareByLength(f1, f2, true);
                    case SORT_BY_SIZE_DESC: return compareByLength(f1, f2, false);
                    default: return 0;
                    }
                }
            });
		}
		
		_files.addAll(children);
		fireOnRoot(path);
		fireContentsChanged(this, 0, _files.size() - 1);
	}
	
	public File getRoot() {
		return _root;
	}
	
	public OnRootListener getOnRootListener() {
		return _listener;
	}
	
	public void setOnRootListener(OnRootListener listener) {
		_listener = listener;
	}

	@Override
	public int getSize() {
		return _files.size();
	}

	@Override
	public Object getElementAt(int index) {
	    if (index >= 0 && index < _files.size()) {
	        return _files.get(index);
	    } else {
	        return null;
	    }
	}
	
	public void refresh() {
		setRoot(_root);
	}
	
	public LocalFile createNewFolder() {
	    File file = null;
	    LocalFile localFile = null;
	    
	    int n = 0;
	    while ((file = new File(_root, I18n.tr("New Folder") + (n == 0 ? "" : " " + n))).exists()) {
	        n++;
	    }
        
	    try {
            file.mkdir();
            localFile = new LocalFile(file);
            localFile.setOnOpenListener(_myOnOpenListener);
            _files.add(localFile);
            int index = _files.size() - 1;
            fireIntervalAdded(this, index, index);
        } catch (Exception e) {
        }
        
        return localFile;
    }
	
	public void sortBy(int criteria) {
	    _sortCriteria = criteria;
        refresh();
    }
	
	protected void fireOnRoot(File path) {
		if (_listener != null) {
			_listener.onRoot(this, path);
		}
	}
	
	private List<LocalFile> getChildren(File path) {
		if (path == null || !path.isDirectory()) {
			return new ArrayList<LocalFile>();
		}
		
		ArrayList<LocalFile> result = new ArrayList<LocalFile>();
		
		for (File f : path.listFiles()) {
			if (!f.isHidden()) {
				LocalFile localFile = new LocalFile(f);
				localFile.setOnOpenListener(_myOnOpenListener);
				result.add(localFile);
			}
		}

		return result;
	}
	
	private int compareByKind(File f1, File f2) {
	    
	    if (f1.isDirectory() && f2.isDirectory()) {
	        return f1.getName().compareTo(f2.getName());
	    } else if (f1.isDirectory()) {
            return -1;
        } else if (f2.isDirectory()) {
            return 1;
        }
	    
	    int index1 = f1.getName().lastIndexOf('.');
	    int index2 = f2.getName().lastIndexOf('.');
	    
	    if (index1 == -1 && index2 == -1) {
	        return f1.getName().compareTo(f2.getName());
	    } else if (index1 == -1) {
	        return 1;
	    } else if (index2 == -1) {
	        return -1;
	    }
	    
	    String ext1 = f1.getName().substring(index1);
	    String ext2 = f2.getName().substring(index2);
	    
	    if (!ext1.equals(ext2)) {
	        return ext1.compareTo(ext2);
	    } else {
	        return f1.getName().compareTo(f2.getName());
	    }
	}
	
	private int compareByLength(File f1, File f2, boolean asc) {
        
        if (f1.isDirectory() && f2.isDirectory()) {
            return f1.getName().compareTo(f2.getName());
        } else if (f1.isDirectory()) {
            return -1;
        } else if (f2.isDirectory()) {
            return 1;
        }
        
        Long l1 = f1.length();
        Long l2 = f2.length();
        
        return (asc ? 1 : -1) * l1.compareTo(l2);
    }
	
	public void rename(int index, String name) {
	    LocalFile localFile = (LocalFile) getElementAt(index);
	    if (localFile != null) {
	        localFile.rename(name);
	        fireContentsChanged(this, index, index);
	    }
	}
	
	public interface OnRootListener {
		public void onRoot(LocalFileListModel localFileListModel, File path);
	}
	
	private final class MyOnOpenListener implements OnOpenListener {
		public void onOpen(LocalFile localFile) {
			setRoot(localFile.getFile());
		}
	}
}
