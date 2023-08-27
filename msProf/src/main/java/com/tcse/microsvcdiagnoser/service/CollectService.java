package com.tcse.microsvcdiagnoser.service;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.tcse.microsvcdiagnoser.context.Header;
import com.tcse.microsvcdiagnoser.dto.AnnotationBo;
import com.tcse.microsvcdiagnoser.dto.SpanBo;
import com.tcse.microsvcdiagnoser.dto.SpanChunkBo;
import com.tcse.microsvcdiagnoser.entity.Span;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.Setter;


/*
* 数据收集的服务类
* */
@Slf4j
@Service
public class CollectService {
    
    @Getter @Setter
    private static Map<String, HashSet<Span>> traceTypeMap = new ConcurrentHashMap<>();
    @Getter @Setter
    private static Set<Span> traceSet = Collections.synchronizedSet(new HashSet<>());         // Span头结点集合
    @Getter @Setter
    private static Set<SpanBo> traceBoSet = Collections.synchronizedSet(new HashSet<>());     // SpanBo头结点集合
    @Getter @Setter
    private static Map<Long, SpanBo> spanBoMap = new ConcurrentHashMap<>();                   // SpanBo的Map，为了便于
    @Getter @Setter
    private static Map<Long, Span> spanMap = new ConcurrentHashMap<>();                       // span的Map
    @Getter @Setter
    private static CopyOnWriteArrayList<Span> noParentSpanSet = new CopyOnWriteArrayList<Span>();   // 有些span会在parent到达前被收集，导致没有父span对象，但parentSpan不为-1，此处先行存储
    @Getter @Setter
    private static BiMap<Integer, String> shortTypeMap = HashBiMap.create();     // key为简写，value为String值
    
    /*
    * 接收到新span后的处理逻辑
    * */
    public void addSpan(SpanBo spanBo){
        // 此处用来检查spanBo的合法性，但由于header并未传递给子span，导致合法性检验不可用
        // if(!checkLegal(spanBo)){
        //     return;
        // }
        
        // span执行时间异常，代表服务崩溃，这种span直接舍弃，否则会导致性能分析出错
        if(spanBo.getElapsed() > 100000){
            return;
        }
        spanBoMap.put(spanBo.getSpanId(), spanBo);
        
        Span span = new Span();
        span.setSpanId(spanBo.getSpanId());
        span.setParentSpanId(spanBo.getParentSpanId());
        span.setElapsed(spanBo.getElapsed());
        span.setEndPoint(spanBo.getEndPoint());
        span.setStartTime(spanBo.getStartTime());
        span.setRpc(spanBo.getRpc());
        span.setTraceType("");
        span.setType("");
        span.setAnnotationBoList(spanBo.getAnnotationBoList());
        span.setSpanEventBoList(spanBo.getSpanEventBoList());
        span.setAgentId(spanBo.getAgentId());
        spanMap.put(span.getSpanId(), span);
        
        
        // new Trace
        if (spanBo.getParentSpanId() == -1){
            traceSet.add(span);
            traceBoSet.add(spanBo);
        // Childspan
        }else{
            Long parentSpanId = span.getParentSpanId();
            // exist parent
            if (spanMap.containsKey(parentSpanId)){
                Span parentSpan = spanMap.get(parentSpanId);
                parentSpan.addChild(span);
            // not exist parent
            }else {
                CollectService.noParentSpanSet.add(span);
            }
            
        }
        // for each new span, check it has no parent span as child
        for(Span noParentSpan: noParentSpanSet){
            if (spanMap.containsKey(noParentSpan.getParentSpanId())) {
                Span noParentSpanParent = spanMap.get(noParentSpan.getParentSpanId());
                noParentSpanParent.addChild(noParentSpan);
                noParentSpanSet.remove(noParentSpan);
            }
        }
    }
    
    /*
    * pinpoint目前版本没有使用这种数据结构
    * */
    public void addSpanChunk(SpanChunkBo spanChunkBo){
        // need not to cal SpanChunk
        // spanSelfTime = span.Elpased-child1.Elapsed-child2.Elapsed-child3.Elapsed...
    }
    
    @Deprecated
    public boolean checkLegal(SpanBo spanBo){
        boolean flag_genId = false;
        boolean flag_testSetId = false;
        boolean flag_testId = false;
        List<AnnotationBo> annotationBoList = spanBo.getAnnotationBoList();
        for (AnnotationBo annotationBo : annotationBoList) {
            if (annotationBo.getKey() == Header.HTTP_MY_TOKEN && annotationBo.getValue() != null) {
                flag_genId = true;
                break;
            }
        }
        for (AnnotationBo annotationBo : annotationBoList) {
            if (annotationBo.getKey() == Header.HTTP_TASK_ID && annotationBo.getValue() != null) {
                flag_testSetId = true;
                break;
            }
        }
        for (AnnotationBo annotationBo : annotationBoList) {
            if (annotationBo.getKey() == Header.HTTP_TEST_ID && annotationBo.getValue() != null) {
                flag_testId = true;
                break;
            }
        }
        if (flag_genId && flag_testSetId && flag_testId){
            return true;
        }
        return false;
    }
    
    /*
    * 写入文件，便于debug
    * */
    public static void writeFile()  {
        File file =new File("methodInvoke.log");
        try {
            if(!file.exists()){
                file.createNewFile();
            }
    
            
            // FileWriter fileWritter = new FileWriter(file.getName(),true);
            // fileWritter.write(String.valueOf(CollectService.testSpanIdTraceMap));
            // fileWritter.close();
        }catch (IOException e){
            log.warn("FILE WRITE ERROR");
        }
        
        
    }

}
