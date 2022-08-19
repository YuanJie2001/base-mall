package com.vector.mallproduct.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vector.mallproduct.entity.AttrAttrgroupRelationEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:27
 */
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);
}
