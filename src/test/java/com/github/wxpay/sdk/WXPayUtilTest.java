package com.github.wxpay.sdk;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * 微信提供工具测试 <br>
 * 签名算法：https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_sl_api.php?chapter=4_3 <br>
 * 签名校验工具：https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=20_1 <br>
 *
 * @author zlikun
 * @date 2018-09-26 11:39
 */
@Slf4j
public class WXPayUtilTest {

    final String key = "admin#2018@zlikun.com";

    /**
     * 生成随机字符串
     */
    @Test
    public void generateNonceStr() {
        // 44d21df4d95346a6b449f2814f6dfd81
        log.info(WXPayUtil.generateNonceStr());
    }

    @Test
    public void timestamp() {
        // 毫秒时间戳：1539067974217
        log.info("毫秒时间戳：{}", System.currentTimeMillis());
        // 秒级时间戳：1539067974
        log.info("秒级时间戳：{}", System.currentTimeMillis() / 1000);
    }

    /**
     * 生成签名
     */
    @Test
    public void generateSignature() throws Exception {

        Map<String, String> data = new HashMap<>();
        data.put("appid", "$wxa_app_id");
        data.put("mch_id", "$wxa_mch_id");
        data.put("device_info", "nothing");
        data.put("body", "买一架宇宙飞船");
        data.put("nonce_str", "44d21df4d95346a6b449f2814f6dfd81");
        // 默认签名算法MD5
        String signature = WXPayUtil.generateSignature(data, key);
        assertEquals("565D3A68A0CCF90BA0EE833530A216B4", signature);

        // 指定使用HMACSHA256算法签名
        String signature2 = WXPayUtil.generateSignature(data, key, WXPayConstants.SignType.HMACSHA256);
        assertEquals("B067979B23B26E6E905C9D8BA19CCC37D07559E3184FD844D5AB65C3C238FE0D", signature2);

        // 生成带有签名的XML信息（默认MD5），将该信息粘入在线验签工具可以验证本测试是否正确
        String xml = WXPayUtil.generateSignedXml(data, key);
        /* -------------------------------------------------
        <xml>
        <nonce_str>44d21df4d95346a6b449f2814f6dfd81</nonce_str>
        <device_info>nothing</device_info>
        <appid>$wxa_app_id</appid>
        <sign>565D3A68A0CCF90BA0EE833530A216B4</sign>
        <mch_id>$wxa_mch_id</mch_id>
        <body>买一架宇宙飞船</body>
        </xml>
        ------------------------------------------------- */
        log.info(xml);

        // 指定使用HMACSHA256算法签名
        xml = WXPayUtil.generateSignedXml(data, key, WXPayConstants.SignType.HMACSHA256);
        /* -------------------------------------------------
        <xml>
        <nonce_str>44d21df4d95346a6b449f2814f6dfd81</nonce_str>
        <device_info>nothing</device_info>
        <appid>$wxa_app_id</appid>
        <sign>B067979B23B26E6E905C9D8BA19CCC37D07559E3184FD844D5AB65C3C238FE0D</sign>
        <mch_id>$wxa_mch_id</mch_id>
        <body>买一架宇宙飞船</body>
        </xml>
        ------------------------------------------------- */
        log.info(xml);
    }

}
