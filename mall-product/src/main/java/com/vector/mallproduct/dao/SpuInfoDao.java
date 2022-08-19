package com.vector.mallproduct.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vector.mallproduct.entity.SpuInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:26
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
