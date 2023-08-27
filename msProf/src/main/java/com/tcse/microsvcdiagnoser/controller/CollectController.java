package com.tcse.microsvcdiagnoser.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.BiMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashBiMap;
import com.tcse.microsvcdiagnoser.context.Header;
import com.tcse.microsvcdiagnoser.dependency.*;
import com.tcse.microsvcdiagnoser.dto.AnnotationBo;
import com.tcse.microsvcdiagnoser.dto.SpanBo;
import com.tcse.microsvcdiagnoser.dto.SpanChunkBo;
import com.tcse.microsvcdiagnoser.entity.Span;
import com.tcse.microsvcdiagnoser.response.Rets;
import com.tcse.microsvcdiagnoser.service.*;
import com.tcse.microsvcdiagnoser.util.CommonArgs;
import com.tcse.microsvcdiagnoser.util.CommonUtils;
// import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tcse.microsvcdiagnoser.util.CommonArgs.testSetSize;
import static java.util.Arrays.*;


@Slf4j
@RestController
@RequestMapping(value = "/collect", produces = "application/json; charset=utf-8")
public class CollectController {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final File fileSpan = new File("span.json");
    private final File fileSpanChunk = new File("spanChunk.json");
    private final FileWriter fileSpanWritter = new FileWriter(fileSpan.getName(), true);
    private final FileWriter fileSpanChunkWritter = new FileWriter(fileSpanChunk.getName(), true);

    
    
    @Autowired
    private CollectService collectService;
    
    @Autowired
    private TraceService traceService;
    
    @Autowired
    private SpanService spanService;
    
    @Autowired
    private DiagnoseService diagnoseService;
    
    @Autowired
    private TestExecutorService testExecutorService;
    
    @Autowired
    private AnomalyDetectionService anomalyDetectionService;
    

    public CollectController() throws IOException {
    }
    
    @PostMapping("/GA")
    public Object execGA() throws IOException {
        testExecutorService.execute();
        return Rets.success("成功");
    }
    
    /*
    * 收集调用链接口
    * */
    @PostMapping("/span")
    public Object collectSpan(@RequestBody String data) throws JsonProcessingException {
        // log.info("Received Span Data :  " + data);
        // 判断是否在性能分析中
        if(ExecedDataDependency.getExecState() == 0){
            SpanBo spanBo = objectMapper.readValue(data, SpanBo.class);
            boolean isMsProfData = false;
            // 判断是否有指定header
            for (AnnotationBo annotationBo : spanBo.getAnnotationBoList()) {
                if (annotationBo.getKey()==Header.TOOL_TYPE && annotationBo.getValue()!=null && annotationBo.getValue().equals("MsProf")) {
                    isMsProfData = true;
                    break;
                }
            }
            if(! isMsProfData){
                return Rets.failure("非MsProf信息");
            }
            // 文本记录
            try {
                this.fileSpanWritter.write(data);
                this.fileSpanWritter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            log.info("Received Data {} {} {}", spanBo.getAgentId(), spanBo.getRpc(), spanBo.getElapsed());
            
            // 处理信息
            collectService.addSpan(spanBo);
    
        }
        
        return Rets.success("成功");
    }
    
    /*
    * spanChunk调用链收集接口
    * 目前pinpoint不会使用这种数据结构
    * */
    @PostMapping("/spanChunk")
    public Object collectSpanChunk(@RequestBody String data) throws JsonProcessingException {
        log.info("Received SpanChunk Data :  " + data);
        try {
            this.fileSpanChunkWritter.write(data);
            this.fileSpanChunkWritter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SpanChunkBo spanChunkBo = objectMapper.readValue(data, SpanChunkBo.class);
        collectService.addSpanChunk(spanChunkBo);
        
        return Rets.success("成功");
    }
    
    /*
    * 异常检测
    * */
    @GetMapping("/anomalyDetection")
    public Object anomalyDetection() {
        anomalyDetectionService.flushData();
        return Rets.success();
    }
    
    /*
    * 前端获取当前测试用例，滚动展示
    * */
    @GetMapping("/getTests")
    public Object getTests() {
        // 设置测试文件目录
        String testBaseDir = CommonArgs.testBaseDir;
    
        // 获取目录下所有以.test结尾的文件
        File[] testFiles = new File(testBaseDir)
                .listFiles((dir, name) -> name.endsWith(".test"));
    
        // 对文件名按字母序排序
        sort(testFiles, Comparator.comparing(File::getName));
    
        Pattern pattern = Pattern.compile("\\d+");
    
        // 拼接文件内容并以换行符分隔
        StringBuilder result = new StringBuilder();
        try{
            for (File file : testFiles) {
                String name = file.getName();
                Matcher matcher1 = pattern.matcher(name);
                String[] ids = new String[2];
                int idx = 0;
                while (matcher1.find()) {
                    ids[idx] = matcher1.group();
                    idx += 1;
                }
    
                result.append("第" + ids[0] + "代 " + "第" + ids[1] + "个测试套件");
                String line = null;
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (true) {
                    
                        if (!((line = reader.readLine()) != null)) break;
                   
                    result.append(line).append("\n");
                }
                result.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n"); // 每个文件之间间隔一行
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 返回拼接结果
        return Rets.success(result.toString());
    }
    
    /*
    * 记录token变化
    * */
    @PostMapping("/tockenRecord")
    public Object tockenRecord(@RequestBody String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode tmp = null;
        try {
            tmp = objectMapper.readTree(data);
            String token = tmp.get("data").textValue();
            HarDependency.getTokenSet().add(token);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return Rets.success();
    }
    
    
    @Deprecated
    @PostMapping("/diagnoseMock")
    public Object performanceDiagnoseMock() throws IOException {
        LinkedList<String> ret = new LinkedList<>();
        ret.add("ts-admin-user-service: 8081");
        ret.add("ts-admin-route-service: 8081");
        ret.add("ts-consign-service: 8081");
        ret.add("ts-route-plan-service: 8081");
        ret.add("ts-travel-plan-service: 8081");
        ret.add("ts-order-service: 8081");
        ret.add("ts-admin-user-service: 8081");
        ret.add("ts-admin-route-service: 8081");
        ret.add("ts-consign-service: 8081");
        ret.add("ts-route-plan-service: 8081");
        ret.add("ts-travel-plan-service: 8081");
        ret.add("ts-order-service: 8081");
        return Rets.success(ret);
    }
    @PostMapping("/traceData")
    public Object getTraceData(@RequestBody String req) throws JsonProcessingException {
        List<HashMap<String, String>> childrenDataMap = new LinkedList<>();
        // HashMap<String, String> tokenMap= objectMapper.readValue(req,  new TypeReference<HashMap<String, String>>(){});
        String my_token = req.substring(9);
        spanService.calSpanSelfTime();
        log.info("[*] getTraceData my_token:  " + my_token);
        String traceString = traceService.getTraceString(my_token);
        return Rets.success(traceString);
    }
    
    @Deprecated
    @GetMapping("/changeVersion")
    public Object changeVersion() {
        // CollectService.traceTypeMap.clear();
        // CollectService.traceSet.clear();
        // CollectService.traceBoSet.clear();
        // CollectService.spanBoMap.clear();
        // CollectService.spanMap.clear();
        // CollectService.noParentSpanSet.clear();
        // CollectService.shortTypeMap.clear();
        return Rets.success();
    }
    
    @Deprecated
    @PostMapping("/finish")
    public Object finish(@RequestBody String task) {
        // clear mem data
        
        // CollectService.sortSpan();
        // log.info(String.valueOf(CollectService.testSpanIdTraceMap));
        // CollectService.writeFile();
        return Rets.success();
    }
    

}

