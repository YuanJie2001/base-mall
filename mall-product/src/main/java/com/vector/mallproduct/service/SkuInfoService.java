package com.vector.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallproduct.entity.SkuInfoEntity;
import com.vector.mallproduct.entity.SpuInfoEntity;
import com.vector.mallproduct.vo.SkuInfoFindRangeVo;
import com.vector.mallproduct.vo.SkuItemVo;
import com.vector.mallproduct.vo.Skus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:26
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);


    void saveSkuInfo(SpuInfoEntity spuInfoEntity, List<Skus> skus);

    PageUtils queryPageByCondition(SkuInfoFindRangeVo skuInfoFindRangeVo);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;
}

