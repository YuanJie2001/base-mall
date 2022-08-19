package com.vector.mallware.openFeign;

import com.vector.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@FeignClient("mall-product")
public interface ProductFeignService {
    /**
     * 信息
     * 1)、让所有请求过网关;
     * 1、@FeignClient("maLl-gateway")。给mall-gateway所在的机器发请求
     * 2、/api/product/skuinfo/info/{skuId}
     * 2)、直接让后台指定服务处理
     * 1、FeignClient("maLl-product")
     * 2、/product/skuinfo/info/{skuId}
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
