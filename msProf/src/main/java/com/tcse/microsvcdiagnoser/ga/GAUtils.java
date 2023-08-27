package com.tcse.microsvcdiagnoser.ga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tcse.microsvcdiagnoser.dto.RequestDto;
import com.tcse.microsvcdiagnoser.entity.Method;
import com.tcse.microsvcdiagnoser.entity.Test;
import com.tcse.microsvcdiagnoser.entity.TestSet;
import com.tcse.microsvcdiagnoser.entity.TestSets;
import com.tcse.microsvcdiagnoser.util.CommonArgs;
import jdk.nashorn.internal.ir.ContinueNode;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static com.tcse.microsvcdiagnoser.util.CommonArgs.*;
import static com.tcse.microsvcdiagnoser.util.FuzzingDict.existPlaceList;
import static com.tcse.microsvcdiagnoser.util.FuzzingDict.notExistPlaceList;
import static java.lang.Math.min;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

/*
* 遗传算法公共方法
* */

@Log4j2
public class GAUtils {
    @Autowired
    CommonArgs commonArgs;
    
    /*
    * non-dominated sorting
    * */
    public static LinkedList<TestSets> nonDominatedSort(TestSets population){
        LinkedList<TestSet> testSets = population.testSets;
        int testSetNum = testSets.size();
        // 非支配排序赋值
        for (int i = 0; i < testSetNum; i++){
            TestSet testSet_i = testSets.get(i);
            TestSet testSet_j;
            for(int j = 0; j < testSetNum; j++){
                if(i == j)
                    continue;
                testSet_j = testSets.get(j);
                if (((testSet_i.cvIncrease>=testSet_j.cvIncrease) && (testSet_i.numIncrease>=testSet_j.numIncrease) && (testSet_i.invokeSvcIncrease>=testSet_j.invokeSvcIncrease) && (testSet_i.timeIncrease>=testSet_j.timeIncrease)) && ((testSet_i.cvIncrease>testSet_j.cvIncrease) || (testSet_i.numIncrease>testSet_j.numIncrease) || (testSet_i.invokeSvcIncrease>testSet_j.invokeSvcIncrease) || (testSet_i.timeIncrease>=testSet_j.timeIncrease))){
                    testSet_i.dominates.add(testSet_j);
                    testSet_j.dominationCount += 1;
                }
            }
        }
        
        LinkedList<TestSets> result = new LinkedList<>();
        
        // 非支配排序
        while(true){
            TestSets F = new TestSets();
            boolean testSetNotInResult = false;
            for (TestSet testSet : testSets) {
                if (testSet.dominationCount == 0 && testSet.inF==false){
                    F.testSets.add(testSet);
                    testSet.inF = true;
                    testSetNotInResult = true;
                }
            }
            if(testSetNotInResult == false)
                break;
            result.add(F);
            for (TestSet testSet : F.testSets) {
                for(TestSet dominateTestSet: testSet.dominates){
                    dominateTestSet.dominationCount -= 1;
                }
            }
        }
        
        // 清空非支配排序参数
        for(TestSet testSet: testSets){
            testSet.dominates = new LinkedList<>();
            testSet.dominationCount = 0;
            testSet.inF = false;
        }
        
        return result;
    }
    
    /*
    * crowding distance sorting
    * */
    public static LinkedList<TestSet> crowdingDistanceSort(TestSets testSets){
        LinkedList<TestSet> testSetList = testSets.testSets;
        testSetList.sort(Comparator.comparingInt(GAUtils::objFunc));
        int maxObjFunc = objFunc(testSetList.getFirst());
        int minObjFunc = objFunc(testSetList.getLast());
        testSetList.getFirst().distance = Float.POSITIVE_INFINITY;;
        testSetList.getLast().distance = Float.NEGATIVE_INFINITY;
        for(int i = 1; i < testSetList.size()-1; i++){
            testSetList.get(i).distance = testSetList.get(i).distance + ((float)(objFunc(testSetList.get(i+1))-objFunc(testSetList.get(i-1))))/(maxObjFunc-minObjFunc);
        }
        testSetList.sort((o1, o2) -> Float.compare(o1.distance, o2.distance));
        return testSetList;
    }
    
    /*
    * 选择(父代子代的集合通过非支配排序和拥挤度排序选择下一代种群)
    * */
    public static TestSets select(TestSets population){
        LinkedList<TestSets> sortedTestSets = nonDominatedSort(population);
        TestSets newGen = new TestSets();
        for(int i = 0; i < sortedTestSets.size(); i++){
            if ((newGen.testSets.size() + sortedTestSets.get(i).testSets.size()) < populationSize){
                newGen.testSets.addAll(sortedTestSets.get(i).testSets);
            }else{
            // 拥挤度排序
                LinkedList<TestSet> crowdingDistanceSortedTestSets = crowdingDistanceSort(sortedTestSets.get(i));
                for(int j = 0; j < crowdingDistanceSortedTestSets.size(); j++){
                    if(newGen.testSets.size() < populationSize) {
                        newGen.testSets.add(crowdingDistanceSortedTestSets.get(j));
                    }else {
                        return newGen;
                    }
                }
            }
        }
        return newGen;
    }
    
    public static int objFunc(TestSet testSet){
        return testSet.timeIncrease + testSet.cvIncrease + testSet.numIncrease + testSet.invokeSvcIncrease;
    }
    
    /*
    * crossover
    * */
    public static LinkedList<TestSet> cross(TestSet parent1_origin, TestSet parent2_origin){
        TestSet parent1 = parent1_origin.clone();
        TestSet parent2 = parent2_origin.clone();
        int len = parent1.testSet.size();
        assert parent1.testSet.size() == parent2.testSet.size();
        int[] vector = new int[len];
        for (int i = 0; i < len; i++){
            double random = Math.random();
            int v;
            if(random >= 0.5)
                v = 1;
            else
                v = 0;
        }
        TestSet child1 = new TestSet();
        TestSet child2 = new TestSet();
        LinkedList<Test> child1TestSet = child1.testSet;
        LinkedList<Test> child2TestSet = child2.testSet;
        for(int i = 0; i < len; i++){
            double random = Math.random();
            if(random >= 0.5){
                child1TestSet.add(parent1.testSet.get(i));
                child2TestSet.add(parent2.testSet.get(i));
            }else{
                child1TestSet.add(parent2.testSet.get(i));
                child2TestSet.add(parent1.testSet.get(i));
            }
            
        }
        child1.parent.add(parent1_origin);
        child1.parent.add(parent2_origin);
        child2.parent.add(parent1_origin);
        child2.parent.add(parent2_origin);
        LinkedList<TestSet> result = new LinkedList<>();
        result.add(child1);
        result.add(child2);
        return result;
    }
    
    /*
    * mutation
    * */
    public static TestSet mutate(TestSet testSet){
        Random random = new Random();
        LinkedList<Test> testSetList = testSet.testSet;
        int len_test = testSetList.size();
        for (Test test: testSet.testSet){
            // if (random.nextInt(len_test) == 1){
            if (true){      // Template 增大变异概率
            // 测试被选择来突变
                int len_request = test.test.size();
                LinkedList<RequestDto> requestDtoList = test.test;
                // ListIterator<RequestDto> iter = requestDtoList.listIterator();
                int requestDtoListIdx = 0;
                while (requestDtoListIdx < requestDtoList.size()){
                    if(random.nextInt(2) == 1 || random.nextInt(len_request) == 1){
                    // 请求被选择来突变
                        int mutateOp = random.nextInt(2);
                        if(mutateOp == 0 && complexFuzzing){
                        // 删除
                            deleteRequestDto(requestDtoList, requestDtoList.get(requestDtoListIdx));
                        }else {
                        // 变异
                            changeRequestDeo(requestDtoList, requestDtoList.get(requestDtoListIdx));
                        }
                    }
                    requestDtoListIdx += 1;
                }
                // 插入
                int insertTime = 0;
                while(requestDtoList.size() < len_request){
                    insertTime += 1;
                    if (random.nextInt() <= Math.pow(insertProbability, insertTime)) {
                        insertRequestDto(requestDtoList);
                    }else {
                        break;
                    }
                }
                
            }
        }
        
        return testSet;
    }
    
    /*
    * 删除操作
    * */
    public static void deleteRequestDto(LinkedList<RequestDto> requestDtoList, RequestDto requestDto){
        // 请求已经被删除
        if (!requestDtoList.contains(requestDto))
            return;
        
        int idxRequestDto = requestDtoList.indexOf(requestDto);
        
        // 依赖requestDto的对象列表
        LinkedList<RequestDto> requestDtoLakeDeps = new LinkedList<>();
        for(int i = 0; i < min(dependencyDistance, requestDtoList.size()); i++){
            if (isDependency(requestDto, requestDtoList.get(i))){
                requestDtoLakeDeps.add(requestDtoList.get(i));
            }
        }
        
        if (requestDtoLakeDeps.size() == 0){
        // 无依赖
            requestDtoList.remove(requestDto);
        }else {
        // 有依赖
            // 修复
            LinkedList<RequestDto> requestDtoListToFix = fixDependency(requestDtoLakeDeps, requestDto);
            if (requestDtoListToFix != null) {
                // 找到可修复列表
                for (RequestDto dtoListToFix : requestDtoListToFix) {
                    if (satisfiedConstraint(requestDtoList, requestDto, idxRequestDto)) {
                        requestDtoList.remove(idxRequestDto);
                        requestDtoList.add(idxRequestDto, dtoListToFix);
                    }
                }
                
            } else {
                // 无法修复，则链式删除
                requestDtoList.remove(requestDto);
                for(RequestDto requestDtoLakeDep: requestDtoLakeDeps){
                    deleteRequestDto(requestDtoList, requestDtoLakeDep);
                }
                
            }
        }
    }
    
    /*
    * 修改操作
    * */
    public static void changeRequestDeo(LinkedList<RequestDto> requestDtoList, RequestDto requestD){
        // 变异
        JsonNode arg = requestD.arguments;
        int len = arg.size();
        Random random = new Random();
        // 注意此处假设参数只有一层，不存在json嵌套
        arg.fields().forEachRemaining(argItem -> {
            String key = argItem.getKey();
            String value = argItem.getValue().textValue();
            if (value == null){
                ((ObjectNode) arg).put(key, "");
                value = "";
            }
            int i = random.nextInt(len);
            // if (i == 1){                         // 理论变异概率
            if (random.nextInt(2) == 1){     // Template 增大变异概率
            // 开始变异
                // 注意此处类型判断都应改为和swagger文档中的类型匹配
                // 地名
                if(key.contains("Place")) {
                    int tmp = random.nextInt(2);
                    if (tmp == 1) {
                        int placeRand = random.nextInt(existPlaceList.length);
                        ((ObjectNode) arg).put(key, existPlaceList[placeRand]);
                    } else {
                        int placeRand = random.nextInt(notExistPlaceList.length);
                        ((ObjectNode) arg).put(key, notExistPlaceList[placeRand]);
                    }
                }else if(isNumeric(value)) {
                // 数字
                    boolean isFloat = false;
                    if(value.contains(".")){
                        isFloat = true;
                    }
                    if(value.startsWith("-")){
                        double tmp = Math.random();
                        if(tmp <= zeroProbability){
                            ((ObjectNode) arg).put(key, 0);
                        }else{
                            ((ObjectNode) arg).put(key, random.nextInt(changeIntScope));
                        }
                    }else if(Float.parseFloat(value) == 0) {
                        double tmp = Math.random();
                        if(tmp <= 0.5){
                            ((ObjectNode) arg).put(key, random.nextInt(changeIntScope));
                        }else{
                            ((ObjectNode) arg).put(key, -random.nextInt(changeIntScope));
                        }
                    }else{
                        double tmp = Math.random();
                        if(tmp <= zeroProbability){
                            ((ObjectNode) arg).put(key, 0);
                        }else{
                            ((ObjectNode) arg).put(key, -random.nextInt(changeIntScope));
                        }
                    }
                }else if(isDate(value)){
                // 日期
                    ((ObjectNode) arg).put(key, makeNewDate());
                }else if(value.equals("true") || value.equals("false") ){
                // boolean
                    if(value.equals("true")){
                        ((ObjectNode) arg).put(key, "false");
                    }else{
                        ((ObjectNode) arg).put(key, "true");
                    }
                }else{
                // 普通字符串
                    double tmp = Math.random();
                    if(tmp <= 0.2){
                        ((ObjectNode) arg).put(key, "");
                    }else{
                        ((ObjectNode) arg).put(key, mutateStr(value));
                    }
                }
                
            }
        });
        
        
    }
    
    public static void insertRequestDto(LinkedList<RequestDto> test){
        // 随机找个位置插入
    }
    
    public static boolean satisfiedConstraint(LinkedList<RequestDto> requestDtoList, RequestDto requestDto, int idxRequestDto){
        return true;
    }
    
    public static boolean isDependency(RequestDto before, RequestDto after){
        String afterUrl = after.path;
        Method afterMethod = after.method;
        String beforeUrl = before.path;
        Method beforeMethod = before.method;
        
        
        return true;
    
    }
    
    public static LinkedList<RequestDto> fixDependency(LinkedList<RequestDto> requestDtoLakeDeps, RequestDto deleteRequestDto){
        return null;
    }
    
    /*
    * 竞标选择 每个父代要通过两个父代竞标选择而来
    * */
    public static LinkedList<TestSet> tournamentSelect(TestSets population){
        int populationNum = population.testSets.size();
        LinkedList<TestSet> result = new LinkedList<>();
        for (int i = 0; i < 2; i++){
            TestSet candidate1 = population.testSets.get((int)(Math.random()*populationNum));
            TestSet candidate2 = population.testSets.get((int)(Math.random()*populationNum));
            
            if (candidate1.timeIncrease > candidate2.timeIncrease)
                result.add(candidate1);
            else
                result.add(candidate2);
        }
        return result;
        
    }
    
    // select
    public static TestSets select(TestSets parent, TestSets children){
        return null;
    }
    
    // constraints
    public static Test fitConstraint(Test test){
        return null;
    }
    
    /*
    * 数字判断
    * */
    public static boolean isNumeric(String str) {
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;//异常 说明包含非数字。
        }
        return true;
    }
    
    /*
    * 日期判断
    * */
    public static boolean isDate(String str){
        String[] parsePatterns = {"yyyy-MM-dd","yyyy年MM月dd日",
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd",
                "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyyMMdd"};
        if (str == null)
            return false;
        try{
            DateUtils.parseDate(str, parsePatterns);
        } catch ( ParseException e) {
            return false;
        }
        return true;
        
    }
    
    /*
    * 生成新日期
    * 注意日期生成，随便写了个符合train-ticket格式的规则 "2022-06-22"
    * */
    public static String makeNewDate(){
        GregorianCalendar gc = new GregorianCalendar();
    
        int year = randBetween(2000, 2030);
    
        gc.set(gc.YEAR, year);
    
        int dayOfYear = randBetween(1, gc.getActualMaximum(gc.DAY_OF_YEAR));
    
        gc.set(gc.DAY_OF_YEAR, dayOfYear);
    
        String yearStr = Integer.toString(gc.get(gc.YEAR));
        String monthStr = Integer.toString(gc.get(gc.MONTH));
        String dayStr = Integer.toString(gc.get(gc.DAY_OF_MONTH));
        if (monthStr.length() < 2){
            monthStr = "0" + monthStr;
        }
        if (dayStr.length() < 2){
            dayStr = "0" + dayStr;
        }
        return (yearStr + "-" + monthStr + "-" + dayStr);
    
    }
    
    /*
    * 随机数生成
    * */
    public static int randBetween(int min, int max) {
        Random random = new Random();
        int i = random.nextInt(max) % (max - min + 1) + min;
        return i;
    }
    
    /*
    * 字符串变异
    * */
    public static String mutateStr(String str){
        String ans = "";
        Random random = new Random();
        for (int i = 0; i < str.length(); i++){
            if(Math.random() <= 0.3){
            // 删
                continue;
            }else if(Math.random() <= 0.6){
            // 改
                Random r = new Random();
                char c = (char)(r.nextInt(26) + 'a');
                ans += Character.toString(c);
            }else{
            // 插入
                Random r = new Random();
                char c = (char)(r.nextInt(26) + 'a');
                ans += Character.toString(c);
                ans += str.charAt(i);
            }
        }
        return ans;
    }
    
}

