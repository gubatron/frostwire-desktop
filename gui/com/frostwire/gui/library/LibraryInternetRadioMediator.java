package com.frostwire.gui.library;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.AudioSource;
import com.limegroup.gnutella.gui.RefreshListener;
import com.limegroup.gnutella.gui.options.OptionsMediator;
import com.limegroup.gnutella.gui.options.OptionsTreeNode;
import com.limegroup.gnutella.gui.options.panes.AbstractPaneItem;
import com.limegroup.gnutella.gui.tables.DefaultMouseListener;
import com.limegroup.gnutella.gui.tables.MouseObserver;
import com.limegroup.gnutella.gui.trees.FilteredTreeModel;

public class LibraryInternetRadioMediator implements RefreshListener, MouseObserver {

    private final JScrollPane scrollPane;

    private final JTree tree;

    private final InternetRadioTreeModel treeModel = new InternetRadioTreeModel();

    private final FilteredTreeModel filteredTreeModel = new FilteredTreeModel(treeModel, true);

    private static LibraryInternetRadioMediator INSTANCE;

    public static LibraryInternetRadioMediator instance() {
        if (INSTANCE == null) {
            INSTANCE = new LibraryInternetRadioMediator();
        }

        return INSTANCE;
    }

    LibraryInternetRadioMediator() {
        tree = new JTree();
        tree.setEditable(false);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.putClientProperty("JTree.lineStyle", "None");
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //tree.addTreeSelectionListener(new InternetRadioTreeSelectionListener(tree));
        //tree.setCellRenderer(new LimeTreeCellRenderer());
        tree.addMouseListener(new DefaultMouseListener(this));

        tree.setModel(filteredTreeModel);

        scrollPane = new JScrollPane(tree);
        //scrollPane.getViewport().setBackground(Color.white);
        scrollPane.setPreferredSize(new Dimension(125, 2000));
        scrollPane.setMinimumSize(new Dimension(125, 300));
    }

    final class InternetRadioTreeModel extends DefaultTreeModel {

        /**
         * Constant handle to the root node of the tree.
         */
        private InternetRadioTreeNode ROOT = null;

        /**
         * The constructor constructs the <tt>MutableTreeNode</tt> instances
         * as well as the <tt>TreeModel</tt>.
         */
        InternetRadioTreeModel() {
            super(null);
            ROOT = new InternetRadioTreeNode("INTERNET_RADIO_ROOT_NODE", "", null);
            setRoot(ROOT);

            InternetRadioTreeNode cat1 = new InternetRadioTreeNode("Cat1", "Cat1", null);
            InternetRadioTreeNode radio1 = new InternetRadioTreeNode("Mis Baladas", "Mis Baladas", "http://173.192.58.37:8100");
            InternetRadioTreeNode cat2 = new InternetRadioTreeNode("Cat2", "Cat2", null);
            InternetRadioTreeNode radio2 = new InternetRadioTreeNode("Latino FM", "Latino FM", "http://92.48.107.35:8000");

            cat1.add(radio1);
            cat2.add(radio2);

            ROOT.add(cat1);
            ROOT.add(cat2);
        }

        /**
         * Adds a new <tt>OptionsTreeNode</tt> to one of the root node's
         * children.  This should only be called during tree construction.
         * The first key cannot denote the root.
         *
         * @param parentKey the unique identifying key of the node to add as
         *                  well as the key for the locale-specific name for
         *                  the node as it appears to the user
         *
         * @param key the unique identifying key of the node to add as well as
         *            the key for the locale-specific name for the node as it
         *            appears to the user
         *
         * @param displayName the name of the node as it is displayed to the
         *                    user
         * @param keywords search keywords associated with this node
         * @return the created node
         * @throws IllegalArgumentException if the parentKey does not
         *                                  correspond to any top-level node
         *                                  in the tree
         */
        final InternetRadioTreeNode addNode(final String parentKey, final String key, final String displayName, String url) {
            InternetRadioTreeNode newNode = new InternetRadioTreeNode(key, displayName, url);
            MutableTreeNode parentNode;

            if (parentKey == OptionsMediator.ROOT_NODE_KEY) {
                parentNode = ROOT;
            } else {
                try {
                    parentNode = getParentNode(ROOT, parentKey);
                } catch (IOException ioe) {
                    //the parent node could not be found, so return
                    return null;
                }
                if (parentNode == null)
                    return null;
            }

            // insert the new node
            insertNodeInto(newNode, parentNode, parentNode.getChildCount());
            reload(parentNode);

            return newNode;
        }

        /**
         * This method performs a recursive depth-first search for the
         * parent node with the specified key.
         *
         * @param node the current node to search through
         * @param parentKey the key that will match the key of the parent node
         *                  we are searching for
         * @return the <tt>MutableTreeNode</tt> instance corresponding to
         *         the specified key, or <tt>null</tt> if it could not be found
         * @throws IOException if a corresponding key does not exist
         */
        private final MutableTreeNode getParentNode(MutableTreeNode node, final String parentKey) throws IOException {
            // note that we use the key to denote equality, as each node may
            // have the same visual name, but it will not have the same key
            for (int i = 0, length = node.getChildCount(); i < length; i++) {
                OptionsTreeNode curNode = (OptionsTreeNode) node.getChildAt(i);

                if (curNode.getTitleKey().equals((parentKey)))
                    return curNode;
                getParentNode(curNode, parentKey);

                if (curNode.isRoot() && i == (length - 1)) {
                    // this means we have looped through all of the nodes
                    // without finding the parent key, so throw an exception
                    String msg = "Parent node not in options tree.";
                    throw new IOException(msg);
                }
            }

            // this will never happen -- the exception should always be thrown
            return null;
        }

    }

    public class InternetRadioTreeNode extends DefaultMutableTreeNode {

        /**
         * The key for uniquely identifying this node.
         */
        private String _titleKey;

        /**
         * The name of this node as it is displayed to the user.
         */
        private String _displayName;

        private URL url;

        private Class<? extends AbstractPaneItem>[] clazzes;

        /**
         * This constructor sets the values for the name of the node to display 
         * to the user as well as the constant key to use for uniquely 
         * identifying this node.
         *
         * @param titleKey the key for the name of the node to display to the 
         *                 user and the unique identifier key for this node
         *
         * @param displayName the name of the node as it is displayed to the
         *                    user
         */
        InternetRadioTreeNode(final String titleKey, final String displayName, String url) {
            _titleKey = titleKey;
            _displayName = displayName;

            if (url != null) {
                try {
                    this.url = new URL(url);
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        /**
         * Defines the class' representation as a <tt>String</tt> object, used 
         * in determining how it is displayed in the <tt>JTree</tt>.
         *
         * @return the <tt>String</tt> identifier for the display of this class
         */
        public String toString() {
            return _displayName;
        }

        /**
         * Returns the <tt>String</tt> denoting both the title of the node
         * as well as the unique identifying <tt>String</tt> for the node.
         */
        public String getTitleKey() {
            return _titleKey;
        }

        public void setClasses(Class<? extends AbstractPaneItem>[] clazzes) {
            this.clazzes = clazzes;
        }

        public Class<? extends AbstractPaneItem>[] getClasses() {
            return clazzes;
        }

        public URL getURL() {
            return url;
        }

    }

    public Component getScrolledTablePane() {
        return scrollPane;
    }

    public void refresh() {

    }

    final class InternetRadioTreeSelectionListener implements TreeSelectionListener {

        /**
         * Handle to the <code>JTree</code> instance that utilizes this listener.
         */
        private JTree _tree;

        /**
         * Sets the <code>JTree</code> reference that utilizes this listener.
         *
         * @param tree the <code>JTree</code> instance that utilizes this listener
         */
        InternetRadioTreeSelectionListener(final JTree tree) {
            _tree = tree;
        }

        /**
         * Implements the <code>TreeSelectionListener</code> interface.
         * Takes any action necessary for responding to the selection of a 
         * node in the tree.
         *
         * @param e the <code>TreeSelectionEvent</code> object containing
         *          information about the selection
         */
        public void valueChanged(TreeSelectionEvent e) {
            Object obj = _tree.getLastSelectedPathComponent();
            if (obj instanceof InternetRadioTreeNode) {
                InternetRadioTreeNode node = (InternetRadioTreeNode) obj;

                // only leaf nodes have corresponding panes to display
                if (node.isLeaf()) {
                    System.out.println(node._displayName);
                } else {
                    _tree.expandPath(new TreePath(node.getPath()));
                    //OptionsMediator.instance().handleSelection((OptionsTreeNode) node.getFirstChild());
                }
            }
        }
    }

    @Override
    public void handleMouseClick(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleMouseDoubleClick(MouseEvent e) {
        Object obj = tree.getLastSelectedPathComponent();
        if (obj instanceof InternetRadioTreeNode) {
            InternetRadioTreeNode node = (InternetRadioTreeNode) obj;

            // only leaf nodes have corresponding panes to display
            if (node.isLeaf()) {
                AudioPlayer.instance().loadSong(new AudioSource(node.getURL()), true, false);
            }
        }
    }

    @Override
    public void handleRightMouseClick(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handlePopupMenu(MouseEvent e) {
        // TODO Auto-generated method stub

    }
}
