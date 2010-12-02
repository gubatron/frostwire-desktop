package com.frostwire.gnutella.gui.sponsors;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JLabel;

import org.limewire.util.OSUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.frostwire.ImageCache;
import com.frostwire.ImageCache.OnLoadedListener;
import com.limegroup.gnutella.LimeWireCore;
import com.limegroup.gnutella.NetworkManager;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.util.LimeWireUtils;


@SuppressWarnings("serial")
public class SponsorBanner extends JLabel {

	private static final long serialVersionUID = -2074000214262216103L;
	private SponsorLoader _sponsorLoader = new SponsorLoader();
	private static NetworkManager networkManager = null;
	public static String ipAddress = "0.0.0.0"; // ipBytesToString(networkManager.getExternalAddress());

	/**
	 * This method fires up a thread and tries to get the external ip address.
	 */
	public static void fetchIpAddress() {
		// We'll try to get the external ipAddress from RouterService
		// and try, and try, 1 second at the time.
		Thread t = new Thread(new Runnable() {
			public void run() {
				LimeWireCore core = GuiCoreMediator.getCore();
				networkManager = core.getNetworkManager();
				byte[] ipBytes = networkManager.getExternalAddress();

				int maxTries = 20;

				while (!(GUIMediator.isConstructed())) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignore) {}
					ipBytes = networkManager.getExternalAddress();

					maxTries--;
					if (maxTries == 0)
						break;
				}

				// instance
				String ip = ipBytesToString(ipBytes);
				SponsorBanner.ipAddress = ip;
			}
		});
		t.start();
	}

	/**
	 * Converts a byte array ip address into a String representation of the ip
	 * 
	 * @param ip
	 * @return
	 */
	public static String ipBytesToString(byte[] ip) {
		if (ip == null) {
			return new String("0.0.0.0");
		}

		int i = 0;
		String result = new String();

		while (i < ip.length) {
			// System.out.println("octet added ->>>> " + Byte.toString(ip[i]));
			if (ip[i] < 0) {
				result += Integer.toString(256 + Integer.parseInt(Byte
						.toString(ip[i])));
			} else {
				result += Byte.toString(ip[i]);
			}
			// System.out.println("Result is " + result);
			i++;
			if (i < ip.length) {
				result += ".";
			} // if
		} // while

		return result;
	} // ipBytesToString

	/**
	 * Uses a SponsorLoader to return a fresh set of Banners from the server.
	 * 
	 * @param container
	 *            : Optional BannerContainer reference. If they pass a refresh
	 *            rate for the banner container to tell us to get banners on the
	 *            XML, we can use this reference to tell the BannerContainer to
	 *            update the timeout of the banner refresh task or to not reload
	 *            banners at all.
	 * 
	 * @return
	 */
	public Set<SponsorBanner> getBannersFromServer(BannerContainer container) {
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
		 * A banner tag is supposed to look like 
		 * 
		 * <banner src=
		 * "http://sponsors.frostwire.com/banners/120x600google_checkout.png"
		 * href="http://www.frostwire.com/sponsors/banners/google_checkout.php"
		 * width="120" height="600" type="torrent|web" countries="VE,US"
		 * language="en" duration="60" />
		 */

		private boolean _status = false;
		private Set<SponsorBanner> _result = new LinkedHashSet<SponsorBanner>();

		private BannerContainer _bannerContainer = null;

		/**
		 * Connects to sponsors.frostwire.com/sponsors.xml and downloads the
		 * meta data This class is an XML ContentHandler, so this method will
		 * create an XMLReader and use the XML parsing methods to instantiate
		 * Banner Objects.
		 * */
		public Set<SponsorBanner> loadBanners() {
			HttpURLConnection connection = null;
			InputSource src = null;

			try {
				// System.out.println("Loading banners...");
				connection = (HttpURLConnection) (new URL(
						"http://sponsors.frostwire.com/")).openConnection();
				String userAgent = "FrostWire/" + OSUtils.getOS() + "/"
						+ LimeWireUtils.getLimeWireVersion();
				// System.out.println("User-Agent: "+userAgent);
				connection.setRequestProperty("User-Agent", userAgent);
				src = new InputSource(connection.getInputStream());

				XMLReader rdr = XMLReaderFactory
						.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");// "org.apache.xerces.parsers.SAXParser"
																									// );
				rdr.setContentHandler(this);
				rdr.parse(src);
			} catch (IOException e) {
				// System.out.println("SponsorLoader.loadBanners() exception " +
				// e.toString());
			} catch (SAXException e2) {
				System.out.println("SponsorLoader.loadBanners() SAX exception "
						+ e2.toString());
				e2.printStackTrace();
			} finally {
				if (connection != null) {
					try {
						connection.getInputStream().close();
					} catch (IOException e) {
					}
					connection.disconnect();
				}
			}
			// System.out.println("Banners loaded!");
			return _result;
		}

		public void characters(char[] ch, int start, int length) {
			// System.out.println("SponsorLoader.characters: \n[" + new
			// String(ch) + "\n]");
		}

		public void endDocument() {
			// System.out.println("SponsorLoader.endDocument()");
		}

		public void endElement(String uri, String localName, String qName) {
		}

		public void startDocument() {
		}

		public void startElement(String uri, String localName, String qName,
				Attributes atts) {
			// Get the status on the first tag of the document, there's should
			// be an attribute
			localName = localName.toLowerCase();

			if (localName.equals("sponsors")) {
				String status = atts.getValue("status").toLowerCase();

				_status = false;
				if (status != null
						&& (status.equals("on") || status.equals("true") || status
								.equals("1"))) {
					_status = true;
				}
			}
			// parse the banners tag. See if they want the BannerContainer to
			// re-request this XML file
			// at a rate different than the default, or not at all
			else if (localName.equals("banners") && _status) {
				// if there is a refresh rate, it expresses how often we should
				// re-reload this file, and we have to notify the
				// BannerContainer
				// about this rate.
				// If the refreshrate is {-1, 0, No, None, False, Off}, it means
				// this file doesn't have to be reloaded.
				String refreshRate = atts.getValue("refreshrate");
				if (refreshRate != null) {
					// System.out.println("SponsorLoader.startElement -> Detected a custom refreshrate -> "
					// + refreshRate);

					if (refreshRate.equals("0") || refreshRate.equals("-1")
							|| refreshRate.equalsIgnoreCase("no")
							|| refreshRate.equalsIgnoreCase("none")
							|| refreshRate.equalsIgnoreCase("false")
							|| refreshRate.equalsIgnoreCase("off")) {
						// kill the refresh banners task
						// System.out.println("SponsorLoader.startElement -> Killing Banner Refresh Tasks");
						get_bannerContainer().setupBannerRefreshTask(-1);
					} else {
						// attempt to update the refresh rate
						int refreshRateInt = -1;

						try {
							refreshRateInt = Integer.parseInt(refreshRate);

							// frostwire can't travel to the past yet, make sure
							// interval is positive
							refreshRateInt = Math.abs(refreshRateInt);
						} catch (Exception e) {

						}

						if (refreshRateInt > 0) {
							// System.out.println("SponsorLoader.startElement -> setting custom banner refresh task interval - "
							// + refreshRateInt);
							get_bannerContainer().setupBannerRefreshTask(
									refreshRateInt);
						}
					}
				}
			}
			// parse the banner tag, only if the status is set to on.
			else if (localName.equals("banner") && _status) {
				// got a banner, read properties here
				String src = new String(atts.getValue("src"));
				String href = new String(atts.getValue("href"));
				int width = Integer.parseInt(atts.getValue("width"));
				int height = Integer.parseInt(atts.getValue("height"));
				int duration = Integer.parseInt(atts.getValue("duration"));

				// now some optional properties

				// ipranges
				Set<String> ipRanges = new HashSet<String>(); // ipranges="100.*.*.*,200.12.23.4*,..."
				if (atts.getValue("ipranges") != null) {
					String ipranges_string = atts.getValue("ipranges");
					// first remove any spaces...
					ipranges_string = ipranges_string.replaceAll("\\s", "");
					String[] ips = ipranges_string.split(",");
					for (String ip : ips) {
						ipRanges.add(ip);
						// System.out.println("Added IP Range -> " + ip);
					}
				} else {
					// System.out.println("No IP Ranges");
					ipRanges = null;
				}

				// banner type (optional) [for next versions, to open torrents,
				// etc.]
				String type = null;
				if (atts.getValue("type") != null) {
					type = new String(atts.getValue("type"));
				}

				// country
				HashMap<String, String> countries = null;
				if (atts.getValue("countries") != null) {
					countries = new HashMap<String, String>();
					String countries_str = new String(atts
							.getValue("countries"));
					countries_str = countries_str.trim();
					String[] countries_array = countries_str.split(",");
					for (String country : countries_array) {
						countries.put(country.toUpperCase(), country
								.toUpperCase());
					}
				}

				// language
				String language = null;
				if (atts.getValue("language") != null) {
					language = new String(atts.getValue("language"));
				}

				// FrostWire version
				String version = null;
				if (atts.getValue("version") != null) {
					version = new String(atts.getValue("version"));
				}

				SponsorBanner banner = new SponsorBanner(href, src, width,
						height, duration, ipRanges, type, countries, language,
						version);

				// FILTERING HAPPENS HERE.

				if (version != null && !frostWireMatchesVersion(version)) {
					// System.out.println("Version mismatch - " + version);
					return;
				}

				// Skip if system country not on given list of countries for
				// this banner
				if (countries != null
						&& !banner.hasCountry(banner.getSystemCountry())) {
					// System.out.println("BANNER SKIPPED FOR COUNTRY NON MATCHING");
					return;
				}

				// Skip language if lang not on given list of languages for this
				// banner
				if (language != null
						&& !language.equals("")
						&& !banner.getSystemLanguage().equals(
								banner.getLanguage())) {
					// System.out.println("BANNER SKIPPED FOR LANGUAGE NOT MATCHING");
					return;
				}

				/**
				 * //IP RANGES MATCHING (Turned off on 4.18.2, no longer used,
				 * can be done on server side)
				 * //System.out.println("OUR IP ADDRESS IS -> " +
				 * SponsorBanner.ipAddress); if
				 * (SponsorBanner.ipAddress.equals("0.0.0.0")) { //If Ip hasn't
				 * been set, don't bother filtering
				 * SponsorBanner.fetchIpAddress(); } else if
				 * (!SponsorBanner.ipAddress.equals("0.0.0.0") &&
				 * banner.getIpRanges() != null && banner.getIpRanges().size() >
				 * 0) { if (!ipMatchesRanges(SponsorBanner.ipAddress,
				 * banner.getIpRanges())) { //System.out.println(
				 * "BANNER SKIPPED FOR IP RANGE NOT MATCHING"); return; } }
				 */

				// All filters passed, add to resulting banner list.
				// System.out.println();
				// System.out.println("Banner Added " + banner.getUrl());
				_result.add(banner);
				// System.out.println();
			}
		} // startElement

		/**
		 * Given a version string, say: 4.*.* 4.13.* 4.13.1
		 * 
		 * It will compare the current version against the given string. If our
		 * version is a match for the given version expression this function
		 * will return true
		 * 
		 * @param version
		 * @return
		 */

		public boolean frostWireMatchesVersion(String version) {
			String currentVersion = "4.17.5"; // CommonUtils.getFrostWireVersion();

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

	} // SponsorLoader - XML parser inner class

	public SponsorBanner() {
	};

	public SponsorBanner(String url, String imageSrc, int width, int height,
			int duration, Set<String> ipRanges, String type,
			HashMap<String, String> countries, String language, String version) {
		setUrl(url);
		setImageSrc(imageSrc);
		setWidth(width);
		setHeight(height);
		setDuration(duration);
		setIpRanges(ipRanges);
		setVersion(version);

		setType(type);

		if (countries != null) {
			setCountries(countries);
		}

		if (language != null) {
			setLanguage(language);
		}
		
		//Get the ImageSrc from the server if it's not available locally as a file://
		//in the image cache.
		URL remoteImageURL = null;
		try {
			remoteImageURL = new URL(getImageSrc());
		} catch (Exception e) {
			
		}
		
		ImageCache.getInstance().getImage(remoteImageURL, new OnLoadedListener() {

			@Override
			public void onLoaded(URL url, BufferedImage image) {
				URL cachedFileURL = ImageCache.getInstance().getCachedFileURL(url);
				SponsorBanner.this.setLocalImageURL(cachedFileURL);
				SponsorBanner.this.onImageLoaded();
			}
			
		});

	}

	public void onImageLoaded() {
		setText("<html><img src=\"" + getLocalImageSrc() + "\" width=\""
				+ getWidth() + "\" height=\"" + getHeight()
				+ "\" border=\"0\"/></html>");
		setSize(new Dimension(getWidth(), getHeight()));

		addMouseListener(new MouseAdapter() {
			// THIS IS WHAT HAPPENS WHEN THEY CLICK ON THE BANNER
			public void mouseClicked(MouseEvent evt) {
				// depending on banner type if set, we do something else,
				// maybe start a download on the network.

				if (getType() == null || getType().equals("web")) {
					GUIMediator.openURL(getUrl());
					return;
				}

				if (getType().equals("torrent")) {
					// System.out.println("SponsorBanner.mouseClicked: Should open a torrent.");
					String urlString = getUrl();

					try {
						java.net.URI uri = new java.net.URI(urlString);

						String scheme = uri.getScheme();
						if (scheme == null || !scheme.equalsIgnoreCase("http")) {
							// System.out.println("Not a torrent URL");
							return;
						}

						String authority = uri.getAuthority();
						if (authority == null || authority.equals("")
								|| authority.indexOf(' ') != -1) {
							// System.out.println("Invalid authority");
							return;
						}

						GUIMediator.instance().openTorrentURI(uri);
					} catch (Exception e) {
						System.out.println(e);
					}
					return;
				}
			} // mouseClicked

			public void mouseEntered(MouseEvent evt) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			} // mouseEntered

			public void mouseExited(MouseEvent evt) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			} // mouseExited
		});		
	}
	
	private String url;
	private String imageSrc;
	private String localImageSrc;
	private int width;
	private int height;
	private int duration;
	private Set<String> ipRanges = null;
	private String type = null;
	private HashMap<String, String> countries = null;
	private String language = null;
	private String locale = null;
	private String version = null;

	public String getUrl() {
		return url;
	}

	public String getImageSrc() {
		return imageSrc;
	}

	public int getWidth() {
		if (width <= 0)
			return 120;
		return width;
	}

	public int getHeight() {
		if (height <= 0)
			return 600;
		return height;
	}

	public int getDuration() {
		if (duration <= 0)
			return 300;
		return duration;
	}

	public Set<String> getIpRanges() {
		return ipRanges;
	}

	public String getVersion() {
		return version;
	}

	/** Could be used to have a different click handler on the banner */
	public String getType() {
		return type;
	}

	public String getBannerLocale() {
		return locale;
	}

	public void addCountry(String country) {
		if (country == null)
			return;

		if (countries == null)
			countries = new HashMap<String, String>();

		countries.put(country, country);
	}

	public boolean hasCountry(String country) {
		if (countries == null || country == null)
			return false;
		return countries.containsKey(country);
	}

	public void setCountries(HashMap<String, String> countriesMap) {
		countries = countriesMap;
	}

	public HashMap<String, String> getCountries() {
		return countries;
	}

	public String getLanguage() {
		return language;
	}

	public String getSystemCountry() {
		String country = System.getProperty("user.country");
		if (country == null)
			country = "";
		return country;
	}

	public String getSystemLanguage() {
		// String language = System.getProperty("user.language");
		if (language == null)
			language = "";
		return language;
	}

	public void setUrl(String u) {
		url = u;
	}

	public void setImageSrc(String src) {
		imageSrc = src;
	}
	
	public void setLocalImageURL(URL localURL) {
		localImageSrc = localURL.toString();
	}
	
	public String getLocalImageSrc() {
		return localImageSrc;
	}

	public void setWidth(int w) {
		width = w;
	}

	public void setHeight(int h) {
		height = h;
	}

	public void setDuration(int d) {
		duration = d;
	}

	public void setIpRanges(Set<String> ipRange) {
		if (ipRange != null && ipRange.size() == 0) {
			ipRange = null;
		}
		ipRanges = ipRange;
	}

	public void setVersion(String v) {
		version = v;
	}

	/**
	 * expected mime types are:
	 * 
	 * - null | webpage -> To open a web browser with the URL - torrent -> To
	 * open and download the file specified by the torrent URL
	 */
	public void setType(String t) {

		if (t != null) {
			t = t.toLowerCase();
		}

		type = t;
	}

	public void setLanguage(String lang) {
		language = lang;
	}

	public void setBannerLocale(String l) {
		locale = l;
	}
}