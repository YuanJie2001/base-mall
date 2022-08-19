package com.vector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1. 引入nacos-config,bootstrap依赖
 * 2.配置模块名. 配置中心默认命名规则
 * # ${spring.application.name}-${spring.profile.active}.${spring.cloud.nacos.config.file-extension}
 *
 * @RefreshScope动态获取并刷新配置
 * @Value("${配置项的名}):获取到的值 如果配置中心和当前应用的配置文件中都配置了相同的项，优先使用配置中心的配置。
 * <p>
 * <p>
 * 细节
 * 1)命名空间: 配置隔离 默认public(保留空间) dev/test/prod
 * 2)每一个微服务之间互相隔离配置,每一个微服务都创建自己的命名空间,只加载自己命名空间下的所有配置
 * 3)命名分组:
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallCouponApplication.class, args);
    }

}
