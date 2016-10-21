package com.honestwalker.android.APICore.API.resp;

import java.io.Serializable;

public class BaseResp<T> implements Serializable {

	private String result;
	private String code;
	
	private String json;

	private String errcode;
	private String errmsg;
	
	private T info;
	
	private Object reqTag;
	
	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o == null) {
			return false;
		}
		
		BaseResp descBaseResp;
		try {
			descBaseResp = (BaseResp)o;
		} catch (Exception e) {
			return false;
		}
		
		String errCode1 = ( code == null?"":code );
		String errCode2 = ( descBaseResp.getCode() == null ? "" : descBaseResp.getCode() );
		if(!errCode1.equals(errCode2)) {
			return false;
		}
		
		String result1 = ( result == null?"":result );
		String result2 = ( descBaseResp.getResult() == null ? "" : descBaseResp.getResult() );
		if(!result1.equals(result2)) {
			return false;
		}
		
		String json1 = ( json == null?"":json );
		String json2 = ( descBaseResp.getJson() == null ? "" : descBaseResp.getJson() );
		if(!json1.equals(json2)) {
			return false;
		}
		
		return true;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public T getInfo() {
		return info;
	}

	public void setInfo(T info) {
		this.info = info;
	}

	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public Object getReqTag() {
		return reqTag;
	}

	public void setReqTag(Object reqTag) {
		this.reqTag = reqTag;
	}

}
