package com.honestwalker.android.APICore.config;

import java.util.HashMap;

/**
 *
 * Created by zhe.lan@honestwalker.com on 15-12-24.
 */
public class ServerConfig<T> {

    /** 当前环境id */
    private String scheme;

    /** 环境对象映射表 */
    private HashMap<String , T> serverContexts = new HashMap<>();

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public HashMap<String , T> getServerContexts() {
        return serverContexts;
    }

    private void setServerContexts(HashMap<String , T> serverContexts) {
        this.serverContexts = serverContexts;
    }

}
