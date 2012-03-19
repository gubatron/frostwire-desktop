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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ProgressDialog extends JDialog implements ChangeListener{ 
    JLabel statusLabel = new JLabel(); 
    JProgressBar progressBar = new JProgressBar(); 
    ProgressMonitor monitor; 
 
    public ProgressDialog(Frame owner, ProgressMonitor monitor) throws HeadlessException{ 
        super(owner, "Progress", true); 
        init(monitor); 
    } 
 
    public ProgressDialog(Dialog owner, ProgressMonitor monitor) throws HeadlessException{ 
        super(owner); 
        init(monitor); 
    } 
 
    private void init(ProgressMonitor monitor){ 
        this.monitor = monitor; 
 
        progressBar = new JProgressBar(0, monitor.getTotal()); 
        if(monitor.isIndeterminate()) 
            progressBar.setIndeterminate(true); 
        else 
            progressBar.setValue(monitor.getCurrent()); 
        statusLabel.setText(monitor.getStatus()); 
 
        JPanel contents = (JPanel)getContentPane(); 
        contents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        contents.add(statusLabel, BorderLayout.NORTH); 
        contents.add(progressBar); 
 
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); 
        monitor.addChangeListener(this); 
    } 
 
    public void stateChanged(final ChangeEvent ce){ 
        // to ensure EDT thread 
        if(!SwingUtilities.isEventDispatchThread()){ 
            SwingUtilities.invokeLater(new Runnable(){ 
                public void run(){ 
                    stateChanged(ce); 
                } 
            }); 
            return; 
        } 
 
        if(monitor.getCurrent()!=monitor.getTotal()){ 
            statusLabel.setText(monitor.getStatus()); 
            if(!monitor.isIndeterminate()) 
                progressBar.setValue(monitor.getCurrent()); 
        }else 
            dispose(); 
    } 
} 
