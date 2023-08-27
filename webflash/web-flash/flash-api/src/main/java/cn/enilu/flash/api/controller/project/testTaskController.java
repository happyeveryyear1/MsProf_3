package cn.enilu.flash.api.controller.project;

import cn.enilu.flash.bean.entity.project.*;
import cn.enilu.flash.bean.entity.system.User;
import cn.enilu.flash.bean.enumeration.Permission;
import cn.enilu.flash.bean.vo.query.SearchFilter;
import cn.enilu.flash.service.project.*;

import cn.enilu.flash.bean.core.BussinessLog;
import cn.enilu.flash.bean.constant.factory.PageFactory;
import cn.enilu.flash.bean.dictmap.CommonDict;
import cn.enilu.flash.bean.enumeration.BizExceptionEnum;
import cn.enilu.flash.bean.exception.ApplicationException;
import cn.enilu.flash.bean.vo.front.Rets;

import cn.enilu.flash.service.system.UserService;
import cn.enilu.flash.utils.*;
import cn.enilu.flash.utils.factory.Page;

import cn.enilu.flash.warpper.UserWarpper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RestController
@RequestMapping("/pro/task")
public class testTaskController {
	private  Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private testTaskService testTaskService;
	@Autowired
	private testActivityService testActivityService;
	@Autowired
	private svcAnalysisService svcAnalysisServiceVar;
	@Autowired
	private  testCaseService testCaseServiceVar;
	@Autowired
	private HarSwaggerDataService harSwaggerDataService;
	@Autowired
	private UserService userService;
	@Autowired
	private StatisticDataService statisticDataService;
//	@RequestMapping(value = "/count",method = RequestMethod.GET)
//	public Object count(@RequestParam(required = false) String testactivityName) {
//		long num = testTaskService.count((SearchFilter.build("testactivityName", SearchFilter.Operator.EQ, testactivityName)));
//		return Rets.success(num);
//	}
	
	@RequestMapping(value = "/listAll",method = RequestMethod.GET)
	public Object listAll(@RequestParam(required = false) String taskName,
						  @RequestParam(required = false) String testactivityName) {
		List<testTask> listAll;
		if(StringUtil.isNotEmpty(taskName)) {
			listAll = testTaskService.queryAll(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		} else {
			if(StringUtil.isNotEmpty(testactivityName)){
				listAll = testTaskService.queryAll(SearchFilter.build("testactivityName", SearchFilter.Operator.EQ, testactivityName));
			}else {
				listAll = testTaskService.queryAll();
			}
		}
		return Rets.success(listAll);
	}
	
	@RequestMapping(value = "/list",method = RequestMethod.GET)
	public Object list(@RequestParam(required = false) String testactivityName,
					   @RequestParam(required = false) String taskName,
					   @RequestParam(required = false) String versionNum,
					   @RequestParam(required = false) String tester,
					   @RequestParam(required = false) String beginTime,
					   @RequestParam(required = false) String endTime,
					   @RequestParam(required = false) String analyseStatus,
					   @RequestParam(required = false) String evaluateStatus,
					   @RequestParam(required = false) String funcStatus) {
		Page<testTask> page = new PageFactory<testTask>().defaultPage();
		if(StringUtil.isNotEmpty(beginTime)) {
			page.addFilter("createTime", SearchFilter.Operator.GTE, DateUtil.parse(beginTime,"yyyyMMddHHmmss"));
		}
		if(StringUtil.isNotEmpty(endTime)) {
			page.addFilter("createTime", SearchFilter.Operator.LTE, DateUtil.parse(endTime,"yyyyMMddHHmmss"));
		}
		if(StringUtil.isNotEmpty(testactivityName)){
			page.addFilter(SearchFilter.build("testactivityName", SearchFilter.Operator.EQ, testactivityName));
		}
		if(StringUtil.isNotEmpty(taskName)){
			String decode_taskName = URLDecoder.decode(taskName);
			page.addFilter(SearchFilter.build("taskName", SearchFilter.Operator.LIKE, decode_taskName));
		}
		if(StringUtil.isNotEmpty(versionNum)){
			page.addFilter(SearchFilter.build("versionNum", SearchFilter.Operator.EQ, versionNum));
		}
		if(StringUtil.isNotEmpty(tester)) {
			page.addFilter(SearchFilter.build("tester", SearchFilter.Operator.LIKE, tester));
		}
		if(StringUtil.isNotEmpty(analyseStatus)) {
			page.addFilter(SearchFilter.build("analyseStatus", SearchFilter.Operator.EQ, analyseStatus));
		}
		if(StringUtil.isNotEmpty(evaluateStatus)) {
			page.addFilter(SearchFilter.build("evaluateStatus", SearchFilter.Operator.EQ, evaluateStatus));
		}
		if(StringUtil.isNotEmpty(funcStatus)) {
			page.addFilter(SearchFilter.build("funcStatus", SearchFilter.Operator.EQ, funcStatus));
		}
		page = testTaskService.queryPage(page);
		List list = (List) new UserWarpper(BeanUtil.objectsToMaps(page.getRecords())).warp();
		page.setRecords(list);
		return Rets.success(page);
	}
//	@RequestMapping(value = "/saveTask",method = RequestMethod.POST)
//	@BussinessLog(value = "编辑项目测试信息表", key = "name",dict= CommonDict.class)
//	public Object save(@ModelAttribute testTask tProTask){
//		if(tProTask.getId()==null){
//			List<testTask> listAll = testTaskService.queryAll();
//			for(int i = 0;i<listAll.size();i++) {
//				if (tProTask.getTaskName().equals(listAll.get(i).getTaskName())) {
//					return Rets.failure("任务名已被使用，请重新输入");
//				}
//			}
//			// 测试活动数加一
//			testActivity tProTestActivity = new testActivity();
//			List<testActivity> list = testActivityService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, tProTask.getTestactivityName()));
//			tProTestActivity.setId(list.get(0).getId());
//			tProTestActivity.setProjectName(list.get(0).getProjectName());
//			tProTestActivity.setTestactivityIntroduction(list.get(0).getTestactivityIntroduction());
//			tProTestActivity.setTestactivityName((list.get(0).getTestactivityName()));
//			tProTestActivity.setTasks(String.valueOf(Integer.parseInt(list.get(0).getTasks())+1));
//			testActivityService.update(tProTestActivity);
//			testTaskService.insert(tProTask); // 任务表插入一条数据
//		}else {
//			testTaskService.update(tProTask);
//		}
//		return Rets.success();
//	}
	
	@RequestMapping(value = "/addTask",method = RequestMethod.POST)
	@BussinessLog(value = "新增项目测试信息表", key = "name",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.TASKS_ADD})
	public Object add(@ModelAttribute testTask tProTask){
		List<testTask> listAll = testTaskService.queryAll();
		for(int i = 0;i<listAll.size();i++) {
			if (tProTask.getTaskName().equals(listAll.get(i).getTaskName())) {
				return Rets.failure("任务名已被使用，请重新输入");
			}
		}
		// 测试活动数加一
		testActivity tProTestActivity = new testActivity();
		List<testActivity> list = testActivityService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, tProTask.getTestactivityName()));
		tProTestActivity.setId(list.get(0).getId());
		tProTestActivity.setProjectName(list.get(0).getProjectName());
		tProTestActivity.setTestactivityIntroduction(list.get(0).getTestactivityIntroduction());
		tProTestActivity.setTestactivityName((list.get(0).getTestactivityName()));
		tProTestActivity.setTasks(String.valueOf(Integer.parseInt(list.get(0).getTasks())+1));
		testActivityService.update(tProTestActivity);
		testTaskService.insert(tProTask); // 任务表插入一条数据
		
		// 添加harSwagger数据行
		HarSwaggerData harSwaggerData = new HarSwaggerData();
		harSwaggerData.setTaskName(tProTask.getTaskName());
		harSwaggerDataService.insert(harSwaggerData);
		
		return Rets.success();
	}
	
	@RequestMapping(value = "/editTask",method = RequestMethod.POST)
	@BussinessLog(value = "编辑项目测试信息表", key = "name",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.TASKS_EDIT})
	public Object edit(@ModelAttribute testTask tProTask){
		testTaskService.update(tProTask);
		return Rets.success();
	}
	
	@RequestMapping(value = "/saveResult", method = RequestMethod.GET)
	@BussinessLog(value = "保存结果", key = "name",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.TASKS_EDIT})
	public Object saveResult(@RequestParam(required = false) String taskName,
							 @RequestParam(required = false) String executionTime,
							 @RequestParam(required = false) String executionResult,
							 @RequestParam(required = false) String exeStatus,
							 @RequestParam(required = false) String evaluateStatus,
							 @RequestParam(required = false) String analyseStatus,
							 @RequestParam(required = false) String harStatus,
							 @RequestParam(required = false) String funcStatus) throws ParseException {
		testTask tProTask = new testTask();
		List<testTask> list = testTaskService.queryAll(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		tProTask.setId(list.get(0).getId());
		tProTask.setCreateTime(list.get(0).getCreateTime());
		if(StringUtil.isNotEmpty(executionTime)) {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = ft.parse(executionTime);
			tProTask.setExecutionTime(date);
		}else {
			tProTask.setExecutionTime(list.get(0).getExecutionTime());
		}
		tProTask.setTaskIntroduction(list.get(0).getTaskIntroduction());
		tProTask.setTaskName(list.get(0).getTaskName());
		tProTask.setTestConfiguration(list.get(0).getTestConfiguration());
		if(StringUtil.isNotEmpty(executionResult)) {
			tProTask.setTestResult(executionResult);
		}else {
			tProTask.setTestResult(list.get(0).getTestResult());
		}
		tProTask.setTestactivityName(list.get(0).getTestactivityName());
		tProTask.setTester(list.get(0).getTester());
		tProTask.setVersionNum(list.get(0).getVersionNum());
		tProTask.setDirectory(list.get(0).getDirectory());
		tProTask.setArtifactInfos(list.get(0).getArtifactInfos());
		tProTask.setOldVersion(list.get(0).getOldVersion());
		if(StringUtil.isNotEmpty(exeStatus)) {
			tProTask.setExeStatus(exeStatus);
		}else {
			tProTask.setExeStatus(list.get(0).getExeStatus());
		}
		if(StringUtil.isNotEmpty(evaluateStatus)) {
			tProTask.setEvaluateStatus(evaluateStatus);
		}else {
			tProTask.setEvaluateStatus(list.get(0).getEvaluateStatus());
		}
		if(StringUtil.isNotEmpty(analyseStatus)) {
			tProTask.setAnalyseStatus(analyseStatus);
		}else {
			tProTask.setAnalyseStatus(list.get(0).getAnalyseStatus());
		}
		tProTask.setTestcases(list.get(0).getTestcases());
		if(StringUtil.isNotEmpty(harStatus)) {
			tProTask.setHarStatus(harStatus);
		}else {
			tProTask.setHarStatus(list.get(0).getHarStatus());
		}
		if(StringUtil.isNotEmpty(funcStatus)) {
			tProTask.setFuncStatus(funcStatus);
		}else {
			tProTask.setFuncStatus(list.get(0).getFuncStatus());
		}
		System.out.println(tProTask);
		
		// 创建服务分析记录
		if(tProTask.getExecutionTime() != null){
			svcAnalysis svcAnalysisTmp = new svcAnalysis();
			
			svcAnalysisTmp.setTaskName(tProTask.getTaskName());
			svcAnalysisTmp.setCreateTime(tProTask.getExecutionTime());
			svcAnalysisTmp.setIsAnalysised("0");
			svcAnalysisServiceVar.insert(svcAnalysisTmp);
			
			List<testActivity> activityName = testActivityService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, tProTask.getTestactivityName()));
			svcAnalysisTmp.setTestActivityName(activityName.get(0).getTestactivityName());
			
			// List<svcAnalysis> svcAnalysisList = svcAnalysisServiceVar.queryAll();
			// svcAnalysisList.add(0, svcAnalysisTmp);
			
			testTaskService.update(tProTask);
		}
		
		return Rets.success();
	}
	
	@RequestMapping(value = "/tmp",method = RequestMethod.GET)
	public Object tmp(Long id){
		svcAnalysis svcAnalysisTmp = new svcAnalysis();
		List<svcAnalysis> svcAnalysisList = svcAnalysisServiceVar.queryAll();
		List<testCase> tmp = testCaseServiceVar.queryAll();
		List<svcAnalysis> svcAnalysisList2 = svcAnalysisServiceVar.queryAll(SearchFilter.build("taskName", SearchFilter.Operator.EQ, "task1"));
		return null;
	}
	
	@RequestMapping(value = "/delete",method = RequestMethod.DELETE)
	@BussinessLog(value = "删除项目测试信息表", key = "id",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.TASKS_DELETE})
	public Object remove(Long id){
		if (id == null) {
			throw new ApplicationException(BizExceptionEnum.REQUEST_NULL);
		}
		// 测试活动数减一
		List<testTask> list1 = testTaskService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, id));
		testActivity tProTestActivity = new testActivity();
		List<testActivity> list = testActivityService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, list1.get(0).getTestactivityName()));
		tProTestActivity.setId(list.get(0).getId());
		tProTestActivity.setProjectName(list.get(0).getProjectName());
		tProTestActivity.setTestactivityIntroduction(list.get(0).getTestactivityIntroduction());
		tProTestActivity.setTestactivityName((list.get(0).getTestactivityName()));
		tProTestActivity.setTasks(String.valueOf(Integer.parseInt(list.get(0).getTasks())-1));
		testActivityService.update(tProTestActivity);
		testTaskService.delete(id); // 删除一条数据
		return Rets.success();
	}
	
	private final String BASEURI = "http://localhost:8083/";
//	private final String BASEURI = "http://39.104.118.163:8083";
	
	//	@RequestMapping(value = "/executeTask", method = RequestMethod.POST)
//	public Object execute(@RequestBody Map pdata) {
//		String REQUEST_URI = BASEURI + "/startTestCase/";
////		Map<String, String> urlParameters = new HashMap<>();
//		System.out.println(pdata);
//		RestTemplate restT = new RestTemplate();
//		Map quote = restT.postForObject(REQUEST_URI, pdata, Map.class);
//		return Rets.success(quote);
//	}
	@RequestMapping(value = "/executeTask", method = RequestMethod.GET)
	public Object execute() {
		String REQUEST_URI = SVCDIAGNOSER_URL + "/execute";
		RestTemplate restT = new RestTemplate();
		System.out.println(REQUEST_URI);
		Map quote = restT.getForObject(REQUEST_URI, Map.class);
		if(quote.get("msg").equals("成功")){
			return Rets.success();
		}
		return Rets.failure("执行失败");
	}
	
	@RequestMapping(value = "/getJobResult", method = RequestMethod.GET)
	public Object getJobResult(@RequestParam(required = false) String jobId) {
		String REQUEST_URI = BASEURI + "/test/result/getJobResult/";
		String requestUri = REQUEST_URI + "?jobId={jobId}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("jobId", jobId);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri, null, Map.class, urlParameters);
		return Rets.success(quote);
	}
	
	@RequestMapping(value = "/getJobResultFile", method = RequestMethod.GET)
	public Object getJobResultFile(@RequestParam(required = false) String filePath) {
		String REQUEST_URI = BASEURI + "/test/result/getJobResultFile/";
		String requestUri = REQUEST_URI + "?filePath={filePath}";
		Map<String, String> urlParameters = new HashMap<>();
		urlParameters.put("filePath", filePath);
		RestTemplate restT = new RestTemplate();
		Map quote = restT.postForObject(requestUri, null, Map.class, urlParameters);
		return Rets.success(quote);
	}
	
	// private final String SVCDIAGNOSER_URL = "http://127.0.0.1:8347";   // 微服务性能分析
	@Value("${SVCDIAGNOSER_URL}")
	private String SVCDIAGNOSER_URL = "http://172.17.0.2:8347";   // 科技云性能分析
	
	@RequestMapping(value = "/mvcDiagnoserAnalyzeHar", method = RequestMethod.POST)
	public Object mvcDiagnoserAnalyzeHar(@RequestParam(required = true) String taskName) {
		
		
		// 请求diagnoser分析Har
		String analyzeHarDataUrl = SVCDIAGNOSER_URL + "mvcDiagnoserAnalyzeHar";
		RestTemplate restT = new RestTemplate();
		testTask testTaskTmp = testTaskService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		String baseUrl = testTaskTmp.getBaseUrl();
		Map quote = restT.postForObject(analyzeHarDataUrl,baseUrl, Map.class);
		if(quote.get("msg").equals("成功")){
			LinkedHashMap data = (LinkedHashMap) quote.get("data");
			String harJson = (String) data.get("harJson");
			String requestMap = (String) data.get("requestMap");
			
			// 刷新数据库标记
			testTaskTmp = testTaskService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
			testTaskTmp.setHarAnalyzeStatus("1");
			testTaskService.update(testTaskTmp);
			
			// 记录结果
			HarSwaggerData harSwaggerData = harSwaggerDataService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
			harSwaggerData.setHarJSON(harJson);
			harSwaggerData.setRequestMap(requestMap);
			harSwaggerDataService.update(harSwaggerData);
			return Rets.success();
		}else{
			return Rets.failure("解析失败");
		}
	}
	
	@RequestMapping(value = "/mvcDiagnoserAnalyzeSwagger", method = RequestMethod.POST)
	public Object mvcDiagnoserAnalyzeSwagger(@RequestParam(required = true) String taskName) {
		
		// 请求diagnoser分析Har
		String analyzeHarDataUrl = SVCDIAGNOSER_URL + "mvcDiagnoserAnalyzeSwagger";
		RestTemplate restT = new RestTemplate();
		Map quote = restT.getForObject(analyzeHarDataUrl,Map.class);
		
		if(quote.get("msg").equals("成功")){
			LinkedHashMap data = (LinkedHashMap) quote.get("data");
			String resList = (String) data.get("resList");
			String resProdConsDep = (String) data.get("resProdConsDep");
			
			// 刷新数据库标记
			testTask testTaskTmp = testTaskService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
			testTaskTmp.setSwaggerAnalyzeStatus("1");
			testTaskService.update(testTaskTmp);
			
			// 记录结果
			HarSwaggerData harSwaggerData = harSwaggerDataService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
			harSwaggerData.setResList(resList);
			harSwaggerData.setResProdConsDep(resProdConsDep);
			harSwaggerDataService.update(harSwaggerData);
			return Rets.success();
		}else{
			return Rets.failure("解析失败");
		}
	}
	
	// @RequestMapping(value = "/getAllExecInfo", method = RequestMethod.POST)
	// public Object getAllExecInfo(@RequestParam(required = true) String taskName) {
	//
	// 	// 请求diagnoser分析Har
	// 	// String getAllExecInfoUrl = "https://8a75d1b4-80a7-432a-8981-4cfad44cb3ff.mock.pstmn.io/getAllExecInfo";
	// 	String getAllExecInfoUrl = SVCDIAGNOSER_URL + "/Statistic/getAllExecInfo";
	// 	RestTemplate restT = new RestTemplate();
	// 	Map quote = restT.getForObject(getAllExecInfoUrl,Map.class);
	// 	System.out.println(quote);
	// 	if(quote.get("msg").equals("成功")){
	// 		LinkedHashMap data = (LinkedHashMap) quote.get("data");
	// 		String svcList = (String) data.get("svcList");
	// 		ObjectMapper mapper = new ObjectMapper();
	// 		String interfaceListJson = null;
	// 		try {
	// 			interfaceListJson = mapper.writeValueAsString(data.get("interfaceListJson"));
	// 		} catch (JsonProcessingException e) {
	// 			e.printStackTrace();
	// 		}
	// 		// interfaceListJson = interfaceListJson.replace("\\\"", "\"");
	//
	// 		// 数据库存储
	// 		StatisticData statisticData = new StatisticData();
	// 		statisticData.setTaskName(taskName);
	// 		statisticData.setSvcList(svcList);
	// 		statisticData.setInterfaceListJson(interfaceListJson);
	// 		statisticDataService.insert(statisticData);
	//
	// 		return Rets.success("获取数据成功");
	// 	}else{
	// 		return Rets.failure("解析失败");
	// 	}
	// }
	
	@RequestMapping(value = "/getAllExecInfoFromDB", method = RequestMethod.POST)
	public Object getAllExecInfoFromDB(@RequestParam(required = true) String taskName) {
		StatisticData statisticData = statisticDataService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		String svcList = statisticData.getSvcList();
		return Rets.success(svcList);
	}
	
	@RequestMapping(value = "/getInterfaceData", method = RequestMethod.POST)
	public Object getInterfaceData(@RequestParam(required = true) String taskName, String interfaceName) {
		
		// 数据库存储
		StatisticData statisticData = statisticDataService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		String interfaceListJson = statisticData.getInterfaceListJson();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			// interfaceListJson = interfaceListJson.substring(1, interfaceListJson.length()-1);
			JsonNode jsonNode = objectMapper.readTree(interfaceListJson);
			JsonNode interfaceData = jsonNode.get(interfaceName);
			return Rets.success(interfaceData.toString());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return Rets.failure("查询失败");
	}
	
	// 根因定位
	@RequestMapping(value = "/getAllExecInfo", method = RequestMethod.POST)
	public Object getAllExecInfo(@RequestParam(required = true) String taskName) {
		StatisticData statisticData = statisticDataService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		if(statisticData != null){
			String svcList = statisticData.getSvcList();
			return Rets.success(svcList);
		}
		// 异常检测
		String getAllExecInfoUrl = SVCDIAGNOSER_URL + "/collect/anomalyDetection";
		RestTemplate restT = new RestTemplate();
		Map quote = restT.getForObject(getAllExecInfoUrl,Map.class);
		System.out.println(quote);
		if(quote.get("msg").equals("成功")){
			String getAllExecInfo = SVCDIAGNOSER_URL + "/Statistic/getAllExecInfo";
			restT = new RestTemplate();
			quote = restT.getForObject(getAllExecInfo,Map.class);
			LinkedHashMap data = (LinkedHashMap) quote.get("data");
			String svcList = (String) data.get("svcList");
			ObjectMapper mapper = new ObjectMapper();
			String interfaceListJson = null;
			try {
				interfaceListJson = mapper.writeValueAsString(data.get("interfaceListJson"));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			// interfaceListJson = interfaceListJson.replace("\\\"", "\"");
			
			// 数据库存储
			statisticData = new StatisticData();
			statisticData.setTaskName(taskName);
			statisticData.setSvcList(svcList);
			statisticData.setInterfaceListJson(interfaceListJson);
			statisticDataService.insert(statisticData);
			
			return Rets.success(svcList);
		}else{
			return Rets.failure("异常检测失败");
		}
	}
	
	// 查询当前状态
	@RequestMapping(value = "/getCurrentStatus", method = RequestMethod.POST)
	public Object getCurrentStatus(@RequestParam(required = true) String taskName) {
		testTask testTaskTmp = testTaskService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		String stats = testTaskTmp.getExeStatus();
		if(stats.equals("1")){
			return Rets.success("1");
		}
		String getCurrentStatusUrl = SVCDIAGNOSER_URL + "/execute/getCurrentState";
		RestTemplate restT = new RestTemplate();
		Map quote = restT.getForObject(getCurrentStatusUrl,Map.class);
		System.out.println(quote);
		if(quote.get("msg").equals("成功")){
			String state = (String) quote.get("data");
			if(state.equals("1")){
				testTaskTmp = testTaskService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
				testTaskTmp.setExeStatus("1");
				
				// 存储测试信息
				String getTestsUrl = SVCDIAGNOSER_URL + "/collect/getTests";
				restT = new RestTemplate();
				quote = restT.getForObject(getTestsUrl,Map.class);
				if(quote.get("msg").equals("成功")) {
					String testInfo = (String) quote.get("data");
					logger.info("testInfo: {}", testInfo);
					testTaskTmp.setTestInfo(testInfo);
				}
				
				testTaskService.update(testTaskTmp);
				return Rets.success("1");
			}else{
				// 注意，这里没有考虑-1的情况，理应不存在
				return Rets.success("0");
			}
		}else{
			return Rets.failure("查询当前状态失败");
		}
	}
	
	// 查询当前状态
	@RequestMapping(value = "/anomalyDetect", method = RequestMethod.POST)
	public Object anomalyDetect(@RequestParam(required = true) String taskName) {
		String anomalyDetectionUrl = SVCDIAGNOSER_URL + "/collect/anomalyDetection";
		RestTemplate restT = new RestTemplate();
		Map quote = restT.getForObject(anomalyDetectionUrl,Map.class);
		System.out.println(quote);
		if(quote.get("msg").equals("成功")){
			return Rets.success("异常检测成功");
		}else{
			return Rets.failure("查询当前状态失败");
		}
	}
	
	@RequestMapping(value = "/getRootCause", method = RequestMethod.POST)
	public Object getRootCause(@RequestParam(required = true) String taskName) {
		testTask testTaskTmp = testTaskService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		if(testTaskTmp != null && testTaskTmp.getRootCauseData()!=null){
			String rootCauseData = testTaskTmp.getRootCauseData();
			return Rets.success(rootCauseData);
		}
		// 异常检测
		String getRootCauseDataUrl = SVCDIAGNOSER_URL + "/Rootcause/getRootCause";
		RestTemplate restT = new RestTemplate();
		Map quote = restT.getForObject(getRootCauseDataUrl,Map.class);
		System.out.println(quote);
		if(quote.get("msg").equals("成功")){
			String rootCauseData = (String) quote.get("data");
			testTaskTmp.setRootCauseData(rootCauseData);
			testTaskService.update(testTaskTmp);
			// 数据库存储
			return Rets.success(rootCauseData);
		}else{
			return Rets.failure("异常检测失败");
		}
	}
	
	@RequestMapping(value = "/getTests", method = RequestMethod.POST)
	public Object getTests(@RequestParam(required = true) String taskName) {
		// 执行结束，查询数据库
		testTask testTaskTmp = testTaskService.get(SearchFilter.build("taskName", SearchFilter.Operator.EQ, taskName));
		if(testTaskTmp != null && testTaskTmp.getTestInfo()!=null){
			String getTestInfo = testTaskTmp.getTestInfo();
			return Rets.success(getTestInfo);
		}
		// 执行未结束，访问Msprof
		String getTestsUrl = SVCDIAGNOSER_URL + "/collect/getTests";
		RestTemplate restT = new RestTemplate();
		Map quote = restT.getForObject(getTestsUrl,Map.class);
		System.out.println(quote);
		if(quote.get("msg").equals("成功")){
			String getTestInfo = (String) quote.get("data");
			// 数据库存储
			return Rets.success(getTestInfo);
		}else{
			return Rets.failure("获取测试信息失败");
		}
	}
}