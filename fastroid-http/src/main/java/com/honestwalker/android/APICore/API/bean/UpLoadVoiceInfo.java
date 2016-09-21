package com.honestwalker.android.APICore.API.bean;

import java.io.Serializable;

/**
 * Created by honestwalker on 15-8-24.
 */
public class UpLoadVoiceInfo implements Serializable {

    String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
