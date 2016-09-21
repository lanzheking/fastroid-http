package com.honestwalker.android.APICore.API.net;

import android.net.Uri;

import com.honestwalker.androidutils.AES;
import com.honestwalker.androidutils.IO.LogCat;
import com.honestwalker.androidutils.MD5;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求参数封装
 * 
 * @author langel 2011-6-17
 * 
 */
public class Parameter {

    List<NameValuePair> paramList = new ArrayList<NameValuePair>();

    public Parameter put(String key, Object value) {
        if (key != null) {
            if (value == null) {
                value = "";
            }
            value = (value + "").trim();
            NameValuePair param = new BasicNameValuePair(key, value + "");
            for (NameValuePair p : paramList) {
                if (p.getName().equals(key)) {
                    paramList.remove(p);
                    break;
                }
            }
            paramList.add(param);
        }
        return this;
    }

    /**
     * 清除全部参数
     */
    public void clear() {
        paramList.clear();
    }

    /**
     * 判断参数列某个参数是否存在
     *
     * @param key
     *            参数名
     * @return
     */
    public Boolean contains(String key) {
        if (key == null) {
            return false;
        } else {
            for (NameValuePair p : paramList) {
                if (p.getName().equals(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 删除某个参数
     *
     * @param key
     * @return
     */
    public void removeParam(String key) {
        if (key != null) {
            for (NameValuePair p : paramList) {
                if (p.getName().equals(key)) {
                    paramList.remove(p);
                    break;
                }
            }
        }
    }

    /**
     * 取得指定参数的值
     *
     * @param key
     * @return
     */
    public String getParam(String key) {
        if (key != null) {
            for (NameValuePair p : paramList) {
                if (p.getName().equals(key)) {
                    return p.getValue();
                }
            }
            return null;
        } else {
            return null;
        }
    }

    public Map<String , String> getParams() {
        Map<String , String> params = new HashMap<String, String>();
        for (NameValuePair p : paramList) {
            params.put(p.getName()	, p.getValue());
        }
        return params;
    }

    public List<NameValuePair> getParameterList() {
        return paramList;
    }

    /**
     * 获取参数排序后的url请求,用于post请求
     *
     * @throws InvalidKeyException
     */
    public List<NameValuePair> sortPostParameter() throws InvalidKeyException {

        // 参数列表排序
        Collections.sort(paramList, new Comparator<NameValuePair>() {
            @Override
            public int compare(NameValuePair object1, NameValuePair object2) {
                return (object1.getName().compareTo(object2.getName()));
            }
        });

        String appSecret = "";
        appSecret = getParam("app_secret");

        // 删除掉sign值 ， 重新md5加密
        removeParam("app_secret");
        removeParam("sign");
        put("sign", sign(appSecret));

        // 加密appkey
        String appKey = getParam("app_key");

        if (true) {
            try {
                LogCat.d("TEST" , "appkey=" + appKey + "appSecret=" + appSecret);
                appKey = AES.encrypt(appKey, appSecret).toLowerCase();
            } catch (InvalidKeyException e2) {
            }
            removeParam("app_key");
        }
        put("app_key", appKey);

        LogCat.d("REQUEST", "\r\n");
//		for (NameValuePair nvp : paramList) {
//			LogCat.d("REQUEST", nvp.getName() + "=" + nvp.getValue());
//		}
        return paramList;

    }

    /** 计算签名 */
    public String sign(String appSecret) {
        // 计算sign
        StringBuilder buf = new StringBuilder();
        int i = 0;
        for (NameValuePair one : paramList) {
            if (one.getName() != null && !one.getName().equals("sign")) {
                buf.append(one.getName());
                buf.append("=");
                buf.append(one.getValue());
                if(i < paramList.size() - 1) {
                    buf.append("&");
                }
                i++;
            }
        }

//		buf.append(appSecret);
        buf.append(appSecret);
        LogCat.d("TEST", "SIGN KV = " + buf.toString());

        String md5Sign = "";
        try {
            md5Sign = MD5.encrypt(new String(buf.toString().getBytes(), "UTF-8"));
        } catch (Exception e) {
        }
        return md5Sign;
    }

    /**
     * 使用post也可以使用此方法，虽然显示的结果是?key=value形式，但实际上还是post发送
     */
    @Override
    public String toString() {
        String result = "?";
        for (NameValuePair param : paramList) {
            result += param.getName() + "=" + Uri.encode(param.getValue()) + "&";
        }
        return result.substring(0, result.length() - 1);
    }

    public String getParameterStr() {
        String result = "";
        for (NameValuePair param : paramList) {
            result += param.getName() + "=" + param.getValue() + "&";
        }
        return result.substring(0, result.length() - 1);
    }
	
}
