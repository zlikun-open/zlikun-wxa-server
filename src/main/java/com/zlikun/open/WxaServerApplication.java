package com.zlikun.open;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * @author zlikun
 * @date 2018-10-09 15:00
 */
@SpringBootApplication
@PropertySource("file:///${user.home}/my/zlikun-wxa-server/application.properties")
public class WxaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxaServerApplication.class, args);
    }
}
