package de.savemytube.prog.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.swing.JDialog;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

import de.http.ProxyDefintion;

public class FrmProxy extends JDialog {

    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JPanel pnlMain = null;
    private JPanel pnlButtons = null;
    private JButton btnOk = null;
    private JLabel lbl = null;
    private JTextField txtProxy = null;
    private JTextField txtPort = null;
    private JLabel lblPort = null;
    private JLabel lblUser = null;
    private JTextField txtUser = null;
    private JLabel lblPassword = null;
    private JPasswordField txtPassword = null;
    private ProxyDefintion proxyDef = null;

    public ProxyDefintion getProxyDef() {
        return proxyDef;
    }

    public void setProxyDef(ProxyDefintion proxyDef) {
        this.proxyDef = proxyDef;
    }

    /**
     * @param owner
     */
    public FrmProxy(Frame owner) {
        super(owner);
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(339, 185);
        this.setResizable(false);
        this.setModal(true);
        this.setTitle("Proxysettings");
        this.setContentPane(getJContentPane());
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
            jContentPane.add(getPnlButtons(), BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    /**
     * This method initializes pnlMain  
     *  
     * @return javax.swing.JPanel   
     */
    private JPanel getPnlMain() {
        if (pnlMain == null) {
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints8.gridy = 3;
            gridBagConstraints8.weightx = 1.0;
            gridBagConstraints8.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints8.gridx = 1;
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 0;
            gridBagConstraints7.gridy = 3;
            lblPassword = new JLabel();
            lblPassword.setText("Password");
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints6.gridy = 2;
            gridBagConstraints6.weightx = 1.0;
            gridBagConstraints6.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints6.gridx = 1;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.anchor = GridBagConstraints.EAST;
            gridBagConstraints5.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints5.gridy = 2;
            lblUser = new JLabel();
            lblUser.setText("User");
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.anchor = GridBagConstraints.EAST;
            gridBagConstraints4.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints4.gridy = 1;
            lblPort = new JLabel();
            lblPort.setText("Port");
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.gridy = 1;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints3.gridx = 1;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.gridy = 0;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints2.gridx = 1;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints1.anchor = GridBagConstraints.EAST;
            gridBagConstraints1.gridy = 0;
            lbl = new JLabel();
            lbl.setText("Proxy");
            pnlMain = new JPanel();
            pnlMain.setLayout(new GridBagLayout());
            pnlMain.add(lbl, gridBagConstraints1);
            pnlMain.add(getTxtProxy(), gridBagConstraints2);
            pnlMain.add(getTxtPort(), gridBagConstraints3);
            pnlMain.add(lblPort, gridBagConstraints4);
            pnlMain.add(lblUser, gridBagConstraints5);
            pnlMain.add(getTxtUser(), gridBagConstraints6);
            pnlMain.add(lblPassword, gridBagConstraints7);
            pnlMain.add(getTxtPassword(), gridBagConstraints8);
        }
        return pnlMain;
    }

    /**
     * This method initializes pnlButtons   
     *  
     * @return javax.swing.JPanel   
     */
    private JPanel getPnlButtons() {
        if (pnlButtons == null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            pnlButtons = new JPanel();
            pnlButtons.setLayout(new GridBagLayout());
            pnlButtons.add(getBtnOk(), gridBagConstraints);
        }
        return pnlButtons;
    }

    /**
     * This method initializes btnOk    
     *  
     * @return javax.swing.JButton  
     */
    private JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton();
            btnOk.setText("Ok");
            btnOk.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    closeDialog();
                }
            });
        }
        return btnOk;
    }

    
    private void closeDialog() {
        
        getProxyDef().setProxy(getTxtProxy().getText());
        getProxyDef().setPort(getTxtPort().getText());
        getProxyDef().setUser(getTxtUser().getText());
        getProxyDef().setPassword(getTxtPassword().getText());        
        setVisible(false);        
    }
    /**
     * This method initializes txtProxy 
     *  
     * @return javax.swing.JTextField   
     */
    private JTextField getTxtProxy() {
        if (txtProxy == null) {
            txtProxy = new JTextField();
        }
        return txtProxy;
    }

    /**
     * This method initializes txtPort  
     *  
     * @return javax.swing.JTextField   
     */
    private JTextField getTxtPort() {
        if (txtPort == null) {
            txtPort = new JTextField();
        }
        return txtPort;
    }

    /**
     * This method initializes txtUser  
     *  
     * @return javax.swing.JTextField   
     */
    private JTextField getTxtUser() {
        if (txtUser == null) {
            txtUser = new JTextField();
        }
        return txtUser;
    }

    /**
     * This method initializes txtPassword  
     *  
     * @return javax.swing.JPasswordField   
     */
    private JPasswordField getTxtPassword() {
        if (txtPassword == null) {
            txtPassword = new JPasswordField();
        }
        return txtPassword;
    }
    
    public void show(ProxyDefintion pdef) {        
        getTxtProxy().setText(pdef.getProxy());
        getTxtPort().setText(pdef.getPort());
        getTxtUser().setText(pdef.getUser());
        getTxtPassword().setText(pdef.getPassword());  
        setProxyDef(pdef);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        centerScreen();
        setVisible(true);
    }
    
    public void centerScreen() {
          Dimension dim = getToolkit().getScreenSize();
          Rectangle abounds = getBounds();
          setLocation((dim.width - abounds.width) / 2,
              (dim.height - abounds.height) / 2);
          
          requestFocus();
        }

}  //  @jve:decl-index=0:visual-constraint="175,52"
