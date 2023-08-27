package com.tcse.microsvcdiagnoser.entity;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/*
* 接口和接口下请求的关系
* 用来获取执行数据的统计
* */
@Data
public class InterfaceMap {
    public String name;
    // 一个接口下有多类请求 key: "ts-travel2-1.0: /api/v1/travel2service/trips/left"(可能因为参数不同而导致不同的请求地址)
    public LinkedHashMap<String, LinkedList<Span>> spanListMap = new LinkedHashMap<>();
    
    public InterfaceMap(String name){
        this.name = name;
    }
}