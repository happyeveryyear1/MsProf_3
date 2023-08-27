package com.tcse.microsvcdiagnoser.dependency;

import com.tcse.microsvcdiagnoser.dto.RequestDto;
import com.tcse.microsvcdiagnoser.dto.SpanBo;
import com.tcse.microsvcdiagnoser.entity.Span;
import com.tcse.microsvcdiagnoser.entity.TestSet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import lombok.Getter;
import lombok.Setter;

/*
* 异常检测后的数据记录
* */
public class AnomalyDetectionDependency {

    
    // 正常数据(String为类型，LinkedList<Span>为该类的trace的头span)
    @Getter @Setter
    private static LinkedHashMap<String, LinkedList<Span>> normalData = new LinkedHashMap<>();
    // 异常检测，按照3sigma分为过短和过长 @Deprecated
    // 异常数据 - 时间过短
    @Getter @Setter
    private static LinkedHashMap<String, LinkedList<Span>> abnormalDataSmall = new LinkedHashMap<>();
    // 异常数据 - 时间过长
    @Getter @Setter
    private static LinkedHashMap<String, LinkedList<Span>> abnormalDataLarge = new LinkedHashMap<>();
    // 异常检测，直接取过长
    // 异常数据 - 时间过长(更大占比)
    @Getter @Setter
    private static LinkedHashMap<String, LinkedList<Span>> abnormalData = new LinkedHashMap<>();
    
    
}
