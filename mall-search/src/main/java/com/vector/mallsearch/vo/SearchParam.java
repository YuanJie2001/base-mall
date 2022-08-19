package com.vector.mallsearch.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SearchParam
 * @Description 封装页面所有可能传递过来的查询条件
 * @Author YuanJie
 * @Date 2022/8/12 1:04
 */
@Data
public class SearchParam {
    private String keyword; // 页面传递过来匹配关键字
    private Long catalog3Id; // 三级分类id
    private String sort; //排序条件
    /**
     * 过滤条件 hasStock是否有货、skuPrice区间、brandId、 cataLog3Id、attrs
     */
    private Integer hasStock; // 是否有货(0无库存,1有库存)
    private String skuPrice; // 价格区间
    private List<Long> brandId; // 按照品牌查询
    private List<String> attrs; // 按照属性进行筛选
    private Integer pageNum = 1; // 页码
    private String _queryString; // 原生的所有查询条件
}
