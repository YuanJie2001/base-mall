package com.vector.mallproduct.service.impl;

import com.vector.mallproduct.vo.Skus;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.Query;

import com.vector.mallproduct.dao.SkuImagesDao;
import com.vector.mallproduct.entity.SkuImagesEntity;
import com.vector.mallproduct.service.SkuImagesService;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuImages(Long skuId, Skus sku) {
        List<SkuImagesEntity> imagesEntities = sku.getImages().stream()
                .filter(images -> StringUtils.isNotBlank(images.getImgUrl())).map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).collect(Collectors.toList());
        this.saveBatch(imagesEntities);
    }

    /**
     * 查询sku图片信息
     */
    @Override
    public List<SkuImagesEntity> getImagesBySkuId(Long skuId) {
        return baseMapper.selectList(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
    }
}