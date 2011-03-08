package com.limegroup.gnutella.bugs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.limewire.inject.Providers;

import com.google.inject.Inject;
import com.limegroup.gnutella.gui.LocalClientInfoFactory;
import com.limegroup.gnutella.gui.LocalClientInfoFactoryImpl;
import com.limegroup.gnutella.gui.MultiLineLabel;
import com.limegroup.gnutella.gui.SplashWindow;


/**
 * A bare-bones bug manager, for fatal errors.
 */
public final class FatalBugManager {
    
    @Inject private static volatile LocalClientInfoFactory localClientInfoFactory;    
    
    private FatalBugManager() {}
    
    /**
     * Handles a fatal bug.
     */
    public static void handleFatalBug(Throwable bug) {
        if( bug instanceof ThreadDeath ) // must rethrow.
	        throw (ThreadDeath)bug;
	        
        bug.printStackTrace();
        
        // Build the LocalClientInfo out of the info ...
        LocalClientInfoFactory factoryToUse = localClientInfoFactory;
        if(factoryToUse == null)
            factoryToUse = new LocalClientInfoFactoryImpl(Providers.of((SessionInfo)new FatalSessionInfo()));
        final LocalClientInfo info = factoryToUse.createLocalClientInfo(bug, Thread.currentThread().getName(), null, true);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                reviewBug(info);
            }
        });
    }
    
    private static String warning() {
	/*
        String msg = "Ui" + "jt!j" + "t!Mjn" + "fXjs" + "f/!U" + "if!pg"+
                     "gjdjbm!xfc" + "tjuf!j" + "t!xx" + "x/mj" + "nfxjs" + "f/d" + "pn/";
        StringBuilder ret = new StringBuilder(msg.length());
        for(int i = 0; i < msg.length(); i++) {
            ret.append((char)(msg.charAt(i) - 1));
	    System.out.println("Converting message: "+ ret.toString());
        }
	System.out.println("Final message is: "+ ret.toString());
        return ret.toString();
	*/
	return "You are using FrostWire. www.frostwire.com";
    }
    
    /**
     * Reviews the bug.
     */
    public static void reviewBug(final LocalClientInfo info) {
        final JDialog DIALOG = new JDialog();
        DIALOG.setTitle("Fatal Error");
		final Dimension DIALOG_DIMENSION = new Dimension(100, 300);
		DIALOG.setSize(DIALOG_DIMENSION);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        MultiLineLabel label = new MultiLineLabel(
            warning() + "\n\n" +
            "FrostWire has encountered a fatal internal error and will now exit. " +
            "This is generally caused by a corrupted installation.  Please try " + 
            "downloading and installing FrostWire again.\n\n" +
            "To aid with debugging, please click 'Send' to notify FrostWire about the problem. " +
            "If desired, you can click 'Review' to look at the information that will be sent. " + 
            "If the problem persists, please visit www.frostwire.com and click the 'Support' " + 
            "link.\n\n" +
            "Thank You.", 400);
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.add(Box.createHorizontalGlue());
		labelPanel.add(label);

        JPanel buttonPanel = new JPanel();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendToServlet(info);
				DIALOG.dispose();
				System.exit(1);
			}
		});

        JButton reviewButton = new JButton("Review");
        reviewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                JTextArea textArea = new JTextArea(info.toBugReport());
                textArea.setColumns(50);
                textArea.setEditable(false);
                textArea.selectAll();
                textArea.copy();
                textArea.setCaretPosition(0);                
                JScrollPane scroller = new JScrollPane(textArea);
                scroller.setBorder(BorderFactory.createEtchedBorder());
                scroller.setPreferredSize( new Dimension(500, 200) );
                showMessage(DIALOG, scroller);
			}
		});

		JButton discardButton = new JButton("Discard");
		discardButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        DIALOG.dispose();
		        System.exit(1);
		    }
		});
        buttonPanel.add(sendButton);
        buttonPanel.add(reviewButton);
        buttonPanel.add(discardButton);

        mainPanel.add(labelPanel);
        mainPanel.add(buttonPanel);

        DIALOG.getContentPane().add(mainPanel);
		DIALOG.pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dialogSize = DIALOG.getSize();
		DIALOG.setLocation((screenSize.width - dialogSize.width)/2,
						   (screenSize.height - dialogSize.height)/2);

        DIALOG.setVisible(true);
        
        try {
        	SplashWindow.instance().setVisible(false);
        } catch (Throwable ignore) {
        }
        DIALOG.toFront();
    }
    
    /**
     * Sends a bug to the servlet & then exits.
     */
    private static void sendToServlet(LocalClientInfo info) {
        new ServletAccessor().getRemoteBugInfo(info);
    }
    
    /**
     * Shows a message.
     */
    private static void showMessage(Component parent, Component toDisplay) {
		JOptionPane.showMessageDialog(parent,
				  toDisplay,
				  "Fatal Error - Review",
				  JOptionPane.INFORMATION_MESSAGE);	
    }
    
    private static class FatalSessionInfo implements SessionInfo {

        public boolean acceptedIncomingConnection() {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean canReceiveSolicited() {
            // TODO Auto-generated method stub
            return false;
        }

        public long getByteBufferCacheSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        public long getContentResponsesSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        public long getCreationCacheSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        public long getCurrentUptime() {
            // TODO Auto-generated method stub
            return 0;
        }

        public long getDiskControllerByteCacheSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getDiskControllerQueueSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        public long getDiskControllerVerifyingCacheSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getNumIndividualDownloaders() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getNumLeafToUltrapeerConnections() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getNumOldConnections() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getNumUltrapeerToLeafConnections() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getNumUltrapeerToUltrapeerConnections() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getNumWaitingDownloads() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getNumberOfPendingTimeouts() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getNumberOfWaitingSockets() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getPort() {
            // TODO Auto-generated method stub
            return 0;
        }

        public boolean isGUESSCapable() {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
}