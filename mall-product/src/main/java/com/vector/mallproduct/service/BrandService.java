package com.vector.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallproduct.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:27
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

