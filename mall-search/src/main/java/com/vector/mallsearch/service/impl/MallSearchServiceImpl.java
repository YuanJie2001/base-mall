package com.vector.mallsearch.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.vector.common.to.es.SkuEsModel;
import com.vector.common.utils.R;
import com.vector.mallsearch.constant.EsConstant;
import com.vector.mallsearch.openfeign.ProductOpenFeignService;
import com.vector.mallsearch.service.MallSearchService;
import com.vector.mallsearch.vo.AttrRespVo;
import com.vector.mallsearch.vo.SearchParam;
import com.vector.mallsearch.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @ClassName MallSearchService
 *  聚合功能有问题
 * @Author YuanJie
 * @Date 2022/8/12 1:05
 */
@Service
@Slf4j
public class MallSearchServiceImpl implements MallSearchService {
    @Resource
    private ElasticsearchClient elasticsearchClient;
    @Resource
    private ProductOpenFeignService productOpenFeignService;

    /**
     * @param searchParam 检索的所有参数
     * @return 返回检索的结果
     */
    public SearchResult search(SearchParam searchParam) {
        // 1.动态构建dsl语句
        SearchResult searchResult = null;

        // 2.准备检索请求
        SearchRequest sourceBuilder = buildSearchRequest(searchParam).build();

        // 3.执行检索请求
        SearchResponse<SkuEsModel> response = null;
        try {
            response = elasticsearchClient.search(s -> s
                            .index(EsConstant.PRODUCT_INDEX)
                            .query(sourceBuilder.query())
                    , SkuEsModel.class);
            // 4.分析数据格式
            searchResult = buildSearchResult(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
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
        if (Optional.ofNullable(searchParam.getHasStock()).isPresent()) {
            Query byHasStock = TermQuery.of(r -> r
                    .field("hasStock")
                    .value(searchParam.getHasStock() == 1))._toQuery();
            boolQuery.filter(byHasStock);
        }
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
        // 品牌聚合的子聚合
        TermsAggregation brandNameAgg = AggregationBuilders.terms().field("brandName").size(1).build();
        TermsAggregation brandImg = AggregationBuilders.terms().field("brandImg").size(1).build();

        // 品牌聚合
        Aggregation brandAgg = new Aggregation.Builder()
                .terms(new TermsAggregation.Builder().field("brandId").size(50).build())
                .aggregations("brandName", brandNameAgg._toAggregation())
                .aggregations("brandImg", brandImg._toAggregation())
                .build();

        sourceBuilder.aggregations("brandAgg", brandAgg);


//  分类子聚合 catalog_agg
        TermsAggregation catalogNameAgg = AggregationBuilders.terms().field("catalogName").size(10).build();
//         分类聚合 catalog_agg
        Aggregation catalogAgg = new Aggregation.Builder()
                .terms(new TermsAggregation.Builder().field("catalogId").size(10).build())
                .aggregations("catalogName", catalogNameAgg._toAggregation()).build();

        sourceBuilder.aggregations("catalogAgg", catalogAgg);

        // 属性聚合
        // 语句未完成不要build
        TermsAggregation attrNameAgg = AggregationBuilders.terms().field("attrs.attrName").size(10).build();
        TermsAggregation attrValueAgg = AggregationBuilders.terms().field("attrs.attrValue").size(10).build();

        Aggregation attrIdAgg = new Aggregation.Builder()
                .terms(new TermsAggregation.Builder().field("attrs.attrId").size(10).build())
                .aggregations("attrs.attrName", attrNameAgg._toAggregation())
                .aggregations("attrs.attrValue", attrValueAgg._toAggregation())
                .build();

        Aggregation attrAgg = new Aggregation.Builder()
                .nested(new NestedAggregation.Builder().path("attrs").build())
                .aggregations("attrIdAgg", attrIdAgg)
                .build();

        sourceBuilder.aggregations("attrAgg", attrAgg);

        return sourceBuilder;
    }

    private SearchResult buildSearchResult(SearchResponse<SkuEsModel> response, SearchParam param) {
        // 4. 构建结果数据

        HitsMetadata<SkuEsModel> hits = response.hits();
        List<Hit<SkuEsModel>> list = hits.hits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (Optional.ofNullable(list).isPresent() && list.size() > 0) {
            for (Hit<SkuEsModel> searchResultHit : list) {
                SkuEsModel source = searchResultHit.source();
                SkuEsModel skuEsModel = new SkuEsModel();
                BeanUtils.copyProperties(source, skuEsModel);
                if (StringUtils.isNotBlank(param.getKeyword())) {
                    List<String> skuTitle = searchResultHit.highlight().get("skuTitle");
                    String s = skuTitle.get(0).toString();
                    skuEsModel.setSkuTitle(s);
                }
                esModels.add(skuEsModel);
            }
        }


        SearchResult searchResult = new SearchResult();
        // 1.封住所有返回查询到的商品
        searchResult.setProducts(esModels);
        // 2.当前所有商品涉及到的所有属性信息
        NestedAggregate attrAgg = response.aggregations().get("attrAgg").nested();
        List<LongTermsBucket> attrIdAgg = attrAgg.aggregations().get("attrIdAgg").lterms().buckets().array();
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        for (LongTermsBucket longTermsBucket : attrIdAgg) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 1.得到属性的id
            long attrId = Long.parseLong(longTermsBucket.key());
            // 2.得到属性的名字
            String attrNameAgg = longTermsBucket.aggregations().get("attrNameAgg").sterms().buckets().array().get(0).key();
            // 3.得到属性的所有值
            List<String> attrValueAgg = longTermsBucket.aggregations().get("attrValueAgg").sterms().buckets().array().stream().map(item -> {
                String key = item.key();
                return key;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrNameAgg);
            attrVo.setAttrValue(attrValueAgg);
            attrVos.add(attrVo);
        }
        searchResult.setAttrs(attrVos);
        // 3.当前所有商品涉及到的所有分类信息
        List<LongTermsBucket> catalog_agg = response.aggregations().get("catalogAgg").lterms().buckets().array();
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (LongTermsBucket longTermsBucket : catalog_agg) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 得到分类id
            String keyAsString = longTermsBucket.keyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            // 得到分类名
            StringTermsAggregate catalog_name_agg = longTermsBucket.aggregations().get("catalogNameAgg").sterms();
            String catalog_name = catalog_name_agg.buckets().array().get(0).toString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVos);
        // 4.当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        List<LongTermsBucket> brand_agg = response.aggregations().get("brandAgg").lterms().buckets().array();
        for (LongTermsBucket longTermsBucket : brand_agg) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 1. 得到品牌的id
            Long i = Long.parseLong(longTermsBucket.key());
            // 2. 得到品牌的名
            String brandNameAgg = longTermsBucket.aggregations().get("brandNameAgg").sterms().buckets().array().get(0).key();
            // 3. 得到品牌的图片
            String brandImgAgg = longTermsBucket.aggregations().get("brandImgAgg").sterms().buckets().array().get(0).key();

            brandVo.setBrandId(i);
            brandVo.setBrandName(brandNameAgg);
            brandVo.setBrandImg(brandImgAgg);
            brandVos.add(brandVo);
        }
        searchResult.setBrands(brandVos);
        // 5. 页码
        searchResult.setPageNum(param.getPageNum());
        // 6. 总记录数
        long total = hits.total().value();
        searchResult.setTotal(total);
        // 7. 总页码
        int totalPages = (int) Math.ceil(total % (double) EsConstant.PRODUCT_PAGESIZE);
        searchResult.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        //6. 构建面包屑导航功能
        if (Optional.ofNullable(param.getAttrs()).isPresent()
                && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // 分析每一个attrs传过来的参数值
                //attrs=2_5存:6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productOpenFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrRespVo data = null;
                    try {
                        data = r.getData("attr", new TypeReference<AttrRespVo>() {
                        });
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                // 2.取消面包屑以后要跳转的地方
                String encode = null;
                try {
                    encode = URLEncoder.encode(attr, "UTF-8");
                    encode = encode.replace("+", "%20");// 浏览器对空格编码和java不一样
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = param.get_queryString().replace("&attrs=" + encode, "");
                navVo.setLink("http://search.mall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());

            searchResult.setNavs(collect);
        }
        return searchResult;
    }
}
