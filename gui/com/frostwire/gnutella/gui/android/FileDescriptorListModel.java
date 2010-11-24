package com.frostwire.gnutella.gui.android;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class FileDescriptorListModel extends AbstractListModel {

    /**
     * 
     */
    private static final long serialVersionUID = 3826940677788298380L;

    private List<FileDescriptor> _fileDescriptors;
    private List<FileDescriptor> _filterFileDescriptors;
    
    private String _filterText;

    public FileDescriptorListModel() {
        _fileDescriptors = new ArrayList<FileDescriptor>();
        _filterFileDescriptors = new ArrayList<FileDescriptor>();
    }

    public void clear() {
        _filterText = null;
        int index = _filterFileDescriptors.size() - 1;
        _fileDescriptors.clear();
        _filterFileDescriptors.clear();
        if (index >= 0) {
            fireIntervalRemoved(this, 0, index);
        }
    }
    
    public void addAll(List<FileDescriptor> fileDescriptors) {
        _fileDescriptors.addAll(fileDescriptors);
        refilter();
    }

    public void update(FileDescriptor fileDescriptor) {
        int index = _filterFileDescriptors.indexOf(fileDescriptor);
        fireContentsChanged(this, index, index);
    }

    public void filter(String filterText) {
        _filterText = filterText;
        refilter();
    }

    @Override
    public int getSize() {
        return _filterFileDescriptors.size();
    }

    @Override
    public Object getElementAt(int index) {
        if (index >= 0 && index < _filterFileDescriptors.size()) {
            return _filterFileDescriptors.get(index);
        } else {
            return null;
        }
    }
    
    private void refilter() {
        _filterFileDescriptors.clear();
        
        if (_filterText == null || _filterText.trim().length() == 0) {
            _filterFileDescriptors.addAll(_fileDescriptors);
        } else {
            String subString = _filterText.trim().toLowerCase();
            for (int i = 0; i < _fileDescriptors.size(); i++) {
                FileDescriptor fileDescriptor = _fileDescriptors.get(i);
                if (fileDescriptor.title.toLowerCase().indexOf(subString) != -1 ||
                    fileDescriptor.artist.toLowerCase().indexOf(subString) != -1) {
                    _filterFileDescriptors.add(fileDescriptor);
                }
            }
        }
        
        fireContentsChanged (this, 0, getSize());
    }
}
