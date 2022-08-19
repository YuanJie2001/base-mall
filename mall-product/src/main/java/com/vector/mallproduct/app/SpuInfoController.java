package com.vector.mallproduct.app;

import java.util.Arrays;


import com.vector.common.validator.group.AddGroup;
import com.vector.mallproduct.vo.SpuInfoFindRagneVo;
import com.vector.mallproduct.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.vector.mallproduct.entity.SpuInfoEntity;
import com.vector.mallproduct.service.SpuInfoService;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.R;


/**
 * spu信息
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:29:41
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;


    /**
     * 上架商品
     */
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId) {
        spuInfoService.up(spuId);

        return R.ok();
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    public R list(SpuInfoFindRagneVo spuInfoFindRagneVo) {
        PageUtils page = spuInfoService.queryPageByCondition(spuInfoFindRagneVo);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody SpuSaveVo vo) {
//		spuInfoService.save(spuInfo);
        spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuInfoEntity spuInfo) {
        spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
