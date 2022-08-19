package com.vector.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallproduct.entity.SkuSaleAttrValueEntity;
import com.vector.mallproduct.vo.Attr;
import com.vector.mallproduct.vo.SkuItemSaleAttrVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:26
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuSaleAttrValue(Long skuId, List<Attr> attr);

    List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId);
}

