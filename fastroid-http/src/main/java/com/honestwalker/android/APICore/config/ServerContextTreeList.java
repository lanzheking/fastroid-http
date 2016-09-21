package com.honestwalker.android.APICore.config;

import java.util.HashMap;

/**
 * Created by honestwalker on 16-1-29.
 */
class ServerContextTreeList {

    /**
     * 对象总映射表 ， 方便查询树
     */
    private HashMap<String , ServerContextTree> serverContextTreeMapping = new HashMap<>();

    private HashMap<String , ServerContextTree> rootServerContextTree = new HashMap<>();

    public HashMap<String, ServerContextTree> getServerContextTreeMapping() {
        return serverContextTreeMapping;
    }

    public HashMap<String, ServerContextTree> getRootServerContextTree() {
        return rootServerContextTree;
    }

}
