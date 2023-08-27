package com.tcse.microsvcdiagnoser.service;

import com.tcse.microsvcdiagnoser.context.Header;
import com.tcse.microsvcdiagnoser.dependency.AnomalyDetectionDependency;
import com.tcse.microsvcdiagnoser.dependency.ExecedDataDependency;
import com.tcse.microsvcdiagnoser.dependency.TestSetDependency;
import com.tcse.microsvcdiagnoser.dto.AnnotationBo;
import com.tcse.microsvcdiagnoser.dto.SpanBo;
import com.tcse.microsvcdiagnoser.entity.Span;
import com.tcse.microsvcdiagnoser.entity.TestSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.tcse.microsvcdiagnoser.util.CommonArgs.anomalyRatio;

/*
* 异常检测服务
* */
@Slf4j
@Service
public class AnomalyDetectionService {
   
    /*
    * 整理数据，异常检测的同时为执行信息统计和根因定位做准备
    * */
    public void flushData(){
        
        // 整理有效数据
        collectData();
        
        // span排序
        sortDataBySpan();
        
        // service排序
        sortDataByService();
  
        // 异常检测
        separateData();
    }
    
    /*
    *   从span数据中提取有效数据。
    *   由于第一版的MsProf会接收所有的pinpoint collector收集的数据，因此在这里需要做性能分析数据和日常执行数据的分离
    *   新版本的MsProf修复了此问题，性能分析会有额外的header
    * */
    public void collectData(){
        // 直接从span开始扫描，分析数据
        for (Span headerSpan : CollectService.getTraceSet()) {
            boolean flag_genId = false;
            boolean flag_testSetId = false;
            boolean flag_testId = false;
            List<AnnotationBo> annotationBoList = headerSpan.getAnnotationBoList();
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
            // 定位数据与性能分析相关，存入ExecedDataDependency
            if (flag_genId && flag_testSetId && flag_testId){
                String APIName = headerSpan.getAgentId() + ":" + headerSpan.getRpc();
                headerSpan.setType(APIName);
                if (ExecedDataDependency.getExecedData().containsKey(APIName))
                    ExecedDataDependency.getExecedData().get(APIName).push(headerSpan);
                else{
                    LinkedList<Span> spanLinkedList = new LinkedList<>();
                    spanLinkedList.add(headerSpan);
                    ExecedDataDependency.getExecedData().put(APIName, spanLinkedList);
                }
            }
        }
    }
    
    /*
    * 排序header节点
    * {APIName1: [spanHeader1, spanHeader2, spanHeader3... spanHeadern(spanHeader排序，排序依据：Elapsed(正序))], APIName2: ...}
    * 每个服务的调用链集合要根据elapsed做3-sigma分析
    * */
    public void sortDataBySpan(){
        for (LinkedList<Span> spanLinkedList: ExecedDataDependency.getExecedData().values()){
            spanLinkedList.sort(Comparator.comparingInt(Span::getElapsed));
        }
    }
    
    /*
    * 排序服务(service+rpc)
    * {APIName1: [spanHeader1, spanHeader2, spanHeader3... spanHeadern(Elapsed排序)], APIName2: ...}(APIName排序，排序依据：变异系数(倒序))
    * 排序服务的异常程度，最异常的服务应当被最先分析
    * */
    public void sortDataByService(){
        List<Map.Entry<String, LinkedList<Span>>> entries = new ArrayList<>(ExecedDataDependency.getExecedData().entrySet());
        entries.sort(Comparator.comparingDouble(o -> (-getCV(o.getValue())))); //逆序
        ExecedDataDependency.getExecedData().clear();
        for(Map.Entry<String, LinkedList<Span>> e: entries)
            ExecedDataDependency.getExecedData().put(e.getKey(), e.getValue());
    }
    
    /*
    * 计算变异系数
    * */
    public double getCV(LinkedList<Span> spanLinkedList){
        double mean = 0;
        for (Span span: spanLinkedList){
            mean += span.getElapsed();
        }
        mean /= spanLinkedList.size();
        
        double std = 0;
        for (Span span: spanLinkedList){
            std += Math.sqrt(span.getElapsed()-mean);
        }
        std /= spanLinkedList.size();
        std = Math.pow(std, 0.5);
        
        return std/mean;
    }
    
    /*
    * 分离正常和异常数据
    * */
    public void separateData(){
        Set<Map.Entry<String, LinkedList<Span>>> entrySet = ExecedDataDependency.getExecedData().entrySet();
        for(Map.Entry<String, LinkedList<Span>> entry: entrySet){
            String API = entry.getKey();
            LinkedList<Span> spanLinkedList = entry.getValue();
            int size = spanLinkedList.size();
            if (size < 3){                  // 数据过少，跳过
                continue;
            }
            int abnormalSize = (int)Math.round(size*anomalyRatio);
            if (2*abnormalSize > size)      // 防止四舍五入之后三部分加起来占比大于1
                abnormalSize -= 1;
            if (abnormalSize <= 0)          // 取不到的情况下跳过
                continue;
            // 取大小两部分异常
            /*
            for (int i = 0; i < abnormalSize; i++){
                if (abnormalDataSmallData.containsKey(API))
                    abnormalDataSmallData.get(API).add(spanLinkedList.get(i));
                else{
                    LinkedList<Span> tmpSpanList = new LinkedList<>();
                    tmpSpanList.add(spanLinkedList.get(i));
                    abnormalDataSmallData.put(API, tmpSpanList);
                }
            }
            for (int i = size-abnormalSize; i < size; i++){
                if (abnormalDataLargeData.containsKey(API))
                    abnormalDataLargeData.get(API).add(spanLinkedList.get(i));
                else{
                    LinkedList<Span> tmpSpanList = new LinkedList<>();
                    tmpSpanList.add(spanLinkedList.get(i));
                    abnormalDataLargeData.put(API, tmpSpanList);
                }
            }
            for (int i = abnormalSize; i < size-abnormalSize; i++){
                if (AnomalyDetectionDependency.getNormalData().containsKey(API))
                    AnomalyDetectionDependency.getNormalData().get(API).add(spanLinkedList.get(i));
                else{
                    LinkedList<Span> tmpSpanList = new LinkedList<>();
                    tmpSpanList.add(spanLinkedList.get(i));
                    AnomalyDetectionDependency.getNormalData().put(API, tmpSpanList);
                }
            }
            */
            // 异常只取执行时间大的部分
            for (int i = 0; i < size-abnormalSize; i++){
                if (AnomalyDetectionDependency.getNormalData().containsKey(API))
                    AnomalyDetectionDependency.getNormalData().get(API).add(spanLinkedList.get(i));
                else{
                    LinkedList<Span> tmpSpanList = new LinkedList<>();
                    tmpSpanList.add(spanLinkedList.get(i));
                    AnomalyDetectionDependency.getNormalData().put(API, tmpSpanList);
                }
            }
            for (int i = size-1; i >= size-abnormalSize; i--){
                if (AnomalyDetectionDependency.getAbnormalData().containsKey(API))
                    AnomalyDetectionDependency.getAbnormalData().get(API).add(spanLinkedList.get(i));
                else{
                    LinkedList<Span> tmpSpanList = new LinkedList<>();
                    tmpSpanList.add(spanLinkedList.get(i));
                    AnomalyDetectionDependency.getAbnormalData().put(API, tmpSpanList);
                }
            }
        }
    }
}
