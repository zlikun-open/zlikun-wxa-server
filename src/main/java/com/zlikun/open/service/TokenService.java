package com.zlikun.open.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import static com.zlikun.open.config.AppConstants.KEY_OPEN_ID;
import static com.zlikun.open.config.AppConstants.KEY_SESSION_KEY;

/**
 * @author zlikun
 * @date 2018-10-09 16:57
 */
@Service
public class TokenService {

    @Autowired
    private Cache cache;

    public String createToken(String openId, String sessionKey) {
        String token = DigestUtils.md5Hex(String.format("%s:%s", openId, sessionKey));
        this.cache.put(String.format("%s:%s", token, KEY_OPEN_ID), openId);
        this.cache.put(String.format("%s:%s", token, KEY_SESSION_KEY), sessionKey);
        return token;
    }

    public String getOpenId(String token) {
        Cache.ValueWrapper wrapper = this.cache.get(String.format("%s:%s", token, KEY_OPEN_ID));
        if (wrapper != null && wrapper.get() != null) {
            return wrapper.get().toString();
        }
        return null;
    }

    public String getSessionKey(String token) {
        Cache.ValueWrapper wrapper = this.cache.get(String.format("%s:%s", token, KEY_SESSION_KEY));
        if (wrapper != null && wrapper.get() != null) {
            return wrapper.get().toString();
        }
        return null;
    }

}
