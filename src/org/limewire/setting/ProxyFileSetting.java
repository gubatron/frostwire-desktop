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

import java.io.File;
import java.util.Properties;


/**
 * A proxy, aka a substitute, for a <code>FileSetting</code> object that returns
 * the value of another file setting as its default value.
 * <p>
 * Create a <code>ProxyFileSetting</code> object with a 
 * {@link SettingsFactory#createProxyFileSetting(String, FileSetting)}.
 * <pre>
        FileSetting genericDownloadFolderSetting = settingsfactory.createFileSetting(
                                                        "genericDownloadFolder", 
                                                        new File("/"));
        ProxyFileSetting audioDownloadFolderSetting = settingsfactory.createProxyFileSetting(
                                                            "audioDownloadFolder", 
                                                     genericDownloadFolderSetting);
        
        // prints the generic setting's value
        System.out.println(audioDownloadFolderSetting.getValue());
        
        audioDownloadFolderSetting.setValue(new File("/audio"));
        
        // audio
        System.out.println(audioDownloadFolderSetting.getValue());
        
        genericDownloadFolderSetting.setValue(new File("/new generic"));
        
        //audio
        System.out.println(audioDownloadFolderSetting.getValue());
        
        audioDownloadFolderSetting.revertToDefault();
        
        ///new generic
        System.out.println(audioDownloadFolderSetting.getValue());
    
        Output:
            C:\
            C:\audio
            C:\audio
            C:\new generic
</pre>
 */
public class ProxyFileSetting extends FileSetting {

	private FileSetting defaultSetting;

	/**
	 * Constructs a new file setting that defaults to a different setting.
	 */
	ProxyFileSetting(Properties defaultProps, Properties props, String key,
					 FileSetting defaultSetting) {
        super(defaultProps, props, key, 
                  new File("impossible-default-limewire-filename3141592"));
        setPrivate(true);
        this.defaultSetting = defaultSetting;
	}

	public File getValue() {
		return isDefault() ? defaultSetting.getValue() : super.getValue();
	}
}

