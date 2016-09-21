package com.honestwalker.android.APICore.API.net.cookie;

import android.content.Context;

import com.honestwalker.androidutils.IO.LogCat;
import com.honestwalker.androidutils.IO.ObjectStreamIO;
import com.honestwalker.androidutils.StringUtil;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ApiCookieManager {
	
	public static final String cookieFileName = "COOKIE";
	
	/**
	 * 保存cookie
	 * @param context
	 * @param httpClient
	 * @throws IOException 存储cookie失败时回调
	 */
	public synchronized static void saveCookie(Context context , DefaultHttpClient httpClient) throws IOException {
		
		List<Cookie> cookies = getCookies(httpClient);
		
//		ArrayList<CookieSerializable> cookieSerializables = new ArrayList<CookieSerializable>();
		HashMap<String, String> cookieMap = null;
		try {
			cookieMap = (HashMap<String, String>) ObjectStreamIO.input(context.getFilesDir().getAbsolutePath(), cookieFileName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//HashMap<String, String> cookieMap = new HashMap<String , String>();

		if(cookies != null && cookies.size() > 0) {
			for(Cookie cookie : cookies) {
//				cookieSerializables.add(new CookieSerializable(cookie.getName(), cookie.getValue()));
				cookieMap.put(cookie.getName(), cookie.getValue());

//				cookieMap.put("domain", cookie.getDomain());
//				cookieMap.put("path", cookie.getPath());
//				Date date = cookie.getExpiryDate();
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//				LogCat.d("cookie", "================>" + sdf.format(date));
			}
			ObjectStreamIO.output(context.getFilesDir().getAbsolutePath(), cookieMap, cookieFileName);
			//ObjectStreamIO.output(context.getCacheDir().toString(), cookieMap, cookieFileName);
			LogCat.d("REQUEST", "\r\n[SAVE COOKIE]:" + ApiCookieManager.getHttpClientCookie(httpClient) + "\r\n");
		} else {
			LogCat.d("REQUEST", "cookie 为 空 不保存");
		}

	}


	public synchronized static void saveCookie(Context context ,String string) throws IOException {

		if(StringUtil.isEmptyOrNull(string)){
			return;
		}
		String[] cookiearray ;
		cookiearray = string.split(";");

		//List<Cookie> cookies = getCookies(httpClient);

//		ArrayList<CookieSerializable> cookieSerializables = new ArrayList<CookieSerializable>();
		HashMap<String, String> cookieMap = null;
		try {
			cookieMap = (HashMap<String, String>) ObjectStreamIO.input(context.getFilesDir().getAbsolutePath(), cookieFileName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//HashMap<String, String> cookieMap = new HashMap<String , String>();

		for (int i = 0 ;i<cookiearray.length;i++){
			String cookieStr = cookiearray[i];
			int index = cookieStr.indexOf("=");
			if(index >= 0){
				cookieMap.put(cookieStr.substring(0, index), cookieStr.substring(index + 1));
			}

			//Log.i("YU", "cookiename" + cookieStr.substring(0, index) + " value " + cookieStr.substring(index + 1));

		}
		ObjectStreamIO.output(context.getFilesDir().getAbsolutePath(), cookieMap, cookieFileName);
		LogCat.i("apicookie","apicookie sync success");
		/*if(cookiearray != null && cookiearray.length > 0) {
			for(String cookie : cookiearray) {
//				cookieSerializables.add(new CookieSerializable(cookie.getName(), cookie.getValue()));
				//cookieMap.put(cookie.getName(), cookie.getValue());
//				cookieMap.put("domain", cookie.getDomain());
//				cookieMap.put("path", cookie.getPath());
//				Date date = cookie.getExpiryDate();
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//				LogCat.d("cookie", "================>" + sdf.format(date));
			}
			ObjectStreamIO.output(context.getCacheDir().toString(), cookieMap, cookieFileName);
			//LogCat.d("REQUEST", "\r\n[SAVE COOKIE]:" + ApiCookieManager.getHttpClientCookie(httpClient) + "\r\n");
		} else {
			LogCat.d("REQUEST", "cookie 为 空 不保存");
		}*/

	}


	/**
	 * 清除本地Request的cookie
	 * @param context
	 */
	public static void clearCookie(Context context) {
		try {
			ObjectStreamIO.output(context.getFilesDir().getAbsolutePath(), new HashMap<String, String>(), cookieFileName);
		} catch (IOException e) {
		}
	}
	
	/**
	 * 获取本地cookie
	 * @param context
	 * @return
	 */
	public synchronized static String getLocalCookie(Context context) {
		try {
			HashMap<String, String> cookieMap = (HashMap<String, String>) ObjectStreamIO.input(context.getFilesDir().getAbsolutePath(), cookieFileName);
			Iterator<Map.Entry<String, String>> iter = cookieMap.entrySet().iterator();
			StringBuffer cookieStr = new StringBuffer();
			while(iter.hasNext()) {
				Map.Entry<String,String> ent = iter.next();
				cookieStr.append(ent.getKey() + "=" + ent.getValue() + ";");
			}
			LogCat.d("COOKIE", cookieStr.toString());
			return cookieStr.toString();
		} catch (Exception e) {
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
	 * @param
	 * @return
	 */
	public synchronized static String setCookie(Context context , HttpEntityEnclosingRequestBase httpRequest) {
		try {
			HashMap<String, String> cookieMap = (HashMap<String, String>) ObjectStreamIO.input(context.getFilesDir().getAbsolutePath(), cookieFileName);
			if(cookieMap == null) {
				return null;
			}
//			CookieStore cookieStore = httpClient.getCookieStore();
//			cookieStore.clear();
			
			Iterator<Map.Entry<String, String>> iter = cookieMap.entrySet().iterator();
			StringBuffer cookieStr = new StringBuffer();
			while(iter.hasNext()) {
				Map.Entry<String,String> ent = iter.next();
				cookieStr.append(ent.getKey() + "=" + ent.getValue() + ";");
			}
			
			httpRequest.addHeader("Cookie", cookieStr.toString());

			LogCat.d("REQUEST", "\r\n[SET COOKIE]" + cookieStr.toString());
			
			return cookieStr.toString();
		} catch (Exception e) {
		}
		return "";
	}


	public synchronized static String setCookie(Context context , HttpRequestBase httpRequest) {
		try {
			HashMap<String, String> cookieMap = (HashMap<String, String>) ObjectStreamIO.input(context.getFilesDir().getAbsolutePath(), cookieFileName);
			if(cookieMap == null) {
				return null;
			}
//			CookieStore cookieStore = httpClient.getCookieStore();
//			cookieStore.clear();

			Iterator<Map.Entry<String, String>> iter = cookieMap.entrySet().iterator();
			StringBuffer cookieStr = new StringBuffer();
			while(iter.hasNext()) {
				Map.Entry<String,String> ent = iter.next();
				cookieStr.append(ent.getKey() + "=" + ent.getValue() + ";");
			}

			httpRequest.addHeader("Cookie", cookieStr.toString());

			LogCat.d("REQUEST", "\r\n[SET COOKIE]" + cookieStr.toString());

			return cookieStr.toString();
		} catch (Exception e) {
		}
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
		LogCat.d("COOKIE", "httpclient.getcookies == 0 " + httpClient.getCookieStore().getCookies().size());
		return httpClient.getCookieStore().getCookies();
	}

	public synchronized static String setCookie(Context context , URLConnection con) {
		try {
			HashMap<String, String> cookieMap = (HashMap<String, String>) ObjectStreamIO.input(context.getFilesDir().getAbsolutePath(), cookieFileName);
			if(cookieMap == null) {
				return null;
			}
//			CookieStore cookieStore = httpClient.getCookieStore();
//			cookieStore.clear();

			Iterator<Map.Entry<String, String>> iter = cookieMap.entrySet().iterator();
			StringBuffer cookieStr = new StringBuffer();
			while(iter.hasNext()) {
				Map.Entry<String,String> ent = iter.next();
				cookieStr.append(ent.getKey() + "=" + ent.getValue() + ";");
			}

			con.addRequestProperty("Cookie", cookieStr.toString());

			LogCat.d("REQUEST", "\r\n[SET COOKIE]" + cookieStr.toString());

			return cookieStr.toString();
		} catch (Exception e) {
		}
		return "";
	}

}
