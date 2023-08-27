/* eslint-disable handle-callback-err */
import { getCurrentVersion, bottleneck, changeVersion, getSpecVersion, tmp, getRecord, checkAnalysisResult, getAllRecord } from '@/api/project/testCase'

export default {
  data() {
    return {
      list: [
        { 'name': 'ts-auth-service:12340 /api/v1/users/login', 'version': '0.1' },
        { 'name': 'ts-auth-service:12340 /api/v1/users/login', 'version': '0.1' },
        { 'name': 'ts-auth-service:12340 /api/v1/users/login', 'version': '0.1' },
        { 'name': 'ts-auth-service:12340 /api/v1/users/login', 'version': '0.1' },
        { 'name': 'ts-auth-service:12340 /api/v1/users/login', 'version': '0.1' },
        { 'name': 'ts-auth-service:12340 /api/v1/users/login', 'version': '0.1' }
      ], // 性能分析列表
      analyseResult: '', // 瓶颈分析结果
      currentVersion: '', // 当前版本号
      yamlPath: '', // yaml地址
      isChange: false,
      listLoading: false,
      img: require('../../../assets/img/img1.png'),
      formVisible: false,
      listVisible: false,
      formTitle: '当前版本信息',
      listTitle: '瓶颈分析结果',
      analyzeIng: '性能分析中...',
      isAdd: false,
      versionIsChange: false,
      analysingVisible: false,
      isAnalyse: false,
      versionList: [
        { 'a': 'ts-admin-user-service', 'b': 0.1, 'c': 'ts-admin-travel-service', 'd': 0.1 },
        { 'a': 'ts-admin-route-service', 'b': 0.1, 'c': 'ts-admin-basic-info-service', 'd': 0.1 },
        { 'a': 'ts-admin-order-service', 'b': 0.1, 'c': 'ts-consign-price-service', 'd': 0.1 },
        { 'a': 'ts-consign-service', 'b': 0.1, 'c': 'ts-food-service', 'd': 0.1 },
        { 'a': 'ts-route-plan-service', 'b': 0.1, 'c': 'ts-food-map-service', 'd': 0.1 },
        { 'a': 'ts-travel-plan-service', 'b': 0.1, 'c': 'ts-seat-service', 'd': 0.1 },
        { 'a': 'ts-assurance-service', 'b': 0.1, 'c': 'ts-cancel-service', 'd': 0.1 },
        { 'a': 'ts-rebook-service', 'b': 0.1, 'c': 'ts-payment-service', 'd': 0.1 },
        { 'a': 'ts-execute-service', 'b': 0.1, 'c': 'ts-inside-payment-service', 'd': 0.1 },
        { 'a': 'ts-security-service', 'b': 0.1, 'c': 'ts-notification-service', 'd': 0.1 },
        { 'a': 'ts-price-service', 'b': 0.1, 'c': 'ts-ticketinfo-service', 'd': 0.1 },
        { 'a': 'ts-basic-service', 'b': 0.1, 'c': 'ts-preserve-other-service', 'd': 0.1 },
        { 'a': 'ts-preserve-service', 'b': 0.1, 'c': 'ts-travel2-service', 'd': 0.1 },
        { 'a': 'ts-travel-service', 'b': 0.1, 'c': 'ts-train-service', 'd': 0.1 },
        { 'a': 'ts-station-service', 'b': 0.1, 'c': 'ts-config-service', 'd': 0.1 },
        { 'a': 'ts-order-other-service', 'b': 0.1, 'c': 'ts-order-service', 'd': 0.1 },
        { 'a': 'ts-contacts-service', 'b': 0.1, 'c': 'ts-route-service', 'd': 0.1 },
        { 'a': 'ts-verification-code-service', 'b': 0.1, 'c': 'ts-user-service', 'd': 0.1 },
        { 'a': 'ts-auth-service', 'b': 0.1, 'c': '', 'd': '' }
      ],
      taskList: [
        { 'testactivityName': '工具测试1', 'taskName': 'task-1141-2', 'createTime': '2022-07-02 23:14:31', 'isAnalysised': '1' },
        { 'testactivityName': '工具测试1', 'taskName': 'task-1141-2', 'createTime': '2022-07-02 23:14:31', 'isAnalysised': '1' },
        { 'testactivityName': '工具测试1', 'taskName': 'task-1141-2', 'createTime': '2022-07-02 23:14:31', 'isAnalysised': '1' },
        { 'testactivityName': '工具测试1', 'taskName': 'task-1141-2', 'createTime': '2022-07-02 23:14:31', 'isAnalysised': '0' }
      ],
      specVersionIsChange: false,
      specVersion: [
        { 'svc_name': 'ts-order-service', 'svc_version': 0.1 }
      ],
      specFormTitle: '当前更新版本',
      specVersionLoading: false,
      specFormVisible: false,
      updateSvcVersionDisable: true,
      updateSvcVersionIsChange: false,
    }
  },

  created() {
    this.init()
  },

  methods: {
    init() {
      // tmp().then(response => {
      //   console.log('[****] response: ', response)
      // })
      const arg = { taskName: 'task3' }
      // getRecord(arg).then(response => {
      //   console.log('[*] getRecord: ', response)
      //   bottleneck(arg).then(response => {
      //     console.log('[*] bottleneck: ', response)
      //   })
      // })
      // checkAnalysisResult(arg).then(response => {
      //   console.log('[*] checkAnalysisResult: ', response)
      // })
      getAllRecord().then(response => {
        console.log('[*] getAllRecord: ', response)
        this.taskList = []
        const taskListTmp = []
        const taskData = response.data
        console.log('taskData: ', taskData)
        for (var key in taskData) {
          var taskEntity = taskData[key]
          const taskListEntity = {}
          taskListEntity.testactivityName = taskEntity[2]
          taskListEntity.taskName = taskEntity[1]
          taskListEntity.createTime = taskEntity[3]
          taskListEntity.isAnalysised = taskEntity[0]
          taskListTmp.push(taskListEntity)
        }
        console.log('taskList: ', JSON.stringify(taskListTmp))
        this.taskList = taskListTmp
        this.taskList.reverse()
        this.taskList = this.taskList.slice(0, 10)
        // console.log('rev taskList: ', JSON.stringify(this.taskList),  taskListTmp.reverse())
        // console.log('[*] taskList: ', this.taskList)
      })
    },

    isAnalysing(n) {
      const taskEntity = this.taskList[n]
      if (taskEntity['isAnalysised'] === '0') {
        return false
      } else if (taskEntity['isAnalysised'] === '1') {
        return false
      } else {
        return true
      }
    },

    getCurrentUpdateVersionFun(){
      this.updateSvcVersionIsChange = true
      getSpecVersion().then(response => {
        console.log('[*] specVersion response: ', response)
        response = JSON.parse(JSON.stringify(response))
        const data = response.data
        let svc_version = data.version
        if (svc_version == '1'){
          svc_version = '0.1'
        }else if (svc_version == '2'){
          svc_version = '0.2'
        }
        this.specVersion = [
          { 'svc_name': data.svc, 'svc_version': svc_version }
        ]
        this.specFormVisible = true
        this.updateSvcVersionIsChange = false
      }).catch(err => {
        console.log('[*] specVersion err: ', err)
        this.updateSvcVersionIsChange = false
      })
    },


    // 1: 分析完成, 2: 性能分析中，0: 未分析
    getName(n) {
      const taskEntity = this.taskList[n]
      if (taskEntity['isAnalysised'] === '0') {
        return '性能分析'
      } else if (taskEntity['isAnalysised'] === '1') {
        return '查看结果'
      } else {
        return '分析中'
      }
    },

    clickCheckorAnalyze(n) {
      const taskEntity = this.taskList[n]
      const arg = { taskName: taskEntity['taskName'] }
      if (taskEntity['isAnalysised'] === '0') {
        taskEntity['isAnalysised'] = '2'
        // this.analysingVisible = true
        // 性能分析
        bottleneck(arg).then(response => {
          response = JSON.parse(JSON.stringify(response))
          setTimeout(function () {
            console.log('[*] analyse response2', response)
            taskEntity['isAnalysised'] = '1'
        }, Math.floor(Math.random()*20) + 3000);
          // this.analysingVisible = false
        })
      } else {
        // 展示结果
        this.listVisible = true
        checkAnalysisResult(arg).then(response => {
          console.log('[*] checkAnalysisResult: ', response)
          let analysisResult = response.data
          this.list = []
          for (let i = 0; i < Math.min(analysisResult.length, 10); i++) {
            const listEntity = {}
            listEntity['name'] = analysisResult[i].svcName
            listEntity['version'] = analysisResult[i].svcVersion
            this.list.push(listEntity)
          }
        })
      }
    },

    analyse() {
      this.isAnalyse = true
      this.listLoading = true
      this.list = []
      bottleneck().then(response => {
        console.log('[*] analyse response', response)
        response = JSON.parse(JSON.stringify(response))
        const data = response.data.data
        this.isAnalyse = false
        for (let i = 0; i < data.length; i++) {
          let a = {}
          a = data[i]
          this.list.push(a)
        }
        this.listLoading = false
        const date = new Date()
        const dateValue = date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate()
        this.analyseResult = '瓶颈分析结果-' + dateValue
      }).catch(err => {
        console.log('[*] analysis err: ', err)
        this.isAnalyse = false
        this.listLoading = false
        this.$notify.error({
          title: '错误',
          message: '性能分析异常'
        })
      })
    },

    changeVersion() {
      this.isChange = true
      if (this.yamlPath) {
        changeVersion({ yamlPath: this.yamlPath }).then(response => {
          this.isChange = false
          if (response.data.description === 'ok') {
            this.currentVersion = response.data.version
            this.updateSvcVersionDisable = false
            this.$notify.info({
              title: 'Info',
              message: '版本切换成功'
            })
          } else if (response.data.description === 'err1') {
            this.$notify.error({
              title: '错误',
              message: '路径不存在'
            })
          } else if (response.data.description === 'err2') {
            this.$notify.error({
              title: '错误',
              message: 'yaml文件内容错误'
            })
          } else if (response.data.description === 'err3') {
            this.$notify.error({
              title: '错误',
              message: '版本切换错误'
            })
          } else {
            this.$notify.error({
              title: '错误',
              message: '版本重复'
            })
          } 
        }).catch(err => {
          this.isChange = false
          this.$notify.error({
            title: '错误',
            message: '版本切换异常'
          })
        })
      } else {
        this.isChange = false
        this.$notify.error({
          title: '错误',
          message: 'yaml路径不能为空'
        })
      }
    },

    getSpecVersionFun() {
      this.specVersionIsChange = true
      getSpecVersion().then(response => {
        console.log('[*] specVersion response: ', response)
        response = JSON.parse(JSON.stringify(response))
        const data = response.data
        this.specVersion = [
          { 'ts-order-service': data.version }
        ]
        this.specFormVisible = true
        this.specVersionIsChange = false
      }).catch(err => {
        console.log('[*] specVersion err: ', err)
        this.specVersionIsChange = false
      })
    },

    getCurrentVersionFun() {
      this.versionIsChange = true
      getCurrentVersion().then(response => {
        console.log('[*] currentVersion response: ', response)
        response = JSON.parse(JSON.stringify(response))
        const data = response.data
        const versionListData = data.versions
        const serviceListData = data.svc
        const versionListTmp = []
        const len = versionListData.length
        for (let i = 0; i < len; i = i + 2) {
          const entry = {}
          entry['a'] = serviceListData[i]
          entry['b'] = versionListData[i]
          entry['c'] = serviceListData[i + 1]
          entry['d'] = versionListData[i + 1]
          versionListTmp.push(entry)
        }
        this.versionList = versionListTmp
        if (response.data.description === 'err') {
          this.versionIsChange = false
          this.$notify.error({
            title: '错误',
            message: '获取版本失败'
          })
        } else {
          this.formTitle = '当前版本信息'
          this.formVisible = true
          this.versionIsChange = false
        }
      }).catch(err => {
        console.log('[*] currentVersion err: ', err)
        this.versionIsChange = false
        this.$notify.error({
          title: '错误',
          message: '获取当前版本异常'
        })
      })
    }
  }

}
