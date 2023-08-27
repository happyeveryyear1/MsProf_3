import { getCaseList } from '@/api/project/testCase'
import { getJobResultFile, getJobResult, getAllTaskList } from '@/api/project/testTask'
import { default as AnsiUp } from 'ansi_up'

export default {
    data() {
        return {
            tableData: [], // 树形表格数据
            rowKey: '1', // 给树形表格排序的rowkey
            formTitle_report: '测试报告', // 测试报告dialog的标题
            formVisible_report: false, // 测试报告dialog
            formTitle_screenShot: '错误截图', // 错误截图dialog的标题
            formVisible_screenShot: false, // 错误截图dialog
            formTitle_video: '用例执行过程视频', // 用例执行过程视频dialog的标题
            formVisible_video: false, // 用例执行过程视频dialog
            listQuery: {
                page: 1,
                limit: 1000,
                taskName: undefined
            },
            params_taskName: '', // 从任务界面接收到的参数taskName
            total: 0, // 用例总数
            list: null, // 用例列表
            listLoading: true,
            selRow: {},
            log: '', // 测试报告
            screenShotSrc: 'data:image/png;base64,', // 截图
            videoSrc: 'data:video/mp4;base64,' // 视频
        }
    },

    created() {
        this.init()
    },

    methods: {
        init() {
            this.query_taskName = this.$route.params.taskName
            this.listLoading = true
            getAllTaskList({ taskName: this.query_taskName }).then(response => {
                this.listQuery.taskName = response.data[0].id
                this.fetchData()
            })
        },

        fetchData() {
            getCaseList(this.listQuery).then(response => {
                this.list = response.data.records
                this.total = response.data.total
                this.rowKey = '1'
                this.tableData = JSON.parse(JSON.stringify(this.list))
                this.tableData = this.module_unique(this.tableData) // 过滤第一层数据
                this.tableData.forEach(element => { // 添加children字段和rowkey
                    this.$set(element, 'children', [])
                    element.rowId = this.rowKey
                    element.role = 'parent'
                    // element.testcaseName = element.menuModule.replace(element.menuModule.split('/')[0]+'/','')
                    element.testcaseName = element.menuModule
                    this.rowKey = Number(this.rowKey) + Number(1)
                })
                this.tableData.forEach(i => { // 给每个菜单模块添加children
                    var success = 0 // 成功个数
                    var fail = 0 // 失败个数
                    var cost = 0 // 时间消耗
                    this.list.forEach(element => { //
                        if (element.menuModule === i.menuModule) {
                            element.rowId = this.rowKey
                            element.role = 'children'
                            if (Number(i.executionTime.split(' ')[0].replace(/-/g, '')) < Number(element.executionTime.split(' ')[0].replace(/-/g, ''))) {
                                i.executionTime = element.executionTime
                            }
                            if (element.executionResult === '1,0') {
                                success = success + 1
                            } else {
                                fail = fail + 1
                            }
                            cost += Number(element.costTime)
                            i.children.push(element)
                            this.rowKey = Number(this.rowKey) + Number(1)
                        }
                    })
                    i.executionResult = success + ',' + fail
                    i.costTime = cost
                })
                this.listLoading = false
            })
        },

        module_unique(arr) { // 模块去重
            const res = new Map()
            return arr.filter((arr) => !res.has(arr.menuModule) && res.set(arr.menuModule))
        },

        tableRowClassName({ row }) {
            if (row.role === 'parent') {
                return 'parent'
            }
            return ''
        },

        detailedInfoClass(i) { // 那三个图标显不显示
            switch (i) {
            case 'parent': return 'parentDetailedInfo'
            case 'children': return 'childrenDetailedInfo'
            }
        },

        viewReport(logId) { // 查看测试报告
            this.formVisible_report = true
            getJobResult({ jobId: this.query_taskName }).then(response => { // 调用接口方法请求测试报告
                const result = response.data.data
                for (let i = 0; i < result.length; i++) {
                    if (logId === result[i].id) {
                        this.log = result[i].log
                    }
                    var ansi_up = new AnsiUp()
                    this.html = ansi_up.ansi_to_html(this.log)
                    console.log(this.html)
                    var cdiv = document.getElementById('log')
                    cdiv.innerHTML = this.html
                }
            }).catch(err => {
                this.$notify.error({
                    title: '错误',
                    message: '测试报告查看异常，请重试'
                })
            })
        },

        viewScreenShot(picPath) { // 查看错误截图
            getJobResultFile({ filePath: picPath }).then(response => { // 调用接口方法请求错误截图
                const fileContent = response.data.data.fileContent
                this.screenShotSrc += fileContent
                this.formVisible_screenShot = true
            }).catch(err => {
                this.$notify.error({
                    title: '错误',
                    message: '截图查看异常，请重试'
                })
            })
        },

        viewVideo(videoPath) { // 查看用例执行过程视频
            getJobResultFile({ filePath: videoPath }).then(response => { // 调用接口方法请求用例执行过程视频
                const fileContent = response.data.data.fileContent
                this.videoSrc += fileContent
                this.formVisible_video = true
            }).catch(err => {
                this.$notify.error({
                    title: '错误',
                    message: '视频查看异常，请重试'
                })
            })
        },

        closeDialog() { // 清除表单验证残留信息
            this.log = '', // 测试报告
            this.screenShotSrc = 'data:image/png;base64,', // 截图
            this.videoSrc = 'data:video/mp4;base64,' // 视频
        }
    }
}
