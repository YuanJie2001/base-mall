package com.vector.mallproduct.openfeign;

import com.vector.common.to.SkuReductionTo;
import com.vector.common.to.SpuBoundTo;
import com.vector.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ClassName SpuOpenFeinService
 *
 * @Author YuanJie
 * @Date 2022/7/13 17:58
 */
@FeignClient("mall-coupon")
@Service
public interface CouponOpenFeinService {
    /**
     * 1.CouponOpenFeinService.saveSpuBounds(spuBoundTo);
     * 1) @RequestBody将对象转为json
     * 2) 找到 mall-coupon服务 给/coupon/spubounds/save 发送请求.将转的json放在请求体的位置
     * 3) 对方服务收到json数据
     *
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/save/info")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
