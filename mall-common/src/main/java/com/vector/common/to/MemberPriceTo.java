package com.vector.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName MemberPriceTo
 * 
 * @Author YuanJie
 * @Date 2022/7/13 18:33
 */
@Data
public class MemberPriceTo {
    private Long id;
    private String name;
    private BigDecimal price;
}
