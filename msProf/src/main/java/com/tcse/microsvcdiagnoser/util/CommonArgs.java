package com.tcse.microsvcdiagnoser.util;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
/*
* 以下为一些重要参数，需要经常配置的参数可以通过配置文件
* */

@Configuration
public class CommonArgs {
    public static int dependencyDistance = 10;      // 向上搜索参数依赖的最高搜索深度
    
    // 交叉变异时的概率
    public static double insertProbability = 0.5;
    public static double zeroProbability = 0.2;
    public static int changeIntScope = 20;
    
    
    public static String testBaseDir = "D:\\MicrosvcDiagnose\\microsvcDiagnoser\\testsets\\"; // 测试生成的地址
    public static int testSetSize = 2;     // 一个测试套件中有多少个测试用例,（测试套件大小）
    // ！！！务必保证①初始个体数小于种群目标个体数; （初始个体数即为测试用例数/测试套件大小），一个Har即为一个测试用例，否则迭代会终止。②初始测试用例数>测试套件大小，否则初始测试套件无法生成
    public static int populationSize = 2;   // 种群大小
    public static int iterNumTime = 1;    // 迭代次数
    
    public static boolean cvFlag = false;  // 是否打开变异系数作为选择参数
    public static boolean complexFuzzing = false;   // 仅使用字符串fuzzing作为变异
    
    public static int testExecTime = 10;   // 测试执行时间，单位s
    public static int locustConcurrentNum = 4;  // 每个测试用例并发数
    
    // 初始个体失败后的重试数
    public static int locustInitTestSetReexecTime = 5;
    public static int locustTestSetReexecTime = 2;
    
    public static String locustExecutor = "src/bin/locustExec.py";  // locust脚本位置
    public static String harDir = "D:\\robot\\brower-mob-proxy-for-robot";  // har文件位置
    public static String APIFile = null;    // swagger文件位置
    
    
    // 异常检测
    // 异常检测异常占比
    public static double anomalyRatio = 0.05;
    
    // 异常展示数量
    public static int abnormalExhibitionNum = 10;
    
    //tomcat入队时间
    public static int TOMCAT_SOCKET_PROCESS_TIME = 904;
    
    // 阻塞分析最少阻塞时间，单位ms，即小于该时间的等待耗时均不认为阻塞发生。
    public static int blockThreshold = 10;
    
    // 阻塞分析区间 时间戳上下各5分钟
    public static long blockAnalyzeSlice = 6000;
    
    // QPS异常概率
    public static double QPSErrorRate = 0.05;
    
    @Value("${common-args.testBaseDir}")
    public String testBaseDirTmp;
    @Value("${common-args.locustExecutor}")
    public String locustExecutorTmp;
    @Value("${common-args.harDir}")
    public String harDirTmp;
    @Value("${common-args.APIFile}")
    public String APIFileTmp;
    @PostConstruct
    public void init() {
        CommonArgs.testBaseDir = testBaseDirTmp;
        CommonArgs.locustExecutor = locustExecutorTmp;
        CommonArgs.harDir = harDirTmp;
        CommonArgs.APIFile = APIFileTmp;
    }
}
