package com.vector.mallproduct.app;

import com.vector.common.utils.PageUtils;
import com.vector.common.utils.R;
import com.vector.mallproduct.entity.SkuImagesEntity;
import com.vector.mallproduct.service.SkuImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * sku图片
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:29:42
 */
@RestController
@RequestMapping("product/skuimages")
public class SkuImagesController {
    @Autowired
    private SkuImagesService skuImagesService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = skuImagesService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SkuImagesEntity skuImages = skuImagesService.getById(id);

        return R.ok().put("skuImages", skuImages);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SkuImagesEntity skuImages) {
        skuImagesService.save(skuImages);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SkuImagesEntity skuImages) {
        skuImagesService.updateById(skuImages);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        skuImagesService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
