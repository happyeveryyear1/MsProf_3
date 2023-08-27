package cn.enilu.flash.api.controller.project;

import cn.enilu.flash.bean.entity.project.HarSwaggerData;
import cn.enilu.flash.bean.entity.project.svcAnalysis;
import cn.enilu.flash.bean.entity.project.testCase;
import cn.enilu.flash.bean.enumeration.Permission;
import cn.enilu.flash.bean.vo.query.SearchFilter;
import cn.enilu.flash.service.project.HarSwaggerDataService;
import cn.enilu.flash.service.project.svcAnalysisService;
import cn.enilu.flash.service.project.testCaseService;

import cn.enilu.flash.bean.core.BussinessLog;
import cn.enilu.flash.bean.constant.factory.PageFactory;
import cn.enilu.flash.bean.dictmap.CommonDict;
import cn.enilu.flash.bean.enumeration.BizExceptionEnum;
import cn.enilu.flash.bean.exception.ApplicationException;
import cn.enilu.flash.bean.vo.front.Rets;

import cn.enilu.flash.utils.DateUtil;
import cn.enilu.flash.utils.Maps;
import cn.enilu.flash.utils.StringUtil;
import cn.enilu.flash.utils.ToolUtil;
import cn.enilu.flash.utils.factory.Page;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;

@RestController
@RequestMapping("/pro/case")
public class testCaseController {
	private  Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private testCaseService testCaseService;
	@Autowired
	private svcAnalysisService svcAnalysisServiceVar;
	@Autowired
	private HarSwaggerDataService harSwaggerDataService;

	@RequestMapping(value = "/listAll",method = RequestMethod.GET)
	public Object listAll(@RequestParam(required = false) String taskName) {
		List<testCase> listAll = testCaseService.queryAll(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		return Rets.success(listAll);
	}

	@RequestMapping(value = "/list",method = RequestMethod.GET)
	public Object list(@RequestParam(required = false) String taskName) {
	Page<testCase> page = new PageFactory<testCase>().defaultPage();
		if(StringUtil.isNotEmpty(taskName)){
			page.addFilter(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		}
		page = testCaseService.queryPage(page);
		return Rets.success(page);
	}
//	@RequestMapping(value = "/saveCase",method = RequestMethod.POST)
//	@BussinessLog(value = "编辑测试用例", key = "name",dict= CommonDict.class)
//	@RequiresPermissions(value = {Permission.TASKS_ADD})
//	public Object save(@ModelAttribute testCase tProCase){
//		if(tProCase.getId()==null){
//			List<SearchFilter> filters = new ArrayList<>();
//			filters.add(SearchFilter.build("taskName", SearchFilter.Operator.EQ, tProCase.getTaskName()));
//			filters.add(SearchFilter.build("testcaseName", SearchFilter.Operator.EQ, tProCase.getTestcaseName()));
//			List<testCase> list = testCaseService.queryAll(filters);
//			if(list.isEmpty()){
//				testCaseService.insert(tProCase);
//			}else {
//				tProCase.setId(list.get(0).getId());
//				tProCase.setMenuModule(list.get(0).getMenuModule());
//				testCaseService.update(tProCase);
//			}
//		}else {
//			testCaseService.update(tProCase);
//		}
//		return Rets.success();
//	}
	@RequestMapping(value = "/addCase",method = RequestMethod.POST)
	@BussinessLog(value = "添加测试用例", key = "name",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.TASKS_ADD})
	public Object add(@ModelAttribute testCase tProCase){
		if(tProCase.getId()==null){
			List<SearchFilter> filters = new ArrayList<>();
			filters.add(SearchFilter.build("taskName", SearchFilter.Operator.EQ, tProCase.getTaskName()));
			filters.add(SearchFilter.build("testcaseName", SearchFilter.Operator.EQ, tProCase.getTestcaseName()));
			List<testCase> list = testCaseService.queryAll(filters);
			if(list.isEmpty()){
				testCaseService.insert(tProCase);
			}else {
				tProCase.setId(list.get(0).getId());
				tProCase.setMenuModule(list.get(0).getMenuModule());
				testCaseService.update(tProCase);
			}
		}else {
			testCaseService.update(tProCase);
		}
		return Rets.success();
	}

	@RequestMapping(value = "/editCase",method = RequestMethod.POST)
	@BussinessLog(value = "编辑测试用例", key = "name",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.TASKS_EDIT})
	public Object edit(@ModelAttribute testCase tProCase){
		if(tProCase.getId()==null){
			List<SearchFilter> filters = new ArrayList<>();
			filters.add(SearchFilter.build("taskName", SearchFilter.Operator.EQ, tProCase.getTaskName()));
			filters.add(SearchFilter.build("testcaseName", SearchFilter.Operator.EQ, tProCase.getTestcaseName()));
			List<testCase> list = testCaseService.queryAll(filters);
			if(list.isEmpty()){
				testCaseService.insert(tProCase);
			}else {
				tProCase.setId(list.get(0).getId());
				tProCase.setMenuModule(list.get(0).getMenuModule());
				testCaseService.update(tProCase);
			}
		}else {
			testCaseService.update(tProCase);
		}
		return Rets.success();
	}

//	@RequestMapping(value = "/changeCase", method = RequestMethod.POST)
//	@BussinessLog(value = "更改后存储测试用例", key = "name",dict= CommonDict.class)
//	public Object changeCase(@ModelAttribute testCase tProCase){
//		testCaseService.insert(tProCase);
//		return Rets.success();
//	}

	@RequestMapping(value = "/delete",method = RequestMethod.DELETE)
	@BussinessLog(value = "删除测试用例", key = "id",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.TASKS_EDIT})
	public Object remove(Long id){
		if (id == null) {
			throw new ApplicationException(BizExceptionEnum.REQUEST_NULL);
		}
		testCaseService.delete(id);
		return Rets.success();
	}

	// private final String BASEURI = "http://localhost:8345"; // 南网
	private final String BASEURI = "http://39.104.118.163:8345"; // 阿里云

	@RequestMapping(value = "/importAll", method = RequestMethod.POST)
	public Object importAll(@RequestParam(required = false) String git,
						    @RequestParam(required = false) String dir) {
		String REQUEST_URI = BASEURI + "/test/list/";
		String requestUri = REQUEST_URI + "?git={git}&dir={dir}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("git", git);
		urlParameters.put("dir", dir);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri, null, Map.class, urlParameters);
		return Rets.success(quote);
	}

//	@RequestMapping(value = "/selectCase", method = RequestMethod.POST)
//	public Object selectTestCase(@RequestParam(required = false) String git,
//								 @RequestParam(required = false) String dir,
//								 @RequestParam(required = false) String newVersion,
//								 @RequestParam(required = false) String oldVersion,
//								 @RequestParam(required = false) String projectName,
//								 @RequestBody Map pdata) {
//		String REQUEST_URI = BASEURI + "/selectUsingPOST";
//		String requestUri = REQUEST_URI + "?git={git}&dir={dir}&newVersion={newVersion}&oldVersion={oldVersion}&projectName={projectName}";
//		Map<String, String> urlParameters = new HashMap<>();
//		urlParameters.put("testGit", git);
//		urlParameters.put("testCaseDir", dir);
//		urlParameters.put("newVersion", newVersion);
//		urlParameters.put("oldVersion", oldVersion);
//		urlParameters.put("projectName", projectName);
//		System.out.println(pdata);
//		RestTemplate restT = new RestTemplate();
//		Map quote = restT.postForObject(requestUri, pdata, Map.class, urlParameters);
//		return Rets.success(quote);
//	}

	@RequestMapping(value = "/selectCase", method = RequestMethod.POST)
	public Object selectTestCase(@RequestBody Map pdata) {
		String REQUEST_URI = BASEURI + "/select/";
		System.out.println(pdata);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(REQUEST_URI,pdata,Map.class);
		return Rets.success(quote);
	}

	@RequestMapping(value = "/collectInfo", method = RequestMethod.POST)
	public Object collectInfo(@RequestBody Map pdata) {
		String REQUEST_URI = BASEURI + "/collect/";
		System.out.println(pdata);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(REQUEST_URI,pdata,Map.class);
		return Rets.success(quote);
	}

	@RequestMapping(value = "/projectInfo", method = RequestMethod.POST)
	public Object importArtifacts(@RequestParam(required = false) String projectName,
							@RequestParam(required = false) String projectVersion) {
		String REQUEST_URI = BASEURI + "/project/info/";
		String requestUri = REQUEST_URI + "?projectName={projectName}&projectVersion={projectVersion}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("projectName", projectName);
		urlParameters.put("projectVersion", projectVersion);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri, null, Map.class, urlParameters);
		return Rets.success(quote);
	}

	@RequestMapping(value = "/deleteSelect", method = RequestMethod.GET)
	public Object deleteSelect(@RequestParam(required = false) String taskName) {
		String REQUEST_URI = BASEURI + "/delete";
		String requestUri = REQUEST_URI + "?taskName={taskName}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("taskName", taskName);

		RestTemplate restT = new RestTemplate();
		System.out.println(requestUri);
		Map quote = restT.getForObject(requestUri, Map.class, urlParameters);
		return Rets.success(quote);
	}
	
	
	// private final String BASEURI1 = "http://localhost:8020";  // 南网
	private final String DIAGNOSER_URL = "http://172.17.0.5:8347";  // 阿里云 微服务性能分析展示项目
	// private final String DIAGNOSER_URL = "http://39.104.118.163:8347";  // 阿里云 微服务性能分析器
	private final String VERSION_CTL_URL = "http://39.104.62.233:55556";	// trainticket及version-control.py所在服务器
	private final String BASEURI1 = "http://39.104.118.163:8085";   // 阿里云

	@RequestMapping(value = "/harAnalysis", method = RequestMethod.GET)
	public Object harAnalysis(@RequestParam(required = false) String projectName,
							  @RequestParam(required = false) String taskId) {
		String REQUEST_URI = BASEURI1 + "/test/haranalysis/";
		String requestUri = REQUEST_URI + "?projectName={projectName}&taskId={taskId}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("projectName", projectName);
		urlParameters.put("taskId", taskId);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri,null,Map.class,urlParameters);
		
		return Rets.success(quote);
	}
	
	String harviewerBaseUrl =  "http://39.104.118.163:49001/webapp/?har=examples/";
	
	@RequestMapping(value = "/resultCheck", method = RequestMethod.GET)
	public Object resultCheck(@RequestParam(required = false) String projectName,
							  @RequestParam(required = false) String taskId,
							  @RequestParam(required = false) String testId,
							  @RequestParam(required = false) String period,
							  @RequestParam(required = false) String endTime,
							  @RequestParam(required = false) String applicationName,
							  @RequestParam(required = false) String deployPlan) {
		String REQUEST_URI = BASEURI1 + "/test/resultcheck/";
		String requestUri = REQUEST_URI + "?projectName={projectName}&taskId={taskId}&testId={testId}&period={period}&endTime={endTime}&applicationName={applicationName}&deployPlan={deployPlan}";
		// Map<String, String> urlParameters = new HashMap<>();
		// urlParameters.put("projectName", projectName);
		// urlParameters.put("taskId", taskId);
		// urlParameters.put("testId", testId);
		// urlParameters.put("period", period);
		// urlParameters.put("endTime", endTime);
		// urlParameters.put("applicationName", applicationName);
		// urlParameters.put("deployPlan", deployPlan);
		// RestTemplate restT = new RestTemplate();
		// Map quote = restT.postForObject(requestUri,null,Map.class,urlParameters);
		
		Map<String, LinkedList> res = new HashMap<>();
		res.put("data", new LinkedList());
		File targetHar = new File("/home/intelliTest/diagnose/myharviewer/webapp/examples/" + taskId + "/" + testId + ".har");

		if(!targetHar.exists()){
			File resHarFolder = new File("/home/intelliTest/cyserver/data/har/" + taskId + "/");
			File targetFolder = new File("/home/intelliTest/diagnose/myharviewer/webapp/examples/" + taskId);
			try {
				FileUtils.copyDirectory(resHarFolder, targetFolder);
			} catch (IOException e) {
				logger.info("copy file err {}", e);
				e.printStackTrace();
			}

		}
		res.get("data").add(harviewerBaseUrl + taskId + "/" + testId + ".har");
		res.get("data").add("noPinpoint");
		
		
		return Rets.success(res);
		
		
	}

	@RequestMapping(value = "/pageInfo", method = RequestMethod.GET)
	public Object pageInfo(@RequestParam(required = false) String projectName,
						   @RequestParam(required = false) String taskId) {
		String REQUEST_URI = BASEURI1 + "/test/pageinfo/";
		String requestUri = REQUEST_URI + "?projectName={projectName}&taskId={taskId}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("projectName", projectName);
		urlParameters.put("taskId", taskId);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri,null,Map.class,urlParameters);
		return Rets.success(quote);
}

	@RequestMapping(value = "/pageNumber", method = RequestMethod.GET)
	public Object pageNumber(@RequestParam(required = false) String projectName,
						   @RequestParam(required = false) String taskId) {
		String REQUEST_URI = BASEURI1 + "/test/pagenumber/";
		String requestUri = REQUEST_URI + "?projectName={projectName}&taskId={taskId}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("projectName", projectName);
		urlParameters.put("taskId", taskId);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri,null,Map.class,urlParameters);
		return Rets.success(quote);
	}

	@RequestMapping(value = "/performanceAnalysis", method = RequestMethod.POST)
	public Object performanceAnalysis(@RequestParam(required = false) String projectName,
									  @RequestParam(required = false) String taskId,
									  @RequestParam(required = false) String applicationName) {
		String REQUEST_URI = BASEURI1 + "/test/performanceanalysis/";
		String requestUri = REQUEST_URI + "?projectName={projectName}&taskId={taskId}&applicationName={applicationName}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("projectName", projectName);
		urlParameters.put("taskId", taskId);
		urlParameters.put("applicationName", applicationName);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri,null,Map.class,urlParameters);
		return Rets.success(quote);
	}

	@RequestMapping(value = "/performanceResult", method = RequestMethod.POST)
	public Object performanceResult(@RequestParam(required = false) String projectName,
									  @RequestParam(required = false) String taskId) {
		String REQUEST_URI = BASEURI1 + "/test/performanceresult/";
		String requestUri = REQUEST_URI + "?projectName={projectName}&taskId={taskId}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("projectName", projectName);
		urlParameters.put("taskId", taskId);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri,null,Map.class,urlParameters);
		return Rets.success(quote);
	}

	@RequestMapping(value = "/performancePercent", method = RequestMethod.POST)
	public Object performancePercent(@RequestParam(required = false) String projectName,
									@RequestParam(required = false) String taskId) {
		String REQUEST_URI = BASEURI1 + "/test/performancepercent/";
		String requestUri = REQUEST_URI + "?projectName={projectName}&taskId={taskId}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("projectName", projectName);
		urlParameters.put("taskId", taskId);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri,null,Map.class,urlParameters);
		return Rets.success(quote);
	}

	@RequestMapping(value = "/functionAnalysis", method = RequestMethod.POST)
	public Object functionAnalysis(@RequestParam(required = false) String projectName,
								   @RequestParam(required = false) String taskId,
								   @RequestParam(required = false) String applicationName) {
		String REQUEST_URI = BASEURI1 + "/test/functionanalysis/";
		String requestUri = REQUEST_URI + "?projectName={projectName}&taskId={taskId}&applicationName={applicationName}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("projectName", projectName);
		urlParameters.put("taskId", taskId);
		urlParameters.put("applicationName", applicationName);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri,null,Map.class,urlParameters);
		return Rets.success(quote);
	}

	@RequestMapping(value = "/functionResult", method = RequestMethod.POST)
	public Object functionResult(@RequestParam(required = false) String projectName,
								 @RequestParam(required = false) String taskId) {
		String REQUEST_URI = BASEURI1 + "/test/functionanalysis/getResult";
		String requestUri = REQUEST_URI + "?projectName={projectName}&taskId={taskId}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("projectName", projectName);
		urlParameters.put("taskId", taskId);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri,null,Map.class,urlParameters);
		return Rets.success(quote);
	}
	
	@RequestMapping(value = "/getCurrentVersion", method = RequestMethod.POST)
	public Object getCurrentVersion(@RequestParam(required = false) String projectName,
									@RequestParam(required = false) String taskId) {
		String REQUEST_URI = VERSION_CTL_URL + "/version/currentVersion";
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(REQUEST_URI,null,Map.class);
		return Rets.success(quote);
	}
	
	@RequestMapping(value = "/getSpecVersion", method = RequestMethod.POST)
	public Object getSpecVersion(@RequestParam(required = false) String projectName,
									@RequestParam(required = false) String taskId) {
		String REQUEST_URI = VERSION_CTL_URL + "/version/specVersion";
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(REQUEST_URI,null,Map.class);
		return Rets.success(quote);
	}
	
	@RequestMapping(value = "/bottleneck", method = RequestMethod.POST)
	public Object bottleneck(@RequestParam(required = true) String taskName) {
		// 更改分析状态
		List<svcAnalysis> svcAnalysisList = svcAnalysisServiceVar.queryAll(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		svcAnalysis svcAnalysisTmplate = svcAnalysisList.get(0);
		svcAnalysisTmplate.setIsAnalysised("2");
		svcAnalysisServiceVar.update(svcAnalysisTmplate);
		
		
		// 分析
		String REQUEST_URI = DIAGNOSER_URL + "/collect/diagnose";
		Map<String, String> urlParameters = new HashMap<>();
		RestTemplate restT = new RestTemplate();
		// 分析结果
		Map quote = restT.postForObject(REQUEST_URI,null,Map.class,urlParameters);
		
		// // 等待
		// try {
		// 	Random r = new Random();
		// 	int time = r.nextInt(10) + 30;		// 90s后上下浮动60s
		// 	Thread.currentThread().sleep(time*1000);
		// } catch (InterruptedException e) {
		// 	e.printStackTrace();
		// }
		
		// 数据写入数据库
		svcAnalysisList = svcAnalysisServiceVar.queryAll(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		svcAnalysisTmplate = svcAnalysisList.get(0);
		svcAnalysisServiceVar.delete(svcAnalysisList.get(0).getId());
		ArrayList<String> data = (ArrayList) quote.get("data");
		for(int i = 0; i < data.size(); i++){
			svcAnalysis svcAnalysisTmp = new svcAnalysis();
			svcAnalysisTmp.setTaskName(svcAnalysisTmplate.getTaskName());
			svcAnalysisTmp.setCreateTime(svcAnalysisTmplate.getCreateTime());
			svcAnalysisTmp.setTestActivityName(svcAnalysisTmplate.getTestActivityName());
			svcAnalysisTmp.setSvcName(data.get(i));
			svcAnalysisTmp.setSvcOrder(Integer.toString(i+1));
			svcAnalysisTmp.setIsAnalysised("1");
			// 获取版本
			String[] svcNameList = data.get(i).split(":");
			RestTemplate restTmp = new RestTemplate();
			// Map<String, String> arg = new HashMap<>();
			// arg.put("podName", data.get(i));
			Map quoteTmp = restTmp.postForObject(VERSION_CTL_URL + "/version/specSvcVersion?podName="+svcNameList[0], null, Map.class,urlParameters);
			svcAnalysisTmp.setSvcVersion((String)quoteTmp.get("version"));
			svcAnalysisServiceVar.insert(svcAnalysisTmp);
			System.out.println(quoteTmp);
		}
		
		// 清空数据
		String clearDataUrl = DIAGNOSER_URL + "/collect/changeVersion";
		urlParameters = new HashMap<>();
		restT = new RestTemplate();
		quote = restT.getForObject(clearDataUrl,Map.class,urlParameters);

		
		return Rets.success(quote);
	}
	
	@RequestMapping(value = "/checkAnalysisResult", method = RequestMethod.POST)
	public Object checkAnalysisResult(@RequestParam(required = true) String taskName) {
		// 分析结果
		
		// 数据写入数据库
		List<svcAnalysis> svcAnalysisList = svcAnalysisServiceVar.queryAll(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		return Rets.success(svcAnalysisList);
	}
	
	@RequestMapping(value = "/getAllRecord", method = RequestMethod.POST)
	public Object getAllRecord() {
		List<svcAnalysis> svcAnalysisList = svcAnalysisServiceVar.queryAll();
		LinkedHashMap<String, LinkedList<String>> record = new LinkedHashMap<>();
		for(int i = 0; i < svcAnalysisList.size(); i++){
			svcAnalysis svcAnalysisTmp = svcAnalysisList.get(i);
			String taskName = svcAnalysisTmp.getTaskName();
			if(!record.containsValue(taskName)){
				LinkedList<String> recordEntity = new LinkedList<>();
				record.put(taskName, recordEntity);
				recordEntity.add(svcAnalysisTmp.getIsAnalysised());
				recordEntity.add(svcAnalysisTmp.getTaskName());
				recordEntity.add(svcAnalysisTmp.getTestActivityName());
				
				recordEntity.add(svcAnalysisTmp.getCreateTime().toString());
			}
		}
		
		return Rets.success(record);
	}
	
	
	@RequestMapping(value = "/getsvcAnaRecord", method = RequestMethod.POST)
	public Object getRecord(@RequestParam(required = true) String taskName) {
		List<svcAnalysis> svcAnalysisList2 = svcAnalysisServiceVar.queryAll(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		return Rets.success(svcAnalysisList2);
	}
	
	@RequestMapping(value = "/changeVersion", method = RequestMethod.POST)
	public Object changeVersion(@RequestParam(required = true) String yamlPath) {
		String REQUEST_URI = VERSION_CTL_URL + "/version/changeVersion?yamlPath="+yamlPath;
		Map<String, String> urlParameters = new HashMap<>();
		// urlParameters.put("yamlPath", yamlPath);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(REQUEST_URI,null,Map.class, urlParameters);
		return Rets.success(quote);
	}
	
	@RequestMapping(value = "/getHarDepData", method = RequestMethod.POST)
	public Object getHarDepData(@RequestParam(required = true) String taskName) {
		HarSwaggerData harSwaggerData = harSwaggerDataService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		HarSwaggerData harSwaggerDataTmp = new HarSwaggerData();
		harSwaggerDataTmp.setHarJSON(harSwaggerData.getHarJSON());
		harSwaggerDataTmp.setRequestMap(harSwaggerData.getRequestMap());
		return Rets.success(harSwaggerDataTmp);
	}
	
	@RequestMapping(value = "/getSwaggerDepData", method = RequestMethod.POST)
	public Object getSwaggerDepData(@RequestParam(required = true) String taskName) {
		HarSwaggerData harSwaggerData = harSwaggerDataService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		HarSwaggerData harSwaggerDataTmp = new HarSwaggerData();
		harSwaggerDataTmp.setResList(harSwaggerData.getResList());
		harSwaggerDataTmp.setResProdConsDep(harSwaggerData.getResProdConsDep());
		return Rets.success(harSwaggerDataTmp);
	}
}