package com.demo.gcmlib;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService{
	private static final String TAG = "GCMIntentService";
	private static final String MSG_JSON_KEY = "json";
	
	private static ArrayList<CDMObserver> observers = new ArrayList<CDMObserver>();
	private static String senderIds = "<your_sender_Ids_goes_here>";//"211007640597";
	
	public GCMIntentService(){
		super(senderIds);
	}
	
	public static void addObserver(CDMObserver observer){
		observers.add(observer);
	}

	public static void removeObserver(CDMObserver observer){
		observers.remove(observer);
	}
	
	public static void removeAllObservers(){
		observers.clear();
	}
	
	public static void setSenderIds(String senderIds){
		GCMIntentService.senderIds = senderIds;
	}
	
	@Override
	protected void onError(Context context, String errorType) {
		Log.e(TAG, "Error: "+errorType);
		
		for(CDMObserver observer : observers){
			observer.onError(context, errorType);
		}
	}
	
	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d(TAG, "onMessage");
		
		String json = intent.getExtras().getString(MSG_JSON_KEY);
		if(json==null){
			Log.e(TAG, "Message doesn't contain JSON text");
			return;
		}
		
		Preferences.initializeInstance(context);
		Preferences.getInstance().putValuesFromJson(json);
		
		for(CDMObserver observer : observers){
			observer.onMessage(context, json);
		}
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		Log.d(TAG, "Registered with regID "+regId);
		
		for(CDMObserver observer : observers){
			observer.onRegistered(context, regId);
		}
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		Log.d(TAG, "Unregistered regId "+regId);
		
		for(CDMObserver observer : observers){
			observer.onUnregistered(context, regId);
		}
	}
}
