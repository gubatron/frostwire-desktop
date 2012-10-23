/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui;

import java.util.Collections;
import java.util.List;

import com.frostwire.core.ConfigurationManager;
import com.frostwire.core.Constants;
import com.frostwire.core.FileDescriptor;
import com.frostwire.gui.library.Finger;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class Librarian {

    private static final Librarian instance = new Librarian();

    public static Librarian instance() {
        return instance;
    }

    private Librarian() {
    }

    public Finger finger(boolean local) {
        Finger finger = new Finger();

        finger.uuid = ConfigurationManager.instance().getUUIDString();
        finger.nickname = ConfigurationManager.instance().getNickname();
        finger.frostwireVersion = Constants.FROSTWIRE_VERSION_STRING;
        finger.totalShared = getNumFiles();

        DeviceInfo di = new DeviceInfo();
        finger.deviceVersion = di.getVersion();
        finger.deviceModel = di.getModel();
        finger.deviceProduct = di.getProduct();
        finger.deviceName = di.getName();
        finger.deviceManufacturer = di.getManufacturer();
        finger.deviceBrand = di.getBrand();
        finger.deviceScreen = di.getScreenMetrics();

        finger.numSharedAudioFiles = getNumFiles(Constants.FILE_TYPE_AUDIO, true);
        finger.numSharedVideoFiles = getNumFiles(Constants.FILE_TYPE_VIDEOS, true);
        finger.numSharedPictureFiles = getNumFiles(Constants.FILE_TYPE_PICTURES, true);
        finger.numSharedDocumentFiles = getNumFiles(Constants.FILE_TYPE_DOCUMENTS, true);
        finger.numSharedApplicationFiles = getNumFiles(Constants.FILE_TYPE_APPLICATIONS, true);
        finger.numSharedRingtoneFiles = getNumFiles(Constants.FILE_TYPE_RINGTONES, true);

        if (local) {
            finger.numTotalAudioFiles = getNumFiles(Constants.FILE_TYPE_AUDIO, false);
            finger.numTotalVideoFiles = getNumFiles(Constants.FILE_TYPE_VIDEOS, false);
            finger.numTotalPictureFiles = getNumFiles(Constants.FILE_TYPE_PICTURES, false);
            finger.numTotalDocumentFiles = getNumFiles(Constants.FILE_TYPE_DOCUMENTS, false);
            finger.numTotalApplicationFiles = getNumFiles(Constants.FILE_TYPE_APPLICATIONS, false);
            finger.numTotalRingtoneFiles = getNumFiles(Constants.FILE_TYPE_RINGTONES, false);
        } else {
            finger.numTotalAudioFiles = finger.numSharedAudioFiles;
            finger.numTotalVideoFiles = finger.numSharedVideoFiles;
            finger.numTotalPictureFiles = finger.numSharedPictureFiles;
            finger.numTotalDocumentFiles = finger.numSharedDocumentFiles;
            finger.numTotalApplicationFiles = finger.numSharedApplicationFiles;
            finger.numTotalRingtoneFiles = finger.numSharedRingtoneFiles;
        }

        return finger;
    }

    public int getNumFiles() {
        // TODO Auto-generated method stub
        return 0;
    }

    private int getNumFiles(byte fileTypeAudio, boolean b) {
        // TODO Auto-generated method stub
        return 0;
    }

    public List<FileDescriptor> getFiles(byte fileType, int i, int maxValue, boolean b) {
        return Collections.emptyList();
    }
}
