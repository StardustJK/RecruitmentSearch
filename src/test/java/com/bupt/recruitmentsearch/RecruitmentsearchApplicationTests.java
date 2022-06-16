package com.bupt.recruitmentsearch;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class RecruitmentsearchApplicationTests {

    /**
    @Test
    void contextLoads() {
    }
    @Autowired
    RestHighLevelClient restHighLevelClient;

    //连接测试
    @Test
    public void test() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest("58*");
        // 获取es前缀过滤下所有索引

        GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
        // 将es查出的索引转换为list
        List<String> elasticsearchList = new ArrayList<>(getIndexResponse.getMappings().keySet());
        elasticsearchList.forEach(System.out::println);
    }

    //搜索接口测试
    @Test
    public void searchTest() throws IOException{
        String searchIndex="lagou";
        String keyword="软件开发";
        //构建查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "pos_name", "enterprise", "pos_keyword","main_tech"));
        //分页
        int page = 1; // 页码
        int size = 5; // 每页显示的条数
        int index = (page - 1) * size;

        searchSourceBuilder.from(index); //设置查询起始位置
        searchSourceBuilder.size(size); //结果集返回的数据条数

        //字段筛选
        String [] returnContext={"pos_name",
                "salary_low_bound",
                "salary_high_bound",
                "salary_fee_months",
                "city",
                "degree",
                "exp",
                "person_in_charge",
                "charge_pos",
                "enterprise",
                "enterprise_scale",
                "url"

        };
        searchSourceBuilder.fetchSource(returnContext, new String[] {});


        //构建查询请求对象，入参为索引
        SearchRequest searchRequest = new SearchRequest(searchIndex);
        //向搜索请求对象中配置搜索源
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        if (RestStatus.OK.equals(response.status())) {
            long total = response.getHits().getTotalHits().value; //检索到符合条件的总数
            SearchHit[] hits = response.getHits().getHits();
            //未指定size，默认查询的是10条
            for (SearchHit hit : hits) {
                String id = hit.getId(); //文档id
                JSONObject jsonObject = JSON.parseObject(hit.getSourceAsString(), JSONObject.class); //文档内容
                System.out.println(id+" :");
                System.out.println(jsonObject);
            }
        }

    }
    **/


}
