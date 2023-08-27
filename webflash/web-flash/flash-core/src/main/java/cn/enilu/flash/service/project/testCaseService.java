package cn.enilu.flash.service.project;


import cn.enilu.flash.bean.entity.project.testCase;
import cn.enilu.flash.dao.project.testCaseRepository;

import cn.enilu.flash.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class testCaseService extends BaseService<testCase,Long,testCaseRepository>  {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private testCaseRepository testCaseRepository;

}

