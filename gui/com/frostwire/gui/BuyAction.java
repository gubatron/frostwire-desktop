/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 *
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

package com.frostwire.gui;

import java.awt.event.ActionEvent;
import java.net.URLEncoder;

import javax.swing.AbstractAction;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.search.SearchResultDataLine;
import com.limegroup.gnutella.util.FrostWireUtils;
import com.limegroup.gnutella.util.QueryUtils;

/**
 * @author gubatron
 *
 * The Buy Action will basically create a query for media on a third party site.
 * If the client is not instructed to send it's parameters a certain URL
 * it will default to amazon.com's amazonmp3 or amazon unbox
 * 
 * BuyAction will check for a redirect uri based on a buyUrl attribute inside the
 * update.xml retrieved upon launch from update.frostwire.com
 * 
 * If update.xml has no "buyUrl" set it will use its default behaviour to make
 * the HTTP request on the default web browser.
 */
public class BuyAction extends AbstractAction implements LimeAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SearchResultDataLine _tableLine = null;
	
	private String _buyUrl = null;
	
	public void setTableLine(SearchResultDataLine tl) { this._tableLine = tl; }
	
	public SearchResultDataLine getTableLine() { return this._tableLine; }
	
	//Use GUIMediator.openURL(String url) to open this url on a browser.
	private static String redirect_url = null; //by default null
	
	public static void setRedirectUrl(String url) {
		BuyAction.redirect_url = url;
	}
	
	public static boolean hasRedirectUrl() {
		return BuyAction.redirect_url != null;
	}
	
	public static String getRedirectUrl() {
		if (!BuyAction.redirect_url.endsWith("/")) {
			//make sure it ends with /
			BuyAction.redirect_url += "/";
		}
		return BuyAction.redirect_url;
	}
	
	/**
	 * 
	 */
	public BuyAction() {
		super();
		this.putValue(LimeAction.SHORT_NAME, I18n.tr("Buy"));
		this.putValue(LimeAction.ICON_NAME,"BUY");
	}
	
	/**
	 * Prepare the URL that we'll open, depending on the settings on update.frostwire.com
	 * and what media type the user has selected on the results.
	 */
	private void prepareBuyUrl() {
		if (this.getTableLine() == null) {
			//no result, no url
			this._buyUrl = null;
			return;
		}

		//use amazon if we don't receive a redirect url from update
		String post = "http://www.amazon.com/gp/associates/link-types/searchbox.html?";
		String tag = "tag=frostwcom-20&"; //our amazon associate id
		String creative = "creative=374005&"; //gotta find out what this number is, could be the same 374005
		String campaign = "campaign=211041&";
		String adid = "adid=1YQ8K05FFCXQ9X16F37M&";
		
		String mode = "mode=aps&"; //this will change depending on the MediaType: (blended:all products, books, photo, digital-music, software, amazontv)
		
		MediaType mt = this.getTableLine().getNamedMediaType().getMediaType();
//		String sha1 = "";
//		if (this.getTableLine().getSHA1Urn()!=null)
//			sha1 = this.getTableLine().getSHA1Urn().toString();
		
		String size = String.valueOf(this.getTableLine().getSize());
		
		if (mt.equals(MediaType.getAudioMediaType())) {
			mode = "mode=digital-music&";
		} else if (mt.equals(MediaType.getVideoMediaType())) {
			mode = "mode=amazontv&";
		} else if (mt.equals(MediaType.getImageMediaType())) {
			mode = "mode=photo&";
		} else if (mt.equals(MediaType.getDocumentMediaType())) {
			mode = "mode=books&";
		} else if (mt.equals(MediaType.getProgramMediaType())) {
			mode= "mode=software&";
		}
		
		String encoded_keywords = null;
		String fwUserAgent = "FrostWire/" + OSUtils.getOS() + "/" + FrostWireUtils.getFrostWireVersion();
		
		try {
			encoded_keywords = URLEncoder.encode(this.getCurrentFileKeywords(),"UTF-8"); //temporarily "madonna" for testing purposes
			fwUserAgent = URLEncoder.encode(fwUserAgent,"UTF-8");
		} catch (Exception e) {
			encoded_keywords = "madonna";
		}
		String keyword = "keyword="+encoded_keywords;
		
		this._buyUrl = new String();

		//Go to redirect URL if present
		if (BuyAction.hasRedirectUrl()) {
			this._buyUrl = BuyAction.getRedirectUrl() + "?" + 
				mode + 
				"keywords=" + encoded_keywords + 
				"&fwUseragent=" + fwUserAgent + 
				//"&sha1=" + sha1 + 
				"&size=" + size;
			//System.out.println("The Buy URL is: " + this._buyUrl);
		} else { 
		    //Go straight to default third party site
			this._buyUrl = post + tag + creative + campaign + adid + mode + keyword;
		}
	} //prepareBuyUrl
	
	private void openBuyUrl() {
		if (this._buyUrl != null)
			GUIMediator.openURL(this._buyUrl);
	}
	
	/**
	 * This should see the file represented inside de current TableLine object
	 * and retrieve the smartest combination of keywords we can send to our third party.
	 * 
	 * In the case of a music file it should at least come back with the name of the artist
	 * and the album.
	 * @return
	 */
	private String getCurrentFileKeywords() {
		if (this.getTableLine()==null) {
			return "radiohead";
		}
		String keywords = QueryUtils.createQueryString(this.getTableLine().getFilename());
		return keywords;
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		//System.out.println("BuyAction.actionPerformed()");
		if (this.getTableLine() != null) {
			prepareBuyUrl();
			openBuyUrl();
			_buyUrl = null; //reset it
		} 
	}

}
