package com.tcse.microsvcdiagnoser.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.tcse.microsvcdiagnoser.entity.Method;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.regex.*;

/*
* 公共方法
* */

public class CommonUtils {
    public static Method getMethod(String method){
        if(method.equalsIgnoreCase("get"))
            return Method.GET;
        if(method.equalsIgnoreCase("put"))
            return Method.PUT;
        if(method.equalsIgnoreCase("post"))
            return Method.POST;
        if(method.equalsIgnoreCase("delete"))
            return Method.DELETE;
        else
            return Method.Err;
    }
    
    /*
    * 正则判断url中是否含有参数，例如: get 127.0.0.1/foodservice/{startstation}/{endstation}，这种参数是写在url中的
    * */
    public static boolean isArgInUrl(String url){
        String pattern = ".*\\{(.*)\\}";
        return Pattern.matches(pattern, url);
    }
    
    public static int getSubStrNum(String str, String target){
        int count = 0;
        for(int i=0;i<=str.length()-target.length();i++){
            if(str.subSequence(i, i+target.length()).equals(target)){
                count++;
            }
        }
        return count;
    }
    
    /*
    * 返回[0, range)范围的随机数
    * */
    public static int getRandomInt(int range){
        return (int)(Math.random()*range);
    }
    
    public static String getInvariablePart(String str){
        String s[] = str.split("/");
        int len = s.length;
        int lenOfBreak =  s[len-1].length()-s[len-1].replace("-","").length();
        if(lenOfBreak == 0)
            return str;
        s[len-1] = "";
        if(len >= 2)
            s[len-2] = "";
        return  StringUtils.join(s, "/");
    }
    
    public static boolean contain(LinkedList<String> container, String str){
        for(String tmp: container){
            if(tmp.contains(str))
                return true;
        }
        return false;
    }
    
    /*
    * 请求字符串处理，删除%（部分span字符串后由于调用地址包含%，所以要格式化）
    * */
    public static String deletePercent(String path){
        StringBuilder result = new StringBuilder();
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].contains("%")) {
                continue;
            }
            if (i == segments.length - 1) {
                result.append(segments[i]);
            } else {
                result.append(segments[i]).append("/");
            }
        }
        String res = result.toString();
        if (res.endsWith("/"))
            res = res.substring(0, res.length()-1);
        return res;
    }
    
    /*
    * 同为格式化，将%换为arg%，-换为arg-
    * */
    public static String formatStr(String str){
        String[] strings = str.split("/");
        String newStr = "";     // 额外的参数处理  TODO:可以换成去接口列表中匹配一个最近的
        for(String strTmp: strings){
            if (strTmp.contains("%")){
                newStr += "arg%";
            }else if(strTmp.contains("-")){
                newStr += "arg-";
            }else{
                newStr += strTmp;
            }
            newStr += "/";
        }
        return newStr.substring(0, newStr.length()-1);
    }
}
