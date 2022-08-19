package com.vector.mallThirdParty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @ClassName MallThirdPartyApplication
 * 
 * @Author YuanJie
 * @Date 2022/7/1 19:42
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MallThirdPartyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallThirdPartyApplication.class, args);
    }
}
