package com.frostwire.gui.library;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import com.limegroup.gnutella.gui.GUIMediator;

public class DeviceFileTypeTreeNode extends LibraryNode {

    private static final long serialVersionUID = 1664082200849026954L;

    private static final Icon applications;
    private static final Icon documents;
    private static final Icon pictures;
    private static final Icon videos;
    private static final Icon ringtones;
    private static final Icon audio;

    static {
        applications = GUIMediator.getThemeImage("speaker");
        documents = GUIMediator.getThemeImage("speaker");
        pictures = GUIMediator.getThemeImage("speaker");
        videos = GUIMediator.getThemeImage("speaker");
        ringtones = GUIMediator.getThemeImage("speaker");
        audio = GUIMediator.getThemeImage("speaker");
    }

    private final Device device;
    private final byte fileType;

    public DeviceFileTypeTreeNode(Device device, byte fileType) {
        super(UITool.getFileTypeAsString(fileType));
        this.device = device;
        this.fileType = fileType;
        updateText();
    }

    public Device getDevice() {
        return device;
    }

    public byte getFileType() {
        return fileType;
    }

    public Icon getIcon() {
        switch (fileType) {
        case DeviceConstants.FILE_TYPE_APPLICATIONS:
            return applications;
        case DeviceConstants.FILE_TYPE_DOCUMENTS:
            return documents;
        case DeviceConstants.FILE_TYPE_PICTURES:
            return pictures;
        case DeviceConstants.FILE_TYPE_VIDEOS:
            return videos;
        case DeviceConstants.FILE_TYPE_RINGTONES:
            return ringtones;
        case DeviceConstants.FILE_TYPE_AUDIO:
            return audio;
        default:
            return null;
        }
    }

    public void updateText() {
        String str = UITool.getFileTypeAsString(fileType) + " (" + UITool.getNumSharedFiles(device.getFinger(), fileType) + ")";
        setUserObject(str);
    }
}
