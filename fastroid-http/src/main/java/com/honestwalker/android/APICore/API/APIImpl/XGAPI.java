package com.honestwalker.android.APICore.API.APIImpl;

import android.content.Context;

import com.honestwalker.android.APICore.API.APIListener;
import com.honestwalker.android.APICore.API.BaseAPI;
import com.honestwalker.android.APICore.API.req.XGTokenReq;
import com.honestwalker.android.APICore.API.resp.BaseResp;

/**
 * Created by lanzhe on 16-8-5.
 */
public class XGAPI extends BaseAPI {

    private static XGAPI instance;
    public XGAPI(Context context) {
        super(context);
    }

    public static XGAPI getInstance(Context context) {
        if (instance == null) {
            instance = new XGAPI(context);
        } else {
            instance.context = context;
        }
        return instance;
    }

    /**
     * 注册token
     * @param token
     * @param deviceID
     * @param listener
     */
    public void registerToken(String token , String deviceID, APIListener<BaseResp> listener) {
        XGTokenReq req = new XGTokenReq();
        req.token = token;
        req.device_id = deviceID;
        request(req, listener , BaseResp.class);
    }

}
