package com.vector.mallproduct.vo;

import lombok.Data;

/**
 * @ClassName AttrRespVo
 * 
 * @Author YuanJie
 * @Date 2022/7/7 14:02
 */
@Data
public class AttrRespVo extends AttrVo {

    private String catalogName;

    private String groupName;

    private Long[] catalogPath;
}
