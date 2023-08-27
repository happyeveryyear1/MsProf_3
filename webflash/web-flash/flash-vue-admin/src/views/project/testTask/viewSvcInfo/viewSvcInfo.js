/* eslint-disable no-unused-vars */
/* eslint-disable handle-callback-err */
import { getAllCaseList, resultCheck, pageNumber } from '@/api/project/testCase'
import { getAllTaskList, getAllExecInfo } from '@/api/project/testTask'
import { getAllActivityList } from '@/api/project/testActivity'
import { getAllProList } from '@/api/project/projectList'

export default {
    data() {
        return {
            ListColSize: 4, // 列表占比空间
            InfoColSize: 20, // 监控信息占比空间
            tableData: [], // 列表数据源
            caseList: '', // 测试用例列表
            pageList: '', // 分页url列表
            query_taskName: '', // 从任务界面接收的taskName
            taskId: '', // 任务ID，用于查询测试用例数据
            projectName: '', // 项目名
            dataNum: 0, // 用例总个数
            har: '', // 查看用例监控信息时的har地址，iframe的数据源src
            iframeHar: '', // 查看用例监控信息时的har地址，resultCheck接口获取的数据
            iframePinpoint: '', // 查看用例监控信息时的pinpoint地址，resultCheck接口获取的数据
            pinpoint: '', // 查看用例监控信息时的pinpoint地址，iframe的数据源src
            viewCase: true, // 判定此时为查看用例监控，为true时展示的是用例列表，为false时展示分页表
            pageInfo: '', // 单页监控信息
            keys: [], // pageinfo接口获取的map数据中所有key的数组，其实也就是所有page的url数组
            srcData: {}, // pageinfo接口获取的数据
            pages: [], // 包含两个参数：pageUrl,hasInfo，前者是存储url，后者判断pageinfo接口是否已经返回结果，为0时无结果，为1时有结果。其实就是分页表的数据源
            pageNum: '', // pagenumber接口返回的条数，即页数
            listLoading: false,
            timer: null, // 设置pageInfo的定时器
            isPage: false, // 为true时是url列表，用于控制tab的可用状态
            isCase: true, // 为true时是用例列表，用于控制tab的可用状态
            activeName: 'har', // 默认tab最初在“har”页
            activeIndex: '1', // 默认菜单最初为“用例”， 为1时是用例，为2时是分页
            shrinkStatus: false, // 用于控制“收起”按钮的可用状态
            expandStatus: false, // 用于控制“展开”按钮的可用状态
            expandLevel: '0', // 用于表示菜单扩展的级别，控制按钮图标状态
            applicationName: '', // resultCheck接口所需的参数
            deployPlan: '', // resultCheck接口所需的参数
            pageTab: true, // 控制分页tab可否点击
            fack_model: '',
            tableData2: [
                {
                    id: '12987122',
                    name: '王小虎',
                    amount1: '234',
                    amount2: '3.2',
                    amount3: 10
                }, {
                    id: '12987123',
                    name: '王小虎',
                    amount1: '165',
                    amount2: '4.43',
                    amount3: 12
                }, {
                    id: '12987124',
                    name: '王小虎',
                    amount1: '324',
                    amount2: '1.9',
                    amount3: 9
                }, {
                    id: '12987125',
                    name: '王小虎',
                    amount1: '621',
                    amount2: '2.2',
                    amount3: 17
                }, {
                    id: '12987126',
                    name: '王小虎',
                    amount1: '539',
                    amount2: '4.1',
                    amount3: 15
                }
            ],
            svclist2: [
                { name: 'aaa', interface: ['bbb', 'ccc'] },
                { name: 'aaa', interface: ['bbb', 'ccc'] },
                { name: 'aaa', interface: ['bbb', 'ccc'] },
                { name: 'aaa', interface: ['bbb', 'ccc'] }
            ],
            svcList_tmp: [
                {
                    id: 1,
                    svcName: 'travelService',
                    interfaceName: '',
                    proportion: '20/500',
                    children: [{
                        id: 2,
                        svcName: 'travelService',
                        interfaceName: '/api/v1/travelservice/trips/left',
                        proportion: '10/500'
                    }, {
                        id: 3,
                        svcName: 'travelService',
                        interfaceName: '/api/v1/travelservice/trips/left',
                        proportion: '10/500'
                    }]
                }, {
                    id: 4,
                    svcName: 'travelService',
                    interfaceName: '',
                    proportion: '20/500',
                    children: [{
                        id: 5,
                        svcName: 'travelService',
                        interfaceName: '/api/v1/travelservice/trips/left',
                        proportion: '10/500'
                    }, {
                        id: 6,
                        svcName: 'travelService',
                        interfaceName: '/api/v1/travelservice/trips/left',
                        proportion: '10/500'
                    }]
                }
            ],
            svcList_TMP2: JSON.parse('[ {\r\n  "id" : 1,\r\n  "svcName" : "ts-inside-payment-1.0",\r\n  "interfaceName" : "",\r\n  "proportion" : "24202/241601",\r\n  "children" : [ {\r\n    "id" : 2,\r\n    "svcName" : "ts-inside-payment-1.0",\r\n    "interfaceName" : "/api/v1/inside_pay_service/inside_payment",\r\n    "proportion" : "20/241601",\r\n    "children" : [ ]\r\n  } ]\r\n}, {\r\n  "id" : 3,\r\n  "svcName" : "ts-order-other-1.0",\r\n  "interfaceName" : "",\r\n  "proportion" : "46532/241601",\r\n  "children" : [ {\r\n    "id" : 4,\r\n    "svcName" : "ts-order-other-1.0",\r\n    "interfaceName" : "/api/v1/orderOtherService/orderOther/refresh",\r\n    "proportion" : "46532/241601",\r\n    "children" : [ ]\r\n  } ]\r\n}, {\r\n  "id" : 5,\r\n  "svcName" : "ts-travel-plan-1.0",\r\n  "interfaceName" : "",\r\n  "proportion" : "51781/241601",\r\n  "children" : [ {\r\n    "id" : 6,\r\n    "svcName" : "ts-travel-plan-1.0",\r\n    "interfaceName" : "/api/v1/travelplanservice/travelPlan/cheapest",\r\n    "proportion" : "16217/241601",\r\n    "children" : [ ]\r\n  }, {\r\n    "id" : 7,\r\n    "svcName" : "ts-travel-plan-1.0",\r\n    "interfaceName" : "/api/v1/travelplanservice/travelPlan/minStation",\r\n    "proportion" : "21513/241601",\r\n    "children" : [ ]\r\n  }, {\r\n    "id" : 8,\r\n    "svcName" : "ts-travel-plan-1.0",\r\n    "interfaceName" : "/api/v1/travelplanservice/travelPlan/quickest",\r\n    "proportion" : "14051/241601",\r\n    "children" : [ ]\r\n  } ]\r\n}, {\r\n  "id" : 9,\r\n  "svcName" : "ts-travel2-1.0",\r\n  "interfaceName" : "",\r\n  "proportion" : "21421/241601",\r\n  "children" : [ {\r\n    "id" : 10,\r\n    "svcName" : "ts-travel2-1.0",\r\n    "interfaceName" : "/api/v1/travel2service/trips/left",\r\n    "proportion" : "40/241601",\r\n    "children" : [ ]\r\n  } ]\r\n}, {\r\n  "id" : 11,\r\n  "svcName" : "ts-order-1.0",\r\n  "interfaceName" : "",\r\n  "proportion" : "31631/241601",\r\n  "children" : [ {\r\n    "id" : 12,\r\n    "svcName" : "ts-order-1.0",\r\n    "interfaceName" : "/api/v1/orderservice/order/refresh",\r\n    "proportion" : "42151/241601",\r\n    "children" : [ ]\r\n  } ]\r\n}, {\r\n  "id" : 13,\r\n  "svcName" : "ts-travel-1.0",\r\n  "interfaceName" : "",\r\n  "proportion" : "49719/241601",\r\n  "children" : [ {\r\n    "id" : 14,\r\n    "svcName" : "ts-travel-1.0",\r\n    "interfaceName" : "/api/v1/travelservice/trips/left",\r\n    "proportion" : "39199/241601",\r\n    "children" : [ ]\r\n  } ]\r\n}, {\r\n  "id" : 15,\r\n  "svcName" : "ts-auth-1.0",\r\n  "interfaceName" : "",\r\n  "proportion" : "16315/241601",\r\n  "children" : [ {\r\n    "id" : 16,\r\n    "svcName" : "ts-auth-1.0",\r\n    "interfaceName" : "/api/v1/users/login",\r\n    "proportion" : "20/241601",\r\n    "children" : [ ]\r\n  } ]\r\n} ]'),
            svcList: []
        }
    },

    created() {
        this.init()
    },

    mounted() {
        this.timer = setInterval(this.getPageData, 60000) // 每一分钟获取一次pageInfo接口数据
    },

    beforeDestroy() {
        clearInterval(this.timer)
    },

    methods: {
        init() {
            this.fetchData()
        },

        enlargeColSize() { // 展开列表
            if (this.expandLevel === '0') { // 放大到全屏
                this.ListColSize = 24
                this.InfoColSize = 0
                this.expandStatus = true
                this.expandLevel = '1'
            } else { // 放大到六分之一
                this.ListColSize = 4
                this.InfoColSize = 20
                this.expandLevel = '0'
                this.shrinkStatus = false
            }
        },

        reduceColSize() { // 收起列表
            if (this.expandLevel === '0') { // 缩小到全无
                this.ListColSize = 0
                this.InfoColSize = 24
                this.shrinkStatus = true
                this.expandLevel = '-1'
            } else { // 缩小到六分之一
                this.ListColSize = 4
                this.InfoColSize = 20
                this.expandLevel = '0'
                this.expandStatus = false
            }
        },

        fetchData() {
            const task_name = this.$route.params.taskName
            this.taskName = task_name
            getAllExecInfo({ taskName: task_name }).then(response => {
                console.log('svcList: ', response)
                this.svcList = JSON.parse(response.data)
            })
        },

        getPageData() { // 获取pageInfo接口数据
            const pdata = {}
            pdata.projectName = this.projectName
            pdata.taskId = this.query_taskName
            // pageInfo(pdata).then(response => {
            //   this.keys = []
            //   this.srcData = response.data.data
            //   for(var key in this.srcData){
            //     this.keys.push(key)
            //   }
            //   if(this.keys.length === this.pageNum){ // 若页数够了就证明sitespeed分析完成，可以停止轮询了
            //   clearInterval(this.timer)
            //   }
            //   for(let i=0;i<this.keys.length;i++){ // 匹配url列表，若pageInfo已返回信息，则将pages列表中hasInfo参数改为1,表示分析完成
            //     for(let j=0;j<this.pageNum;j++){
            //       if(this.keys[i] === this.pages[j].pageUrl){
            //         this.pages[j].hasInfo = 1
            //       }
            //     }
            //   }
            // })
        },

        viewMonitoringInfo_case(testcaseName, createTime, costTime) { // 获取用例的har和pinpoint页面的url
            if (this.activeName === 'har') {
                this.pinpoint = ''
            } else {
                this.har = ''
            }
            const pdata = {}
            pdata.projectName = this.projectName
            pdata.taskId = this.query_taskName
            pdata.testId = testcaseName.split('.')[0]
            const period = parseInt(Number(costTime) / 1000) + 1
            pdata.period = period + 's'
            pdata.endTime = createTime.replace(' ', '-').replace(':', '-').replace(':', '-')
            pdata.applicationName = this.applicationName
            pdata.deployPlan = this.deployPlan
            console.log('viewMonitoringInfo pdata: ', pdata)
            resultCheck(pdata).then(response => {
                console.log('viewMonitoringInfo response: ', response)
                const urls = response.data.data
                this.iframeHar = urls[0]
                this.iframePinpoint = urls[1]
                if (this.activeName === 'har') {
                    this.har = urls[0]
                } else {
                    this.pinpoint = urls[1]
                }
                if (this.expandLevel === '1') {
                    this.reduceColSize()
                }
            }).catch(err => {
                this.$notify.error({
                    title: '错误',
                    message: '接口异常'
                })
            })
        },

        viewMonitoringInfo_page(pageUrl) { // 获取某个页面url的sitespeed信息
            this.pageInfo = this.srcData[pageUrl]
            if (this.expandLevel === '1') {
                this.reduceColSize()
            }
        },

        viewPages() { // 切换“分页”菜单
            this.tableData = this.pages
            this.har = ''
            this.pinpoint = ''
            this.viewCase = false
            this.isPage = true
            this.isCase = false
            this.activeName = 'sitespeed'
        },

        viewCases() { // 切换“用例”菜单
            this.pageInfo = ''
            this.tableData = this.caseList
            this.viewCase = true
            this.isCase = true
            this.isPage = false
            this.activeName = 'har'
        },

        openUrl() {
            if (this.activeName === 'har' && this.har) {
                window.open(this.har, '_blank')
            } else if (this.activeName === 'pinpoint' && this.pinpoint) {
                window.open(this.pinpoint, '_blank')
            } else if (this.activeName === 'sitespeed' && this.pageInfo) {
                window.open(this.pageInfo, '_blank')
            } else {
                this.$alert('无数据', '提示', {
                    confirmButtonText: '确定'
                })
            }
        },

        handleClick(tab, event) { // 获取tab的lebel
            this.activeName = tab.label
            if (this.activeName === 'pinpoint') {
                this.pinpoint = this.iframePinpoint
            } else if (this.activeName === 'har') {
                this.har = this.iframeHar
            }
        }
    }
}
