package com.vector.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vector.common.utils.PageUtils;
import com.vector.mallproduct.entity.CategoryEntity;
import com.vector.mallproduct.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:27
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    Long[] findCatalogPath(Long catalogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<Catalog2Vo>> getCatalogJson() throws JsonProcessingException;
}

