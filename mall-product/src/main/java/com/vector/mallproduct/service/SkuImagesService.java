package com.vector.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallproduct.entity.SkuImagesEntity;
import com.vector.mallproduct.vo.Skus;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:26
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuImages(Long skuId, Skus sku);

    List<SkuImagesEntity> getImagesBySkuId(Long skuId);
}

