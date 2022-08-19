package com.vector.mallorder.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vector.mallorder.entity.MqMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 17:24:28
 */
@Mapper
public interface MqMessageDao extends BaseMapper<MqMessageEntity> {

}
