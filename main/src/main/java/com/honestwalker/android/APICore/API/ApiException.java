package com.honestwalker.android.APICore.API;

import com.honestwalker.android.APICore.API.resp.BaseResp;


/**
 * Api请求自定义异常
 * @author honestwalker
 *
 */
public class ApiException extends Exception {
	
	private BaseResp baseResp;
	
	public ApiException() {}
	
	public ApiException(BaseResp baseResp){
		this.baseResp = baseResp;
	}
	
	public ApiException(String msg) {
		super(msg);
	}
	
	public ApiException(BaseResp baseResp , String errDesc){
		super(errDesc);
		this.baseResp = baseResp;
	}
	
	public BaseResp getBaseResp() {
		return baseResp;
	}
	
}
