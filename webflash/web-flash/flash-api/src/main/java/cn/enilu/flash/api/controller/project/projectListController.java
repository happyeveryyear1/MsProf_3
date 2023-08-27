package cn.enilu.flash.api.controller.project;

import cn.enilu.flash.bean.entity.project.projectList;
import cn.enilu.flash.bean.enumeration.Permission;
import cn.enilu.flash.bean.vo.query.SearchFilter;
import cn.enilu.flash.service.project.projectListService;

import cn.enilu.flash.bean.core.BussinessLog;
import cn.enilu.flash.bean.constant.factory.PageFactory;
import cn.enilu.flash.bean.dictmap.CommonDict;
import cn.enilu.flash.bean.enumeration.BizExceptionEnum;
import cn.enilu.flash.bean.exception.ApplicationException;
import cn.enilu.flash.bean.vo.front.Rets;

import cn.enilu.flash.service.system.UserService;
import cn.enilu.flash.utils.BeanUtil;
import cn.enilu.flash.utils.StringUtil;
import cn.enilu.flash.utils.factory.Page;

import cn.enilu.flash.warpper.UserWarpper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/pro/list")
public class projectListController {
	private  Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private projectListService projectListService;
	private  UserService userService;

	@RequestMapping(value = "/listAll",method = RequestMethod.GET)
	public Object listAll(@RequestParam(required = false) String projectName,
						  @RequestParam(required = false) String id) {
		List<projectList> listAll;
		if(StringUtil.isNotEmpty(projectName)){
			listAll = projectListService.queryAll(SearchFilter.build("projectName", SearchFilter.Operator.EQ, projectName));
		}else if(StringUtil.isNotEmpty(id)) {
			listAll = projectListService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, id));
		}else {
		  	listAll = projectListService.queryAll();
		}

		return Rets.success(listAll);
	}

	@RequestMapping(method = RequestMethod.GET)
	public Object list(@RequestParam(required = false) String projectName,
					   @RequestParam(required = false) String projectLeader,
					   @RequestParam(required = false) String systemName) {
		Page<projectList> page = new PageFactory<projectList>().defaultPage();
		if(StringUtil.isNotEmpty(projectName)){
			String decode_projName = URLDecoder.decode(projectName);
			System.out.println(decode_projName);
			page.addFilter(SearchFilter.build("projectName", SearchFilter.Operator.LIKE, decode_projName));
		}
		if(StringUtil.isNotEmpty(projectLeader)){
			page.addFilter(SearchFilter.build("projectLeader", SearchFilter.Operator.LIKE, projectLeader));
		}
		if(StringUtil.isNotEmpty(systemName)){
			page.addFilter(SearchFilter.build("systemName", SearchFilter.Operator.EQ, systemName));
		}
		page = projectListService.queryPage(page);
		List list = (List) new UserWarpper(BeanUtil.objectsToMaps(page.getRecords())).warp();
		page.setRecords(list);
		return Rets.success(page);
	}

//	@RequestMapping(value = "/savePro", method = RequestMethod.POST)
//	@BussinessLog(value = "编辑项目总表", key = "name",dict= CommonDict.class)
//	public Object save(@ModelAttribute projectList tProList){
//		if(tProList.getId()==null){
//			List<projectList> listAll = projectListService.queryAll();
//			for(int i = 0;i<listAll.size();i++){
//				if(tProList.getProjectName().equals(listAll.get(i).getProjectName())){
//					return Rets.failure("项目名已被使用，请重新输入");
//				}
//			}
//			projectListService.insert(tProList);
//		}else {
//			projectListService.update(tProList);
//		}
//		List<projectList> listAll = projectListService.queryAll(SearchFilter.build("projectName", SearchFilter.Operator.EQ, tProList.getProjectName()));
//		return Rets.success(listAll.get(0).getId());
//	}

	@RequestMapping(value = "/addPro", method = RequestMethod.POST)
	@BussinessLog(value = "编辑项目总表", key = "name",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.PROJECT_ADD})
	public Object add(@ModelAttribute projectList tProList){
		List<projectList> listAll = projectListService.queryAll();
		for(int i = 0;i<listAll.size();i++){
			if(tProList.getProjectName().equals(listAll.get(i).getProjectName())){
				return Rets.failure("项目名已被使用，请重新输入");
			}
		}
		projectListService.insert(tProList);
		listAll = projectListService.queryAll(SearchFilter.build("projectName", SearchFilter.Operator.EQ, tProList.getProjectName()));
		// 此处svcDiagnoser创建项目
		return Rets.success(listAll.get(0).getId());
	}

	@RequestMapping(value = "/editPro", method = RequestMethod.POST)
	@BussinessLog(value = "编辑项目总表", key = "name",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.PROJECT_EDIT})
	public Object edit(@ModelAttribute projectList tProList){
		projectListService.update(tProList);
		return Rets.success();
	}

//	@RequestMapping(value = "/saveActivityNum", method = RequestMethod.GET)
//	public Object saveActivityNum(@RequestParam(required = false) String id,
//								  @RequestParam(required = false) String num){
//		projectList tProList = new projectList();
//		List<projectList> list = projectListService.queryAll(SearchFilter.build("id", SearchFilter.Operator.EQ, id));
//		tProList.setId(list.get(0).getId());
//		tProList.setProjectIntroduction(list.get(0).getProjectIntroduction());
//		tProList.setProjectLeader(list.get(0).getProjectLeader());
//		tProList.setProjectName((list.get(0).getProjectName()));
//		tProList.setSonarId(list.get(0).getSonarId());
//		tProList.setTestcaseAddress(list.get(0).getTestcaseAddress());
//		tProList.setActivities(num);
//		tProList.setApplicationName(list.get(0).getApplicationName());
//		tProList.setDeployPlan(list.get(0).getDeployPlan());
//		tProList.setSystemName(list.get(0).getSystemName());
//		projectListService.update(tProList);
//		return Rets.success();
//	}
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@BussinessLog(value = "删除项目总表", key = "id",dict= CommonDict.class)
	@RequiresPermissions(value = {Permission.PROJECT_DELETE})
	public Object remove(Long id){
		if (id == null) {
			throw new ApplicationException(BizExceptionEnum.REQUEST_NULL);
		}
		projectListService.delete(id);
		return Rets.success();
	}

	@RequestMapping(value = "/getApplicationName",method = RequestMethod.GET)
	public Object getApplicationName() {
		ArrayList<String> namelist = new ArrayList<String>();
		try{
				// 本地
//				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\gitDepository\\config.txt"), "UTF-8"));
				// 阿里云
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("/home/intelliTest/ybc/config.txt"), "UTF-8"));
//				// 南网
		    // BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("/root/ybc/config.txt"), "UTF-8"));
				String line;
				while ((line = in.readLine())!=null) {
					namelist.add(line);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		return Rets.success(namelist);
	}

}