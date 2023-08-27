import { getList } from '@/api/system/notice'
import { mapGetters } from 'vuex'
import ECharts from 'vue-echarts/components/ECharts'
import 'echarts/lib/chart/bar'
import 'echarts/lib/chart/line'
import 'echarts/lib/chart/pie'
import 'echarts/lib/chart/map'
import 'echarts/lib/chart/radar'
import 'echarts/lib/chart/scatter'
import 'echarts/lib/chart/effectScatter'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/polar'
import 'echarts/lib/component/geo'
import 'echarts/lib/component/legend'
import 'echarts/lib/component/title'
import 'echarts/lib/component/visualMap'
import 'echarts/lib/component/dataset'
import 'echarts/map/js/world'
import 'zrender/lib/svg/svg'

import { getAllProList } from '@/api/project/projectList' // 项目
import { getAllActivityList } from '@/api/project/testActivity' // 测试活动
import { getAllTaskList } from '@/api/project/testTask' // 测试任务

export default {

  name: 'dashboard',
  components: {
    chart: ECharts
  },
  data() {
    const data = []
    for (let i = 0; i <= 360; i++) { // 估计是画圆饼图
      const t = i / 180 * Math.PI
      const r = Math.sin(2 * t) * Math.cos(2 * t)
      data.push([r, i])
    }
    return { 
      loading: true,
      projectList: [], // 项目列表
      proIdSysMap: {}, // 项目id-sys匹配表
      proIdNameMap: {}, // 项目id-name匹配表
      proSysMap: { // 每个系统包含的项目id
        yx:[],
        sc:[],
        zc:[],
        cw:[],
        rz:[]
      },
      activityList: [], // 测试活动列表
      acIdSysMap: {}, // 测试活动id-sys匹配表
      acIdNameMap: {}, // 测试活动id-name匹配表
      taskList: [], // 任务列表
      taskIdMap: {
        yx: [],
        sc: [],
        zc: [],
        cw: [],
        rz: []
      }, // 任务sys匹配表

      projectNum: '', // 项目总数
      activityNum: '', // 测试活动总数
      taskNum: '', // 任务总数

      months: [], // 存储x轴的六个月份
      months_format: [], // 将六个月份转化成['2020-09','2020-08'...]的格式
      proList_month: [], // 存储半年内项目，[{ '2020-08-11': [{},{},{}]}, { ... }]
      proGrow_yx: [], // 项目增长数-营销系统（半年）
      proGrow_sc: [], // 项目增长数-生产系统（半年）
      proGrow_zc: [], // 项目增长数-资产系统（半年）
      proGrow_cw: [], // 项目增长数-财务系统（半年）
      proGrow_rz: [], // 项目增长数-人资系统（半年）

      pieData_project: {}, // 项目饼状图的系统分布数据，{ '营销系统': 5, '生产系统': 5,.... }
      pieData_activity: {}, // 测试活动的系统分布数据
      pieData_task: {}, // 测试任务的系统分布数据

      barData_project_system: '全部',  // 项目情况的下拉框绑定值
      allProList: [], // 全部项目情况
      yxProList: [], // 营销系统项目情况
      scProList: [], // 生产系统项目情况
      zcProList: [], // 资产系统项目情况
      cwProList: [], // 财务系统项目情况
      rzProList: [], // 人资系统项目情况

      mixData_system: '营销系统', // 近期任务用例执行情况下拉框绑定值

      barData_analyse_system: '营销系统', // 性能分析情况下拉框绑定值

      barData_evaluate_system: '营销系统', // 质量评估情况下拉框绑定值
      yxevaluate: {}, // 营销系统最近5个任务的评估情况，{'营销系统v1.0':[23,1]}
      scevaluate: {}, // 生产系统最近12个任务的评估情况
      zcevaluate: {}, // 资产系统最近12个任务的评估情况
      cwevaluate: {}, // 财务系统最近12个任务的评估情况
      rzevaluate: {}, // 人资系统最近12个任务的评估情况
       
      notice: [],
 
      pieData: { // 饼状图数据 
        title: {
          text: '系统占比',
          x: 'center'
        },
        tooltip: {
          trigger: 'item',
          // formatter: '{a} <br/>{b} : {c} ({d}%)'
          formatter: '{b} : {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['营销系统','生产系统','资产系统','财务系统','人资系统']
        },
        series: [ 
          {
            name: '',
            type: 'pie',
            radius: '55%',
            center: ['50%', '60%'],
            data: [
              { value: 0, name: '营销系统' },
              { value: 0, name: '生产系统' },
              { value: 0, name: '资产系统' },
              { value: 0, name: '财务系统' },
              { value: 0, name: '人资系统' }
            ],
            itemStyle: {
              emphasis: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            
          }
        ]
      },

      lineData: { // 【系统新增项目数】
        title: {
          text: '系统新增项目数（个/月）'
        },
        tooltip: { // 鼠标移到x轴会触发提示
          trigger: 'axis'
        },
        legend: {
          data: ['营销系统','生产系统','资产系统','财务系统','人资系统']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        toolbox: { // 工具盒，就是右上角的那个下载图片的图标
          feature: {
            saveAsImage: {}
          }
        }, 
        xAxis: { // x轴
          type: 'category',
          boundaryGap: false,
          width: '100%',
          data: []
        },
        yAxis: { // y轴  
          type: 'value'
        },
        series: [ // 数据
          {
            name: '营销系统',
            type: 'line',
            // stack: '总量',
            // data: [120, 132, 101, 134, 90, 230, 210]
            data: []
          },
          {
            name: '生产系统',
            type: 'line',
            // stack: '总量',
            // data: [220, 182, 191, 234, 290, 330, 310]
            data: []
          },
          {
            name: '资产系统',
            type: 'line',
            // stack: '总量',
            // data: [150, 232, 201, 154, 190, 330, 410]
            data: []
          },
          {
            name: '财务系统',
            type: 'line',
            // stack: '总量',
            // data: [320, 332, 301, 334, 390, 330, 320]
            data: []
          },
          {
            name: '人资系统',
            type: 'line',
            // stack: '总量',
            // data: [820, 932, 901, 934, 1290, 1330, 1320]
            data: []
          }
        ]
      },

      barData_project: { // 【项目情况】
        title: {
          text: '项目情况',
        },
        dataZoom : [
          {
            type: 'slider',
            show: true,
            start: 70,
            end: 100,
            handleSize: 3
          },
          {
            type: 'inside',
            start: 0,
            end: 100
          }, 
          // {
          //           type: 'slider',
          //           show: true,
          //           yAxisIndex: 0,
          //           filterMode: 'empty',
          //           width: 12,
          //           height: '70%',
          //           handleSize: 8,
          //           showDataShadow: false,
          //           left: '93%'
          //       }
        ],
        legend: {
          data: ['测试活动个数','任务个数']
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['营销系统v1.0','营销系统v2.0','营销系统v3.0','营销系统v4.0','营销系统v5.0','营销系统v6.0','营销系统v7.0','营销系统v8.0','营销系统v9.0','营销系统v10.0'],
        },
        yAxis: {
          type: 'value'
        },
        series: [  
          {
            name: '测试活动个数',
            // data: [5, 4, 1, 3, 1, 3, 2, 5, 4, 1, 3, 1, 3, 2],
            data: [],
            type: 'bar'
          },{
            name: '任务个数',
            // data: [120, 200, 150, 80, 70, 110, 130,120, 200, 150, 80, 70, 110, 130],
            data: [],
            type: 'bar'
          },
        ]
      },

      mixData:{ // 【(近期任务)用例执行情况】
        title: {
          text: '(近期任务)用例执行情况',
          left:'20px',
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          selectedMode: true, // 可点击
          data: ['用例执行成功率',  '用例执行成功个数', '用例执行失败个数'],
          // bottom: 0	
        },
        grid: {
          left: 100
        },
        xAxis: [  
          {
            type: 'category',
            // data: ['task-35-1', 'task-35-2', 'task-35-3', 'task-35-4', 'task-35-5', 'task-35-6', 'task-35-7', 'task-35-8', 'task-35-9', 'task-35-10', 'task-35-11', 'task-35-12'],
            data: [],
            splitLine: { // 不显示分割线
              show: false
            },
            axisLabel: {
              show: true,
              textStyle: {
                fontSize: 10
              }
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '用例执行成功率（%）',
            splitLine: { // 显示分割线
              show: false
            }
          },
          {
            type: 'value',
            name: '用例执行情况（个）',
//	             axisLabel: {
//	                formatter: '{value} °C'
//               },
            splitLine: {
                show: true
            }
          }
        ],
        series: [ 
          {
            name: '用例执行成功个数',
            type: 'bar',
            // data: [7, 4, 7, 14, 21, 7, 13, 16, 32, 20, 6, 3],
            data: [],
            stack:'111', // 堆叠
            itemStyle: {
              color:'rgba(132, 136, 211, 1)'
            }
          },
          {
            name: '用例执行失败个数',
            type: 'bar',
            stack:'111', // 堆叠
            // data: [1, 1, 1, 1, 2, 7, 1, 10, 3, 2, 6, 1],
            data: [],
            itemStyle: {
              color:'rgba(133,133,133,2)'
            }
          },
          {
            name: '用例执行成功率',
            type: 'line',
            yAxisIndex: 1, // 索引从0开始
            // data: [87.5, 80, 87.5, 93.3, 91.3, 50, 92.8, 61.5, 91.4, 90.9, 50, 75],
            data: [],
            itemStyle: {
              color:'rgba(91, 245, 204, 1)'
            }
          }
        ]
      },

      barData_analyse: { // 性能分析情况
        title: {
          text: '性能分析情况',
          subtext: '(最近项目)'
        },
        legend: {
          data: ['分析完成任务数','分析未完成任务数']
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          // data: ['营销系统v1.0','营销系统v2.0','营销系统v3.0','营销系统v4.0','营销系统v5.0'],
          data: [],
          axisLabel: {
            interval: 0,
            rotate: 20, // 20度角倾斜显示(***这里是关键)
            // textStyle: {
            //     color: '#00c5d7'
            // }
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '分析完成任务数',
            // data: [120, 200, 150, 80, 70, 110, 130],
            data: [],
            type: 'bar',
            // itemStyle: {
            //   color: 'rgba(91, 245, 204, 1)'
            // }
          },{
            name: '分析未完成任务数',
            // data: [10, 20, 15, 8, 7, 11, 13],
            data: [],
            type: 'bar'
          },
        ]
      },

      barData_evaluate: { // 质量评估情况
        title: {
          text: '质量评估情况',
          subtext: '(最近项目)'
        },
        legend: {
          data: ['评估完成任务数','评估未完成任务数']
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          // data: ['营销系统v1.0','营销系统v2.0','营销系统v3.0','营销系统v4.0','营销系统v5.0'],
          data: [],
          axisLabel: {
            interval: 0,
            rotate: 20, // 20度角倾斜显示(***这里是关键)
            // textStyle: {
            //     color: '#00c5d7'
            // }
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '评估完成任务数',
            // data: [120, 200, 150, 80, 70, 110, 130],
            data: [],
            type: 'bar'
          },{
            name: '评估未完成任务数',
            // data: [50, 40, 12, 33, 17, 31, 23],
            data: [],
            type: 'bar'
          },
        ]
      },
    }
  },

  computed: {
    ...mapGetters([
      'name'
    ])
  },

  created() {
    this.fetchData()
  },

  methods: {
    fetchData() {
      this.loading = true
      const self = this

      let nowDate = new Date(); // 获取【系统新增项目数】的x轴坐标
      let year = nowDate.getFullYear()
      let month = nowDate.getMonth()+2
      let month_format = ''
      for(let i=0;i<=5;i++){
        if(month === 1){
          year = year - 1
          month = 13
        }
        month = month - 1
        this.months.push(year+'年'+month+'月')
        if(month<10){
          month_format = '0'+month
        }else{
          month_format = month
        }
        this.months_format.push(year+'-'+month_format)
        this.proList_month[this.months_format[i]] = []
      }        
      this.lineData.xAxis.data = this.months.reverse()
      
      getAllProList().then(response => { // 获取所有项目
        this.projectList = response.data
        this.projectNum = response.data.length
        let pro_yx = 0
        let pro_sc = 0
        let pro_zc = 0
        let pro_cw = 0
        let pro_rz = 0
        let createTime_format = ''
        this.projectList.forEach(element => { // 获取项目列表
          this.proIdSysMap[element.id] = element.systemName
          this.proIdNameMap[element.id] = element.projectName
          this.allProList[element.projectName] = []
          this.allProList[element.projectName][0] = Number(element.activities)
          this.allProList[element.projectName][1] = 0
          if(element.systemName === '营销系统'){
            pro_yx++
            this.yxProList[element.projectName] = []
            this.yxProList[element.projectName][0] = Number(element.activities)
            this.yxProList[element.projectName][1] = 0
            this.proSysMap.yx.push(element.id)
          } else if(element.systemName === '生产系统'){
            pro_sc++
            this.scProList[element.projectName] = []
            this.scProList[element.projectName][0] = Number(element.activities)
            this.scProList[element.projectName][1] = 0
            this.proSysMap.sc.push(element.id)
          } else if(element.systemName === '资产系统'){
            pro_zc++
            this.zcProList[element.projectName] = []
            this.zcProList[element.projectName][0] = Number(element.activities)
            this.zcProList[element.projectName][1] = 0
            this.proSysMap.zc.push(element.id)
          } else if(element.systemName === '财务系统'){
            pro_cw++
            this.cwProList[element.projectName] = []
            this.cwProList[element.projectName][0] = Number(element.activities)
            this.cwProList[element.projectName][1] = 0
            this.proSysMap.cw.push(element.id)
          } else if(element.systemName === '人资系统'){
            pro_rz++
            this.rzProList[element.projectName] = []
            this.rzProList[element.projectName][0] = Number(element.activities)
            this.rzProList[element.projectName][1] = 0
            this.proSysMap.rz.push(element.id)
          }      
          createTime_format = element.createTime.split('-')[0] + '-' + element.createTime.split('-')[1]
          for(let i=0;i<this.months_format.length;i++){ // 将半年内的项目都存到proList_month里
            if(createTime_format === this.months_format[i]){
              this.proList_month[this.months_format[i]].push(element)
            }
          }
        })
        let fensys = []
        let fensys_yx = 0 
        let fensys_sc = 0 
        let fensys_zc = 0 
        let fensys_cw = 0 
        let fensys_rz = 0  
        for(let i=5; i>=0;i--){ // 将每个月份的数据取出来分成各系统数据，然后push到各自data里
          fensys = this.proList_month[this.months_format[i]]
          fensys_yx = 0
          fensys_sc = 0
          fensys_zc = 0
          fensys_cw = 0
          fensys_rz = 0
          for(let j=0;j<fensys.length;j++){
            if(fensys[j].systemName === '营销系统'){
              fensys_yx++
            }else if(fensys[j].systemName === '生产系统'){
              fensys_sc++
            }else if(fensys[j].systemName === '资产系统'){
              fensys_zc++
            }else if(fensys[j].systemName === '财务系统'){
              fensys_cw++
            }else if(fensys[j].systemName === '人资系统'){
              fensys_rz++
            }
          }
          this.proGrow_yx.push(fensys_yx)
          this.proGrow_sc.push(fensys_sc)
          this.proGrow_zc.push(fensys_zc)
          this.proGrow_cw.push(fensys_cw)
          this.proGrow_rz.push(fensys_rz)
        }
        this.$nextTick(() => {
          this.lineData.series[0].data = this.proGrow_yx
          this.lineData.series[1].data = this.proGrow_sc
          this.lineData.series[2].data = this.proGrow_zc
          this.lineData.series[3].data = this.proGrow_cw
          this.lineData.series[4].data = this.proGrow_rz
          console.log(this.lineData.series)
        })
        this.pieData_project['营销系统'] = pro_yx
        this.pieData_project['生产系统'] = pro_sc
        this.pieData_project['资产系统'] = pro_zc
        this.pieData_project['财务系统'] = pro_cw 
        this.pieData_project['人资系统'] = pro_rz  
        
        getAllActivityList().then(response => { // 获取测试活动列表
          this.activityList = response.data
          this.activityNum = response.data.length
          let ac_yx = 0
          let ac_sc = 0
          let ac_zc = 0
          let ac_cw = 0
          let ac_rz = 0
          this.activityList.forEach(element => {
            this.allProList[this.proIdNameMap[element.projectName]][1] = this.allProList[this.proIdNameMap[element.projectName]][1] + Number(element.tasks)
            if(this.proIdSysMap[element.projectName] === '营销系统'){
              this.acIdSysMap[element.id] = '营销系统'
              ac_yx++
              this.yxProList[this.proIdNameMap[element.projectName]][1] = this.yxProList[this.proIdNameMap[element.projectName]][1] + Number(element.tasks)
            } else if(this.proIdSysMap[element.projectName] === '生产系统'){
              this.acIdSysMap[element.id] = '生产系统'
              ac_sc++
              this.scProList[this.proIdNameMap[element.projectName]][1] = this.scProList[this.proIdNameMap[element.projectName]][1] + Number(element.tasks)
            } else if(this.proIdSysMap[element.projectName] === '资产系统'){
              this.acIdSysMap[element.id] = '资产系统'
              ac_zc++
              this.zcProList[this.proIdNameMap[element.projectName]][1] = this.zcProList[this.proIdNameMap[element.projectName]][1] + Number(element.tasks)
            } else if(this.proIdSysMap[element.projectName] === '财务系统'){
              this.acIdSysMap[element.id] = '财务系统'
              ac_cw++
              this.cwProList[this.proIdNameMap[element.projectName]][1] = this.cwProList[this.proIdNameMap[element.projectName]][1] + Number(element.tasks)

            } else if(this.proIdSysMap[element.projectName] === '人资系统'){
              this.acIdSysMap[element.id] = '人资系统'
              ac_rz++
              this.rzProList[this.proIdNameMap[element.projectName]][1] = this.rzProList[this.proIdNameMap[element.projectName]][1] + Number(element.tasks)
            }
          })
          this.pieData_activity['营销系统'] = ac_yx
          this.pieData_activity['生产系统'] = ac_sc
          this.pieData_activity['资产系统'] = ac_zc
          this.pieData_activity['财务系统'] = ac_cw 
          this.pieData_activity['人资系统'] = ac_rz 

          let xData = []   // 第二个图（项目新增），先显示所有项目
          let aData = []
          let tData = []
          for(var key in this.allProList){
            xData.push(key)
            aData.push(this.allProList[key][0])
            tData.push(this.allProList[key][1])
          }
          this.barData_project.xAxis.data = xData
          this.barData_project.series[0].data = aData
          this.barData_project.series[1].data = tData


          getAllTaskList().then(response => { // 获取任务列表
            this.taskList = response.data
            this.taskNum = response.data.length
            let task_yx = 0
            let task_sc = 0
            let task_zc = 0
            let task_cw = 0
            let task_rz = 0
            this.taskIdMap.yx=[]
            this.taskIdMap.sc=[]
            this.taskIdMap.zc=[]
            this.taskIdMap.cw=[]
            this.taskIdMap.rz=[]
            this.taskList.forEach(element => {
              
              if(this.acIdSysMap[element.testactivityName] === '营销系统'){
                this.taskIdMap.yx.push(element.taskName)
                task_yx++
              } else if(this.acIdSysMap[element.testactivityName] === '生产系统'){
                this.taskIdMap.sc.push(element.taskName)
                task_sc++
              } else if(this.acIdSysMap[element.testactivityName] === '资产系统'){
                this.taskIdMap.zc.push(element.taskName)
                task_zc++
              } else if(this.acIdSysMap[element.testactivityName] === '财务系统'){
                this.taskIdMap.cw.push(element.taskName)
                task_cw++
              } else if(this.acIdSysMap[element.testactivityName] === '人资系统'){
                this.taskIdMap.rz.push(element.taskName)
                task_rz++
              } 
            })

            this.pieData_task['营销系统'] = task_yx
            this.pieData_task['生产系统'] = task_sc
            this.pieData_task['资产系统'] = task_zc
            this.pieData_task['财务系统'] = task_cw 
            this.pieData_task['人资系统'] = task_rz  

             //this.mixData.xAxis[0].data   this.mixData.series[0].data[]
            
            let xData = [] // x轴数组
            let sData = [] // 用例执行成功数组
            let fData = [] // 用例执行失败数组
            let rData = [] // 用例执行成功率数组
            let cNum = 0 // 用例总数
            let sNum = 0 // 用例成功个数
            let fNum = 0 // 用例失败个数
            let rNum = 0 // 用例执行成功率
            let length = this.taskIdMap.yx.length
            if(length<12){ 
              for(let i=0;i<length;i++){
                xData.push(this.taskIdMap.yx[i])
                for(let j=0;j<this.taskList.length;j++){
                  if(this.taskIdMap.yx[i] === this.taskList[j].taskName){
                    if(this.taskList[j].testResult){
                      sNum = Number(this.taskList[j].testResult.split(',')[0])
                      fNum = Number(this.taskList[j].testResult.split(',')[1])
                      sData.push(sNum)
                      fData.push(fNum)
                      cNum = sNum + fNum
                      rNum = parseFloat(sNum/cNum*100).toFixed(2)
                      rData.push(rNum)
                    }else{
                      sData.push(0)
                      fData.push(0)
                      rData.push(parseFloat(0).toFixed(2))
                    }
                  }
                }
              }
            }else{  
              for(let i=12;i>=1;i--){
                xData.push(this.taskIdMap.yx[length-i])
                for(let j=0;j<this.taskList.length;j++){
                  if(this.taskIdMap.yx[length-i] === this.taskList[j].taskName){
                    if(this.taskList[j].testResult){
                      sNum = Number(this.taskList[j].testResult.split(',')[0])
                      fNum = Number(this.taskList[j].testResult.split(',')[1])
                      sData.push(sNum)
                      fData.push(fNum)
                      cNum = sNum + fNum
                      rNum = parseFloat(sNum/cNum*100).toFixed(2)
                      rData.push(rNum)
                    }else{
                      sData.push(0)
                      fData.push(0)
                      rData.push(parseFloat(0).toFixed(2))
                    }
                  }
                }
              }
            }
            this.mixData.xAxis[0].data = xData
            this.mixData.series[0].data = sData
            this.mixData.series[1].data = fData
            this.mixData.series[2].data = rData

            let analyse_xData = []
            let evaluate_xData = []
            let analyse_sData = []
            let evaluate_sData = []
            let analyse_fData = []
            let evaluate_fData = []
            let yx_length = this.proSysMap.yx.length
            let analyse_sNum = 0
            let evaluate_sNum = 0
            let analyse_fNum = 0
            let evaluate_fNum = 0
            let proId = ''
            if(yx_length>=5){
              for(let i=5;i>0;i--){
                proId = this.proSysMap.yx[yx_length-i]
                analyse_xData.push(this.proIdNameMap[proId])
                evaluate_xData.push(this.proIdNameMap[proId])
                for(let j=0;j<this.activityList.length;j++){
                  if(proId === this.activityList[j].projectName){
                    for(let k=0;k<this.taskList.length;k++){
                      if(this.activityList[j].id === this.taskList[k].testactivityName){
                        if(this.taskList[k].analyseStatus === '1'){
                          analyse_sNum++
                        }else{
                          analyse_fNum++
                        }
                        if(this.taskList[k].evaluateStatus === '1'){
                          evaluate_sNum++
                        }else{
                          evaluate_fNum++
                        }
                      }
                    }
                  }
                }
                analyse_sData.push(analyse_sNum)
                analyse_fData.push(analyse_fNum)
                evaluate_sData.push(evaluate_sNum)
                evaluate_fData.push(evaluate_fNum)
                analyse_sNum = 0
                analyse_fNum = 0
                evaluate_sNum = 0
                evaluate_fNum = 0
              }
            }else{
              for(let i=0;i<yx_length;i++){
                proId = this.proSysMap.yx[i]
                analyse_xData.push(this.proIdNameMap[proId])
                evaluate_xData.push(this.proIdNameMap[proId])
                for(let j=0;j<this.activityList.length;j++){
                  if(proId === this.activityList[j].projectName){
                    for(let k=0;k<this.taskList.length;k++){
                      if(this.activityList[j].id === this.taskList[k].testactivityName){
                        if(this.taskList[k].analyseStatus === '1'){
                          analyse_sNum++
                        }else{
                          analyse_fNum++
                        }
                        if(this.taskList[k].evaluateStatus === '1'){
                          evaluate_sNum++
                        }else{
                          evaluate_fNum++
                        }
                      }
                    }
                  }
                }
                analyse_sData.push(analyse_sNum)
                analyse_fData.push(analyse_fNum)
                evaluate_sData.push(evaluate_sNum)
                evaluate_fData.push(evaluate_fNum)
                analyse_sNum = 0
                analyse_fNum = 0
                evaluate_sNum = 0
                evaluate_fNum = 0
              }
            }
            this.barData_analyse.xAxis.data = analyse_xData
            this.barData_analyse.series[0].data = analyse_sData
            this.barData_analyse.series[1].data = analyse_fData
            this.barData_evaluate.xAxis.data = evaluate_xData
            this.barData_evaluate.series[0].data = evaluate_sData
            this.barData_evaluate.series[1].data = evaluate_fData  
            getList(self.listQuery).then(response => { // 获取右上角的那句提示：欢迎使用web-flash后台管理系统
              for (var i = 0; i < response.data.length; i++) {
                var notice = response.data[i]
                self.$notify({
                  title: notice.title,
                  message: notice.content,
                  duration: 3000
                })
              }    
            })
            this.loading = false 
          })
    
        })
      })
    }, 

    viewProjectNum(){ // 查看项目总数系统占比，即点击“项目总数”的数字，更新pieData的data值。
      this.$nextTick(() => {
        this.pieData.series[0].data[0].value = this.pieData_project['营销系统']
        this.pieData.series[0].data[1].value = this.pieData_project['生产系统']
        this.pieData.series[0].data[2].value = this.pieData_project['资产系统']
        this.pieData.series[0].data[3].value = this.pieData_project['财务系统']
        this.pieData.series[0].data[4].value = this.pieData_project['人资系统']
      })
    },

    viewActivityNum(){
      this.$nextTick(() => {
        this.pieData.series[0].data[0].value = this.pieData_activity['营销系统']
        this.pieData.series[0].data[1].value = this.pieData_activity['生产系统']
        this.pieData.series[0].data[2].value = this.pieData_activity['资产系统']
        this.pieData.series[0].data[3].value = this.pieData_activity['财务系统']
        this.pieData.series[0].data[4].value = this.pieData_activity['人资系统']
      })
    },

    viewTaskNum(){
      this.$nextTick(() => {
        this.pieData.series[0].data[0].value = this.pieData_task['营销系统']
        this.pieData.series[0].data[1].value = this.pieData_task['生产系统']
        this.pieData.series[0].data[2].value = this.pieData_task['资产系统']
        this.pieData.series[0].data[3].value = this.pieData_task['财务系统']
        this.pieData.series[0].data[4].value = this.pieData_task['人资系统']
      })
    },

    pro_changesys(){ // 第二个图（项目情况）更改系统
      let xData = []   
      let aData = []
      let tData = []
      let sys = []
      if(this.barData_project_system === '营销系统'){
        sys = this.yxProList
      }else if(this.barData_project_system === '生产系统'){
        sys = this.scProList
      }else if(this.barData_project_system === '资产系统'){
        sys = this.zcProList
      }else if(this.barData_project_system === '财务系统'){
        sys = this.cwProList
      }else if(this.barData_project_system === '人资系统'){
        sys = this.rzProList
      }else{
        sys = this.allProList
      }
      for(var key in sys){
        xData.push(key)
        aData.push(sys[key][0])
        tData.push(sys[key][1])
      }
        this.barData_project.xAxis.data = xData
        this.barData_project.series[0].data = aData
        this.barData_project.series[1].data = tData
    },

    task_changesys(){ // 第三个图（任务执行情况）更改系统
      let xData = [] // x轴数组
      let sData = [] // 用例执行成功数组
      let fData = [] // 用例执行失败数组
      let rData = [] // 用例执行成功率数组
      let cNum = 0 // 用例总数
      let sNum = 0 // 用例成功个数
      let fNum = 0 // 用例失败个数
      let rNum = 0 // 用例执行成功率
      let sys = ''
      if(this.mixData_system === '营销系统'){
        sys = this.taskIdMap.yx
      }else if(this.mixData_system === '生产系统'){
        sys = this.taskIdMap.sc
      }else if(this.mixData_system === '资产系统'){
        sys = this.taskIdMap.zc
      }else if(this.mixData_system === '财务系统'){
        sys = this.taskIdMap.cw
      }else if(this.mixData_system === '人资系统'){
        sys = this.taskIdMap.rz
      }
      let length = sys.length
      if(length<12){ 
        for(let i=0;i<length;i++){
          xData.push(sys[i])
          for(let j=0;j<this.taskList.length;j++){
            if(sys[i] === this.taskList[j].taskName){
              if(this.taskList[j].testResult){
                sNum = Number(this.taskList[j].testResult.split(',')[0])
                fNum = Number(this.taskList[j].testResult.split(',')[1])
                sData.push(sNum)
                fData.push(fNum)
                cNum = sNum + fNum
                rNum = parseFloat(sNum/cNum*100).toFixed(2)
                rData.push(rNum)
              }else{
                sData.push(0)
                fData.push(0)
                rData.push(parseFloat(0).toFixed(2))
              }
            }
          }
        }
      }else{  
        for(let i=12;i>=1;i--){
          xData.push(sys[length-i])
          for(let j=0;j<this.taskList.length;j++){
            if(sys[length-i] === this.taskList[j].taskName){
              if(this.taskList[j].testResult){
                sNum = Number(this.taskList[j].testResult.split(',')[0])
                fNum = Number(this.taskList[j].testResult.split(',')[1])
                sData.push(sNum)
                fData.push(fNum)
                cNum = sNum + fNum
                rNum = parseFloat(sNum/cNum*100).toFixed(2)
                rData.push(rNum)
              }else{
                sData.push(0)
                fData.push(0)
                rData.push(parseFloat(0).toFixed(2))
              }
            }
          } 
        }
      }      
      this.mixData.xAxis[0].data = xData
      this.mixData.series[0].data = sData
      this.mixData.series[1].data = fData
      this.mixData.series[2].data = rData
    },

    analyse_changesys(){
      let analyse_xData = []
      let analyse_sData = []
      let analyse_fData = []
      let sys = ''
      if(this.barData_analyse_system === '营销系统'){
        sys = this.proSysMap.yx
      }else if(this.barData_analyse_system === '生产系统'){
        sys = this.proSysMap.sc
      }else if(this.barData_analyse_system === '资产系统'){
        sys = this.proSysMap.zc
      }else if(this.barData_analyse_system === '财务系统'){
        sys = this.proSysMap.cw
      }else if(this.barData_analyse_system === '人资系统'){
        sys = this.proSysMap.rz
      }
      let length = sys.length
      let analyse_sNum = 0
      let analyse_fNum = 0
      let proId = ''
      if(length>=5){
        for(let i=5;i>0;i--){
          proId = sys[length-i]
          analyse_xData.push(this.proIdNameMap[proId])
          for(let j=0;j<this.activityList.length;j++){
            if(proId === this.activityList[j].projectName){
              for(let k=0;k<this.taskList.length;k++){
                if(this.activityList[j].id === this.taskList[k].testactivityName){
                  if(this.taskList[k].analyseStatus === '1'){
                    analyse_sNum++
                  }else{
                    analyse_fNum++
                  }
                }
              }
            }
          }
          analyse_sData.push(analyse_sNum)
          analyse_fData.push(analyse_fNum)
          analyse_sNum = 0
          analyse_fNum = 0
        }
      }else{
        for(let i=0;i<length;i++){
          proId = sys[i]
          analyse_xData.push(this.proIdNameMap[proId])
          for(let j=0;j<this.activityList.length;j++){
            if(proId === this.activityList[j].projectName){
              for(let k=0;k<this.taskList.length;k++){
                if(this.activityList[j].id === this.taskList[k].testactivityName){
                  if(this.taskList[k].analyseStatus === '1'){
                    analyse_sNum++
                  }else{
                    analyse_fNum++
                  }
                }
              }
            }
          }
          analyse_sData.push(analyse_sNum)
          analyse_fData.push(analyse_fNum)
          analyse_sNum = 0
          analyse_fNum = 0
        }
      }
      this.barData_analyse.xAxis.data = analyse_xData
      this.barData_analyse.series[0].data = analyse_sData
      this.barData_analyse.series[1].data = analyse_fData
    },

    evaluate_changesys(){
      let evaluate_xData = []
      let evaluate_sData = []
      let evaluate_fData = []
      let sys = ''
      if(this.barData_evaluate_system === '营销系统'){
        sys = this.proSysMap.yx
      }else if(this.barData_evaluate_system === '生产系统'){
        sys = this.proSysMap.sc
      }else if(this.barData_evaluate_system === '资产系统'){
        sys = this.proSysMap.zc
      }else if(this.barData_evaluate_system === '财务系统'){
        sys = this.proSysMap.cw
      }else if(this.barData_evaluate_system === '人资系统'){
        sys = this.proSysMap.rz
      }
      let length = sys.length
      let evaluate_sNum = 0
      let evaluate_fNum = 0
      let proId = ''
      if(length>=5){
        for(let i=5;i>0;i--){
          proId = sys[length-i]
          evaluate_xData.push(this.proIdNameMap[proId])
          for(let j=0;j<this.activityList.length;j++){
            if(proId === this.activityList[j].projectName){
              for(let k=0;k<this.taskList.length;k++){
                if(this.activityList[j].id === this.taskList[k].testactivityName){
                  if(this.taskList[k].evaluateStatus === '1'){
                    evaluate_sNum++
                  }else{
                    evaluate_fNum++
                  }
                }
              }
            }
          }
          evaluate_sData.push(evaluate_sNum)
          evaluate_fData.push(evaluate_fNum)
          evaluate_sNum = 0
          evaluate_fNum = 0
        }
      }else{
        for(let i=0;i<length;i++){
          proId = sys[i]
          evaluate_xData.push(this.proIdNameMap[proId])
          for(let j=0;j<this.activityList.length;j++){
            if(proId === this.activityList[j].projectName){
              for(let k=0;k<this.taskList.length;k++){
                if(this.activityList[j].id === this.taskList[k].testactivityName){
                  if(this.taskList[k].evaluateStatus === '1'){
                    evaluate_sNum++
                  }else{
                    evaluate_fNum++
                  }
                }
              }
            }
          }
          evaluate_sData.push(evaluate_sNum)
          evaluate_fData.push(evaluate_fNum)
          evaluate_sNum = 0
          evaluate_fNum = 0
        }
      }
      this.barData_evaluate.xAxis.data = evaluate_xData
      this.barData_evaluate.series[0].data = evaluate_sData
      this.barData_evaluate.series[1].data = evaluate_fData  
    }  
  },
  watch: {
    //观察option的变化
    option: {
      handler(newVal, oldVal) {
        if (this.barData_project) {
          if (newVal) {
            this.barData_project.setOption(newVal);
          } else {
            this.barData_project.setOption(oldVal); 
          }
        } else if(this.mixData) {
          if (newVal) {
            this.mixData.setOption(newVal);
          } else {
            this.mixData.setOption(oldVal); 
          }
        } else if(this.barData_analyse) {
          if (newVal) {
            this.barData_analyse.setOption(newVal);
          } else {
            this.barData_analyse.setOption(oldVal); 
          }
        } else if(this.barData_evaluate) {
          if (newVal) {
            this.barData_evaluate.setOption(newVal);
          } else {
            this.barData_evaluate.setOption(oldVal); 
          }
        } else if(this.pieData) {
          if (newVal) {
            this.pieData.setOption(newVal);
          } else {
            this.pieData.setOption(oldVal); 
          }
        }
      },
      deep: true //对象内部属性的监听，关键。
    },
  }
}
