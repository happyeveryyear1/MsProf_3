package cn.enilu.flash.bean.entity.project;


import cn.enilu.flash.bean.entity.BaseEntity;
import lombok.Data;
import org.hibernate.annotations.Table;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name="t_statistic_data")
@Table(appliesTo = "t_statistic_data",comment = "项目测试信息表")
@Data

public class StatisticData extends BaseEntity {
    @Column(columnDefinition = "VARCHAR(32) COMMENT '任务名'")
    private String taskName;
    @Column(columnDefinition = "LONGTEXT COMMENT '接口列表'")
    private String svcList;
    @Column(columnDefinition = "LONGTEXT COMMENT '接口统计信息'")
    private String interfaceListJson;
}
