package cn.enilu.flash.bean.entity.project;


import cn.enilu.flash.bean.entity.BaseEntity;
import lombok.Data;
import org.hibernate.annotations.Table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;

@Entity(name="t_pro_test_activity")
@Table(appliesTo = "t_pro_test_activity",comment = "项目测试活动")
@Data

public class testActivity extends BaseEntity {
    @Column(columnDefinition = "bigint COMMENT '项目名'")
    private Long projectName;
    @Column(columnDefinition = "VARCHAR(32) COMMENT '测试活动名'")
    private String testactivityName;
    @Column(columnDefinition = "TEXT COMMENT '测试活动简介'")
    private String testactivityIntroduction;
    @Column(columnDefinition = "VARCHAR(10) COMMENT '任务数'")
    private String tasks;
}

