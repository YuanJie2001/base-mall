package com.vector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.JsonData;
import com.vector.mallsearch.constant.EsConstant;
import com.vector.mallsearch.vo.SearchParam;
import com.vector.mallsearch.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


/**
 * @ClassName MallSearchService
 * 
 * @Author YuanJie
 * @Date 2022/8/12 1:05
 */
@SpringBootTest
@Slf4j
public class MallSearchServiceTests {
    @Resource
    private ElasticsearchClient elasticsearchClient;

    /**
     * @param searchParam 检索的所有参数
     * @return 返回检索的结果
     */
    @Test
    public void search(SearchParam searchParam) {
        // 1.动态构建dsl语句
        SearchResult searchResult = null;

        // 2.准备检索请求
        SearchRequest.Builder sourceBuilder = buildSearchRequest(searchParam);

        // 3.执行检索请求
        SearchResponse<SearchResult> response = null;
        try {
            response = elasticsearchClient.search(s -> s
                            .index("product")
                            .query(sourceBuilder.build().query())
                    , SearchResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 4. 构建结果数据
        System.out.println("数据格式:" + response.toString());


        TotalHits total = response.hits().total();
        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

        if (isExactResult) {
            log.info("There are " + total.value() + " results");
        } else {
            log.info("There are more than " + total.value() + " results");
        }
        List<Hit<SearchResult>> hits = response.hits().hits();
        for (Hit<SearchResult> hit : hits) {
            SearchResult source = hit.source();
            log.info("Found source " + source.toString());
        }
    }

    private SearchRequest.Builder buildSearchRequest(SearchParam searchParam) {
        SearchRequest.Builder sourceBuilder = new SearchRequest.Builder();
        // 模糊关键字匹配
        BoolQuery.Builder boolQuery = QueryBuilders.bool();
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            // MatchQuery匹配查询,field字段名,query查询值,fuzziness允许误差字数1,2,3
            Query bySkuTitle = MatchQuery.of(r -> r
                    .field("skuTitle")
                    .query(searchParam.getKeyword())
                    .fuzziness("2"))._toQuery();
            boolQuery.must(bySkuTitle);
        }
        // 按照三级分类id查询
        if (Optional.ofNullable(searchParam.getCatalog3Id()).isPresent()) {
            // TermQuery非文本精确匹配字段
            Query byCatalogId = TermQuery.of(r -> r
                    .field("catalogId")
                    .value(searchParam.getCatalog3Id()))._toQuery();
            boolQuery.filter(byCatalogId);
        }
        // 按照品牌id查询
        if (Optional.ofNullable(searchParam.getBrandId()).isPresent()
                && searchParam.getBrandId().size() > 0) {
            Query byTerms = TermsQuery.of(r -> r
                    .field("brandId")
                    .terms((TermsQueryField) searchParam.getBrandId()))._toQuery();
            boolQuery.filter(byTerms);
        }
        // 按照所有指定的属性查询
        if (Optional.ofNullable(searchParam.getAttrs()).isPresent()
                && searchParam.getAttrs().size() > 0) {
            /// attrs=1_5寸:8寸&attrs=2_16G:8G
            for (String attrStr : searchParam.getAttrs()) {
                // 构建bool查询
                BoolQuery.Builder nestBoolQuery = QueryBuilders.bool();
                // attr=1_5寸:8寸
                String[] s = attrStr.split("_");
                String attrId = s[0];  // 检索的属性id
                String[] attrValues = s[1].split(":"); // 检索用的值
                Query byTerm = TermQuery.of(r -> r
                        .field("attrs.attrId")
                        .value(attrId))._toQuery();
                Query byTerms = TermsQuery.of(r -> r
                        .field("attrs.attrValue")
                        .terms((TermsQueryField) JsonData.of(attrValues)))._toQuery();
                // bool整合
                nestBoolQuery.must(byTerm).must(byTerms);
                // 潜入bool查询
                Query byAttrId = NestedQuery.of(r -> r
                        .path("attrs")
                        .query(nestBoolQuery.build()._toQuery())
                        .scoreMode(null))._toQuery();
                boolQuery.filter(byAttrId);
            }


        }
        // 按照库存查询
        Query byHasStock = TermQuery.of(r -> r
                .field("hasStock")
                .value(searchParam.getHasStock() == 1))._toQuery();
        boolQuery.filter(byHasStock);
        // 按照价格区间
        if (StringUtils.isNotBlank(searchParam.getSkuPrice())) {
            String[] s = searchParam.getSkuPrice().split("_");
            if (searchParam.getSkuPrice().startsWith("_")) {
                // 范围查询
                Query byRangeSkuPrice = RangeQuery.of(r -> r
                        .field("skuPrice")
                        .lte(JsonData.of(s[0]))
                )._toQuery();
                boolQuery.filter(byRangeSkuPrice);
            }
            if (searchParam.getSkuPrice().endsWith("_")) {
                // 范围查询
                Query byRangeSkuPrice = RangeQuery.of(r -> r
                        .field("skuPrice")
                        .gte(JsonData.of(s[0]))
                )._toQuery();
                boolQuery.filter(byRangeSkuPrice);
            } else {
                // 范围查询
                Query byRangeSkuPrice = RangeQuery.of(r -> r
                        .field("skuPrice")
                        .gte(JsonData.of(s[0]))
                        .lte(JsonData.of(s[1]))
                )._toQuery();
                boolQuery.filter(byRangeSkuPrice);
            }
        }

        // 整合所有query
        sourceBuilder.query(boolQuery.build()._toQuery());

        // 排序,分页,高亮

        // 排序
        if (StringUtils.isNotBlank(searchParam.getSort())) {
            String sort = searchParam.getSort();
            // sort=hotScore_asc/desc
            String[] str = sort.split("_");
            SortOrder order = str[1].equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc;
            sourceBuilder.sort(s -> s
                    .field(f -> f
                            .field(str[0])
                            .order(order)));
        }
        // 分页
        //pageNum:1 from:0 size: 5
        // pageNum:2 from:5 size: 5
        // from = (pageNum-1)*size
        sourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        // 高亮
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            sourceBuilder.highlight(h -> h
                    // preTags 前置高亮标签  postTags 后置高亮标签
                    .fields("skuTitle", f -> f
                            .preTags("<font color='red'>")
                            .postTags("</font>")));
        }
        // 聚合分析
        // 品牌聚合
        TermsAggregation.Builder brandAgg = AggregationBuilders.terms()
                .name("brand_agg").field("brandId").size(50);

        // 品牌聚合的子聚合
        TermsAggregation.Builder brandNameAgg = AggregationBuilders.terms()
                .name("brand_name_agg")
                .field("brandName")
                .size(1);
        brandAgg.build()._toAggregation()
                .children()._toAggregation()
                .aggregations()
                .put("brand_name_agg", brandNameAgg.build()._toAggregation());

        TermsAggregation.Builder brandImg = AggregationBuilders.terms()
                .name("brand_img_agg")
                .field("brandImg")
                .size(1);
        brandAgg.build()._toAggregation()
                .children()._toAggregation()
                .aggregations()
                .put("brand_img_agg", brandImg.build()._toAggregation());

        sourceBuilder.aggregations("brand_agg", brandAgg.build()._toAggregation());

        // 分类聚合 catalog_agg
        TermsAggregation.Builder catalogAgg = AggregationBuilders.terms()
                .name("catalog_agg").field("catalogId").size(10);


        TermsAggregation.Builder catalogNameAgg = AggregationBuilders.terms()
                .name("catalog_name_agg").field("catalogName").size(10);
        catalogAgg.build()._toAggregation()
                .children()._toAggregation()
                .aggregations()
                .put("catalog_name_agg", catalogNameAgg.build()._toAggregation());

        sourceBuilder.aggregations("catalog_agg", catalogAgg.build()._toAggregation());

        // 属性聚合
        NestedAggregation.Builder attrAgg = AggregationBuilders.nested().name("attr_agg").path("attrs");

        TermsAggregation.Builder attrIdAgg = AggregationBuilders.terms().name("attr_id_agg").field("attrs.attrId").size(10);

        TermsAggregation.Builder attrNameAgg = AggregationBuilders.terms().name("attr_name_agg").field("attrs.attrName").size(10);
        TermsAggregation.Builder attrValueAgg = AggregationBuilders.terms().name("attr_value_agg").field("attrs.attrValue").size(10);
        attrIdAgg.build()._toAggregation()
                .children()._toAggregation()
                .aggregations()
                .put("attr_name_agg", attrNameAgg.build()._toAggregation());
        attrIdAgg.build()._toAggregation()
                .children()._toAggregation()
                .aggregations()
                .put("attr_value_agg", attrValueAgg.build()._toAggregation());

        attrAgg.build()._toAggregation()
                .children()._toAggregation()
                .aggregations().put("attr_id_agg", attrIdAgg.build()._toAggregation());
        sourceBuilder.aggregations("attr_agg", attrAgg.build()._toAggregation());

        return sourceBuilder;
    }

}
