/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.bittorrent;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;

import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.torrent.PaymentOptions;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LimeTextField;

public class PaymentOptionsPanel extends JPanel {

    //"bitcoin :14F6JPXK2fR5b4gZp3134qLRGgYtvabMWL", 
    //"litecoin:LiYp3Dg11N5BgV8qKW42ubSZXFmjDByjoV",
    private final LimeTextField bitcoinAddress;
    private final LimeTextField litecoinAddress;
    private final LimeTextField dogecoinAddress;
    private final LimeTextField paypalUrlAddress;
    

    public PaymentOptionsPanel() {
        initBorder();
        bitcoinAddress = new LimeTextField();
        litecoinAddress = new LimeTextField();
        dogecoinAddress = new LimeTextField();
        paypalUrlAddress = new LimeTextField();
        
        setLayout(new MigLayout("fill"));
        initComponents();
        initListeners();
    }

    private void initListeners() {
        
        bitcoinAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                onCryptoAddressPressed(bitcoinAddress);
            }
        });
        litecoinAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                onCryptoAddressPressed(litecoinAddress);
            }
        });
        dogecoinAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                onCryptoAddressPressed(dogecoinAddress);
            }
        });
    }

    protected void onCryptoAddressPressed(LimeTextField textField) {
        boolean hasValidPrefixOrNoPrefix = false;
        String prefix = "";
        if (textField.equals(bitcoinAddress)) {
            prefix = "bitcoin:";
        } else if (textField.equals(litecoinAddress)) {
            prefix = "litecoin:";
        } else if (textField.equals(dogecoinAddress)) {
            prefix = "dogecoin:";
        }
        
        hasValidPrefixOrNoPrefix = hasValidPrefixOrNoPrefix(prefix, textField);
        
        if (!hasValidAddress(prefix, textField) || !hasValidPrefixOrNoPrefix) {
            textField.setForeground(Color.red);
        } else {
            textField.setForeground(Color.black);
        }
        
        int caretPosition = textField.getCaretPosition();
        int lengthBefore = textField.getText().length();
        textField.setText(textField.getText().replaceAll(" ", ""));
        int lengthAfter = textField.getText().length();
        if (lengthAfter < lengthBefore) {
            caretPosition -= (lengthBefore - lengthAfter);
        }
        textField.setCaretPosition(caretPosition);

    }

    private boolean hasValidPrefixOrNoPrefix(String prefix, LimeTextField textField) {
        boolean hasPrefix = false;
        boolean hasValidPrefix = false;
        String text = textField.getText();
        
        if (text.contains(":")) {
            hasPrefix = true;
            hasValidPrefix = text.startsWith(prefix);
        } else {
            hasPrefix = false;
        }
        
        return (hasPrefix && hasValidPrefix) || !hasPrefix;
    }

    private void initComponents() {
        add(new JLabel("<html><strong>Bitcoin</strong> wallet address</html>"),"wrap, span");
        add(new JLabel(GUIMediator.getThemeImage("bitcoin_accepted.png")),"aligny top");
        add(bitcoinAddress,"aligny top, growx, push, wrap");

        
        add(new JLabel("<html><strong>Litecoin</strong> wallet address</html>"),"wrap, span");
        add(new JLabel(GUIMediator.getThemeImage("litecoin_accepted.png")),"aligny top");
        add(litecoinAddress, "aligny top, growx, push, wrap");

        add(new JLabel("<html><strong>Dogecoin</strong> wallet address</html>"),"wrap, span");
        add(new JLabel(GUIMediator.getThemeImage("dogecoin_accepted.png")),"aligny top");
        add(dogecoinAddress, "aligny top, growx, push, wrap");

        add(new JLabel("<html><strong>Paypal</strong> payment/donation page url</html>"),"wrap, span");
        add(new JLabel(GUIMediator.getThemeImage("paypal_accepted.png")), "aligny top");
        add(paypalUrlAddress, "aligny top, growx, push");
    }

    private void initBorder() {
        Border titleBorder = BorderFactory.createTitledBorder(I18n
                .tr("\"Name your price\", \"Tips\", \"Donations\" payment options"));
        Border lineBorder = BorderFactory.createLineBorder(ThemeMediator.LIGHT_BORDER_COLOR);
        Border border = BorderFactory.createCompoundBorder(lineBorder, titleBorder);
        setBorder(border);
    }
    
    public PaymentOptions getPaymentOptions() {
        PaymentOptions result = null;

        boolean validBitcoin = hasValidAddress("bitcoin:", bitcoinAddress);
        boolean validLitecoin = hasValidAddress("litecoin:", litecoinAddress);
        boolean validDogecoin = hasValidAddress("dogecoin:", dogecoinAddress);
            
        if (validBitcoin || validLitecoin || validDogecoin || (paypalUrlAddress.getText()!=null && !paypalUrlAddress.getText().isEmpty())) {
            String bitcoin = validBitcoin ? normalizeValidAddress("bitcoin:", bitcoinAddress.getText().trim()) : null;
            String litecoin = validLitecoin ? normalizeValidAddress("litecoin:", litecoinAddress.getText().trim()) : null;
            String dogecoin = validDogecoin ? normalizeValidAddress("dogecoin:", dogecoinAddress.getText().trim()) : null;
            
            result = new PaymentOptions(bitcoin,litecoin,dogecoin,paypalUrlAddress.getText());
        }
        
        return result;
    }
    
    private String normalizeValidAddress(String prefix, String validAddress) {
        String result = validAddress;
        if (!validAddress.startsWith(prefix)) {
            result = prefix + validAddress;
        }
        return result;
    }

    private boolean hasValidAddress(String optionalPrefix, LimeTextField textField) {
        boolean result = false;
        String text = textField.getText().trim();
        if (text != null && !text.isEmpty()) {
            text = text.replaceAll(optionalPrefix, "");
            char firstChar = text.charAt(0);
            
            boolean validFirstChar = false;
            if (optionalPrefix.equals("bitcoin:")) {
                validFirstChar = (firstChar == '1' || firstChar == '3');
            } else if (optionalPrefix.equals("litecoin:")) {
                validFirstChar = firstChar == 'L';
            } else if (optionalPrefix.equals("dogecoin:")) {
                validFirstChar = firstChar == 'D';
            }

            System.out.println("text ["+text+"] - " + text.length() + " - valid 1st char? " + validFirstChar);
            
            result = (26 <= text.length() && text.length() <= 34) && validFirstChar;
        }
        return result;
    }
    
    public boolean hasPaymentOptions() {
        return false;
    }
}