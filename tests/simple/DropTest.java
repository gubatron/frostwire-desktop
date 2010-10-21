import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.TransferHandler;


public class DropTest {

	public static void printFlavors(Transferable transferable, JTextArea log) {
		DataFlavor[] flavors = transferable.getTransferDataFlavors();
		log.append("== BEGIN FLAVORS ("+ flavors.length +") ==\r\n");
		for (DataFlavor f : flavors) {
			if (!f.isFlavorJavaFileListType())
				continue;
			log.append("Flavor Name: " + f.getHumanPresentableName() + "\r\n");
			log.append("Mime Type: " + f.getMimeType() + "\r\n");
			log.append("Primary Type: " + f.getPrimaryType() + "\r\n");
			log.append("Sub Type: " + f.getSubType() + "\r\n");
			log.append("Is Java List?: " + f.isFlavorJavaFileListType() + "\r\n");
			log.append("\r\n");
		}
		log.append("== END FLAVORS ("+ flavors.length +") ==\r\n\r\n");
	}
	
	public static void printTransferData(Object t, JTextArea log) {
		log.append("Object ("+t.getClass().getCanonicalName()+"): " + t.toString() + "\r\n");
	}
	
	public static void main(String[] args) {
		final HashMap<Object, Object> seen = new HashMap<Object,Object>();
		JFrame frame = new JFrame();
		JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		final JTextArea log = new JTextArea("Drop Log:\r\n");
		log.setRows(30);
		log.setColumns(40);
		log.setBackground(Color.black);
		log.setForeground(Color.orange);
		
		log.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >1)
					log.setText("");
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}});
		
		final JScrollPane scrollLog = new JScrollPane(log);
		scrollLog.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				scrollLog.scrollRectToVisible(new Rectangle(scrollLog.getWidth(),scrollLog.getHeight()));
			}
		});
		
		final JTextArea dropArea = new JTextArea();
		dropArea.setRows(20);
		dropArea.setColumns(40);
		
		//dropArea.setDragEnabled(true);
		dropArea.setDropMode(DropMode.USE_SELECTION);
		dropArea.setTransferHandler(new TransferHandler() {
			@Override
			public int getSourceActions(JComponent c) {
				return COPY_OR_MOVE;
			}
			
			
			@Override
			public boolean importData(JComponent comp, Transferable t) {
				if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
					try {
						printTransferData(t.getTransferData(DataFlavor.javaFileListFlavor),log);
						return true;
					} catch (UnsupportedFlavorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				return false;
			}
			
			
			@Override
			public boolean canImport(TransferSupport support) {
				if (support == null) {
					log.append("Support Null\r\n");
					return false;
				}

		 		if (support.getTransferable() == null) {
		 			log.append("Transferable Null\r\n");
					return false;
		 		}	
		
		 		if (support.getTransferable()!=null) {
		 			printFlavors(support.getTransferable(),log);
		 		    return true;
		 		}
		 		
		 		return false;
			}
			
			

		});
			
		panel.add(dropArea);
		panel.add(scrollLog);

		frame.setContentPane(panel);
		
		frame.pack();
		frame.setVisible(true);
		frame.invalidate();
		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}});
	}
}
