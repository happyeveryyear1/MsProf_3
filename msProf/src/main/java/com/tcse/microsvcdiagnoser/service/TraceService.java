package com.tcse.microsvcdiagnoser.service;



import com.tcse.microsvcdiagnoser.context.Header;
import com.tcse.microsvcdiagnoser.dto.AnnotationBo;
import com.tcse.microsvcdiagnoser.dto.SpanBo;
import com.tcse.microsvcdiagnoser.entity.Span;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/*
* Trace相关的计算
* */
@Slf4j
@Service
public class TraceService {
    /*
    * 时间格式转换
    * */
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS" );
    
    public void identifyTraceType(){
        Set<Span> traceSet = CollectService.getTraceSet();
        for(Span traceHeaderSpan: traceSet){
            if(traceHeaderSpan.getType() != ""){
                continue;
            }
            identifySpanType(traceHeaderSpan);
        }
        for(Span traceHeaderSpan: traceSet){
            if(traceHeaderSpan.getTraceType() != ""){
                continue;
            }
            LinkedList<Span> queue = new LinkedList<>();
            queue.add(traceHeaderSpan);
            Span delimiter = new Span();
            delimiter.setEndPoint("|");
            Span sameLevelDelimiter = new Span();
            sameLevelDelimiter.setEndPoint(" ");
            if(traceHeaderSpan.getChildSpan().size() != 0) {
                queue.add(delimiter);
            }
            while(queue.size()!=0){
                Span tmpSpan = queue.pop();
                traceHeaderSpan.setTraceType(traceHeaderSpan.getTraceType() + tmpSpan.getEndPoint() + " ");
                for (Span childSpan: tmpSpan.getChildSpan())
                    queue.add(childSpan);
                if (tmpSpan.getType() != "|" && tmpSpan.getChildSpan().size()!=0){
                    queue.add(delimiter);
                }
                
            }
        }
    }
    
    /*
    * 设置span的type（深度优先遍历，span的endpoint拼接）
    * */
    public void identifySpanType(Span span){
        String type = "<" + span.getEndPoint()+":";
        for (Span childSpan: span.getChildSpan()){
            identifySpanType(childSpan);
            type += childSpan.getEndPoint();
            type += "-";
        }
        type += ">";
        span.setType(type);
    }
    
    public void arrangeTrace(){
        Map<String, HashSet<Span>> traceTypeMap = CollectService.getTraceTypeMap();
        Set<Span> traceSet = CollectService.getTraceSet();
        for (Span traceHeaderSpan: traceSet){
            if(traceTypeMap.containsKey(traceHeaderSpan.getTraceType()))
                traceTypeMap.get(traceHeaderSpan.getTraceType()).add(traceHeaderSpan);
            else{
                // new Type, register
                CollectService.getShortTypeMap().put(CollectService.getShortTypeMap().size(), traceHeaderSpan.getTraceType());
                traceTypeMap.put(traceHeaderSpan.getTraceType(), new HashSet<Span>(){{
                    add(traceHeaderSpan);
                }});
            }
        }
    }
    
    @Deprecated
    public String getTraceString(String my_token){
        Map<Long, Span> spanMap = CollectService.getSpanMap();
        Set<SpanBo> traceBoSet = CollectService.getTraceBoSet();
        SpanBo headerBo = new SpanBo();
        boolean flag = false;
        
        for (SpanBo headerSpanBo: traceBoSet){
            List<AnnotationBo> annotationBoList = headerSpanBo.getAnnotationBoList();
            for (AnnotationBo annotationBo: annotationBoList){
                if (annotationBo.getKey() == Header.HTTP_MY_TOKEN && annotationBo.getValue() != null && annotationBo.getValue().toString().equals(my_token)){
                    headerBo = headerSpanBo;
                    flag = true;
                    break;
                }
            }
            if (flag)
                break;
        }
        
        String emptyData = "{" +
                "\"data\":[" +
                "{" +
                "\n" +
                "\"Point\": \" \",\n" +
                "\"RPC\": \" \",\n" +
                "\"StartTime\": \" \",\n" +
                "\"SelfTime\": \" \",\n" +
                "\"Elapsed\": \" \",\n" +
                "}" +
                "]," +
                "\"total\": 1" +
                "}";
        
        if (headerBo.getSpanId() == 0) {
            return emptyData;
        }
        
        Span headerSpan = spanMap.get(headerBo.getSpanId());
        int num = 0;
        // HashMap<String, String> dataMap = new HashMap<>();
        JSONObject dataJson = new JSONObject();
        createSubData(headerSpan, dataJson, num);

        String JsonDataMap = dataJson.toString();
        String data = "{" +
                "\"data\":[" +
                JsonDataMap +
                "], " +
                "\"total\": " + num +
                "}";
        return data;
    
    
    }
    
    /*
    * Trace数据整理为jsonObject
    * */
    public void createSubData(Span span, JSONObject dataJson, int num) {
        dataJson.put("Application", CollectService.getSpanBoMap().get(span.getSpanId()).getApplicationId());
        dataJson.put("Point", span.getEndPoint());
        dataJson.put("RPC", span.getRpc());
        dataJson.put("StartTime", sdf.format(new Date(span.getStartTime())));
        dataJson.put("SelfTime", Long.toString(span.getSelfTime()));
        dataJson.put("Elapsed", Long.toString(span.getElapsed()));
        List<JSONObject> childrenDataMap = new LinkedList<>();
        for (Span childSpan: span.getChildSpan()){
            JSONObject tmpDataJson = new JSONObject();
            createSubData(childSpan, tmpDataJson, num++);
            childrenDataMap.add(tmpDataJson);
        }
        // String childrenDataMapStr = "[]";
        // try {
        //     childrenDataMapStr = new ObjectMapper().writeValueAsString(childrenDataMap);
        // } catch (JsonProcessingException e) {
        //     e.printStackTrace();
        // }
        dataJson.put("children", childrenDataMap);
    }
}


// data 样例
/*
{
        "data":[
        {
        "Method": 111,
        "Argument": 111,
        "Start Time": 111,
        "gap(ms)": 111,
        "exec(ms)": 111,
        "self(ms)": 111,
        "Class": 111,
        "API": 111,
        "Agent": 111,
        "children":[{
        "Method": 222,
        "Argument": 111,
        "Start Time": 111,
        "gap(ms)": 111,
        "exec(ms)": 111,
        "self(ms)": 111,
        "Class": 111,
        "API": 111,
        "Agent": 111,
        },
        {
        "Method": 333,
        "Argument": 111,
        "Start Time": 111,
        "gap(ms)": 111,
        "exec(ms)": 111,
        "self(ms)": 111,
        "Class": 111,
        "API": 111,
        "Agent": 111
        }]
        },
        {
        "Method": 444,
        "Argument": 111,
        "Start Time": 111,
        "gap(ms)": 111,
        "exec(ms)": 111,
        "self(ms)": 111,
        "Class": 111,
        "API": 111,
        "Agent": 111
        }
        ],
        "total": 1
 }
 */
