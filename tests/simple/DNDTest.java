import java.awt.FlowLayout;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.dnd.FileTransferable;

public class DNDTest {
	static File[] files;
	
	public static void main(String[] arg) {

		final JFrame frame = new JFrame("DNDTest");
		FlowLayout flow = new FlowLayout(FlowLayout.LEFT);
		JPanel panel = new JPanel(flow);;
		frame.setContentPane(panel);
		
		try {
			files = new File("/Users/gubatron/tmp").listFiles();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		final TableModel dm = new TableModel() {

			@Override
			public void addTableModelListener(TableModelListener l) {
				System.out.println("Model Changed...");
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return null;
			}

			@Override
			public int getColumnCount() {
				// TODO Auto-generated method stub
				return 1;
			}

			@Override
			public String getColumnName(int columnIndex) {
				return "File Name";
			}

			@Override
			public int getRowCount() {
				return files.length;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return files[rowIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public void removeTableModelListener(TableModelListener l) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				System.out.println("Set value at: "+rowIndex + "," + columnIndex);
			}
			
		};
		
		final JTable table = new JTable(dm);	
		table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
		
		JScrollPane scrollpane = new JScrollPane(table);
		panel.add(scrollpane);
		
		frame.pack();
		frame.setVisible(true);
		
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		
		//table.setTransferHandler(DNDUtils.DEFAULT_TRANSFER_HANDLER);
		table.setTransferHandler(DNDUtils.DEFAULT_TRANSFER_HANDLER);
		table.setDragEnabled(true);
		DragSource source = DragSource.getDefaultDragSource();
		source.createDefaultDragGestureRecognizer(table, DnDConstants.ACTION_COPY, new DragGestureListener() {

			@Override
			public void dragGestureRecognized(DragGestureEvent dge) {
				System.out.println("Recognized gesture event...");
				ArrayList<File> files = new ArrayList<File>();
				for (int row : table.getSelectedRows()) {
					files.add((File) dm.getValueAt(row, 1));
				}
				
				Transferable transferable = new FileTransferable(files); 
				dge.startDrag(null,transferable);
			}
		});
		
	}

}