package cn.enilu.flash.bean.entity.project;


import cn.enilu.flash.bean.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.Table;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity(name="t_pro_mvcanalysis")
@Table(appliesTo = "t_pro_mvcanalysis",comment = "服务分析")
@Data
public class svcAnalysis extends BaseEntity {
    
    @Column(columnDefinition = "VARCHAR(45) COMMENT '测试活动名'")
    private String testActivityName;
    @Column(columnDefinition = "VARCHAR(45) COMMENT '任务名'")
    private String taskName;
    @Column(columnDefinition = "VARCHAR(45) COMMENT '服务排序'")
    private String svcOrder;
    @Column(columnDefinition = "VARCHAR(255) COMMENT '服务名'")
    private String svcName;
    @Column(columnDefinition = "VARCHAR(45) COMMENT '服务版本'")
    private String svcVersion;
    @Column(columnDefinition = "VARCHAR(45) COMMENT '服务版本'")
    private String isAnalysised;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Column(columnDefinition = "DATETIME COMMENT '创建时间'")
    private Date createTime;
    
    
}
