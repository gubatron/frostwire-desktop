/*
 * Copyright (c) 2005-2010 Laf-Plugin Kirill Grouchnikov and contributors. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.lafplugin;

import java.util.Iterator;
import java.util.Set;

import javax.swing.UIDefaults;

/**
 * Plugin manager for look-and-feels.
 * 
 * @author Kirill Grouchnikov
 * @author Erik Vickroy
 * @author Robert Beeger
 * @author Frederic Lavigne
 * @author Pattrick Gotthardt
 */
public class ComponentPluginManager extends PluginManager {
	/**
	 * Simple constructor.
	 * 
	 * @param xmlName
	 *            The name of XML file that contains plugin configuration.
	 * @param mainTag
	 *            The main tag in the XML configuration file.
	 * @param pluginTag
	 *            The tag that corresponds to a single plugin kind. Specifies
	 *            the plugin kind that will be located in
	 *            {@link #getAvailablePlugins(boolean)}.
	 */
	public ComponentPluginManager(String xmlName) {
		super(xmlName, LafComponentPlugin.TAG_MAIN,
				LafComponentPlugin.COMPONENT_TAG_PLUGIN_CLASS);
	}

	/**
	 * Helper function to initialize all available component plugins of
	 * <code>this</code> plugin manager. Calls the
	 * {@link LafComponentPlugin#initialize()} of all available component
	 * plugins.
	 */
	public void initializeAll() {
		Set availablePlugins = this.getAvailablePlugins();
		for (Iterator iterator = availablePlugins.iterator(); iterator
				.hasNext();) {
			Object pluginObject = iterator.next();
			if (pluginObject instanceof LafComponentPlugin)
				((LafComponentPlugin) pluginObject).initialize();
		}
	}

	/**
	 * Helper function to uninitialize all available component plugins of
	 * <code>this</code> plugin manager. Calls the
	 * {@link LafComponentPlugin#uninitialize()} of all available component
	 * plugins.
	 */
	public void uninitializeAll() {
		Set availablePlugins = this.getAvailablePlugins();
		for (Iterator iterator = availablePlugins.iterator(); iterator
				.hasNext();) {
			Object pluginObject = iterator.next();
			if (pluginObject instanceof LafComponentPlugin)
				((LafComponentPlugin) pluginObject).uninitialize();
		}
	}

	/**
	 * Helper function to process the (possibly) theme-dependent default
	 * settings of all available component plugins of <code>this</code> plugin
	 * manager. Calls the {@link LafComponentPlugin#getDefaults(Object)} of all
	 * available plugins and puts the respective results in the specified table.
	 * 
	 * @param table
	 *            The table that will be updated with the (possibly)
	 *            theme-dependent default settings of all available component
	 *            plugins.
	 * @param themeInfo
	 *            LAF-specific information on the current theme.
	 */
	public void processAllDefaultsEntries(UIDefaults table, Object themeInfo) {
		Set availablePlugins = this.getAvailablePlugins();
		for (Iterator iterator = availablePlugins.iterator(); iterator
				.hasNext();) {
			Object pluginObject = iterator.next();
			if (pluginObject instanceof LafComponentPlugin) {
				Object[] defaults = ((LafComponentPlugin) pluginObject)
						.getDefaults(themeInfo);
				if (defaults != null) {
					table.putDefaults(defaults);
				}
			}
		}
	}
}
