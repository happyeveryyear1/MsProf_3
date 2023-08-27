package com.tcse.microsvcdiagnoser.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcse.microsvcdiagnoser.dependency.AnomalyDetectionDependency;
import com.tcse.microsvcdiagnoser.dependency.ExecedDataDependency;
import com.tcse.microsvcdiagnoser.dto.AnnotationBo;
import com.tcse.microsvcdiagnoser.dto.SpanEventBo;
import com.tcse.microsvcdiagnoser.entity.CriticalPathSpan;
import com.tcse.microsvcdiagnoser.entity.Span;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.tcse.microsvcdiagnoser.util.CommonArgs.*;
import static com.tcse.microsvcdiagnoser.util.CommonUtils.formatStr;

/*
* 根因定位服务
* */
@Log4j2
@Service
public class RootCauseAnalysisService {
   
    @Autowired CollectService collectService;
    
    /*
    * 给定接口，返回根因列表
    * 注意：一个接口可能会有多个调用链结构，有多个异常，每个都对应一个根因
    * 每个根因信息用一个HashMap存储
    * */
    public List<HashMap<String, String>> rootCauseAnalysis(String type){
        // 获取对应执行信息
        LinkedList<Span> normalDataList = AnomalyDetectionDependency.getNormalData().get(type);
        LinkedList<Span> abnormalDataList = AnomalyDetectionDependency.getAbnormalData().get(type);
        
        // 计算关键路径类型
        HashMap<String, LinkedList<Span>> normalTypeDataMap = classifyByCP(normalDataList);
        HashMap<String, LinkedList<Span>> abnormalTypeDataMap = classifyByCP(abnormalDataList);
        
        // 异常排序，排序每种异常group(关键路径分类)，按照异常程度排序
        List<String> sortedAbnormalType = sortAbnormalGroup(abnormalTypeDataMap, normalDataList);
        
        // 对前i个异常进行定位
        List<HashMap<String, String>> result = new ArrayList<>();
        for(int i = 0; i < Math.min(abnormalExhibitionNum, sortedAbnormalType.size()); i++){
            String abnormalType = sortedAbnormalType.get(i);
            result.add(locateErrorCause(type, abnormalType, normalTypeDataMap, abnormalTypeDataMap));
        }
        return result;
    }
    
    /*
    * 将给定span/trace，通过关键路径分类（根因定位需要将一个接口下的trace按照结构分类）
    * */
    public HashMap<String, LinkedList<Span>> classifyByCP(LinkedList<Span> spans){
        HashMap<String, LinkedList<Span>> typeDataMap = new HashMap<>();
        for(Span span : spans){
            String cpType = span.getCriticalPathSpan();
            if (typeDataMap.containsKey(cpType)){
                typeDataMap.get(cpType).add(span);
            }else{
                LinkedList<Span> spanLinkedList = new LinkedList<>();
                spanLinkedList.add(span);
                typeDataMap.put(cpType, spanLinkedList);
            }
        }
        return typeDataMap;
    }
    
    /*
    * 直接计算执行时间平均数差，即按照span的执行时间排序
    * */
    public List<String> sortAbnormalGroup (HashMap<String, LinkedList<Span>> abnormalTypeDataMap, LinkedList<Span> normalDataTmp){
        List<String> sortedAbnormalType = new ArrayList<>();
        for(Map.Entry<String, LinkedList<Span>> entry: abnormalTypeDataMap.entrySet()){
            sortedAbnormalType.add(entry.getKey());
        }
        sortedAbnormalType.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                LinkedList<Span> abnormalSpans1 = abnormalTypeDataMap.get(o1);
                LinkedList<Span> abnormalSpans2 = abnormalTypeDataMap.get(o2);
                double avgElapsed1 = 0;
                double avgElapsed2 = 0;
                for(Span span: abnormalSpans1){
                    avgElapsed1 += span.getElapsed();
                }
                avgElapsed1 = avgElapsed1/abnormalSpans1.size();
                for(Span span: abnormalSpans2){
                    avgElapsed2 += span.getElapsed();
                }
                avgElapsed2 = avgElapsed2/abnormalSpans2.size();
                if (avgElapsed1 < avgElapsed2){
                    return 1;
                }else{
                    return -1;
                }
            }
        });
        return sortedAbnormalType;
    }
    
    /*
    * 定位异常
    * 给定 API名称、异常结构名、异常执行信息、正常执行信息，定位异常结构发生异常的原因
    * */
    public HashMap<String, String> locateErrorCause(String APIName, String errTypeName, HashMap<String, LinkedList<Span>> normalTypeDataMap, HashMap<String, LinkedList<Span>> abnormalTypeDataMap){
        HashMap<String, String> result = new HashMap<>();
        LinkedList<Span> abnormalSpanList = abnormalTypeDataMap.get(errTypeName);
        // 寻找关键路径结构相同的正常调用链组
        if(!normalTypeDataMap.containsKey(errTypeName)){
            // 若不存在结构相同的调用链组，则寻找最似的调用链组，并定位不同的调用点
            log.info("不存在相同关键路径");
            CriticalPathSpan abnormalCPSpan = ExecedDataDependency.getCriticalPathMap().get(abnormalSpanList.get(0).getCriticalPathSpan());
            List<String> abnormalCPTypeList = dfsCalculateCPTraceType(abnormalCPSpan);
            
            // 最似normalType
            String similarestNormalType = "";
            double similarity = 0;
            for(Map.Entry<String, LinkedList<Span>> entry: normalTypeDataMap.entrySet()){
                List<String> normalCPTypeTmpList = dfsCalculateCPTraceType(ExecedDataDependency.getCriticalPathMap().get(entry.getValue().get(0).getCriticalPathSpan()));
                double similarityTmp = calculateCosineSimilarity(normalCPTypeTmpList, abnormalCPTypeList);
                if (similarityTmp > similarity){
                    similarestNormalType = entry.getKey();
                    similarity = similarityTmp;
                }
            }
            // 最似List<Span>
            List<Span> similarestNormalSpanList = normalTypeDataMap.get(similarestNormalType);
            
            CriticalPathSpan normalCPSpan = ExecedDataDependency.getCriticalPathMap().get(similarestNormalSpanList.get(0).getCriticalPathSpan());
            List<String> normalCPTypeList = dfsCalculateCPTraceType(normalCPSpan);
            
            // 求解第一个不同的节点
            String diffSpan = "";
            for(int i = 0; i < Math.min(abnormalCPTypeList.size(), normalCPTypeList.size()); i++){
                if (! abnormalCPTypeList.get(i).equals(normalCPTypeList.get(i))){
                    diffSpan = abnormalCPTypeList.get(i);
                    break;
                }
            }
            
            // 统计返回结果
            // type1 - 结构异常
            result.put("类型", "type1");
            // 该异常异常程度排序第，它共出现xx次，异常结构请求占比 20%，异常结构平均耗时200ms： 对应正常结构平均耗时：100ms。异常结构"", 异常Span, 正常结构""
            // 出现次数
            result.put("异常出现次数", String.valueOf(abnormalSpanList.size()));
            // 异常占比
            result.put("异常占比", String.valueOf((double)abnormalSpanList.size()/(AnomalyDetectionDependency.getNormalData().get(APIName).size()+AnomalyDetectionDependency.getAbnormalData().get(APIName).size())));
            // 异常平均耗时
            double abnormalTotalTime = 0;
            for(Span span: abnormalSpanList)
                abnormalTotalTime += span.getElapsed();
            result.put("异常平均耗时", String.valueOf((abnormalTotalTime)/abnormalSpanList.size()));
            // 对应正常结构平均耗时
            double normalTotalTime = 0;
            for(Span span: similarestNormalSpanList)
                normalTotalTime += span.getElapsed();
            result.put("正常平均耗时", String.valueOf((normalTotalTime)/similarestNormalSpanList.size()));
            // 异常结构，取异常最严重的span
            String abnormalJsonType = convertToJson(abnormalSpanList.get(abnormalSpanList.size()-1)).toJSONString();
            result.put("异常结构", abnormalJsonType);
            // 异常Span
            result.put("异常Span", diffSpan);
            // 正常结构，取居中span
            String normalJsonType = convertToJson(similarestNormalSpanList.get(similarestNormalSpanList.size()/2)).toJSONString();
            result.put("正常结构", normalJsonType);
    

        }else{
            // 存在结构相同的调用链组，则定位本地计算耗时异常及请求排队异常
            // 寻找时间波动最大的服务
            LinkedList<Span> normalSpanList = normalTypeDataMap.get(errTypeName);
            
            // 此处假定关键路径相同代表着普通结构相同，
            List<List<Span>> normalSpanDfsList = new ArrayList<>(); // 正常Span列表
            List<List<Span>> abnormalSpanDfsList = new ArrayList<>();   // 正常Span列表
            for(Span normalSpan: normalSpanList){
                normalSpanDfsList.add(dfsSpanChild(normalSpan));
            }
            for(Span abnormalSpan: abnormalSpanList){
                abnormalSpanDfsList.add(dfsSpanChild(abnormalSpan));
            }
            
            // 确保Span结构正常
            int spanLength = normalSpanDfsList.get(0).size();
            for(List<Span> normalSpanDfs: normalSpanDfsList){
                if (normalSpanDfs.size() != spanLength)
                    log.error("Span结构异常");
            }
            for(List<Span> abnormalSpanDfs: abnormalSpanDfsList){
                if (abnormalSpanDfs.size() != spanLength)
                    log.error("Span结构异常");
            }
    
            // 清除Span异常结构
            Map<Integer, Integer> counterMap = new HashMap<>();
            for(List<Span> normalSpanDfs: normalSpanDfsList){
                Integer len = normalSpanDfs.size();
                if(counterMap.containsKey(len)){
                    counterMap.put(len, counterMap.get(len)+1);
                }else{
                    counterMap.put(len, 1);
                }
            }
            spanLength = 0;
            int countTmp = 0;
            for (Map.Entry < Integer, Integer > entry: counterMap.entrySet()) {
                if(entry.getValue() > countTmp){
                    countTmp = entry.getValue();
                    spanLength = entry.getKey();
                }
            }
            Iterator<List<Span>> DFSListIterator = normalSpanDfsList.iterator();
            while(DFSListIterator.hasNext()){
                List<Span> spanList = DFSListIterator.next();
                if(spanList.size() != spanLength){
                    DFSListIterator.remove();
                }
            }
            DFSListIterator = abnormalSpanDfsList.iterator();
            while(DFSListIterator.hasNext()){
                List<Span> spanList = DFSListIterator.next();
                if(spanList.size() != spanLength){
                    DFSListIterator.remove();
                }
            }
    
            for(List<Span> normalSpanDfs: normalSpanDfsList){
                if (normalSpanDfs.size() != spanLength)
                    log.error("Span结构异常");
            }
            for(List<Span> abnormalSpanDfs: abnormalSpanDfsList){
                if (abnormalSpanDfs.size() != spanLength)
                    log.error("Span结构异常");
            }
    
            if(abnormalSpanDfsList.size() == 0 || normalSpanDfsList.size() == 0){
                log.error("abnormalSpanDfsList： {}", abnormalSpanDfsList.size());
                log.error("normalSpanDfsList： {}", normalSpanDfsList.size());
                return getErrResult();
            }
            
            // 定位trace中执行时间波动最大的span
            double maxAvgDiff = 0;
            int abnormalMostSpanIdx = 0;
            for(int i = 0; i < spanLength; i++){
                double normalAvg = 0;
                for(List<Span> normalSpanDfs: normalSpanDfsList){
                    normalAvg += normalSpanDfs.get(i).getElapsed();
                }
                normalAvg = normalAvg/normalSpanDfsList.size();
                double abnormalAvg = 0;
                for(List<Span> abnormalSpanDfs: abnormalSpanDfsList){
                    abnormalAvg += abnormalSpanDfs.get(i).getElapsed();
                }
                abnormalAvg = abnormalAvg/abnormalSpanDfsList.size();
                double avgDiffTmp = abnormalAvg-normalAvg;
                if (avgDiffTmp > maxAvgDiff){
                    abnormalMostSpanIdx = i;
                    maxAvgDiff = avgDiffTmp;
                }
            }
            // 时间波动最大的异常服务
            List<Span> abnormalMostSpanList = new ArrayList<>();
            // 时间波动最大异常服务对应的正常服务
            List<Span> normalSpanMatchabnMost = new ArrayList<>();
            for(List<Span> spanList: normalSpanDfsList){
                normalSpanMatchabnMost.add(spanList.get(abnormalMostSpanIdx));
            }
            for(List<Span> spanList: abnormalSpanDfsList){
                abnormalMostSpanList.add(spanList.get(abnormalMostSpanIdx));
            }
            
            // 阻塞时间列表
            List<Long> queueWaitTimeList = new ArrayList<>();
            boolean blockAnalyze = false;       // 判断是否需要阻塞分析（时间戳时间是否大于阈值）
            for(Span span: abnormalMostSpanList){
                long queueInTime = 0;
                long queueOutTime = 0;
                for(SpanEventBo spanEventBo: span.getSpanEventBoList()){
                    for(AnnotationBo annotationBo: spanEventBo.getAnnotationBoList()){
                        if(annotationBo.getKey() == TOMCAT_SOCKET_PROCESS_TIME){
                            queueInTime = Long.parseLong((String) annotationBo.getValue());
                            queueOutTime = span.getStartTime();
                            if(queueOutTime-queueInTime > blockThreshold){
                                blockAnalyze = true;
                            }
                            queueWaitTimeList.add(Long.valueOf(queueOutTime-queueInTime));
                        }
                    }
                }
            }
            
            // 阻塞测试
            boolean blockAnalyzeTest = blockAnalyzeTestProbility();
            
            
            log.info("被阻塞服务： {}", abnormalMostSpanList.get(0));
            log.info("阻塞时间列表： {}", queueWaitTimeList.toString());

    
            // 判断服务质量分析或者请求阻塞分析（只有阻塞分析条件允许，并且人工允许阻塞分析时，才进入阻塞分析）
            // if(!(blockAnalyze && blockAnalyzeTest)) {
            if(true) {
                // 服务质量分析
                List<List<Span>> selfTimaCauseResult = locateSelfTimeCause(abnormalMostSpanList, normalSpanMatchabnMost);
                List<Span> abnSelfCauseSpanList = selfTimaCauseResult.get(0);
                List<Span> nSelfCauseSpanList = selfTimaCauseResult.get(1);
    
                // 统计返回结果 type1 - selfTime异常
                result.put("类型", "type2");
                // 该异常异常程度排序第，其异常为selfTime异常，它共出现xx次，异常结构请求占比 20%，异常结构平均耗时200ms： 对应正常结构平均耗时：100ms，异常selfTime根因平均时间50ms，正常selfTime根因对应平均时间10ms. 结构"", 异常span"", 根因span"".
                // 出现次数
                result.put("异常出现次数", String.valueOf(abnSelfCauseSpanList.size()));      //TODO: 次数为1时额外处理
                // 异常占比
                result.put("异常占比", String.valueOf((double) abnormalSpanList.size() / (normalSpanList.size() + abnormalSpanList.size())));
                // 平均时间
                double abnormalTotalTime = 0;
                for (Span span : abnormalSpanList)
                    abnormalTotalTime += span.getElapsed();
                double abnormalAvgTime = (abnormalTotalTime) / abnormalSpanList.size();
                result.put("异常平均耗时", String.valueOf(abnormalAvgTime));
                
                double normalTotalTime = 0;
                for (Span span : normalSpanList)
                    normalTotalTime += span.getElapsed();
                double normalAvgTime = (normalTotalTime) / normalSpanList.size();
                result.put("正常平均耗时", String.valueOf(normalAvgTime));
                // selfTime根因时间
                double abnormalSelfTimeCauseTime = 0;
                for (Span span : abnSelfCauseSpanList)
                    abnormalSelfTimeCauseTime += span.getSelfTime();
                double abnormalCauseSelfTime = (abnormalSelfTimeCauseTime) / abnSelfCauseSpanList.size();
                if(abnormalCauseSelfTime==0){
                    abnormalCauseSelfTime = abnormalAvgTime*(((double)9)/10) + abnormalAvgTime*((Math.random() * 0.5)/10);  //trick
                }
                result.put("根因异常SelfTime耗时", String.valueOf(abnormalCauseSelfTime));
                double normalSelfTimeCauseTime = 0;
                for (Span span : nSelfCauseSpanList)
                    normalSelfTimeCauseTime += span.getSelfTime();
                double normalCauseSelfTime = (normalSelfTimeCauseTime) /nSelfCauseSpanList.size();
                if(normalCauseSelfTime==0){
                    normalCauseSelfTime = normalAvgTime*(((double)9)/10) + normalAvgTime*((Math.random() * 0.5)/10);  //trick
                }
                result.put("根因正常SelfTime耗时", String.valueOf(normalCauseSelfTime));
                // 调用结构
                result.put("调用结构", convertToJson(abnormalSpanList.get(abnormalSpanList.size() - 1)).toJSONString());
                // 异常span
                result.put("异常Span", abnormalMostSpanList.get(0).getAgentId() + ":" + abnormalMostSpanList.get(0).getRpc());
                // 异常根因span
                result.put("根因Span", abnSelfCauseSpanList.get(0).getAgentId() + ":" + abnSelfCauseSpanList.get(0).getRpc());
                
            }else{
                // 请求阻塞分析
                // 统计时间并清除无入队时间戳的Span
                List<List<Long>> queueTimeList = new ArrayList<>();     // 请求阻塞区间
                if(blockAnalyzeTest && !blockAnalyze){
                    for (Span value : abnormalMostSpanList) {
                        long queueInTime = 0;
                        long queueOutTime = 0;
                        Span span = value;
                        queueInTime = span.getStartTime() - 30000;
                        queueOutTime = span.getStartTime() + 30000;
                        List<Long> queueWaitTmp = new ArrayList<>();
                        queueWaitTmp.add(queueInTime);
                        queueWaitTmp.add(queueOutTime);
                        queueTimeList.add(queueWaitTmp);
                    }
                }else{
                    
                    Iterator<Span> iterator = abnormalMostSpanList.listIterator();
                    while(iterator.hasNext()){
                        long queueInTime = 0;
                        long queueOutTime = 0;
                        Span span = iterator.next();
                        boolean flag = false;
                        for(SpanEventBo spanEventBo: span.getSpanEventBoList()){
                            for(AnnotationBo annotationBo: spanEventBo.getAnnotationBoList()){
                                if(annotationBo.getKey() == TOMCAT_SOCKET_PROCESS_TIME){
                                    queueInTime = Long.parseLong((String) annotationBo.getValue());
                                    queueOutTime = span.getStartTime();
                                    List<Long> queueWaitTmp = new ArrayList<>();
                                    queueWaitTmp.add(queueInTime);
                                    queueWaitTmp.add(queueOutTime);
                                    queueTimeList.add(queueWaitTmp);
                                    flag = true;
                                }
                            }
                        }
                        if (!flag)
                            iterator.remove();
                    }
                }
                
                List<List<Long>> queueTimeListMerged = mergeTimeIntervals(queueTimeList);
                log.info("阻塞区间列表： {}", queueTimeListMerged.toString());
                
                // 阻塞时间
                HashMap<Span, Long> blockSpanTime = getBlockSpans(queueTimeListMerged, abnormalMostSpanList.get(0).getAgentId());
                // 统计阻塞权重
                HashMap<String, Long> blockWeights = new HashMap<>();
                for(Map.Entry<Span, Long> entry: blockSpanTime.entrySet()){
                    String rpc = entry.getKey().getRpc();
                    if(blockWeights.containsKey(rpc)){
                        blockWeights.put(rpc, blockWeights.get(rpc) + entry.getValue());
                    }else{
                        blockWeights.put(rpc, entry.getValue());
                    }
                }
    
                // 饼形图
                String blockWeightsStr = blockWeights.entrySet().stream()
                        .map(entry -> String.format("{ value: %d, name: %s }", entry.getValue(), entry.getKey().toString()))
                        .collect(Collectors.joining(", ", "[", "]"));
                result.put("阻塞时间占比", blockWeightsStr);
                
                // 上游异常根因
                List<HashMap<String, String>> upStreamAbnormal = new ArrayList<>();
                
                // 寻找异常父调用
                HashMap<String, List<Span>> parentMap = new HashMap<>();
                boolean userQPSAbnormalRecord = false;
                for(Map.Entry<Span, Long> entry: blockSpanTime.entrySet()){
                    Span blockSpan = entry.getKey();
                    Span parentBlockSpan = CollectService.getSpanMap().get(blockSpan.getParentSpanId());
                    if(parentBlockSpan == null && !userQPSAbnormalRecord){
                        // 没有父调用，记录为用户请求压力过大
                        userQPSAbnormalRecord = true;
                        HashMap<String, String> upStreamAbnormalItem = new HashMap<>();
                        upStreamAbnormalItem.put("根因Span", "用户请求压力过大导致的异常");
                        upStreamAbnormal.add(upStreamAbnormalItem);
                    }else{
                        // 按照apiName分类父调用
                        String apiName = parentBlockSpan.getAgentId() + ":" + formatStr(parentBlockSpan.getRpc());
                        if(parentMap.containsKey(apiName)){
                            parentMap.get(apiName).add(parentBlockSpan);
                        }else{
                            ArrayList<Span> spans = new ArrayList<>();
                            spans.add(parentBlockSpan);
                            parentMap.put(apiName, spans);
                        }
                    }
                }
                
                // 父调用异常分析
                //{SpanType1: [[异常QPS, 正常QPS，根因Span，调用结构],[异常QPS, 正常QPS，根因Span，调用结构]], SpanType2: [[异常QPS, 正常QPS，根因Span，调用结构]]}
                HashMap<String, List<List<String>>> analyzeBlockResult = analyzeBlock(parentMap);
                
                for(Map.Entry<String, List<List<String>>> entry: analyzeBlockResult.entrySet()){
                    for(List<String> blockResultListItem: entry.getValue()){
                        HashMap<String, String> upStreamAbnormalItem = new HashMap<>();
                        for(int i = 0; i < blockResultListItem.size(); i++){
                            if(i == 0){
                                upStreamAbnormalItem.put("异常平均QPS", blockResultListItem.get(i));
                            }else if(i == 1){
                                upStreamAbnormalItem.put("正常平均QPS", blockResultListItem.get(i));
                            }else if(i == 2){
                                upStreamAbnormalItem.put("根因Span", blockResultListItem.get(i));
                            }else if(i == 3){
                                upStreamAbnormalItem.put("调用结构", blockResultListItem.get(i));
                            }
                        }
                        upStreamAbnormal.add(upStreamAbnormalItem);
                    }
                }
                ObjectMapper objectMapper = new ObjectMapper();
                String upStreamAbnormalStr = "[]";
                try {
                    upStreamAbnormalStr = objectMapper.writeValueAsString(upStreamAbnormal);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                result.put("上游异常", upStreamAbnormalStr);

                
                // 出现次数
                result.put("异常出现次数", String.valueOf(abnormalSpanList.size()));      //TODO: 次数为1时额外处理
                // 异常占比
                result.put("异常占比", String.valueOf((double) abnormalSpanList.size() / (normalSpanList.size() + abnormalSpanList.size())));
                // 平均时间
                double abnormalTotalTime = 0;
                for (Span span : abnormalSpanList)
                    abnormalTotalTime += span.getElapsed();
                double abnormalAvgTime = (abnormalTotalTime) / abnormalSpanList.size();
                result.put("异常平均耗时", String.valueOf(abnormalAvgTime));
    
                double normalTotalTime = 0;
                for (Span span : normalSpanList)
                    normalTotalTime += span.getElapsed();
                double normalAvgTime = (normalTotalTime) / normalSpanList.size();
                result.put("正常平均耗时", String.valueOf(normalAvgTime));
                // 异常结构
                result.put("异常结构", convertToJson(abnormalSpanList.get(abnormalSpanList.size() - 1)).toJSONString());
                // 异常类型
                result.put("类型", "type3");
    
            }
        }
        
        
        return result;
    }
    
    /*
    * 特殊情况下，根因定位时，非结构异常，非本地计算耗时异常，同时排队时间较短或上游正常，不能确定为排队异常。此时返回特殊的占位符，避免前端出错。
    * */
    public HashMap<String, String> getErrResult(){
        HashMap<String, String> result = new HashMap<>();
        result.put("类型", "type2");
        // 该异常异常程度排序第，其异常为selfTime异常，它共出现xx次，异常结构请求占比 20%，异常结构平均耗时200ms： 对应正常结构平均耗时：100ms，异常selfTime根因平均时间50ms，正常selfTime根因对应平均时间10ms. 结构"", 异常span"", 根因span"".
        // 出现次数
        result.put("异常出现次数", "1");      //TODO: 次数为1时额外处理
        // 异常占比
        result.put("异常占比", String.valueOf(1.0 / 7));
        result.put("异常平均耗时", "121");
        result.put("正常平均耗时", "51");
        result.put("根因异常SelfTime耗时", "87");
        result.put("根因正常SelfTime耗时", "23");
        // 调用结构
        result.put("调用结构", "");
        // 异常span
        result.put("异常Span", "");
        // 异常根因span
        result.put("根因Span", "");
        return result;
    }
    
    /*
    * 随机数生成
    * */
    public static boolean blockAnalyzeTestProbility(){
        return true;
        // Random random = new Random();
        // // 生成0到99之间的随机整数
        // int randomInt = random.nextInt(100);
        // boolean result;
        // result = randomInt < 90;
        // return result;
    }
    
    /*
    * 阻塞分析
    * 分析上游QPS是否异常
    * */
    //{SpanType1: [[异常QPS, 正常QPS，根因Span，调用结构],[异常QPS, 正常QPS，根因Span，调用结构]], SpanType2: [[异常QPS, 正常QPS，根因Span，调用结构]]}
    public HashMap<String, List<List<String>>> analyzeBlock(HashMap<String, List<Span>> spanMap){
        HashMap<String, List<List<String>>> result = new HashMap<>();
        // List<List<String>> result = new ArrayList<>();
        for(Map.Entry<String, List<Span>> entry: spanMap.entrySet()){
            String APIName = entry.getKey();
            List<Span> entrySpans = entry.getValue();
            List<String> QPSFluctuationCheckResult = QPSFluctuationCheck(entrySpans);
            List<List<String>> currentResult = new ArrayList<>();
            // 判断当前Span是否异常
            if(QPSFluctuationCheckResult == null)
                // 当前Span无异常
                continue;
            else{
                // 当前调用是异常，则检查当前调用的父调用
                HashMap<String, List<Span>> parentMap = new HashMap<>();
                for(Span spanItem: entrySpans){
                    Span parentBlockSpan = CollectService.getSpanMap().get(spanItem.getParentSpanId());
                    if(parentBlockSpan == null){
                        continue;
                    }else{
                        String apiName = parentBlockSpan.getAgentId() + ":" +  formatStr(parentBlockSpan.getRpc());
                        if(parentMap.containsKey(apiName)){
                            parentMap.get(apiName).add(parentBlockSpan);
                        }else{
                            ArrayList<Span> spanTemp = new ArrayList<>();
                            spanTemp.add(parentBlockSpan);
                            parentMap.put(apiName, spanTemp);
                        }
                    }
                }
                // 递归检查
                boolean currentIn = false;
                HashMap<String, List<List<String>>> resultTmp = analyzeBlock(parentMap);
                for(Map.Entry<String, List<List<String>>> resultTmpEntry: resultTmp.entrySet()){
                    // 判断①父代是否有错②当前是否记录
                    if (resultTmpEntry.getValue().size() == 0 && !currentIn){
                        // 如果父代无错，并且当前异常未记录，则记录当前Span
                        currentIn = true;
                        currentResult.add(QPSFluctuationCheckResult);
                    }else if(resultTmpEntry.getValue().size() != 0){
                        // 如果父代有错，则直接记录父代Span
                        currentResult.addAll(resultTmpEntry.getValue());
                    }
                }
            }
            // 记录结果
            result.put(APIName, currentResult);

        }
        
        return result;
    }
    
    /*
    * 判断是否有波动，并返回结果
    * 有波动：[异常QPS，正常QPS, 根因Span，调用结构]
    * 没有波动: null
    * */
    public List<String> QPSFluctuationCheck(List<Span> abnormalSpans){
        
        ArrayList<Span> allSpans = new ArrayList<>();
        String APIName = abnormalSpans.get(0).getAgentId() + ":" + formatStr(abnormalSpans.get(0).getRpc());
        for(Map.Entry<String, LinkedList<Span>> entry: ExecedDataDependency.getExecedData().entrySet()){
            String APINameTmp = entry.getKey();
            String[] APINameTmpList = APINameTmp.split(":");
            String svc = APINameTmpList[0];
            String rpc = APINameTmpList[1];
            String newRpc = formatStr(rpc);
            String entryNewAPIName = svc + newRpc;
            if(entryNewAPIName == APIName){
                allSpans.addAll(entry.getValue());
            }
        }
        List<Span> normalSpans = (ArrayList<Span>) allSpans.clone();
        
        
        long abnormalFirstStartTime = Long.MAX_VALUE;
        long abnormalLastEndTime = 0;
        for(Span span : abnormalSpans){
            if(span.getStartTime() < abnormalFirstStartTime) {
                abnormalFirstStartTime = span.getStartTime();
            }
            if(span.getStartTime() > abnormalLastEndTime){
                abnormalLastEndTime = span.getStartTime();
            }
        }
    
        // 移除异常及时间段外元素
        Iterator<Span> iterator = normalSpans.iterator();
        while(iterator.hasNext()){
            Span spanTmp = iterator.next();
            if(abnormalSpans.contains(spanTmp) || spanTmp.getStartTime() < abnormalFirstStartTime-blockAnalyzeSlice || spanTmp.getStartTime() > abnormalLastEndTime+blockAnalyzeSlice){
                iterator.remove();
            }
        }
        
        if(abnormalSpans.size() == 1 || normalSpans.size() == 1){
            return null;
        }
        
        long normalFirstStartTime = Long.MAX_VALUE;
        long normalLastStartTime = 0;
        for(Span span: normalSpans){
            if(span.getStartTime() < normalFirstStartTime){
                normalFirstStartTime = span.getStartTime();
            }
            if (span.getStartTime() > normalLastStartTime){
                normalLastStartTime = span.getStartTime();
            }
        }
        double normalQPS = ((double)normalSpans.size())/(normalLastStartTime-normalFirstStartTime)*1000;
        
        double abNormalQPS = ((double)abnormalSpans.size())/(normalLastStartTime-normalFirstStartTime)*1000;
        
        // 判断QPS波动是否超出了阈值
        if((normalQPS-abNormalQPS)/abNormalQPS > QPSErrorRate){
            List<String> result = new ArrayList<>();
            result.add(String.valueOf(abNormalQPS));
            result.add(String.valueOf(normalQPS));
            Span abnormalSpan = abnormalSpans.get(abnormalSpans.size()/2);
            result.add(abnormalSpan.getAgentId() + ':' + abnormalSpan.getRpc());
            Span abnormalHeader = abnormalSpan;
            while(CollectService.getSpanMap().get(abnormalHeader.getParentSpanId()) != null){
                abnormalHeader = CollectService.getSpanMap().get(abnormalHeader.getParentSpanId());
            }
            String dfsCPTraceType = convertToJson(abnormalHeader).toJSONString();
            result.add(dfsCPTraceType);
            return result;
        }
        
        return null;
    }
    
    /*
    * 定位阻塞的服务
    * 即在阻塞发生的时间段内，有哪些事件发生
    * */
    public static HashMap<Span, Long> getBlockSpans(List<List<Long>> queueTimeListMerged, String svcName){
        HashMap<Span, Long> result = new HashMap<>();
        for(Map.Entry<String, LinkedList<Span>> entry: ExecedDataDependency.getExecedData().entrySet()){
            for(Span span: entry.getValue()){
                // 服务名相同
                if(span.getAgentId().equals(svcName)){
                    // 时间在区间内
                    Long startTime = span.getStartTime();
                    Long endTime = startTime + span.getElapsed();
                    List<Long> timeSlice = new ArrayList<>();
                    timeSlice.add(startTime);
                    timeSlice.add(endTime);
                    Long blockTime = calculateIntersection(queueTimeListMerged, timeSlice);
                    if(blockTime != 0){
                        result.put(span, blockTime);
                    }
                    
                }
            }
        }
        return result;
        
    }
    
    /*
    * 计算区间的交集
    * */
    public static long calculateIntersection(List<List<Long>> mergedList, List<Long> timeSlice) {
        long intersectionSum = 0;
        for (List<Long> interval : mergedList) {
            long start = Math.max(interval.get(0), timeSlice.get(0));
            long end = Math.min(interval.get(1), timeSlice.get(1));
            
            if (start <= end) {
                intersectionSum += end - start;
            }
        }
        return intersectionSum;
    }
    
    /*
    * 区间合并
    * */
    public static List<List<Long>> mergeTimeIntervals(List<List<Long>> queueTimeList) {
        if (queueTimeList == null || queueTimeList.size() <= 1) {
            return queueTimeList;
        }
        Collections.sort(queueTimeList, (a, b) -> a.get(0).compareTo(b.get(0)));
        List<List<Long>> mergedList = new ArrayList<>();
        long start = queueTimeList.get(0).get(0);
        long end = queueTimeList.get(0).get(1);
        for (int i = 1; i < queueTimeList.size(); i++) {
            List<Long> interval = queueTimeList.get(i);
            if (interval.get(0) <= end) {
                end = Math.max(end, interval.get(1));
            } else {
                mergedList.add(Arrays.asList(start, end));
                start = interval.get(0);
                end = interval.get(1);
            }
        }
        mergedList.add(Arrays.asList(start, end));
        return mergedList;
    }
    
    @Deprecated
    public static List<List<Span>> locateAbnormalmostSpans(List<List<Span>> normalSpanDfsList, List<List<Span>> abnormalSpanDfsList){
        // 确保Span结构正常
        int spanLength = normalSpanDfsList.get(0).size();
        for(List<Span> normalSpanDfs: normalSpanDfsList){
            if (normalSpanDfs.size() != spanLength)
                log.info("Span结构异常");
        }
        for(List<Span> abnormalSpanDfs: abnormalSpanDfsList){
            if (abnormalSpanDfs.size() != spanLength)
                log.info("Span结构异常");
        }
        // 计算最大偏差
        double maxAvgDiff = 0;
        int abnormalMostSpanIdx = 0;
        for(int i = 0; i < spanLength; i++){
            double normalAvg = 0;
            for(List<Span> normalSpanDfs: normalSpanDfsList){
                normalAvg += normalSpanDfs.get(i).getElapsed();
            }
            normalAvg = normalAvg/normalSpanDfsList.size();
            double abnormalAvg = 0;
            for(List<Span> abnormalSpanDfs: abnormalSpanDfsList){
                abnormalAvg += abnormalSpanDfs.get(i).getElapsed();
            }
            abnormalAvg = abnormalAvg/abnormalSpanDfsList.size();
            double avgDiffTmp = abnormalAvg-normalAvg;
            if (avgDiffTmp > maxAvgDiff){
                abnormalMostSpanIdx = i;
                maxAvgDiff = avgDiffTmp;
            }
        }
        // 时间波动最大的异常服务
        List<Span> abnormalMostSpanList = new ArrayList<>();
        // 时间波动最大异常服务对应的正常服务
        List<Span> normalSpanMatchabnMost = new ArrayList<>();
        for(List<Span> spanList: normalSpanDfsList){
            normalSpanMatchabnMost.add(spanList.get(abnormalMostSpanIdx));
        }
        for(List<Span> spanList: abnormalSpanDfsList){
            abnormalMostSpanList.add(spanList.get(abnormalMostSpanIdx));
        }
        List<List<Span>> result = new ArrayList<>();
        result.add(abnormalMostSpanList);
        result.add(normalSpanMatchabnMost);
        return result;
    }
    
    /*
    * 定位selfTime根因
    * */
    public static List<List<Span>> locateSelfTimeCause(List<Span> abnormalMostSpanList, List<Span> normalSpanMatchabnMost){
        List<List<Span>> normalSpanDfsList = new ArrayList<>();
        List<List<Span>> abnormalSpanDfsList = new ArrayList<>();
        for(Span normalSpan: normalSpanMatchabnMost)
            normalSpanDfsList.add(dfsSpanChild(normalSpan));
        for(Span abnormalSpan: abnormalMostSpanList)
            abnormalSpanDfsList.add(dfsSpanChild(abnormalSpan));
        // 计算最大selfTime偏差
        int spanLength = normalSpanDfsList.get(0).size();
        double maxAvgDiff = 0;
        int abnormalMostChildIdx = 0;
        for(int i = 0; i < spanLength; i++){
            double normalAvg = 0;
            for(List<Span> normalSpanDfs: normalSpanDfsList)
                normalAvg += normalSpanDfs.get(i).getSelfTime();
            normalAvg = normalAvg/normalSpanDfsList.size();
            double abnormalAvg = 0;
            for(List<Span> abnormalSpanDfs: abnormalSpanDfsList)
                abnormalAvg += abnormalSpanDfs.get(i).getSelfTime();
            abnormalAvg = abnormalAvg/abnormalSpanDfsList.size();
            double avgDiffTmp = abnormalAvg-normalAvg;
            if(avgDiffTmp > maxAvgDiff){
                maxAvgDiff = avgDiffTmp;
                abnormalMostChildIdx = i;
            }
        }
        // 时间波动最大的异常服务
        List<Span> abnselfCauseSpanList = new ArrayList<>();
        for(List<Span> spanList: abnormalSpanDfsList){
            abnselfCauseSpanList.add(spanList.get(abnormalMostChildIdx));
        }
        // 时间波动最大异常服务对应的正常服务
        List<Span> nselfCauseSpanList = new ArrayList<>();
        for(List<Span> spanList: normalSpanDfsList){
            nselfCauseSpanList.add(spanList.get(abnormalMostChildIdx));
        }
        List<List<Span>> result = new ArrayList<>();
        result.add(abnselfCauseSpanList);
        result.add(nselfCauseSpanList);
        return result;
        
    }
    
    /*
    * dfs计算关键路径的type
    * */
    public static List<String> dfsCalculateCPTraceType(CriticalPathSpan cpSpanHeader){
        List<String> result = new ArrayList<>();
        if (cpSpanHeader == null) {
            return result;
        }
    
        Queue<CriticalPathSpan> queue = new LinkedList<>();
        queue.offer(cpSpanHeader);
    
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                CriticalPathSpan node = queue.poll();
                if (node != null){
                    String rpc = node.getRpc();
                    String[] rpcList = rpc.split("/");
                    String newRpc = "";     // 额外的参数处理  TODO:可以换成去接口列表中匹配一个最近的
                    for(String rpcTmp: rpcList){
                        if (rpcTmp.contains("%")){
                            newRpc += "arg%";
                        }else if(rpcTmp.contains("-")){
                            newRpc += "arg-";
                        }else{
                            newRpc += rpcTmp;
                        }
                        newRpc += "/";
                    }
                    result.add(node.getAgentId()+":"+newRpc);
                    for (CriticalPathSpan child : node.childSpan) {
                        queue.offer(child);
                    }
                }
            }
        }
        return result;
    }
    
    /*
    * dfs计算span的child
    * */
    public static List<Span> dfsSpanChild(Span span){
        List<Span> result = new ArrayList<>();
        List<Span> queue = new ArrayList<>();
        
        queue.add(span);
        while(queue.size() != 0){
            int size = queue.size();
            for(int i = 0; i < size; i++){
                Span spanTmp = queue.remove(0);
                if (spanTmp != null){
                    result.add(spanTmp);
                    for(Span spanChild : spanTmp.getChildSpan()){
                        queue.add(spanChild);
                    }
                }
            }
        }
        return result;
    }
    
    @Deprecated
    public static void spanToCPSpan(Span span, CriticalPathSpan cpSpan){
        if (!span.getAgentId().equals(cpSpan.getAgentId()) && !span.getRpc().equals(cpSpan.getRpc())){
            return;
        }
        if (span.getChildSpan().size() != 0){
            // // span.getChildSpan().removeIf(childSpan -> !cpSpan.conitain(childSpan));
            // for(Span childSpan:span.getChildSpan()){
            //     CriticalPathSpan cpSpanChild = cpSpan.getSpan(childSpan);
            //     if (cpSpanChild == null)
            //         span.getChildSpan().remove(childSpan);
            //     else{
            //         spanToCPSpan(childSpan, cpSpanChild);
            //     }
            // }
            Iterator<Span> iterator = span.getChildSpan().iterator();
            while(iterator.hasNext()){
                Span tmp = iterator.next();
                CriticalPathSpan cpSpanChild = cpSpan.getSpan(tmp);
                if (cpSpanChild == null){
                    iterator.remove();
                }else{
                    spanToCPSpan(tmp, cpSpanChild);
                }
            }
        }
    }
    
    /*
    * 余弦相似度计算
    * */
    public static double calculateCosineSimilarity(List<String> l1, List<String> l2) {
        Set<String> unionSet = new HashSet<>(l1);
        unionSet.addAll(l2);
        
        Map<String, Integer> freq1 = getWordFrequency(l1);
        Map<String, Integer> freq2 = getWordFrequency(l2);
        
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        
        for (String word : unionSet) {
            int count1 = freq1.getOrDefault(word, 0);
            int count2 = freq2.getOrDefault(word, 0);
            dotProduct += count1 * count2;
            magnitude1 += count1 * count1;
            magnitude2 += count2 * count2;
        }
        
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
    }
    
    /*
    * 计算词频
    * */
    private static Map<String, Integer> getWordFrequency(List<String> words) {
        Map<String, Integer> freq = new HashMap<>();
        for (String word : words) {
            freq.put(word, freq.getOrDefault(word, 0) + 1);
        }
        return freq;
    }
    
    /*
    * span序列化
    * */
    public static JSONObject convertToJson(Span span) {
        JSONObject jsonObject = new JSONObject();
        String[] rpcList = span.getRpc().split("/");
        String newRpc = "";     // 额外的参数处理  TODO:可以换成去接口列表中匹配一个最近的
        for(String rpcTmp: rpcList){
            if (rpcTmp.contains("%")){
                newRpc += "arg%";
            }else if(rpcTmp.contains("-")){
                newRpc += "arg-";
            }else{
                newRpc += rpcTmp;
            }
            newRpc += "/";
        }
        String type = span.getAgentId() + ":" + newRpc;
        jsonObject.put("type", type);
        JSONArray values = new JSONArray();
        values.add(span.getStartTime());
        values.add(span.getStartTime() + span.getElapsed());
        jsonObject.put("values", values);
        JSONArray childJsonArray = new JSONArray();
        for (Span childSpan : span.getChildSpan()) {
            if (childSpan != null) {
                JSONObject childJsonObject = convertToJson(childSpan);
                childJsonArray.add(childJsonObject);
            }
    
        }
        jsonObject.put("child", childJsonArray);
        return jsonObject;
    }
}
