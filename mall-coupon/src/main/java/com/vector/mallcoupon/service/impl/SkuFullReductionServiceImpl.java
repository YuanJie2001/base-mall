package com.vector.mallcoupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vector.common.to.MemberPriceTo;
import com.vector.common.to.SkuReductionTo;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.Query;
import com.vector.mallcoupon.dao.SkuFullReductionDao;
import com.vector.mallcoupon.entity.SkuFullReductionEntity;
import com.vector.mallcoupon.entity.SkuLadderEntity;
import com.vector.mallcoupon.service.MemberPriceService;
import com.vector.mallcoupon.service.SkuFullReductionService;
import com.vector.mallcoupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    SkuLadderService skuLadderService;
    @Resource
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 5.4 sms_sku_ladder
        // 1)
        if (skuReductionTo.getFullCount() > 0) {
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
            skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
            skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
            skuLadderService.save(skuLadderEntity);
        }

        // 2) sms_sku_full_reduction
        if (skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
            SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
            BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
            this.save(skuFullReductionEntity);
        }

        // 3) sms_member_price
        List<MemberPriceTo> memberPrice = skuReductionTo.getMemberPrice();
        memberPriceService.saveBatchMemberPrice(skuReductionTo, memberPrice);

    }

}
