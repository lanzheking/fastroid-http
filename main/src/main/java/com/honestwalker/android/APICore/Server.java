package com.honestwalker.android.APICore;

import android.content.Context;

import com.honestwalker.android.APICore.config.ServerConfig;
import com.honestwalker.android.APICore.config.ServerContext;
import com.honestwalker.android.APICore.config.ServerLoader;
import com.honestwalker.android.APICore.exception.ServerConfigException;
import com.honestwalker.androidutils.IO.LogCat;
import com.honestwalker.androidutils.exception.ExceptionUtil;

/**
 * 获得Server环境对象
 * Created by honestwalker on 16-1-29.
 */
public class Server {

    private static ServerContext serverContext;

    private static int serverConfigRes;

    public static void init(int serverConfigRes) {
        Server.serverConfigRes = serverConfigRes;
    }

    /**
     * 获得当前Server环境
     * @param context
     * @return
     */
    public static synchronized ServerContext context(Context context) {

        if(serverContext != null) return serverContext;

        try {
            ServerConfig<ServerContext> config = ServerLoader.getServerConfig(context, ServerContext.class, Server.serverConfigRes);
            serverContext = config.getServerContexts().get(config.getScheme());

            if(serverContext == null) {
                throw new ServerConfigException("找不到环境：" + config.getScheme());
            }

            return serverContext;
        } catch (Exception e) {
            ExceptionUtil.showException("REQUEST" , e);
            return null;
        }
    }

    private static void testShow(ServerContext serverContext) {
        LogCat.d("ServerConfig" , serverContext.getMethod() + " " + serverContext.getHost());
    }

}
