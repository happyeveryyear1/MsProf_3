package com.tcse.microsvcdiagnoser.service;

import com.tcse.microsvcdiagnoser.context.Header;
import com.tcse.microsvcdiagnoser.dependency.ExecedDataDependency;
import com.tcse.microsvcdiagnoser.dependency.TestSetDependency;
import com.tcse.microsvcdiagnoser.dto.AnnotationBo;
import com.tcse.microsvcdiagnoser.dto.RequestDto;
import com.tcse.microsvcdiagnoser.dto.SpanBo;
import com.tcse.microsvcdiagnoser.entity.*;
import com.tcse.microsvcdiagnoser.ga.GAUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.tcse.microsvcdiagnoser.util.CommonArgs.*;

/*
* 测试执行，即遗传算法
* */
@Slf4j
@Service
public class TestExecutorService {
    // @Autowired
    // HarDepService harDepService;
    
    public TestSets population = new TestSets();    // 当前种群
    public int genNum = 0;                          // 迭代次数
    public int testSetNum = 0;                      // 测试套件数(当前代的测试套件数)
    @Getter @Setter
    public static ExecutorService execPool = Executors.newFixedThreadPool(testSetSize);
    public LinkedList<LinkedList<Integer>> increaseTimeRecord = new LinkedList<>();         // 每代，每个测试套件的执行时间增加程度
    public List<List<List<Integer>>> timeRecord = new LinkedList<>();                       // 每代，每个测试用例的执行时间
    public LinkedList<TestSet> execedTestSets = TestSetDependency.getExecedTestSets();

    
    /*
    * 前端接口的对应，遗传算法的总入口
    * */
    public void execute(){
        // 设置tag，前端需要知道后端的执行状态
        ExecedDataDependency.setExecState(0);
        // 初始化种群
        initInitialPopulation();
        log.info("初始种群中测试套件数目： {}", population.testSets.size());
        // 遗传算法
        GA();
        // 接触tag
        ExecedDataDependency.setExecState(1);
    
    }
    
    /*
    * 初始化种群
    * 写入测试文件并执行
    * */
    public void initInitialPopulation(){
        // 初始为若干请求序列集合
        LinkedList<LinkedList<RequestDto>> initialTestSet = TestSetDependency.getInitialTestList();
        File testSetFile = new File(testBaseDir);
        if (!testSetFile.exists()) {
            testSetFile.mkdirs();
        }
        String testSetStr = "";
        // 初始请求序列，按照测试套件大小区分，分为若干测试套件
        for(int i = 0; i < initialTestSet.size(); i++){
            testSetStr += "========\n";
            LinkedList<RequestDto> test = initialTestSet.get(i);
            for (int j = 0; j < test.size(); j++){
                // testSetStr += test.get(j).getString();
                testSetStr += test.get(j).getStringWithHeader();
                testSetStr += "\n";
            }
            // 若干testSet分为一组，合并作为初始的TestSets
            // 每个testSet中，序列使用 ==== 作为分组
            // 测试套件名：testSet-代数-测试用例idx(第几个测试用例)
            if ((i+1)%testSetSize == 0){
                try {
                    FileUtils.writeStringToFile(new File(testBaseDir+"testset-" + this.genNum + "-" + (i+1)/testSetSize + ".test"), testSetStr, "UTF-8");
                    Thread.sleep(5000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                testSetStr = "";
                // 执行并确保完成
                boolean execSuccess = false;
                int execTime = 0;
                TestSet tmpTestSet = new TestSet();
                while (!execSuccess){
                    // 执行当前测试套件
                    executeTestSet(this.genNum, (i+1)/testSetSize);
                    
                    // 实例化测试对象TestSet
                    tmpTestSet = new TestSet();
                    for (int k = testSetSize-1; k >= 0 ; k--){
                        LinkedList<RequestDto> test_k_dto = initialTestSet.get(i-k);
                        Test test_k = new Test();
                        test_k.test = test_k_dto;
                        tmpTestSet.testSet.add(test_k);
                    }
                    tmpTestSet.genId = genNum;
                    tmpTestSet.testSetId = (i+1)/testSetSize;
                    
                    // 等待执行完成
                    try {
                        Thread.sleep(testExecTime*1000+1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    // 统计执行时间
                    for (int k = 0; k < testSetSize; k++) {
                        Test tmpTest = tmpTestSet.testSet.get(k);
                        int testLen = tmpTest.getTest().size();
                        int totalExecTime = 0;
                        int totalRequestNum = 0;
                        for (Span headerSpan : CollectService.getTraceSet()) {
                            boolean flag_genId = false;
                            boolean flag_testSetId = false;
                            boolean flag_testId = false;
                            List<AnnotationBo> annotationBoList = headerSpan.getAnnotationBoList();
                            // 需要筛选数据为当前执行数据
                            for (AnnotationBo annotationBo : annotationBoList) {
                                if (annotationBo.getKey() == Header.HTTP_MY_TOKEN && annotationBo.getValue() != null && annotationBo.getValue().toString().equals(String.valueOf(tmpTestSet.getGenId())))
                                    flag_genId = true;
                            }
                            for (AnnotationBo annotationBo : annotationBoList) {
                                if (annotationBo.getKey() == Header.HTTP_TASK_ID && annotationBo.getValue() != null && annotationBo.getValue().toString().equals(String.valueOf(tmpTestSet.getTestSetId())))
                                    flag_testSetId = true;
                            }
                            for (AnnotationBo annotationBo : annotationBoList) {
                                if (annotationBo.getKey() == Header.HTTP_TEST_ID && annotationBo.getValue() != null && annotationBo.getValue().toString().equals(String.valueOf(k)))
                                    flag_testId = true;
                            }
                            if (flag_genId && flag_testSetId && flag_testId) {
                                totalExecTime += headerSpan.getElapsed();
                                totalRequestNum += 1;
                            }
                        }
                        // 如果执行总时长为0、测试长度为0，说明执行出错，需要重新执行。
                        // 若重新执行次数过多，说明被测系统宕机，则请准备好被测系统后重启性能分析
                        if (totalExecTime == 0 || testLen == 0){
                            log.error("初始测试序{} 列执行错误，执行时间:{}, 执行长度:{}, 重新执行", 0+"-"+tmpTestSet.testSetId, totalExecTime, testLen);
                            execTime += 1;
                            if (execTime >= locustInitTestSetReexecTime){
                                // TODO 出错次数过多，需要终止/重启程序
                                log.error("测试序列执行错误超过{}次，请重新运行", execTime);
                            }
                        }else{
                            log.info("初始测试序列{} 执行成功，执行时间:{}, 执行长度:{}", 0+"-"+tmpTestSet.testSetId, totalExecTime, testLen);
                            execSuccess = true;
                            if ((totalRequestNum/testLen) == 0) {               // 测试用例过短
                                tmpTest.setTotalExecTime(totalExecTime);
                                tmpTest.setAvgExecTime(totalExecTime);
                            }else{                                              // 时间均分
                                tmpTest.setTotalExecTime(totalExecTime);
                                tmpTest.setAvgExecTime(totalExecTime/(totalRequestNum/testLen));
                            }
                        }
                    }
                }
                
                
                population.testSets.add(tmpTestSet);
                execedTestSets.add(tmpTestSet);
            }
            
            
        }
    }
    
    
    /*
    * 执行测试套件
    * 多线程并发执行每个测试套件
    * 每个测试套件的并发度由locust控制
    * 类似初始测试用例的执行，做一些特殊化的处理
    * */
    public void executeChild(TestSet testSet, int curGenTestSetIdx){
        try {
            // 更新套件信息
            testSet.setGenId(this.genNum);
            testSet.setTestSetId(curGenTestSetIdx);
            // 写入测试用例
            writeTestSet(testSet, curGenTestSetIdx);
            // 执行并确保完成
            boolean execSuccess = false;
            int execTime = 0;
            while (!execSuccess) {
                execTime += 1;
                if (execTime >= locustTestSetReexecTime) {       // 超过执行次数，舍弃
                    testSet.setDeprecated(true);
                    log.error("测试序列{} 执行错误大于{}次，舍弃", this.genNum + "-" + curGenTestSetIdx, locustTestSetReexecTime);
                    break;
                }
                // 执行测试套件
                executeTestSet(this.genNum, curGenTestSetIdx);
                // 等待执行完成
                try {
                    Thread.sleep(testExecTime * 1000 + 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 统计执行时间
                synchronized (CollectService.getTraceSet()) {
                    for (int k = 0; k < testSetSize; k++) {
                        Test tmpTest = testSet.testSet.get(k);
                        int testLen = tmpTest.getTest().size();
                        int totalExecTime = 0;
                        int totalRequestNum = 0;
                        Iterator<Span> iterator = CollectService.getTraceSet().iterator();
                        // for (Span headerSpan : traceSet) {
                        while(iterator.hasNext()){
                            Span headerSpan = iterator.next();
                        
                            boolean flag_genId = false;
                            boolean flag_testSetId = false;
                            boolean flag_testId = false;
                            List<AnnotationBo> annotationBoList = headerSpan.getAnnotationBoList();
                            for (AnnotationBo annotationBo : annotationBoList) {
                                if (annotationBo.getKey() == Header.HTTP_MY_TOKEN && annotationBo.getValue() != null && annotationBo.getValue().toString().equals(String.valueOf(testSet.getGenId())))
                                    flag_genId = true;
                            }
                            for (AnnotationBo annotationBo : annotationBoList) {
                                if (annotationBo.getKey() == Header.HTTP_TASK_ID && annotationBo.getValue() != null && annotationBo.getValue().toString().equals(String.valueOf(testSet.getTestSetId())))
                                    flag_testSetId = true;
                            }
                            for (AnnotationBo annotationBo : annotationBoList) {
                                if (annotationBo.getKey() == Header.HTTP_TEST_ID && annotationBo.getValue() != null && annotationBo.getValue().toString().equals(String.valueOf(k)))
                                    flag_testId = true;
                            }
                            if (flag_genId && flag_testSetId && flag_testId) {
                                totalExecTime += headerSpan.getElapsed();
                                totalRequestNum += 1;
                            }
                        }
                        if (totalExecTime == 0 || testLen == 0) {
                            log.error("测试序列{} 执行错误，执行时间:{}, 执行长度:{}, 重新执行", this.genNum + "-" + curGenTestSetIdx, totalExecTime, testLen);
                        } else {
                            execSuccess = true;
                            log.info("测试序列{} 执行成功，执行时间:{}, 执行长度:{}", this.genNum + "-" + curGenTestSetIdx, totalExecTime, testLen);
                            if ((totalRequestNum / testLen) == 0) {                                // 测试用例过短
                                tmpTest.setTotalExecTime(totalExecTime);
                                tmpTest.setAvgExecTime(totalExecTime);
                            } else {                                                             // 时间均分
                                tmpTest.setTotalExecTime(totalExecTime);
                                tmpTest.setAvgExecTime(totalExecTime / (totalRequestNum / testLen));
                            }
                        }
                    }
                }
            }
    
            // 记录执行
            execedTestSets.add(testSet);
        }catch (ConcurrentModificationException e){
            log.info(String.valueOf(e));
        }
        
    }
    
    /*
    *  testSet写入文件
    * */
    public void writeTestSet(TestSet testSet, int curGenTestSetIdx){
        File testSetFile = new File(testBaseDir);
        if (!testSetFile.exists()) {
            testSetFile.mkdirs();
        }
        String testSetStr = "";
        for(int i = 0; i < testSet.testSet.size(); i++){
            testSetStr += "========\n";
            LinkedList<RequestDto> test = testSet.testSet.get(i).test;
            for (int j = 0; j < test.size(); j++){
                testSetStr += test.get(j).getStringWithHeader();
                testSetStr += "\n";
            }
            
        }
        try {
            FileUtils.writeStringToFile(new File(testBaseDir+"testset-" + this.genNum + "-" + curGenTestSetIdx + ".test"), testSetStr, "UTF-8");
            Thread.sleep(5000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    
    
    /* 调用locust执行测试
     * locust -f D:\\MicrosvcDiagnose\\microsvcDiagnoser\\src\\bin\\locustExec.py --headless -t=5s -u=5 --testsets-path=D:\\MicrosvcDiagnose\\microsvcDiagnoser\\testsets\\ --testset-name='testset-0-1.test' --testset-id=2 --test-id=1 --html=locust.html
     * --testsets-path   测试套件目录
     * --testset-name    测试套件名： '代数-测试套件id'
     * --testset-id      测试套件id  当前代汇总第id个测试套件
     * --test-id         测试用例id  执行第i个测试序列(启动若干用户并行执行)
     * -u=5              单个测试用例并行用户数
     *
     **/
    public void executeTestSet(int genId, int testSetId){
        
        for(int i = 0; i < testSetSize; i++){
            String cmd = String.format("locust -f %s --headless -t=%s -u=%s --testsets-path=%s --testset-name=%s --testset-id=%s --test-id=%s --html=%s", locustExecutor, testExecTime, locustConcurrentNum, testBaseDir, "testset-"+genId+"-"+testSetId+".test", testSetId, i, testBaseDir+"locust"+"-"+genId+"-"+testSetId+"-"+i+".html");
            log.info("exec cmd: {}", cmd);
            execPool.submit(() -> {
                try {
                    TestExecutorService.execCmdLine(cmd.split(" "));
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    
    /*
    * 新建线程执行locust测试
    * 注意：由于locust的设计问题，正确输出被指向了“错误流”，因此出现错误流为正常情况
    * */
    private static void execCmdLine(String[] cmd) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd, null);
        
        //开启两个线程用来读取流，否则会造成死锁问题
        new Thread(() -> {
            // FileOutputStream fileOutputStream = null;
            // TeeInputStream teeInputStream = null;
            // 如果是其他平台,可能需要使用utf-8格式
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                // fileOutputStream = new FileOutputStream(logFile, true);
                // //使用分流器，输出日志文件
                // teeInputStream = new TeeInputStream(inputStream, fileOutputStream);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    log.info("locust info");
                    log.info("{}", line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                    // teeInputStream.close();
                    // fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
        }).start();
        new Thread(() -> {
            InputStreamReader err = new InputStreamReader(process.getErrorStream());
            BufferedReader bferr = new BufferedReader(err);
            String errline = "";
            try {
                while ((errline = bferr.readLine()) != null) {
                    log.info("locust错误流");
                    log.info(errline);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    bferr.close();
                    err.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        process.waitFor();
        process.destroy();
    }
    
    /*
    * 遗传算法主逻辑
    * */
    public void GA(){
        int curGenChildIdx = 0;
        HashMap<String, Integer> coefficientOfVariation = new HashMap<>();
        // 初始化变异系数
        // 遗传算法终止条件
        while (this.genNum < iterNumTime){
            log.info("Gen Num:{}", this.genNum);
            
            this.genNum += 1;               // 迭代次数
            curGenChildIdx = 0;             // 种群中第i个子代
            
            // 产生子代
            // FIXME 子代应该在全部产生后才加入种群？
            
            while(population.testSets.size() < populationSize*2) {
                LinkedList<TestSet> parents = GAUtils.tournamentSelect(population);
                // 交叉
                LinkedList<TestSet> children = GAUtils.cross(parents.get(0), parents.get(1));
                // 变异
                GAUtils.mutate(children.get(0));
                GAUtils.mutate(children.get(1));
                // child1
                executeChild(children.get(0), curGenChildIdx+=1);   // 执行
                updateIndicators(children.get(0));                  // 更新指标
                if (children.get(0).isDeprecated()){                // 执行失败，舍弃
                    curGenChildIdx -= 1;
                }else{                                              // 执行成功，加入种群
                    population.testSets.push(children.get(0));
                }
                
                // child2
                executeChild(children.get(1), curGenChildIdx+=1);   // 执行
                updateIndicators(children.get(1));                  // 更新指标
                if (children.get(1).isDeprecated()){                // 执行失败，舍弃
                    curGenChildIdx -= 1;
                }else{                                              // 执行成功，加入种群
                    population.testSets.push(children.get(1));
                }
            }
            
            // 种群选择
            population = GAUtils.select(population);
            
            // 时间增加程度记录
            LinkedList<Integer> increaseTimeTmp = new LinkedList<>();
            // 时间记录
            LinkedList<List<Integer>> timeTmp = new LinkedList<>();
            for (TestSet testSets: population.testSets){
                increaseTimeTmp.add(testSets.timeIncrease);
                List<Integer> testTime = new ArrayList<>();
                for(Test testTmp: testSets.getTestSet()){
                    testTime.add(testTmp.avgExecTime);
                }
                timeTmp.add(testTime);
            }
            increaseTimeRecord.add(increaseTimeTmp);
            timeRecord.add(timeTmp);
            log.info("timeIncrease: {}", String.valueOf(increaseTimeRecord));
            log.info("time: {}", String.valueOf(timeRecord));
            
            // 时间记录
            // LinkedList<Integer> tmpTime2 = new LinkedList<>();
            // for (TestSet testSets: population.testSets){
            //     tmpTime2.add(testSets.timeIncrease);
            // }
            // timeRecord.add(tmpTime);
            
        }
        if(this.genNum == iterNumTime){
            log.info("timeIncrease: {}", String.valueOf(increaseTimeRecord));
            log.info("time: {}", String.valueOf(timeRecord));
        }
    }
    
    /*
    * 更新指标（测试用例施加增加程度）
    * */
    public void updateIndicators(TestSet testSet){
        TestSet parent1 = testSet.parent.get(0);
        TestSet parent2 = testSet.parent.get(1);
        int timeIncrease = 0;
        for(int i = 0; i < testSetSize; i++){
            if(testSet.getTestSet().get(i).avgExecTime>parent1.getTestSet().get(i).avgExecTime)
                timeIncrease += (testSet.getTestSet().get(i).avgExecTime-parent1.getTestSet().get(i).avgExecTime);
            if(testSet.getTestSet().get(i).avgExecTime>parent2.getTestSet().get(i).avgExecTime)
                timeIncrease += (testSet.getTestSet().get(i).avgExecTime-parent2.getTestSet().get(i).avgExecTime);
        }
        testSet.setTimeIncrease(timeIncrease);
    }
    
    
}



// old TestSet Exec
    /*  class Task implements Runnable {
        int taskTestIdx;
        int testGenNum;
        String testSetsPath;
        String testSetName;
        String locustUserNum;
        String time;
        boolean htmlReport;
    
        public Task(int taskTestIdx, int testGenNum, String testSetName, String testSetsPath, String locustUserNum, String time, boolean htmlReport) {
            this.taskTestIdx = taskTestIdx;
            this.testSetName = testSetName;
            this.testSetsPath = testSetsPath;
            this.testGenNum = testGenNum;
            this.locustUserNum = locustUserNum;
            this.time = time;
            this.htmlReport = htmlReport;
        }
    
    
        @Override
        public void run() {
            CommandLine cmd = new CommandLine("bash");
        }
    }*/

// public void executeTestSet(int genNum, int testSetId, int curGenTestSetIdx){
//
//     for(int i = 0; i < testSetSize; i++){
//         String cmd = String.format("locust -f %s --headless -t=%s --testsets-path=%s --testset-name=%s --testset-id=%s --test-id=%s --html=%s", locustExecutor, testExecTime, testBaseDir, "testset-"+genNum+"-"+curGenTestSetIdx+".test", testSetNum, i, locustBaseDir+i+"/locust.html");
//         log.info("exec cmd: {}", cmd);
//         execPool.submit(() -> {
//             try {
//                 TestExecutorService.execCmdLine(cmd.split(" "));
//             } catch (IOException | InterruptedException e) {
//                 e.printStackTrace();
//             }
//         });
//     }
// }

    /*
    locust -f D:\\MicrosvcDiagnose\\microsvcDiagnoser\\src\\bin\\locustExec.py --headless -t 10s --testsets-path=D:\\MicrosvcDiagnose\\microsvcDiagnoser\\testsets\\ --testset-name='testset-0-1.test' --test-idx=1 --html=locust.html
    --testsets-path 测试套件目录
    --testset-name  测试套件名称
    --test-idx      执行第i个测试序列(启动若干用户并行执行)
    -u=5            单个测试用例并行用户数
    */