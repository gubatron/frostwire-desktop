package com.frostwire.gnutella.gui.android;

public abstract class Activity implements Runnable {

	private OnChangedListener _listener;
	
	private int _progress;
	
	private boolean _canceled;
	
	private boolean _failed;
	
	private Exception _failException;

	public Activity() {
	}
	
	public OnChangedListener getOnChangedListener() {
		return _listener;
	}
	
	public void setOnChangedListener(OnChangedListener listener) {
		_listener = listener;
	}
		
	public int getProgress() {
		return _progress;
	}
	
	public void setProgress(int progress) {
		
		// cast progress to [0..100]
		progress = (progress < 0) ? 0 : progress;
		progress = (progress > 100) ? 100 : progress;
		
		_progress = progress;
		fireOnChanged();
	}
	
	public boolean isCanceled() {
		return _canceled;
	}
	
	public void cancel() {
		if (getProgress() == 100) {
			return;
		}
		
		_canceled = true;
		fireOnChanged();
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
		fireOnChanged();
	}
	
	protected void fireOnChanged() {
		if (_listener != null) {
			_listener.onChanged(this);
		}
	}
	
	public interface OnChangedListener {

		public void onChanged(Activity activity);
	}
}
