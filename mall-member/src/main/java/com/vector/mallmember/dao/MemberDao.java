package com.vector.mallmember.dao;

import com.vector.mallmember.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 15:47:30
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

}
