package com.vector.mallproduct.service.impl;

import com.vector.common.to.SkuReductionTo;
import com.vector.common.utils.R;
import com.vector.mallproduct.entity.SkuImagesEntity;
import com.vector.mallproduct.entity.SpuInfoDescEntity;
import com.vector.mallproduct.entity.SpuInfoEntity;
import com.vector.mallproduct.openfeign.CouponOpenFeinService;
import com.vector.mallproduct.service.SkuImagesService;
import com.vector.mallproduct.service.SkuSaleAttrValueService;
import com.vector.mallproduct.service.SpuInfoDescService;
import com.vector.mallproduct.vo.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.Query;

import com.vector.mallproduct.dao.SkuInfoDao;
import com.vector.mallproduct.entity.SkuInfoEntity;
import com.vector.mallproduct.service.SkuInfoService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    private CouponOpenFeinService couponOpenFeinService;
    @Resource
    SpuInfoDescService spuInfoDescService;
    @Resource
    AttrGroupServiceImpl attrGroupService;

    @Resource
    ThreadPoolExecutor executor;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSkuInfo(SpuInfoEntity spuInfoEntity, List<Skus> skus) {
        if (CollectionUtils.isNotEmpty(skus)) {
            skus.forEach(sku -> {
                String defaultImg = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                // 5.1 sku的基本信息 pms_sku_info
                this.saveBaseSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                // 5.2 pms_sku_images
                skuImagesService.saveSkuImages(skuId, sku);
                // 5.3 pms_sku_sale_attr_value
                List<Attr> attr = sku.getAttr();
                skuSaleAttrValueService.saveSkuSaleAttrValue(skuId, attr);
                // 5.4 sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                    R r = couponOpenFeinService.saveSkuReduction(skuReductionTo);
                    if (r.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败!");
                    }
                }
            });
        }
    }

    @Override
    public PageUtils queryPageByCondition(SkuInfoFindRangeVo skuInfoFindRangeVo) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        wrapper.nested(w -> w.apply("1=1").eq(StringUtils.isNotBlank(skuInfoFindRangeVo.getKey()),
                                "sku_id", skuInfoFindRangeVo.getKey()).or()
                        .eq(StringUtils.isNotBlank(skuInfoFindRangeVo.getKey()),
                                "sku_name", skuInfoFindRangeVo.getKey()))
                .eq(StringUtils.isNotBlank(skuInfoFindRangeVo.getBrandId()) && !"0".equalsIgnoreCase(skuInfoFindRangeVo.getBrandId()),
                        "brand_id", skuInfoFindRangeVo.getBrandId())
                .eq(StringUtils.isNotBlank(skuInfoFindRangeVo.getCatalogId()) && !"0".equalsIgnoreCase(skuInfoFindRangeVo.getCatalogId()),
                        "catalog_id", skuInfoFindRangeVo.getCatalogId());
        // 在 左范围小于右范围,且均不为默认的0 0 才开始查询
        if (StringUtils.isNotBlank(skuInfoFindRangeVo.getMax()) && StringUtils.isNotBlank(skuInfoFindRangeVo.getMin())) {
            if (skuInfoFindRangeVo.getMax().compareTo(skuInfoFindRangeVo.getMin()) > 0) {
                wrapper.le("price", skuInfoFindRangeVo.getMax())
                        .ge("price", skuInfoFindRangeVo.getMin());
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(new HashMap<>()),
                wrapper);
        return new PageUtils(page);
    }

    /**
     * 查询skuId商品信息，封装VO返回
     */
    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo result = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            // 1.获取sku基本信息（pms_sku_info）【默认图片、标题、副标题、价格】
            SkuInfoEntity skuInfo = getById(skuId);
            result.setInfo(skuInfo);
            return skuInfo;
        }, executor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            // 2.获取sku图片信息（pms_sku_images）
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            result.setImages(images);
        }, executor);


        CompletableFuture<Void> saleAttrFuture = skuInfoFuture.thenAcceptAsync((skuInfo) -> {
            // 4.获取当前sku所属spu下的所有销售属性组合（pms_sku_info、pms_sku_sale_attr_value）
            List<SkuItemSaleAttrVo> saleAttr = skuSaleAttrValueService.getSaleAttrBySpuId(skuInfo.getSpuId());
            result.setSaleAttr(saleAttr);
        }, executor);

        CompletableFuture<Void> descFuture = skuInfoFuture.thenAcceptAsync((skuInfo) -> {
            // 5.获取spu商品介绍（pms_spu_info_desc）【描述图片】
            SpuInfoDescEntity desc = spuInfoDescService.getById(skuInfo.getSpuId());
            result.setDesc(desc);
        }, executor);

        CompletableFuture<Void> groupAttrsFuture = skuInfoFuture.thenAcceptAsync((skuInfo) -> {
            // 6.获取spu规格参数信息（pms_product_attr_value、pms_attr_attrgroup_relation、pms_attr_group）
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(skuInfo.getSpuId(), skuInfo.getCatalogId());
            result.setGroupAttrs(groupAttrs);
        }, executor);

        // 等待所有任务都完成
        CompletableFuture.allOf(imagesFuture, saleAttrFuture, descFuture, groupAttrsFuture).get();

        return result;
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    private void saveBaseSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }
}
