package com.frostwire.gnutella.gui.sponsors;

import java.util.HashSet;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXException;

import java.net.URL;
import java.net.HttpURLConnection;

import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Iterator;

import java.io.IOException;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import javax.swing.JLabel;

import com.limegroup.gnutella.util.LimeWireUtils;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.LimeWireCore;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.NetworkManager;



public class SponsorBanner extends JLabel {
	private SponsorLoader _sponsorLoader = new SponsorLoader();
	private static NetworkManager networkManager = null; 
	public static String ipAddress = "0.0.0.0"; //ipBytesToString(networkManager.getExternalAddress());
	

	/** 
	 * This method fires up a thread and tries to get
	 * the external ip address.
	 */
	public static void fetchIpAddress() {
		//We'll try to get the external ipAddress from RouterService
		//and try, and try, 1 second at the time.
		Thread t = new Thread(new Runnable() {
			public void run() {
				LimeWireCore core = GuiCoreMediator.getCore();
				networkManager = core.getNetworkManager();
				byte[] ipBytes = networkManager.getExternalAddress();
				//System.out.println("Fetching ip address on separate thread");
				//System.out.println("My IP address looks like this ->" + ipBytesToString(ipBytes));
				
				int maxTries = 20;
				int seconds=0;
				while (!(GUIMediator.isConstructed()) //&& 
					   //GuiCoreMediator.getCore() != null && 
					   //GuiCoreMediator.getLifecycleManager().isLoaded())
					   ) {
					   //|| 
					   //ipBytesToString(ipBytes).equals("0.0.0.0")) {					
					//System.out.println("Waiting one more second to get the ipAddress");
					try {
					    Thread.currentThread().sleep(1000); } catch (Exception e) {};
					ipBytes = networkManager.getExternalAddress();
					//System.out.println("This is what the networkManager grabbed ->");
					//System.out.println(ipBytesToString(ipBytes));
					//System.out.println();
					
					maxTries--;
					if (maxTries==0) break;
				}
				
				//instance
				String ip = ipBytesToString(ipBytes);
				//System.out.println("MY IP ADDRESS IS THIS ->>>> " + ip);
				SponsorBanner.ipAddress = ip;
			}
		});
		t.start();
	}

	/**
	 * Converts a byte array ip address into a String representation of the ip
	 * @param ip
	 * @return
	 */
	public static String ipBytesToString(byte[] ip) {
		if (ip == null) {
			return new String("0.0.0.0");
		}
		
		int i=0;		
		String result = new String();

		while(i < ip.length) {
			//System.out.println("octet added ->>>> " + Byte.toString(ip[i]));
			if (ip[i] < 0) {					
			result += Integer.toString(256 + Integer.parseInt(Byte.toString(ip[i])));
			} else {
			result += Byte.toString(ip[i]);
			}		
			//System.out.println("Result is " + result);
			i++;
			if (i < ip.length) {
				result += ".";
			} //if
		} //while

		return result;
	} //ipBytesToString

	/**
	 * Uses a SponsorLoader to return a fresh set of Banners from the server.
	 * 
	 * @param container : Optional BannerContainer reference. If they pass a refresh rate for the banner
	 *                    container to tell us to get banners on the XML, we can use this reference
	 *                    to tell the BannerContainer to update the timeout of the banner refresh task
	 *                    or to not reload banners at all.
	 * 
	 * @return
	 */
	public HashSet<SponsorBanner> getBannersFromServer(BannerContainer container) {
		SponsorLoader sponsorLoader = getSponsorLoader();
		
		if (container != null) {
			sponsorLoader.set_bannerContainer(container);
		}
		
		return sponsorLoader.loadBanners();
	}
	
	public SponsorLoader getSponsorLoader() {
		return _sponsorLoader;
	}
	
	private class SponsorLoader implements ContentHandler {
		/**
		 * A banner tag is supposed to look like this.
		 *         
		   <banner src="http://sponsors.frostwire.com/banners/120x600google_checkout.png"
           href="http://www.frostwire.com/sponsors/banners/google_checkout.php"
           width="120" height="600"
           type="torrent|web"
           countries="VE,US"
           language="en"
           duration="60" />
		 */
		public String ipAddress = new String("0.0.0.0");
		

		private boolean _status = false;
		private HashSet<SponsorBanner> _result = new HashSet<SponsorBanner>();
		
		private BannerContainer _bannerContainer = null;
		
		/** 
		 * Connects to sponsors.frostwire.com/sponsors.xml and downloads the meta data
		 * This class is an XML ContentHandler, so this method will create an
		 * XMLReader and use the XML parsing methods to instantiate Banner Objects. 
		 * */
		public HashSet<SponsorBanner> loadBanners() {
			HttpURLConnection connection = null;
			InputSource src = null;
			
			try {
				//System.out.println("Loading banners...");
				connection = (HttpURLConnection) (new URL("http://sponsors.frostwire.com/")).openConnection();
				String userAgent = "FrostWire/" + OSUtils.getOS() + "/" + LimeWireUtils.getLimeWireVersion();
				//System.out.println("User-Agent: "+userAgent);
				connection.setRequestProperty("User-Agent",userAgent);
				src = new InputSource(connection.getInputStream());
				
				XMLReader rdr = XMLReaderFactory.
			    createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser" );//"org.apache.xerces.parsers.SAXParser" );
				rdr.setContentHandler(this);
				rdr.parse(src);
				
				connection.getInputStream().close();
				connection.disconnect();
			} catch (IOException e) {
				//System.out.println("SponsorLoader.loadBanners() exception " + e.toString());
			} catch (SAXException e2) {
				System.out.println("SponsorLoader.loadBanners() SAX exception " + e2.toString());
				e2.printStackTrace();
			}
			//System.out.println("Banners loaded!");			
			return _result;
		}

		public void characters(char[] ch, int start, int length) {
			//System.out.println("SponsorLoader.characters: \n[" + new String(ch) + "\n]");
		}

		public  void endDocument() {
			//System.out.println("SponsorLoader.endDocument()");
		}

		public void endElement(String uri, String localName, String qName) {
		}

		public void startDocument() {
		}

		public void startElement(String uri, String localName, String qName, Attributes atts) {
			//Get the status on the first tag of the document, there's should be an attribute
			localName = localName.toLowerCase();
			
			if (localName.equals("sponsors")) {
				String status = atts.getValue("status").toLowerCase();
				
				this._status = false;
				if (status != null && (status.equals("on") || status.equals("true") || status.equals("1"))) {
					this._status = true;
				} 
			}
			//parse the banners tag. See if they want the BannerContainer to re-request this XML file
			//at a rate different than the default, or not at all
			else if (localName.equals("banners") && this._status) {
				//if there is a refresh rate, it expresses how often we should
				//re-reload this file, and we have to notify the BannerContainer
				//about this rate.
				//If the refreshrate is {-1, 0, No, None, False, Off}, it means this file doesn't have to be reloaded.
				String refreshRate =  atts.getValue("refreshrate");
				if (refreshRate != null) {
					//System.out.println("SponsorLoader.startElement -> Detected a custom refreshrate -> " + refreshRate);
					
					if (refreshRate.equals("0") ||
						refreshRate.equals("-1") ||
						refreshRate.equalsIgnoreCase("no") ||
						refreshRate.equalsIgnoreCase("none") || 
						refreshRate.equalsIgnoreCase("false") ||
						refreshRate.equalsIgnoreCase("off")) {
						//kill the refresh banners task
						//System.out.println("SponsorLoader.startElement -> Killing Banner Refresh Tasks");
						this.get_bannerContainer().setupBannerRefreshTask(-1);
					} else {
						//attempt to update the refresh rate
						int refreshRateInt = -1;
						
						try {
							refreshRateInt = Integer.parseInt(refreshRate);
							
							//frostwire can't travel to the past yet, make sure interval is positive
							refreshRateInt = Math.abs(refreshRateInt); 
						} catch (Exception e) {
							
						}
						
						if (refreshRateInt > 0) {
							//System.out.println("SponsorLoader.startElement -> setting custom banner refresh task interval - " + refreshRateInt);
							this.get_bannerContainer().setupBannerRefreshTask(refreshRateInt);
						}
					}
				}
			}
			//parse the banner tag, only if the status is set to on. 
			else if (localName.equals("banner") && this._status) {
				//got a banner, read properties here
				String src = new String(atts.getValue("src"));
				String href= new String(atts.getValue("href"));
				int width = Integer.parseInt(atts.getValue("width"));
				int height = Integer.parseInt(atts.getValue("height"));
				int duration = Integer.parseInt(atts.getValue("duration"));
				
				//now some optional properties
				
				//ipranges
				Set<String> ipRanges = new HashSet<String>(); //ipranges="100.*.*.*,200.12.23.4*,..."
				if (atts.getValue("ipranges")!=null) {
					String ipranges_string = atts.getValue("ipranges");
					//first remove any spaces...
					ipranges_string = ipranges_string.replaceAll("\\s","");
					String[] ips = ipranges_string.split(",");
					for (String ip:ips) {
						ipRanges.add(ip);
						//System.out.println("Added IP Range -> " + ip);
					}
				} else {
					//System.out.println("No IP Ranges");
					ipRanges = null;	
				}
				
				//banner type (optional) [for next versions, to open torrents, etc.]
				String type = null;
				if (atts.getValue("type")!=null) {
					type = new String(atts.getValue("type"));
				}
				
				//country
				HashMap<String, String> countries = null;
				if (atts.getValue("countries")!=null) {
					countries = new HashMap<String,String>();
					String countries_str = new String(atts.getValue("countries"));
					countries_str = countries_str.trim();
					String[] countries_array = countries_str.split(",");
					for (String country: countries_array) {
						countries.put(country.toUpperCase(),country.toUpperCase());
					}
				}
				
				//language
				String language = null;
				if (atts.getValue("language")!=null) {
					language = new String(atts.getValue("language"));
				}
				
				//FrostWire version
				String version = null;
				if (atts.getValue("version")!=null) {
					version = new String(atts.getValue("version"));
				}
				
				SponsorBanner banner = new SponsorBanner(href,src,width,height,duration,ipRanges,type,countries,language,version);

				//FILTERING HAPPENS HERE.
				
				if (version != null && !frostWireMatchesVersion(version)) {
					//System.out.println("Version mismatch - " + version);					
					return;
				}
	
				//Skip if system country not on given list of countries for this banner
				if (countries != null &&
					!banner.hasCountry(banner.getSystemCountry())) {
					//System.out.println("BANNER SKIPPED FOR COUNTRY NON MATCHING");
					return;
				}
				
				//Skip language if lang not on given list of languages for this banner
				if (language != null && 
					!language.equals("") &&
					!banner.getSystemLanguage().equals(banner.getLanguage())) {
					//System.out.println("BANNER SKIPPED FOR LANGUAGE NOT MATCHING");
					return;
				}
				
				
				
				/**
				 * //IP RANGES MATCHING (Turned off on 4.18.2, no longer used, can be done on server side)
				//System.out.println("OUR IP ADDRESS IS -> " + SponsorBanner.ipAddress);
				if (SponsorBanner.ipAddress.equals("0.0.0.0")) {
					//If Ip hasn't been set, don't bother filtering
					SponsorBanner.fetchIpAddress();
				} else if (!SponsorBanner.ipAddress.equals("0.0.0.0") &&
					banner.getIpRanges() != null &&
					banner.getIpRanges().size() > 0) {
					if (!ipMatchesRanges(SponsorBanner.ipAddress, banner.getIpRanges())) {
						//System.out.println("BANNER SKIPPED FOR IP RANGE NOT MATCHING");
						return;
					}
				}
				*/

				
				//All filters passed, add to resulting banner list.
				//System.out.println();
				//System.out.println("Banner Added " + banner.getUrl());
				this._result.add(banner);
				//System.out.println();
			}
		} //startElement
		
		/**
		 * Given a version string, say:
		 * 4.*.*
		 * 4.13.*
		 * 4.13.1
		 * 
		 * It will compare the current version against the given string.
		 * If our version is a match for the given version expression this function will return true 
		 *
		 * @param version
		 * @return
		 */

	    public boolean frostWireMatchesVersion(String version) {
	        String currentVersion = "4.17.5"; //CommonUtils.getFrostWireVersion();

	        if (version == null || version.equals("")) {
	            return false;
	        }

	        String[] fwVersionParts = currentVersion.split("\\.");
	        String fw_major = fwVersionParts[0];
	        String fw_release = fwVersionParts[1];
	        String fw_service = fwVersionParts[2];

	        String[] versionParts = version.split("\\.");
	        String v_major = versionParts[0];
	        String v_release = versionParts[1];
	        String v_service = versionParts[2];

	        if (!v_major.equals("*") && !fw_major.equals(v_major)) {
	            return false;
	        }

	        if (!v_release.equals("*") && !fw_release.equals(v_release)) {
	            return false;
	        }

	        if (!v_service.equals("*") && !fw_service.equals(v_service)) {
	            return false;
	        }


	        return true;
	    }
		
		/** Compares an ip address to a bunch of ipRange expressions.
		 * If at least one matches, then it returns true.
		 * @param ip
		 * @param ipRanges
		 * @return
		 */
		public boolean ipMatchesRanges(String ip, Set<String> ipRanges) {
			if (ip==null || ip.equals("0.0.0.0")) {
				return false;
				//should probably throw an exception here
			}

			if (ipRanges != null && ip != null) {
				Iterator<String> iterator = ipRanges.iterator();
				String ipRange = null;
				while (iterator.hasNext()) {
					ipRange = iterator.next();
					//System.out.println("About to compare ("+ip+") vs ("+ipRange+")");
					if (ipMatchesRange(ip,ipRange)) {
						//System.out.println("Found a match -> " + ip + " vs " + ipRange);
						return true;
					}
				} //for
			} //if
			
			return false;
		} //ipMatchesRanges
		
		/**
		 * Compares ip octects to a single ipRange.
		 * Examples
		 *  ip = 192.168.34.2
		 *  ipRange = 192.168.*.*
		 *  output = true
		 *  
		 *  Only wildcards allowed are * and x, represents any number from 0-255
		 * @param ip
		 * @param ipRange
		 * @return
		 */
		public boolean ipMatchesRange(String ip, String ipRange) {
			if (ip == null) { System.out.println("ip null"); return false; }
			if (ipRange == null) { System.out.println("ipRange null"); return false; }

			String[] ipOctets = ip.split("\\.");
			String[] ipRangeOctets = ipRange.split("\\.");
			int octetIndex = 0;

			if (ipOctets.length != ipRangeOctets.length) {
				//System.out.println("Octet lengths don't match");
				return false;
			}

			for (String octet:ipOctets) {
				//If I get a wild card, or if the current octect matches
				//the ipRange octect, we have a match

				//System.out.println("Comparing ("+octet+") vs ("+ipRangeOctets[octetIndex]+")");
				if (!(ipRangeOctets[octetIndex].equals("*") ||
						ipRangeOctets[octetIndex].equals("x") ||
						ipRangeOctets[octetIndex].equals(octet))) {
					//System.out.println("FAILED\n");
					return false;
				}
				//System.out.println("PASSED");
				octetIndex+=1;
			}
			//System.out.println();
			return true;
		} //ipMatchesRange



		
		public void skippedEntity(String name) {

		}
		
		public void processingInstruction(String target, String data) {

		}
		
		public void ignorableWhitespace(char[] ch, int start, int length) {
			
		}
		
		public void setDocumentLocator(Locator locator) {
			
		}
		
		public void startPrefixMapping(String prefix, String uri) {
			
		}
		
		public void endPrefixMapping(String prefix) {
			
		}

		public BannerContainer get_bannerContainer() {
			return _bannerContainer;
		}

		public void set_bannerContainer(BannerContainer container) {
			_bannerContainer = container;
		}
	
	} //SponsorLoader - XML parser inner class

	
	public SponsorBanner() {};
	
	public SponsorBanner(String url, 
			             String imageSrc, 
			             int width, 
			             int height, 
			             int duration,
			             Set<String> ipRanges,
			             String type,
			             HashMap<String,String> countries,
			             String language,
			             String version) {
        this.setUrl(url);
        this.setImageSrc(imageSrc);
		this.setWidth(width);
		this.setHeight(height);
		this.setDuration(duration);
		this.setIpRanges(ipRanges);
		this.setVersion(version);
		
		this.setType(type);
		
		if (countries != null) {
			this.setCountries(countries);
		}
		
		if (language != null) {
			this.setLanguage(language);
		}
		
		this.setText("<html><img src=\""+this.getImageSrc()+"\" width=\""+this.getWidth()+"\" height=\""+this.getHeight()+"\" border=\"0\"/></html>");
		this.setSize(new Dimension(this.getWidth(),this.getHeight()));

		addMouseListener(new MouseAdapter() {
			//THIS IS WHAT HAPPENS WHEN THEY CLICK ON THE BANNER
			public void mouseClicked(MouseEvent evt) {
				//depending on banner type if set, we do something else,
				//maybe start a download on the network.
				
				if (getType() == null ||
					getType().equals("web")) {
					GUIMediator.openURL(getUrl());
					return;
				}
				
				if (getType().equals("torrent")) {
					//System.out.println("SponsorBanner.mouseClicked: Should open a torrent.");
					String urlString = getUrl();
					
					try {
						java.net.URI uri = new java.net.URI(urlString);
						
						String scheme = uri.getScheme();
						if(scheme == null || !scheme.equalsIgnoreCase("http")) {
							//System.out.println("Not a torrent URL");
							return;
						}
						
						String authority = uri.getAuthority();
						if(authority == null || authority.equals("") || authority.indexOf(' ') != -1) {
							//System.out.println("Invalid authority");
							return;
						}
						
						GUIMediator.instance().openTorrentURI(uri);
					} catch (Exception e) {
						System.out.println(e);
					}
					return;
				}
			} //mouseClicked
			
			public void mouseEntered(MouseEvent evt) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			} //mouseEntered
			
			public void mouseExited(MouseEvent evt) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			} //mouseExited
		});
	}
	
	private String url;
	private String imageSrc;
	private int width;
	private int height;
	private int duration;
	private Set<String> ipRanges = null;
	private String type = null;
	private HashMap<String, String> countries = null;
	private String language = null;
	private String locale = null;
	private String version = null;
	
	public String getUrl() { return this.url; }
	public String getImageSrc() { return this.imageSrc; }
	public int getWidth() { if (this.width<=0) return 120; return this.width; } 
	public int getHeight() { if (this.height<=0) return 600; return this.height; }
	public int getDuration() { if (this.duration<=0) return 300; return this.duration; }	
	public Set<String> getIpRanges() { return this.ipRanges; }
	public String getVersion() { return this.version; }
	
	/** Could be used to have a different click handler on the banner */
	public String getType() { return this.type; }

	public String getBannerLocale() { return this.locale; }
	
	public void addCountry(String country) {
		if (country == null)
			return;
		
		if (this.countries == null)
			this.countries = new HashMap<String,String>();
		
		this.countries.put(country, country); 
	}
	
	public boolean hasCountry(String country) { 
		if (this.countries == null || country == null)
			return false;
		return this.countries.containsKey(country); 
	}
	
	public void setCountries(HashMap<String,String> countries) {
		this.countries = countries;
	}
	
	public HashMap<String,String> getCountries() { return this.countries; }
	
	public String getLanguage() { return this.language; }

	public String getSystemCountry() {
		String country = System.getProperty("user.country");
		if (country == null) country = "";
		return country;
	}
	
	public String getSystemLanguage() {
		//String language = System.getProperty("user.language");
		if (language == null) language = "";
		return language;
	}
	
	public void setUrl(String url) { this.url = url; }
	public void setImageSrc(String src) { this.imageSrc = src; }
	public void setWidth(int w) { this.width = w; }
	public void setHeight(int h ) { this.height = h; }
	public void setDuration(int d) { this.duration = d; }
	public void setIpRanges(Set<String> ipRanges) { 
		if (ipRanges != null &&
			ipRanges.size() == 0) {
			ipRanges = null;
		}
		this.ipRanges = ipRanges; 
	}
	public void setVersion(String v) { this.version = v; }

	/** 
	 * expected mime types are:
	 * 
	 * - null | webpage -> To open a web browser with the URL
	 * - torrent -> To open and download the file specified by the torrent URL
	 */
	public void setType(String type) { 
	
		if (type != null) {
			type = type.toLowerCase();
		}
		
		this.type = type; 
	}
	
	public void setLanguage(String language) { this.language = language; }
	public void setBannerLocale(String locale) {this.locale = locale; }
}