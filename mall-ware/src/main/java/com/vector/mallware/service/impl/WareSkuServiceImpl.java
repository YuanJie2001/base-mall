package com.vector.mallware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.Query;
import com.vector.common.utils.R;
import com.vector.mallware.dao.WareSkuDao;
import com.vector.mallware.entity.WareSkuEntity;
import com.vector.mallware.openFeign.ProductFeignService;
import com.vector.mallware.service.WareSkuService;
import com.vector.mallware.vo.SkuHasStockVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Resource
    private WareSkuDao wareSkuDao;

    @Resource
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wareSkuEntityQueryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        wareSkuEntityQueryWrapper
                .eq(StringUtils.isNotBlank(skuId), "sku_id", skuId)
                .eq(StringUtils.isNotBlank(wareId), "ware_id", wareId);
        ;

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wareSkuEntityQueryWrapper

        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Long skuNum) {
        // 1. 是否存在库存记录
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (CollectionUtils.isEmpty(wareSkuEntities)) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败,整个事务无需回滚
            // 1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚?
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            wareSkuEntity.setStock(skuNum);
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<WareSkuEntity> wareSkuEntities = baseMapper.selectList(new QueryWrapper<WareSkuEntity>()
                .in(CollectionUtils.isNotEmpty(skuIds), "sku_id", skuIds)
                .groupBy("sku_id")
                .select("SUM(stock-stock_locked) stock", "sku_id")
        );

        return wareSkuEntities.stream().map(sku -> {
            SkuHasStockVo stockVo = new SkuHasStockVo();
            Long stock = sku.getStock();
            stockVo.setSkuId(sku.getSkuId());
            if (Optional.ofNullable(stock).isPresent() && stock > 0) {
                stockVo.setHasStock(true);
                return stockVo;
            }
            stockVo.setHasStock(false);
            return stockVo;
        }).collect(Collectors.toList());
    }

}
