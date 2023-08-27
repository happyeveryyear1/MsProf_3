package cn.enilu.flash.api.controller.project;

import cn.enilu.flash.bean.entity.project.projectList;
import cn.enilu.flash.bean.entity.project.testActivity;
import cn.enilu.flash.bean.enumeration.Permission;
import cn.enilu.flash.bean.vo.query.SearchFilter;
import cn.enilu.flash.service.project.projectListService;
import cn.enilu.flash.service.project.testActivityService;

import cn.enilu.flash.bean.core.BussinessLog;
import cn.enilu.flash.bean.constant.factory.PageFactory;
import cn.enilu.flash.bean.dictmap.CommonDict;
import cn.enilu.flash.bean.enumeration.BizExceptionEnum;
import cn.enilu.flash.bean.exception.ApplicationException;
import cn.enilu.flash.bean.vo.front.Rets;

import cn.enilu.flash.utils.BeanUtil;
import cn.enilu.flash.utils.StringUtil;
import cn.enilu.flash.utils.factory.Page;

import cn.enilu.flash.warpper.ProjectWarpper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.util.List;

@RestController
@RequestMapping("/pro/test/activity")
public class testActivityController {
	private  Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private testActivityService testActivityService;
	@Autowired
	private projectListService projectListService;

//	public testActivityController(cn.enilu.flash.service.project.projectListService projectListService) {
//		this.projectListService = projectListService;
//	}


//	@RequestMapping(value = "/count",method = RequestMethod.GET)
//	public Object count(@RequestParam(required = false) String projectName) {
//		long num = testActivityService.countByProjectName(projectName);
//		long num = testActivityService.count((SearchFilter.build("projectName", SearchFilter.Operator.EQ, projectName)));
//		return Rets.success(num);
//	}

	@RequestMapping(value = "/listAll",method = RequestMethod.GET)
	public Object listAll(@RequestParam(required = false) String testactivityName,
						  @RequestParam(required = false) String testactivityID,
						  @RequestParam(required = false) String projectName) {
		List<testActivity> listAll;
		if (StringUtil.isNotEmpty(testactivityName)) {
			listAll = testActivityService.queryAll(SearchFilter.build("testactivityName", SearchFilter.Operator.EQ, testactivityName));
		}else if(StringUtil.isNotEmpty(testactivityID)){
			listAll = testActivityService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, testactivityID));
		}else if(StringUtil.isNotEmpty(projectName)){
			listAll = testActivityService.queryAll(SearchFilter.build("projectName", SearchFilter.Operator.EQ, projectName));
		}else {
			listAll = testActivityService.queryAll();
		}
		return Rets.success(listAll);
	}

	@RequestMapping(value = "/list",method = RequestMethod.GET)
	public Object list(@RequestParam(required = false) String projectName,
					   @RequestParam(required = false) String testactivityName,
					   @RequestParam(required = false) String id) {
	Page<testActivity> page = new PageFactory<testActivity>().defaultPage();
		if(StringUtil.isNotEmpty(projectName)){
			page.addFilter(SearchFilter.build("projectName", SearchFilter.Operator.EQ, projectName));
		}
		if(StringUtil.isNotEmpty(testactivityName)){
			String decode_activityName = URLDecoder.decode(testactivityName);
			page.addFilter(SearchFilter.build("testactivityName", SearchFilter.Operator.LIKE, decode_activityName));
		}
		if(StringUtil.isNotEmpty(id)){
			page.addFilter(SearchFilter.build("id", SearchFilter.Operator.EQ, id));
		}
		page = testActivityService.queryPage(page);
		List list = (List) new ProjectWarpper(BeanUtil.objectsToMaps(page.getRecords())).warp();
		page.setRecords(list);
		return Rets.success(page);
	}

//	@RequestMapping(value = "/listLike",method = RequestMethod.GET)
//	public Object listLike(@RequestParam(required = false) String projectName, @RequestParam(required = false) String testactivityName) {
//		Page<testActivity> page = new PageFactory<testActivity>().defaultPage();
//		if(StringUtil.isNotEmpty(projectName)){
//			page.addFilter(SearchFilter.build("projectName", SearchFilter.Operator.EQ, projectName));
//		}
//		if(StringUtil.isNotEmpty(testactivityName)){
//			page.addFilter(SearchFilter.build("testactivityName", SearchFilter.Operator.LIKE, testactivityName));
//		}
//		page = testActivityService.queryPage(page);
//		return Rets.success(page);
//	}


//	@RequestMapping(value = "/saveActivity",method = RequestMethod.POST)
//	@BussinessLog(value = "编辑项目测试活动", key = "name",dict= CommonDict.class)
//	public Object save(@ModelAttribute testActivity tProTestActivity){
//		if(tProTestActivity.getId()==null){
//			List<testActivity> listAll = testActivityService.queryAll();
//			for(int i = 0;i<listAll.size();i++) {
//				if (tProTestActivity.getTestactivityName().equals(listAll.get(i).getTestactivityName())) {
//					return Rets.failure("测试活动名已被使用，请重新输入");
//				}
//			}
//			// 关联项目测试活动数加一
//			projectList tProList = new projectList();
//			List<projectList> list = projectListService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, tProTestActivity.getProjectName()));
//			tProList.setId(list.get(0).getId());
//			tProList.setProjectIntroduction(list.get(0).getProjectIntroduction());
//			tProList.setProjectLeader(list.get(0).getProjectLeader());
//			tProList.setProjectName((list.get(0).getProjectName()));
//			tProList.setSonarId(list.get(0).getSonarId());
//			tProList.setTestcaseAddress(list.get(0).getTestcaseAddress());
//			tProList.setActivities(String.valueOf(Integer.parseInt(list.get(0).getActivities())+1));
//			tProList.setApplicationName(list.get(0).getApplicationName());
//			tProList.setDeployPlan(list.get(0).getDeployPlan());
//			tProList.setSystemName(list.get(0).getSystemName());
//			projectListService.update(tProList);
//			testActivityService.insert(tProTestActivity); // 插入测试活动数据
//		}else {
//			testActivityService.update(tProTestActivity);
//		}
//		List<testActivity> listAll = testActivityService.queryAll(SearchFilter.build("testactivityName", SearchFilter.Operator.EQ, tProTestActivity.getTestactivityName()));
//		return Rets.success(listAll.get(0).getId());
//	}

	@RequestMapping(value = "/addActivity",method = RequestMethod.POST)
	@BussinessLog(value = "编辑项目测试活动", key = "name",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.ACTIVITY_ADD})
	public Object add(@ModelAttribute testActivity tProTestActivity){
		List<testActivity> listAll = testActivityService.queryAll();
		for(int i = 0;i<listAll.size();i++) {
			if (tProTestActivity.getTestactivityName().equals(listAll.get(i).getTestactivityName())) {
				return Rets.failure("测试活动名已被使用，请重新输入");
			}
		}
			// 关联项目测试活动数加一
			projectList tProList = new projectList();
			List<projectList> list = projectListService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, tProTestActivity.getProjectName()));
			tProList.setId(list.get(0).getId());
			tProList.setProjectIntroduction(list.get(0).getProjectIntroduction());
			tProList.setProjectLeader(list.get(0).getProjectLeader());
			tProList.setProjectName((list.get(0).getProjectName()));
			tProList.setSonarId(list.get(0).getSonarId());
			tProList.setTestcaseAddress(list.get(0).getTestcaseAddress());
			tProList.setActivities(String.valueOf(Integer.parseInt(list.get(0).getActivities())+1));
			tProList.setApplicationName(list.get(0).getApplicationName());
			tProList.setDeployPlan(list.get(0).getDeployPlan());
			tProList.setSystemName(list.get(0).getSystemName());
			projectListService.update(tProList);
			testActivityService.insert(tProTestActivity); // 插入测试活动数据

		List<testActivity> list1 = testActivityService.queryAll(SearchFilter.build("testactivityName", SearchFilter.Operator.EQ, tProTestActivity.getTestactivityName()));
		return Rets.success(list1.get(0).getId());
	}

	@RequestMapping(value = "/editActivity",method = RequestMethod.POST)
	@BussinessLog(value = "编辑项目测试活动", key = "name",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.ACTIVITY_EDIT})
	public Object edit(@ModelAttribute testActivity tProTestActivity){
		testActivityService.update(tProTestActivity);
		return Rets.success();
	}

//	@RequestMapping(value = "/saveTaskNum",method = RequestMethod.GET)
//	public Object saveTaskNum(@RequestParam(required = false) String id,
//							  @RequestParam(required = false) String num){
//		testActivity tProTestActivity = new testActivity();
//		List<testActivity> list = testActivityService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, id));
//		tProTestActivity.setId(list.get(0).getId());
//		tProTestActivity.setProjectName(list.get(0).getProjectName());
//		tProTestActivity.setTestactivityIntroduction(list.get(0).getTestactivityIntroduction());
//		tProTestActivity.setTestactivityName((list.get(0).getTestactivityName()));
//		tProTestActivity.setTasks(num);
//		testActivityService.update(tProTestActivity);
//		return Rets.success();
//	}
	@RequestMapping(value = "/delete",method = RequestMethod.DELETE)
	@BussinessLog(value = "删除项目测试活动", key = "id",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.ACTIVITY_DELETE})
	public Object remove(Long id){
		if (id == null) {
			throw new ApplicationException(BizExceptionEnum.REQUEST_NULL);
		}
		// 项目测试活动数减一
		List<testActivity> list1 = testActivityService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, id));
		projectList tProList = new projectList();
		List<projectList> list = projectListService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, list1.get(0).getProjectName()));
		tProList.setId(list.get(0).getId());
		tProList.setProjectIntroduction(list.get(0).getProjectIntroduction());
		tProList.setProjectLeader(list.get(0).getProjectLeader());
		tProList.setProjectName((list.get(0).getProjectName()));
		tProList.setSonarId(list.get(0).getSonarId());
		tProList.setTestcaseAddress(list.get(0).getTestcaseAddress());
		tProList.setActivities(String.valueOf(Integer.parseInt(list.get(0).getActivities())-1));
		tProList.setApplicationName(list.get(0).getApplicationName());
		tProList.setDeployPlan(list.get(0).getDeployPlan());
		tProList.setSystemName(list.get(0).getSystemName());
		projectListService.update(tProList);
		testActivityService.delete(id); // 删除测试活动
		return Rets.success();
	}
}