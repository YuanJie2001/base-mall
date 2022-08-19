package com.vector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1, 想要远程调用别的服务
 * 1),引入open-feign
 * 2),编写接口,告诉SpringCloud这个接口需要远程调用服务
 * 3),声明接口调用的每一个方法都是调用哪个远程服务的那个请求
 * 开启远程调用的功能
 */
@EnableFeignClients(basePackages = {"com.vector.mallmember.openfeign"})
@SpringBootApplication
@EnableDiscoveryClient
public class MemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }

}
