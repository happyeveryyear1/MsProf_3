package com.tcse.microsvcdiagnoser.controller;


import com.tcse.microsvcdiagnoser.dependency.ExecedDataDependency;
import com.tcse.microsvcdiagnoser.response.Rets;
import com.tcse.microsvcdiagnoser.service.SwaggerDepService;
import com.tcse.microsvcdiagnoser.service.TestExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 测试执行、遗传算法
* */
@Slf4j
@RestController
@RequestMapping("/execute")
public class TestExecutorController {
    
    @Autowired
    private TestExecutorService testExecutorService;
    
    @GetMapping
    public Object execute(){
        testExecutorService.execute();
        return Rets.success("任务提交成功");
    }
    
    @GetMapping("/getCurrentState")
    public Object getCurrentState(){
        
        return Rets.success(Integer.toString(ExecedDataDependency.getExecState()));
    }
}
