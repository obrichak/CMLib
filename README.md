CMLib
==========

cloud messaging lib - handle cloud messaging for Amazon/Android devices (but using deprecated GCMBaseIntentService class)

Insert following lines into your project's AndroidManifest.xml to use the library:

<application>
....

    <receiver android:name="com.tatem.gcmlib.GCMReceiver" android:permission="com.google.android.c2dm.permission.SEND" >
  		<intent-filter>
    		<action android:name="com.google.android.c2dm.intent.RECEIVE" />
    		<action android:name="com.google.android.c2dm.intent.REGISTRATION" />
    		<category android:name="<your_application_package_name>"/>
  		</intent-filter>
	</receiver>
        
	<service android:name="com.tatem.gcmlib.GCMIntentService"/>

....
</application>

<permission android:name="<your_application_package_name>.permission.C2D_MESSAGE" android:protectionLevel="signature" />
<uses-permission android:name="<your_application_package_name>.permission.C2D_MESSAGE" />
	
<!-- App receives GCM messages. -->
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
<!-- GCM connects to Google Services. -->
<uses-permission android:name="android.permission.INTERNET" /> 
<!-- GCM requires a Google account. -->
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<!-- Keeps the processor from sleeping when a message is received. -->
<uses-permission android:name="android.permission.WAKE_LOCK" />