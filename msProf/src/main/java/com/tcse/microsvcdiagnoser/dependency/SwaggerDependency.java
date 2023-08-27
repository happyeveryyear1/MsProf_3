package com.tcse.microsvcdiagnoser.dependency;

import com.fasterxml.jackson.databind.JsonNode;
import com.tcse.microsvcdiagnoser.entity.Method;
import com.tcse.microsvcdiagnoser.entity.Resource;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/*
* Swagger解析后的依赖
* */
public class SwaggerDependency {
    // 全局变量存储swagger
    @Getter
    @Setter
    private static JsonNode swaggerJSON;
    
    @Getter @Setter
    private static JsonNode definitionsJSON;
    // 全局变量存储依赖
    
    // 全局变量存储资源列表，单例资源
    @Getter @Setter
    private static Set<Resource> resourceSet = new HashSet<>();
    // Source资源操作
    @Getter @Setter
    private static HashMap<String, HashMap<Method, LinkedList>> resourceOPMap = new HashMap<>();
    // Source列表
    @Getter @Setter
    private static HashMap<String, JsonNode> resourceMap = new HashMap<>();
    
    // 全局变量存储
}
