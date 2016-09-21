package com.honestwalker.android.APICore.API.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.example.hkyy.fastroid_http.R;
import com.honestwalker.androidutils.UIHandler;

public class NetworkToastManager {

	private NetworkToastManager() {
	}

	/** 网络超时提示 */
	private static NetworkToastManager	TIMEOUT;
	/** 无网络链接提示 */
	private static NetworkToastManager	UNCONNECTED;

	private static Toast	timeoutToast;
	private static Toast	unconnectedToast;

	private static boolean	timeoutToastIsShowing		= false;
	private static boolean	unconnectedToastIsShowing	= false;

	/** 对话框显示时间 */
	private static long		showTime					= 3000;
	
	private static Activity mainActivity;
	
	public static void regist(Activity activity) {
		mainActivity = activity;

		if (TIMEOUT == null) {
			TIMEOUT = new NetworkToastManager();
			registerReceiver(activity, ACTION_NETWORK_TIMEOUT, new NetworkToastReceiver(TIMEOUT , NETWORK_TOAST_TYPE.TIMEOUT));
		}

		if (UNCONNECTED == null) {
			UNCONNECTED = new NetworkToastManager();
			registerReceiver(activity, ACTION_NETWORK_UNCONNECTED, new NetworkToastReceiver(UNCONNECTED , NETWORK_TOAST_TYPE.UNCONNECTED));
		}

		buildToast(activity);

	}

	private static void buildToast(Activity activity) {
		timeoutToast = Toast.makeText(activity, "请求超时，稍后重试", Toast.LENGTH_LONG);
		unconnectedToast = Toast.makeText(activity, "无法连接网络", Toast.LENGTH_LONG);
	}

	public synchronized static void alert(final NETWORK_TOAST_TYPE type) {
		if(NETWORK_TOAST_TYPE.TIMEOUT.equals(type)) {
			sendBroadcast(mainActivity, ACTION_NETWORK_TIMEOUT);
		} else if(NETWORK_TOAST_TYPE.UNCONNECTED.equals(type)) {
			sendBroadcast(mainActivity, ACTION_NETWORK_UNCONNECTED);
		}
	}

	static void alertAction(final NETWORK_TOAST_TYPE type) {
		
		if (type == null) return;

		Toast toast = null;
		boolean isShowing = false;

		if (NETWORK_TOAST_TYPE.TIMEOUT.equals(type)) {
			toast = timeoutToast;
			isShowing = timeoutToastIsShowing;
		} else if (NETWORK_TOAST_TYPE.UNCONNECTED.equals(type)) {
			toast = unconnectedToast;
			isShowing = unconnectedToastIsShowing;
		}

		// toast框正在显示 不提时
		if (toast == null || isShowing) return;

		if (NETWORK_TOAST_TYPE.TIMEOUT.equals(type)) {
			timeoutToastIsShowing = true;
		} else if (NETWORK_TOAST_TYPE.UNCONNECTED.equals(type)) {
			unconnectedToastIsShowing = true;
		}

		showToast(toast);

		UIHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (NETWORK_TOAST_TYPE.TIMEOUT.equals(type)) {
					timeoutToastIsShowing = false;
				} else if (NETWORK_TOAST_TYPE.UNCONNECTED.equals(type)) {
					unconnectedToastIsShowing = false;
				}
			}
		}, showTime);
	}

	/**
	 * 显示Toast
	 * @param toast
	 */
	private static void showToast(final Toast toast) {
		UIHandler.post(new Runnable() {
			@Override
			public void run() {
				toast.show();
			}
		});
	}

	static final String ACTION_NETWORK_TIMEOUT = "ACTION_NETWORK_TIMEOUT";
	static final String ACTION_NETWORK_UNCONNECTED = "ACTION_NETWORK_UNCONNECTED";
	
	public static void registerReceiver(Context context, String action,
			BroadcastReceiver receiver) {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(R.class.getPackage().getName()
				+ action.toString());
		context.registerReceiver(receiver, myIntentFilter);
	}
	
	public static void sendBroadcast(Context context, String action) {
		Intent intent = new Intent(R.class.getPackage().getName()
					+ action.toString());
		if (context != null) {
			context.sendBroadcast(intent);
		}
	}
}
