package com.honestwalker.android.APICore.API;

public interface APIListener<T> {

	/** 发送请求给服务端 */
	public void onStart();
	
	/** 链接成功时的逻辑 */
	public void onComplete(T t);
	
	/** 链接失败时逻辑 */
	public void onFail(ApiException e);
	
}
