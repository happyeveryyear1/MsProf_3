package cn.enilu.flash.bean.entity.project;


import cn.enilu.flash.bean.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.Table;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;

@Entity(name="t_pro_har_swagger_data")
@Table(appliesTo = "t_pro_har_swagger_data",comment = "项目测试信息表")
@Data

public class HarSwaggerData extends BaseEntity {
    @Column(columnDefinition = "VARCHAR(32) COMMENT '任务名'")
    private String taskName;
    @Column(columnDefinition = "LONGTEXT COMMENT '当前请求列表'")
    private String harJSON;
    @Column(columnDefinition = "LONGTEXT COMMENT '请求依赖关系'")
    private String requestMap;
    @Column(columnDefinition = "LONGTEXT COMMENT '资源列表'")
    private String resList;
    @Column(columnDefinition = "LONGTEXT COMMENT '资源生产消费关系'")
    private String resProdConsDep;
}
