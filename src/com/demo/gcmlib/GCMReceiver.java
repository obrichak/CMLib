package com.demo.gcmlib;

import android.content.Context;
import android.util.Log;

import com.google.android.gcm.GCMBroadcastReceiver;

public class GCMReceiver extends GCMBroadcastReceiver {
	private static final String TAG = "GCMBroadcastReceiver";
	
	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		String className = GCMIntentService.class.getName();
		Log.d(TAG, "Returning class name "+className);
		return className;
	}
}
