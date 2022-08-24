```
谷粒商城是尚硅谷雷丰阳老师教学的一套B2C商城项目，项目由业务集群系统+后台管理系统构成，打通了分布式开发及全栈开发技能，包含前后分离全栈开发、Restful接口、数据校验、网关、注册发现、配置中心、熔断、限流、降级、链路追踪、性能监控、压力测试、系统预警、集群部署、持续集成、持续部署等
项目结构：
mall
├── mall-common -- 工具类
├── renren-generator -- 代码生成器（人人开源项目）
├── renren-fast -- 后台管理系统（人人开源项目）
├── renren-fast-vue -- 后台管理前端系统（人人开源项目）
├── mall-auth-server -- 认证中心（社交登录、OAuth2.0、单点登录）
├── mall-cart -- 购物车服务
├── mall-coupon -- 优惠卷服务
├── mall-member -- 会员服务
├── mall-gateway -- 网关服务
├── mall-order -- 订单服务
├── mall-product -- 商品服务
├── mall-search -- 检索服务
├── mall-seckill -- 秒杀服务
├── mall-third-party -- 第三方服务
└── mall-ware -- 仓储服务

问题汇总：
    1、项目拉取后无法直接运行，请先根据《环境搭建.md》搭建好运行环境，例如nacos、redis、mysql、rabbitmq等等
    2、查看笔记图片无法显示的问题，请将笔记与assets文件夹放在同一目录
    3、sql脚本存放了建库建表语句
    4、静态资源\html文件夹，是可以直接放入到nginx目录下的

觉得笔记不错的可以给个star，谢谢大家；

本项目是基于
spring-boot-dependencies 2.6.3
spring-cloud-dependencies 2021.0.1
spring-cloud-alibaba-dependencies 2021.0.1.0
nacos 1.4.2
nginx:last
elatiscsearch: 7.17.3
redis 6
mysql 8.26.3
```



