package com.tcse.microsvcdiagnoser.entity;

import lombok.Data;

import java.util.LinkedList;

/*
* 种群
* */
@Data
public class TestSets {
    public LinkedList<TestSet> testSets = new LinkedList<>();
}
