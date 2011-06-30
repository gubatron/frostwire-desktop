package com.frostwire;

public interface HttpFetcherListener {
	public void onError(Exception e);
	
	public void onSuccess(byte[] body);
}