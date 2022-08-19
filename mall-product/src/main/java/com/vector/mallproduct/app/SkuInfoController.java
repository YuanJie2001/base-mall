package com.vector.mallproduct.app;

import com.vector.common.utils.PageUtils;
import com.vector.common.utils.R;
import com.vector.common.valid.FindGroup;
import com.vector.mallproduct.entity.SkuInfoEntity;
import com.vector.mallproduct.service.SkuInfoService;
import com.vector.mallproduct.vo.SkuInfoFindRangeVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;


/**
 * sku信息
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:29:42
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@Validated({FindGroup.class}) SkuInfoFindRangeVo skuInfoFindRangeVo) {
        PageUtils page = skuInfoService.queryPageByCondition(skuInfoFindRangeVo);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SkuInfoEntity skuInfo) {
        skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SkuInfoEntity skuInfo) {
        skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] skuIds) {
        skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
