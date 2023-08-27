package com.tcse.microsvcdiagnoser.controller;

import com.tcse.microsvcdiagnoser.response.Rets;
import com.tcse.microsvcdiagnoser.service.HarDepService;
import com.tcse.microsvcdiagnoser.service.SwaggerDepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@Slf4j
@RestController
@RequestMapping("/dependencyAnalysis")
public class DenpendencyController {
    
    @Autowired
    private HarDepService harDepService;
    
    @Autowired
    private SwaggerDepService swaggerDepService;
    
    // swagger分析
    @PostMapping("swagger")
    public Object swaggerAnalysis(){
        return Rets.success();
    }
    
    // har分析
    @PostMapping("har")
    public Object harAnalysis(){
        return Rets.success();
    }
    
    
    
}
