package com.vector.mallproduct.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WangJiaHui
 * @description: test
 * @ClassName RedissonConfig
 * @date 2022/3/5 10:11
 */
@EnableCaching
@Configuration
@Slf4j
public class RedissonConfig {
    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        //config.useClusterServers().addNodeAddress("127.0.0.1:6379").setPassword("123456");
        config.useSingleServer().setAddress("redis://192.168.68.3:6379");
        return Redisson.create(config);
    }

    // springCache整合Redisson
    @Bean
    CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();
        /**
         * maxIdleTime允许缓存对象保留在比maxIdleTime更短的时间段内，只要它被请求
         * ttl 将使缓存的对象在几秒钟后失效，无论请求多少次或何时。
         * 创建一个名称为"category"的缓存，过期时间ttl为24分钟，同时最长空闲时maxIdleTime为12分钟。
         */
        CacheConfig cacheConfig = new CacheConfig(24 * 60 * 1000, 12 * 60 * 1000);
        config.put("category", cacheConfig);
        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(redissonClient, config);
        cacheManager.setCodec(new JsonJacksonCodec()); // 默认jack序列化
        cacheManager.setAllowNullValues(true); // 允许缓存空值
        // cacheManager.setCacheNames(); // 定义“固定”缓存名称 (Collection<String> names)
        // cacheManager.setTransactionAware(true); 定义缓存是否能够识别 Spring 管理的事务。
        // cacheManager.setConfig(); 设置按缓存名称映射的缓存配置 (Map<String,? extends CacheConfig> config)
        return cacheManager;
    }
}
