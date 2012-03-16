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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ProgressUtil{ 
    static class MonitorListener implements ChangeListener, ActionListener{ 
        ProgressMonitor monitor; 
        Window owner;  
        Timer timer; 
 
        public MonitorListener(Window owner, ProgressMonitor monitor){ 
            this.owner = owner; 
            this.monitor = monitor; 
        } 
 
        public void stateChanged(ChangeEvent ce){ 
            ProgressMonitor monitor = (ProgressMonitor)ce.getSource(); 
            if(monitor.getCurrent()!=monitor.getTotal()){ 
                if(timer==null){ 
                    timer = new Timer(monitor.getMilliSecondsToWait(), this); 
                    timer.setRepeats(false); 
                    timer.start(); 
                } 
            }else{ 
                if(timer!=null && timer.isRunning()) 
                    timer.stop(); 
                monitor.removeChangeListener(this); 
            } 
        } 
 
        public void actionPerformed(ActionEvent e){ 
            monitor.removeChangeListener(this); 
            ProgressDialog dlg = null;
            if (owner instanceof Frame) {
               dlg = new ProgressDialog((Frame)owner, monitor);
            }
            else { 
               dlg = new ProgressDialog((Dialog)owner, monitor);
            }
                      
                   
            dlg.pack(); 
            dlg.setLocationRelativeTo(null); 
            dlg.setVisible(true); 
        } 
    } 
 
    public static ProgressMonitor createModalProgressMonitor(Component owner, int total, boolean indeterminate, int milliSecondsToWait){ 
        ProgressMonitor monitor = new ProgressMonitor(total, indeterminate, milliSecondsToWait); 
        Window window = owner instanceof Window 
                ? (Window)owner 
                : SwingUtilities.getWindowAncestor(owner); 
        monitor.addChangeListener(new MonitorListener(window, monitor)); 
        return monitor; 
    } 
}
