package com.vector.mallcoupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vector.mallcoupon.entity.MemberPriceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 15:38:07
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {

}
