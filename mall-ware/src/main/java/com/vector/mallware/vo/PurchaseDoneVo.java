package com.vector.mallware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName PurchaseDoneVo
 * 
 * @Author YuanJie
 * @Date 2022/7/18 22:05
 */
@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id;  // 采购单id

    private List<PurchaseItemDoneVo> items;

}
