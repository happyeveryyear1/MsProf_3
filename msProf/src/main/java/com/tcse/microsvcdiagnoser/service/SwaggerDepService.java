package com.tcse.microsvcdiagnoser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcse.microsvcdiagnoser.dependency.RequestDependency;
import com.tcse.microsvcdiagnoser.dependency.SwaggerDependency;
import com.tcse.microsvcdiagnoser.entity.Method;
import com.tcse.microsvcdiagnoser.entity.Request;
import com.tcse.microsvcdiagnoser.util.CommonArgs;
import com.tcse.microsvcdiagnoser.util.CommonUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/*
* Swagger分析
* */
@Service
public class SwaggerDepService {
    
    
    /*
    * 解析swagger文档
    * 主要解析swagger中的请求和对应的definitions
    * 通过definitions、path、tags等关键字解析
    * */
    public void analyzeSwagger() throws IOException{
        File file=new File(CommonArgs.APIFile);
        String content= FileUtils.readFileToString(file,"UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode swaggerJSON = mapper.readTree(content);
        
        SwaggerDependency.setSwaggerJSON(swaggerJSON);
        SwaggerDependency.setDefinitionsJSON(swaggerJSON.get("definitions"));
        JsonNode pathsJson = swaggerJSON.get("paths");
        JsonNode definitionsJson = swaggerJSON.get("definitions");
        
        // definitions记录
        definitionsJson.fields().forEachRemaining(definitionJson -> {
            String name = definitionJson.getKey();
            SwaggerDependency.getResourceMap().put(name, definitionsJson);
        });
        
        // 分析每一个path
        pathsJson.fields().forEachRemaining(pathJson ->{
                    String API = pathJson.getKey();
                    JsonNode methodsJson = pathJson.getValue();
                    methodsJson.fields().forEachRemaining(methodJson -> {
                        String methodStr = methodJson.getKey();
                        Method method = getMethod(methodStr);
                        String tags = methodJson.getValue().get("tags").toString();
                        JsonNode argJson = methodJson.getValue().get("parameters");
                        JsonNode resJson = methodJson.getValue().get("responses");
                        Request request = new Request(API, method, tags, argJson, resJson);
                        // 参数在url中，即aaa/bb/{name}
                        if (CommonUtils.isArgInUrl(API))
                            request.setArgInUrl(true);
                        // RequestDependency
                        HashMap<Method, Request> methodMap = RequestDependency.getRequestMap().get(API);
                        if (methodMap == null){
                            methodMap = new HashMap<>();
                            RequestDependency.getRequestMap().put(API, methodMap);
                        }
                        methodMap.put(method, request);
                        
                        // 依赖操作记录
                        if (argJson != null && argJson.get(0).get("schema")!=null){
                            argJson = argJson.get(0);
                            String ref =  argJson.get("schema").get("$ref").textValue();
                            String[] definitionList = ref.split("/");
                            String definition = definitionList[definitionList.length-1];
                            HashMap<Method, LinkedList> resourceOP = SwaggerDependency.getResourceOPMap().get(definition);
                            if (resourceOP == null){
                                resourceOP = new HashMap<>();
                                SwaggerDependency.getResourceOPMap().put(definition, resourceOP);
                            }
                            LinkedList<String> APIList = resourceOP.get(method);
                            if (APIList == null){
                                APIList = new LinkedList<>();
                                resourceOP.put(method, APIList);
                            }
                            APIList.add(API);
                        }
                        
                    });
                }
        );

        
        // TestExecutorService testExecutorService = new TestExecutorService();
        // testExecutorService.execute();
        
        
    }
    
    public void analyzeSwaggerSource(Request request){
    
    }
    
    /*
    * getMethod
    * */
    public Method getMethod(String method){
        if(method.equals("get"))
            return Method.GET;
        if(method.equals("put"))
            return Method.PUT;
        if(method.equals("post"))
            return Method.POST;
        else
            return Method.DELETE;
    }
}


/*

        // System.out.println(content);
        JSONObject swaggerJSON= JSON.parseObject(content);
        // System.out.println((swaggerJSON));
        SwaggerDependency.swaggerJSON = swaggerJSON;
        SwaggerDependency.definitionsJSON = JSON.parseObject(swaggerJSON.get("definitions").toString());

        String  a = "{        \"RouteInfo\": {\n" +
        "            \"type\": \"object\",\n" +
        "            \"properties\": {\n" +
        "                \"distanceList\": {\n" +
        "                    \"type\": \"string\"\n" +
        "                },\n" +
        "                \"endStation\": {\n" +
        "                    \"type\": \"string\"\n" +
        "                },\n" +
        "                \"id\": {\n" +
        "                    \"type\": \"string\"\n" +
        "                },\n" +
        "                \"loginId\": {\n" +
        "                    \"type\": \"string\"\n" +
        "                },\n" +
        "                \"startStation\": {\n" +
        "                    \"type\": \"string\"\n" +
        "                },\n" +
        "                \"stationList\": {\n" +
        "                    \"type\": \"string\"\n" +
        "                }\n" +
        "            }\n" +
        "        }}";
        JsonNode tmp = mapper.readTree(a);
        System.out.println(tmp);
        Iterator<String> ite  = tmp.fieldNames();
        while (ite.hasNext()){
            System.out.println(ite.next());
        }
 */
