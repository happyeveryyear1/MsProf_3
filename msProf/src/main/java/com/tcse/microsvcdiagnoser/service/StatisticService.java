package com.tcse.microsvcdiagnoser.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tcse.microsvcdiagnoser.dependency.*;
import com.tcse.microsvcdiagnoser.entity.*;
import lombok.extern.slf4j.Slf4j;
import com.tcse.microsvcdiagnoser.dependency.StatisticDependency;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tcse.microsvcdiagnoser.util.CommonUtils.formatStr;

/*
* 静态分析
* 主要为整理数据，同时给出前端需要的执行信息，即服务接口信息，
* */
@Slf4j
@Service
public class StatisticService {
    // 服务 - 多个接口 - 多个请求
    // public HashMap<String, HashMap<String, LinkedHashMap>> srvSpanMap = new HashMap<>();
    
    /*
    * 记录的数据转为interfaceMap结构
    * */
    public void flushData(){
        for (Map.Entry<String, LinkedList<Span>> entry : ExecedDataDependency.getExecedData().entrySet()) {
            String svcUrl = entry.getKey(); // ts-travel2-1.0: /api/v1/travel2service/trips/left -> {LinkedList@6747}  size = 36
            LinkedList<Span> spanLinkedList = entry.getValue();
            String[] parts = svcUrl.split(":");
            String svcName = parts[0];
            StringBuilder urlBuffer = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                urlBuffer.append(parts[i]);
            }
            String url = urlBuffer.toString();
            
            String interfaceStr = getInterface(url);
            ServiceMap svc = StatisticDependency.getSrvMap().get(svcName);
            if (svc == null){
                svc = new ServiceMap(svcName);
                StatisticDependency.getSrvMap().put(svcName, svc);
            }
            InterfaceMap interfaceMap = svc.getInterfaceMap().get(interfaceStr);
            if(interfaceMap == null){
                interfaceMap = new InterfaceMap(interfaceStr);
                svc.interfaceMap.put(interfaceStr, interfaceMap);
            }
            interfaceMap.spanListMap.put(svcUrl, spanLinkedList);
        }
    }
    
    /*
    * url转为API接口。使用相似度比对
    * */
    public static String getInterface(String url){
        String res = "";
        double similarityValue = 0;
        for (String interfaceStr: RequestDependency.getRequestMap().keySet()){
            double similarityTmp = cosineSimilarity(interfaceStr, url);
            if (similarityTmp > similarityValue){
                res = interfaceStr;
                similarityValue = similarityTmp;
            }
        }
        return res;
    }
    

    /*
    * 为每条trace计算关键路径
    * */
    public void staticsCriticalPath(){
        for(LinkedList<Span> spanList: ExecedDataDependency.getExecedData().values()){
            for (Span spanHeader: spanList){
                // 注意，为了方便统计时间，在计算关键路径的时候直接累积执行时间，所以请勿重复计算关键路径
                CriticalPathSpan criticalPathSpan = calculateCriticalPath(spanHeader);  // 计算关键路径
                String cpSpanType = dfsCalculateCPTraceType(criticalPathSpan);          // dfs遍历关键路径，生成该关键路径的type
                // 记录数据
                spanHeader.setCriticalPathSpan(cpSpanType);
                if (ExecedDataDependency.getCriticalPathMap().containsKey(cpSpanType)){
                    ExecedDataDependency.getCriticalPathMap().get(cpSpanType).criticalPathSet.add(spanHeader);
                }else{
                    ExecedDataDependency.getCriticalPathMap().put(cpSpanType, criticalPathSpan);
                    criticalPathSpan.criticalPathSet.add(spanHeader);
                }
            }
        }
        
        // 为方便格式化数据，计算关键路径的世界、关键路径的child Num
        for(CriticalPathSpan criticalPathSpan: ExecedDataDependency.getCriticalPathMap().values()){
            calculateCPTime(criticalPathSpan);
            calculateCPChildNum(criticalPathSpan);
        }
    }
    
    /*
    * 给定字符串 agentID+RPC，返回该接口下的所有关键路径
    * */
    public static String getInterfaceCP(String agentIdRpc) {
        CriticalPathSpan deepestCPSpan = null;
        int depthTmp = 0;
        String[] agentIdRpcList = agentIdRpc.split("/");
        String newAgentIdRpc = agentIdRpcList[0] + "/";
        for(int i = 1; i < agentIdRpcList.length; i++){
            if (agentIdRpcList[i].contains("%")){
                newAgentIdRpc += "arg%";
            }else if(agentIdRpcList[i].contains("-")){
                newAgentIdRpc += "arg-";
            }else{
                newAgentIdRpc += agentIdRpcList[i];
            }
            newAgentIdRpc += "/";
        }
        for(Map.Entry<String, CriticalPathSpan> entry: ExecedDataDependency.getCriticalPathMap().entrySet()){
            if (entry.getKey().startsWith(newAgentIdRpc) && entry.getValue().childNum > depthTmp){
                depthTmp = entry.getValue().childNum;
                deepestCPSpan = entry.getValue();
            }
        }
        String json = cpSpan2String(deepestCPSpan).toString();
        return json;
    }
    
    /*
    * 关键路径转string
    * */
    public static JSONObject cpSpan2String(CriticalPathSpan criticalPathSpan){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("name", criticalPathSpan.getAgentId() + ":" + criticalPathSpan.getRpc());
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
    * 给定服务名+接口名，获取所有trace
    * */
    public static List<String> getTraceData(String svcStr, String interfaceStr){
        
        ServiceMap svcMap = StatisticDependency.getSrvMap().get(svcStr);
        InterfaceMap interfaceMap = svcMap.interfaceMap.get(interfaceStr);
        List<String> result = new LinkedList<>();
        List<String> record = new LinkedList<>();
        for(Map.Entry<String, LinkedList<Span>> entry: interfaceMap.getSpanListMap().entrySet()){
            for(Span span: entry.getValue()){
                if(! record.contains(span.getTraceType())){
                    result.add(convertToJson(span).toJSONString());
                    record.add(span.getTraceType());
                }
            }
        }
        return result;
    }
    
    public static void calculateSpanTypeAndDepth(){
        // 计算类型和深度，类型为dfs-json格式
        for(Map.Entry<String, LinkedList<Span>> entry: ExecedDataDependency.getExecedData().entrySet()){
            LinkedList<Span> spanLinkedList = entry.getValue();
            for(Span span: spanLinkedList){
                span.setTraceType(calSpanTraceType(span).toJSONString());
                calculateSpanChildNum(span);
            }
        
        }
    }
    
    /*
    * traceType转为JSONnode格式，便于前端使用
    * */
    // 注意 TraceType不包含时间数据
    public static JSONObject calSpanTraceType(Span span){
        JSONObject jsonObject = new JSONObject();
        String rpc = span.getRpc().replaceAll("[^\\-]+", "_");   // Trick 把token简化
        rpc = rpc.replace("[^\\%]+", "_");  // Trick 把参数简化
        String type = span.getAgentId() + ":" + rpc;
        
        jsonObject.put("type", type);
        JSONArray childJsonArray = new JSONArray();
        for (Span childSpan : span.getChildSpan()) {
            if(childSpan != null){
                JSONObject childJsonObject = calSpanTraceType(childSpan);
                childJsonArray.add(childJsonObject);
            }
            
        }
        jsonObject.put("child", childJsonArray);
        return jsonObject;
    }
    
    /*
    * 层序遍历
    * */
    public static void calculateSpanChildNum(Span span){
        Queue<Span> queue = new LinkedList<>();
        queue.add(span);
        while (!queue.isEmpty()) {
            Span current = queue.poll();
            span.childrenNum += 1;
            for (Span child : current.getChildSpan()) {
                queue.add(child);
            }
        }
    }
    
    /*
    * span JSON转换
    * */
    public static JSONObject convertToJson(Span span) {
        JSONObject jsonObject = new JSONObject();
        String type = span.getAgentId() + ":" + span.getRpc();
        jsonObject.put("type", type);
        JSONArray values = new JSONArray();
        values.add(span.getStartTime());
        values.add(span.getStartTime() + span.getElapsed());
        jsonObject.put("values", values);
        JSONArray childJsonArray = new JSONArray();
        for (Span childSpan : span.getChildSpan()) {
            if(childSpan != null){
                JSONObject childJsonObject = convertToJson(childSpan);
                childJsonArray.add(childJsonObject);
            }

        }
        jsonObject.put("child", childJsonArray);
        return jsonObject;
    }
    
    /*
    * 给定服务名、接口名，返回请求耗时-概率(占比)分布图，函数内为一些数据统计的工作
    * */
    public static String getStatisticTime_ElapsedProb(String svcStr, String interfaceStr){
        
        ServiceMap svcMap = StatisticDependency.getSrvMap().get(svcStr);
        InterfaceMap interfaceMap = svcMap.interfaceMap.get(interfaceStr);

        HashMap<Long, Integer> elapsedCountMap = new HashMap<>();
        
        int totalCount = 0;
        
        for(Map.Entry<String, LinkedList<Span>> entry: interfaceMap.getSpanListMap().entrySet()){
            LinkedList<Span> spanLinkedList = entry.getValue();
            for(Span span: spanLinkedList){
                totalCount += 1;
                long elapsed = span.getElapsed();
                if(elapsedCountMap.containsKey(elapsed)){
                    elapsedCountMap.put(elapsed, elapsedCountMap.get(elapsed) + 1);
                }else{
                    elapsedCountMap.put(elapsed, 1);
                }
            }
        }
    
        Map<Long, String> xAxisSeriesDataMap = new TreeMap<Long, String>(new Comparator<Long>() {
            @Override
            public int compare(Long i1, Long i2) {
                return i1.compareTo(i2);
            }
        });
        DecimalFormat df = new DecimalFormat("0.0000"); // #.00 表示保留四位小数
        for(Map.Entry<Long, Integer> entry: elapsedCountMap.entrySet()){
            xAxisSeriesDataMap.put(entry.getKey(), df.format(((double)entry.getValue())/totalCount));
        }
    
        JSONObject jsonObject = new JSONObject();
        
        JSONArray xAxisArray = new JSONArray();
        for (Long key : xAxisSeriesDataMap.keySet()) {
            xAxisArray.add(key);
        }
        
        JSONArray seriesDataArray = new JSONArray();
        for (String value : xAxisSeriesDataMap.values()) {
            seriesDataArray.add(value);
        }
        
        jsonObject.put("xAxis", xAxisArray);
        jsonObject.put("seriesData", seriesDataArray);
    
        return jsonObject.toString();
    
    }
    
    /*
    * 给定服务名、接口名，返回请求耗时-时间分布图，函数内为一些数据统计的工作
    * */
    public static String getStatisticTime_TimeElapsed(String svcStr, String interfaceStr){
        
        ServiceMap svcMap = StatisticDependency.getSrvMap().get(svcStr);
        InterfaceMap interfaceMap = svcMap.interfaceMap.get(interfaceStr);
        
        HashMap<String, Long> xAxisSeriesDataMap = new HashMap<>();
        
        for(Map.Entry<String, LinkedList<Span>> entry: interfaceMap.getSpanListMap().entrySet()){
            LinkedList<Span> spanLinkedList = entry.getValue();
            for(Span span: spanLinkedList){
                long time = span.getStartTime();
                long elapsed = span.getElapsed();
                String timeString = stampToTime(time);
                xAxisSeriesDataMap.put(timeString, elapsed);
            }
        }
        
        JSONObject jsonObject = new JSONObject();
        
        JSONArray xAxisArray = new JSONArray();
        for (String key : xAxisSeriesDataMap.keySet()) {
            xAxisArray.add(key);
        }
        
        JSONArray seriesDataArray = new JSONArray();
        for (long value : xAxisSeriesDataMap.values()) {
            seriesDataArray.add(value);
        }
        
        jsonObject.put("xAxis", xAxisArray);
        jsonObject.put("seriesData", seriesDataArray);
        
        return jsonObject.toString();
        
    }
    
    /*
    * 时间戳转时间
    * */
    public static String stampToTime(long stamp){
        //获得系统的时间，单位为毫秒,转换为妙
        long totalMilliSeconds = stamp;
        long milliSeconds = stamp % 1000;
        long totalSeconds = totalMilliSeconds / 1000;
        
        //求出现在的秒
        long currentSecond = totalSeconds % 60;
        
        //求出现在的分
        long totalMinutes = totalSeconds / 60;
        long currentMinute = totalMinutes % 60;
        
        //求出现在的小时
        long totalHour = totalMinutes / 60;
        long currentHour = totalHour % 24;
        
        //显示时间
        // System.out.println("总毫秒为： " + totalMilliSeconds);
        return (currentHour + ":" + currentMinute + ":" + currentSecond + " " + milliSeconds);
    }
    
    /*
    * 给定spanHeader对象，转换为关键路径对象
    * */
    public static CriticalPathSpan calculateCriticalPath(Span spanHeader){
        // System.out.println(spanHeader);
        CriticalPathSpan criticalPathSpan = new CriticalPathSpan(spanHeader);
        criticalPathSpan.setOriginSpan(spanHeader);
        criticalPathSpan.setElapsed(criticalPathSpan.getElapsed() + spanHeader.getElapsed());
        if (spanHeader.getChildSpan().size() == 0){
            return criticalPathSpan;
        }
        // List<Span> childrenSpanList = new ArrayList<>(spanHeader.getChildSpan());
        // List<Span> childrenSpanList = spanHeader.getChildSpan().stream()    // 去除Null
        //         .filter(Objects::nonNull)
        //         .collect(Collectors.toList());
        // 清空null元素
        log.info("clean start");
        spanHeader.setChildSpan(clearNull(spanHeader.getChildSpan()));
        log.info("clean end");
        List<Span> childrenSpanList = new ArrayList<>(spanHeader.getChildSpan());
        // System.out.println(childrenSpanList);
        // Collections.reverse(childrenSpanList); 此处排序按照开始时间排序，错误
        childrenSpanList.sort((o1, o2) -> Long.compare(o2.getStartTime() + o2.getElapsed(), o1.getStartTime() + o1.getElapsed()));
        CriticalPathSpan lfc = calculateCriticalPath(childrenSpanList.get(0));
        criticalPathSpan.childSpan.add(lfc);
        for(int i = 1; i < childrenSpanList.size(); i++){
            Span child = childrenSpanList.get(i);
            long childEndTime = child.getStartTime() + child.getElapsed();
            long lfcStartTime = lfc.getOriginSpan().getStartTime();
            if (childEndTime < lfcStartTime){
                CriticalPathSpan childCriticalPath = calculateCriticalPath(child);
                criticalPathSpan.childSpan.add(0, childCriticalPath);
                lfc = childCriticalPath;
            }
        }
        return criticalPathSpan;
    }
    
    /*
    * 由于特殊情况下null数据会混入，此处额外清理
    * */
    public static PriorityQueue<Span> clearNull(PriorityQueue<Span> oldPQ){
        PriorityQueue<Span> newPQ = new PriorityQueue<>(new Comparator<Span>() {
            @Override
            public int compare(Span o1, Span o2) {
                if(o1.getStartTime() < o2.getStartTime())
                    return -1;
                if(o1.getStartTime() > o2.getStartTime())
                    return 1;
                return 0;
            }
        });
        
        Iterator<Span> iterator = oldPQ.iterator();
        while(iterator.hasNext()){
            Span spanTmp = iterator.next();
            if(spanTmp != null){
                newPQ.add(spanTmp);
            }
        }
        
        return newPQ;
    }
    
    /*
    * 计算关键路径的耗时
    * 生成新的关键路径对象后，要为它及其子criticalPathSpan设置时间
    * */
    public static void calculateCPTime(CriticalPathSpan criticalPathSpan){
        Queue<CriticalPathSpan> queue = new LinkedList<>();
        queue.add(criticalPathSpan);
        while (!queue.isEmpty()) {
            CriticalPathSpan current = queue.poll();
            if(criticalPathSpan.getChildSpan().size() != 0){
                current.setElapsed(current.getElapsed());   //除法会出现0的情况
                // current.setElapsed(current.getElapsed()/criticalPathSpan.getChildSpan().size());
            }
            for (CriticalPathSpan child : current.childSpan) {
                queue.add(child);
            }
        }
    }
    
    /*
    * 计算关键路径的child个数
    * 生成新的关键路径对象后，要为它及其子criticalPathSpan设置child个数
    * */
    public static void calculateCPChildNum(CriticalPathSpan criticalPathSpan){
        Queue<CriticalPathSpan> queue = new LinkedList<>();
        queue.add(criticalPathSpan);
        while (!queue.isEmpty()) {
            CriticalPathSpan current = queue.poll();
            criticalPathSpan.childNum += 1;
            for (CriticalPathSpan child : current.childSpan) {
                queue.add(child);
            }
        }
    }
    
    /*
    * 处理请求地址中包含的%和-等会变化的内容
    * */
    public static String dfsCalculateCPTraceType(CriticalPathSpan cpSpanHeader){
        StringBuilder type = new StringBuilder();
        String rpc = cpSpanHeader.getRpc();
        String[] rpcList = rpc.split("/");
        String newRpc = "";     // 额外的参数处理
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
        type.append(cpSpanHeader.getAgentId()).append(newRpc).append(" ");
        if (cpSpanHeader.getChildSpan().size() == 0)
            return type.toString();
        for(CriticalPathSpan CPSpan: cpSpanHeader.getChildSpan()){
            type.append(dfsCalculateCPTraceType(CPSpan));
        }
        return type.toString();
        
    }
    
    // 合并关键路径，从根节点开始，对多棵树的同层合并，举例如下：
    /*
    * 合并前：
    * root1 7
    *   rpc11 1
    *   rpc12 2
    *   rpc13 3
    * root2 23
    *   rpc21 4
    *   rpc22 5
    *   rpc23 6
    *   rpc24 7
    * root2 29
    *   rpc31 18
    *       rpc311 8
    *       rpc311 10
    *   rpc32 10
    *
    * 合并后：
    * root1 7
    *   rpc12 2
    *   rpc13 3
    *   rpc11 1
    * root2 52
    *   rpc31 18
    *       rpc311 18
    *   rpc23 6
    *   rpc24 7
    *   rpc21 4
    *   rpc32 10
     *  rpc22 5
    * */
    public static CriticalPathSpan mergeSpans(List<CriticalPathSpan> spans) {
        if (spans == null || spans.isEmpty()) {
            return null;
        }
        // map用来保存每个type对应的span
        Map<String, CriticalPathSpan> typeMap = new HashMap<>();
        // 新建一个结果对象
        CriticalPathSpan result = new CriticalPathSpan();
        
        // 遍历所有的spans
        for (CriticalPathSpan span : spans) {
            // 如果该span的type没有在map中出现过，将其加入map
            // 如果该span的type已经在map中存在，将两个span合并
            CriticalPathSpan existingSpan = typeMap.get(span.type);
            if (existingSpan == null) {
                typeMap.put(span.type, span);
            } else {
                existingSpan.elapsed += span.elapsed;
                existingSpan.childSpan.addAll(span.childSpan);
            }
        }
        
        // 将所有合并后的span加入result的childSpan
        for (CriticalPathSpan span : typeMap.values()) {
            result.childSpan.add(span);
        }
        
        // 递归遍历所有childSpan
        for (CriticalPathSpan span : result.childSpan) {
            CriticalPathSpan tmp = mergeSpans(span.childSpan);
            if (tmp != null)
                span.childSpan = tmp.childSpan;
            else
                span.childSpan = span.childSpan;
        }
        return result;
    }
    
    /*
    * 字符串相似度计算
    * */
    public static double strSimilarity(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m+1][n+1];
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = Math.min(dp[i][j-1], Math.min(dp[i-1][j], dp[i-1][j-1])) + 1;
                }
            }
        }
        return 1.0 / (1.0 + dp[m][n]);
    }
    
    /*
    * 计算字符串相似度
    * 编辑距离误差太大，选用余弦相似度
    * */
    public static double cosineSimilarity(String str1, String str2) {
        Map<String, Integer> vector1 = getWordCount(str1);
        Map<String, Integer> vector2 = getWordCount(str2);
        
        Set<String> words = new HashSet<>();
        words.addAll(vector1.keySet());
        words.addAll(vector2.keySet());
        
        int[] vectorA = new int[words.size()];
        int[] vectorB = new int[words.size()];
        int i = 0;
        for (String word : words) {
            vectorA[i] = vector1.getOrDefault(word, 0);
            vectorB[i] = vector2.getOrDefault(word, 0);
            i++;
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (i = 0; i < words.size(); i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    public static Map<String, Integer> getWordCount(String str) {
        Map<String, Integer> map = new HashMap<>();
        String[] words = str.split("[^a-zA-Z0-9]+");
        for (String word : words) {
            if (word.length() > 0) {
                map.put(word, map.getOrDefault(word, 0) + 1);
            }
        }
        return map;
    }
    
    
    
    
}
