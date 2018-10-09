package com.zlikun.open.wxa;

import com.github.wxpay.sdk.WXPayConfig;

import java.io.InputStream;

/**
 * @author zlikun
 * @date 2018-09-26 14:46
 */
public class WXPayConfigImpl implements WXPayConfig {

    private String appId;
    private String mchId;
    private String apiKey;

    public WXPayConfigImpl(String appId, String mchId, String apiKey) {
        this.appId = appId;
        this.mchId = mchId;
        this.apiKey = apiKey;
    }

    @Override
    public String getAppID() {
        return this.appId;
    }

    @Override
    public String getMchID() {
        return this.mchId;
    }

    @Override
    public String getKey() {
        return this.apiKey;
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 2000;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 2000;
    }
}
