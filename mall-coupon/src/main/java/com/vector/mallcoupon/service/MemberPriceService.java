package com.vector.mallcoupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.to.MemberPriceTo;
import com.vector.common.to.SkuReductionTo;
import com.vector.common.utils.PageUtils;
import com.vector.mallcoupon.entity.MemberPriceEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品会员价格
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 15:38:07
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatchMemberPrice(SkuReductionTo skuReductionTo, List<MemberPriceTo> memberPrice);
}

