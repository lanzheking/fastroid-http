package com.honestwalker.android.APICore.config.context;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by honestwalker on 16-1-26.
 */
public class ContextConfigBean<T> {

    private String environment;

    private HashMap<String , T> environmentGroup = new HashMap<>();

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public HashMap<String, T> getEnvironmentGroup() {
        return environmentGroup;
    }

}
