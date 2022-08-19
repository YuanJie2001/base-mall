package com.vector.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallproduct.entity.AttrGroupEntity;
import com.vector.mallproduct.vo.AttrGroupWithAttrsVo;
import com.vector.mallproduct.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:27
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catalogId);

    List<AttrGroupWithAttrsVo> getAttrgroupWithAttrsByCatalogId(Long catlogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

