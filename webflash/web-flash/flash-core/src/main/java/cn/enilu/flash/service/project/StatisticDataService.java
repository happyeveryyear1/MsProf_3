package cn.enilu.flash.service.project;


import cn.enilu.flash.bean.entity.project.HarSwaggerData;
import cn.enilu.flash.bean.entity.project.StatisticData;
import cn.enilu.flash.dao.project.HarSwaggerDataRepository;
import cn.enilu.flash.dao.project.StatisticDataRepository;
import cn.enilu.flash.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticDataService extends BaseService<StatisticData,Long, StatisticDataRepository>  {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private StatisticDataRepository statisticDataRepository;
    

}

