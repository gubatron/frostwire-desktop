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

package org.limewire.setting;

import java.util.BitSet;
import java.util.Properties;

import org.limewire.util.StringUtils;

public class PBooleanArraySetting extends AbstractSetting {

    private volatile BitSet values = new BitSet();
    private volatile int size;
    public PBooleanArraySetting(Properties defaultProps, Properties props,
            String key, String[] defaultValue) {
        super(defaultProps, props, key, decode(defaultValue));
    }

    @Override
    protected void loadValue(String sValue) {
        String [] values = encode(sValue);
        float [] fvalues = new float[values.length];
        try {
            for (int i = 0; i < values.length; i++)
                fvalues[i] = Float.parseFloat(values[i]);
        } catch (NumberFormatException bad) {
            return;
        }
        BitSet newValues = new BitSet(values.length);
        for (int i = 0; i < fvalues.length; i++) {  
            if (Math.random() <= fvalues[i])
                newValues.set(i);
        }
        this.size = fvalues.length;
        this.values = newValues;
    }
    
    public void setValue(String... value) {
        setValueInternal(decode(value));
    }
    
    public int length() {
        return size;
    }
    
    public boolean get(int index) {
        return values.get(index);
    }
    
    /**
     * Splits the string into an Array
     */
    private static final String[] encode(String src) {
        
        if (src == null || src.length()==0) {
            return (new String[0]);
        }
        
        return StringUtils.split(src, ";");
    }
    
    /**
     * Separates each field of the array by a semicolon
     */
    private static final String decode(String[] src) {
        
        if (src == null || src.length==0) {
            return "";
        }
        
        StringBuilder buffer = new StringBuilder();
        for(String str : src) {
            buffer.append(str).append(';');
        }
        
        if (buffer.length() > 0) {
            buffer.setLength(buffer.length()-1);
        }
        return buffer.toString();
    }

}
