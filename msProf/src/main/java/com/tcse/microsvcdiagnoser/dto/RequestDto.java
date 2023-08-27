package com.tcse.microsvcdiagnoser.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.tcse.microsvcdiagnoser.dependency.HarDependency;
import com.tcse.microsvcdiagnoser.entity.Method;
import lombok.Data;

import java.util.LinkedList;
import java.util.Random;

/*
* Request对象
* 测试生成时，每个请求即为一个对象。
* */
@Data
public class RequestDto implements Cloneable{
    public String path;             // 请求地址
    public Method method;           // 请求方法
    public JsonNode arguments;      // 请求参数
    public JsonNode responses;      // 响应
    // 参数在url内 例如：/api/v1/adminbasicservice/adminbasic/configs/{name}，name即为请求地址内参数
    public boolean argInUrl = false;
    
    // 该request涉及的被测系统的调用服务
    public LinkedList<String> serviceInvoked = new LinkedList<>();
    
    // 调用链
    public SpanBo headerSpanBo = new SpanBo();
    
    // 历史执行时间(元素为选中作为父代的执行时间)
    public LinkedList<Integer> timeHistory = new LinkedList<Integer>();
    
    
    
    // TODO HEADER处理
    
    
    public RequestDto(String path, Method method, JsonNode arguments, JsonNode response){
        this.path = path;
        this.method = method;
        this.arguments = arguments;
        this.responses = response;
    }
    
    public RequestDto(String path, Method method, JsonNode arguments, JsonNode response, boolean argInUrl){
        this.path = path;
        this.method = method;
        this.arguments = arguments;
        this.responses = response;
        this.argInUrl = argInUrl;
    }
    
    public String getString(){
        return "{ " +
                "\""+ "path" + "\""+ ": " + "\"" + path.toString() + "\"" + "," +
                "\""+ "method" + "\""+ ": " + "\""  + method.toString() + "\""  + "," +
                "\""+ "argument" + "\""+ ": " + arguments.toString() + "," +
                "\""+ "argInUrl" + "\""+ ": " + "\""+ argInUrl + "\""+
                "}";
        
    }
    
    // 生成request对象的字符串记录，用来写入测试用例文件
    public String getStringWithHeader(){
        // 权限处理
        String authKey = null;
        String authValue = null;
        if(HarDependency.getHeaderAuthKeyType().size() == 0 | HarDependency.getTokenSet().size() == 0){
            return getString();
        }
        Random random = new Random();
        Object[] keys = HarDependency.getHeaderAuthKeyType().keySet().toArray();
        // Object randomKey = keys[random.nextInt(keys.length)];
        Object randomKey = keys[keys.length-1];
        String randomValue = HarDependency.getHeaderAuthKeyType().get(randomKey);
        authKey = randomKey.toString();
    
        random = new Random();
        Object[] elements = HarDependency.getTokenSet().toArray();
        // Object randomToken = elements[random.nextInt(elements.length)];
        Object randomToken = elements[elements.length-1];
        
        if (randomValue.equals("Bearer ")){     // 为Bearer做额外处理
            authValue = "Bearer " + randomToken.toString();
        }else{
            authValue = randomToken.toString();
        }
        
        return "{ " +
                "\"" + "headers" + "\"" +  ": " +
                    "{ " +
                    "\"" + authKey + "\"" +  ": " + "\"" + authValue + "\"" +
                "} " + "," +
                "\""+ "path" + "\""+ ": " + "\"" + path.toString() + "\"" + "," +
                "\""+ "method" + "\""+ ": " + "\""  + method.toString() + "\""  + "," +
                "\""+ "argument" + "\""+ ": " + arguments.toString() + "," +
                "\""+ "argInUrl" + "\""+ ": " + "\""+ argInUrl + "\""+
                "}";
        
    }
    
    @Override
    public RequestDto clone() {
        try {
            RequestDto cloneObj = (RequestDto) super.clone();
            cloneObj.path = this.path;
            cloneObj.method = this.method;
            cloneObj.arguments = this.arguments.deepCopy();
            cloneObj.responses = this.responses.deepCopy();
            cloneObj.argInUrl = this.argInUrl;
            cloneObj.serviceInvoked = new LinkedList<>();
            cloneObj.serviceInvoked.addAll(this.serviceInvoked);
            cloneObj.timeHistory = new LinkedList<>();
            cloneObj.timeHistory.addAll(this.timeHistory);
            return cloneObj;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
