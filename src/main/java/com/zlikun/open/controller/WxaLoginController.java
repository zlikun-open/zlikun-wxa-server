package com.zlikun.open.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zlikun.open.config.AppConstants;
import com.zlikun.open.service.TokenService;
import com.zlikun.open.util.DecryptUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

import static com.zlikun.open.config.AppConstants.KEY_SESSION_KEY;

/**
 * 小程序登录控制器
 *
 * @author zlikun
 * @date 2018-10-09 14:12
 */
@Slf4j
@RestController
public class WxaLoginController {

    @Autowired
    private OkHttpClient client;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private TokenService tokenService;

    @Value("${wxa.sub_appid}")
    private String appId;
    @Value("${wxa.sub_app_secret}")
    private String appSecret;
    private String grantType = "authorization_code";

    /**
     * 通过微信API获取openid和session_key信息 <br>
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html <br>
     * https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.login.html <br>
     * https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/code2Session.html <br>
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/union-id.html <br>
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/signature.html <br>
     *
     * @param code
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<?> doLogin(String code) throws IOException {

        // code = 0614f75E0xHMjd2pkK2E05gd5E04f75s
        log.info("code = {}", code);

        // https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/code2Session.html
        Request request = new Request.Builder()
                .url("https://api.weixin.qq.com/sns/jscode2session")
                .post(new FormBody.Builder()
                        .add("appid", appId)
                        .add("secret", appSecret)
                        .add("js_code", code)
                        .add("grant_type", grantType)
                        .build())
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            log.error("status = {}, message = {}", response.code(), response.message());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String content = response.body().string();

        // content = {"session_key":"SDzNC+zMHi\/jHLqIvw4qwg==","openid":"owh1s5cs-Bf5u7SrSLzrlIAKw9nA"}
        log.info("content = {}", content);

        // 提取session_key和openid字段
        Map<String, String> data = mapper.readValue(content, Map.class);

        if (data.get(AppConstants.KEY_OPEN_ID) != null) {
            // openid, session_key[, unionid]
            // 获取openid和session_key信息
            String openId = data.get(AppConstants.KEY_OPEN_ID);
            String sessionKey = data.get(KEY_SESSION_KEY);
            // 加密生成token返回客户端，服务端映射token与openid和session_key关系
            String token = tokenService.createToken(openId, sessionKey);
            // token = 9b89a00aa5b0b20e01cdda8c30be27b0,
            // openId = owh1s5cs-Bf5u7SrSLzrlIAKw9nA,
            // sessionKey = SDzNC+zMHi/jHLqIvw4qwg==
            log.info("token = {}, openId = {}, sessionKey = {}", token, openId, sessionKey);
            return ResponseEntity.ok(token);
        } else {
            // errcode, errmsg
            log.error("errcode = {}, errmsg = {}", data.get("errcode"), data.get("errmsg"));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 对用户数据进行验签和解密 <br>
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/signature.html <br>
     *
     * @param token
     * @param rawData
     * @param signature
     * @param encryptedData
     * @param iv
     * @return
     */
    @PostMapping("/login/verify")
    public ResponseEntity<?> verify(String token,
                                    // 验签参数
                                    String rawData, String signature,
                                    // 解密参数
                                    String encryptedData, String iv) {
        String sessionKey = tokenService.getSessionKey(token);
        // token = e88656050f6aeea103742778b0e4cf9f, sessionKey = xrTa5e4StgI0711P/dwKBw==
        log.info("token = {}, sessionKey = {}", token, sessionKey);
        // rawData = {"nickName":"张立坤","gender":1,"language":"zh_CN","city":"","province":"","country":"中国",
        // "avatarUrl":"https://wx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTIPkIoGZuzfibBprNziagPkmbWPTrIiavXGGgYHibYwfB5
        // BoibYn6tyaGvfVAZ7owvmdiaEVicichECakKWEg/132"}
        log.info("rawData = {}", rawData);
        // signature = ad96fee2ad4b33a78f120db28ccc0a7610020cfc
        log.info("signature = {}", signature);
        // encryptedData = ETbN/SJpY7gaqSesPB72AbtOsIynizTCpUTHsBNswzqflxOamTwDP4Oo35dBoYgkILiEBX4/hqSwbtGyY4dBc
        // 6NVxxZdngDhxkrpbDPRbQeEijVCtecdm59uK3X3jxkG/bJPS05oRMLM2OXxXc0DH4/lfCq8nRWzJrpPS+SM0cv5ZeMt98fTXXPvm3
        // rasn+GPHP6JdUdPZpkrhWB9OqQw/zzHcHRR4DdWZLV3k5QoGAMmowYo421z5pVMuslPPv4gJGkEl883cJEOynq6wZIxxU8miZmncJ
        // IWou2TgTE3uu1Ry2qZt15PKjyhOEQZ898Y6jn9yw96V6F0m9pn405+fR2kt4M4m6MWkZrLLSJKGssB5xv44Kt79hyHBq3wbzeRGri
        // EFLPqYhvq/sabkhm06GNur/5T651px/DjV7sAOaunR8s4W7dKM1ChctNHM0XgHUGnxZUn16w6GlGVOi/Bg==
        log.info("encryptedData = {}", encryptedData);
        // iv = V/QQAS5lMgh8npTLH8edEQ==
        log.info("iv = {}", iv);

        // 执行验签
        String signature2 = DigestUtils.sha1Hex(String.format("%s%s", rawData, sessionKey));
        // signature2 = ad96fee2ad4b33a78f120db28ccc0a7610020cfc
        log.info("signature2 = {}", signature2);
        // 比较API返回签名与重新计算签名是否一致
        boolean flag = signature.equals(signature2);

        if (flag) {
            // 执行解密（不一定会有unionId值，取决于用户是否同时使用同一开放平台下的多个应用）
            byte[] original = DecryptUtils.wxaAesDecrypt(sessionKey, iv, encryptedData);
            // original => {"openId":"owh1s5cs-Bf5u7SrSLzrlIAKw9nA","nickName":"张立坤","gender":1,"language":"zh_CN",
            // "city":"","province":"","country":"中国","avatarUrl":"https://wx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTIP
            // kIoGZuzfibBprNziagPkmbWPTrIiavXGGgYHibYwfB5BoibYn6tyaGvfVAZ7owvmdiaEVicichECakKWEg/132",
            // "watermark":{"timestamp":1539075705,"appid":"wx1b241adf97d06107"}}
            log.info("original => {}", new String(original));
        }

        return ResponseEntity.ok(flag);
    }

}
