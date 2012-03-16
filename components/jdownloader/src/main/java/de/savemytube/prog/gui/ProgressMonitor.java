/**
 * JToothpaste - Copyright (C) 2007 Matthias
 * Schuhmann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package de.savemytube.prog.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ProgressMonitor{ 
    int total, current=-1; 
    boolean indeterminate; 
    int milliSecondsToWait = 500; // half second 
    String status;
    public boolean started; 
 
    public ProgressMonitor(int total, boolean indeterminate, int milliSecondsToWait){ 
        this.total = total; 
        this.indeterminate = indeterminate; 
        this.milliSecondsToWait = milliSecondsToWait; 
    } 
 
    public ProgressMonitor(int total, boolean indeterminate){ 
        this.total = total; 
        this.indeterminate = indeterminate; 
    } 
 
    public int getTotal(){ 
        return total; 
    } 
 
    public void start(String status){ 
        if(current!=-1) 
            throw new IllegalStateException("not started yet"); 
        this.status = status; 
        current = 0; 
        fireChangeEvent(); 
        started = true;
    } 
 
    public int getMilliSecondsToWait(){ 
        return milliSecondsToWait; 
    } 
 
    public int getCurrent(){ 
        return current; 
    } 
 
    public String getStatus(){ 
        return status; 
    } 
 
    public boolean isIndeterminate(){ 
        return indeterminate; 
    } 
 
    public void setCurrent(String status, int current){ 
        if(current==-1) 
            throw new IllegalStateException("not started yet"); 
        this.current = current; 
        if(status!=null) 
            this.status = status; 
        fireChangeEvent(); 
    } 
 
    /*--------------------------------[ ListenerSupport ]--------------------------------*/ 
 
    private ArrayList listeners = new ArrayList(); 
    private ChangeEvent ce = new ChangeEvent(this); 
 
    public void addChangeListener(ChangeListener listener){ 
        listeners.add(listener); 
    } 
 
    public void removeChangeListener(ChangeListener listener){ 
        listeners.remove(listener); 
    } 
 
    private void fireChangeEvent(){ 
        Iterator iter = listeners.iterator(); 
        while(iter.hasNext()){ 
            ((ChangeListener)iter.next()).stateChanged(ce); 
        } 
    } 
}