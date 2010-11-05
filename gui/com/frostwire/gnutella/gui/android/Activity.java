package com.frostwire.gnutella.gui.android;

public abstract class Activity implements Runnable {

	public static final int ACTION_BROWSE = 0;
	
	public static final String DEVICE = "device";
	
	public static final String DESKTOP = "desktop";
	
	private int _action;
	
	private int _progress;
	
	private String _source;
	
	private String _target;
	
	private boolean _canceled;

	public Activity(int action, String source, String target) {
		_action = action;
		_source = source;
		_target = target;
	}
	
	public int getAction() {
		return _action;
	}
	
	public int getProgress() {
		return _progress;
	}
	
	protected void setProgress(int progress) {
		_progress = progress;
	}
	
	public String getSource() {
		return _source;
	}
	
	public String getTarget() {
		return _target;
	}
	
	public boolean isCanceled() {
		return _canceled;
	}
	
	public void cancel() {
		_canceled = true;
	}
}
