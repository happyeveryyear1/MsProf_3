import { measureVer } from '@/api/quality/version'
import { getTaskList, saveResult } from '@/api/project/testTask'
import { getAllProList } from '@/api/project/projectList'
import { getAllActivityList } from '@/api/project/testActivity'
import { pageInfo,pageNumber } from '@/api/project/testCase'

export default {
  data() {
    return {
      list: [],  // 表格数据源
      percentages: {}, // taskName:百分数。的格式，表示评估进度
      itv: {}, // 设置timer，也是map映射情况。
      total: 0, // 列表数据总数
      proj_id: null, // 项目列表传过来的项目id
      proj_name: '', // 项目列表穿过的项目id对应的项目名
      listLoading: true,
      search_taskName: '',
      listQuery: {
        page: 1,
        limit: 10,
        testactivityName: undefined, // 测试活动名
        taskName: undefined,  // 任务名
        evaluateStatus: undefined // 评估情况
      },
      select_testactivityName: '', // 查询条件框-测试活动名绑定的值
      select_testactivityID: '', // 查询条件框-测试活动名绑定的值的id
      select_status: '', // 查询条件框-评估状态绑定的值
      testactivityList: [], // 测试活动列表
    }
  },
  created() {
    this.init()
  },

  methods: {
    init(){
      this.proj_id = this.$route.params.id
      getAllProList({id:this.proj_id}).then(response => {
        this.proj_name = response.data[0].projectName
        getAllActivityList({projectName:this.proj_id}).then(response => {
          this.testactivityList = response.data
          if(this.testactivityList.length){
            this.select_testactivityName = this.testactivityList[0].testactivityName
            this.select_testactivityID = this.testactivityList[0].id
          }
          this.fetchData()
        })
      })
    },

    fetchData(){
      this.listLoading = true
      if(this.select_status){
        if(this.select_status === '已完成'){
          this.listQuery.evaluateStatus = '1'
        }else{
          this.listQuery.evaluateStatus = '-1'
        }
      }
      if(this.search_taskName){
        this.listQuery.taskName = encodeURI(this.search_taskName)
      }else{
        this.listQuery.taskName = ''
      }
      if(this.select_testactivityName){         
        for(let i=0;i<this.testactivityList.length;i++){
          if(this.select_testactivityName === this.testactivityList[i].testactivityName){
            this.listQuery.testactivityName = this.testactivityList[i].id
            this.select_testactivityID = this.testactivityList[i].id
            break
          }
        }
      }else{
        this.listQuery.testactivityName = 0
      } 
      getTaskList(this.listQuery).then(response => {
        this.list = response.data.records
        this.total = response.data.total
        this.list.forEach(element => {
          if(element.evaluateStatus === '1'){
            this.percentages[element.taskName] = 100
          }
        })
        this.listLoading = false
      })       
    },

    measure(taskName,index) {
      this.list[index].evaluateStatus = '0'
      if(this.list[index].exeStatus === '-1'){
        this.$alert('任务未执行不能进行质量评估（点击任务名可以跳转到任务界面）', '提示', {
          confirmButtonText: '确定',
          })
        this.list[index].evaluateStatus = '-1'
      }else if(this.list[index].exeStatus === '0'){
        this.$alert('任务正在执行中不能进行质量评估', '提示', {
          confirmButtonText: '确定',
        })
        this.list[index].evaluateStatus = '-1'
      }else{
        let pageNum = 0
        let pageInfosNum = 0
        let pdata = {}
        pdata.projectName = this.proj_name
        pdata.taskId = taskName
        pageNumber(pdata).then(response => {
          pageNum = response.data.data.length
          pageInfo(pdata).then(response => {
            this.srcData = response.data.data
            for(var key in this.srcData){
              pageInfosNum ++
            }
            if(pageNum === pageInfosNum){
              this.list[index].evaluateStatus = '0'
              this.percentages[taskName] = 0
              let _percentages = {}
              for (let tag in this.percentages) {
                _percentages[tag] = this.percentages[tag]
              }
              this.itv[taskName] = setInterval(() => {
                _percentages[taskName] += 10
                for (let tag in this.percentages) {
                  if (this.percentages[tag] >= _percentages[tag]){
                    _percentages[tag] = this.percentages[tag]
                  }
                }
                this.percentages = _percentages
                if(_percentages[taskName]>=90) {
                  clearInterval(this.itv[taskName])
                }
              },1600)
              let queryData = {}
              queryData['ver_tag'] = taskName
              queryData['proj_id'] = this.proj_id
              measureVer(queryData).then(response => {
                this.$nextTick(() => {
                _percentages[taskName] = 100
                for (let tag in this.percentages) {
                  if (this.percentages[tag] >= _percentages[tag]){
                    _percentages[tag] = this.percentages[tag]
                  }
                }
                this.percentages = _percentages
                clearInterval(this.itv[taskName])
                this.list[index].evaluateStatus = '1'
                saveResult({taskName:taskName,evaluateStatus:'1'})
                })
              }).catch(err => {
                console.log(err)
                clearInterval(this.itv[taskName])
                this.list[index].evaluateStatus = '-1'
              })
            }else{
              this.$alert('sitespeed未分析完成，请稍后再试', '提示', {
              confirmButtonText: '确定',
              })
              this.list[index].evaluateStatus = '-1'
            }  
          }).catch(err => {
            this.list[index].evaluateStatus = '-1'
          })
        }).catch(err => {
          this.list[index].evaluateStatus = '-1'
        })
      }
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

    reset(){
      this.listQuery.testactivityName = ''
      this.listQuery.taskName = ''
      this.listQuery.evaluateStatus = ''
      this.select_testactivityName = this.testactivityList[0].testactivityName
      this.select_status = ''
      this.search_taskName = ''
      this.fetchData()
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
  }
}