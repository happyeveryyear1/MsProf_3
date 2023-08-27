package com.tcse.microsvcdiagnoser.dependency;

import com.tcse.microsvcdiagnoser.entity.ServiceMap;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;

/*
* 为方便统计分析后的序列化，记录执行数据
* */
public class StatisticDependency {
    
    
    // 服务 - 多个接口 - 多个请求
    // public static HashMap<String, HashMap<String, LinkedHashMap>> srvSpanMap = new HashMap<>();
    
    // 一个系统包含多个服务，一个服务对应一个serviceMap。一个serviceMap又包含多个请求
    @Getter
    @Setter
    private static HashMap<String, ServiceMap> srvMap = new HashMap<>();
}



