import { getAllProList } from '@/api/project/projectList'
import { getAllActivityList } from '@/api/project/testActivity'
import { getTaskList, saveResult } from '@/api/project/testTask'
import { performancePercent, performanceResult, performanceAnalysis,functionAnalysis, functionResult } from '@/api/project/testCase'

export default {
  data() {
    return {  
      allProjectList: [], // 全部项目列表
      allActivityList: [], // 全部测试活动列表  
      projectList: [], // 项目列表， 查询条件框数据源
      testactivityList: [], // 测试活动列表， 查询条件框数据源
      select_projectName: '' , // 查询条件框绑定的项目名
      select_testactivityName: '' , // 查询条件框绑定的测试活动名
      select_status: '', // 查询条件框绑定的性能分析状态
      select_status_func: '', // 查询条件框绑定的功能缺陷分析状态
      taskList: [], // 任务列表，table数据源
      search_taskName: '',
      listQuery: {
        page: 1,
        limit: 10,
        testactivityName: '',
        taskName: undefined,
        analyseStatus: undefined,
        functionStatus: undefined
      },
      total: 0,
      listLoading: false,
      selectData:[], // 勾选的数据
      functionName: '', // 性能分析结果-方法名
      rate: '', // 性能分析结果-置信率
      mapData: [], // 性能分析数据表（接口返回的是key-value的格式）
      formVisible: false, // 控制性能分析结果dialog
      formVisible_func: false, // 控制功能分析结果dialog
      timer: '', // 计时器
      testactivityMap: {}, // 存储测试活动id与name的匹配表
      projectMap: {}, // 存储项目名name与applicationName的匹配表
    }
  },

  created() {
    this.init()
  },

  beforeDestroy() {
    clearInterval(this.timer); // 销毁页面前清除计时器
  },

  methods: {
    init() {
      this.listLoading = true
      getAllProList().then(response => { // 获取所有项目
        this.allProjectList = response.data
        this.allProjectList.forEach(element => {
          this.projectMap[element.projectName] = element.applicationName
        })
        this.projectList = this.allProjectList
        getAllActivityList().then(response => { // 获取所有测试活动
          this.allActivityList = response.data
          let num = 0
          this.allActivityList.forEach(element => { // 将测试活动表的projectname字段从id改为name
            num++
            this.testactivityMap[element.testactivityName] = element.id
            for(let i=0;i<this.projectList.length;i++){
              if(element.projectName === this.projectList[i].id){
                element.projectName = this.projectList[i].projectName
              }
            }
            this.testactivityList = this.allActivityList
            if(num===this.testactivityList.length){
              this.fetchData()
            }
          })
        })
      })
    },

    search() { // 搜索查询相似的，也就是包含文字就行
      this.total = 0
      this.listQuery.page = 1
      this.list = []
      if(this.validInput(this.search_taskName)){
        this.fetchData()
      }else{
        this.$notify.error({
          title: '错误',
          message: '输入不合法'
        })
      }
    },

    fetchData() {
      this.listLoading = true  
      for(let i=0;i<this.testactivityList.length;i++){ // 将搜索条件的测试活动名改为测试活动ID，方便查询任务列表
        if(this.select_testactivityName === this.testactivityList[i].testactivityName){
          this.listQuery.testactivityName = this.testactivityList[i].id
        }
      }
      if(this.select_projectName && !this.select_testactivityName){
        this.listQuery.testactivityName = '0' 
      }
      if(this.select_status === '已完成'){ // 将搜索条件的性能分析状态改为数字，方便查询任务列表
        this.listQuery.analyseStatus = '1'
      }else if(this.select_status === '执行中'){
        this.listQuery.analyseStatus = '0'
      }else if(this.select_status === '未执行'){
        this.listQuery.analyseStatus = '-1'
      }else{
      }

      if(this.select_status_func === '已完成'){ // 将搜索条件的性能分析状态改为数字，方便查询任务列表
        this.listQuery.funcStatus = '1'
      }else if(this.select_status_func === '执行中'){
        this.listQuery.funcStatus = '0'
      }else if(this.select_status_func === '未执行'){
        this.listQuery.funcStatus = '-1'
      }else{
      }

      if(this.search_taskName){
        this.listQuery.taskName  = encodeURI(this.search_taskName)
      }else{
        this.listQuery.taskName = ''
      }
      getTaskList(this.listQuery).then(response => { // 获取查询出来的任务列表
        this.taskList = response.data.records
        this.total = response.data.total
        if(this.total === 0){
          this.listLoading = false
        } else {
          this.taskList.forEach(element => {
            this.$set(element,'projectName','')
            this.$set(element,'status','')
            this.$set(element,'functionStatus', '')
            this.$set(element,'applicationName','')
            for(let i=0;i<this.testactivityList.length;i++){
              if(element.testactivityName === this.testactivityList[i].id){
                element.projectName = this.testactivityList[i].projectName
                element.testactivityName = this.testactivityList[i].testactivityName
                element.applicationName = this.projectMap[element.projectName]
              }
            }
            if(element.funcStatus === '-1'){
              element.functionStatus = '未执行'
            } else if(element.funcStatus === '1'){
              element.functionStatus = '已完成'
            } else {
              element.functionStatus = '执行中'
            }
            
            if(element.analyseStatus === '-1'){
              element.status = '未执行'
            } else if(element.analyseStatus === '1'){
              element.status = '已完成'
            } else { // 任务执行中，调接口查询执行情况
              element.status = '执行中'
              let pdata = {}
              pdata.projectName = element.projectName
              pdata.taskId = element.taskName
              performanceResult(pdata).then(response => {
                if(response.data.data === -1){ // 若还没分析结果，则调percent接口判断是排队中还是执行中
                  performancePercent(pdata).then(response => {
                    if(response.data.data === 0){
                      element.status = '排队中'
                    } else {
                      element.status = '执行中'
                    }
                  })
                } else { // 性能分析结果已经有了，那么更改任务分析状态
                  saveResult({taskName:element.taskName, analyseStatus: '1'})
                  element.status = '已完成'
                }
              })
              clearInterval(this.timer)
              this.timer = setInterval(this.refresh, 30000) // 每三十秒刷新一次页面
            } 
            this.listLoading = false        
          })  
        } 
      })
    },

    refresh(){ // 刷新执行状态
      let n = 0 // 列表正在分析的个数，若为0，则取消轮询
      this.taskList.forEach(element => {
        let pdata = {}
        pdata.projectName = element.projectName
        pdata.taskId = element.taskName
        if(element.status === '执行中' || element.status === '排队中'){
          n++
          performanceResult(pdata).then(response => {
            if(response.data.data === -1){ // 若还没分析结果，则调percent接口判断是排队中还是执行中
              performancePercent(pdata).then(response => {
                if(response.data.data === 0){
                  element.status = '排队中'
                } else {
                  element.status = '执行中'
                }
              })
            } else { // 性能分析结果已经有了，那么更改任务分析状态
              saveResult({taskName:element.taskName, analyseStatus: '1'})
              element.status = '已完成'
            }
          })
        }
      }) 
      if(n===0){
        clearInterval(this.timer); // 清除轮询计时器
      }      
    },

    changeProject(){
      this.testactivityList = []
      this.select_testactivityName = ''
      let proID
      for(let i=0;i<this.projectList.length;i++){
        if(this.select_projectName === this.projectList[i].projectName){
          proID = this.projectList[i].id
        }
      }
      getAllActivityList({ projectName: proID }).then(response => {
        this.testactivityList = response.data
        this.testactivityList.forEach(element => {
          element.projectName = this.select_projectName
        })
        this.select_testactivityName = this.testactivityList[0].testactivityName
      })
    },

    changeActivity(){
      for(let i=0;i<this.testactivityList.length;i++){
        if(this.testactivityList[i].testactivityName === this.select_testactivityName){
          this.select_projectName = this.testactivityList[i].projectName
        }
      }
    },

    reset() {
      this.listQuery.testactivityName = ''
      this.listQuery.taskName = ''
      this.listQuery.analyseStatus = ''
      this.listQuery.funcStatus = ''
      this.select_projectName = ''
      this.select_testactivityName = ''
      this.select_status = ''
      this.select_status_func = ''
      this.projectList = this.allProjectList
      this.testactivityList = this.allActivityList
      this.search_taskName = ''
      this.fetchData()
    },

    handleFilter() {
      this.listQuery.page = 1
      this.fetchData()
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

    analyse(){ // 多任务性能分析
      if(this.$refs.multipleTable.selection[0]){ // 有勾选任务
        this.selectData = []
        for(let i=0;i<this.$refs.multipleTable.selection.length;i++){
          if(this.$refs.multipleTable.selection[i].status === '未执行' && Number(this.$refs.multipleTable.selection[i].testcases)>4){
            this.selectData.push(this.$refs.multipleTable.selection[i])
          }
        }
        // this.selectData = this.$refs.multipleTable.selection
        let n = 0 // 第一个分析的任务状态为“执行中”，另外的都为“排队中”
        // let tips = ''
        this.selectData.forEach(element => {
          n++
          // if(Number(element.testcases) >= 5){
            for(let i = 0;i<this.taskList.length;i++){
              if(this.taskList[i].id === element.id){
                if(n === 1){
                  this.taskList[i].status = '执行中'
                }else{
                  this.taskList[i].status = '排队中'
                }    
              }
            }
            saveResult({taskName:element.taskName,analyseStatus:'0'})
            performanceAnalysis({ projectName:element.projectName, taskId:element.taskName, applicationName:element.applicationName}).catch(err => {
              if(err.response.status === 500) {
                saveResult({taskName:element.taskName,analyseStatus:'-1'})
                for(let j = 0;j<this.taskList.length;j++){
                  if(this.taskList[j].id === element.id){
                    this.taskList[j].status = '未执行'
                  }
                }
                clearInterval(this.timer)
                this.$notify.error({
                  title: '错误',
                  message: '性能分析异常，请重试'
                })
              }
            })    
        //   }else{
        //     tips += element.taskName + '，'
        //   }
        })
        // if(tips){
        //   tips += '由于测试用例数少于5个，不能进行性能分析！'
        //   this.$alert(tips, '提示', {
        //     confirmButtonText: '确定',
        //   })
        // }
        clearInterval(this.timer)
        this.timer = setInterval(this.refresh, 30000) // 每三十秒刷新一次页面
      } else {
        this.$message({ // 没勾选任务
          message: this.$t('common.mustSelectOne'),
          type: 'warning'
        })
      }
    },

    analyseOneTask(index,projectName,taskName){ // 单个任务分析
      // if(Number(this.taskList[index].testcases) >= 5){
        this.taskList[index].status = '执行中'
        saveResult({taskName:taskName,analyseStatus:'0'})
        performanceAnalysis({ projectName:projectName, taskId:taskName, applicationName:this.projectMap[projectName]}).catch(err => {
          if(err.response.status === 500) {
            saveResult({taskName:taskName,analyseStatus:'-1'})
            this.taskList[index].status = '未执行'
            clearInterval(this.timer)
            this.$notify.error({
              title: '错误',
              message: '性能分析异常，请重试'
            })
          }
        })
        clearInterval(this.timer)
        this.timer = setInterval(this.refresh, 30000) // 每三十秒刷新一次页面
      // }else{
      //   this.$alert('该任务测试用例数目少于5个，不能进行性能分析！', '提示', {
      //     confirmButtonText: '确定',
      //   })
      // }
    },

    result(projectName,taskName,deployPlan){
      let pdata = {}
      pdata.projectName = projectName
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
        this.formVisible = true
      }).catch(err => {
        this.$notify.error({
          title: '错误',
          message: '分析结果获取异常，请重试'
        })
      })
    },

    closeDialog(){
      this.mapData = []
    },

    handleDisable(row, index) {
      // 函数需要一个返回值,true为可选,false为不可选择
      // if (row.exeStatus==='1' && row.status === '未执行' && Number(row.testcases)>4) {
      //   return true
      // } else {
      //   return false
      // }

      if(row.exeStatus !== '1' || (row.analyseStatus === '1' && row.funcStatus === '1')){
        return false
      }else{
        return true
      }
    },

    filterTag(value, row) {
      return row.status === value;
    },

    validInput(str){
      if(str){
        var reg = /^(?!_)(?!.*?_$)[a-zA-Z0-9_\-\.\u4e00-\u9fa5]+$/
			  if (reg.test(str)) {
				  return true
			  } else {
          return false
        }
      }else{
        return true
      }
    },

    analyse_function(){ // 多任务功能分析
      if(this.$refs.multipleTable.selection[0]){ // 有勾选任务
        this.selectData = []
        for(let i=0;i<this.$refs.multipleTable.selection.length;i++){
          if(this.$refs.multipleTable.selection[i].functionStatus === '未执行' && this.$refs.multipleTable.selection[i].testResult.split(',')[1] !== '0'){
            this.selectData.push(this.$refs.multipleTable.selection[i])
          }
        }
        // this.selectData = this.$refs.multipleTable.selection
        let n = 0 // 第一个分析的任务状态为“执行中”，另外的都为“排队中”
        this.selectData.forEach(element => {
          n++     
          for(let i = 0;i<this.taskList.length;i++){
            if(this.taskList[i].id === element.id){
              // if(n === 1){
                this.taskList[i].functionStatus = '执行中'
              // }else{
                // this.taskList[i].functionStatus = '排队中'
              // }    
            }
          }
            saveResult({taskName:element.taskName,funcStatus:'0'})
            functionAnalysis({ projectName:element.projectName, taskId:element.taskName, applicationName:element.applicationName}).then(response => {
              saveResult({taskName:element.taskName,funcStatus:'1'})
              for(let j = 0;j<this.taskList.length;j++){
                if(this.taskList[j].id === element.id){
                  this.taskList[j].functionStatus = '已完成'
                }
              }
            }).catch(err => {
              saveResult({taskName:element.taskName,funcStatus:'-1'})
              for(let j = 0;j<this.taskList.length;j++){
                if(this.taskList[j].id === element.id){
                  this.taskList[j].functionStatus = '未执行'
                }
              }
              this.$notify.error({
                title: '错误',
                message: '功能缺陷分析异常，请重试'
              })
            })    
          })
      } else {
        this.$message({ // 没勾选任务
          message: this.$t('common.mustSelectOne'),
          type: 'warning'
        })
      }
    },

    analyseOneTask_function(index,projectName,taskName){ // 单个任务功能缺陷分析
      this.taskList[index].functionStatus = '执行中'
      saveResult({taskName:taskName,funcStatus:'0'})
      functionAnalysis({ projectName:projectName, taskId:taskName, applicationName:this.projectMap[projectName]}).then(response => {
        this.taskList[index].functionStatus = '已完成'
        saveResult({taskName:taskName,funcStatus:'1'})
      }).catch(err => {
        this.taskList[index].functionStatus = '未执行'
        saveResult({taskName:taskName,funcStatus:'-1'})
        this.$notify.error({
              title: '错误',
              message: '功能缺陷分析异常，请重试'
            })
      })
    },

    funcResult(projectName,taskName){ // 获取功能缺陷分析结果
      let pdata = {}
      pdata.projectName = projectName
      pdata.taskId = taskName
      functionResult(pdata).then(response => {
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
        this.formVisible_func = true
      }).catch(err => {
        this.$notify.error({
          title: '错误',
          message: '分析结果获取异常，请重试'
        })
      })
    },

  }
}
