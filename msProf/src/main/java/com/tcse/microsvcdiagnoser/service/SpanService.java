package com.tcse.microsvcdiagnoser.service;


import com.tcse.microsvcdiagnoser.entity.Span;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
* span的服务类
* */
@Slf4j
@Service
public class SpanService {
    
    /*
    * 计算span的selfTime(pinpoint自带数据结构没有这个信息)
    * */
    public void calSpanSelfTime(){
        for (Span span: CollectService.getSpanMap().values()){
            int selfTime = span.getElapsed();
            for (Span childSpan: span.getChildSpan()){
                selfTime -= childSpan.getElapsed();
            }
            span.setSelfTime(selfTime);
        }
    }
}
