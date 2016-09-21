package com.honestwalker.android.APICore.API.net.cookie;

import android.content.Context;

import com.honestwalker.androidutils.IO.LogCat;
import com.honestwalker.androidutils.IO.ObjectStreamIO;
import com.honestwalker.androidutils.IO.SharedPreferencesLoader;
import com.honestwalker.androidutils.StringUtil;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CookieManager {
	
	public static final String cookieFileName = "COOKIE";
	
	/**
	 * 保存cookie
	 * @param context
	 * @param httpClient
	 * @throws java.io.IOException 存储cookie失败时回调
	 */
	public synchronized static void saveCookie(Context context , DefaultHttpClient httpClient) throws IOException {
		
		List<Cookie> cookies = getCookies(httpClient);
		
//		ArrayList<CookieSerializable> cookieSerializables = new ArrayList<CookieSerializable>();
		HashMap<String, String> cookieMap = new HashMap<String , String>();
		
		if(cookies != null && cookies.size() > 0) {

//			for(Cookie cookie : cookies) {
////				cookieSerializables.add(new CookieSerializable(cookie.getName(), cookie.getValue()));
//				cookieMap.put(cookie.getName(), cookie.getValue());
////				cookieMap.put("domain", cookie.getDomain());
////				cookieMap.put("path", cookie.getPath());
////				Date date = cookie.getExpiryDate();
////				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
////				LogCat.d("cookie", "================>" + sdf.format(date));
//			}
//			ObjectStreamIO.output(context.getCacheDir().toString(), cookieMap, cookieFileName);
			String cookie = getHttpClientCookie(httpClient);

			SharedPreferencesLoader.getInstance(context).putString("cookie", cookie);

			LogCat.d("REQUEST", "\r\n[SAVE COOKIE]:" + cookie + "\r\n");
		} else {
			LogCat.d("REQUEST", "cookie 为 空 不保存");
		}

	}
	
	/**
	 * 清除本地Request的cookie
	 * @param context
	 */
	public static void clearCookie(Context context) {
		try {
			ObjectStreamIO.output(context.getCacheDir().toString(), null, cookieFileName);
		} catch (IOException e) {
		}
	}
	
	/**
	 * 获取本地cookie
	 * @param context
	 * @return
	 */
	public synchronized static String getLocalCookie(Context context) {
//		try {
//			HashMap<String, String> cookieMap = (HashMap<String, String>) ObjectStreamIO.input(context.getCacheDir().toString(), cookieFileName);
//			Iterator<Map.Entry<String, String>> iter = cookieMap.entrySet().iterator();
//			StringBuffer cookieStr = new StringBuffer();
//			while(iter.hasNext()) {
//				Map.Entry<String,String> ent = iter.next();
//				cookieStr.append(ent.getKey() + "=" + ent.getValue() + ";");
//			}
//			LogCat.d("COOKIE", cookieStr.toString());
//			return cookieStr.toString();
//		} catch (Exception e) {
//		}
		String cookie = SharedPreferencesLoader.getInstance(context).getString("cookie");
		if(!StringUtil.isEmptyOrNull(cookie)) {
			return cookie;
		}
		LogCat.d("COOKIE", "本地cookie不存在。");
		return "";
	}
	
	/**
	 * 获取cookie字符串 主要用于输出
	 * @param httpClient
	 * @return
	 */
	public static String getHttpClientCookie(DefaultHttpClient httpClient) {
		StringBuffer sb = new StringBuffer();
		{	// cookie
			CookieStore cookieStore = httpClient.getCookieStore();
			List<Cookie> cookies = cookieStore.getCookies();
			if(cookies != null && cookies.size() > 0) {
				for(Cookie cookie : cookies) {
					sb.append(cookie.getName() + "=" + cookie.getValue() + ";");
//					sb.append("domain=").append(cookie.getDomain()+";");
//					sb.append("path=").append(cookie.getPath()+";");
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * 为httpClient设置cookie
	 * @param context
	 * @param httpRequest
	 * @return
	 */
	public synchronized static String setCookie(Context context , HttpEntityEnclosingRequestBase httpRequest) {

		String cookie = SharedPreferencesLoader.getInstance(context).getString("cookie");
		if(!StringUtil.isEmptyOrNull(cookie)) {
			httpRequest.addHeader("Cookie", cookie);
			return cookie;
		}

//		try {
//			HashMap<String, String> cookieMap = (HashMap<String, String>) ObjectStreamIO.input(context.getCacheDir().toString(), cookieFileName);
//			if(cookieMap == null) {
//				return null;
//			}
////			CookieStore cookieStore = httpClient.getCookieStore();
////			cookieStore.clear();
//
//			Iterator<Map.Entry<String, String>> iter = cookieMap.entrySet().iterator();
//			StringBuffer cookieStr = new StringBuffer();
//			while(iter.hasNext()) {
//				Map.Entry<String,String> ent = iter.next();
//				cookieStr.append(ent.getKey() + "=" + ent.getValue() + ";");
//			}
//
//			httpRequest.addHeader("Cookie", cookieStr.toString());
//
//			LogCat.d("REQUEST", "\r\n[SET COOKIE]" + cookieStr.toString());
//
//			return cookieStr.toString();
//		} catch (Exception e) {
//		}
		return "";
	}
	
	private synchronized static String showCookiesInCookieStore(CookieStore cookieStore) {
		StringBuffer cookieSB = new StringBuffer();
		for(Cookie c : cookieStore.getCookies()) {
//			LogCat.d("COOKIE", c.getName() + "=" + c.getValue() + "  ");
			cookieSB.append(c.getName() + "=" + c.getValue() + ";");
		}
		return cookieSB.toString();
	}
	
	/**
	 * 获取当前httpClient cookie
	 * @param httpClient
	 * @return
	 */
	private synchronized static List<Cookie> getCookies(DefaultHttpClient httpClient) {
		LogCat.d("COOKIE", "httpclient.getcookies == 0 "+ httpClient.getCookieStore().getCookies().size());
		return httpClient.getCookieStore().getCookies();
	}
	
}
