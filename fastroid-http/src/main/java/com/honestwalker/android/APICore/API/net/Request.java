package com.honestwalker.android.APICore.API.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.honestwalker.android.APICore.API.net.cookie.ApiCookieManager;
import com.honestwalker.androidutils.Application;
import com.honestwalker.androidutils.IO.LogCat;

import org.apache.commons.httpclient.DefaultMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * kancart 请求对象
 *
 * @author Lan zhe
 */
public class Request {

    private Context context;

    private final String TAG = "REQUEST";

    protected Gson gson = new Gson();

    private final int timeout = 25000;
    private final int retryTimes = 3;

    /** needSign : true 时会进行签名运算  fales 时不会进行签名运算 */
    private boolean needSign = true;

    public Request(Context context, boolean needSign) {
        this.context = context;
        this.needSign = needSign;
    }

    public String doGet(String url, Parameter parameters) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
        return request(RequestMethod.GET, url, parameters);
    }

    public String doPost(String url, Parameter parameters) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
        return request(RequestMethod.POST, url, parameters);
    }

    public String doPost(String url, Parameter parameters, Map<String, String> files) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
        return request(RequestMethod.POST, url, parameters, files);
    }

    public String doDelete(String url, Parameter parameters) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
        return request(RequestMethod.DELETE, url, parameters);
    }

    public String doPut(String url, Parameter parameters) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
        return request(RequestMethod.PUT, url, parameters);
    }

    public String doPut(String url, Parameter parameters, Map<String, String> files) throws IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
        return request(RequestMethod.PUT, url, parameters, files);
    }

    private String request(RequestMethod requestMethod, String url, Parameter parameters) throws ClientProtocolException, IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
        if (RequestMethod.GET.equals(requestMethod)) {
            return requestBase(requestMethod, url, parameters);
        } else if (RequestMethod.DELETE.equals(requestMethod)) {
            return requestBase(requestMethod, url, parameters);
        } else if (RequestMethod.PUT.equals(requestMethod)) {
            return requestEnclosing(requestMethod, url, parameters);
        } else if (RequestMethod.POST.equals(requestMethod)) {
            return requestEnclosing(requestMethod, url, parameters);
        }
        return "Error Request Method";
    }

    private String request(RequestMethod requestMethod, String url, Parameter parameters, Map<String, String> files)
            throws ClientProtocolException, IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {
        if (RequestMethod.GET.equals(requestMethod)) {
            return requestBase(requestMethod, url, parameters);
        } else if (RequestMethod.DELETE.equals(requestMethod)) {
            return requestBase(requestMethod, url, parameters);
        } else if (RequestMethod.PUT.equals(requestMethod)) {
            return requestEnclosingFile(requestMethod, url, parameters, files);
        } else if (RequestMethod.POST.equals(requestMethod)) {
            return requestEnclosingFile(requestMethod, url, parameters, files);
        }
        return "Error Request Method";
    }

    /**
     * 主要用于delete和get请求
     *
     * @param requestMethod
     * @param url
     * @param parameters
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    private String requestBase(RequestMethod requestMethod, String url, Parameter parameters) throws ClientProtocolException, IOException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        // 重试设置
        DefaultMethodRetryHandler retryhandler = new DefaultMethodRetryHandler();
        retryhandler.setRetryCount(retryTimes);
        retryhandler.setRequestSentRetryEnabled(true);
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                retryhandler);
        // 超时设置
        httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, timeout);

        url += parameters.toString();
        LogCat.d(TAG, "\r\n[GET]: " + url + "\r\n\r\n");
        HttpRequestBase httpRequest = null;

        if (RequestMethod.DELETE.equals(requestMethod)) {
            httpRequest = new HttpDelete(url);
        } else {
            httpRequest = new HttpGet(url);
        }

//		httpRequest.setHeader("header", "" );
        HttpResponse response = httpClient.execute(httpRequest);

        String responseStr = EntityUtils.toString(response.getEntity());
        LogCat.d(TAG, "\r\n[RESPONSE]: " + responseStr);
        return responseStr;

    }

    /**
     * 主要用于post和put请求
     *
     * @param requestMethod
     * @param url
     * @param parameters
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws InvalidKeyException
     */
    private String requestEnclosing(RequestMethod requestMethod, String url, Parameter parameters) throws ClientProtocolException, IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {

        String method = parameters.getParam("method");

        StringBuffer logSB = new StringBuffer();

        DefaultHttpClient httpClient = new DefaultHttpClient();

        // 重试设置
        DefaultMethodRetryHandler retryhandler = new DefaultMethodRetryHandler();
        retryhandler.setRetryCount(retryTimes);
        retryhandler.setRequestSentRetryEnabled(true);
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);

        // 超时设置
        httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, timeout);

        HttpEntityEnclosingRequestBase httpRequest = null;

        if (RequestMethod.PUT.equals(requestMethod)) {
            httpRequest = new HttpPut(url);
        } else {
            httpRequest = new HttpPost(url);
        }

        httpRequest.addHeader("User-Agent", "Android SHEITC-YDZW/" + Application.getAppVersion(context));

        UrlEncodedFormEntity entity = null;

        if (needSign) {
            entity = new UrlEncodedFormEntity(
                    parameters.sortPostParameter(), HTTP.UTF_8);
        } else {
            entity = new UrlEncodedFormEntity(
                    parameters.getParameterList(), HTTP.UTF_8);
        }

        logSB.append("\r\n");
        String urllog = url + URLDecoder.decode(parameters.toString());
        logSB.append("\r\n[" + requestMethod + "]: " + urllog + "\r\n");
        logSB.append("\r\n[Params]: " + urllog.substring(urllog.indexOf("?")).replace("&", "\r\n").replace("?", "\r\n") + "\r\n");

        logSB.append("\r\n[httpClient COOKIE]: " + ApiCookieManager.getHttpClientCookie(httpClient) + "\r\n\r\n");

        httpRequest.setEntity(entity);

        ApiCookieManager.setCookie(context, httpRequest);

//		CookieStore cs = new BasicCookieStore();
//		org.apache.http.cookie.Cookie c = new BasicClientCookie("domain", "efomm900641q22nav9fn44f3g3");
//		cs.addCookie(c);
//		httpClient.setCookieStore(cs);

        HttpResponse response = httpClient.execute(httpRequest);

        String responseStr = EntityUtils.toString(response.getEntity());
        logSB.append("[RESPONSE]: " + responseStr);
        if (responseStr != null) {
            try {
                responseStr = responseStr.substring(responseStr.indexOf("{"));
            } catch (Exception e) {
            }
        }

        LogCat.d(TAG, logSB.toString());

        // 是登录请求才保存COOKIE
        if("Kancart.User.Login".equals(method) || "Kancart.User.Weixin.Login".equals(method)) {
            LogCat.d("REQUEST" , "监听到登录请求保存cookie");
            ApiCookieManager.saveCookie(context, httpClient);
        }


        return responseStr;

    }

    /**
     * 主要用于post和put请求
     *
     * @param requestMethod
     * @param url
     * @param parameters
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws InvalidKeyException
     */
    private String requestEnclosingFile(RequestMethod requestMethod, String url, Parameter parameters, Map<String, String> file) throws ClientProtocolException, IOException, InvalidKeyException, ConnectionPoolTimeoutException, ConnectTimeoutException, SocketTimeoutException {

        StringBuffer logSB = new StringBuffer();

        DefaultHttpClient httpClient = new DefaultHttpClient();

        // 重试设置
        DefaultMethodRetryHandler retryhandler = new DefaultMethodRetryHandler();
        retryhandler.setRetryCount(retryTimes);
        retryhandler.setRequestSentRetryEnabled(true);
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);

        // 超时设置
        httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, timeout);

        HttpEntityEnclosingRequestBase httpRequest = null;

        if (RequestMethod.PUT.equals(requestMethod)) {
            httpRequest = new HttpPut(url);
        } else {
            httpRequest = new HttpPost(url);
        }

        httpRequest.addHeader("User-Agent", "Android SHEITC-YDZW/" + Application.getAppVersion(context));

        MultipartEntity entity = new MultipartEntity();

//		if(needSign) {
//			entity = new UrlEncodedFormEntity(
//					parameters.sortPostParameter(), HTTP.UTF_8);
//		} else {
//			entity = new UrlEncodedFormEntity(
//					parameters.getParameterList(), HTTP.UTF_8);
//		}
        List<NameValuePair> nvps = null;
        if (needSign) {
            nvps = parameters.sortPostParameter();
        } else {
            nvps = parameters.getParameterList();
        }
        if (nvps != null) {
            for (NameValuePair pair : nvps) {
                StringBody stringBody = new StringBody(pair.getValue(), Charset.forName("UTF-8"));
                entity.addPart(pair.getName(), stringBody);
            }
        }

        logSB.append("\r\n");
        String urllog = url + URLDecoder.decode(parameters.toString());
        logSB.append("\r\n[" + requestMethod + "]: " + urllog + "\r\n");
        logSB.append("\r\n[Params]: " + urllog.substring(urllog.indexOf("?")).replace("&", "\r\n").replace("?", "\r\n") + "\r\n");

        logSB.append("\r\n[COOKIE]: " + ApiCookieManager.getLocalCookie(context) + "\r\n\r\n");
//        logSB.append("\r\n[COOKIE]: " + CookieManager.getHttpClientCookie(httpClient) + "\r\n\r\n");

        httpRequest.setEntity(entity);

        ApiCookieManager.setCookie(context, httpRequest);

        {   // 文件
//			MultipartEntity reqEntity = new MultipartEntity();
            if (file != null) {
                Iterator<Map.Entry<String, String>> iter = file.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> ent = iter.next();
                    if (ent.getValue() != null) {
                        FileBody fileBody = new FileBody(new File(ent.getValue()));
                        entity.addPart(ent.getKey(), fileBody);   //file1为请求后台的File upload;属性
                        logSB.append("\r\n File:" + ent.getKey() + " = " + ent.getValue());
                    }
                }
            }
        }

        httpRequest.setEntity(entity);

        HttpResponse response = httpClient.execute(httpRequest);

        String responseStr = EntityUtils.toString(response.getEntity());
        logSB.append("[RESPONSE]: " + responseStr);
        if (responseStr != null) {
            try {
                responseStr = responseStr.substring(responseStr.indexOf("{"));
            } catch (Exception e) {
            }
        }

        LogCat.d(TAG, logSB.toString());

        ApiCookieManager.saveCookie(context, httpClient);

        return responseStr;

    }

	/*public String doPost(String url, Parameter parameters)
            throws InvalidKeyException, ClientProtocolException,
			RequestFailException, IOException, Exception {

		StringBuffer logSB = new StringBuffer();

		DefaultHttpClient httpClient = new DefaultHttpClient();

		// 重试设置
		DefaultMethodRetryHandler retryhandler = new DefaultMethodRetryHandler();
		retryhandler.setRetryCount(3);
		retryhandler.setRequestSentRetryEnabled(true);
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);

		// 超时设置
		httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 40000);

		HttpPost httpPost = new HttpPost(url);
//		httpPost.setHeader("TD-Agent","en_US@currency=USD,device=ios,ad421e72d15c0c3fa230c55cd728d7fd");
		httpPost.setHeader("TD-Agent",getTDHeader());
		LogCat.d("REQUEST_HEADER", getTDHeader());
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
				parameters.getParameterList(), HTTP.UTF_8);

		logSB.append("\r\n");
		logSB.append("\r\n[POST]: " + url + parameters.toString() + "\r\n\r\n");

		httpPost.setEntity(entity);

		HttpResponse response = httpClient.execute(httpPost);
		String responseStr = EntityUtils.toString(response.getEntity());
		logSB.append("[RESPONSE]: " + responseStr);
		if (responseStr != null) {
			try {
				responseStr = responseStr.substring(responseStr.indexOf("{"));
			} catch (Exception e) {
			}
		}

		LogCat.d(TAG, logSB.toString());

		return responseStr;
	}*/

    /**
     * 向指定url发送请求
     */
    public String doGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = new DefaultHttpClient()
                    .execute(httpGet);
            return EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            LogCat.d(e.toString());
        } catch (IOException e) {
            LogCat.d(e.toString());
        }
        return "";
    }

    /**
     * 向指定url发送请求，和参数
     *
     * @param url
     * @param parameters
     * @return
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClientProtocolException
     */
	/*public String doGet(String url, Parameter parameters)
			throws InvalidKeyException, ClientProtocolException,
			RequestFailException, IOException, Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		// 重试设置
		DefaultMethodRetryHandler retryhandler = new DefaultMethodRetryHandler();
		retryhandler.setRetryCount(3);
		retryhandler.setRequestSentRetryEnabled(true);
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				retryhandler);
		// 超时设置
		httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT,
				40000);

		url += parameters.toString();
		LogCat.d(TAG, "\r\n[GET]: " + url + "\r\n\r\n");
		HttpGet httpGet = new HttpGet(url);
//		httpGet.setHeader("TD-Agent","en_US@currency=USD,device=ios,ad421e72d15c0c3fa230c55cd728d7fd");
		httpGet.setHeader("TD-Agent",getTDHeader());
		HttpResponse response = httpClient.execute(httpGet);

		String responseStr = EntityUtils.toString(response.getEntity());
		LogCat.d(TAG, "[RESPONSE]: " + responseStr);
		return responseStr;
	}*/

    private static int checkConnectDialogShowTimes = -1; // 避免连续跳窗提示


    public void checkConnect2() {

        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        LogCat.d("connect", "networkinfo == null" + (networkinfo == null));
        if (networkinfo == null || !networkinfo.isAvailable()) {
//			DialogHelper.alert(context, "Warning", "You must connect to the internet to use Tinydeal!");
//			loading(false);
        }

    }


}
