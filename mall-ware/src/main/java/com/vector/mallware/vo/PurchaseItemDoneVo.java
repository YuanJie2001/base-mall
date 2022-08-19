package com.vector.mallware.vo;

import lombok.Data;

/**
 * @ClassName ItemVO
 * 
 * @Author YuanJie
 * @Date 2022/7/18 22:06
 */
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer Status;
    private String reason;
}
