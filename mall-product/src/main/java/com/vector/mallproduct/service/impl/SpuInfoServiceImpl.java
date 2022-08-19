package com.vector.mallproduct.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vector.common.to.SkuHasStockTo;
import com.vector.common.to.SpuBoundTo;
import com.vector.common.to.es.SkuEsModel;
import com.vector.common.utils.R;
import com.vector.mallproduct.entity.*;
import com.vector.mallproduct.openfeign.CouponOpenFeinService;
import com.vector.mallproduct.openfeign.SearchOpenFeignService;
import com.vector.mallproduct.openfeign.WareOpenFeinService;
import com.vector.mallproduct.service.*;
import com.vector.mallproduct.vo.*;
import constant.StatusEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.Query;

import com.vector.mallproduct.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private SpuImagesService spuImagesService;
    @Resource
    private ProductAttrValueService productAttrValueService;
    @Resource
    private AttrService attrService;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private CouponOpenFeinService couponOpenFeinService;
    @Resource
    private BrandService brandService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private WareOpenFeinService wareOpenFeinService;
    @Resource
    private SearchOpenFeignService searchOpenFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    // TODO 高级部分完善失败处理逻辑
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1. 保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        Long id = spuInfoEntity.getId();
        // 2. 保存spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        spuInfoDescService.saveSpuInfoDesc(id, decript);
        // 3. 保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(id, images);
        // 4. 保存spu的规格参数: pms_prduct_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        productAttrValueService.saveProductAttrValue(id, baseAttrs);
        // 5. 保存spu积分信息 sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(id);
        if (spuBoundTo.getBuyBounds().compareTo(new BigDecimal("0")) > 0 ||
                spuBoundTo.getGrowBounds().compareTo(new BigDecimal("0")) > 0) {
            R r = couponOpenFeinService.saveSpuBounds(spuBoundTo);
            if (r.getCode() != 0) {
                log.error("远程保存spu积分信息失败!");
            }
        }

        // 5. 保存当前spu对应的sku信息
        List<Skus> skus = vo.getSkus();
        skuInfoService.saveSkuInfo(spuInfoEntity, skus);

    }

    @Override
    public PageUtils queryPageByCondition(SpuInfoFindRagneVo spuInfoFindRagneVo) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        wrapper.nested(w -> w.apply("1=1").eq(StringUtils.isNotBlank(spuInfoFindRagneVo.getKey()),
                                "id", spuInfoFindRagneVo.getKey()).or()
                        .like(StringUtils.isNotBlank(spuInfoFindRagneVo.getKey()),
                                "spu_name", spuInfoFindRagneVo.getKey()))
                .eq(StringUtils.isNotBlank(spuInfoFindRagneVo.getBrandId()) && !"0".equalsIgnoreCase(spuInfoFindRagneVo.getBrandId()),
                        "brand_id", spuInfoFindRagneVo.getBrandId())
                .eq(StringUtils.isNotBlank(spuInfoFindRagneVo.getCatalogId()) && !"0".equalsIgnoreCase(spuInfoFindRagneVo.getCatalogId()),
                        "catalog_id", spuInfoFindRagneVo.getCatalogId())
                .eq(StringUtils.isNotBlank(spuInfoFindRagneVo.getStatus()),
                        "publish_status", spuInfoFindRagneVo.getStatus());
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(new HashMap<>()), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1.查询当前spuId对应的所有sku信息
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //  查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrListforspu = productAttrValueService.baseAttrListforspu(spuId);
        List<Long> attrIds = baseAttrListforspu.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<AttrEntity> attrEntities = attrService.selectSearchAttrs(attrIds);
        Set<Long> searchAttrIds = attrEntities.stream().map(AttrEntity::getAttrId).collect(Collectors.toSet());
        List<SkuEsModel.Attrs> attrs = baseAttrListforspu.stream().filter(i -> searchAttrIds.contains(i.getAttrId())).map(item -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        // 发送远程调用,库存系统是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareOpenFeinService.getSkuHasStock(skuIds);
            TypeReference<List<SkuHasStockTo>> typeReference = new TypeReference<List<SkuHasStockTo>>() {
            };
            stockMap = r.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常: 原因{}", e);
        }
        Map<Long, Boolean> finalStockMap = stockMap;
        // 2.封装每一个sku信息
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(sku -> {
            // 组装需要的数据
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            // skuPrice,skuImg,brandName,brandImg,catalogName;
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            // hasStock,hotSorce,
            // 设置库存信息
            if (!Optional.ofNullable(finalStockMap).isPresent()) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            //  热度评分 默认0
            skuEsModel.setHotScore(0L);
            //  查询品牌和分类名字信息
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandImg(brand.getLogo());
            skuEsModel.setBrandName(brand.getName());

            CategoryEntity categoryEntity = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            // 设置检索属性
            skuEsModel.setAttrs(attrs);
            return skuEsModel;
        }).collect(Collectors.toList());
        //  发送给es保存
        R r = searchOpenFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {
            //  修改上架状态
            baseMapper.updateSpuStatus(spuId, StatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            //TODO 重复调用? 接口幂等性;重试机制
            // 1.Feign调用流程
            /**
             * 1.构造请求数据,将对象转为json
             * RequestTemplate template = this.buildTemplateFromArgs.create(argv);
             * 2.发送请求进行执行 (执行成功会解码响应数据)
             * return this.executeAndDecode(template, options);
             * 3.执行请求会有重试机制
             *  while(true){
             *      try{
             *          return this.executeAndDecode(template, options);
             *      }catch{
             *              RetryableException e = var9;
             *              try {
             *                  retryer.continueOrPropagate(e);
             *              }catch (RetryableException var8) {
             *                     Throwable cause = var8.getCause();
             *                     if (this.propagationPolicy == ExceptionPropagationPolicy.UNWRAP && cause != null) {
             *                         throw cause;
             *                     }
             *                     throw var8;
             *      }
             *  }
             *
             */
        }

    }

    private void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

}
