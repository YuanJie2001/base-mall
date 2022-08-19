package com.vector.mallproduct.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.vector.mallproduct.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @ClassName AttrGroupWithAttrsVo
 * 
 * @Author YuanJie
 * @Date 2022/7/12 10:43
 */
@Data
public class AttrGroupWithAttrsVo {

    /**
     * 分组id
     */
    @TableId
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catalogId;

    private List<AttrEntity> attrs;
}
