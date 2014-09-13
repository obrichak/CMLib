package com.demo.gcmlib;

import com.amazon.device.messaging.ADMMessageReceiver;

public class ADMReceiver extends ADMMessageReceiver{
	public ADMReceiver() {
		super(ADMMessageHandler.class);
	}
}
