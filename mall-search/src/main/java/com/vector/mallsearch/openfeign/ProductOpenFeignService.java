package com.vector.mallsearch.openfeign;

import com.vector.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @ClassName ProductFeignService
 *
 * @Author YuanJie
 * @Date 2022/8/16 14:51
 */
@FeignClient("mall-product")
public interface ProductOpenFeignService {
    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);

}
