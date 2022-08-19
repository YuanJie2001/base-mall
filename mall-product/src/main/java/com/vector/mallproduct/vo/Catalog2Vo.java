package com.vector.mallproduct.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName Catalog2Vo
 * @Description 二级分类
 * @Author YuanJie
 * @Date 2022/8/5 22:13
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catalog2Vo {
    private String catalog1Id; // 1级父分类
    private List<Catalog3Vo> catalog3List; // 三级子分类
    private String id;
    private String name;

    /**
     * 三级分类
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catalog3Vo {
        private String catalog2Id; // 2级分类id
        private String id;
        private String name;
    }

}
