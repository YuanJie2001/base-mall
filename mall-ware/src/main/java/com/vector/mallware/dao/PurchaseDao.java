package com.vector.mallware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vector.mallware.entity.PurchaseEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购单
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 17:31:39
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {

}
