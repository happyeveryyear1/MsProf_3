package com.tcse.microsvcdiagnoser.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.*;

/*
* request对象，在异常算法中使用
* */
@Data
public class Request {

    String path;
    Method method;
    String tags;
    JsonNode arguments;
    JsonNode responses;
    // 特定header
    boolean Bearer = false;
    boolean BearerFlag = false;
    
    // 参数在url内
    boolean argInUrl = false;
    
    // url在文档中
    boolean urlInSwagger = true;
    
    // parHistory
    public LinkedList<JsonNode> argHistory = new LinkedList<>();
    // resHistory
    public LinkedList<JsonNode> resHistory = new LinkedList<>();
    
    // 偏序
    public LinkedList<HashMap<String, String>> argSource = new LinkedList<>();
    
    // cv变化
    public int cvVariation = 0;

    

    
    public Request(String path, Method method, String tags, JsonNode arguments, JsonNode response){
        this.path = path;
        this.method = method;
        this.tags = tags;
        this.arguments = arguments;
        this.responses = response;
    }
    

}

