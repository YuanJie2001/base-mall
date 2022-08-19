package com.vector.mallsearch.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.vector.common.to.es.SkuEsModel;
import com.vector.mallsearch.constant.EsConstant;
import com.vector.mallsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @ClassName ProductSaveServiceImpl
 *
 * @Author YuanJie
 * @Date 2022/8/4 22:29
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {
    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 保存到es
        // 1.给es建立索引product,建立映射关系
        BulkRequest.Builder builder = new BulkRequest.Builder();
        for (SkuEsModel skuEsModel : skuEsModels) {
            builder.operations(op -> op
                    .index(idx -> idx
                            .index(EsConstant.PRODUCT_INDEX)
                            .id(skuEsModel.getSkuId().toString())
                            .document(skuEsModel))
            );
        }
        BulkResponse result = elasticsearchClient.bulk(builder.build());
        // Log errors, if any
        boolean b = result.errors();
        if (b) {
            log.error("商品上架错误!");
            for (BulkResponseItem item : result.items()) {
                if (item.error() != null) {
                    log.error(item.error().reason());
                }
            }
        }
        return !b;
    }
}
