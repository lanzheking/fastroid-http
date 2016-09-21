package com.honestwalker.android.APICore.API;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.example.hkyy.fastroid_http.R;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.honestwalker.android.APICore.API.ParseStrategy.ParseStrategy;
import com.honestwalker.android.APICore.API.net.Parameter;
import com.honestwalker.android.APICore.API.net.Request;
import com.honestwalker.android.APICore.API.net.RequestMethod;
import com.honestwalker.android.APICore.API.req.BaseReq;
import com.honestwalker.android.APICore.API.resp.BaseResp;
import com.honestwalker.android.APICore.API.utils.NETWORK_TOAST_TYPE;
import com.honestwalker.android.APICore.API.utils.NetworkToastManager;
import com.honestwalker.android.APICore.IO.API;
import com.honestwalker.android.APICore.IO.ResponseMethod;
import com.honestwalker.android.APICore.Server;
import com.honestwalker.android.APICore.config.context.ContextConfigBean;
import com.honestwalker.android.APICore.config.context.ContextLoader;
import com.honestwalker.android.APICore.config.context.Environment;
import com.honestwalker.androidutils.Application;
import com.honestwalker.androidutils.IO.LogCat;
import com.honestwalker.androidutils.StringUtil;
import com.honestwalker.androidutils.TimeUtil;
import com.honestwalker.androidutils.UIHandler;
import com.honestwalker.androidutils.equipment.TelephoneUtil;
import com.honestwalker.androidutils.exception.ExceptionUtil;
import com.honestwalker.androidutils.pool.ThreadPool;
import com.honestwalker.androidutils.propertices.ProperticesLoader;
import com.honestwalker.androidutils.propertices.exception.ProperticeException;
import com.honestwalker.androidutils.views.AlertDialogPage;
import com.honestwalker.androidutils.window.DialogHelper;
import com.honestwalker.androidutils.window.ToastHelper;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class BaseAPI {

    protected Context context;

    private final static String TAG = "REQUEST";

    private Gson gson = new Gson();

    /** 环境配置错误标记 */
    private boolean configError = false;

    /**
     * 服务端环境配置
     */
    private ContextConfigBean<Environment> contextConfig = null;

    public BaseAPI(Context context) {
        this.context = context;
//        loadContextConfig();
    }

    protected Request getRequest() {
        return new Request(context , false);
    }

    /**
     * 读取环境配置文件
     */
    private synchronized void loadContextConfig() {

        if(configError) {
            LogCat.d("REQUEST" , "环境配置文件错误。");
            return;
        }

        if(contextConfig == null) {
            ProperticesLoader loader = new ProperticesLoader();
            InputStream is = context.getResources().openRawResource(R.raw.context);
            InputStreamReader isr = new InputStreamReader(is);
            try {
                loader.loadConfig(isr);
                contextConfig = ContextLoader.load(loader, Environment.class);
            } catch (ProperticeException e) {
                configError = true;
                ExceptionUtil.showException("REQUEST" , e);
            }
        }

    }

    /**
     * 从注解中读取响应请求信息
     * @param req
     * @return
     * @throws ApiException
     */
    private API readRequestAnnotation(BaseReq req) throws ApiException {
        Class<?> target = req.getClass();
        API api = target.getAnnotation(API.class);

        if (api == null) throw new ApiException("req 未配置");

        return api;
    }

    protected <T extends BaseResp> void request(
            final BaseReq req,
            final APIListener<T> listener ,
            final Class clazz ) {
        request(req, listener, clazz, true);
    }

    /**
     *
     * @param req  请求对象
     * @param listener 请求回调
     * @param clazz json映射对象
     * @param unloginAutoSkipToLogin 需要登录是否自动条登录页面默认true (不传为true)
     */
    protected <T extends BaseResp> void request(
            final BaseReq req,
            final APIListener<T> listener ,
            final Class clazz ,
            final boolean unloginAutoSkipToLogin ) {
        request(req, null, listener, clazz, unloginAutoSkipToLogin);
    }

    protected <T extends BaseResp> void request(
            final BaseReq req,
            final Map<String, String> files,
            final APIListener<T> listener ,
            final Class clazz ) {
        request(req, files, listener, clazz, true);
    }

    /**
     * 包括文件上传的请求
     */
    protected <T extends BaseResp> void request(
            final BaseReq req,
            final Map<String, String> files,
            final APIListener<T> listener ,
            final Class clazz,
            final boolean unloginAutoSkipToLogin ) {

        if(configError) {
            LogCat.d("REQUEST" , "环境配置文件错误。");
            if(listener != null) {
                listener.onFail(new ApiException("环境配置错误！"));
            }
            return;
        }

        onStart(listener);

        final Object reqTag = req.reqTag;

        ThreadPool.threadPool(reqTag, new Runnable() {
            @Override
            public void run() {
                try {

                    checkClientConnect();

                    API api = readRequestAnnotation(req);

                    // 发送请求
                    String result = doRequest(req, api, files);

                    // 获得json解析后的数据对象
                    BaseResp<T> t = parse(api, result, clazz);

                    if (t == null) throw new ApiException("json 解析失败! " + api.value());

                    t.setReqTag(reqTag);
                    t.setJson(result);

                    if ("success".equals(t.getResult())) {
                        onComplete(t, listener);
                    } else if ("2".equals(t.getCode())) {
                        if (unloginAutoSkipToLogin && loginTimeoutHandler != null) {
                            loginTimeoutHandler.sendEmptyMessage(0);
                        } else {
                            onFail(listener , new ApiException("未登录"));
                        }
                    } else {
                        if (!api.handleErrorPersonally()) {
                            int code = 1;
                            try {
                                code = Integer.parseInt(t.getCode());
                            } catch (Exception e){}
                            if (t.getErrmsg() != null && code >= 1000) {
                                alert(t.getErrmsg());
                            }
                        }

                        throw new ApiException(t, t.getJson());
                    }

                } catch (ConnectionPoolTimeoutException e) {
                    onTimeout(listener);
                } catch (ConnectTimeoutException e) {
                    onTimeout(listener);
                } catch (SocketTimeoutException e) {
                    onTimeout(listener);
                } catch (ApiException e) {
                    LogCat.d("api", "ApiException" + e.getMessage());
                    onFail(listener, e);
                } catch (Exception e) {
                    LogCat.d("api", "Exception" + e.getMessage());
                    ApiException apiException = new ApiException(e.getMessage());
                    apiException.setStackTrace(e.getStackTrace());
                    onFail(listener, apiException);
                }
            }
        });

    }

    /**
     * 发起请求，获取api返回值json
     * @param api          req对象的api注解对象
     * @param files        上传的文件
     * @return
     * @throws IOException
     * @throws InvalidKeyException
     */
    private String doRequest(BaseReq req , API api , Map<String, String> files) throws IOException, InvalidKeyException {

        Parameter parameters = getParameters(req);
        parameters.put(Server.context(context).getAction_key() , api.value());
        fillKancartAPIParam(parameters);

        RequestMethod requestMethod = api.requestMethod();
        String result = "";
        String url = Server.context(context).getHost();
//        String url = getEnvironment().getApi_host();

        // req 可以host覆盖
        if (!StringUtil.isEmptyOrNull(api.host()))  url = api.host();
        // 如果请求地址有uri ， 拼接上uri地址
        if (!StringUtil.isEmptyOrNull(api.uri()))   url = url + api.uri();

        if (RequestMethod.POST.equals(requestMethod)) {
            result = getRequest().doPost(url, parameters, files);
        } else if (RequestMethod.DELETE.equals(requestMethod)) {
            result = getRequest().doDelete(url, parameters);
        } else if (RequestMethod.PUT.equals(requestMethod)) {
            result = getRequest().doPut(url, parameters, files);
        } else {
            result = getRequest().doGet(url, parameters);
        }

        // 处理返回类型 json 或 string , string类型自动转换成json
        ResponseMethod responseMethod = api.responseMethod();
        if (ResponseMethod.STRING.equals(responseMethod)) {
            result = "{\"result\":\"success\" , \"info\":\"" + result + "\"}";
            LogCat.d("REQUEST", "返回类型String ， 转换成: " + result);
        }

        return result;
    }

    /**
     * 解析数据对象
     * @param api
     * @param result
     * @param defaultClass
     * @param <T>
     * @return
     */
    private <T> BaseResp<T> parse(API api , String result , Class defaultClass) {
        BaseResp<T> t = null;
        try {
            // 先用标准数据结构解析， 如果解析失败标记，并走策略模式进行解析
            t =  (BaseResp<T>) gson.fromJson(result, defaultClass);
            return t;
        } catch (Exception e) {}

        Class[] parseStrategies = api.parseStrategies();
        if(parseStrategies != null) {
            for(Class strategyClass : parseStrategies) {
                try {
                    LogCat.d("json" , "解析策略 " + strategyClass);
                    ParseStrategy parseStrategy = (ParseStrategy) strategyClass.newInstance();
                    BaseResp<T> ts = (BaseResp<T>) gson.fromJson(result, parseStrategy.getStrategyClass());
                    t = (BaseResp<T>) parseStrategy.transition(ts);
                    LogCat.d("json" , "目标 " + parseStrategy.getStrategyClass());
                    LogCat.d("json" , strategyClass + " 解析成功 : " + t);
                    return t;
                } catch (Exception e) {
                    LogCat.d("json", strategyClass + "  解析失败");
                }
            }
        }
        return null;
    }

    /**
     * Handler 执行onComplete
     * @param t
     * @param listener
     * @param <T>
     */
    private <T> void onComplete(final BaseResp<T> t , final APIListener listener) {
        if(listener == null) return;
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onComplete(t);
            }
        });
    }

    /**
     * 请求超时回调
     * @param listener
     */
    private void onTimeout(APIListener listener) {
        if(listener == null) return;
        NetworkToastManager.alert(NETWORK_TOAST_TYPE.TIMEOUT);
        listener.onFail(new ApiException());
    }

    /**
     * 检测连接状态
     * @throws Exception
     */
    private void checkClientConnect() throws Exception {

        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {
            throw new Exception("NetWork Error");
        }

    }

    /**
     * 检测host
     * @param host
     * @throws Exception
     */
    private void checkHost(String host) throws Exception {
        if(isIPAddress(host)) {
            boolean checkResult = isIpReachable(getIpAddress(host));
            if(!checkResult) {
                hostError();
                throw new Exception("服务器未响应");
            }
        }
    }
    /**
     * host错误回调
     */
    private void hostError() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                ToastHelper.alert(context, context.getString(R.string.server_not_reached));
            }
        });
    }

    private boolean isIpReachable(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            if (addr.isReachable(3000)) {
                return true;
            }
            return false;
        } catch (UnknownHostException e)  {
        } catch (IOException e) {
        }
        return false;
    }

    private String getIpAddress(String host) {
        String ip = host.replace("http://", "");
        if(ip.indexOf(":") > -1) {
            ip = ip.substring(0 , ip.indexOf(":"));
        }
        if(ip.indexOf("/") > -1) {
            ip = ip.substring(0 , ip.indexOf("/"));
        }
        return ip;
    }

    private boolean isIPAddress(String host) {
        String ip = host.replace("http://", "");
        if(ip.indexOf(":") > -1) {
            ip = ip.substring(0 , ip.indexOf(":"));
        }
        if(ip.indexOf("/") > -1) {
            ip = ip.substring(0 , ip.indexOf("/"));
        }
        Pattern pattern = Pattern.compile( "^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$" );
        return pattern.matcher( ip ).matches();
    }

    private void alert(final String msg) {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
//                DialogHelper.alert(context, msg);
                try {
                    AlertDialogPage dialog = new AlertDialogPage((Activity)context);
                    dialog.setContent(msg);
                    dialog.setTitleVisible(false);
                    dialog.show();
                } catch (Exception e){}
            }
        });
    }

    private void onStart(final APIListener listener) {
        if(listener == null) return;
        UIHandler.post(new Runnable() {

            @Override
            public void run() {
                listener.onStart();
            }
        });
    }

    /**
     * 请求失败回调
     * @param listener
     * @param e
     * @param <T>
     */
    private <T> void onFail(final APIListener<T> listener , final ApiException e) {
        ExceptionUtil.showException(e);

        if(e.toString().indexOf("Unable to resolve host") > -1) {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    ToastHelper.alert(context, context.getString(R.string.server_not_reached));
                }
            });
        } else if(e.toString().indexOf("NetWork Error") > -1) {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    NetworkToastManager.alert(NETWORK_TOAST_TYPE.UNCONNECTED);
                    ToastHelper.alert(context, context.getString(R.string.network_unavailable));
                }
            });
        }

        if(listener == null) return;
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onFail(e);
            }
        });
    }

    /** request对象转parameter */
    private Parameter getParameters(BaseReq req) {
        Parameter params = new Parameter();
        Field[] fields = req.getClass().getFields();
        for(Field field : fields) {
            try {
                if(field.getType().equals(HashMap.class)) {   // 如果是自定义参数类型
                    HashMap<String, String> kvs = (HashMap<String, String>) field.get(req);
                    Iterator<Map.Entry<String, String>> iter = kvs.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, String> ent = iter.next();
                        params.put(ent.getKey(), ent.getValue());
                    }
                } else {
                    if(field.get(req) != null && !StringUtil.isEmptyOrNull(field.get(req) + "")) {
                        SerializedName anno = field.getAnnotation(SerializedName.class);
                        if(anno != null) {
                            params.put(anno.value() , field.get(req));
                        } else {
                            params.put(field.getName(), field.get(req));
                        }

					}
                }
            } catch (Exception e) {
                ExceptionUtil.showException(e);
            }
        }

        fillKancartAPIParam(params);

        return params;
    }

    /**
     * 填充系统参数
     * @param params
     */
    private void fillKancartAPIParam(Parameter params) {
        try {
//			params.put("currency", "CNY");
//			params.put("format", "JSON");
//			params.put("v", "1.1");
            params.put("timestamp", new TimeUtil().getNow());
//			SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");
//			params.put("session", "");
            params.put("sign_method", "md5");
//			params.put("language", "zh-cn");
//            params.put("device", TelephoneUtil.getInstance(context).getDeviceId());
//			params.put("session_id", TelephoneUtil.getInstance(context).getDeviceId());
            params.put("client", "android");
            params.put("version", Application.getAppVersion(context));
            params.put("app_key", Server.context(context).getApp_key());
            params.put("app_secret", Server.context(context).getApp_secret());
            params.sortPostParameter();
        } catch (Exception e) {
            ExceptionUtil.showException(e);
        }

    }

    private Environment getEnvironment() {
        String currentEnvironmentName = contextConfig.getEnvironment();
        Environment environment = contextConfig.getEnvironmentGroup().get(currentEnvironmentName);
        return environment;
    }

    /**
     * 超时回调Handler
     */
    public static Handler loginTimeoutHandler;
    public static void setLoginTimeoutCallback(Handler handler) {
        loginTimeoutHandler = handler;
    }

}
