package com.honestwalker.android.APICore.API.ParseStrategy;


import com.honestwalker.android.APICore.API.resp.BaseResp;

/**
 * Created by honestwalker on 16-1-21.
 */
public interface ParseStrategy<T> {
    public Class getStrategyClass();
    public T transition(BaseResp obj) throws Exception ;
//    public BaseResp<T> transition(BaseResp obj) throws Exception ;
}
