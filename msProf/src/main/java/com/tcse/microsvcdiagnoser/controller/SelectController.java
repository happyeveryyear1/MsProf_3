package com.tcse.microsvcdiagnoser.controller;


import com.tcse.microsvcdiagnoser.response.Rets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@Slf4j
@RestController
@RequestMapping(value="/select", produces = "application/json; charset=utf-8")
public class SelectController {
    @PostMapping
    public Object select(){
        // [taskDto] "TaskDto(taskName=T2021-0923-1705, projectName=iTrust, newVersion=v1_0, oldVersion=, artifactInfos=[ArtifactDto(name=iTrust, version=1.0)], testGit=https://gitee.com/William-WZL/itrust-test.git, testCaseDir=, testType=robot)"
        
        return Rets.success();
    }
}
