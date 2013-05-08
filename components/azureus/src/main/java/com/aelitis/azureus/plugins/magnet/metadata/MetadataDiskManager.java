/*
 * Created by  Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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

package com.aelitis.azureus.plugins.magnet.metadata;

import java.io.File;

import org.gudy.azureus2.core3.disk.DiskManager;
import org.gudy.azureus2.core3.disk.DiskManagerCheckRequest;
import org.gudy.azureus2.core3.disk.DiskManagerCheckRequestListener;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.disk.DiskManagerListener;
import org.gudy.azureus2.core3.disk.DiskManagerPiece;
import org.gudy.azureus2.core3.disk.DiskManagerReadRequest;
import org.gudy.azureus2.core3.disk.DiskManagerReadRequestListener;
import org.gudy.azureus2.core3.disk.DiskManagerWriteRequest;
import org.gudy.azureus2.core3.disk.DiskManagerWriteRequestListener;
import org.gudy.azureus2.core3.disk.impl.DiskManagerFileInfoImpl;
import org.gudy.azureus2.core3.disk.impl.DiskManagerFileInfoSetImpl;
import org.gudy.azureus2.core3.disk.impl.piecemapper.DMPieceList;
import org.gudy.azureus2.core3.disk.impl.piecemapper.DMPieceMap;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.impl.TOTorrentMetadata;
import org.gudy.azureus2.core3.util.DirectByteBuffer;
import org.gudy.azureus2.core3.util.IndentWriter;

public class MetadataDiskManager implements DiskManager {

    private final TOTorrentMetadata torrent;

    public MetadataDiskManager(TOTorrentMetadata torrent) {
        this.torrent = torrent;
    }

    @Override
    public boolean stop(boolean closing) {
        return false;
    }

    @Override
    public void start() {
    }

    @Override
    public void setPieceCheckingEnabled(boolean enabled) {
    }

    @Override
    public void saveState() {
    }

    @Override
    public void saveResumeData(boolean interim_save) throws Exception {
    }

    @Override
    public void removeListener(DiskManagerListener l) {
        // TODO Auto-generated method stub

    }

    @Override
    public DirectByteBuffer readBlock(int pieceNumber, int offset, int length) {
        return null;
    }

    @Override
    public void moveDataFiles(File new_parent_dir, String dl_name) {
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public boolean isInteresting(int pieceNumber) {
        return true;
    }

    @Override
    public boolean isDone(int pieceNumber) {
        return false;
    }

    @Override
    public boolean hasOutstandingWriteRequestForPiece(int piece_number) {
        return false;
    }

    @Override
    public boolean hasOutstandingReadRequestForPiece(int piece_number) {
        return false;
    }

    @Override
    public boolean hasOutstandingCheckRequestForPiece(int piece_number) {
        return false;
    }

    @Override
    public boolean hasListener(DiskManagerListener l) {
        return false;
    }

    @Override
    public long getTotalLength() {
        return 0;
    }

    @Override
    public TOTorrent getTorrent() {
        return torrent;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public File getSaveLocation() {
        return null;
    }

    @Override
    public long getRemainingExcludingDND() {
        return 0;
    }

    @Override
    public long getRemaining() {
        return 0;
    }

    @Override
    public long[] getReadStats() {
        return null;
    }

    @Override
    public DiskManagerPiece[] getPieces() {
        return new DiskManagerPiece[0];
    }

    @Override
    public DMPieceMap getPieceMap() {
        return null;
    }

    @Override
    public DMPieceList getPieceList(int pieceNumber) {
        return null;
    }

    @Override
    public int getPieceLength(int piece_number) {
        return 0;
    }

    @Override
    public int getPieceLength() {
        return 0;
    }

    @Override
    public DiskManagerPiece getPiece(int PieceNumber) {
        return null;
    }

    @Override
    public int getPercentDone() {
        return 0;
    }

    @Override
    public int getNbPieces() {
        return 0;
    }

    @Override
    public DiskManagerFileInfo[] getFiles() {
        return new DiskManagerFileInfo[0];
    }

    @Override
    public DiskManagerFileInfoSet getFileSet() {
        return new DiskManagerFileInfoSetImpl(new DiskManagerFileInfoImpl[0], null);
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public int getCompleteRecheckStatus() {
        return 0;
    }

    @Override
    public int getCacheMode() {
        return 0;
    }

    @Override
    public void generateEvidence(IndentWriter writer) {
    }

    @Override
    public boolean filesExist() {
        return false;
    }

    @Override
    public void enqueueWriteRequest(DiskManagerWriteRequest request, DiskManagerWriteRequestListener listener) {
    }

    @Override
    public void enqueueReadRequest(DiskManagerReadRequest request, DiskManagerReadRequestListener listener) {
    }

    @Override
    public void enqueueCompleteRecheckRequest(DiskManagerCheckRequest request, DiskManagerCheckRequestListener listener) {
    }

    @Override
    public void enqueueCheckRequest(DiskManagerCheckRequest request, DiskManagerCheckRequestListener listener) {
    }

    @Override
    public void downloadRemoved() {
    }

    @Override
    public void downloadEnded() {
    }

    @Override
    public DiskManagerWriteRequest createWriteRequest(int pieceNumber, int offset, DirectByteBuffer data, Object user_data) {
        return null;
    }

    @Override
    public DiskManagerReadRequest createReadRequest(int pieceNumber, int offset, int length) {
        return null;
    }

    @Override
    public DiskManagerCheckRequest createCheckRequest(int pieceNumber, Object user_data) {
        return null;
    }

    @Override
    public boolean checkBlockConsistencyForWrite(String originator, int pieceNumber, int offset, DirectByteBuffer data) {
        return false;
    }

    @Override
    public boolean checkBlockConsistencyForRead(String originator, boolean peer_request, int pieceNumber, int offset, int length) {
        return false;
    }

    @Override
    public boolean checkBlockConsistencyForHint(String originator, int pieceNumber, int offset, int length) {
        return false;
    }

    @Override
    public void addListener(DiskManagerListener l) {
    }
}
