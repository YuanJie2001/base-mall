package com.vector.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallproduct.entity.SpuInfoEntity;
import com.vector.mallproduct.vo.SpuInfoFindRagneVo;
import com.vector.mallproduct.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:04:26
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    PageUtils queryPageByCondition(SpuInfoFindRagneVo spuInfoFindRagneVo);

    /**
     * 商品上架
     *
     * @param spuId
     */
    void up(Long spuId);
}

