package com.tcse.microsvcdiagnoser.entity;

import com.tcse.microsvcdiagnoser.dto.AnnotationBo;
import lombok.Data;

import java.util.*;

import static com.tcse.microsvcdiagnoser.util.CommonUtils.deletePercent;

/*
* trace转换为关键路径后的span序列
* 转换为关键路径后，可能丢失一些span，所以要有一个新的trace链记录
* */

@Data
public class CriticalPathSpan {
    public String endPoint;
    public String rpc;
    public String type = "";    // agentId+rpc
    public String traceType = "";   // trace的span的遍历，每类关键路径的类型
    public String agentId = "";
    public int elapsed;
    public Span originSpan;
    
    // 记录关键路径头（对应之前的span有哪些）
    public HashSet<Span> criticalPathSet = new HashSet<>();
    
    // 关键路径子span（父子关系将span组成trace）
    // private PriorityQueue<CriticalPathSpan> childSpan = new PriorityQueue<>(new CriticalPathSpanComparator());
    public LinkedList<CriticalPathSpan> childSpan = new LinkedList<>();
    
    public int childNum;
    
    public String toString(){
        return type;
    }
    
    public CriticalPathSpan(Span span){
        this.endPoint = span.getEndPoint();
        this.rpc = span.getRpc();
        this.agentId = span.getAgentId();
        this.type = this.agentId + ": " + deletePercent(this.rpc);
        this.originSpan = span;
        
    }
    
    public CriticalPathSpan(){
    
    }
    
    public CriticalPathSpan getSpan(Span span){
        for (CriticalPathSpan cpSpan: childSpan){
            if (cpSpan.agentId.equals(span.getAgentId()) && cpSpan.rpc.equals(span.getRpc()))
                return cpSpan;
        }
        return null;
    }
}

