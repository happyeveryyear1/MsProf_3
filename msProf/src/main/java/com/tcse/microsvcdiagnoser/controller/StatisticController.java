package com.tcse.microsvcdiagnoser.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.BiMap;
import com.tcse.microsvcdiagnoser.dependency.ExecedDataDependency;
import com.tcse.microsvcdiagnoser.dependency.HarDependency;
import com.tcse.microsvcdiagnoser.dependency.StatisticDependency;
import com.tcse.microsvcdiagnoser.dto.SpanBo;
import com.tcse.microsvcdiagnoser.dto.SpanChunkBo;
import com.tcse.microsvcdiagnoser.entity.CriticalPathSpan;
import com.tcse.microsvcdiagnoser.entity.InterfaceMap;
import com.tcse.microsvcdiagnoser.entity.ServiceMap;
import com.tcse.microsvcdiagnoser.entity.Span;
import com.tcse.microsvcdiagnoser.response.Rets;
import com.tcse.microsvcdiagnoser.service.*;
import com.tcse.microsvcdiagnoser.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.tcse.microsvcdiagnoser.service.StatisticService.calculateSpanTypeAndDepth;


/*
* 获取统计信息
* */
@Slf4j
@RestController
@RequestMapping(value = "/Statistic", produces = "application/json; charset=utf-8")
public class StatisticController {
    
    @Autowired StatisticService statisticService;
    
    @Deprecated
    // @GetMapping("/getRandomCP")
    public String getRandomCP() {
        log.info("staticsCriticalPath start");
        statisticService.staticsCriticalPath();
        log.info("staticsCriticalPath end");
    
        CriticalPathSpan deepestCPSpan = null;
        int depthTmp = 0;
        for(CriticalPathSpan criticalPathSpan: ExecedDataDependency.getCriticalPathMap().values()){
            if(criticalPathSpan.childNum > depthTmp) {
                depthTmp = criticalPathSpan.childNum;
                deepestCPSpan = criticalPathSpan;
            }
        }
        String json = cpSpan2String(deepestCPSpan).toString();
        return json;
    }
    
    // criticalPath 字符串化
    public JSONObject cpSpan2String(CriticalPathSpan criticalPathSpan){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("name", criticalPathSpan.getType());
        jsonObj.put("value", criticalPathSpan.getElapsed());
        JSONArray childrenArr = new JSONArray();
        for (CriticalPathSpan childSpan : criticalPathSpan.getChildSpan()) {
            JSONObject childJsonObj = cpSpan2String(childSpan);
            childrenArr.add(childJsonObj);
        }
        jsonObj.put("children", childrenArr);
        return jsonObj;
    }
    
    /*
    * 获取所有执行的统计信息
    * 包含：
    *   有哪些服务，服务有哪些接口
    *   每个接口执行的火焰图、请求耗时-时间图、请求耗时-概率图以及关键路径
    * */
    @GetMapping("/getAllExecInfo")
    public Object getAllExecInfo() {
        // 计算关键路径
        statisticService.staticsCriticalPath();
        
        // 计算span的类型和深度(深度需要在关键路径序列化为前端数据时使用)
        calculateSpanTypeAndDepth();
        
        HashMap<String, Object> result = new HashMap<>();
        
        // 获取服务-接口执行信息
        String svcList = this.gerAllInterface();
        result.put("svcList", svcList);
        ObjectMapper objectMapper = new ObjectMapper();
        
        HashMap<String, Object> interfaceListMap = new HashMap<>();
        
        // 获取接口执行信息
        // 遍历SvcMap 遍历服务
        for (Map.Entry<String, ServiceMap> serviceEntry : StatisticDependency.getSrvMap().entrySet()) {
            String serviceName = serviceEntry.getKey();
            ServiceMap serviceMap = serviceEntry.getValue();
            // 遍历interfaceMap 遍历接口
            for (Map.Entry<String, InterfaceMap> interfaceEntry : serviceMap.interfaceMap.entrySet()) {
                String interfaceName = interfaceEntry.getKey();
                // 序列化数据
                String flame_graph_data = StatisticService.getInterfaceCP(serviceName + interfaceName);
                String line_data = StatisticService.getStatisticTime_ElapsedProb(serviceName, interfaceName);
                String API_data = StatisticService.getStatisticTime_TimeElapsed(serviceName, interfaceName);
                String bar_data_src = StatisticService.getTraceData(serviceName, interfaceName).toString();
                HashMap<String, String> interfaceDataMap = new HashMap<>();
                interfaceDataMap.put("flame_graph_data", flame_graph_data);
                interfaceDataMap.put("line_data", line_data);
                interfaceDataMap.put("API_data", API_data);
                interfaceDataMap.put("bar_data_src", bar_data_src);
                interfaceListMap.put(interfaceName, interfaceDataMap);
            }
        }
    
        result.put("interfaceListJson", interfaceListMap);
    
    
        return Rets.success(result);
    }
    
    /*
    * 获取服务-接口执行信息
    * */
    // @GetMapping("/getAllInterface")
    public String gerAllInterface() {
        statisticService.flushData();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    
        // 创建一个存放结果的List
        List<SrvMapEntry> result = new ArrayList<>();
        
        int totalSpanCount = getTotalSpanCount();
    
        // 遍历srvMap对象
        int id = 0;
        for (Map.Entry<String, ServiceMap> serviceEntry : StatisticDependency.getSrvMap().entrySet()) {
            String serviceName = serviceEntry.getKey();
            ServiceMap serviceMap = serviceEntry.getValue();
            int serviceSpanCount = getServiceSpanCount(serviceMap);
            String serviceProportion = serviceSpanCount + " / " + totalSpanCount;   // 计算服务占比
        
            SrvMapEntry serviceEntryObj = new SrvMapEntry(++id, serviceName, "", serviceProportion);
            result.add(serviceEntryObj);
        
            // 遍历interfaceMap对象
            for (Map.Entry<String, InterfaceMap> interfaceEntry : serviceMap.interfaceMap.entrySet()) {
                String interfaceName = interfaceEntry.getKey();
                InterfaceMap interfaceMap = interfaceEntry.getValue();
                int interfaceSpanCount = getInterfaceSpanCount(interfaceMap);
                String interfaceProportion = interfaceSpanCount + " / " + totalSpanCount;   // 计算接口占比
            
                SrvMapEntry interfaceEntryObj = new SrvMapEntry(++id, serviceName, interfaceName, interfaceProportion);
                serviceEntryObj.addChild(interfaceEntryObj);
            }
        }
    
        // 将结果对象序列化为JSON字符串
        String json = null;
        try {
            json = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info("getAllInterface");
        log.info("{}, ", json);
        // return Rets.success(json);
        return json;
    }
    
    
    private static int getServiceSpanCount(ServiceMap serviceMap) {
        int count = 0;
        for (InterfaceMap interfaceMap : serviceMap.interfaceMap.values()) {
            count += getInterfaceSpanCount(interfaceMap);
        }
        return count;
    }
    
    private static int getInterfaceSpanCount(InterfaceMap interfaceMap) {
        int count = 0;
        for (LinkedList<Span> spanList : interfaceMap.spanListMap.values()) {
            count += spanList.size();
        }
        return count;
    }
    
    private static int getTotalSpanCount() {
        int count = 0;
        for (ServiceMap serviceMap : StatisticDependency.getSrvMap().values()) {
            count += getServiceSpanCount(serviceMap);
        }
        return count;
    }
    
}

class SrvMapEntry {
    public int id;
    public String svcName;
    public String interfaceName;
    public String proportion;
    public List<SrvMapEntry> children;
    
    public SrvMapEntry(int id, String svcName, String interfaceName, String proportion) {
        this.id = id;
        this.svcName = svcName;
        this.interfaceName = interfaceName;
        this.proportion = proportion;
        this.children = new ArrayList<>();
    }
    
    public void addChild(SrvMapEntry child) {
        this.children.add(child);
    }
}

