package com.honestwalker.android.APICore.config;

import java.util.HashMap;

/**
 * 记录server配置继承关系的树 ， 子节点用map 方便检索
 * Created by honestwalker on 16-1-29.
 */
public class ServerContextTree {

    /**
     * 树数据对象
     */
    private ServerContextSupport serverContext;

    /**
     * 子节点映射表，仅包括下级节点
     */
    private HashMap<String , ServerContextTree> child = new HashMap<>();

    public ServerContextSupport getServerContext() {
        return serverContext;
    }

    public void setServerContext(ServerContextSupport serverContext) {
        this.serverContext = serverContext;
    }

    public HashMap<String, ServerContextTree> getChild() {
        return child;
    }

}
