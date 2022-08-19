package com.vector.mallcoupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vector.common.to.MemberPriceTo;
import com.vector.common.to.SkuReductionTo;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.Query;
import com.vector.mallcoupon.dao.MemberPriceDao;
import com.vector.mallcoupon.entity.MemberPriceEntity;
import com.vector.mallcoupon.service.MemberPriceService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("memberPriceService")
public class MemberPriceServiceImpl extends ServiceImpl<MemberPriceDao, MemberPriceEntity> implements MemberPriceService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberPriceEntity> page = this.page(
                new Query<MemberPriceEntity>().getPage(params),
                new QueryWrapper<MemberPriceEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBatchMemberPrice(SkuReductionTo skuReductionTo, List<MemberPriceTo> memberPrice) {
        if (CollectionUtils.isNotEmpty(memberPrice)) {
            List<MemberPriceEntity> memberPriceEntities = memberPrice.stream().filter(item -> {
                return item.getPrice().compareTo(new BigDecimal("0")) > 0;
            }).map(item -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                memberPriceEntity.setMemberLevelId(item.getId());
                memberPriceEntity.setMemberLevelName(item.getName());
                memberPriceEntity.setMemberPrice(item.getPrice());
                memberPriceEntity.setAddOther(1);
                return memberPriceEntity;
            }).collect(Collectors.toList());
            this.saveBatch(memberPriceEntities);
        }
    }

}
