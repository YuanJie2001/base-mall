package com.vector.mallcoupon;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootTest
@Slf4j
class MallCouponApplicationTests {

    @Autowired
    DataSource dataSource;

    @Test
    void contextLoads() throws SQLException {
        log.info("数据源{}", dataSource.getConnection());
        log.info("检索目录{}", dataSource.getConnection().getCatalog());
        log.info("默认数据源为{}", dataSource.getConnection().getClass());
    }

}
