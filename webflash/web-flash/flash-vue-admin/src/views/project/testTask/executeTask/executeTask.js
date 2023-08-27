/* eslint-disable no-unused-vars */
/* eslint-disable handle-callback-err */
import { getAllCaseList, editCase, harAnalysis } from '@/api/project/testCase'
import { getAllTaskList, getJobResult, saveResult, anomalyDetect, getAllExecInfo, getRootCause, getCurrentStatus, getTests } from '@/api/project/testTask'
import { getAllActivityList, getActivityList } from '@/api/project/testActivity'
import { getAllProList } from '@/api/project/projectList'

export default {
    data() {
        return {
            timer: '', // 设置定时器，定时刷新页面，定期调用getJobResult接口获取用例执行结果
            execTimer: '',
            count: 0, // 用例总数
            finish: 0, // 执行完的测试用例个数，finish/count就等于执行进度
            resultList: [], // 执行完成的结果数据列表，即getJobResult获取的结果列表
            query_taskName: '', // 从任务界面传过来的参数：任务名
            taskId: '', // 用于查询测试用例的任务id
            testactivityID: '', // 测试活动id，用于获取项目名
            projectName: '', // 项目名
            tableData: [], // 测试用例列表数据源
            percentage: 0, // 进度条百分数
            selRow: {},
            executionTime: '', // 任务执行完成时间,保存到任务列表对应字段
            isFinish: false, // 判断是否已经执行完
            finishExe: false, // 判断是否是一开始就执行完成的，若是一开始就完成的，则不必更新任务表
            sum: [], // 存储执行进度数据
            execFinish: false,	// 执行完成判断7
            message: '' // 测试信息
        }
    },

    created() {
        this.init()
    },

    mounted() {
        this.timer = setInterval(this.refreshPage, 1500) // 每十秒刷新一次页面
        this.execTimer = setInterval(this.refreshStatus, 1500)
    },

    beforeDestroy() {
        clearInterval(this.timer) // 销毁页面前清除计时器
    },

    methods: {
        init() {
            this.query_taskName = this.$route.params.taskName
            getAllTaskList({ taskName: this.query_taskName }).then(response => {
                this.taskId = response.data[0].id
                this.testactivityID = response.data[0].testactivityName
                if (response.data[0].executionTime) { // 任务已经执行完成过，是再次点进执行界面（即点击执行过程）
                    this.finishExe = true
                }
                this.refreshPage()
            })
        },

        refreshPage() { // 刷新测试信息
            if (this.execFinish) {
                clearInterval(this.execTimer)
                clearInterval(this.timer)
                this.timer = null
                this.execTimer = null
                return
            }
            getTests({ taskName: this.query_taskName }).then(response => {
                console.log('获取测试信息')
                this.message = response.data
                this.scrollToBottom()
            })
        },

        scrollToBottom() {
            // 将滚动条滚动到底部
            const messageDiv = this.$refs.message
            messageDiv.scrollTop = messageDiv.scrollHeight
        },

        refreshStatus: function() {
            if (this.execFinish) {
                clearInterval(this.execTimer)
                clearInterval(this.timer)
                this.timer = null
                this.execTimer = null
                return
            }
            getCurrentStatus({ taskName: this.query_taskName }).then(response => {
                console.log('getCurrentStatus response: ', JSON.stringify(response))
                const data = response.data
                if (data === '1') {
                    this.$notify({
                        title: '执行完成',
                        message: '执行完成，请回到任务列表开始分析'
                    })
                    // 取消计时器
                    clearInterval(this.execTimer)
                    clearInterval(this.timer)
                    this.timer = null
                    this.execTimer = null
                    this.execFinish = true
                //     // ④异常检测
                //     anomalyDetect({ taskName: this.query_taskName }).then(response => {
                //         console.log('anomalyDetect response: ', JSON.stringify(response))
                //         data = response.data
                //         if (data === '异常检测成功') {
                //             // ⑤获取所有数据
                //             getAllExecInfo({ taskName: this.query_taskName }).then(response => {
                //                 console.log('getAllExecInfo response: ', JSON.stringify(response))
                //                 data = response.data
                //                 if (data === '获取数据成功') {
                //                     // ⑥获取根因
                //                     getRootCause({ taskName: this.query_taskName }).then(response => {
                //                         console.log('getRootCause response: ', JSON.stringify(response))
                //                         data = response.data
                //                         if (data === '根因定位成功') {
                //                             this.$notify({
                //                                 title: '根因定位完成',
                //                                 message: '根因定位完成，请前往任务列表页面查看'
                //                             })
                //                             return
                //                         }
                //                     })
                //                 }
                //             })
                //         }
                //     })
                //     this.$notify.error({
                //         title: '根因定位异常',
                //         message: '根因定位异常'
                //     })
                }
            })
        }
    }
}
