package com.zlikun.open.controller;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.zlikun.open.service.TokenService;
import com.zlikun.open.wxa.WXPayConfigImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.zlikun.open.config.AppConstants.KEY_RETURN_CODE;

/**
 * @author zlikun
 * @date 2018-10-09 18:42
 */
@Slf4j
@RestController
@RequestMapping("/pay")
public class WxaPaymentController {

    @Autowired
    private TokenService tokenService;

    @Value("${wxa.mch_id}")
    private String mch_id;
    @Value("${wxa.appid}")
    private String appid;
    @Value("${wxa.api_key}")
    private String api_key;
    @Value("${wxa.sub_mch_id}")
    private String sub_mch_id;
    @Value("${wxa.sub_appid}")
    private String sub_appid;

    @PostMapping("/unified-order")
    public Object unifiedOrder(String token) throws Exception {

        String openId = tokenService.getOpenId(token);

        // token = c67416c4c90e53c6967c8007848de921, openId = owh1s5cs-Bf5u7SrSLzrlIAKw9nA
        log.info("token = {}, openId = {}, sessionKey = {}", token, openId);

        // 统一下单
        // https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_sl_api.php?chapter=9_1&index=1
        WXPay pay = new WXPay(new WXPayConfigImpl(appid, mch_id, api_key));

        Map<String, String> requestData = new HashMap<>(8);
        requestData.put("sub_appid", sub_appid);
        requestData.put("sub_mch_id", sub_mch_id);
        requestData.put("body", "测试支付");
        requestData.put("out_trade_no", "1217752501201407033233368014");
        requestData.put("total_fee", "1");
        requestData.put("spbill_create_ip", "116.228.16.198");
        requestData.put("notify_url", "https://open.zlikun.com/weixin/notify");
        requestData.put("trade_type", "JSAPI");
        requestData.put("sub_openid", openId);

        Map<String, String> responseData = pay.unifiedOrder(requestData);
        System.out.println(requestData);
        System.out.println(responseData);

        if ("SUCCESS".equals(responseData.get(KEY_RETURN_CODE))) {
            log.info("统一下单成功!");
        } else {
            // TODO 统一下单失败
            log.error("统一下单失败!");
        }

        // 将数据再次签名
        // https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_sl_api.php?chapter=7_4&index=3
        // https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_sl_api.php?chapter=7_7&index=3
        // sub_appid, nonce_str, prepay_id | sign_type, timestamp
        Map<String, String> newData = new HashMap<>(8);
        newData.put("signType", WXPayConstants.MD5);
        newData.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        newData.put("appId", responseData.get("sub_appid"));
        newData.put("nonceStr", WXPayUtil.generateNonceStr());
        newData.put("package", String.format("prepay_id=%s", responseData.get("prepay_id")));
        String signature = WXPayUtil.generateSignature(newData, api_key, WXPayConstants.SignType.MD5);
        newData.put("sign", signature);
        /*
        {timeStamp=1539160240,
        signType=MD5,
        sign=735EEE51EF8AADD82778957379B4DDC0,
        package=prepay_id=wx10163153145256455af110040812691042,
        nonceStr=20de12893dd14102ac3a41287eb9bee2,
        appId=***}
         */
        System.out.println(newData);
        System.out.println(signature);

        return newData;

    }

}
