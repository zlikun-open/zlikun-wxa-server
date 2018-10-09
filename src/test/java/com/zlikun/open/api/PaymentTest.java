package com.zlikun.open.api;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.zlikun.open.wxa.WXPayConfigImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * 微信支付API测试 <br>
 * https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_sl_api.php?chapter=7_4&index=3 <br>
 *
 * @author zlikun
 * @date 2018-09-26 11:54
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentTest {

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

    @Test
    public void test() throws Exception {

        String openId = "owh1s5cs-Bf5u7SrSLzrlIAKw9nA";

        // 统一下单
        // https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_sl_api.php?chapter=9_1&index=1
        WXPay pay = new WXPay(new WXPayConfigImpl(appid, mch_id, api_key));

        Map<String, String> requestData = new HashMap<>();
        requestData.put("sub_appid", sub_appid);
        requestData.put("sub_mch_id", sub_mch_id);
        requestData.put("body", "测试支付");
        requestData.put("out_trade_no", "1217752501201407033233368018");
        requestData.put("total_fee", "1");
        requestData.put("spbill_create_ip", "116.228.16.198");
        requestData.put("notify_url", "https://open.zlikun.com/weixin/notify");
        requestData.put("trade_type", "JSAPI");
        requestData.put("sub_openid", openId);

        Map<String, String> resultData = pay.unifiedOrder(requestData);
        /*
        {
        nonce_str=8e8dc866ae6240a882fe752bfe3f91d6,
        sign=5DFE1A6D691FD973EF7296FAF6141466,
        sub_appid=***,
        sub_mch_id=***,
        body=测试支付,
        notify_url=https://open.zlikun.com/weixin/notify,
        mch_id=***,
        spbill_create_ip=116.228.16.198,
        sub_openid=owh1s5cs-Bf5u7SrSLzrlIAKw9nA,
        out_trade_no=1217752501201407033233368018,
        total_fee=1,
        appid=***,
        trade_type=JSAPI,
        sign_type=MD5
        }
         */
        System.out.println(requestData);
        /*
        {nonce_str=B3sHJhlcRobYY3bW,
        appid=***,
        sign=F9EF731F79287D1D414B5121C480D1A1,
        trade_type=JSAPI,
        return_msg=OK,
        result_code=SUCCESS,
        mch_id=***,
        sub_mch_id=***,
        sub_appid=***,
        return_code=SUCCESS,
        prepay_id=wx10162119620862d280c1bd771513244621}
         */
        System.out.println(resultData);

        // 将数据再次签名
        // https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_sl_api.php?chapter=7_4&index=3
        // https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_sl_api.php?chapter=7_7&index=3
        // sub_appid, nonce_str, prepay_id | sign_type, timestamp
        Map<String, String> newData = new HashMap<>();
        newData.put("signType", WXPayConstants.MD5);
        newData.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        newData.put("appId", resultData.get("sub_appid"));
        newData.put("nonceStr", resultData.get("nonce_str"));
        newData.put("package", String.format("prepay_id=%s", resultData.get("prepay_id")));
        String signature = WXPayUtil.generateSignature(newData, api_key);
        newData.put("sign", signature);

        /*
        {timeStamp=1539160954,
        package=prepay_id=wx10164348073665d280c1bd771168914145,
        appId=***,
        signType=MD5,
        sign=735EEE51EF8AADD82778957379B4DDC0,
        nonceStr=C2qme1Go2oCD6SZ7}
         */
        System.out.println(newData);

        // 多次签名，结果应一致
        String signature2 = WXPayUtil.generateSignature(newData, api_key);
        assertEquals(signature, signature2);
        System.out.println(signature2);

        // 使用XML方式签名结果也应一致
        String signedXml = WXPayUtil.generateSignedXml(newData, api_key);
        System.out.println(signedXml);
        System.out.println(api_key);

    }

}
