package com.honestwalker.android.APICore.API.req;

import com.honestwalker.android.APICore.IO.API;

/**
 * 信鸽相关api
 * Created by honestwalker on 15-8-24.
 */

@API(uri="user/device-token")
public class XGTokenReq extends BaseReq {

    public String token;

    public String device_id;

}
