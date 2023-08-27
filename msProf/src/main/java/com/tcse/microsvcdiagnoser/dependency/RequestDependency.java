package com.tcse.microsvcdiagnoser.dependency;

import com.fasterxml.jackson.databind.JsonNode;
import com.tcse.microsvcdiagnoser.entity.Method;
import com.tcse.microsvcdiagnoser.entity.Request;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
* 记录分析Swagger和Har之后的可用的系统接口，用这些接口来生成子代进行测试
* */
public class RequestDependency {
    // 列表存储所有请求
    @Getter @Setter
    private static HashMap<String, HashMap<Method, Request>> requestMap = new HashMap<>();
    @Getter @Setter
    private static String baseURL = "http://133.133.135.182:32677";
}
