package com.vector.mallproduct.vo;


import com.vector.common.valid.FindGroup;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * @ClassName SkuInfoFindRangeVo
 * 
 * @Author YuanJie
 * @Date 2022/7/15 16:36
 */
@Data
public class SkuInfoFindRangeVo {
    private String key;
    private String page;
    private String limit;
    private String brandId;
    private String catalogId;
    @Pattern(regexp = "^[0-9]*$", message = "价格范围必须是数字", groups = {FindGroup.class})
    private String min;
    @Pattern(regexp = "^[0-9]*$", message = "价格范围必须是数字", groups = {FindGroup.class})
    private String max;
}
