package com.frostwire.bittorrent;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;

/**
 * This class creates the tree and all the GUI for handling
 * BT Files.
 *   
 * It calls a graphical Dialog to let the user select whichs to download.
 * 
 * 
 * 
 * @author Fernando Toussaint
 * 
 *
 */

/**
 * Windows to choose torrents
 */
public class BTFilesManager extends JDialog {

	/**
     * 
     */
    private static final long serialVersionUID = 3858296202021922229L;
    public List<com.limegroup.bittorrent.BTData.BTFileData> _files = null;
	public File _tfile;
	public List<com.limegroup.bittorrent.BTData.BTFileData> _selectedfiles = null;
	public Set<String> _selectedfolders = null;

        //public List<com.limegroup.bittorrent.BTData.BTFileData> items_data; // File list containing the position including the root folder


	private TreeCheckBox[] _nodes;

  public BTFilesManager(List<com.limegroup.bittorrent.BTData.BTFileData> filesinside, File torrentfilename) {
        super(GUIMediator.getAppFrame(), I18n.tr("Select files to download"), true);
	this._files= filesinside;
	this._tfile= torrentfilename;
	buildWindow();
  }
  
  public BTFilesManager() {
    super(GUIMediator.getAppFrame(), I18n.tr("Select files to download"), true);
    //List<com.limegroup.bittorrent.BTData.BTFileData> filesinside
	buildWindow();
    

  }

  private void buildWindow() {	
	Integer itemstotal = _files.size() + 1; // Total items + Root Folder checkbox

	if (itemstotal == 0)
		return; // no items to list

	String rootfn = _tfile.getName().substring(0,_tfile.getName().lastIndexOf(".")); // Root Folder Name
	
        // *** The following items maybe could be converted to convert in FileData Format USING ITEMS_data
	String[] items = new String[itemstotal]; // files + root folder (symbolic) used by frostwire to save the file inside "Saved" folder
        Long[] items_size = new Long[itemstotal]; // files + root folder (symbolic) used by frostwire to save the file inside "Saved" folder

	String nodename="";
    	String subfolder="";
    	String subfolderpath="";
        //List<String> pathscreated = new ArrayList(); // Folders added
	Set<String> pathscreated = new HashSet<String>(); // Folders added in format used by BTData

	Integer pos=0;
	Integer numsubfolders=0; // Max number of subfolders (deepest level)
        Integer numsubfoldersthis=0;

	items[pos] = rootfn; // Generates the name for the first node, in other words the root node.
	items_size[pos] = null; // 0 because it's a folder
        //items_data.add(new com.limegroup.bittorrent.BTData.BTFileData(null, rootfn));
	String thisfile="";

	for(com.limegroup.bittorrent.BTData.BTFileData currfile : _files) {	
		pos++;
		thisfile=currfile.getPath().substring(currfile.getPath().indexOf(File.separator)+1);
		//    /Ubuntu.Transformation.Pack.sh/downtr/!!!Readme_first!!!!.txt
		//    Ubuntu.Transformation.Pack.sh/downtr/!!!Readme_first!!!!.txt    
                // thisfile= downtr/!!!Readme_first!!!!.txt    
		while (thisfile.indexOf(File.separator) > -1) { // There's a subfolder
		
		subfolder = thisfile.substring(0,thisfile.indexOf(File.separator)); // Ubuntu.Transformation.Pack.sh	
		thisfile = thisfile.replaceFirst(subfolder + File.separator,""); // get deep inside the subfolders
		//System.out.println("Subfolder: *"+ subfolder +"* Ruta Subfolders: *" + nodename + "*");

			if (!subfolder.equals("")) { // if the file is inside a subfolder then add the subfolder to the tree. > -1 checks is not a folder it's a file

				// old style works subfolderpath = subfolderpath + subfolder + File.separator;
				subfolderpath = subfolderpath  + File.separator + subfolder;

				if (!isInList(subfolderpath,pathscreated)) {
		            //System.out.println("Subfolder *"+ subfolder +"* has been created!");
					pathscreated.add(subfolderpath);
				}

				
			
			} // end of this contains subfolders
		
		}//end of while subfolders

		//System.out.println("File inside Meta From Download factory!!: " + currfile.getPath() + " " + currfile.getLength() + " Bytes - Number of subfolders: " + currfile.getPath().replaceAll("[^"+ File.separator +"]", "").length());
		// Get the number of folders (elements) to be created first
		//if (currfile.getPath().replaceAll("[^"+ File.separator +"]", "").length() > numsubfolders)
		//	numsubfoldersthis=currfile.getPath().substring(1).replaceAll("[^"+ File.separator +"]", "").length();
		String path=File.separator; //temporary just testing for Windows
		if (path.contains("\\"))
			path = path + "\\";
		if (currfile.getPath().replaceAll("[^"+ File.separator +"]", "").length() < numsubfoldersthis) {
			numsubfolders = numsubfolders + numsubfoldersthis;
			numsubfoldersthis = 0;
		}
			
		items[pos] = currfile.getPath();
		items_size[pos] = currfile.getLength();
		//System.out.println("items[" + pos + "] = "+ currfile.getPath());
        //System.out.println("items_size[" + pos + "] = "+ currfile.getLength());
		subfolderpath="";
		}
	numsubfolders = pathscreated.size();
	if (numsubfolders > 0)
		itemstotal = itemstotal + numsubfolders;

	//System.out.println("Subfolders in total: " + numsubfolders);
	_selectedfolders = pathscreated;
	/*
	for(String currfolder : pathscreated) {	
		System.out.println("Folders at my WINDOW: " + currfolder);

	}
	*/

		
		//File.separator

    // Funciona
    //TreeCheckBox[] nodes = new TreeCheckBox[itemstotal+1]; // items(files) + subfolders
    _nodes = new TreeCheckBox[itemstotal+1]; // items(files) + subfolders
    //System.out.println("Positions in node: " + itemstotal);
    /*
    for (int i=0;i<items.length;i++) {
      nodes[i] = new TreeCheckBox(items[i]);
      System.out.println("****Root position for "+ items[i] +": " + items[i].indexOf(File.separator));
    }
*/

    Integer nodepos=0; // Node position
    Integer parentpos=0; // Parent

    List<String> psorted = new ArrayList<String>(); // In format used for being a Sorted list
    List<Integer> folderspos = new ArrayList<Integer>(); // quick way to know if the node is a folder

    for (int k=0;k<items.length;k++) { //itemstotal //items.length // works ok for single file
	//System.out.println("Ruta es: " + items[k]);	
	nodename = items[k].substring(items[k].indexOf(File.separator)+1);
	//System.out.println("Quitando el SLASH el nodo se llama: " + nodename);	
	while (nodename.indexOf(File.separator) > -1) { // There's a subfolder
		
		subfolder = nodename.substring(0,nodename.indexOf(File.separator)); 
		nodename = nodename.replaceFirst(subfolder + File.separator,""); 
		

			if (!subfolder.equals("")) { // if the file is inside a subfolder then add the subfolder to the tree. > -1 checks is not a folder it's a file

				subfolderpath = subfolderpath  + File.separator + subfolder;

				if (!isInList(subfolderpath,psorted)) {
		            		//FTA DEBUG System.out.println("Subfolder *"+ subfolder +"* has been created!");
					//FTA DEBUG System.out.println("Node at position " + nodepos + ": " + subfolder + "(folder!)");
					psorted.add(subfolderpath);
					folderspos.add(nodepos);
					//Generates node
    					_nodes[nodepos] = new TreeCheckBox(subfolder); // Not saved
			    		nodepos++; // moves the tree one level up	
				}

				
			
			} // end of this contains subfolders
		
	}//end of while subfolders
	/*
	while (nodename.indexOf(File.separator) > -1) { // There's a subfolder
		//if (nodename.indexOf(File.separator) == 0) // contains anothersubfolder
		//	nodename = nodename.substring(nodename.indexOf(File.separator)+1);
		//System.out.print("Posicion del separador: " + nodename.indexOf(File.separator));
		subfolder = nodename.substring(0,nodename.indexOf(File.separator));	
		nodename = nodename.replaceFirst(subfolder + File.separator,""); // get deep inside the subfolders
		//System.out.println("Subfolder: *"+ subfolder +"* Ruta Subfolders: *" + nodename + "*");

		if (!subfolder.equals("") && nodename.indexOf(File.separator) > -1) { // if the file is inside a subfolder then add the subfolder to the tree. > -1 checks is not a folder it's a file
			//System.out.print("Creating subfolder "+ subfolder +"...");
			//System.out.println(" ¿Fue creada antes?: " + pathscreated.contains(subfolderpath));	
			subfolderpath = subfolderpath + subfolder + File.separator;
			

			//if (!pathscreated.contains(subfolderpath)) {
			if (!isInList(subfolderpath,pathscreated)) {
		            System.out.println("Subfolder *"+ subfolder +"* has been created!");
			    _nodes[nodepos] = new TreeCheckBox(subfolder);
			    nodepos++; // moves the tree one level up	
			    pathscreated.add(subfolderpath);
			}
			//nodes[pathpos].add(nodes[nodepos]);
			
			pathpos++; // mueve la posicion***
			
		}
		// Agrego nodo
		//items[i].indexOf(File.separator)
	} // end of while subfolder
	*/
	subfolderpath="";

	//System.out.println("Nodes[" + nodepos + "] = TreeCheckBox("+ nodename +")");
        //System.out.println("Nodes[" + nodepos + "].setFilePath(items["+ k +"]) Value = " + items[k]);
        //System.out.println("Nodes[" + nodepos + "].setFileSize(items_size["+ k +"]) Value = " + items_size[k]);
	//FTA DEBUG System.out.println("Node at position " + nodepos + ": " + nodename + "(file!)");
	_nodes[nodepos] = new TreeCheckBox(nodename); // Here i should grab the full path
	_nodes[nodepos].setFilePath(items[k]); // Saves the path for this file
	_nodes[nodepos].setFileSize(items_size[k]); // Saves the size for this file
	//System.out.println("GetFileSize=" + _nodes[nodepos].getFileSize());
	//items_data.add(new com.limegroup.bittorrent.BTData.BTFileData(items_size[pos], items[k]));
	//System.out.println("Node position: " + nodepos);
	//nodes[pathpos].add(nodes[nodepos]);
	nodepos++;
	//subfolder=""; // return to the root
	
    }



nodepos=1;



//System.out.println("We have this number of nodes minus 1: " + items.length);

//System.out.println("-----*-*-*-*-**********--------NODES");
//System.out.println("Nodo cero: " + _nodes[0] + " Ruta Full: " + _nodes[0].getFilePath());
//System.out.println("Nodo uno: " + _nodes[1] + " Ruta Full: " + _nodes[1].getFilePath());
//System.out.println("Nodo dos: " + _nodes[2] + " Ruta Full: " + _nodes[2].getFilePath());
//System.out.println("Nodo tres: " + _nodes[3] + " Ruta Full: " + _nodes[3].getFilePath());


for (int it=1;it<itemstotal;it++) { 
	if (!folderspos.contains(it)) { // If the current node is not a folder, then proceed
		if (psorted != null)
			parentpos=getParentNode(_nodes[it],psorted,folderspos);
		else
			parentpos=0;

		if (parentpos !=-1) {
			//System.out.println("Padre: "+ parentpos + " le agrego en posicion: " + it);
			_nodes[parentpos].add(_nodes[it]);
			//System.out.println(_nodes[parentpos] + " le agrego el hijo archivo: " + _nodes[it].getFilePath());
		}
		else {
			_nodes[0].add(_nodes[it]);	
		}
	//_nodes[parentpos].add(folderNode);
	//folderNode = new TreeCheckBox(subfolder);
	//System.out.println("Padre: " + _nodes[parentpos] + " le agrego el child: " + subfolder + " en nodo se llama: " + folderNode);
	//_nodes[parentpos].add(folderNode);
	} else{ // Vinculo carpetas
	System.out.println("Es una carpeta posicion: " + it);
	_nodes[it-1].add(_nodes[it]);
	
	}


}


/*
// it works but just for one folder or single files
 for (int f=0;f<items.length-1;f++) { // works for single file for (int f=0;f<items.length-1;f++) {
	nodename = items[f].substring(items[f].indexOf(File.separator)+1);
	System.out.println("--------\nNode name: " + nodename);

	while (nodename.indexOf(File.separator) > -1) { // There's a subfolder
		subfolder = nodename.substring(0,nodename.indexOf(File.separator));	
		nodename = nodename.replaceFirst(subfolder + File.separator,""); // get deep inside the subfolders
	
		if (!subfolder.equals("")) { // if the file is inside a subfolder then add the subfolder to the tree. > -1 checks is not a folder it's a file
			System.out.println("Subcarpeta detectada: " + subfolder);
			//nodepos++; // moves the tree one level up
			folderNode = new TreeCheckBox(subfolder);
			System.out.println("Padre: " + _nodes[parentpos] + " le agrego el child: " + subfolder + " en nodo se llama: " + folderNode);
			_nodes[parentpos].add(folderNode);
			parentpos++; // mueve la posicion***

			//System.out.println("Nodes[" + parentpos + "].Add(nodes"+ nodepos +")");
			//System.out.println("Child a agregar: " + _nodes[nodepos]);
			//if (_nodes[nodepos] != null) // must check
			//	_nodes[parentpos].add(_nodes[nodepos]);
		}
		// Agrego nodo
		//items[i].indexOf(File.separator)
	}

	if (!subfolder.equals("")) // is coming a file inside a folder?
		nodepos++;

	System.out.println("Agrego hijo archivo en posicion "+ nodepos +":" + _nodes[nodepos] + " a padre que esta en la posicion " + parentpos);
	//System.out.println("Nodes[" + parentpos + "].Add(nodes["+ nodepos +"])");
	
	if (_nodes[parentpos] != null) // must see
		_nodes[parentpos].add(_nodes[nodepos]);
	//nodes[pathpos].add(nodes[nodepos]);
	nodepos++;
	//parentpos=0;
	subfolder="";
	//parentpos++; // return to the root
 } // end of it works single mode only
*/



//_nodes[0].add(nodes[1]);
//_nodes[1].add(nodes[2]);

//         /ruta/gfs/fernando.txt
/*
    _nodes[0].add(nodes[1]);
    _nodes[1].add(nodes[2]);
    _nodes[1].add(nodes[3]);
    _nodes[0].add(nodes[4]);
    _nodes[3].setSelected(true); // seleccion ala 3
*/
//
    _nodes[0].setSelected(true);
    // set Icon directly
    //
    _nodes[0].setIcon(MetalIconFactory.getFileChooserHomeFolderIcon());
    //WindowUtilities.setNativeLookAndFeel(); 
    try {
    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (Exception e) {
	System.out.println("BTFilesManager - Couldn't set the Windows Look & Feel: " + e);
	}
    JTree tree = new JTree( _nodes[0] );
    tree.putClientProperty("JTree.icons", createIcons());
    tree.setCellRenderer(new TreeRenderer());
    tree.getSelectionModel().setSelectionMode(
      TreeSelectionModel.SINGLE_TREE_SELECTION
    );
    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.addMouseListener(new NodeSelectionListener(tree));
    JScrollPane sp = new JScrollPane(tree);

    JTextArea textArea = new JTextArea(7,10); //3,10
    JButton cmdcancel = new JButton(I18n.tr("Cancel"));
    JButton button = new JButton(I18n.tr("OK"));
    button.addActionListener(
      new ButtonActionListener(_nodes[0], textArea));
    cmdcancel.addActionListener(
      new CancelAction());


    JPanel panel = new JPanel(new BorderLayout());    
    panel.add(button, BorderLayout.NORTH);
    panel.add(cmdcancel, BorderLayout.SOUTH);
    //panel.add(cmdtest, BorderLayout.SOUTH);
    
    getContentPane().add(sp,    BorderLayout.CENTER);
    getContentPane().add(panel, BorderLayout.EAST);
    //getContentPane().add(textPanel, BorderLayout.SOUTH);
  }
  private boolean isInList(String elem,Set<String> elements) {
	       for(String currelem : elements) {	

		if (currelem.equals(elem))
			return true;
	       }
  return false;
  }

  private boolean isInList(String elem,List<String> elements) {
   // TODO: Should convert "List" to "Set" and then call the analog function
   // instead of write the same code. List keep the folders sorted. Set is not.
	       for(String currelem : elements) {	

		if (currelem.equals(elem))
			return true;
	       }
  return false;
  }

  private Integer getParentNode(TreeCheckBox childNode,List<String> parentNodes,List<Integer> folderspos) {
	String childFile = childNode.getFilePath();
	if (childFile.lastIndexOf(File.separator) == -1)
		return 0; // single file
	childFile = childFile.substring(childFile.lastIndexOf(File.separator));
	String childPath = childNode.getFilePath().replace(childFile,"");
	
 	for(int t=1;t<parentNodes.size();t++) {
		if (childPath.equals(parentNodes.get(t))) {
			//System.out.println("Parent path is at position "+ t +": " + parentNodes.get(t) );
			return folderspos.get(t);
		}		
	       }
 	return -1;
  }

  public String greeting() {
    return "FTA: *Exit status*\nHi what's up!";
  }

 private Hashtable<?, ?> createIcons() {
    Hashtable<Object, Object> icons = new Hashtable<Object, Object>();
    icons.put("zip",MetalIconFactory.getTreeFloppyDriveIcon());
    //icons.put("c"    ,TextIcons.getIcon("c"));
    return icons;
  }



  class NodeSelectionListener extends MouseAdapter {
    JTree tree;
    
    NodeSelectionListener(JTree tree) {
      this.tree = tree;
    }
    
    public void mouseClicked(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      int row = tree.getRowForLocation(x, y);
      TreePath  path = tree.getPathForRow(row);
      //TreePath  path = tree.getSelectionPath();
      if (path != null) {
        TreeCheckBox node = (TreeCheckBox)path.getLastPathComponent();
        boolean isSelected = ! (node.isSelected());
        node.setSelected(isSelected);
        if (node.getSelectionMode() == TreeCheckBox.DIG_IN_SELECTION) {
          if ( isSelected ) {
            tree.expandPath(path);
          } else {
            tree.collapsePath(path);
          }
        }
        ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
        // I need revalidate if node is root.  but why?
        if (row == 0) {
          tree.revalidate();
          tree.repaint();
        }
      }
    }
  }
  

  class CancelAction extends AbstractAction {
            
            /**
     * 
     */
    private static final long serialVersionUID = -6175209379399098736L;

            public CancelAction() {
                super(I18n.tr("Cancel"));
            }
            
            public void actionPerformed(ActionEvent a) {
                GUIUtils.getDisposeAction().actionPerformed(a);
            }
  }

  class ButtonActionListener implements ActionListener {
    TreeCheckBox root;
    JTextArea textArea;
    
    ButtonActionListener(final TreeCheckBox root,
                         final JTextArea textArea) {
      this.root     = root;
      this.textArea = textArea;
    }
    
    public void actionPerformed(ActionEvent e) {
      Enumeration<?> items = root.breadthFirstEnumeration();
      // Since selectedfiles is null i can't add any item, so I've copied the same structure contained in files and then i cleaned this up. 
      // This way i'm not creating any new object and i'll have an empty list with the same structure.
      _selectedfiles = _files;
      _selectedfiles.clear();

      while (items.hasMoreElements()) {
        TreeCheckBox node = (TreeCheckBox)items.nextElement();
        if (node.isSelected()) {
          TreeNode[] nodes = node.getPath();
          textArea.append("\n" + nodes[0].toString());
          for (int i=1;i<nodes.length;i++) {
            textArea.append("/" + nodes[i].toString());
            textArea.append(node.getFileSize().toString() + " bytes");
	    //System.out.println("NODE NAME IS "+ node.getFilePath() +"SIZE IS: " + node.getFileSize());
	    //_selectedfiles[0]=new com.limegroup.bittorrent.BTData.BTFileData(node.getFileSize(), node.getFilePath());
	    _selectedfiles.add(new com.limegroup.bittorrent.BTData.BTFileData(node.getFileSize(), node.getFilePath()));
            //System.out.println("Output Nodes["+ i +"]: "+ nodes[i].toString() + "Bytes: " + _nodes[pos].getFileSize());
          }
        }
      } // end while
      if (_selectedfiles.size() == 0) {
	com.limegroup.gnutella.gui.GUIMediator.showMessage("Please, select at least one file to download"); //FTA: debug
	_selectedfiles = null;
	return;
      }
	
      setVisible(false);
      GUIUtils.getDisposeAction();
    }
  }

  // Console testing code
  // *Debug purposes only*
  public static void main(String args[]) {
    BTFilesManager frame = new BTFilesManager();
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    frame.setSize(500, 200);
    frame.setVisible(true);
  }
}
