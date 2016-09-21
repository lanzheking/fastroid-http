package com.honestwalker.android.APICore.config;

/**
 * Created by honestwalker on 15-12-24.
 */
public class ServerContext extends ServerContextSupport {

    private String method;
    private String host;
    private String web_host;
    private String action_key;
    private String app_key;
    private String app_secret;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAction_key() {
        return action_key;
    }

    public void setAction_key(String action_key) {
        this.action_key = action_key;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public String getApp_secret() {
        return app_secret;
    }

    public void setApp_secret(String app_secret) {
        this.app_secret = app_secret;
    }

    public String getWeb_host() {
        return web_host;
    }

    public void setWeb_host(String web_host) {
        this.web_host = web_host;
    }
}
