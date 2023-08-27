package com.tcse.microsvcdiagnoser.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.HashBiMap;
import com.tcse.microsvcdiagnoser.dependency.*;
import com.tcse.microsvcdiagnoser.entity.CriticalPathSpan;
import com.tcse.microsvcdiagnoser.entity.InterfaceMap;
import com.tcse.microsvcdiagnoser.entity.ServiceMap;
import com.tcse.microsvcdiagnoser.entity.Span;
import com.tcse.microsvcdiagnoser.response.Rets;
import com.tcse.microsvcdiagnoser.service.CollectService;
import com.tcse.microsvcdiagnoser.service.RootCauseAnalysisService;
import com.tcse.microsvcdiagnoser.service.StatisticService;
import com.tcse.microsvcdiagnoser.service.TestExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;

import static com.tcse.microsvcdiagnoser.service.StatisticService.calculateSpanTypeAndDepth;
import static com.tcse.microsvcdiagnoser.util.CommonArgs.testSetSize;

/*
* 根因定位接口
* */
@Slf4j
@RestController
@RequestMapping(value = "/Rootcause", produces = "application/json; charset=utf-8")
public class RootCauseController {
    
    @Autowired
    RootCauseAnalysisService rootCauseAnalysisService;
    
    /*
    * 获取根因
    * */
    @GetMapping("/getRootCause")
    public Object getRandomCP() {
        // 对变异系数由大到小的前十个接口进行根因定位
        // 注意一个接口下可能有多个调用链结构，可能有多个异常
        
        // 异常接口按照严重程度排序
        Map<String, Double> abnormalTypeMap = new HashMap<>();
        for(Map.Entry<String, LinkedList<Span>> entry: AnomalyDetectionDependency.getAbnormalData().entrySet()){
            String type = entry.getKey();
            abnormalTypeMap.put(type, valueAbnormal(AnomalyDetectionDependency.getAbnormalData().get(type), AnomalyDetectionDependency.getNormalData().get(type)));
        }
        Comparator<String> valueComparator = new Comparator<String>() {
            public int compare(String k1, String k2) {
                return -Double.compare(abnormalTypeMap.get(k1), abnormalTypeMap.get(k2));
            }
        };
        TreeMap<String, Double> sortedAbnormalTypeMap = new TreeMap<>(valueComparator);
        sortedAbnormalTypeMap.putAll(abnormalTypeMap);
        
        // 取前10个异常
        int abnormalNum = Math.min(10, sortedAbnormalTypeMap.size());
        int tmp = 0;
        List<List<HashMap<String, String>>> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedAbnormalTypeMap.entrySet()) {
            result.add(rootCauseAnalysisService.rootCauseAnalysis(entry.getKey()));
            tmp += 1;
            if (tmp >= abnormalNum){
                break;
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        String resultStr = null;
        try {
            resultStr = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        // 根因定位表示流程结束，清空数据
        clearDependency();
        
        return Rets.success(resultStr);
    }
   
    /*
    * 异常评估
    * 使用异常span的平均耗时和正常span的平均耗时之差作为评价标准，差越大，越异常
    * 注意，此处使用trace的顶部span，因为顶部span包含整个调用时间
    * */
    public double valueAbnormal(LinkedList<Span> abnormalSpans, LinkedList<Span> normalSpans){
        double abnormalAvgElapsed = 0;
        for(Span span: abnormalSpans)
            abnormalAvgElapsed += span.getElapsed();
        abnormalAvgElapsed = abnormalAvgElapsed/abnormalSpans.size();
        double normalAvgElapsed = 0;
        for(Span span: abnormalSpans)
            normalAvgElapsed += span.getElapsed();
        normalAvgElapsed = normalAvgElapsed/normalSpans.size();
        return abnormalAvgElapsed-normalAvgElapsed;
    }
    
    /*
    * 依赖数据清理
    * */
    public void clearDependency() {
        AnomalyDetectionDependency.getNormalData().clear();
        AnomalyDetectionDependency.setNormalData(new LinkedHashMap<>());
        AnomalyDetectionDependency.getAbnormalDataSmall().clear();
        AnomalyDetectionDependency.setAbnormalDataSmall(new LinkedHashMap<>());
        AnomalyDetectionDependency.getAbnormalDataLarge().clear();
        AnomalyDetectionDependency.setAbnormalDataLarge(new LinkedHashMap<>());
        AnomalyDetectionDependency.getAbnormalData().clear();
        AnomalyDetectionDependency.setAbnormalData(new LinkedHashMap<>());
        
        ExecedDataDependency.getCriticalPathMap().clear();
        ExecedDataDependency.setCriticalPathMap(new LinkedHashMap<>());
        ExecedDataDependency.getExecedData().clear();
        ExecedDataDependency.setExecedData(new LinkedHashMap<>());
        ExecedDataDependency.setExecState(-1);
        
        HarDependency.setHarJSON(null);
        HarDependency.getTokenSet().clear();
        HarDependency.setTokenSet(new CopyOnWriteArraySet<>());
        HarDependency.getHeaderAuthKeyType().clear();
        HarDependency.setHeaderAuthKeyType(new HashMap<>());
        
        RequestDependency.getRequestMap().clear();
        RequestDependency.setRequestMap(new HashMap<>());
        RequestDependency.setBaseURL(null);
        
        StatisticDependency.getSrvMap().clear();
        StatisticDependency.setSrvMap(new HashMap<>());
        
        SwaggerDependency.getSwaggerJSON();
        SwaggerDependency.setSwaggerJSON(null);
        SwaggerDependency.getDefinitionsJSON();
        SwaggerDependency.setDefinitionsJSON(null);
        SwaggerDependency.getResourceSet().clear();
        SwaggerDependency.setResourceSet(new HashSet<>());
        SwaggerDependency.getResourceOPMap().clear();
        SwaggerDependency.setResourceOPMap(new HashMap<>());
        SwaggerDependency.getResourceMap().clear();
        SwaggerDependency.setResourceMap(new HashMap<>());
        
        TestSetDependency.getInitialTestList().clear();
        TestSetDependency.setInitialTestList(new LinkedList<>());
        TestSetDependency.getExecedTestSets().clear();
        TestSetDependency.setExecedTestSets(new LinkedList<>());
        
        CollectService.getTraceTypeMap().clear();
        CollectService.setTraceTypeMap(new ConcurrentHashMap<>());
        CollectService.getTraceSet().clear();
        CollectService.setTraceSet(new HashSet<>());
        CollectService.getTraceBoSet().clear();
        CollectService.setTraceBoSet(new HashSet<>());
        CollectService.getSpanBoMap().clear();
        CollectService.setSpanBoMap(new ConcurrentHashMap<>());
        CollectService.getSpanMap().clear();
        CollectService.setSpanMap(new ConcurrentHashMap<>());
        CollectService.getNoParentSpanSet().clear();
        CollectService.setNoParentSpanSet(new CopyOnWriteArrayList<>());
        CollectService.getShortTypeMap().clear();
        CollectService.setShortTypeMap(HashBiMap.create());
        
        TestExecutorService.getExecPool().shutdown();
        TestExecutorService.setExecPool(Executors.newFixedThreadPool(testSetSize));
    }
    
    
    
}

