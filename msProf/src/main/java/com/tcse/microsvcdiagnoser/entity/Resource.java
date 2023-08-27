package com.tcse.microsvcdiagnoser.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
* 全局资源对象，用作资源约束
* */
@Slf4j
@Data
public class Resource {
    
    public String name;
    public JsonNode resource;   // 该资源对应的参数组成
    public Set<Request> postRequest = new HashSet<>();  // 操作该资源的post方法
    public Set<Request> putRequest = new HashSet<>();   // 操作该资源的put方法
    public Set<Request> deleteRequest = new HashSet<>();// 操作该资源的delete方法
    public Set<Request> getRequest = new HashSet<>();   // 操作该资源的get方法
    
    public Resource(String name, JsonNode resource){
        this.name = name;
        this.resource = resource;
    }
    
    public Resource(){
    
    }
    
    public void methodAnalysis(Request request){
        switch (request.method){
            case GET:
                this.getRequest.add(request);
                break;
            case PUT:
                this.putRequest.add(request);
                break;
            case DELETE:
                this.deleteRequest.add(request);
                break;
            case POST:
                this.postRequest.add(request);
                break;
        }
    }
    
    public Resource getInstance(JsonNode resource){
        String name = "";
        int fieldNum = 0;
        Iterator<String> iter  = resource.fieldNames();
        while (iter.hasNext()){
            name = iter.next();
            fieldNum += 1;
        }
        try {
            if (fieldNum > 1){
                throw new Exception();
            }
        }catch (Exception e){
            log.warn("More then one name in Resource: " + resource.toString());
        }
    
        JsonNode resourceBody = resource.get(name);
        return new Resource();
    }
}
