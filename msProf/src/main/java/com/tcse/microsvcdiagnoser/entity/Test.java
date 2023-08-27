package com.tcse.microsvcdiagnoser.entity;

import com.tcse.microsvcdiagnoser.dto.RequestDto;
import lombok.Data;

import java.util.LinkedList;

/*
* 请求序列
* */
@Data
public class Test implements Cloneable{
    // 请求序列
    public LinkedList<RequestDto> test = new LinkedList<>();
    
    // 调用服务
    public LinkedList<String> serviceInvoked = new LinkedList<>();
    
    // 平均执行时间
    public int avgExecTime;
    // 当次测试用例总执行时间
    public int totalExecTime;
    
    @Override
    public Test clone() {
        try {
            Test cloneObj = (Test) super.clone();
            cloneObj.test = new LinkedList<>();
            for(RequestDto requestDto: this.test)
                cloneObj.test.add(requestDto.clone());
            cloneObj.serviceInvoked = new LinkedList<>();
            for(String s: this.serviceInvoked)
                cloneObj.serviceInvoked.add(s);
            cloneObj.avgExecTime = this.avgExecTime;
            cloneObj.totalExecTime = this.totalExecTime;
            return cloneObj;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
