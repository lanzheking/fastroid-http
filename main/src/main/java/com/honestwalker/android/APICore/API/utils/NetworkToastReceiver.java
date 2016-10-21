package com.honestwalker.android.APICore.API.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkToastReceiver extends BroadcastReceiver {

    private NETWORK_TOAST_TYPE type;

    public NetworkToastReceiver(NetworkToastManager networkToastManager , NETWORK_TOAST_TYPE type) {
        this.type = type;
    }

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		NetworkToastManager.alertAction(type);
	}

}
