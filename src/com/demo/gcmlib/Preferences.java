package com.demo.gcmlib;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

class Preferences {
	private static String TAG = "GCMLib Preferences";
	private static String PREFS_NAME = "GCMLib_prefs";
	
	private SharedPreferences prefs;
	
	private static Preferences ourInstance;
	
	
	private Preferences(Context context){
		prefs = context.getSharedPreferences(PREFS_NAME, 0);
	}
	
	/*
	 * Call it once before using this Preferences object via getInstance() method
	 */
	public static void initializeInstance(Context context){
		if(ourInstance==null)
			ourInstance = new Preferences(context);
	}
	
	public static Preferences getInstance(){
		return ourInstance;
	}
	
	public String getValue(String key){
		return prefs.getString(key, null);
	}
	
	public String getValue(String key, String defaultValue){
		return prefs.getString(key, defaultValue);
	}
	
	public void putValue(String key, String value){
		prefs.edit().putString(key, value).commit();
	}
	
	public void putValuesFromJson(String json){
		SharedPreferences.Editor editor = prefs.edit();
		
		try {
			JSONObject jsonObj = new JSONObject(json);
			Iterator<String> it = jsonObj.keys();
			
			while(it.hasNext()){
				String key = it.next();
				String value = jsonObj.getString(key);
				
				Log.d(TAG, "Putting pair "+key+" : "+value);
				
				editor.putString(key, value);
			}
			
		} catch (JSONException e) {
			Log.e(TAG, "Error while parsing json");
			e.printStackTrace();
		}finally{
			editor.commit();
		}
	}
}
