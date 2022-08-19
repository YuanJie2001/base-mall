package com.vector.mallcoupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vector.mallcoupon.entity.CouponSpuRelationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 15:38:07
 */
@Mapper
public interface CouponSpuRelationDao extends BaseMapper<CouponSpuRelationEntity> {

}
