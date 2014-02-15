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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.torrent.PaymentOptions;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LimeTextField;

public class PaymentOptionsPanel extends JPanel {

    //"bitcoin :14F6JPXK2fR5b4gZp3134qLRGgYtvabMWL", 
    //"litecoin:LiYp3Dg11N5BgV8qKW42ubSZXFmjDByjoV",
    private final LimeTextField bitcoinAddress;
    private final LimeTextField litecoinAddress;
    private final LimeTextField dogecoinAddress;
    private final LimeTextField genericPaymentAddress;
    

    public PaymentOptionsPanel() {
        initBorder();
        bitcoinAddress = new LimeTextField();
        litecoinAddress = new LimeTextField();
        dogecoinAddress = new LimeTextField();
        genericPaymentAddress = new LimeTextField();
    }

    private void initBorder() {
        Border titleBorder = BorderFactory.createTitledBorder(I18n
                .tr("Name your price Payment Options"));
        Border lineBorder = BorderFactory.createLineBorder(ThemeMediator.LIGHT_BORDER_COLOR);
        Border border = BorderFactory.createCompoundBorder(lineBorder, titleBorder);
        setBorder(border);
    }
    
    public PaymentOptions getPaymentOptions() {
        PaymentOptions result = null;

        boolean validBitcoin = hasValidAddress("bitcoin:", bitcoinAddress);
        boolean validLitecoin = hasValidAddress("litecoin:", litecoinAddress);
        boolean validDogecoin = hasValidAddress("dogecoin:", dogecoinAddress);
            
        if (validBitcoin || validLitecoin || validDogecoin) {
            String bitcoin = validBitcoin ? normalizeValidAddress("bitcoin:", bitcoinAddress.getText().trim()) : null;
            String litecoin = validLitecoin ? normalizeValidAddress("litecoin:", litecoinAddress.getText().trim()) : null;
            String dogecoin = validDogecoin ? normalizeValidAddress("dogecoin:", dogecoinAddress.getText().trim()) : null;
            
            result = new PaymentOptions(bitcoin,litecoin,dogecoin,genericPaymentAddress.getText());
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
            
            boolean validBitcoin1stChar = optionalPrefix.equals("bitcoin:") && firstChar == '1' || firstChar == '3';
            boolean validLitecoin1stChar = optionalPrefix.equals("litecoin:") && firstChar == 'L';
            boolean validDogecoin1stChar = optionalPrefix.equals("dogecoin:") && firstChar == 'D';
            
            result = (27 <= text.length() && text.length() <= 34) &&
                     (validBitcoin1stChar || validLitecoin1stChar || validDogecoin1stChar);
        }
        
        return result;
    }
    
    public boolean hasPaymentOptions() {
        return false;
    }

}
