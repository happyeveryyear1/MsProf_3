package com.tcse.microsvcdiagnoser.dependency;


import com.fasterxml.jackson.databind.JsonNode;
import com.tcse.microsvcdiagnoser.entity.Method;
import com.tcse.microsvcdiagnoser.entity.Request;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/*
* Har分析后的记录
* */
public class HarDependency {
    // 全局变量存储偏序
    @Getter @Setter
    private static JsonNode harJSON;
    
    // 验证
    // public static HashSet<String> tokenSet = new HashSet<>();
    // 测试涉及的的token存储
    @Getter @Setter
    private static Set<String> tokenSet = new CopyOnWriteArraySet<>();
    @Getter @Setter
    private static HashMap<String, String> headerAuthKeyType = new HashMap<>();
}
