package com.tcse.microsvcdiagnoser.dependency;

import com.tcse.microsvcdiagnoser.entity.CriticalPathSpan;
import com.tcse.microsvcdiagnoser.entity.Span;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/*
* 执行信息记录
* */
public class ExecedDataDependency {
    // 关键路径
    // key: 关键路径type， value: LinkedList<CriticalSpanHeader>
    @Getter @Setter
    private static LinkedHashMap<String, CriticalPathSpan> criticalPathMap = new LinkedHashMap<>();
    
    // 历史执行数据
    // key: service+rpc, value: LinkedList<spanHeaderBo>
    @Getter @Setter
    private static LinkedHashMap<String, LinkedList<Span>> execedData = new LinkedHashMap<>();
    
    // -1: 什么都不做；0：正在执行；1：执行结束.
    @Getter @Setter
    private static int execState = -1;
    
}
