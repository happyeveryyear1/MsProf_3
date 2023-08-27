package com.tcse.microsvcdiagnoser.dependency;

import com.tcse.microsvcdiagnoser.dto.RequestDto;
import com.tcse.microsvcdiagnoser.entity.TestSet;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

/*
* 测试套件依赖存储，用来进行遗传算法时的迭代
* */
public class TestSetDependency {
    // 初始测试套件
    @Getter
    @Setter
    private static LinkedList<LinkedList<RequestDto>> initialTestList = new LinkedList<>();
    // 历史执行的测试套件
    @Getter @Setter
    private static LinkedList<TestSet> execedTestSets = new LinkedList<TestSet>();
}
