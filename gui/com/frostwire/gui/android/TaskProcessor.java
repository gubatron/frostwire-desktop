package com.frostwire.gui.android;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskProcessor {
	
	private BlockingQueue<Task> _queue;
	
	public TaskProcessor() {
		_queue = new LinkedBlockingQueue<Task>();
	}
	
	public void start() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					while (true) {
	
						Task task = _queue.poll(1, TimeUnit.SECONDS); //this will wait if queue is empty
	
						if (task != null) {
							try {
								task.run();
							} catch (Exception e) {
								System.out.println("Error ejecuting task " + task + ", error=" + e.getMessage());
							}
						}
					}
				} catch (Exception e) {
					System.out.println("Error processing device integration activities: " + e.getMessage());
				}
			}
		}).start();
	}

	public void addTask(Task task) {
	    if (task.enqueue()) {
	        _queue.add(task);
	    } else {
	        new Thread(task).start();
	    }
	}
}
