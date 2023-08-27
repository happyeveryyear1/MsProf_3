package com.tcse.microsvcdiagnoser.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tcse.microsvcdiagnoser.dependency.HarDependency;
import com.tcse.microsvcdiagnoser.dependency.RequestDependency;
import com.tcse.microsvcdiagnoser.dependency.SwaggerDependency;
import com.tcse.microsvcdiagnoser.entity.Method;
import com.tcse.microsvcdiagnoser.entity.Request;
import com.tcse.microsvcdiagnoser.response.Rets;
import com.tcse.microsvcdiagnoser.service.HarDepService;
import com.tcse.microsvcdiagnoser.service.SwaggerDepService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/*
* swagger依赖管理
* */
@Slf4j
@RestController
@RequestMapping(value="/mvcDiagnoserAnalyzeSwagger", produces = "application/json; charset=utf-8")
public class SwaggerDepController {
    
    @Autowired
    SwaggerDepService swaggerDepService;
    
    
    @GetMapping
    public Object analyzeHar(){
        try {
            swaggerDepService.analyzeSwagger();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        HashMap<String, HashMap<String, LinkedList<String>>> modifiedMap = new HashMap<>();
        for (String key : SwaggerDependency.getResourceOPMap().keySet()) {
            HashMap<Method, LinkedList> methodMap = SwaggerDependency.getResourceOPMap().get(key);
            HashMap<String, LinkedList<String>> modifiedMethodMap = new HashMap<>();
            for (Method method : methodMap.keySet()) {
                LinkedList<String> originalList = methodMap.get(method);
                LinkedList<String> modifiedList = new LinkedList<>();
                for (String url : originalList) {
                    modifiedList.add(url + ":" + method.toString().toLowerCase());
                }
                modifiedMethodMap.put(method.toString(), modifiedList);
            }
            modifiedMap.put(key, modifiedMethodMap);
        }
        
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Method.class, new MethodSerializer());
        mapper.registerModule(module);
        // mapper.addMixIn(Method.class, MethodMixin.class);
        // TODO: add data to resourceOPMap
    
        String resListJson = null;
        try {
            resListJson = mapper.writeValueAsString(modifiedMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    
        resListJson = resListJson.replace("POST", "新增");
        resListJson = resListJson.replace("GET", "查询");
        resListJson = resListJson.replace("DELETE", "删除");
        resListJson = resListJson.replace("PUT", "修改");

        SwaggerDepRet swaggerDepRet = new SwaggerDepRet();
        swaggerDepRet.resList = SwaggerDependency.getDefinitionsJSON().toString();
        swaggerDepRet.resProdConsDep = resListJson;
        
        return Rets.success(swaggerDepRet);
    }
}

@Data
class SwaggerDepRet{
    String resList;
    String resProdConsDep;
}

/*
* 序列化
* */
class MethodSerializer extends JsonSerializer<Method> {
    @Override
    public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        switch (method) {
            case POST:
                jsonGenerator.writeString("新增");
                break;
            case GET:
                jsonGenerator.writeString("查询");
                break;
            case DELETE:
                jsonGenerator.writeString("删除");
                break;
            case PUT:
                jsonGenerator.writeString("修改");
                break;
            case Err:
                jsonGenerator.writeString("Err");
                break;
            default:
                jsonGenerator.writeString("");
        }
    }
}

abstract class MethodMixin {
    public MethodMixin() {}
    
    // 将Method中的枚举值映射为对应的字符串
    @JsonCreator
    public static Method fromString(String value) {
        if (value.equalsIgnoreCase("新增")) {
            return Method.POST;
        } else if (value.equalsIgnoreCase("查询")) {
            return Method.GET;
        } else if (value.equalsIgnoreCase("删除")) {
            return Method.DELETE;
        } else if (value.equalsIgnoreCase("修改")) {
            return Method.PUT;
        } else {
            return Method.Err;
        }
    }
    
    @JsonValue
    public abstract String toString();
}

