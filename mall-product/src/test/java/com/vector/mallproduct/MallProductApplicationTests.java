package com.vector.mallproduct;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vector.mallproduct.entity.BrandEntity;
import com.vector.mallproduct.service.BrandService;

import com.vector.mallproduct.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;


import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Slf4j
@SpringBootTest
public class MallProductApplicationTests {

    @Resource
    BrandService brandService;
    @Resource
    CategoryService categoryService;
    @Resource
    RedissonClient redissonClient;

    @Test
    public void teststringRedisTemplate() {
        System.out.println(redissonClient);
    }

    @Test
    public void testFindPath() {
        Long[] catalogPath = categoryService.findCatalogPath(225L);
        log.info("完整路径{}", Arrays.asList(catalogPath));
    }

    @Test
    public void contextLoads() {
        List<BrandEntity> list =
                brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 30L));
        list.forEach((item) -> {
            log.info("{}", item);
        });
    }

    @Resource
    DataSource dataSource;

    @Test
    void test() throws SQLException {
        log.info("数据源{}", dataSource.getConnection());
        log.info("检索目录{}", dataSource.getConnection().getCatalog());
        log.info("默认数据源为{}", dataSource.getConnection().getClass());
    }


}
