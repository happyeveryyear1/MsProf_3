package com.tcse.microsvcdiagnoser.entity;

import com.tcse.microsvcdiagnoser.dto.RequestDto;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/*
* 测试套件
* */
@Data
public class TestSet implements Cloneable{
    // 测试套件
    // public LinkedList<Test> testSet = new LinkedList<>();
    public LinkedList<Test> testSet = new LinkedList<>();
    
    // 执行时间
    public int totalExecTime;
    
    // parents
    public LinkedList<TestSet> parent = new LinkedList<>();
    
    // children
    public LinkedList<TestSet> children = new LinkedList<>();
    
    // id
    public int genId = 0;
    public int testSetId = 0;
    
    // 指标
    public int timeIncrease = 0;
    public int numIncrease = 0;
    public int cvIncrease = 0;
    public int invokeSvcIncrease = 0;
    
    // 非支配排序参数
    public int dominationCount = 0;
    public LinkedList<TestSet> dominates = new LinkedList<>();
    public boolean inF = false;
    
    // 舍弃标志
    public boolean deprecated = false;
    
    // 拥挤度排序参数
    public float distance = 0;
    
    // 每个请求执行后的变异系数
    public HashMap<RequestDto, Integer> coefficientOfVariation = new HashMap<>();
    
    // 每个请求序列的平均执行时间
    public LinkedList<Integer> testExecTime = new LinkedList<>();
    
    
    @Override
    public TestSet clone() {
        try {
            TestSet cloneObj = (TestSet) super.clone();
            cloneObj.testSet = new LinkedList<>();
            for(Test test: this.testSet)
                cloneObj.testSet.add(test.clone());
            cloneObj.totalExecTime = this.totalExecTime;
            cloneObj.parent = new LinkedList<TestSet>();
            cloneObj.children = new LinkedList<TestSet>();
            cloneObj.genId = 0;
            cloneObj.testSetId = 0;
            cloneObj.timeIncrease = 0;
            cloneObj.numIncrease = 0;
            cloneObj.cvIncrease = 0;
            cloneObj.invokeSvcIncrease = 0;
            return cloneObj;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
