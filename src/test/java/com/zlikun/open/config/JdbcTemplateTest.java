package com.zlikun.open.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author zlikun
 * @date 2018-10-09 15:19
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class JdbcTemplateTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void query() {
        jdbcTemplate.query("select 'x'", rs -> {
            assertEquals(1, rs.getRow());
        });
    }

}
