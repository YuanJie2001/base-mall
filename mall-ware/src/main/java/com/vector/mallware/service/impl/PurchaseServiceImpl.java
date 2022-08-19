package com.vector.mallware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.Query;
import com.vector.mallware.dao.PurchaseDao;
import com.vector.mallware.entity.PurchaseDetailEntity;
import com.vector.mallware.entity.PurchaseEntity;
import com.vector.mallware.service.PurchaseDetailService;
import com.vector.mallware.service.PurchaseService;
import com.vector.mallware.service.WareSkuService;
import com.vector.mallware.vo.MergeVo;
import com.vector.mallware.vo.PurchaseDoneVo;
import com.vector.mallware.vo.PurchaseItemDoneVo;
import constant.PurchaseDetailStatusEnum;
import constant.PurchaseSatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Resource
    private PurchaseDetailService detailService;

    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
                        .eq("status", 0)
                        .or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purcharseId = mergeVo.getPurcharseId();
        if (purcharseId == null) {
            // 1.新建一个
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(PurchaseSatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purcharseId = purchaseEntity.getId();
        }
        ;

        List<Long> items = mergeVo.getItems();
        Long finalPurcharseId = purcharseId;

        // TODO 循环查库
        List<PurchaseDetailEntity> collect = items.stream().filter(item -> {
            PurchaseEntity purchaseEntity = this.getById(item);
            if (purchaseEntity != null) {
                return (purchaseEntity.getStatus() == PurchaseDetailStatusEnum.CREATED.getCode() ||
                        purchaseEntity.getStatus() == PurchaseSatusEnum.ASSIGNED.getCode());
            }
            return false;
        }).map(i -> {
            if (i != null) {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(i);
                purchaseDetailEntity.setPurchaseId(finalPurcharseId);
                purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }
            return null;
        }).collect(Collectors.toList());
        detailService.updateBatchById(collect);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purcharseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
        // 1. 确认当前采购单是新建或是已分配状态
        List<PurchaseEntity> collect = ids.stream().map(this::getById).filter(item -> {
            return item.getStatus() == PurchaseSatusEnum.CREATED.getCode() ||
                    item.getStatus() == PurchaseSatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            item.setStatus(PurchaseSatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        // 2. 改变采购单状态
        this.updateBatchById(collect);

        // 3. 刷新页面采购单采购项状态
        collect.forEach((item) -> {
            List<PurchaseDetailEntity> purchaseDetailEntities =
                    detailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> detailEntities =
                    purchaseDetailEntities.stream().map(entity -> {
                        PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                        purchaseDetailEntity.setId(entity.getId());
                        purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.BUYING.getCode());
                        return purchaseDetailEntity;
                    }).collect(Collectors.toList());
            detailService.updateBatchById(detailEntities);
        });
    }

    @Override
    public void done(PurchaseDoneVo doneVo) {
        // 1. 改变采购单状态
        Long id = doneVo.getId();

        // 2. 改变采购项的状态
        boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();

        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == PurchaseSatusEnum.HASERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(PurchaseSatusEnum.HASERROR.getCode());
            } else {
                detailEntity.setStatus(PurchaseSatusEnum.FINISH.getCode());
                // 3. 将成功的采购进行入库
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        detailService.updateBatchById(updates);
        // 1. 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag
                ? PurchaseSatusEnum.FINISH.getCode()
                : PurchaseSatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}
