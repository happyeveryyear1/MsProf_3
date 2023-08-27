package com.tcse.microsvcdiagnoser.service;

import com.google.common.collect.BiMap;
import com.tcse.microsvcdiagnoser.entity.Span;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import java.util.*;

/*
* 旧版本的诊断，废弃
* */
@Deprecated
@Slf4j
@Service
public class DiagnoseService {
    
    @Deprecated
    /*public Pair<LinkedList<Triple<Integer, Integer, Double>>, LinkedList<Triple<Integer, Integer, Double>>> diagnose(){
        Map<String, HashSet<Span>> traceTypeMap = CollectService.traceTypeMap;
    
        HashMap<String, LinkedList<Double>> traceSpanMeanMap = new HashMap<>();
        HashMap<String, LinkedList<Double>> traceSpanVarMap = new HashMap<>();
        
        // {
        //      type1 = [Span1-1[1,2,1,4], Span1-2[1,3,1,6], Span1-3[4,6,5,4]...]
        //      type2 = [Span2-1[1,2], Span2-2[1,1], Span2-3[4,5]...]
        // }
        HashMap<String, LinkedList<LinkedList<Integer>>> traceSpanTimeMap = new HashMap<>();
        
        // for each type
        for (Map.Entry<String, HashSet<Span>> entry: traceTypeMap.entrySet()){
            // [Span1[1,2,1,4], Span2[1,3,1,6], Span3[4,6,5,4]...]
            LinkedList<LinkedList<Integer>> traceSpanTimeList = new LinkedList<>();
            traceSpanTimeMap.put(entry.getKey(), traceSpanTimeList);
            // entry.value: {headSpan1, headSpan2, headSpan3...}
            for(int i = 0; i < entry.getValue().size(); i++){
                traceSpanTimeList.add(new LinkedList<>());
            }
            // for traceHeaderSpan in traceSet, make
            // [[1,2,3,5]   span1
            //  [2,4,2,5]   span2
            //  [3.4.2.1]]  span3
            int traceIdx = 0;
            for (Span traceHeaderspan: entry.getValue()){
                LinkedList<Span> queue = new LinkedList<>();
                queue.add(traceHeaderspan);
                while (queue.size() != 0){
                    Span tmpSpan = queue.pop();
                    traceSpanTimeList.get(traceIdx).add(tmpSpan.getSelfTime());
                    queue.addAll(tmpSpan.getChildSpan());
                }
                traceIdx += 1;
            }
           
        }
        
        // cal mean var
        for (Map.Entry<String, LinkedList<LinkedList<Integer>>> entry: traceSpanTimeMap.entrySet()){
            LinkedList<Double> traceSpanMeanList = new LinkedList<>();
            LinkedList<Double> traceSpanVarList = new LinkedList<>();
            traceSpanMeanMap.put(entry.getKey(), traceSpanMeanList);
            traceSpanVarMap.put(entry.getKey(), traceSpanVarList);
    
            LinkedList<LinkedList<Integer>> traceSpanList = entry.getValue();
            int traceNum = traceSpanList.size(); // traces in same type
            int traceId = 0; // trace in same type Id
            int spanNum = traceSpanList.get(0).size();   // span num of trace 0
            int spanId = 0; // span id for all trace
            
            for (spanId = 0; spanId < spanNum; spanId ++){
                LinkedList<Integer> spanTimeList = new LinkedList<>();  // time list of same spanId
                for (traceId = 0; traceId < traceNum; traceId++){
                    spanTimeList.add(traceSpanList.get(traceId).get(spanId));
                }
                double mean = calMean(spanTimeList);
                double var = calVar(spanTimeList);
                traceSpanMeanList.add(mean);
                traceSpanVarList.add(var);
            }
            
            
        }
    
        Comparator<Triple<Integer, Integer, Double>> TripleComparator = new Comparator<Triple<Integer, Integer, Double>>() {
            public int compare(Triple<Integer, Integer, Double> o1, Triple<Integer, Integer, Double> o2) {
                if (o1.getRight() < o2.getRight()) {
                    return 1;
                } else if (o1.getRight() > o2.getRight()) {
                    return -1;
                }else {
                    return 0;
                }
            }
        };
        
        // sort
        // [{Type1,index1, var1}, {Type2,index2, var2}, {Type3,index3,var3}]
        LinkedList<Triple<Integer, Integer, Double>> pMeanQueue = new LinkedList<>();
        LinkedList<Triple<Integer, Integer, Double>> pVarQueue = new LinkedList<>();
        BiMap<String, Integer> shortTypeMapReverse = CollectService.shortTypeMap.inverse();
        
        for(Map.Entry<String, LinkedList<Double>> entry: traceSpanMeanMap.entrySet()){
            Integer shortTypeMapKey = shortTypeMapReverse.get(entry.getKey());
            
            for(int i = 0; i < entry.getValue().size(); i++){
                pMeanQueue.add(Triple.of(shortTypeMapKey, i, entry.getValue().get(i)));
            }
        }
        
        for(Map.Entry<String, LinkedList<Double>> entry: traceSpanVarMap.entrySet()){
            Integer shortTypeMapValue = shortTypeMapReverse.get(entry.getKey());
            for(int i = 0; i < entry.getValue().size(); i++){
                pVarQueue.add(Triple.of(shortTypeMapValue, i, entry.getValue().get(i)));
            }
        }
        Collections.sort(pMeanQueue, TripleComparator);
        Collections.sort(pVarQueue, TripleComparator);
        
        return new Pair<>(pMeanQueue, pVarQueue);
        
    }*/
    
    public double calMean(LinkedList<Integer> list){
        double mean = 0.0;
        for (Integer integer : list) {
            mean += integer;
        }
        return mean / list.size();
    }
    
    public double calVar(LinkedList<Integer> list){
        double mean = 0.0;
        for (Integer integer : list) {
            mean += integer;
        }
        mean =  mean / list.size();
    
        double variance = 0;
        for (Integer integer : list) {
            variance += Math.pow(integer - mean, 2);
        }
        variance /= list.size();
        return variance;
    }
}

@Deprecated
class meanVarComparator implements Comparator<Triple<Integer, Integer, Double>> {
    @Override
    public int compare(Triple<Integer, Integer, Double> p1, Triple<Integer, Integer, Double> p2) {
        return -Double.compare(p1.getRight(), p2.getRight());
    }
}

    
    
    // // init queue
    // LinkedList<LinkedList<Span>> multiQueue = new LinkedList<>();
    // int traceNum = entry.getValue().size();
    //         for (int i = 0; i < traceNum; i++){
    //     multiQueue.push(new LinkedList<Span>());
    //     }
    //     // add first header
    //     int index = 0;
    //     for (Span traceHeaderspan: entry.getValue()){
    //     multiQueue.get(index).add(traceHeaderspan);
    //     index += 1;
    //     }
    //     while (multiQueue.get(0).size() != 0){
    //     // cal var
    //     int
    //     for(int i = 0; i < traceNum; i++){
    //
    //     }
    //     for(int i = 0; i < traceNum; i++){
    //     Span tmpSpan = multiQueue.get(i).pop();
    //     }
    //     }