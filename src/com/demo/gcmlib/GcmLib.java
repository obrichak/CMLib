package com.demo.gcmlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.amazon.device.messaging.ADM;
import com.amazon.device.messaging.development.ADMManifest;
import com.google.android.gcm.GCMRegistrar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public class GcmLib {
	public enum Provider{
		GOOGLE,
		AMAZON
	}
	
	private static final String TAG = "GCMLib";
	private static final String SERVER_APP_NAME = "gcmAdmin";
	private static final String REGISTER_URL = "register";
	private static final String UNREGISTER_URL = "unregister";
	private static final String PARAM_REGISTRATION_ID = "regid";
	private static final String PARAM_DEVICE_NAME = "name";
	private static final String PARAM_PROJECT_ID = "projectid";
	private static final String PARAM_PROVIDER = "provider";
	private static final int MAX_REQUEST_RETRIES = 3;
	
	private static GcmLib ourInstance;
	
	private Provider provider;
	
	private String serverAddress;
	private int serverPort; 
	private long projectId;
	private String senderId;
	
	private String serverUrl;
	
	private Context context; 
	private Preferences prefs;
	
	private ArrayList<GcmLibObserver> observers;
	
	private static boolean GCM_SUPPORTED = true;
	
	private ADM adm;
	
	private GcmLib(){
		observers = new ArrayList<GcmLib.GcmLibObserver>();
	}
	
	public static GcmLib getInstance(){
		if(ourInstance==null)
			ourInstance = new GcmLib();
		return ourInstance;
	}
	
	public void addObserver(GcmLibObserver observer){
		observers.add(observer);
	}
	
	public void removeObserver(GcmLibObserver observer){
		observers.remove(observer);
	}
	
	/*
	 * Call it in Activity's onDestroy method
	 */
	public void removeAllObservers(){
		observers.clear();
		
		if(!GCM_SUPPORTED)
			return;
		
		if(provider==Provider.GOOGLE)
			GCMIntentService.removeAllObservers();
		else if(provider==Provider.AMAZON)
			ADMMessageHandler.removeAllObservers();
	}
	
	/**
	 * Init a GcmLib instance with everything it needs to know.
	 * @param provider Cloud messaging api provider. Could be GOOGLE or AMAZON.
	 * @param senderId Sender number which you've got from your Google APIs console. Not used with Amazon provider.
	 * @param address Address of A 3'rd party server which runs GCM Control Panel application.
	 * @param port Port of the 3'rd party server. Pass 0 to use default value.
	 * @param ID of the project in the GCM Control Panel. Can be seen on the project's summary page.
	 */
	public void initialize(Context context, Provider provider, String senderId, String address, int port, long projectId){
		this.context = context;
		this.senderId = senderId;
		this.serverAddress = address;
		this.serverPort = port;
		this.projectId = projectId;
		this.provider = provider;
		
		Log.d(TAG, "Initializing with "+provider.name()+" provider");
		
		if(provider==Provider.GOOGLE)
			GCMIntentService.setSenderIds(senderId);
		
		StringBuilder serverUrlBuilder = new StringBuilder("http://");
		serverUrlBuilder.append(serverAddress);
		if(serverPort>0)
			serverUrlBuilder.append(':').append(serverPort);
		serverUrlBuilder.append('/').append(SERVER_APP_NAME).append('/');
		serverUrl = serverUrlBuilder.toString();
		Log.d(TAG, "Server URL: "+serverUrl);
		
		Preferences.initializeInstance(context);
		prefs = Preferences.getInstance();
		
		switch(provider){
		case GOOGLE:
			initGcm();
			break;
			
		case AMAZON:
			boolean AdmAvailable = false;
			try{
				Class.forName( "com.amazon.device.messaging.ADM" );
				Log.d(TAG, "Class presence check passed");
				adm = new ADM(context);
				AdmAvailable = true;
			}catch(Exception e){
				e.printStackTrace();
			}
			if(!AdmAvailable){
				Log.d(TAG, "ADM Is not available on this device");
				GCM_SUPPORTED = false;
				return;
			}
			
			Log.d(TAG, "ADM supported");
			initAdm();
			break;
		}
	}
	
	private void initGcm(){
		try{
			Log.d(TAG, "Checking device...");
			GCMRegistrar.checkDevice(context);
			Log.d(TAG, "Checking manifest...");
			GCMRegistrar.checkManifest(context);
			String regId = GCMRegistrar.getRegistrationId(context);
			if(regId.equals("")){
				Log.d(TAG, "Registering for messages from sender ID "+senderId);
				GCMRegistrar.register(context, senderId);
			}else{
				Log.d(TAG, "Device is already registered with ID "+regId);
				registerOnServer(regId);
			}
		}catch(Exception e){
			GCM_SUPPORTED = false;
			Log.e(TAG, "GCM is not supported on this device");
			e.printStackTrace();
			return;
		}
		
		GCMIntentService.addObserver(new CDMObserver() {
			@Override
			public void onUnregistered(Context context, String regId) {
				unregisterOnServer(regId);
			}
			@Override
			public void onRegistered(Context context, String regId) {
				Log.d(TAG, "Registered with regId "+regId);
				registerOnServer(regId);
			}
			@Override
			public void onMessage(Context context, String json) {
				for (GcmLibObserver observer : observers)
					observer.onNewMessage(json);
			}
			@Override
			public void onError(Context context, String errorType) {}
		});
	}
	
	private void initAdm(){
//		if(adm.isSupported()){
//			Log.e(TAG, "ADM is not supported on this device");
//			return;
//		}
		
		String regId = adm.getRegistrationId();
		if(regId==null){
			Log.d(TAG, "Registering...");
			adm.startRegister();
		}else{
			Log.d(TAG, "Device already registered with regId "+regId);
			registerOnServer(regId);
		}
		
		ADMMessageHandler.addObserver(new CDMObserver() {
			@Override
			public void onUnregistered(Context context, String regId) {
				unregisterOnServer(regId);
			}
			
			@Override
			public void onRegistered(Context context, String regId) {
				Log.d(TAG, "Registered with regId "+regId);
				registerOnServer(regId);
			}
			
			@Override
			public void onMessage(Context context, String json) {
				for(GcmLibObserver observer : observers){
					observer.onNewMessage(json);
				}
			}
			
			@Override
			public void onError(Context context, String reason) {
				Log.e(TAG, "Error: "+reason);
				GCM_SUPPORTED = false;
			}
		});
	}
	
	public String getPreference(String key){
		return prefs.getValue(key);
	}
	
	public String getPreference(String key, String defaultValue){
		return prefs.getValue(key, defaultValue);
	}
	
	public void setPreference(String key, String value){
		prefs.putValue(key, value);
	}
	
	private void registerOnServer(String regId){
		String url = serverUrl+REGISTER_URL;
		Log.d(TAG, "URL: "+url);
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(PARAM_REGISTRATION_ID, regId);
		params.put(PARAM_PROJECT_ID, Long.toString(projectId));
		params.put(PARAM_DEVICE_NAME, getDeviceName());
		params.put(PARAM_PROVIDER, provider.name());
		
//		new HttpRequest(url, params).execute(MAX_REQUEST_RETRIES);
		new HttpRequest(url, params, false).execute(MAX_REQUEST_RETRIES);
	}
	
	private void unregisterOnServer(String regId){
		String url = serverUrl+UNREGISTER_URL;
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(PARAM_REGISTRATION_ID, regId);
		
		new HttpRequest(url, params, true).execute(MAX_REQUEST_RETRIES);
	}
	
	private String getDeviceName(){
		StringBuilder res = new StringBuilder();
		String accountName = "No account";
		
		Account[] accounts = AccountManager.get(context).getAccounts();
		for(Account account : accounts){
			accountName = account.name;
			if(account.type.equals("com.google"))
				break;
		}
		
		res.append(accountName).append(" on ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL);
		
		return res.toString();
	}
	
	public String getRegId(){
		if(provider == Provider.AMAZON)
			return adm.getRegistrationId();
		
		return GCMRegistrar.getRegistrationId(context);
	}
	
	public boolean supported(){
		return GCM_SUPPORTED;
	}
	
	public interface GcmLibObserver {
		public void onNewMessage(String json);
	}
}
