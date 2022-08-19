/**
 * Copyright 2022 bejson.com
 */
package com.vector.mallproduct.vo;

import com.vector.common.valid.AddGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2022-07-12 13:46:34
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo {
    @NotNull(message = "基本属性名必须提交", groups = {AddGroup.class})
    private String spuName;
    @NotNull(message = "基本介绍必须提交", groups = {AddGroup.class})
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;
    private List<String> decript;

    @NotNull(message = "图片集必须提交", groups = {AddGroup.class})
    private List<String> images;
    private Bounds bounds;
    @NotNull(message = "基本属性信息必须提交", groups = {AddGroup.class})
    private List<BaseAttrs> baseAttrs;

    @NotNull(message = "销售信息必须提交", groups = {AddGroup.class})
    private List<Skus> skus;


}
