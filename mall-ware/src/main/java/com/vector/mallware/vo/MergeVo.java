package com.vector.mallware.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName MergeVo
 * @Author YuanJie
 * @Date 2022/7/18 12:30
 */
@Data
public class MergeVo {
    private Long purcharseId;  //整单id
    private List<Long> items; // [1,2,3,4] 合并项集合
}
