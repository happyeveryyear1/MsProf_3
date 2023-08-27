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

@Entity(name="t_pro_case")
@Table(appliesTo = "t_pro_case",comment = "测试用例")
@Data

public class testCase extends BaseEntity {
    // @Column(columnDefinition = "bigint COMMENT '任务名'")
    private Long taskName;
    // @Column(columnDefinition = "VARCHAR(32) COMMENT '菜单模块'")
    private String menuModule;
    // @Column(columnDefinition = "VARCHAR(32) COMMENT '测试用例名'")
    private String testcaseName;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Column(columnDefinition = "DATETIME COMMENT '执行完成时间'")
    private Date executionTime;
    @Column(columnDefinition = "VARCHAR(32) COMMENT '结果'")
    private String executionResult;
    @Column(columnDefinition = "VARCHAR(32) COMMENT '花费时间'")
    private String costTime;
    @Column(columnDefinition = "VARCHAR(32) COMMENT '日志'")
    private String logId;
    @Column(columnDefinition = "VARCHAR(255) COMMENT '图片路径'")
    private String picPath;
    @Column(columnDefinition = "VARCHAR(255) COMMENT '视频路径'")
    private String videoPath;
    @Column(columnDefinition = "VARCHAR(255) COMMENT 'har文件路径'")
    private String harPath;
}
