package com.tcse.microsvcdiagnoser.controller;

import com.tcse.microsvcdiagnoser.response.Rets;
import com.tcse.microsvcdiagnoser.service.RobotScannerService;
import com.tcse.microsvcdiagnoser.util.CommonArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
@RestController
public class TestController {
    
    @Autowired
    private CommonArgs commonArgs;
    
    @Autowired
    private RobotScannerService robotScannerService;
    
    @PostMapping("/test/list/")
    public Object list(@RequestParam("git") String git,
                       @RequestParam(value = "dir", required = false)  String dir,
                       @RequestParam(value = "type", required = false, defaultValue = "cypress") String type){
        List<String> testCaseList = robotScannerService.scan(git);
        Map data = new HashMap();
        data.put("data", testCaseList);
        return Rets.success(testCaseList);
    }
    
    @GetMapping("/getArg")
    public Object getArg(){
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(CommonArgs.testBaseDir);
        tmp.add(commonArgs.locustExecutor);
        tmp.add(commonArgs.harDir);
        return tmp;
    }
}