package com.limegroup.gnutella.gui.wizard;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;

/**
 * This class provides a generic wizard. It manages {@link WizardPage}
 * objects which are displayed in a dialog.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public class Wizard {	

	/**
	 * The minimum width of the window.
	 */
	public static final int DIALOG_WIDTH = 540;

	/**
	 * The minimum height of the window.
	 */
	public static final int DIALOG_HEIGHT = 360;

	/**
	 * the dialog window that holds all other gui elements for the setup.
	 */
	protected JDialog dialog;

	/** 
	 * the holder for the setup windows 
	 */
	private WizardPagePanel pageContainer = new WizardPagePanel();

	/**
	 * holder for the current setup window.
	 */
	private WizardPage currentPage;

	public static final int ACTION_PREVIOUS = 1;
	
	public static final int ACTION_NEXT = 2;
	
	public static final int ACTION_FINISH = 4;
	
	public static final int ACTION_CANCEL = 8;
	
	private PreviousAction previousAction = new PreviousAction();
	
	private NextAction nextAction = new NextAction();
	
	private FinishAction finishAction = new FinishAction();
	
	private CancelAction cancelAction = new CancelAction();
	
	private AbstractAction[] actions = new AbstractAction[] {
			previousAction, nextAction, finishAction, cancelAction
	};
	
	public Wizard() {
    }
    
    public void addPage(WizardPage page) {
    	page.setWizard(this);
    	page.createPage();
    	pageContainer.add(page);
    }
    
    /*
	 * Creates the main <tt>JDialog</tt> instance and
	 * creates all of the setup window classes, buttons, etc.
	 */
	public JDialog createDialog(Frame parent) {
		dialog = new JDialog(parent);
		dialog.setModal(true);
	   
		// JDialog sizing seems to work differently with some Unix
		// systems, so we'll just make it resizable.
		if(!OSUtils.isUnix())
		    dialog.setResizable(false);
		
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                performCancel();
            }
        });
        GUIUtils.addHideAction((JComponent)dialog.getContentPane());

		// set the layout of the content pane
		Container container = dialog.getContentPane();
		BoxLayout containerLayout = new BoxLayout(container, BoxLayout.Y_AXIS);
        container.setLayout(containerLayout);

		// create the main panel
		JPanel setupPanel = new JPanel();
		setupPanel.setBorder(new EmptyBorder(1, 1, 0, 0));
		BoxLayout layout = new BoxLayout(setupPanel, BoxLayout.Y_AXIS);
		setupPanel.setLayout(layout);

    	// create the setup buttons panel
		setupPanel.add(pageContainer);
		setupPanel.add(Box.createVerticalStrut(10));
		ButtonRow buttons = new ButtonRow(actions, ButtonRow.X_AXIS, ButtonRow.LEFT_GLUE);
		buttons.setBorder(new EmptyBorder(5, 5, 5, 5));
		setupPanel.add(buttons);
		
		if (pageContainer.getFirst() != null) {
			show(pageContainer.getFirst());
		}

		// add the panel and make it visible		
		container.add(setupPanel);

		int width = Math.max(((JComponent)container).getPreferredSize().width, DIALOG_WIDTH);
		int height = Math.max(((JComponent)container).getPreferredSize().height, DIALOG_HEIGHT);
		((JComponent)container).setPreferredSize(new Dimension(width, height));
		dialog.pack();
		
		return dialog;
	}
   
	/**
	 * Enables the bitmask of specified actions, the other actions are explicitly
	 * disabled.
	 * <p>
	 * To enable finish and previous you would call
	 * {@link #enableActions(int) enableActions(SetupManager.ACTION_FINISH|SetupManager.ACTION_PREVIOUS)}.
	 * @param actions
	 */
	public void enableActions(int actions) {
		previousAction.setEnabled((actions & ACTION_PREVIOUS) != 0);
		nextAction.setEnabled((actions & ACTION_NEXT) != 0);
		finishAction.setEnabled((actions & ACTION_FINISH) != 0);
		cancelAction.setEnabled((actions & ACTION_CANCEL) != 0);
	}
	
	public int getEnabledActions() {
		int actions = 0;
		if (previousAction.isEnabled()) {
			actions |= ACTION_PREVIOUS;
		}
		if (nextAction.isEnabled()) {
			actions |= ACTION_NEXT;
		}
		if (finishAction.isEnabled()) {
			actions |= ACTION_FINISH;
		}
		if (cancelAction.isEnabled()) {
			actions |= ACTION_CANCEL;
		}
		return actions;
	}
	
	public WizardPage getNextPage(WizardPage page) {
		return pageContainer.getNext(page);
	}

	public WizardPage getPreviousPage(WizardPage page) {
		return pageContainer.getPrevious(page);
	}

	/**
	 * Displays the next window in the setup sequence.
	 */
	public void performNext() {
		WizardPage page = currentPage.getNext();
		show(page);
	}

	
	/**
	 * Displays the previous window in the setup sequence.
	 */
	public void performPrevious() {
		WizardPage page = currentPage.getPrevious();
		show(page);
	}
	

	/**
	 * Cancels the setup.
	 */
	public void performCancel() {
		dialog.dispose();
	}

	
	/**
	 * Completes the setup.
	 */
	public void performFinish() {		
		dialog.dispose();
	}
	
	/**
	 * Show the specified page.
	 */
	private void show(WizardPage page) {  
		pageContainer.show(page.getKey());
		currentPage = page;
		page.pageShown();
	}
	
	/**
	 * Updates the buttons according to the status of the currently visible
	 * page.
	 */
	public void updateButtons() {
		if (currentPage == null) {
			finishAction.setEnabled(false);
			nextAction.setEnabled(false);
			previousAction.setEnabled(false);			
		} else {
			boolean complete = currentPage.isPageComplete();
			boolean canFlipToNext = currentPage.canFlipToNextPage();
			finishAction.setEnabled(complete && !canFlipToNext);
			nextAction.setEnabled(complete && canFlipToNext);
			previousAction.setEnabled(currentPage.getPrevious() != null);
		}
	}

	private class CancelAction extends AbstractAction {

		public CancelAction() {
			putValue(Action.NAME, I18n.tr("Cancel"));
		}

		public void actionPerformed(ActionEvent e) {
			performCancel();
		}
	}
	
	private class NextAction extends AbstractAction {
		
		public NextAction() {
			putValue(Action.NAME, I18n.tr("Next >>"));
		}

		public void actionPerformed(ActionEvent e) {
			performNext();
		}
	}
	
	private class PreviousAction extends AbstractAction {

		public PreviousAction() {
			putValue(Action.NAME, I18n.tr("<< Back"));
		}
		
		public void actionPerformed(ActionEvent e) {
			performPrevious();
		}
	}
	
	private class FinishAction extends AbstractAction {
		
		public FinishAction() {
			putValue(Action.NAME, I18n.tr("Finish"));
		}

		public void actionPerformed(ActionEvent e) {
			performFinish();
		}
		
	}

	/**
	 * This class serves two purposes.  First, it is a JPanel that
	 * contains the body of a LimeWire setup window.  Second, it 
	 * serves as a proxy for the underlying SetupWindow object that
	 * that handles the actual drawing.
	 */
	private class WizardPagePanel extends JPanel {

		/**
		 * The <tt>CardLayout</tt> instance for the setup windows.
		 */
		private final CardLayout CARD_LAYOUT = new CardLayout();

		/**
		 * Sets the <tt>CardLayout</tt> for the setup windows.
		 */
		WizardPagePanel() {
			setLayout(CARD_LAYOUT);	   
		}

		/**
		 * Adds the speficied window to the CardLayout based on its title.
		 *
		 * @param window the <tt>SetupWindow</tt> to add
		 */
		void add(WizardPage page) {
			add(page, page.getKey());
		}

		public WizardPage getFirst() {
			if (getComponentCount() > 0) {
				return (WizardPage) getComponent(0);
			} else {
				return null;
			}
		}

		public WizardPage getLast() {
			if (getComponentCount() > 0) {
				return (WizardPage) getComponent(getComponentCount() - 1);
			} else {
				return null;
			}
		}
		
		public WizardPage getNext(WizardPage page) {
			Component[] pages = getComponents();
			for (int i = 0; i < pages.length; i++) {
				if (pages[i] == page && i < pages.length - 1) {
					return (WizardPage) pages[i + 1];
				} 
			}
			return null;
		}

		public WizardPage getPrevious(WizardPage page) {
			Component[] pages = getComponents();
			for (int i = 0; i < pages.length; i++) {
				if (pages[i] == page && i > 0) {
					return (WizardPage) pages[i - 1];
				} 
			}
			return null;
		}

		/**
		 * Shows the window speficied by its title.
		 * 
		 * @param key the unique key of the <tt>Component</tt> to show
		 */
		void show(String key) {
			CARD_LAYOUT.show(this, key);
		}
		
	}

}
