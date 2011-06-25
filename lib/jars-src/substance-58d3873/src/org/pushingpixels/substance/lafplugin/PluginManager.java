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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import javax.swing.UIManager;

/**
 * Plugin manager for look-and-feels.
 * 
 * @author Kirill Grouchnikov
 * @author Erik Vickroy
 * @author Robert Beeger
 * @author Frederic Lavigne
 * @author Pattrick Gotthardt
 */
public class PluginManager {
	private String mainTag;

	private String pluginTag;

	private String xmlName;

	private Set plugins;

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
	public PluginManager(String xmlName, String mainTag, String pluginTag) {
		this.xmlName = xmlName;
		this.mainTag = mainTag;
		this.pluginTag = pluginTag;
		this.plugins = null;
	}

	// protected String getPluginClass(URL pluginUrl) {
	// InputStream is = null;
	// try {
	// DocumentBuilder builder = DocumentBuilderFactory.newInstance()
	// .newDocumentBuilder();
	// is = pluginUrl.openStream();
	// Document doc = builder.parse(is);
	// Node root = doc.getFirstChild();
	// if (!this.mainTag.equals(root.getNodeName()))
	// return null;
	// NodeList children = root.getChildNodes();
	// for (int i = 0; i < children.getLength(); i++) {
	// Node child = children.item(i);
	// if (!this.pluginTag.equals(child.getNodeName()))
	// continue;
	// if (child.getChildNodes().getLength() != 1)
	// return null;
	// Node text = child.getFirstChild();
	// if (text.getNodeType() != Node.TEXT_NODE)
	// return null;
	// return text.getNodeValue();
	// }
	// return null;
	// } catch (Exception exc) {
	// return null;
	// } finally {
	// if (is != null) {
	// try {
	// is.close();
	// } catch (Exception e) {
	// }
	// }
	// }
	// }
	//
	protected String getPluginClass(URL pluginUrl) {
		InputStream is = null;
		InputStreamReader isr = null;
		try {
			XMLElement xml = new XMLElement();
			is = pluginUrl.openStream();
			isr = new InputStreamReader(is);
			xml.parseFromReader(isr);
			if (!this.mainTag.equals(xml.getName()))
				return null;
			Enumeration children = xml.enumerateChildren();
			while (children.hasMoreElements()) {
				XMLElement child = (XMLElement) children.nextElement();
				if (!this.pluginTag.equals(child.getName()))
					continue;
				if (child.countChildren() != 0)
					return null;
				return child.getContent();
			}
			return null;
		} catch (Exception exc) {
			return null;
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (Exception e) {
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
	}

	protected Object getPlugin(URL pluginUrl) throws Exception {
		String pluginClassName = this.getPluginClass(pluginUrl);
		if (pluginClassName == null)
			return null;
		ClassLoader classLoader = (ClassLoader) UIManager.get("ClassLoader");
		if (classLoader == null)
			classLoader = Thread.currentThread().getContextClassLoader();
		Class pluginClass = Class.forName(pluginClassName, true, classLoader);
		if (pluginClass == null)
			return null;
		Object pluginInstance = pluginClass.newInstance();
		if (pluginInstance == null)
			return null;
		return pluginInstance;
	}

	/**
	 * Returns a collection of all available plugins.
	 * 
	 * @return Collection of all available plugins. The classpath is scanned
	 *         only once.
	 * @see #getAvailablePlugins(boolean)
	 */
	public Set getAvailablePlugins() {
		return this.getAvailablePlugins(false);
	}

	/**
	 * Returns a collection of all available plugins. The parameter specifies
	 * whether the classpath should be rescanned or whether to return the
	 * already found plugins (after first-time scan).
	 * 
	 * @param toReload
	 *            If <code>true</code>, the classpath is scanned for available
	 *            plugins every time <code>this</code> function is called. If
	 *            <code>false</code>, the classpath scan is performed only once.
	 *            The consecutive calls return the cached result.
	 * @return Collection of all available plugins.
	 */
	public Set getAvailablePlugins(boolean toReload) {
		if (!toReload && (this.plugins != null))
			return this.plugins;

		this.plugins = new HashSet();

		// the following is fix by Dag Joar and Christian Schlichtherle
		// for application running with -Xbootclasspath VM flag. In this case,
		// the using MyClass.class.getClassLoader() would return null,
		// but the context class loader will function properly
		// that classes will be properly loaded regardless of whether the lib is
		// added to the system class path, the extension class path and
		// regardless of the class loader architecture set up by some
		// frameworks.
		ClassLoader cl = (ClassLoader) UIManager.get("ClassLoader");
		if (cl == null)
			cl = Thread.currentThread().getContextClassLoader();
		try {
			Enumeration urls = cl.getResources(this.xmlName);
			while (urls.hasMoreElements()) {
				URL pluginUrl = (URL) urls.nextElement();
				Object pluginInstance = this.getPlugin(pluginUrl);
				if (pluginInstance != null)
					this.plugins.add(pluginInstance);

			}
		} catch (Exception exc) {
			return null;
		}

		return plugins;
	}
}
