package com.bupt.recruitmentsearch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiTest {

    public static void main(String[] args) {
        String salaryStr = "k-50k";
        String pattern = "\\d+";     // 30k-50k
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(salaryStr);

        List<String> list = new ArrayList<>();
        while (m.find()) {
//            System.out.println(m.groupCount()); 这块是为啥等于0啊
            list.add(m.group());
        }


        System.out.println("list:" + list.size());
//        System.out.println(list.get(0) + " " + list.get(1));

        float salaryLow = 0, salaryHigh = 0;


        if (list.size() == 2) {
            salaryLow = Float.parseFloat(list.get(0));
            salaryHigh = Float.parseFloat(list.get(1));
        }else if(list.size() == 1){
            salaryLow = Float.parseFloat(list.get(0));
        }else {
            System.out.println("前端传入预期薪资范围有误！");
        }
        System.out.println("salaryLow:" + salaryLow + ",salaryHigh:" + salaryHigh);

//        JSONArray jsonArray = new JSONArray()

    }
}
