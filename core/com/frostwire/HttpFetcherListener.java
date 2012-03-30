package com.frostwire;

public interface HttpFetcherListener {
	public void onError(Throwable e);
	
	public void onSuccess(byte[] body);
}