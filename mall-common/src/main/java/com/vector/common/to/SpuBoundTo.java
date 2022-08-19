package com.vector.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName SpuBoundTo
 * 
 * @Author YuanJie
 * @Date 2022/7/13 18:17
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
