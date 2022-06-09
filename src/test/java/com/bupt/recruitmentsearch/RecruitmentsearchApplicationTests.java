package com.bupt.recruitmentsearch;


import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class RecruitmentsearchApplicationTests {

    @Test
    void contextLoads() {
    }
    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Test
    public void test() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest("58*");
        // 获取es前缀过滤下所有索引

        GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
        // 将es查出的索引转换为list
        List<String> elasticsearchList = new ArrayList<>(getIndexResponse.getMappings().keySet());
        elasticsearchList.forEach(System.out::println);
    }


}
