package com.tcse.microsvcdiagnoser.entity;

import lombok.Data;

import java.util.LinkedHashMap;

/*
* service和接口的对应关系，配合InterfaceMap使用
* 用来获取执行数据的统计
* */
@Data
public class ServiceMap {
    public String name;
    // 一个svc下有多个接口
    public LinkedHashMap<String, InterfaceMap> interfaceMap = new LinkedHashMap<>();
    
    public ServiceMap(String name){
        this.name = name;
    }
}