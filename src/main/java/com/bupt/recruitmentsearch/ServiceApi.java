package com.bupt.recruitmentsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bupt.recruitmentsearch.response.ResponseResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@CrossOrigin
public class ServiceApi {

    private static final String pageNo = "1";
    private static final String pageSize = "20";

    // 将前端参数转换为es中的参数
    private static final Map<String, String> keymapping = Map.of("posDomain", "pos_domain",
            "enterScale", "enterprise_scale", "posSource", "pos_source");

    // 如果是3k以下就填下限
//    private static final String[] salaryVal = {"3k-5k", "5k-10k", "10k-20k", "20k-30k"};

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @GetMapping("search")
    public ResponseResult search(
            @RequestParam Map<String, Object> paramMap
    ) throws IOException {
        // 0. 格式化查询参数
        Map<String, String> formattedMap = new HashMap<>();
        verifyParams(paramMap, formattedMap);
        int pageNo = Integer.parseInt(formattedMap.get("pageNo"));
        int pageSize = Integer.parseInt(formattedMap.get("pageSize"));
        // 分页查询起始位置
        int index = (pageNo - 1) * pageSize;
        // 取值后删除
        formattedMap.remove("pageNo");
        formattedMap.remove("pageSize");


        String posKeyword = formattedMap.get("posKeyword");

        //  正则取薪资上下限
        String salaryStr = formattedMap.get("salaryStr");
        float salaryLow = 0, salaryHigh = 0;
        String pattern = "\\d+";     // 30k-50k k-80k  ""等
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(salaryStr);

        List<String> salaryRegList = new ArrayList<>();
        while (m.find()) {
            salaryRegList.add(m.group());
        }
        if (salaryRegList.size() == 2) {
            salaryLow = Float.parseFloat(salaryRegList.get(0));
            salaryHigh = Float.parseFloat(salaryRegList.get(1));
        } else if (salaryRegList.size() == 1 && salaryStr.contains("以上")) {
            // 30k以上
            salaryLow = Float.parseFloat(salaryRegList.get(0));
        } else if (salaryRegList.size() == 1 && salaryStr.contains("以下")) {
            // 3k以下
            salaryHigh = Float.parseFloat(salaryRegList.get(0));
        } else {
            System.out.println("前端传入的预期薪资有误"); // 但是还需要进行其他的
        }


        String salarySort = formattedMap.get("salarySort");
        String scaleSort = formattedMap.get("scaleSort");


        // 1. 构建查询源构建器
        String searchIndex = "job_info_full";
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建查询语句
        BoolQueryBuilder multiBoolBuilder = QueryBuilders.boolQuery();
        // 去重查询
//        CollapseBuilder collapseBuilder = new CollapseBuilder("pos_name.keyword");


        // 筛选条件的数量
        int filterCount = 0, sortCount = 0;
        for (String s : formattedMap.keySet()) {
            switch (s) {
                case "posKeyword":
                    // 1.1 搜索框中有输入
                    if (!posKeyword.equals("")) {
                        filterCount++;
                        // 相当于 should:"multi_match": "match":pos_name:posKeyword ; "match":pos_keyword:posKeyword
                        multiBoolBuilder.should(QueryBuilders.multiMatchQuery(posKeyword, "pos_name", "pos_keyword", "pos_domain", "city.keyword", "location", "enterprise"))
                                .minimumShouldMatch(1);
                    }
                    break;
                case "city":
                case "exp":
                case "degree":
                    // 城市、经验、学历要求、岗位领域、企业规模、数据来源按照keyword查询
                    if (!formattedMap.get(s).equals("") && !formattedMap.get(s).equals("不限")) {
                        filterCount++;
                        System.out.println("筛选条件：" + s + "值为：" + formattedMap.get(s));
                        // Todo:这里不知道为啥创建以后都是text类型，只有keyword属性才是keyword
                        // 如果库是job_info_full，就是说从控制台创建的库，那么不需要加keyword
                        multiBoolBuilder.must(QueryBuilders.termQuery(s + ".keyword", formattedMap.get(s)));
                    }
                    // 否则不做任何更改
                    break;

                // 用于筛选的时候都是keyword
                case "posDomain":
                case "enterScale":
                case "posSource":
                    // 城市、经验、学历要求、岗位领域、企业规模、数据来源按照keyword查询
                    if (!formattedMap.get(s).equals("") && !formattedMap.get(s).equals("不限")) {
                        filterCount++;
                        String tmpKey = ServiceApi.keymapping.get(s);
                        System.out.println("筛选条件：" + s + "值为：" + formattedMap.get(s));
//                        // Todo:这里不知道为啥创建以后都是text类型，只有keyword属性才是keyword
                        multiBoolBuilder.must(QueryBuilders.termQuery(tmpKey + ".keyword", formattedMap.get(s)));
//                        if (tmpKey.equals("pos_domain")) {
//                            multiBoolBuilder.must(QueryBuilders.termQuery(tmpKey+".keyword", formattedMap.get(s)));
//                        } else {
//                            multiBoolBuilder.must(QueryBuilders.termQuery(tmpKey, formattedMap.get(s)));
//                        }
                    }
                    break;
                case "salaryStr":
                    if (salaryLow != 0 || salaryHigh != 0) filterCount++;
                    // 需要查询一定的范围，默认为0，所以只要参数里有这个字段，就肯定会进行一次查询
                    // 要么是按照薪资范围查询，要么是查询到所有的数据
                    System.out.println("筛选条件：" + s + "值为：" + formattedMap.get(s) + "薪资上限：" + salaryHigh + "薪资下限：" + salaryLow);
                    if ((salaryLow == 0) && (salaryHigh == 0)) {
                        // Todo:这个写法不professional吧
                        multiBoolBuilder.must(QueryBuilders.matchAllQuery());
                    } else if (salaryHigh == 0) {
                        // 20k以上
                        multiBoolBuilder.must(QueryBuilders.rangeQuery("salary_low_bound").gte(salaryLow));
                    } else if (salaryLow == 0) {
                        // 3k以下
                        multiBoolBuilder.must(QueryBuilders.rangeQuery("salary_low_bound").lte(salaryHigh));
                    } else {
                        multiBoolBuilder.filter(QueryBuilders.rangeQuery("salary_low_bound").gte(salaryLow));
                        multiBoolBuilder.filter(QueryBuilders.rangeQuery("salary_high_bound").lte(salaryHigh));
                    }
                    break;
                case "salarySort":
                    break;
                case "scaleSort":
                    if (scaleSort.equals("asc")) {
                        // 升序中需要先删除带有"其他"类型的企业
                        multiBoolBuilder.must(QueryBuilders.rangeQuery("scale_mapping").gt(0));
                    }
                    break;
            }
        }


        // Todo:为builder执行查询是什么意思，同时需要（在薪资判断时）保证结果里有值
        searchSourceBuilder.query(multiBoolBuilder);
        if (!salarySort.equals("no")) {
            System.out.println("需要按照薪资排序：" + salarySort);
            sortCount++;
            // 查询完后再排序
            searchSourceBuilder.sort("salary_low_bound", salarySort.equals("asc") ?
                    SortOrder.ASC : SortOrder.DESC);
        } else {
            System.out.println("不需要按照薪资排序");
        }

        if (!scaleSort.equals("no")) {
            System.out.println("需要按照企业规模排序：" + scaleSort);
            sortCount++;
            // 查询完后再排序
            searchSourceBuilder.sort("scale_mapping", scaleSort.equals("asc") ?
                    SortOrder.ASC : SortOrder.DESC);
        } else {
            System.out.println("不需要按企业规模排序");
        }


        System.out.println("总共收到筛选参数：filters:" + filterCount + ",排序参数：sorts:" + sortCount);


        searchSourceBuilder.from(index); // 设置查询起始位置
        searchSourceBuilder.size(pageSize); // 结果集返回的数据条数


        // 2. 构建查询请求对象，入参为索引
        SearchRequest searchRequest = new SearchRequest(searchIndex);
        // 设置获取真实的分页数量：
//        searchSourceBuilder.trackTotalHits(false);
        // 3. 向搜索请求对象中配置搜索源
        searchRequest.source(searchSourceBuilder);
        // 4. 执行搜索,向ES发起http请求
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<JSONObject> results = new ArrayList<>();
        if (RestStatus.OK.equals(response.status())) {
            long total = response.getHits().getTotalHits().value; //检索到符合条件的总数
            System.out.println("totalHits:" + total);
            // 是不是可以在分类之前就获得这个size?
            JSONObject tmpTotal = new JSONObject();
            tmpTotal.put("total", total);
            results.add(tmpTotal);

            SearchHit[] hits = response.getHits().getHits();
            // 未指定size，默认查询的是10条
            for (SearchHit hit : hits) {
                String id = hit.getId(); //文档id
                JSONObject jsonObject = JSON.parseObject(hit.getSourceAsString(), JSONObject.class); // 文档内容
                jsonObject.put("id", id);

                // 处理：将数组转换为字符串返回
                Object targetObj = jsonObject.get("pos_name");
                Object posKeywordObj = jsonObject.get("pos_keyword");
                String pos_name = "", pos_keyword_total = "";
                if (targetObj instanceof JSONArray) {
                    pos_name = (String) ((JSONArray) targetObj).get(0);
                    jsonObject.remove("pos_name");
                    System.out.println("JSONArray 数组, pos_name:" + pos_name);
                    jsonObject.put("pos_name", pos_name);
                } else if (targetObj instanceof JSONObject) {
                    System.out.println("JSONObject 对象, 不需要改变, pos_name:" + pos_name);
                }

                if (posKeywordObj instanceof JSONArray) {
                    // 直接toString不好使，会带有逗号和换行符，欸我去了不就行吗哈哈哈晕
                    pos_keyword_total = posKeywordObj.toString();
                    jsonObject.remove("pos_keyword");
                    pos_keyword_total = pos_keyword_total.replace("\"", "");
                    pos_keyword_total = pos_keyword_total.replace(",", " ");
                    pos_keyword_total = pos_keyword_total.substring(1, pos_keyword_total.length() - 1);
                    System.out.println("JSONArray 数组, pos_keyword_total:" + pos_keyword_total);
                    jsonObject.put("pos_keyword", pos_keyword_total);
                } else if (targetObj instanceof JSONObject) {
                    System.out.println("JSONObject 对象, 不需要改变, pos_keyword:" + pos_keyword_total);
                }

                results.add(jsonObject);
            }
            if (results.size() == 1) {
                return ResponseResult.FAILED("results.size()==0, 搜索无结果：total=0");
            }
            return ResponseResult.SUCCESS("搜索结果").setData(results);
        } else
            return ResponseResult.FAILED("搜索无结果");
    }

    void verifyParams(Map<String, Object> paramMap, Map<String, String> formattedMap) {
        /**
         * posKeyword:      string, text(ik_smart)  （不限）
         * city:            string:北京、上海、广州、深圳、杭州 ,keyword  （不限）
         * salaryStr:       string text(ik_smart)               （不限）
         * salarySort:      string:no, asc, desc
         * exp:             string
         * degree:          string
         * posDomain:       string
         * enterScale:      string
         * scaleSort:       string:no, up, down
         * posSource:       string:猎聘、58同城、智联招聘、    keyword
         * pageNo:          string==>转换为int
         * pageSize:        string==>转换为int
         * **/
        String pageNo, pageSize;
        for (String key : paramMap.keySet()) {
            if (key.equals("pageNo") || key.equals("pageSize") || key.equals("salarySort") || key.equals("scaleSort"))
                continue;
            else {
                // 将所有筛选参数的空值改为空字符串""
                if (paramMap.get(key).equals("不限") || paramMap.get(key).equals("")) {
                    formattedMap.put(key, "");
                } else {
                    formattedMap.put(key, (String) paramMap.get(key));
                }
            }
        }


        // 将页码和页面大小的空值改为1、20的默认值
        // Todo:test一下当返回结果为空的时候，pageSize会不会导致报错
        System.out.println("pageNo in paramap:" + paramMap.get("pageNo"));
        System.out.println("pageSize paramap:" + paramMap.get("pageSize"));
        pageNo = (paramMap.get("pageNo").equals("")) ? ServiceApi.pageNo : (String) paramMap.get("pageNo");
        pageSize = (paramMap.get("pageSize").equals("")) ? ServiceApi.pageSize : (String) paramMap.get("pageSize");
        formattedMap.put("pageNo", pageNo);
        formattedMap.put("pageSize", pageSize);
        System.out.println("pageNo in formatted paramap:" + formattedMap.get("pageNo"));
        System.out.println("pageSize in formatted paramap:" + formattedMap.get("pageSize"));


        // 将排序参数的空值改为no
        formattedMap.put("salarySort", paramMap.get("salarySort").equals("") ? "no" : (String) paramMap.get("salarySort"));
        formattedMap.put("scaleSort", paramMap.get("scaleSort").equals("") ? "no" : (String) paramMap.get("scaleSort"));
    }


    @PostMapping("test")
    public ResponseResult test(@RequestBody JSONObject input) {
        input.put("test", "test");
        return ResponseResult.SUCCESS("发送成功").setData(input);
    }
}
