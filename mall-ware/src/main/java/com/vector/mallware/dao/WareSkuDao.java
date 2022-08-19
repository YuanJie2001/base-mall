package com.vector.mallware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vector.mallware.entity.WareSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 17:31:39
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Long skuNum);
}
