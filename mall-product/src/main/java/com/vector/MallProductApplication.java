package com.vector;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1.整合mybatis-plus
 * 2.配置数据源: 导入数据库驱动
 * 3.配置mybatisplus
 * <p>
 * 逻辑删除
 * 配置全局的逻辑删除规则
 * 给Bean加上逻辑删除注解@TableLogic
 * <p>
 * JSR303
 * 1)给Bean添加校验注解 javax.validation.constraints
 * 2)开启校验校验功能@Valid
 * 3)给校验的结果紧跟一个BindingResult
 * 4)分组校验(根据save,update,delete,find的所需参数不同,自适应校验)
 *
 * @NotBlank(message = "品牌名必须提交",groups = {UpdateGroup.class,AddGroup.class})
 * 默认没有指定分组的校验注解@NotBLank，在分组校验情况下不生效
 * 5)自定义校验
 * 编写自定义校验注解
 * 编写自定义校验器
 * 关联校验注解和校验器
 * @Documented // 该注解修饰的值需要被谁校验
 * @Constraint( validatedBy = {ListValueConstrainValidator.class,
 * // 可以指定多个不同的校验器,适配不同的校验
 * })
 * @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
 * @Retention(RetentionPolicy.RUNTIME) 统一异常处理
 * @ControllerAdvice 1) 抽取异常处理类
 * <p>
 * 6、整合redis
 * 1)、引入data-redis-starter
 * 2)、简单配置redis的host等信息
 * 3)、使用SpringBoot自动配置好的StringRedisTemplate
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.vector.mallproduct.openfeign")
@MapperScan("com.vector.mallproduct.dao")
public class MallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallProductApplication.class, args);
    }

}
