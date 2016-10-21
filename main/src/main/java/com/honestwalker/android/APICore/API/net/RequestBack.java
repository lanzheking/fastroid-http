package com.honestwalker.android.APICore.API.net;

import android.content.Context;
import android.text.TextUtils;

import com.honestwalker.androidutils.IO.LogCat;
import com.honestwalker.androidutils.IO.SharedPreferencesLoader;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.PreferencesCookieStore;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.cookie.Cookie;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.Map;

/**
 * kancart 请求对象
 *
 * @author Lan zhe
 */
public class RequestBack {

//    private final String TAG = "REQUEST";
//    private Context context;
//    private HttpUtils http;
//    private PreferencesCookieStore cookieStore;
//    private String result;
//
//    public RequestBack(Context context) {
//        this.context = context;
//        http = new HttpUtils(context);
//        cookieStore = new PreferencesCookieStore(context);
//    }
//
//    public String doGet(String url, Parameter parameters) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
//        return request(HttpRequest.HttpMethod.GET, url, parameters);
//    }
//
//    public String doPost(String url, Parameter parameters) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
//        return request(HttpRequest.HttpMethod.POST, url, parameters);
//    }
//
//    public String doPost(String url, Parameter parameters, Map<String, String> files) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
//        return request(HttpRequest.HttpMethod.POST, url, parameters, files);
//    }
//
//    public String doDelete(String url, Parameter parameters) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
//        return request(HttpRequest.HttpMethod.DELETE, url, parameters);
//    }
//
//    public String doPut(String url, Parameter parameters) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
//        return request(HttpRequest.HttpMethod.PUT, url, parameters);
//    }
//
//    public String doPut(String url, Parameter parameters, Map<String, String> files) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
//        return request(HttpRequest.HttpMethod.PUT, url, parameters, files);
//    }
//
//    private String request(HttpRequest.HttpMethod requestMethod, String url, Parameter parameters) {
//        return request(requestMethod, url, parameters, null);
//    }
//
//    private String request(HttpRequest.HttpMethod requestMethod, String url, Parameter parameters, Map<String, String> files) {
//
//        String method = parameters.getParam("method");
//
//        StringBuffer logSB = new StringBuffer();
//        String urllog = url + URLDecoder.decode(parameters.toString());
//        logSB.append("\r\n[" + requestMethod + "]: " + urllog + "\r\n");
//        logSB.append("\r\n[Params]: " + urllog.substring(urllog.indexOf("?")).replace("&", "\r\n").replace("?", "\r\n") + "\r\n");
//
////        StringBuffer cookieSB = new StringBuffer();
////        for (Cookie cookie : cookieStore.getCookies()) {
////            cookieSB.append("cookiename:"+cookie.getName());
////            cookieSB.append(";cookievalue:" + cookie.getValue());
////            cookieSB.append(";getDomain:" + cookie.getDomain());
////            cookieSB.append(";getPath:" + cookie.getPath());
////            cookieSB.append(";getVersion:" + cookie.getVersion());
////            cookieSB.append(";getExpiryDate:" + cookie.getExpiryDate());
////            cookieSB.append("\n");
////        }
//
////        logSB.append("\r\n[httpClient COOKIE]: " + cookieSB.toString() + "\r\n\r\n");
//
//        RequestParams requestParams = new RequestParams();
//
//        requestParams.addBodyParameter(parameters.getParameterList());
//        fillFilesParams(requestParams, files);
//        if(!"Kancart.User.Login".equals(method)) {
//            http.configCookieStore(cookieStore);
//        }
//        http.send(requestMethod, url, requestParams, new RequestCallBack<String>() {
//            @Override
//            public void onSuccess(ResponseInfo<String> responseInfo) {
//                result = responseInfo.result;
//            }
//
//            @Override
//            public void onFailure(HttpException error, String msg) {
//                result = msg;
//            }
//        });
//
//        while (TextUtils.isEmpty(result)) ;
//
////        SharedPreferencesLoader.getInstance(context).putString("cookie", cookieSB.toString());   //存储cookie
//
//        if("Kancart.User.Login".equals(method)) {
//            LogCat.d(TAG , "监听到登录请求，保存COOKIE");
//
//            StringBuffer cookieSB = new StringBuffer();
//            String domain = "";
//            for (Cookie cookie : cookieStore.getCookies()) {
//                cookieSB.append(cookie.getName() + "=" + cookie.getValue() + ";");
//                domain = "tmc.kaiwangpu.com";
//            }
//            cookieSB.append(" domain=" + domain);
//
//            SharedPreferencesLoader.getInstance(context).putString("cookie", cookieSB.toString());
//            logSB.append("\r\n[httpClient COOKIE]: " + cookieSB.toString() + "\r\n\r\n");
//        }
//
//        logSB.append("[RESPONSE]: " + result);
//        LogCat.d(TAG, logSB.toString());
//        return result;
//    }
//
//    private void fillFilesParams(RequestParams requestParams, Map<String, String> file) {
//        if (file != null) {
//            Iterator<Map.Entry<String, String>> iter = file.entrySet().iterator();
//            while (iter.hasNext()) {
//                Map.Entry<String, String> ent = iter.next();
//                if (ent.getValue() != null) {
//                    requestParams.addBodyParameter(ent.getKey(), new File(ent.getValue()));
//                }
//            }
//        }
//    }

}
