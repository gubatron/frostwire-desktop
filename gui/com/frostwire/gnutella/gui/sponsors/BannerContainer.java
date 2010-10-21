package com.frostwire.gnutella.gui.sponsors;

import com.frostwire.gnutella.gui.sponsors.SponsorBanner;

import java.awt.CardLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JPanel;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Iterator;
import java.util.Calendar;

/**
 * This is the component that displays the banners that are specified on the frostwire servers.
 * This container is responsible for using a Timer to switch the banners depending on their duration.
*/		
public class BannerContainer extends JPanel {

    private CardLayout layout = null; //new CardLayout();
	private HashSet<SponsorBanner> banners;
	
    private Timer bannerSwitcher; //timer to switch banner one by one
    private Timer bannerRefresher; //timer to reload all banners from server
	private SponsorBanner currentBanner;
	
	private int DEFAULT_BANNER_REFRESH_RATE = 10; //secs
	
	private long lastTimeSwitched = 0;
	
	public BannerContainer() {
	    layout = null;
	    layout = new CardLayout();
	    setLayout(layout);
	    refreshBanners();
	    setupMouseListeners();
	}
	
	public BannerContainer(HashSet<SponsorBanner> banners) {
	    layout = null;
        layout = new CardLayout();
	    setLayout(layout);
	    setBanners(banners);
	    setupMouseListeners();
	    setupBannerRefreshTask(DEFAULT_BANNER_REFRESH_RATE);
	}

	/**
	 * Tells in between how many seconds the BannerContainer should reload all the banners from
	 * the server. Basically uses the bannerRefresher as a daemon timer which will invoke over and over
	 * a BannerRefreshTask.
	 * 
	 * If you pass -1 it will kill the current daemon if its running, or it will not start one at all
	 * if this is the first time.
	 * @param intervalInSeconds
	 */
	public void setupBannerRefreshTask(int intervalInSeconds) {
		if (intervalInSeconds == -1) {
			if (bannerRefresher != null) {
				bannerRefresher.cancel();
			}
			
			bannerRefresher = null;
			//System.out.println("BannerContainer.setupBannerRefreshTask() - No banner refresh for me.");
			return;
		}
		
		intervalInSeconds = intervalInSeconds * 1000;

		//instanciate timer if its not been done already
		if (bannerRefresher != null) {
			bannerRefresher.cancel();
			bannerRefresher = null;
		}

		//System.out.println("BannerContainer.setupBannerRefreshTask() - Refreshing every " + String.valueOf(intervalInSeconds)  + "secs");
		bannerRefresher = new Timer(true);		
		bannerRefresher.scheduleAtFixedRate(new BannersRefreshTask(this),intervalInSeconds,intervalInSeconds);
	} //setupBannerRefreshTask
	
	private void setupMouseListeners() {
	    //onclick, switch current banner
	    addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
			lastTimeSwitched = 0;
			switchBanner();
		    }
		});
	} //setupMouseListeners
	
	public void removeAllBanners() {
		//remove all SponsorBanner components if any and stop the timer.
		if (this.banners != null && getComponentCount() > 0) {
			//System.out.println("BannerContainer.setBanners -> Removing all banners");
			bannerSwitcher.cancel();
			removeAll();
		}
	}
	
	/** Removes all the existing banners if any, and will place the given ones */
	public void setBanners(HashSet<SponsorBanner> banners) {

	    this.banners = null;
		this.banners = banners;
		
		Iterator<SponsorBanner> iterator = this.banners.iterator();

		//System.out.println("BannerContainer.banners.size() -> " + this.banners.size());
		
 		if (bannerSwitcher != null) {
		    bannerSwitcher.cancel();
		    bannerSwitcher.purge();
		}

		bannerSwitcher = new Timer(true);
		
		while (iterator.hasNext()) {
			SponsorBanner banner = iterator.next();
			add(banner, banner.getImageSrc());
		}
		
		this.repaint();
		
		//Every 5 seconds have the Timer attempt to switch the current banner
		lastTimeSwitched = Calendar.getInstance().getTimeInMillis();
		bannerSwitcher.scheduleAtFixedRate(new BannerSwitchTask(this),0,5000);
	} //setBanners

		/** Private implementation of a TimerTask. this will basically invoke the SponsorBanner.switchBanner method every time
		 *  it's invoked by the timer.
		 * @author gubatron
		 *
		 */
		private class BannerSwitchTask extends TimerTask {
			private BannerContainer container;
			
			public BannerSwitchTask(BannerContainer container) {
				this.container = container; 
			}
			
			public void run() {
				this.container.switchBanner();
			}
		} //class BannerSwitchTask


	   /** Private implementation of a TimerTask. This one will reload all banners every 30 minutes
	   in case we updated banners.xml, this way the user doesn't have to restart FrostWire to see
	   new messages */
	   private class BannersRefreshTask extends TimerTask {
		   private BannerContainer container;
	
	       public BannersRefreshTask(BannerContainer container) {
	    	   this.container = container;
	       }
	
	       public void run() {
	    	   //System.out.println("BannerRefreshTask.run() - Reload banners from server");
	    	   this.container.refreshBanners();
	       }
	   } //class BannersRefreshTask
	
	/** Reloads the banners from the Server */
	public void refreshBanners() {
		//System.out.println("Removing previous banners...");
		removeAllBanners();
		//System.out.println("Getting banners from server...");
		setBanners((new SponsorBanner().getBannersFromServer(this)));
		//System.out.println("Refresh ended");
	} //refreshBanners
	
	/** Returns a reference to the currently shown banner.
	 * There was no method on the layout to do this, so we must ask each of the banners if they're being shown.
	 * @return
	 */
	public SponsorBanner getCurrentShownBanner() {
		Iterator<SponsorBanner> iterator = this.banners.iterator();
		
		while (iterator.hasNext()) {
			SponsorBanner banner = iterator.next();
			if (banner.isShowing()) {
				return banner;
			}
		}
		return null;
	} //getCurrentShownBanner
	
	/** this will switch to the next banner if the time of the current banner isn't due. */
	public void switchBanner() {
		SponsorBanner currentBanner = getCurrentShownBanner();
		
		if (currentBanner == null) {
			return;
		}
		
		long duration = ((long) currentBanner.getDuration()) * 1000;
		
		long now = Calendar.getInstance().getTimeInMillis();
		long timeOfExpiration = lastTimeSwitched + duration;
				
		if (now > timeOfExpiration) {
			lastTimeSwitched = now;
			layout.next(this);
			this.repaint();
		}
	} //switchBanner
}
