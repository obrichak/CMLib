package com.demo.gcmlib;

import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

class HttpRequest {
	private static final String TAG = "HttpRequest";
	
	private String url;
	private Map<String, String> urlParams;
	
	private static DefaultHttpClient httpClient;
	
	public HttpRequest(String url, Map<String, String> params, boolean isSecure){
		this.url = url;
		this.urlParams = params;
		
		if(httpClient == null) {
			if(!isSecure) {
				httpClient = new DefaultHttpClient();
			} else 
			{
				SSLContext ctx;
				try {
					ctx = SSLContext.getInstance("TLS");

			        ctx.init(null, new TrustManager[] { new CustomX509TrustManager() },
			                new SecureRandom());
	
			        HttpClient client = new DefaultHttpClient();
	
			        SSLSocketFactory ssf = new CustomSSLSocketFactory(ctx);
			        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			        ClientConnectionManager ccm = client.getConnectionManager();
			        SchemeRegistry sr = ccm.getSchemeRegistry();
			        sr.register(new Scheme("https", ssf, 443));
			        
			        httpClient = new DefaultHttpClient(ccm,
			                client.getParams());
			        
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "NoSuchAlgorithmException!");
					e.printStackTrace();
				} catch (KeyManagementException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "KeyManagementException!");
					e.printStackTrace();
				} catch (KeyStoreException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "KeyStoreException!");
					e.printStackTrace();
				} catch (UnrecoverableKeyException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "UnrecoverableKeyException!");
					e.printStackTrace();
					
				}
			}
		}

	}

	void execute(final int maxRetries){
		final HttpPost request = new HttpPost(url);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(); 
		for(String key : urlParams.keySet()){
			params.add(new BasicNameValuePair(key, urlParams.get(key)));
		}
		
		try {
			request.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unsupported encoding!");
			e.printStackTrace();
			return;
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				int nRetries = maxRetries;
				Log.d(TAG, "Trying to send request with "+nRetries+" retries max.");
				
				while(nRetries>=0){
					Log.d(TAG, nRetries+" retries. Executing...");
					try{
						httpClient.execute(request);
						Log.d(TAG, "Aborting...");
						request.abort();
						Log.d(TAG, "Request executed successfully");
						break;
					}catch(Exception e){
						Log.e(TAG, "Error while executing request");
						e.printStackTrace();
						nRetries--;
						continue;
					}
				}
			}
		}).start();
	}
}
