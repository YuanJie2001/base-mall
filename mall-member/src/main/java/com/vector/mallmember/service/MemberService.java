package com.vector.mallmember.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallmember.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 15:47:30
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

