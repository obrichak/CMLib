package com.demo.gcmlib;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazon.device.messaging.ADMMessageHandlerBase;

public class ADMMessageHandler extends ADMMessageHandlerBase{
	private static final String TAG = "ADMMessageHandler";
	private static final String MSG_JSON_KEY = "json";
	
	private static ArrayList<CDMObserver> observers = new ArrayList<CDMObserver>();
	
	public ADMMessageHandler() {
		super("ADMHandler");
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

	@Override
	protected void onMessage(Intent message) {
		Log.d(TAG, "onMessage");
		
		Bundle extras = message.getExtras();
		if(!extras.containsKey(MSG_JSON_KEY)){
			System.out.println("No JSON extra in the message");
			return;
		}
		
		String json = extras.getString(MSG_JSON_KEY);
		Preferences.getInstance().putValuesFromJson(json);
		
		for(CDMObserver observer : observers){
			observer.onMessage(null, json);
		}
	}

	@Override
	protected void onRegistered(String regId) {
		Log.d(TAG, "Registered with regId "+regId);
		
		for(CDMObserver observer : observers){
			observer.onRegistered(null, regId);
		}
	}

	@Override
	protected void onRegistrationError(String reason) {
		Log.e(TAG, "Error: "+reason);
		
		for(CDMObserver observer : observers){
			observer.onError(null, reason);
		}
	}

	@Override
	protected void onUnregistered(String regId) {
		Log.d(TAG, "Unregistered regId "+regId);
		
		for(CDMObserver observer : observers){
			observer.onUnregistered(null, regId);
		}
	}

}
