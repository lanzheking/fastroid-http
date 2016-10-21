package com.honestwalker.android.APICore.config.context;

import com.honestwalker.androidutils.IO.LogCat;
import com.honestwalker.androidutils.propertices.ProperticesLoader;
import com.honestwalker.androidutils.propertices.exception.ProperticeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 读取环境配置 context.propertice
 * Created by honestwalker on 16-1-26.
 */
public final class ContextLoader {

    private final static String TAG = "REQUEST";

    /**
     * 开始读取配置
     * @param loader
     * @param enviromentConfigClass
     * @param <T>
     * @return
     * @throws ProperticeException
     */
    public static <T> ContextConfigBean<T> load(ProperticesLoader loader , Class<T> enviromentConfigClass) throws ProperticeException {

        try {
            ContextConfigBean<T> bean = new ContextConfigBean();
            String currentEnvironment = loader.getPropertice("environment");

            bean.setEnvironment(currentEnvironment);

            ArrayList<String> groups = loader.groups();
            for(String group : groups) {

                T environment = enviromentConfigClass.newInstance();

                Field[] environmentFields = environment.getClass().getDeclaredFields();
                for(Field environmentField : environmentFields) {
                    environmentField.setAccessible(true);
                    String environmentFieldName = environmentField.getName();
                    String environmentFieldValue = loader.getProperticeByGroup(group, environmentFieldName);
                    environmentField.set(environment, environmentFieldValue);
                }

                bean.getEnvironmentGroup().put(group, environment);
            }

            testShow((ContextConfigBean<Environment>) bean);

            return bean;
        } catch (Exception e) {
            throw new ProperticeException("环境配置 [context.propertice] 错误!");
        }
    }

    private static void testShow(ContextConfigBean<Environment> config) {

        LogCat.d(TAG , "当前环境" + config.getEnvironment());

        LogCat.d(TAG , "环境列表:");

        Iterator<HashMap.Entry<String , Environment>> iterator = config.getEnvironmentGroup().entrySet().iterator();
        while(iterator.hasNext()) {
            HashMap.Entry<String , Environment> entry = iterator.next();
            String environmentName = entry.getKey();
            Environment environment = entry.getValue();
            LogCat.d(TAG , "[" + environmentName + "]");
            LogCat.d(TAG , "api_host:" + environment.getApi_host());
            LogCat.d(TAG , "host:" + environment.getHost());
        }

    }


}
