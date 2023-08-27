package com.tcse.microsvcdiagnoser.entity;

import com.tcse.microsvcdiagnoser.dto.AnnotationBo;
import com.tcse.microsvcdiagnoser.dto.SpanEventBo;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/*
* span对象，调用链的单元
* */
@Data
public class Span {
    private int elapsed;                    // 调用耗时
    private long SpanId;                    // SpanId
    private long startTime;                 // 开始时间
    private String endPoint;                // 目标IP
    private long parentSpanId;              // 父Span
    private String rpc;                     // 目标地址
    private int selfTime;                   // 自己的耗时
    
    private String type = "";               // agentId+rpc
    
    private String traceType = "";          // // private String Application; only traceHeader have this proprity
    private String agentId = "";            // agentId
    private List<AnnotationBo> annotationBoList = new ArrayList<>();    // 事件信息记录
    private List<SpanEventBo> spanEventBoList = new ArrayList<>();      // 事件记录
    
    private PriorityQueue<Span> childSpan = new PriorityQueue<>(new SpanComparator());      // 子span，父子关系组成trace
    
    private String criticalPathSpan = null; // 对应的关键路径
    
    public int childrenNum = 0;             // child数目
    
    public Span parent = null;
    
    
    public void addChild(Span span){
        // 防止空值进入
        if(span != null)
            this.childSpan.add(span);
    }
    
    public String toString(){
        return "Span{" +
                "SpanId=" + SpanId +
                ", startTime=" + startTime +
                ", endPoint=" + endPoint +
                ", type=" + type +
                ", selfTime=" + selfTime +
                '}';
    }
    
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Span spanObj = (Span) object;
        return this.elapsed == spanObj.elapsed && this.SpanId == spanObj.SpanId && this.startTime == spanObj.startTime && this.endPoint == spanObj.endPoint && this.parentSpanId == spanObj.parentSpanId && this.rpc == spanObj.rpc && this.selfTime == spanObj.selfTime && this.agentId == spanObj.agentId;
    }
    
    
}

/*
* 使用开始时间对child span 排序
* */
class SpanComparator implements Comparator<Span> {
    
    @Override
    public int compare(Span o1, Span o2) {
        if(o1.getStartTime() < o2.getStartTime())
            return -1;
        if(o1.getStartTime() > o2.getStartTime())
            return 1;
        return 0;
    }
}