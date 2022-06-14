package com.bupt.recruitmentsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bupt.recruitmentsearch.response.ResponseResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ServiceApi {


    @Autowired
    RestHighLevelClient restHighLevelClient;

    @GetMapping("search")
    public ResponseResult search(@RequestParam("keyword") String keyword,
                                 @RequestParam("page") int page,
                                 @RequestParam("size") int size
    ) throws IOException {
        String searchIndex = "lagou";
        //构建查询源构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "pos_name", "enterprise", "pos_keyword", "main_tech"));
        //分页
        if (size <= 0) {
            size = 20;
        }
        if (page<=0){
            page=1;
        }

        int index = (page - 1) * size;

        searchSourceBuilder.from(index); //设置查询起始位置
        searchSourceBuilder.size(size); //结果集返回的数据条数

        //字段筛选
        String[] returnContext = {"pos_name",
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
        searchSourceBuilder.fetchSource(returnContext, new String[]{});


        //构建查询请求对象，入参为索引
        SearchRequest searchRequest = new SearchRequest(searchIndex);
        //向搜索请求对象中配置搜索源
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<JSONObject> results = new ArrayList<>();
        if (RestStatus.OK.equals(response.status())) {
            long total = response.getHits().getTotalHits().value; //检索到符合条件的总数

            SearchHit[] hits = response.getHits().getHits();
            //未指定size，默认查询的是10条
            for (SearchHit hit : hits) {
                String id = hit.getId(); //文档id
                JSONObject jsonObject = JSON.parseObject(hit.getSourceAsString(), JSONObject.class); //文档内容
                jsonObject.put("id", id);
                results.add(jsonObject);
            }
            if (results.size()==0){
                return ResponseResult.FAILED("results.size()==0,搜索无结果");

            }
            System.out.println("results num "+results.size());

            return ResponseResult.SUCCESS("搜索结果").setData(results);

        } else
            return ResponseResult.FAILED("搜索无结果");


    }

}
