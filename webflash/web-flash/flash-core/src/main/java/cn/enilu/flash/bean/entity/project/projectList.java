package cn.enilu.flash.bean.entity.project;


import cn.enilu.flash.bean.entity.BaseEntity;
import lombok.Data;
import org.hibernate.annotations.Table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;

@Entity(name="t_pro_list")
@Table(appliesTo = "t_pro_list",comment = "项目总表")
@Data

public class projectList extends BaseEntity {
    @Column(columnDefinition = "VARCHAR(32) COMMENT '项目名'")
    private String projectName;
    @Column(columnDefinition = "TEXT COMMENT '项目简介'")
    private String projectIntroduction;
    @Column(columnDefinition = "bigint COMMENT '项目负责人'")
    private Long projectLeader;
    @Column(columnDefinition = "VARCHAR(255) COMMENT '测试用例地址'")
    private String testcaseAddress;
    @Column(columnDefinition = "VARCHAR(255) COMMENT 'Har地址'")
    private String harAddress;
    @Column(columnDefinition = "VARCHAR(255) COMMENT 'Swagger地址'")
    private String swaggerAddress;
    @Column(columnDefinition = "VARCHAR(32) COMMENT 'sonarID'")
    private String sonarId;
    @Column(columnDefinition = "VARCHAR(10) COMMENT '测试活动数'")
    private String activities;
    @Column(columnDefinition = "VARCHAR(64) COMMENT '应用名'")
    private String applicationName;
    @Column(columnDefinition = "VARCHAR(64) COMMENT '部署计划'")
    private String deployPlan;
    @Column(columnDefinition = "VARCHAR(32) COMMENT '系统'")
    private String systemName;
}
