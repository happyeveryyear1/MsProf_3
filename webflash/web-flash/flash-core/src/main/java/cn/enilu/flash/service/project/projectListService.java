package cn.enilu.flash.service.project;


import cn.enilu.flash.bean.entity.project.projectList;
import cn.enilu.flash.dao.project.projectListRepository;

import cn.enilu.flash.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class projectListService extends BaseService<projectList,Long,projectListRepository>  {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private projectListRepository projectListRepository;

}

