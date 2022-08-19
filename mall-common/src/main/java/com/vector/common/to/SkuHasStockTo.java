package com.vector.common.to;

import lombok.Data;

/**
 * @ClassName SkuHasStockVo
 * 
 * @Author YuanJie
 * @Date 2022/8/4 17:31
 */
@Data
public class SkuHasStockTo {
    private Long skuId;
    private Boolean hasStock;

}
