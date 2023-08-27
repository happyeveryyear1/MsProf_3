package com.tcse.microsvcdiagnoser.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tcse.microsvcdiagnoser.dependency.HarDependency;
import com.tcse.microsvcdiagnoser.dependency.RequestDependency;
import com.tcse.microsvcdiagnoser.entity.Method;
import com.tcse.microsvcdiagnoser.entity.Request;
import com.tcse.microsvcdiagnoser.response.Rets;
import com.tcse.microsvcdiagnoser.service.HarDepService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
* Har依赖管理controller
* */
@Slf4j
@RestController
@RequestMapping(value="/mvcDiagnoserAnalyzeHar", produces = "application/json; charset=utf-8")
public class HarDepController {
    
    @Autowired HarDepService harDepService;
    
    /*
    * 解析Har依赖（前端调用）
    * */
    @PostMapping
    public Object analyzeHar(@RequestBody String baseUrl){
        // 设置被测系统baseUrl
        RequestDependency.setBaseURL(baseUrl);
        // 解析依赖
        harDepService.analyzeHarDir();
        
        // 组装返回数据
        HarDepRet harDepRet = new HarDepRet();
        harDepRet.harJson= HarDependency.getHarJSON().toString();
    
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new SimpleModule().addSerializer(Request.class, new RequestSerializer()));
        String json = "";
        try {
            json = mapper.writeValueAsString(RequestDependency.getRequestMap());
        } catch (JsonProcessingException e) {
            log.info("RequestDependency.requestMap转json出错，RequestDependency.requestMap：{}", RequestDependency.getRequestMap());
            e.printStackTrace();
        }
        log.debug("HarDependency");
        log.debug("{}", json);
        harDepRet.requestMap = json;
        return Rets.success(harDepRet);
    }
}

/*
* 序列化
* */
@Data
class HarDepRet{
    String harJson;
    String requestMap;
}

/*
 * 序列化
 * */
abstract class RequestMixIn {
    @JsonIgnore boolean Bearer;
    @JsonIgnore boolean BearerFlag;
    @JsonIgnore boolean argInUrl;
    @JsonIgnore int cvVariation;
    @JsonIgnore JsonNode arguments;
    @JsonIgnore JsonNode responses;
}

/*
 * 序列化
 * */
class RequestSerializer extends JsonSerializer<Request> {
    @Override
    public void serialize(Request request, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("path", request.getPath());
        jsonGenerator.writeStringField("method", request.getMethod().toString());
        jsonGenerator.writeStringField("tags", request.getTags());
        // 处理argSource属性
        jsonGenerator.writeArrayFieldStart("argSource");
        for (HashMap<String, String> argSrc : request.argSource) {
            jsonGenerator.writeStartObject();
            for (Map.Entry<String, String> entry : argSrc.entrySet()) {
                String value = entry.getValue();
                // 将value按照一定规则进行转换
                String[] values = value.split(":");
                String[] urlValues = values[0].split("-");
                String url = String.join("-", Arrays.copyOfRange(urlValues, 0, urlValues.length - 2));
                String position = urlValues[urlValues.length - 2];
                String method = urlValues[urlValues.length - 1];
                String argSrcValue = values[1].replace("[", "").replace("]", "").replace("<", "[").replace(">", "]").replace(", ", "");
                jsonGenerator.writeObjectField(entry.getKey(), new ArgSrc(url, position, method, argSrcValue));
            }
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}

/*
 * 序列化
 * */
class ArgSrc {
    private final String url;
    private final String position;
    private final String method;
    private final String argSrc;
    
    public ArgSrc(String url, String position, String method, String argSrc) {
        this.url = url;
        this.position = position;
        this.method = method;
        this.argSrc = argSrc;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getPosition() {
        return position;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getArgSrc() {
        return argSrc;
    }
}
