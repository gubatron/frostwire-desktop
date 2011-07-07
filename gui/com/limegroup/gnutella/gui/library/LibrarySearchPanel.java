package com.limegroup.gnutella.gui.library;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.Document;

import com.frostwire.gui.bittorrent.TorrentUtil;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.AutoCompleteTextField;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.search.SearchField;
import com.limegroup.gnutella.gui.search.SearchInformation;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.settings.SharingSettings;

/**
 * Panel that embeds the search bar for the library panel. 
 * 
 * Contains a label, a search field and a button horizontally aligned. The
 * button triggers a search action that searches the shared files for the
 * keywords in the text field and hands the search results to the
 * {@link LibrarySearchResultsHolder}.
 */
public class LibrarySearchPanel extends JPanel {

	/**
     * 
     */
    private static final long serialVersionUID = -8099451849619924981L;

    private AutoCompleteSearchField queryField = new AutoCompleteSearchField(40);
		        
	LibrarySearchPanel() {
		super(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 2, 0, 2);
		gbc.anchor = GridBagConstraints.WEST;
		JLabel label = new JLabel(I18n.tr("Search In Shared Files:") + " ");
		label.setLabelFor(queryField);
		label.setDisplayedMnemonic('S');
		add(label, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		add(queryField, gbc);
		Action a = new SearchLibraryAction();
		GUIUtils.bindKeyToAction(queryField, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), a);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		add(new JButton(a), gbc);
	}
        
	private class SearchLibraryAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = -2182314529781104010L;

        public SearchLibraryAction() {
			putValue(Action.NAME, I18n.tr("Search"));
		}
		
		public boolean validate(SearchInformation info) {
		    switch (SearchMediator.validateInfo(info)) {
		    case SearchMediator.QUERY_EMPTY:
		        return false;
	        case SearchMediator.QUERY_XML_TOO_LONG:
	            // cannot happen
	        case SearchMediator.QUERY_VALID:
	        default:
	            return true;
		    }
		}
		
		public void actionPerformed(ActionEvent e) {
			String query = queryField.getText().trim();
			if (query.length() == 0) {
				queryField.getToolkit().beep();
				return;
			}
			final SearchInformation info = SearchInformation.createKeywordSearch(query, null, MediaType.getAnyTypeMediaType());
			if (!validate(info)) {
			    return;
			}
			queryField.addToDictionary();
			BackgroundExecutorService.schedule(new SearchRunnable(query));
		}
		
	}
	
	private class AutoCompleteSearchField extends AutoCompleteTextField {
		
		/**
         * 
         */
        private static final long serialVersionUID = -1429993304589825874L;

        public AutoCompleteSearchField(int columns) {
			super(columns);
		}
		
		@Override
		protected Document createDefaultModel() {
			return new SearchField.SearchFieldDocument();
		}
	}
	
	private static final class SearchRunnable implements Runnable {
        
        private final String _query;
        
        public SearchRunnable(String query) {
            _query = query;
        }

        public void run() {
            
            GUIMediator.safeInvokeLater(new Runnable() {
                public void run() {
                    LibraryTree.instance().getSearchResultsHolder().setResults(new File[0]);
                    LibraryTree.instance().setSearchResultsNodeSelected();
                }
            });
            
            search(SharingSettings.TORRENTS_DIR_SETTING.getValue(), new HashSet<File>());
            File file = SharingSettings.TORRENT_DATA_DIR_SETTING.getValue();
            search(file, TorrentUtil.getIncompleteFiles());
        }
        
        /**
         * It searches _query in haystackDir.
         * 
         * @param haystackDir
         * @param excludeFiles - Usually a list of incomplete files.
         */
        private void search(File haystackDir, Set<File> excludeFiles) {
            
            if (!haystackDir.isDirectory()) {
                return;
            }
            
            final List<File> directories = new ArrayList<File>();
            final List<File> results = new ArrayList<File>();
            SearchFileFilter searchFilter = new SearchFileFilter(_query);
            
            for (File child : haystackDir.listFiles(searchFilter)) {

            	/////
            	//Stop search if the user selected another item in the library tree
                DirectoryHolder directoryHolder = LibraryTree.instance().getSelectedDirectoryHolder();
                LibrarySearchResultsHolder searchResultsHolder = LibraryTree.instance().getSearchResultsHolder();

                if (directoryHolder != null && !searchResultsHolder.equals(directoryHolder)) {
                    return;
                }
                /////
                
                if (excludeFiles.contains(child)) {
                    continue;
                }
                if (child.isHidden()) {
                    continue;
                }

                if (child.isDirectory()) {
                    directories.add(child);
                    
                    if (searchFilter.accept(child, false)) {
                    	results.add(child);
                    }
                    
                } else if (child.isFile()) {
                    results.add(child);
                }
            }
            
            Runnable r = new Runnable() {
                public void run() {
                    LibraryMediator.instance().addFilesToLibraryTable(results);
                }
            };
            GUIMediator.safeInvokeLater(r);
            
            for (File directory : directories) {
                search(directory, excludeFiles);
            }
        }
    }
	
	private static final class SearchFileFilter implements FileFilter {
	    
	    private final String[] _tokens;
	    
	    public SearchFileFilter(String query) {
	    	_tokens = query.toLowerCase(Locale.US).split(" ");
	    }
	    
	    public boolean accept (File pathname) {
	    	return accept(pathname, true);
	    }
	    
	    /**
	     * 
	     * @param pathname
	     * @param includeAllDirectories - if true, it will say TRUE to any directory
	     * @return
	     */
        public boolean accept(File pathname, boolean includeAllDirectories) {
        	
        	if (pathname.isDirectory() && includeAllDirectories) {
        		return true;
        	}
            
            String name = pathname.getName();
            
            for (String token : _tokens)  {
                if (!name.toLowerCase(Locale.US).contains(token)) {
                    return false;
                }
            }
            
            return true;
        }	    
	}
}