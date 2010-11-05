package com.frostwire.gnutella.gui.android;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ActivityProcessor {
	
	private BlockingQueue<Activity> _queue;
	
	public ActivityProcessor() {
		_queue = new LinkedBlockingQueue<Activity>();
	}
	
	public void start() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					while (true) {
	
						Activity activity = _queue.poll(1, TimeUnit.SECONDS); //this will wait if queue is empty
	
						if (activity != null) {
							try {
								activity.run();
							} catch (Exception e) {
								System.out.println("Error ejecuting activity " + activity + ", error=" + e.getMessage());
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void add(Activity activity) {
		_queue.add(activity);
	}
}
