package com.vector.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.Query;
import com.vector.mallproduct.dao.SkuSaleAttrValueDao;
import com.vector.mallproduct.entity.SkuSaleAttrValueEntity;
import com.vector.mallproduct.service.SkuSaleAttrValueService;
import com.vector.mallproduct.vo.Attr;
import com.vector.mallproduct.vo.SkuItemSaleAttrVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuSaleAttrValue(Long skuId, List<Attr> attr) {
        if (CollectionUtils.isNotEmpty(attr)) {
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(item -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(item, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuId);
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            this.saveBatch(skuSaleAttrValueEntities);
        }
    }

    /**
     * 获取spu下的所有销售属性组合
     */
    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId) {
        // 1.通过spuId查询所有sku（pms_sku_info）
        // 2.查询sku涉及到的所有销售属性（pms_sku_sale_attr_value）
        return baseMapper.getSaleAttrBySpuId(spuId);
    }

}
