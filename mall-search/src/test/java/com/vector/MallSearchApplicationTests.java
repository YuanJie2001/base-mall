package com.vector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


/**
 * @ClassName MallSearchApplication
 * 
 * @Author YuanJie
 * @Date 2022/8/3 12:11
 */
@SpringBootTest
@Slf4j
public class MallSearchApplicationTests {
    //同步客户端
    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Data
    @ToString
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    /**
     * 保存信息
     */
    @Test
    void indexData() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", "渊洁");
        map.put("age", "18");
        map.put("gender", "男");
        // 索引
        IndexResponse response = elasticsearchClient.index(i -> i
                .index("users") // 索引名
                .id("1") // 序号
                .document(map) // 请求体,传入的为document(Object).支持对象,集合,json字符串等 详见javadoc

        );
        log.info("Indexed with version " + response.version());
    }

    /**
     * 搜索文档
     */
    @Test
    public void searchData() throws IOException {
        Query query = MatchQuery.of(q -> q
                .field("address")
                .query("mill")
        )._toQuery();
        // 1.创建检索请求
        SearchResponse<Account> response = elasticsearchClient.search(s -> s
                        .index("bank")
                        .query(query)
                        // 按照年龄值分布聚合
                        .aggregations("ageAgg", a -> a
                                .terms(h -> h
                                        .field("age")
                                        .size(10)
                                ))
                        // 计算平均薪资
                        .aggregations("balanceAvg", b -> b
                                .avg(h -> h
                                        .field("balance"))),
                Account.class
        );
        log.info("max score " + response.hits().maxScore());
        log.info("response.aggregations" + response.aggregations());


        List<Hit<Account>> hits = response.hits().hits();
        for (Hit<Account> hit : hits) {
            log.info("Found source " + hit.source() + ", score "
                    + hit.score()
                    + ", index " + hit.index()
                    + ", id " + hit.id());
        }

        List<LongTermsBucket> ageAgg = response.aggregations().get("ageAgg").lterms().buckets().array();
        for (LongTermsBucket longTermsBucket : ageAgg) {
            log.info(" ageAgg " + longTermsBucket.docCount() +
                    " bikes under " + longTermsBucket.key());
        }

        AvgAggregate balanceAvg = response.aggregations().get("balanceAvg").avg();
        log.info(" name " + balanceAvg._aggregateKind() +
                "balanceAvg " + balanceAvg.value());

    }

}
