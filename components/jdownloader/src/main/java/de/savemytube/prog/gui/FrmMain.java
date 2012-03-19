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

import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.http.ProxyDefintion;
import de.savemytube.prog.Processor;
import de.savemytube.prog.util.OpenBrowser;

import javax.swing.JCheckBox;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;

public class FrmMain extends JFrame {
	
	private About about;
	private static final long serialVersionUID = 1L;
    private static final String THE_URL = "http://dasbecks.jucktmich.net";
	private JPanel jContentPane = null;
	private JPanel pnlMain = null;
	private JPanel pnlInput = null;
	private JLabel lblInput = null;
	private JTextField txtUrl = null;
	private JButton jButton = null;
	private JButton btnOpen = null;
	private JMenuBar jJMenuBar = null;
	private JMenu jMenu = null; 
	private JMenuItem exit = null;
	private JMenu JMenu2 = null;
	private JMenuItem Info = null;
    private Processor proc = new Processor();
    private String theFolder;
    private FolderFilter folderFilter;
    private JPanel pnlMedia = null;
    private JCheckBox cbxVideo = null;
    private JCheckBox cbxMusic = null;
    private ProxyDefintion theProxyDef = null;
    private JMenu JMenu3;
    private JMenuItem menuProxy;
    private FrmProxy frmProxy = null;
    
    private FrmProxy getFrmProxy() {
        if (frmProxy == null) {
            frmProxy = new FrmProxy(this);
        }
        return frmProxy;
    }
	/**
	 * This method initializes pnlMain	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPnlMain() {
		if (pnlMain == null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints4.weightx = 0.0D;
			gridBagConstraints4.weighty = 0.0D;
			gridBagConstraints4.fill = GridBagConstraints.NONE;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.gridy = 2;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints3.fill = GridBagConstraints.NONE;
			gridBagConstraints3.gridy = 2;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridwidth = 3;
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.insets = new Insets(5, 5,5,5);
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints.gridy = 1;
			lblInput = new JLabel();
			lblInput.setText("Youtube Url:");
			lblInput.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			pnlMain = new JPanel();
			pnlMain.setLayout(new GridBagLayout());
			pnlMain.add(getPnlInput(), gridBagConstraints2);
			pnlMain.add(lblInput, gridBagConstraints);
			pnlMain.add(getTxtUrl(), gridBagConstraints1);
			pnlMain.add(getJButton(), gridBagConstraints3);
			pnlMain.add(getBtnOpen(), gridBagConstraints4);            
		}
		return pnlMain;
	}

	/**
	 * This method initializes pnlInput	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPnlInput() {
		if (pnlInput == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.gridy = 0;
			pnlInput = new JPanel();
			pnlInput.setLayout(new GridBagLayout());
			pnlInput.add(getPnlMedia(), gridBagConstraints7);
		}
		return pnlInput;
	}

	/**
	 * This method initializes txtUrl	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtUrl() {
		if (txtUrl == null) {
			txtUrl = new JTextField();
			txtUrl.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			txtUrl.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
                    processUrl(txtUrl.getText());
				}
			});
		}
		return txtUrl;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Download");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					processUrl(txtUrl.getText());
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes btnOpen	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnOpen() {
		if (btnOpen == null) {
			btnOpen = new JButton();
			btnOpen.setText("Open");
			btnOpen.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
                    
                    String file = showChooser();                    
                    processFile(file);
				}
			});
		}
		return btnOpen;
	}

    private String showChooser() {
        
        if (theFolder == null) {
            theFolder = System.getProperty("user.dir");
        }
                
        JFileChooser chooser = new JFileChooser(theFolder);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            if (f.isDirectory()) return true;
            return f.getName().toLowerCase().endsWith(".flv");
          }
          public String getDescription () { return "FLVs"; }  
        });
        chooser.setMultiSelectionEnabled(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            theFolder = chooser.getSelectedFile().getAbsoluteFile().getAbsolutePath();
            return chooser.getSelectedFile().getPath();
        }   
        return "";
    }
    
	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getJMenu());
            jJMenuBar.add(getJMenu3());
			jJMenuBar.add(getJMenu2());            
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getJMenu() {
		if (jMenu == null) {
			jMenu = new JMenu();
			jMenu.setText("File");
			jMenu.add(getExit());
		}
		return jMenu;
	}

	/**
	 * This method initializes exit	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExit() {
		if (exit == null) {
			exit = new JMenuItem();
			exit.setText("Exit");
			exit.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.exit(1);
				}
			});
		}
		return exit;
	}

	public void centerScreen() {
		  Dimension dim = getToolkit().getScreenSize();
		  Rectangle abounds = getBounds();
		  setLocation((dim.width - abounds.width) / 2,
		      (dim.height - abounds.height) / 2);
		  
		  requestFocus();
		}
	
	/**
	 * This method initializes JMenu2	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getJMenu2() {
		if (JMenu2 == null) {
			JMenu2 = new JMenu();
			JMenu2.setText("Info");
			JMenu2.add(getInfo());
		}
		return JMenu2;
	}

	/**
	 * This method initializes Info	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getInfo() {
		if (Info == null) {
			Info = new JMenuItem();
			Info.setText("About");
            final Container c = this;
			Info.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					about.setVisible(true);                    
				}
			});
		}
		return Info;
	}
	
    private JMenu getJMenu3() {
        if (JMenu3 == null) {
            JMenu3 = new JMenu();
            JMenu3.setText("Proxy");
            JMenu3.add(getMenuProxy());
        }
        return JMenu3;
    }
    
    private JMenuItem getMenuProxy() {
        if (menuProxy == null) {
            menuProxy = new JMenuItem();
            menuProxy.setText("Settings");
            menuProxy.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showProxySettings();
                }
            });
        }
        return menuProxy;
    }
    
    
    private void showProxySettings() {        
        getFrmProxy().show(getProxyDef());
        this.theProxyDef = getFrmProxy().getProxyDef();
    }
	/**
     * This method initializes pnlMedia	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPnlMedia() {
        if (pnlMedia == null) {
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.insets = new Insets(0, 0, 0, 0);
            gridBagConstraints6.anchor = GridBagConstraints.WEST;
            gridBagConstraints6.gridy = 0;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.anchor = GridBagConstraints.WEST;
            gridBagConstraints5.insets = new Insets(0, 0, 0, 0);
            gridBagConstraints5.gridy = 1;
            pnlMedia = new JPanel();
            pnlMedia.setLayout(new GridBagLayout());
            pnlMedia.setBorder(BorderFactory.createTitledBorder(null, "Extract", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
            pnlMedia.add(getCbxVideo(), gridBagConstraints6);
            pnlMedia.add(getCbxMusic(), gridBagConstraints5);
        }
        return pnlMedia;
    }

    /**
     * This method initializes cbxVideo	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getCbxVideo() {
        if (cbxVideo == null) {
            cbxVideo = new JCheckBox();
            cbxVideo.setText("Video");
        }
        return cbxVideo;
    }

    /**
     * This method initializes cbxMusic	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getCbxMusic() {
        if (cbxMusic == null) {
            cbxMusic = new JCheckBox();
            cbxMusic.setText("Audio");
        }
        return cbxMusic;
    }

    /**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				FrmMain thisClass = new FrmMain();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public FrmMain() {
		super();
		initialize();
        folderFilter = new FolderFilter();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(378, 201);
		this.setJMenuBar(getJJMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle("JToothpaste");
		centerScreen();
		about = new About(this);
        IconLoader ic = new IconLoader();
        setIconImage(ic.createImage("toothpaste_short.gif",""));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getCbxMusic().setSelected(true);
        applyColors(this);
        applyColors(getFrmProxy());
        applyColors(about);
        //applyColors(getJJMenuBar());
        
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getPnlMain(), BorderLayout.CENTER);
		}
		return jContentPane;
	}
    
    public void processUrl(final String url) {
        if (url == null || url.trim().length() == 0) {
            return;
        } 
        
        /*if (url.indexOf("youtube") == -1) {
            JOptionPane.showMessageDialog(this,"Only youtube supported yet!");
            return;
        }*/
        
        
        
        if ("".equals(getFolder())){
            OpenBrowser.openURL(THE_URL);
            return;
        }    
        OpenBrowser.openURL(THE_URL);
        
        final ProxyDefintion proxyDef = getProxyDef();
        final JFrame dlg = this;
        String origTitle = getTitle();
        Runnable run = new Runnable() {

            public void run() {
                String[] s = new String[] {"--","\\","|","/"};
                int counter = 0;
                String text = "Processing";
                while(true) {
                    
                    if (counter > s.length - 1) {
                        counter = 0;
                    }
                    
                    setTitle(text + " " +s[counter]);
                    
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {                  
                    }
                    counter = counter + 1;
                }
            }    
        };
        
        
        Thread t = new Thread(run);
        t.start();
        
        
        try {
            proc.process(url, getCbxMusic().isSelected(), getCbxVideo().isSelected(),theFolder,proxyDef);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        t.stop();
        
        setTitle(origTitle);
        
    }
    
    public void processFile(final String url) {
        if (url == null || url.trim().length() == 0) {
            return;
        }
        final JFrame dlg = this;
                   
        OpenBrowser.openURL(THE_URL);
        
        Runnable run = new Runnable() {

            public void run() {
                ProgressMonitor monitor = ProgressUtil.createModalProgressMonitor(dlg, 100,true, 1000);
                monitor.start("Processing");
                proc.processFLV(url, getCbxMusic().isSelected(), getCbxVideo().isSelected());
                try {
                    monitor.setCurrent(null, monitor.getTotal());
                }
                catch(Exception ex) {}
            }            
        };
        Thread t = new Thread(run);
        t.start();
       
        
    }
    
    private String getFolder() {
        if (theFolder == null) {
            theFolder = System.getProperty("user.dir");
        }
        
        final JFileChooser fc = new JFileChooser(theFolder);
        fc.setFileFilter(folderFilter);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            theFolder = fc.getSelectedFile().getAbsoluteFile().getAbsolutePath();
            return theFolder;
        }
        return "";
    }
    
    private ProxyDefintion getProxyDef() {
        
        if (theProxyDef == null) {
            theProxyDef = new ProxyDefintion("","","","");
        }
        
        return theProxyDef;
    }
    
    private void applyColors(Container comp) {
             
        Component[] components = comp.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setBackground(Color.GRAY);
            components[i].setForeground(Color.white);
            if (components[i] instanceof Container) {
                applyColors((Container)components[i]);
            }                           
            if (components[i] instanceof JButton || 
                components[i] instanceof JTextField ||
                components[i] instanceof JMenuBar ||
                components[i] instanceof JMenuItem ||
                components[i] instanceof JMenu                 
                ) {
                components[i].setBackground(new Color(100,100,100));
            }
        }
        
        
     
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
