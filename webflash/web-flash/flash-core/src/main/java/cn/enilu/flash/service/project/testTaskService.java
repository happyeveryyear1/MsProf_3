package cn.enilu.flash.service.project;


import cn.enilu.flash.bean.entity.project.testTask;
import cn.enilu.flash.dao.project.testTaskRepository;

import cn.enilu.flash.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class testTaskService extends BaseService<testTask,Long,testTaskRepository>  {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private testTaskRepository testTaskRepository;

}

