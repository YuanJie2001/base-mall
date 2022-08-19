package com.vector.mallcoupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vector.mallcoupon.entity.HomeSubjectEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 15:38:08
 */
@Mapper
public interface HomeSubjectDao extends BaseMapper<HomeSubjectEntity> {

}
