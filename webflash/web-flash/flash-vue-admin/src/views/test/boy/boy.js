import { getAllTaskList, removeTask, getTaskList, saveTask, executeTask, saveResult, getJobResult } from '@/api/project/testTask'
import { getAllActivityList,saveTaskNum } from '@/api/project/testActivity'
import { getAllCaseList, importAllTestCase, selectTestCase, importArtifacts, deleteSelect, removeCase, saveCase, pageInfo,pageNumber } from '@/api/project/testCase'
import { parseTime } from '@/utils/index'
import { getAllProList } from '@/api/project/projectList'
import { getList } from '@/api/system/user' //关联测试员
import { newTask, delTask, measureVer } from '@/api/quality/version'
import { performanceResult, performanceAnalysis, harAnalysis } from '@/api/project/testCase'

export default {
  props: ['projectlist'],
  data() {
    return {
      formVisible: false,
      caseVisible: false,  // 判断是否可以更换测试用例
      formTitle: '',
      isAdd: false,  // 判断是否在添加任务页面
      serverList: [], // 匹配测试机配置IP地址下拉框的数据
      testactivityList: [], // 测试活动列表
      testList: [], // 接收到的全部测试用例列表
      testList_tree_all: [], // 处理后的测试用例数据(all)-treeData结构
      testList_select: [], // select测试用例数据
      checkIds: [], // 选择测试用例勾选的用例
      canSelect: true, // 控制选择测试用例按钮的可用状态
      checkCase: [], // 保存已勾选的用例
      viewCase: [], // 查看测试用例时tree的数据
      form: {
        taskName: '', // 任务名
        versionNum: '', // 版本号
        testactivityName: '', // 测试活动名（存储的时候转换成ID）
        taskIntroduction: '', // 任务简介
        executionTime: '', // 执行完成时间
        testResult: '', // 测试结果
        testConfiguration: '', // 测试机配置
        id: '', // 任务id（自动生成）
        ipAddress: '', // 测试机配置IP地址
        browser: '', // 测试机配置浏览器
        dynamicItem: [], // 存储测试机配置数据（新增）
        dynamicItem_artifactInfo: [], // 存储war包组件信息（新增）
        directory: '', // 导入全部测试用例的相对路径
        oldVersion: '', // 旧版本
        artifactInfoName: '',  // war包组件信息name
        artifactInfoVersion: '' , // war包组件信息version
        artifactInfos: '', // war包组件信息（全）
        tester: '', // 测试员
        exeStatus: '', // 任务执行状态
        evaluateStatus: '-1', // 质量评估状态
        analyseStatus: '-1', // 性能分析状态
        testcases: '0' // 测试用例数
      },
      listQuery: {
        page: 1,
        limit: 10,
        beginTime: undefined,
        endTime: undefined,
        taskName: undefined,
        versionNum: undefined,
        testactivityName: undefined,
        tester: undefined
      },
      params_testactivityName: '', // 测试活动页面传过来的“测试活动名”
      total: 0, // 列表总数
      dataNum: 0, // 记录任务总数目
      list: null, // table绑定的数据源
      listAll: null, // 获取全部任务数据，list获取的是分页后单页的数据。用于列出查询条件下拉框数据。
      listLoading: true,
      selRow: {},
      projectName: '', // 任务对应的项目名
      testcaseList: '', // 测试用例名列表(发送给执行接口) 一串字符
      activityID: '', // 测试活动ID，用于查询任务列表，以及拼成taskID
      measureList: [], // 存放质量报告情况列表
      cantEditCase: false, // 控制是否可以更改测试用例
      gitAddress: '', // git地址
      pageNum: 0, // number接口返回的数目
      pageInfosNum: 0, // pageinfo分析完成数目
      testerList: [], // 测试员集合
      testerId: '', // 测试员id
      tester: '', // 登录测试员name
      query_tester: '', // 查询条件中测试人员
      treeDataArray: [], // 所有用例转换成treeData之后再转换成数组 [{id:'',key:'',label:''},{...}]这样的格式。
      caseListArray: [], // 任务已有的测试用例列表。格式与treeDataArray相同，用于更改用例
      showCheckBox: true, // 用于导入用例的时候显示“用例加载中……”然后前面不要用勾选框
      evaluateLoading: false, // 控制评估按钮可用状态
      tasks: '', // 测试活动下所有任务数
      submitBtn: false, // 控制编辑/添加页面的“确定”按钮
      cancelBtn: false, // 控制编辑/添加页面的"取消"按钮
      performanceResultTitle: '', // 性能分析结果页面标题
      performanceResultVisible: false, // 性能分析结果dialog
      mapData: [], // 性能分析结果页面表格数据
      isRefresh_analyse: {}, // 控制性能分析中的那个刷新按钮的图标显示
      isRefresh_task: {}, //控制任务执行中的那个刷新按钮的图标显示
    }
  },
  created() {
    this.init()
  },

  methods: {
    init() {
      this.tester = this.$store.state.user.profile.name
      this.$route.params.testactivityName = '0928_activity'
      this.params_testactivityName = this.$route.params.testactivityName
      if(this.$route.query.taskName){
        this.listQuery.taskName = this.$route.query.taskName
      }
      this.listLoading = true
      getList({ page: 1, limit: 10000 }).then(response => { // 获取所有用户数据
        this.testerList = response.data.records
        for(let i=0;i<this.testerList.length;i++){
          if(this.$store.state.user.profile.name === this.testerList[i].name){ // 将用户名换成用户id
            this.testerId = this.testerList[i].id
            break
          }
        }
      })

      getAllActivityList({testactivityName:this.params_testactivityName}).then(response => { // 通过测试活动名查测试活动id
        this.activityID = response.data[0].id 
        let proId = response.data[0].projectName
        this.listQuery.testactivityName = this.activityID
        this.tasks = response.data[0].tasks
        getAllProList({id:proId}).then(response => {  // 通过项目id查项目名和测试用例git地址
          this.fetchData() 
          this.projectName = response.data[0].projectName 
          this.gitAddress = response.data[0].testcaseAddress
        })
      })
    },

    fetchData() {
      this.listLoading = true
      if(this.query_tester){
        for(let i=0;i<this.testerList.length;i++){
          if(this.query_tester === this.testerList[i].name){ // 将查询条件中选中的测试人员改为测试人员id
            this.listQuery.tester = this.testerList[i].id
            break
          }
        }
      }

      getAllTaskList().then(response => { // 获取所有任务数，方便检测是否重名
        this.listAll = response.data
        this.dataNum = response.data.length
        this.serverList = []
        this.listAll.forEach(element => { // 把testConfigution字段每组值分割，然后存到serverList的value里
          var i = 0
          while (element.testConfiguration.split(';')[i]) {
            var testGroup = {}
            testGroup.value = element.testConfiguration.split(';')[i]
            this.serverList.push(testGroup) // 存储每一组配置
            i = i + 1
          }
          this.serverList.forEach(element => { // 再对value进行分割，只存ip地址
            element.value = element.value.split(',')[0]
          })          
        })
      })
      
      getTaskList(this.listQuery).then(response => { // 获取t_pro_task分页数据/查询数据
        this.list = response.data.records
        this.total = response.data.total
        this.list.forEach(element => {
          for(let i=0;i<this.testerList.length;i++){ // 将测试员从id改为name
            if(element.tester === this.testerList[i].id){
              element.tester = this.testerList[i].name
              break
            }
          }
        })
        for(let i = 0;i<this.list.length;i++){ // 获取性能分析执行中的任务的执行情况
          if(this.list[i].analyseStatus === '0'){
            this.refresh(this.list[i].taskName,i)
          }
          if(this.list[i].exeStatus === '0'){
            this.getTaskResult(this.list[i].taskName,i)
          }
        }
        this.listLoading = false
      })
    },

    search() { // 查询
      this.listQuery.page = 1
      this.fetchData()
    },

    reset() { // 重置
      this.listQuery.beginTime = ''
      this.listQuery.endTime = ''
      this.listQuery.versionNum = ''
      this.listQuery.taskName = ''
      this.listQuery.tester = ''
      this.query_tester = ''
      this.fetchData()
    },

    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },

    handleClose() {

    },

    fetchNext() {
      this.listQuery.page = this.listQuery.page + 1
      this.fetchData()
    },

    fetchPrev() {
      this.listQuery.page = this.listQuery.page - 1
      this.fetchData()
    },

    fetchPage(page) {
      this.listQuery.page = page
      this.fetchData()
    },

    changeSize(limit) {
      this.listQuery.limit = limit
      this.fetchData()
    },

    handleCurrentChange(currentRow, oldCurrentRow) {
      this.selRow = currentRow
    },

    resetForm() { // 重置表单信息
      this.form = {
        testactivityName: this.$route.params.testactivityName,
        versionNum: '',
        taskName: '',
        taskIntroduction: '',
        executionTime: '',
        testConfiguration: '',
        id: '',
        ipAddress: '',
        browser: '',
        dynamicItem: [],
        dynamicItem_artifactInfo: [],
        testResult: '',
        directory: '',
        oldVersion: '', 
        artifactInfoName: '', 
        artifactInfoVersion: '',
        artifactInfos: '',
        tester: this.tester,
        exeStatus: '-1',
        evaluateStatus: '-1',
        analyseStatus: '-1',
        testcases: '0'
      }
      this.testList_tree_all = []
      
    },

    add() { // 添加任务
      this.resetForm()
      this.formTitle = '添加任务'
      this.isAdd = true
      this.formVisible = true
      this.cantEditCase = false
      if(this.list[0]){ // 自动获取任务名
        let taskNum = Number(this.list[0].taskName.split('-')[2])+1
        this.form.taskName = 'task-'+this.activityID+'-'+taskNum
      }else{
        this.form.taskName = 'task-'+this.activityID+'-1'
      }
      this.getItem()
    },

    save() { // 保存任务和用例
      this.$confirm('是否提交表单内容?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.checkCase = []
        if(!this.cantEditCase){
          this.checkCase = this.$refs.tree.getCheckedNodes(true)
          this.form.testcases = this.checkCase.length
        }
        var j = 0
        while (j !== this.dataNum && this.listAll[j].taskName !== this.form.taskName) {
          j++
        }
        if (j < this.dataNum && this.form.id !== this.listAll[j].id) {
          this.$notify.error({
            title: '错误',
            message: '该任务名已存在'
          })
        } else {
          this.form.testConfiguration = this.form.ipAddress + ',' + this.form.browser
          var i = 0
          while (this.form.dynamicItem[i]) {
            this.form.testConfiguration += ';' + this.form.dynamicItem[i].ipAddress + ',' + this.form.dynamicItem[i].browser
            i = i + 1
          }
          this.form.artifactInfos = this.form.artifactInfoName + ',' + this.form.artifactInfoVersion
          var k = 0
          while(this.form.dynamicItem_artifactInfo[k]){
            this.form.artifactInfos += ';' + this.form.dynamicItem_artifactInfo[k].artifactInfoName + ',' + this.form.dynamicItem_artifactInfo[k].artifactInfoVersion
            k = k + 1
          }
          let formTesterID = ''
          for(let i=0;i<this.testerList.length;i++){ // 将tester转成id
            if(this.form.tester === this.testerList[i].name){
              formTesterID = this.testerList[i].id
            }
          }
          this.$refs['form'].validate((valid) => {
            if (valid) {
              if(this.checkCase[0] || this.cantEditCase){
                this.submitBtn = true
                saveTask({
                  testactivityName: this.activityID,
                  versionNum: this.form.versionNum,
                  taskName: this.form.taskName,
                  taskIntroduction: this.form.taskIntroduction,
                  executionTime: this.form.executionTime,
                  testResult: this.form.testResult,
                  createTime: parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'),
                  testConfiguration: this.form.testConfiguration,
                  tester: formTesterID,
                  id: this.form.id,
                  directory: this.form.directory,
                  oldVersion: this.form.oldVersion,
                  artifactInfos: this.form.artifactInfos,
                  exeStatus: this.form.exeStatus,
                  evaluateStatus: this.form.evaluateStatus,
                  analyseStatus: this.form.analyseStatus,
                  testcases: this.form.testcases
                }).then(response => {
                  if(this.isAdd){
                    newTask({projectName: this.projectName,verTag: this.form.taskName})
                    this.tasks = Number(this.tasks) + 1
                    saveTaskNum({id:this.activityID, num:this.tasks})
                  }
                  getAllTaskList({ taskName: this.form.taskName}).then(response => { // if是编辑，else是添加
                    let taskID = response.data[0].id
                    if(!this.isAdd){ // 编辑用例
                      for(let i=0;i<this.caseListArray.length;i++){
                        for(let j=0;j<this.checkCase.length;j++){
                          if(this.caseListArray[i].id === this.checkCase[j].id){
                            break
                          }
                          if(j===this.checkCase.length-1){
                            removeCase(this.caseListArray[i].caseId)
                          }
                        }
                      }
                      for(let i=0;i<this.checkCase.length;i++){
                        for(let j=0;j<this.caseListArray.length;j++){
                          if(this.checkCase[i].id === this.caseListArray[j].id){
                            break
                          }
                          if(j===this.caseListArray.length-1){
                            let menu = ''
                            if(this.checkCase[i].label !== this.checkCase[i].key){ 
                              var labelString = '/' + this.checkCase[i].label
                              menu = this.checkCase[i].key.replace(labelString, '') 
                            }
                            saveCase({
                              taskName: taskID,
                              testcaseName: this.checkCase[i].label,
                              menuModule: menu
                            })
                          }
                        }
                      }       
                    } else {
                      let i = 0
                      while (this.checkCase[i]) {
                        let menu = ''
                        if(this.checkCase[i].label !== this.checkCase[i].key){ 
                          var labelString = '/' + this.checkCase[i].label
                          menu = this.checkCase[i].key.replace(labelString, '')
                        } 
                        saveCase({
                          taskName: taskID,
                          testcaseName: this.checkCase[i].label,
                          menuModule: menu
                        })
                        i++     
                      }
                    }
                    this.formVisible = false
                    this.submitBtn = false
                    this.fetchData()
                    this.$message({
                      message: this.$t('common.optionSuccess'),
                      type: 'success'
                    })
                  })  
                })
              } else { 
                this.$notify.error({
                  title: '错误',
                  message: '请先选择测试用例'
                })
              }
            }else{
              return false
            }
          })
        }
      })
    },

    checkSel() { // 检验是否选择了table的某一行
      if (this.selRow && this.selRow.id) {
        return true
      } else  {
        this.$message({
          message: this.$t('common.mustSelectOne'),
          type: 'warning'
        })
        return false
      }
    },

    edit() { // 编辑任务和用例
      if (this.checkSel()) {
        this.isAdd = false
        this.submitBtn = true
        this.cancelBtn = true
        if(this.selRow.exeStatus==='-1'){ // 编辑未执行的任务
          this.cantEditCase = false
          this.showCheckBox = false
          this.testList_tree_all = [{id:'1',key:'用例加载中……',label:'用例加载中……'}]
          this.formVisible = true 
          let taskCaseList = []  // 任务原有用例列表
          getAllCaseList({ taskName: this.selRow.id }).then(response => { // 获取任务原有用例  
            taskCaseList = response.data
            if(taskCaseList.length!==0){ 
              importAllTestCase({ dir: this.selRow.directory, git: this.gitAddress }).then(response => { 
                this.showCheckBox = true
                this.testList = response.data.data
                this.testList_tree_all = this.pathToTree(this.testList) // 将用例转成treeData
                this.canSelect = false // 选择用例按钮变为可用状态
                this.forTreeData(this.testList_tree_all) // 将treeData转为数组形式  
                taskCaseList.forEach(element => {
                  let oneCase = {}
                  oneCase.caseId = element.id
                  oneCase.id = ''
                  oneCase.key = element.menuModule+'/'+element.testcaseName
                  oneCase.label = element.testcaseName
                  for(let i=0;i<this.treeDataArray.length;i++){
                    if(oneCase.key === this.treeDataArray[i].key){
                      oneCase.id = this.treeDataArray[i].id
                      this.caseListArray.push(oneCase) // 将用例以数组形式存到caseListArray中
                      this.checkIds.push(oneCase.id) // 将原用例的id都存放到checkIds中
                    }
                  }
                })    
                this.setCheckedNodes(this.checkIds) 
                this.submitBtn = false 
                this.cancelBtn = false 
              })
            } else {
              this.showCheckBox = true
              this.testList_tree_all = []
            }
          })
        } else if(this.selRow.exeStatus === '0') { // 编辑执行中的任务
          this.$alert('该任务正在执行中，请等执行完再编辑', '提示', {
            confirmButtonText: '确定',
          })
        } else { // 编辑执行完成的任务
          this.cantEditCase = true
          this.formVisible = true
          this.showCheckBox = false
          this.testList_tree_all = [{id:'1',key:'用例加载中……',label:'用例加载中……'}]
          let allCasesList = []
          getAllCaseList({ taskName: this.selRow.id }).then(response => {
            let allCases = response.data
            for(let i=0;i<allCases.length;i++){
              let menu = allCases[i].menuModule
              allCasesList.push(menu+"/"+allCases[i].testcaseName)
            }
            this.testList_tree_all = this.pathToTree(allCasesList)
            this.submitBtn = false
            this.cancelBtn = false
          })
        }
        let selData = JSON.parse(JSON.stringify(this.selRow))
        this.form = selData
        this.$set(this.form, 'ipAddress','')
        this.$set(this.form, 'browser', '')
        this.$set(this.form, 'artifactInfoName','')
        this.$set(this.form, 'artifactInfoVersion', '')
        for(let i=0;i<this.testerList.length;i++){  // 将tester改为name
          if(this.form.tester === this.testerList[i].id){
            this.form.tester = this.testerList[i].name
          }
        }
        this.formTitle = '编辑任务'
        var i = 1 //测试机配置
        this.form.ipAddress = this.form.testConfiguration.split(';')[0].split(',')[0]
        this.form.browser = this.form.testConfiguration.split(';')[0].split(',')[1]
        this.$set(this.form, 'dynamicItem',[])
        while (this.form.testConfiguration.split(';')[i]) {
          var item = {}
          item.ipAddress = this.form.testConfiguration.split(';')[i].split(',')[0]
          item.browser = this.form.testConfiguration.split(';')[i].split(',')[1]
          this.form.dynamicItem.push(item)
          i = i + 1
        }
        var j = 1 // artifact组件信息
        this.form.artifactInfoName = this.form.artifactInfos.split(';')[0].split(',')[0]
        this.form.artifactInfoVersion = this.form.artifactInfos.split(';')[0].split(',')[1]
        this.$set(this.form, 'dynamicItem_artifactInfo', [])
        while (this.form.artifactInfos.split(';')[j]) {
          var item = {}
          item.artifactInfoName = this.form.artifactInfos.split(';')[j].split(',')[0]
          item.artifactInfoVersion = this.form.artifactInfos.split(';')[j].split(',')[1]
          this.form.dynamicItem_artifactInfo.push(item)
          j = j + 1
        }
      }
    },

    remove() { // 删除任务
      if (this.checkSel()) {
        var id = this.selRow.id
        var removeTaskName = this.selRow.taskName
        this.$confirm(this.$t('common.deleteConfirm'), this.$t('common.tooltip'), {
          confirmButtonText: this.$t('button.submit'),
          cancelButtonText: this.$t('button.cancel'),
          type: 'warning'
        }).then(() => {
          removeTask(id).then(response => {
            this.$message({
              message: this.$t('common.optionSuccess'),
              type: 'success'
            })
            this.tasks = Number(this.tasks) - 1
            saveTaskNum({id:this.activityID, num: this.tasks})
            if(this.total % this.listQuery.limit === 1){
              this.listQuery.page = this.listQuery.page - 1
            }
            this.fetchData()
            delTask({ // 删除质量模块的任务
              projectName: this.projectName,
              verTag: removeTaskName
            })
          }).catch(err => {
            this.$notify.error({
              title: '错误',
              message: err
            })
          })
        }).catch(() => {
        })
      }
    },

    tester_unique(arr) { // 测试人名去重
      const res = new Map()
      return arr.filter((arr) => !res.has(arr.tester) && res.set(arr.tester, 1))
    },

    version_unique(arr) { // 版本号去重
      const res = new Map()
      return arr.filter((arr) => !res.has(arr.versionNum) && res.set(arr.versionNum, 1))
    },

    ip_unique(arr) { // ip地址去重
      const res = new Map()
      return arr.filter((arr) => !res.has(arr.value) && res.set(arr.value, 1))
    },

    querySearch(queryString, cb) { // 这个方法跟下方的方法用于ip地址的输入匹配
      var serverList = this.serverList
      var results = queryString ? serverList.filter(this.createFilter(queryString)) : serverList
      // 调用 callback 返回建议列表的数据
      cb(this.ip_unique(results))
    },

    createFilter(queryString) {
      return (ipList) => {
        return (ipList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0)
      }
    },

    handleSelect(item) {
      console.log(item)
    },

    addItem() { // 添加测试机配置项
      this.form.dynamicItem.push({
        ipAddress: '',
        browser: ''
      })
    },

    deleteItem(item, index) { // 删除测试机配置项
      this.form.dynamicItem.splice(index, 1)
    },

    addItem_artifactInfo() { // 添加war包artifact组件信息
      this.form.dynamicItem_artifactInfo.push({
        artifactInfoName: '',
        artifactInfoVersion: ''
      })
    },

    deleteItem_artifactInfo(item, index) { // 删除war包artifact组件信息
      this.form.dynamicItem_artifactInfo.splice(index, 1)
    },

    closeDialog() { // 清除表单验证残留信息
      this.$refs['form'].resetFields()
      this.testList_tree_all = []
      this.testList_select = []
      this.checkIds = []
      this.testList = []
      this.canSelect = true
      this.isAdd = false 
      this.caseListArray = []
      this.cantEditCase = false
      this.showCheckBox = true
    },
    
    viewTestCase(taskName) {  // dialog展开
      this.viewCase = []
      getAllCaseList({ taskName: taskName }).then(response => {
        let allCases = response.data
        for(let i=0;i<allCases.length;i++){
          let menu = allCases[i].menuModule
          this.viewCase.push(menu+"/"+allCases[i].testcaseName)
        }
        this.viewCase = this.pathToTree(this.viewCase)
        this.caseVisible = true
      })  
    },

    execute() { // 执行任务
      if (this.checkSel()) {
        if(this.selRow.exeStatus === '1'){
          this.$confirm('该任务已执行过, 重新执行会默认创建一个新任务，是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => { // 已执行过，再次执行创建一个全新任务，信息一致。
            this.listLoading = true
            getAllTaskList({ taskName: this.selRow.taskName }).then(response => {
              let pdata = {}
              let taskNum = Number(this.list[0].taskName.split('-')[2])+1
              pdata.taskName = 'task-'+this.activityID+'-'+taskNum
              pdata.testactivityName = response.data[0].testactivityName
              pdata.versionNum = response.data[0].versionNum
              pdata.taskIntroduction = response.data[0].taskIntroduction
              pdata.createTime = parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')
              pdata.executionTime = ''
              pdata.testConfiguration = response.data[0].testConfiguration
              pdata.tester = this.testerId  // 创建人要改一下
              pdata.id = ''
              pdata.testResult = ''
              pdata.directory = response.data[0].directory
              pdata.oldVersion = response.data[0].oldVersion
              pdata.artifactInfos = response.data[0].artifactInfos
              pdata.exeStatus = '-1'
              pdata.evaluateStatus = '-1'
              pdata.analyseStatus = '-1',
              pdata.testcases = response.data[0].testcases
              saveTask(pdata).then(response => {
                newTask({projectName: this.projectName,verTag: pdata.taskName})
                this.tasks = Number(this.tasks) + 1
                saveTaskNum({id:this.activityID,num: this.tasks})
                getAllTaskList({ taskName: pdata.taskName }).then(response => {
                  let taskID = response.data[0].id
                  getAllCaseList({ taskName: this.selRow.id }).then(response => {
                    let cases = response.data
                    cases.forEach(element => {
                      saveCase({
                        taskName: taskID,
                        testcaseName: element.testcaseName,
                        menuModule: element.menuModule
                      })
                    })   
                    this.fetchData() 
                  })
                })
              }) 
            })
          }).catch(() => {
            this.$message({
              type: 'info',
              message: '已取消执行'
            })         
          })
        } else if (this.selRow.exeStatus === '0'){
          this.$alert('该任务正在执行中，请勿重复执行。请点击【执行过程】进入执行页面', '提示', {
            confirmButtonText: '确定',
          })
        } else { // 跳转执行页面
          getAllCaseList({ taskName: this.selRow.id }).then(response => {
            let casesList = response.data
            if(casesList.length!==0){
              this.testcaseList = ''
              casesList.forEach(element => {
                this.testcaseList += element.menuModule + '/' + element.testcaseName + ','
              })
              let pdata = {
                fileNameList: this.testcaseList,
                projectName: this.projectName,
                jobId: this.selRow.taskName
              }
              executeTask(pdata).then(response => {
                if(response.msg === '成功'){
                  saveResult({taskName: this.selRow.taskName, exeStatus: '0' }).then(response => {
                    this.$router.push({path: '/project/executeTask/'+this.selRow.taskName})
                  })
                }
              }).catch(() => {
                this.$notify.error({
                  title: '错误',
                  message: '执行失败'
                })   
              })
            } else {
              this.$notify.error({
                title: '错误',
                message: '该任务没有用例可执行！'
              })
            }
          })
        }
      }
    },

    viewExeProcess(){ // 查看执行过程
      if (this.checkSel()) {
        if (this.selRow.exeStatus === '-1'){
          this.$alert('该任务还未执行，请先点击执行按钮进行执行', '提示', {
            confirmButtonText: '确定',
          })
        } else {
          this.$router.push({ path: '/project/executeTask/'+this.selRow.taskName})
        }
      } 
    },

    importAllTestCase() { // 导入全部测试用例
      this.showCheckBox = false
      this.testList_tree_all = [{id:'1',key:'用例加载中……',label:'用例加载中……'}]
      this.checkIds = []  
      let testCaseDir = ''
      if(!this.form.directory){ // 若不填用例目录，默认为“/”
        testCaseDir = '/'
      }else{
        testCaseDir = this.form.directory
      }
      importAllTestCase({ dir: testCaseDir, git: this.gitAddress }).then(response => { 
        this.showCheckBox = true
        this.testList = response.data.data
        this.testList_tree_all = this.pathToTree(this.testList)
        this.setCheckedNodes(this.checkIds) // 先全部勾选
        this.canSelect = false // 选择用例按钮变为可用状态
      })
    },

    selectTestCase() { // 选择测试用例
      if (this.form.versionNum && this.form.artifactInfoName) {
        let testCaseDir = ''
        if(!this.form.directory){
          testCaseDir = '/'
        } else {
          testCaseDir = this.form.directory
        }
        let pdata = {
          taskName: this.form.taskName,
          artifactInfos: [],
          newVersion: this.form.versionNum,
          oldVersion: this.form.oldVersion,
          projectName: this.projectName,
          testCaseDir: testCaseDir,
          testGit: this.gitAddress
        }  
        let artifact = {}
        artifact.name = this.form.artifactInfoName
        artifact.version = this.form.artifactInfoVersion
        pdata.artifactInfos.push(artifact)
        var i = 0
        while (this.form.dynamicItem_artifactInfo[i]) {
          let artifacts = {}
          artifacts.name = this.form.dynamicItem_artifactInfo[i].artifactInfoName
          artifacts.version = this.form.dynamicItem_artifactInfo[i].artifactInfoVersion
          pdata.artifactInfos.push(artifacts)
          i = i + 1
        }   
        selectTestCase(pdata).then(response => {
          deleteSelect({taskName: this.form.taskName})
          this.testList_select = response.data.data
          for(let n=0; n<this.testList_select.length;n++){ //每一条数据单独处理
            let labels = [] // 将数据分割后存入数组 labels 中
            labels = this.testList_select[n].split('/')
            let childrens = []
            childrens = this.testList_tree_all
            if(labels.length > 1){ // 是 xxx/xxx/xxx.java 的格式，而不是 xxx.java 的格式
              for(let i = 0; i<labels.length-1; i++){ // 一级一级匹配，每次都返回children[]，最后childrens存着倒数第二级的children[]
                childrens=this.checked(childrens,labels[i])
              }
            }
            for(let j = 0;j<childrens.length;j++){ // 找出最后的children[]中匹配文件名的那一项，返回id，把它放入checkIds数组中
              if(labels[labels.length-1] === childrens[j].label){
                this.checkIds.push(childrens[j].id)
              }
            }
          }
          console.log('ids：', this.checkIds) // 循环完每条数据后会匹配出一个checkIds数组，调用方法将数组里的节点勾选。
          this.setCheckedNodes(this.checkIds)
        }).catch(err => {
          deleteSelect({taskName: this.form.taskName})
        })       
      } else {
        this.$notify.error({
          title: '错误',
          message: '新版本号、旧版本号和组件名称必填'
        })
      }
    },

    pathToTree(input){ // 把路径转化成tree数据
      let root = [];
      let id = 1;
      for (let i=0;i<input.length;i++){
        let chain = input[i].split("/");
        let currentHierarchy = root;
        for(let j = 0; j < chain.length;j++){
          let wantedNode = chain[j]
          if(wantedNode === ''){
            continue;
          }
          let lastHierarchy = currentHierarchy;
          // 遍历root是否已有该层级
          for(let k = 0; k < currentHierarchy.length;k++){
            if(currentHierarchy[k].label === wantedNode){
              currentHierarchy = currentHierarchy[k].children;
              break;
            }
          }  
          if(lastHierarchy === currentHierarchy) {
            let key;
            if(j === chain.length - 1){
              key = input[i];
            } else {
              key = chain.slice(0,j+1).join('/')+'/';
            }
            let newNode = {
              id: id++,
              key: key,
              label: wantedNode,
              children: []
            };
            // 文件，最后一个字符不是"/“符号
            if(j=== chain.length-1){
              delete newNode.children;
            }
            currentHierarchy.push(newNode);
            currentHierarchy = newNode.children;
          }
        }
      }  
      return root;
    },

    forTreeData(data) { // 将tree数据转成数组，方便匹配获取id
      this.treeDataArray = []
      let that = this
      for(let i of data){
        this.treeDataArray.push({
          id: i.id,
          key: i.key,
          label: i.label
        })
        if(i.children){
          that.forTreeData(i.children)   // 灵魂语句
        }
      }
    },

    setCheckedNodes(ids) { // 设置勾选节点，
      if(ids.length !== 0){
        this.$refs.tree.setCheckedKeys(ids); // 勾选匹配部分
      } else {
        let arr = []
        for(let i = 1; i<=this.testList.length;i++){
          arr.push(i)
        }
        this.$refs.tree.setCheckedKeys(arr); //勾选全部
      }    
    },

    checked(children,label){ // 逐级检查，往下获取子集
      for(let i = 0;i<children.length;i++){
        if(label === children[i].label){
          return children[i].children
        }
      }
    },

    importArtifactInfo(){ // 导入artifact组件信息
      if (this.form.oldVersion){
        let pdata = {}
        pdata.projectName = this.projectName
        pdata.projectVersion = this.form.oldVersion
        importArtifacts(pdata).then(response => {
          let artifactsInfo = response.data.data.artifacts
          this.form.artifactInfoName = artifactsInfo[0].name
          this.form.artifactInfoVersion = artifactsInfo[0].version
          let i = 1
          while(artifactsInfo[i]){
            this.form.dynamicItem_artifactInfo.push({ artifactInfoName: artifactsInfo[i].name, artifactInfoVersion: artifactsInfo[i].version })
            i++
          }
        })
      } else {
        this.$notify.error({
          title: '错误',
          message: '旧版本号不能为空'
        })
      }
    },

    evaluate(taskName,index){ // 质量评估
      if(this.list[index].exeStatus === '1'){
        this.list[index].evaluateStatus = '0'
        this.evaluateLoading = true
        this.pageNum = 0
        this.pageInfosNum = 0
        let pdata = {}
        pdata.projectName = this.projectName
        pdata.taskId = this.list[index].taskName
        pageNumber(pdata).then(response => {
          this.pageNum = response.data.data.length
          pageInfo(pdata).then(response => {
            this.srcData = response.data.data
            for(var key in this.srcData){
              this.pageInfosNum ++
            }
            if(this.pageNum === this.pageInfosNum){ // 若sitespeed分析完成，则可以评估
              this.evaluateLoading = false
              let queryData = {}
              queryData['ver_tag'] = taskName
              queryData['proj_id'] = this.projectName
              measureVer(queryData).then(response => {
                if(response.msg === "成功"){
                  this.list[index].evaluateStatus = '1'
                  saveResult({taskName: taskName, evaluateStatus: '1' })
                }
              }).catch(function(error) {
                console.log(error)
              })
            } else if(this.list[index].exeStatus === '0') {
              this.$alert('该任务正在执行中，请执行完成后再进行质量评估', '提示', {
                confirmButtonText: '确定',
              })
              this.list[index].evaluateStatus = '-1'
            } else {
              this.$alert('sitespeed未分析完成，请稍后再试', '提示', {
                confirmButtonText: '确定',
              })
              this.list[index].evaluateStatus = '-1'
            }
          })
        })
      } else {
        this.$alert('任务未执行，不可进行评估', '提示', {
          confirmButtonText: '确定',
        })
      }
    },

    saveLocal(){ // 将表单内容保存到本地sessionStorage
      const FORM_VERSION_NUM = 'versionNum'
      const FORM_TASK_INTRODUCTION = 'taskIntroduction'
      const FORM_IP_ADDRESS = 'ipAddress'
      const FORM_BROWSER = 'browser'
      const FORM_DYNAMICITEM = 'dynamicItem'
      const FORM_DIRECTORY = 'directory'
      const FORM_OLDVERSION = 'oldVersion'
      const FORM_ARTIFACTNAME = 'artifactInfoName'
      const FORM_ARTIFACTVERSION = 'artifactInfoVersion'
      const FORM_DYNAMICITEM_ARTIFACTINFO = 'dynamicItem_artifactInfo'
      const FORM_TESTLIST_TREE_ALL = 'testList_tree_all'
      const FORM_CHECKCASE = 'checkCase'
      window.sessionStorage.setItem(FORM_VERSION_NUM,JSON.stringify(this.form.versionNum))
      window.sessionStorage.setItem(FORM_TASK_INTRODUCTION,JSON.stringify(this.form.taskIntroduction))
      window.sessionStorage.setItem(FORM_IP_ADDRESS,JSON.stringify(this.form.ipAddress))
      window.sessionStorage.setItem(FORM_BROWSER,JSON.stringify(this.form.browser))
      window.sessionStorage.setItem(FORM_DYNAMICITEM,JSON.stringify(this.form.dynamicItem))
      window.sessionStorage.setItem(FORM_DIRECTORY,JSON.stringify(this.form.directory))
      window.sessionStorage.setItem(FORM_OLDVERSION,JSON.stringify(this.form.oldVersion))
      window.sessionStorage.setItem(FORM_ARTIFACTNAME,JSON.stringify(this.form.artifactInfoName))
      window.sessionStorage.setItem(FORM_ARTIFACTVERSION,JSON.stringify(this.form.artifactInfoVersion))
      window.sessionStorage.setItem(FORM_DYNAMICITEM_ARTIFACTINFO,JSON.stringify(this.form.dynamicItem_artifactInfo))
      window.sessionStorage.setItem(FORM_TESTLIST_TREE_ALL,JSON.stringify(this.testList_tree_all))
      window.sessionStorage.setItem(FORM_CHECKCASE,JSON.stringify(this.$refs.tree.getCheckedNodes(true)))
      this.$notify({
        title: '成功',
        type: 'success'
      });
    },

    getItem(){ // 取出数据
      if(window.sessionStorage.getItem("versionNum")){
        this.form.versionNum = JSON.parse(window.sessionStorage.getItem("versionNum"));
      }
      if(window.sessionStorage.getItem("taskIntroduction")){
        this.form.taskIntroduction = JSON.parse(window.sessionStorage.getItem("taskIntroduction"));
      }
      if(window.sessionStorage.getItem("ipAddress")){
        this.form.ipAddress = JSON.parse(window.sessionStorage.getItem("ipAddress"));
      }
      if(window.sessionStorage.getItem("browser")){
        this.form.browser = JSON.parse(window.sessionStorage.getItem("browser"));
      }
      if(window.sessionStorage.getItem("directory")){
        this.form.directory = JSON.parse(window.sessionStorage.getItem("directory"));
      }
      if(window.sessionStorage.getItem("oldVersion")){
        this.form.oldVersion = JSON.parse(window.sessionStorage.getItem("oldVersion"));
      }
      if(window.sessionStorage.getItem("artifactInfoName")){
        this.form.artifactInfoName = JSON.parse(window.sessionStorage.getItem("artifactInfoName"));
      }
      if(window.sessionStorage.getItem("artifactInfoVersion")){
        this.form.artifactInfoVersion = JSON.parse(window.sessionStorage.getItem("artifactInfoVersion"));
      }
      this.form.dynamicItem = JSON.parse(window.sessionStorage.getItem("dynamicItem") || '[]');     
      this.form.dynamicItem_artifactInfo = JSON.parse(window.sessionStorage.getItem("dynamicItem_artifactInfo") || '[]');
      this.testList_tree_all = JSON.parse(window.sessionStorage.getItem("testList_tree_all") || '[]')
      if(this.testList_tree_all.length){
        this.canSelect = false
      }
      let checkItem = JSON.parse(window.sessionStorage.getItem("checkCase") || '[]')
      let ids = []
      for(let i=0;i<checkItem.length;i++){
        ids.push(checkItem[i].id)
      }
      this.$nextTick(() => {
        this.$refs.tree.setCheckedKeys(ids)  
      })
    },

    clearForm(){ // 清空表单
      this.canSelect = true
      this.form.versionNum = ''
      this.form.taskIntroduction = ''
      this.form.ipAddress = ''
      this.form.browser = ''
      this.form.dynamicItem = []
      this.form.directory = ''
      this.form.oldVersion = ''
      this.form.artifactInfoName = ''
      this.form.artifactInfoVersion = ''
      this.form.dynamicItem_artifactInfo = []
      this.testList_tree_all = []
    },

    exeTask(index){ // 展开行的执行按钮，点击之后先选中该行，然后调用执行方法
      this.$refs.singleTable.setCurrentRow(this.list[index])
      this.execute()
    },

    getPerformanceResult(taskName){
      this.mapData = []
      let pdata = {}
      pdata.projectName = this.projectName
      pdata.taskId = taskName
      performanceResult(pdata).then(response => {
        let data = response.data.data
        for(let i=0;i<data.length;i++){
            let map = {}
            this.$set(map, 'functionName','')
            this.$set(map, 'rate', '')
            for(var key in data[i]){
              map.functionName = key
              map.rate = data[i][key]
            }
              this.mapData.push(map)  
        }
        this.performanceResultTitle = taskName + '：性能分析结果'
        this.performanceResultVisible = true
      })
    },

    analyse(taskName,testcases,index){ // 性能分析-执行
      if(this.list[index].exeStatus === '1'){
        if(Number(testcases) >= 5){
          this.list[index].analyseStatus = '0'
          performanceAnalysis({ projectName:this.projectName, taskId:taskName})
          saveResult({taskName:taskName,analyseStatus:'0'})
        }else{
          this.$alert('该任务测试用例数目少于5个，不能进行性能分析！', '提示', {
            confirmButtonText: '确定',
          })
        }
      } else {
        this.$alert('该任务还未执行完成，不能进行性能分析！', '提示', {
          confirmButtonText: '确定',
        })
      }
    },

    refresh(taskName,index){ // 性能分析-刷新
      this.isRefresh_analyse[taskName] = true
      let pdata = {}
      pdata.projectName = this.projectName
      pdata.taskId = taskName
      performanceResult(pdata).then(response => {
        if(response.data.data !== -1){ // 表示分析已经有结果了
          this.list[index].analyseStatus = '1'
          saveResult({taskName:taskName,analyseStatus:'1'})
        }
        this.isRefresh_analyse[taskName] = false
      })
    },

    getTaskResult(taskName,index){ // 获取任务执行结果
      this.isRefresh_task[taskName] = true
      getAllCaseList({ taskName:this.list[index].id }).then(response => { // 获取该任务所有测试用例
        let resData = response.data
        let count = resData.length
        let finish = 0
        let success = 0
        let failure = 0
        let exeTime = ''
        let percentage = 0
        resData.forEach(element => {
          if(element.executionResult){
            finish++
            if(element.executionResult === '1,0'){
              success ++
            } else {
              failure ++
            }
            if(element.executionTime>exeTime){ // 执行完成时间一直保持最大值
              exeTime = element.executionTime
            }
          }
        })
        percentage = parseInt(finish*100/count) // 任务完成进度
        if(percentage === 100){
          this.list[index].exeStatus = '1'
          this.list[index].executionTime = exeTime
          this.list[index].testResult = success + ',' + failure
          saveResult({taskName: taskName, executionTime:exeTime, executionResult: success+','+ failure, exeStatus: '1' })
          let pdata = {}
          pdata.projectName = this.projectName
          pdata.taskId = taskName
          // disable har analysis to avoid 502 alert
          // harAnalysis(pdata) 
          this.isRefresh_task[taskName] = false
        } else {    
          getJobResult({ jobId:taskName }).then(response => {
            let resultList = []
            resultList = response.data.data
            for(let i=finish; i<resultList.length; i++){
              let pdata = {}
              pdata.taskName = this.list[index].id
              pdata.testcaseName = resultList[i].name.split('.')[0] + '.lcl'
              pdata.executionResult = resultList[i].success + ',' + resultList[i].failure
              pdata.costTime = resultList[i].time
              pdata.executionTime = resultList[i].createTime
              pdata.logId = resultList[i].id
              pdata.picPath = resultList[i].picPath
              pdata.videoPath = resultList[i].videoPath
              pdata.harPath = resultList[i].harPath
              saveCase(pdata)
            }
            this.isRefresh_task[taskName] = false 
          })
        }
      })
    },
  }
}
