package com.demo.gcmlib;

import android.content.Context;

interface CDMObserver{
	public void onError(Context context, String errorType);
	public void onMessage(Context context, String json);
	public void onRegistered(Context context, String regId);
	public void onUnregistered(Context context, String regId);
}