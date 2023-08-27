package cn.enilu.flash.service.project;


import cn.enilu.flash.bean.entity.project.testActivity;
import cn.enilu.flash.dao.project.testActivityRepository;

import cn.enilu.flash.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class testActivityService extends BaseService<testActivity,Long,testActivityRepository>  {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private testActivityRepository testActivityRepository;

//    public int countByProjectName(String projectName){
//        int num = testActivityRepository.countByProjectName(projectName);
//        return num;
//    }

}

