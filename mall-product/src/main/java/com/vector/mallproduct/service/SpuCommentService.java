package com.vector.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallproduct.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:26
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

