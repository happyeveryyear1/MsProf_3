package cn.enilu.flash.bean.enumeration;

/**
 * 权限编码列表<br>
 * 权限编码需要和菜单中的菜单编码一致
 * @author ：enilu
 * @date ：Created in 2019/7/31 11:05
 */
public interface Permission {

    //系统管理
    String CFG = "cfg";
    String CFG_EDIT = "cfgEdit";
    String CFG_DEL = "cfgDelete";
    String DICT = "dict";
    String DICT_EDIT = "dictEdit";
    String LOG = "log";
    String LOG_CLEAR = "logClear";
    String LOGIN_LOG = "loginLog";
    String LOGIN_LOG_CLEAR = "loginLogClear";
    String ROLE = "role";
    String ROLE_EDIT = "roleEdit";
    String ROLE_DEL = "roleDelete";
    String ROLE_ADD = "roleAdd";
    String TASK = "task";
    String TASK_EDIT = "taskEdit";
    String TASK_DEL = "taskDelete";
    String MENU = "menu";
    String MENU_EDIT = "menuEdit";
    String MENU_DEL = "menuDelete";
    String USER = "mgr";
    String USER_EDIT = "mgrEdit";
    String USER_DEL = "mgrDelete";
    String USER_ADD = "mgrAdd";
    String DEPT = "dept";
    String DEPT_EDIT = "deptEdit";
    String DEPT_DEL = "deptDelete";
    String DEPT_ADD = "deptAdd";


    //消息管理
    String MSG = "msg";
    String MSG_CLEAR = "msgClear";
    String MSG_SENDER = "msgSender";
    String MSG_SENDER_EDIT = "msgSenderEdit";
    String MSG_SENDER_DEL = "msgSenderDelete";
    String MSG_TPL = "msgTpl";
    String MSG_TPL_EDIT = "msgTplEdit";
    String MSG_TPL_DEL = "msgTplDelete";

    //CMS管理
    String ARTICLE = "article";
    String ARTICLE_EDIT = "editArticle";
    String ARTICLE_DEL = "deleteArticle";
    String BANNER = "banner";
    String BANNER_EDIT = "bannerEdit";
    String BANNER_DEL = "bannerDelete";
    String CHANNEL = "channel";
    String CHANNEL_EDIT = "channelEdit";
    String CHANNEL_DEL = "channelDelete";
    String CONTACTS = "contacts";
    String FILE = "file";
    String FILE_UPLOAD = "fileUpload";

    //项目管理
    String PROJECT_MGR = "projMgr";
    String PROJECT_LIST= "projectList";  // 项目总表
    String PROJECT_ADD = "proAdd";   //添加项目
    String PROJECT_EDIT = "proEdit";   //编辑项目
    String PROJECT_DELETE = "proDelete";   //删除项目
    String PROJECT_ACTIVITY = "projectActivity";  // 项目测试活动
    String ACTIVITY_ADD = "activityAdd";   // 添加测试活动
    String ACTIVITY_EDIT = "activityEdit";    // 编辑测试活动
    String ACTIVITY_DELETE = "activityDelete";   //删除测试活动
    String PROJECT_TASK = "testTask";  // 测试任务
    String TASKS_ADD = "tasksAdd";  // 添加任务
    String TASKS_EDIT = "tasksEdit";   // 编辑任务
    String TASKS_DELETE = "tasksDelete";  // 删除任务
    String TASKS_EXE = "tasksExe";   // 执行任务
    String EXECUTE_TASK = "executeTask"; // 任务执行页面
    String VIEW_MONITOR = "viewMonitor";  // 查看监控信息
    String TESTCASE_RESULT = "testcaseResult";  // 测试用例结果

    //综合质量评估
    String QUALITY_ASSESSMENT = "qualityAssessment"; // 综合质量评估
    String PRO_LIST = "proList";   // 项目列表
    String VERSION_LIST = "versionList";  // 版本列表
    String QUALITY_SYS_MGR =  "qualitySysMgr";   // 质量体系管理
    String EVALUATION_REPORT = "evaluationReport";  // 评估报告
    String COMPARISION = "comparision";  // 对比

    // 测试性能分析
    String PERFORMANCE_ANALYSE = "performanceAnalyse"; // 测试性能分析


}
