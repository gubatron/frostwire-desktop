package com.frostwire.gnutella.gui.android;

import java.util.ArrayList;
import java.util.List;

public abstract class Task implements Runnable {

	private List<OnChangedListener> _listeners;
	
	private int _progress;
	
	private boolean _canceled;
	
	private boolean _failed;
	
	private Exception _failException;

	public Task() {
	    _listeners = new ArrayList<Task.OnChangedListener>();
	}
	
	public List<OnChangedListener> getOnChangedListeners() {
		return _listeners;
	}
	
	public void addOnChangedListener(OnChangedListener listener) {
		_listeners.add(listener);
	}
		
	public int getProgress() {
		return _progress;
	}
	
	public void setProgress(int progress) {
		
		// cast progress to [0..100]
	    if (progress < 0 || progress > 100) {
	        System.out.println("que paso");
	    }
		progress = (progress < 0) ? 0 : progress;
		progress = (progress > 100) ? 100 : progress;
		
		_progress = progress;
		notifyOnChanged();
	}
	
	public boolean isCanceled() {
		return _canceled;
	}
	
	public boolean isRunning() {
	    return !isCanceled() && !isFailed() && getProgress() != 100;
	}
	
	public void cancel() {
		if (getProgress() == 100) {
			return;
		}
		
		_canceled = true;
		notifyOnChanged();
	}
	
	public boolean isFailed() {
		return _failed;
	}
	
	public Exception getFailException() {
		return _failException;
	}
	
	public void fail(Exception e) {
		if (getProgress() == 100) {
			return;
		}
		
		_failed = true;
		_failException = e;
		notifyOnChanged();
	}
	
	public boolean enqueue() {
	    return true;
	}
	
	protected void notifyOnChanged() {
	    for (int i = 0; i < _listeners.size(); i++) { // no thread safe
	        OnChangedListener listener = _listeners.get(i);
	        if (listener != null) {
	            listener.onChanged(this);
	        }
	    }
	}
	
	public interface OnChangedListener {
		public void onChanged(Task task);
	}
}
