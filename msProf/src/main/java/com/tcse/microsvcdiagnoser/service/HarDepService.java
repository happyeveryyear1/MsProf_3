package com.tcse.microsvcdiagnoser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tcse.microsvcdiagnoser.dependency.HarDependency;
import com.tcse.microsvcdiagnoser.dependency.RequestDependency;
import com.tcse.microsvcdiagnoser.dependency.TestSetDependency;
import com.tcse.microsvcdiagnoser.dto.RequestDto;
import com.tcse.microsvcdiagnoser.entity.Method;
import com.tcse.microsvcdiagnoser.entity.Request;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.tcse.microsvcdiagnoser.util.CommonArgs.harDir;
import static com.tcse.microsvcdiagnoser.util.CommonUtils.getMethod;
import static com.tcse.microsvcdiagnoser.util.CommonUtils.getSubStrNum;

/*
* Har依赖分析
* */
@Log4j2
@Service
public class HarDepService {
    
    /* JSON反序列化 */
    ObjectMapper commonMapper = new ObjectMapper();
    
    // 解析har文件
    public void analyzeHarDir() {
        String dirPath = harDir;
        File file = new File(dirPath);
        File[] fs = file.listFiles();
        ObjectMapper mapper = new ObjectMapper();
        // 分析每一个文件
        for(File f:fs){
            if(!f.isDirectory() && f.getName().endsWith(".har")) {
                String content = null;
                try {
                    content = FileUtils.readFileToString(f, "UTF-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 字符串转Json
                JsonNode harSrcJSON = null;
                try {
                    harSrcJSON = mapper.readTree(content);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                // 清理无用数据
                JsonNode harJSON = clearHarJson(harSrcJSON);
                // 记录依赖原始信息
                HarDependency.setHarJSON(harJSON);
                // 分析依赖
                analyzeHarJson(harJSON);
                
            }
        }
    }
    
    /*
    * 清除har中的js、css、png等信息
    * 清除har中的图片信息
    * 清除har中的HTML和文本信息
    * */
    public JsonNode clearHarJson(JsonNode harJson){
        JsonNode entriesJson = harJson.get("log").get("entries");
        
        Iterator<JsonNode> nodes = entriesJson.elements();
        while(nodes.hasNext()) {
            JsonNode nodeTmp = nodes.next();
            String url = nodeTmp.get("request").get("url").textValue();
            if(url.endsWith(".js") || url.endsWith(".css") || url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".html")|| url.contains("googleapis") || url.contains("accounts.google")|| url.contains("fontawesome") || url.contains(".com") || url.endsWith("/")){
                nodes.remove();
            } else if(nodeTmp.get("response")!=null && nodeTmp.get("response").get("content")!=null && nodeTmp.get("response").get("content").get("encoding")!=null && nodeTmp.get("response").get("content").get("encoding").textValue().endsWith("base64")){
                nodes.remove();
            }else if(nodeTmp.get("response")!=null && nodeTmp.get("response").get("content")!=null && nodeTmp.get("response").get("content").get("mimeType")!=null && nodeTmp.get("response").get("content").get("mimeType").textValue().endsWith("text/html")){
                nodes.remove();
            }
            
        }
        
        return entriesJson;
    }
    
    /*
    * 分析Har
    * */
    public void analyzeHarJson(JsonNode harJSON){
        // 初始化测试用例，即初代种群
        LinkedList<LinkedList<RequestDto>> initialTestList = TestSetDependency.getInitialTestList();
        LinkedList<RequestDto> requestDtoList = new LinkedList<>();
        initialTestList.add(requestDtoList);
        
        String baseUrl = RequestDependency.getBaseURL();
        int idx = 0;
        // 处理每一个JSON节点，即每一个request
        for(JsonNode requestEntityJson : harJSON){
            // request的请求和响应都需要分析。
            JsonNode requestJson = requestEntityJson.get("request");
            JsonNode responseJson = requestEntityJson.get("response");
            String url = requestJson.get("url").textValue();
            String API;
            if (url.equals(baseUrl))
                API = "";
            else
                API = url.substring(baseUrl.length());
            
            // 判断请求是否已经出现过（历史有过相同地址的请求）
            if (RequestDependency.getRequestMap().containsKey(API)){
                // 请求属于map（请求出现过）
                Method method = getMethod(requestJson.get("method").textValue());
                Request request = RequestDependency.getRequestMap().get(API).get(method);
                
                // arg
                JsonNode arg = getArg(requestJson);
                request.argHistory.add(arg);
                this.identifyAuthorKey(requestJson);
                
                // res
                JsonNode res = getRes(responseJson);
                request.resHistory.add(res);
                this.recordToken(res);
                
                // 分析来源
                HashMap<String, String> tmpArgsSource = analyzeArgsSource(arg, request, idx, requestEntityJson, harJSON);
                if(tmpArgsSource!=null && !tmpArgsSource.isEmpty()){
                    request.argSource.add(tmpArgsSource);
                }
                
                // 初始化测试初始种群
                requestDtoList.add(new RequestDto(API, method, arg.deepCopy(), res.deepCopy()));
                
            }else{
                // map中无法查询到请求（请求没有出现过）
                String requestJsonURL = API;
                // 尝试匹配内部含有参数的API
                // 例如 /api/v1/adminbasicservice/adminbasic/configs/{name}
                // 由于name字段的不同，会有不同的字符串，但应当属于同一请求。
                Request request = matchAPIwithArgIn(requestJsonURL);
                if(request != null){
                // 能匹配内部含有参数的API
                    // req
                    ObjectNode arg = new ObjectMapper().createObjectNode();
                    arg.put("argInPath", requestJsonURL);
                    request.argHistory.add(arg);
                    // res
                    JsonNode res = getRes(responseJson);
                    this.recordToken(res);
                    request.resHistory.add(res);
                    // 构建arg jsonNode
                    ObjectNode argJson = new ObjectMapper().createObjectNode();
                    String requestAPI = request.getPath();
                    String[] requestAPIArray = requestAPI.split("/");
                    String[] requestJsonURLArray = requestJsonURL.split("/");
                    for(int i = 0; i < requestAPIArray.length; i++){
                        requestJsonURLArray[i] = requestJsonURLArray[i].replace("%20", " ");
                        
                        if (!requestAPIArray[i].equals(requestJsonURLArray[i]))
                            argJson.put(requestAPIArray[i], requestJsonURLArray[i]);
                    }
                    // 来源分析
                    HashMap<String, String> tmpArgsSource = analyzeArgsSource(argJson, request, idx, requestEntityJson, harJSON);
                    if(tmpArgsSource!=null && !tmpArgsSource.isEmpty()){
                        request.argSource.add(tmpArgsSource);
                    }
                    
                    // 初始化测试初始种群
                    Method method = getMethod(requestJson.get("method").textValue());
                    requestDtoList.add(new RequestDto(request.getPath(), method, argJson.deepCopy(), res.deepCopy(), true));
    
                }else{
                // 不能匹配到内部含有参数的API，则新建请求
                    String methodStr = requestJson.get("method").textValue();
                    Method method = getMethod(methodStr);
                    String tags = "";
                    JsonNode argJson = new ObjectMapper().createObjectNode();
                    JsonNode resJson = new ObjectMapper().createObjectNode();
                    Request newRequest = new Request(API, method, tags, argJson, resJson);
                    HashMap<Method, Request> methodMap = RequestDependency.getRequestMap().get(API);
                    if (methodMap == null){
                        methodMap = new HashMap<>();
                        RequestDependency.getRequestMap().put(API, methodMap);
                    }
                    methodMap.put(method, newRequest);
    
                    // arg
                    JsonNode arg = getArg(requestJson);
                    newRequest.argHistory.add(arg);
                    this.identifyAuthorKey(requestJson);
    
                    // res
                    JsonNode res = getRes(responseJson);
                    newRequest.resHistory.add(res);
                    this.recordToken(res);
                    
                    // 不在swagger中标志
                    newRequest.setUrlInSwagger(false);
    
                    // System.out.println(request);
                    // 分析来源
                    HashMap<String, String> tmpArgsSource = analyzeArgsSource(arg, newRequest, idx, requestEntityJson, harJSON);
                    if(tmpArgsSource!=null && !tmpArgsSource.isEmpty()){
                        newRequest.argSource.add(tmpArgsSource);
                    }
    
                    // 初始化测试初始种群
                    requestDtoList.add(new RequestDto(API, method, arg.deepCopy(), res.deepCopy()));
                }
                
            }
            idx += 1;
        }
        // System.out.println(RequestDependency.requestMap);
        
    }
    
    /*
    * 提取参数argument部分
    * */
    public JsonNode getArg(JsonNode requestJson){
        JsonNode arg = requestJson.get("postData");
        if (arg != null){
            try {
                arg = commonMapper.readTree(arg.get("text").textValue());
            } catch (JsonProcessingException e) {
                log.error("convert to jsonNode error, test: {}", arg.get("text"));
            }
        }
        if (arg == null)
            arg = new ObjectMapper().createObjectNode();
        return arg;
    }
    
    /*
    * 提取响应response部分
    * */
    public JsonNode getRes(JsonNode requestJson){
        JsonNode res = requestJson.get("content");
        if (res != null && res.get("text") != null){
            // 排除验证码、图片等以字符串返回
            if (! res.get("text").textValue().startsWith("/")) {
                // 排除html的情况
                if (!(res.get("mimeType") != null && (!res.get("mimeType").toString().contains("json")))) {
                    try {
                        res = commonMapper.readTree(res.get("text").textValue());
                    } catch (JsonProcessingException e) {
                        log.error("convert to jsonNode error, test: {}", res.get("text"));
                    }
                } else {
                    // 字符串数据 暂时无用，丢弃
                    // TODO 处理字符串数据
                }
            }
        }
        if (res == null)
            res = new ObjectMapper().createObjectNode();
        return res;
    }
    
    /*
    *  提取请求header
    * */
    public JsonNode getReqHeader(JsonNode requestJson){
        JsonNode arg = requestJson.get("headers");
        if (arg != null){
            try {
                arg = commonMapper.readTree(arg.get("text").textValue());
            } catch (JsonProcessingException e) {
                log.error("convert to jsonNode error, test: {}", arg.get("text"));
            }
        }
        if (arg == null)
            arg = new ObjectMapper().createObjectNode();
        return arg;
    }
    
    /*
    * 记录出现过的token。当生成测试时，也应带有token
    * */
    public void recordToken(JsonNode responseJson){
        if(responseJson.get("data") != null){
            if(responseJson.get("data").get("token") != null) {
                HarDependency.getTokenSet().add(responseJson.get("data").get("token").textValue());
            }
        }
    }
    
    /*
    * 由于不同的token有不同的关键词，这里额外处理Bearer类型的token
    * 其他的token如果遇到则额外处理
    * */
    public void identifyAuthorKey(JsonNode requestJson){
        if(requestJson.get("headers") != null) {
            JsonNode headerJson = requestJson.get("headers");
            String authorizationValue = null;
            for (JsonNode headerNode : headerJson) {
                String name = headerNode.get("name").asText();
                if (name.equals("Authorization")) {
                    authorizationValue = headerNode.get("value").asText();
                    break;
                }
            }
            if (authorizationValue != null) {
                if (authorizationValue.startsWith("Bearer ")) {
                    HarDependency.getHeaderAuthKeyType().put("Authorization", "Bearer ");
                }
            }
        }
    }
    
    /*
    * 判断当前请求地址，是否和记录中的API匹配。即aaa/bbb/cc/{arg}的泛化
    * */
    public Request matchAPIwithArgIn(String url){
        HashMap<String, HashMap<Method, Request>> requestMap = RequestDependency.getRequestMap();
        Request result = null;
        int value = 0;
        for(Map.Entry<String, HashMap<Method, Request>> requestMapEntry: requestMap.entrySet()){
            for(Map.Entry<Method, Request> requestEntry: requestMapEntry.getValue().entrySet()){
                Request requestTmp = requestEntry.getValue();
                String APITmp = requestTmp.getPath();
                boolean isArgInUrl = requestTmp.isArgInUrl();
                if (!isArgInUrl)
                    continue;
                int valueTmp = evaluateSimilarity(APITmp, url);
                if (valueTmp > value){
                    value = valueTmp;
                    result = requestTmp;
                }
            }
        }
        return result;
    }
    
    /*
    * 评估字符串相似度
    * */
    public int evaluateSimilarity(String API, String url){
        int value = 0;
        if (getSubStrNum(API, "/") != getSubStrNum(url, "/"))
            return value;
        String[] apiArray = API.split("/");
        String[] urlArray = url.split("/");
        int len = apiArray.length;
        for(int i = 0; i< len; i++){
            if(apiArray[i].startsWith("{") && apiArray[i].endsWith("}"))
                value += 1;
            if(urlArray[i].equals(apiArray[i]))
                value += 1;
            if(! urlArray[i].equals(apiArray[i]))
                return value;
        }
        return value;
    }
    
    /*
    * 分析参数来源
    * 在历史请求中查询当前参数是否出现过，先检查历史请求的返回值，再检查历史请求的参数，最后使用字符串记录参数来源
    * */
    public HashMap<String, String> analyzeArgsSource(JsonNode arg, Request request, int idx, JsonNode requestEntityJson, JsonNode harJSON){
        // 建立新的来源
        HashMap<String, String> tmpArgsSource = new HashMap<>();
        // System.out.println(arg);
        arg.fields().forEachRemaining(argItem ->{
            String key = argItem.getKey();
            String value = argItem.getValue().textValue();
            int i = idx;
            // 倒着遍历之前的请求，寻找参数来源
            
            // 查询返回值
            while(i-1 >= 0 && i-1 >= idx-10){
                i -= 1;
                JsonNode i_requestEntityJson = harJSON.get(i);
                JsonNode i_responseJson = i_requestEntityJson.get("response");
                JsonNode i_requestJson = i_requestEntityJson.get("request");
                
                // 检查返回值
                JsonNode i_res = i_responseJson.get("content");
                if (!checkJsonNodeTextLegal(i_res))
                    continue;
                
                try {
                    i_res = commonMapper.readTree(i_res.get("text").textValue());
                } catch (JsonProcessingException e) {
                    log.error("convert to jsonNode error, test: {}", i_res.get("text"));
                }
                
                LinkedList<String> pathList = new LinkedList<>();
                boolean isSearched = getPath(i_res, value, key, pathList);
                if(isSearched){
                    Method i_method = getMethod(i_requestJson.get("method").textValue());
                    String i_API = i_requestJson.get("url").textValue().substring(RequestDependency.getBaseURL().length());
                    tmpArgsSource.put(key, i_API + "-res"  + "-" + i_method.toString() + ":" + pathList.toString());
                }else{
                    // TODO 无来源
                }
            }
            
            // 查询请求参数
            if (tmpArgsSource.containsKey(key) == false){
                i = idx;
                while(i-1 >= 0 && i-1 >= idx-10){
                    i -= 1;
                    JsonNode i_requestEntityJson = harJSON.get(i);
                    JsonNode i_requestJson = i_requestEntityJson.get("request");
        
                    // 检查请求
                    JsonNode i_req = i_requestJson.get("content");
                    if (!checkJsonNodeTextLegal(i_req))
                        continue;
                    try {
                        i_req = commonMapper.readTree(i_req.get("text").textValue());
                    } catch (JsonProcessingException e) {
                        log.error("convert to jsonNode error, test: {}", i_req.get("text"));
                    }
                    
                    LinkedList<String> pathList = new LinkedList<>();
                    boolean isSearched = getPath(i_req, value, key, pathList);
                    if(isSearched){
                        Method i_method = getMethod(i_requestJson.get("method").textValue());
                        String i_API = i_requestJson.get("url").textValue().substring(RequestDependency.getBaseURL().length());
                        tmpArgsSource.put(key, i_API + "-req"  + "-" + i_method.toString() + ":" + pathList.toString());
                    }else{
                        // TODO 无来源
                    }
                    i -= 1;
                }
            }
            
        });
        // 和旧的来源比较
        // TODO HEADER来源
        
        return tmpArgsSource;
    }
    
    /*
    * 排除一些额外情况
    * */
    public boolean checkJsonNodeTextLegal(JsonNode jsonNode){
        return jsonNode != null && jsonNode.get("text") != null && !jsonNode.get("text").textValue().startsWith("<!doctype html>") && (!jsonNode.get("text").textValue().startsWith("/"));
    }
    
    /*
    * 检查JsonNode中是否存在想要的参数
    * 由于JsonNode(请求参数）存在多层的嵌套关系，因此需要递归分析
    * */
    public boolean getPath(JsonNode response, String target_value, String target_key, LinkedList<String> path){
        if (response.isValueNode()){
            if(response.toString().equals(target_value) || (response.textValue() != null && response.textValue().equals(target_value))){
                String last = path.removeLast();
                last = last.substring(0, last.length() - 1);
                path.add(last);
                return true;
            }
        }
        if (response.isArray()){
            int idx = 0;
            for (JsonNode jsonNode: response){
                path.add("<" + idx + ">-");
                boolean ifSearched = getPath(jsonNode, target_value, target_key, path);
                if(ifSearched)
                    return true;
                else
                    path.removeLast();
                idx += 1;
            }
        }
        Iterator<Map.Entry<String, JsonNode>> ite  = response.fields();
        Map.Entry<String, JsonNode> jsonItem;
        while (ite.hasNext()){
            jsonItem = ite.next();
            path.add(jsonItem.getKey() + "-");
            boolean ifSearched =  getPath(jsonItem.getValue(), target_value, target_key, path);
            if(ifSearched)
                return true;
            else
                path.removeLast();
            
        }
        return false;
    }
    
}

