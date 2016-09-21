package com.honestwalker.android.APICore.API.net.cookie;

import java.io.Serializable;

public class CookieSerializable implements Serializable {
	
	private String name;
	private String value;
	
	public CookieSerializable(){}
	
	public CookieSerializable(String name , String value) {
		this.name  = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
