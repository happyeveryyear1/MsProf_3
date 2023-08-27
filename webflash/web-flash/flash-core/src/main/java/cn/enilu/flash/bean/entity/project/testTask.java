package cn.enilu.flash.bean.entity.project;


import cn.enilu.flash.bean.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.Table;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name="t_pro_task")
@Table(appliesTo = "t_pro_task",comment = "项目测试信息表")
@Data

public class testTask extends BaseEntity {
    @Column(columnDefinition = "VARCHAR(32) COMMENT '任务名'")
    private String taskName;
    @Column(columnDefinition = "bigint COMMENT '测试活动名'")
    private Long testactivityName;
    @Column(columnDefinition = "VARCHAR(32) COMMENT '版本号'")
    private String versionNum;
    @Column(columnDefinition = "TEXT COMMENT '任务简介'")
    private String taskIntroduction;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Column(columnDefinition = "DATETIME COMMENT '执行时间'")
    private Date executionTime;
    @Column(columnDefinition = "VARCHAR(255) COMMENT '测试机配置'")
    private String testConfiguration;
    @Column(columnDefinition = "VARCHAR(32) COMMENT '测试结果'")
    private String testResult;
    @Column(columnDefinition = "bigint COMMENT '测试员'")
    private Long tester;
    @Column(columnDefinition = "VARCHAR(64) COMMENT '测试用例目录'")
    private String directory;
    @Column(columnDefinition = "VARCHAR(32) COMMENT '旧版本号'")
    private String oldVersion;
    @Column(columnDefinition = "TEXT COMMENT '组件信息'")
    private String artifactInfos;
    @Column(columnDefinition = "VARCHAR(2) COMMENT '执行状态'")
    private String exeStatus;
    @Column(columnDefinition = "VARCHAR(2) COMMENT '评估状态'")
    private String evaluateStatus;
    @Column(columnDefinition = "VARCHAR(2) COMMENT '性能分析状态'")
    private String analyseStatus;
    @Column(columnDefinition = "VARCHAR(10) COMMENT '用例数'")
    private String testcases;
    @Column(columnDefinition = "VARCHAR(2) COMMENT 'har分析状态'")
    private String harStatus;
    @Column(columnDefinition = "VARCHAR(2) COMMENT 'har分析状态'")
    private String harAnalyzeStatus;
    @Column(columnDefinition = "VARCHAR(2) COMMENT 'swagger分析状态'")
    private String swaggerAnalyzeStatus;
    @Column(columnDefinition = "VARCHAR(2) COMMENT '功能缺陷分析状态'")
    private String funcStatus;
    @Column(columnDefinition = "LONGTEXT COMMENT '根因结果'")
    private String rootCauseData;
    @Column(columnDefinition = "LONGTEXT COMMENT '基础地址'")
    private String baseUrl;
    @Column(columnDefinition = "LONGTEXT COMMENT '测试信息'")
    private String testInfo;
}
