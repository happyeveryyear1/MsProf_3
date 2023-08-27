/* eslint-disable handle-callback-err */
import { resultCheck, getHarDepData } from '@/api/project/testCase'
import JsonViewer from 'vue-json-viewer'

export default {
    components: {
        JsonViewer
    },
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
            HarData: {
                total: 25,
                limit: 10,
                skip: 0,
                links: {
                    previous: undefined,
                    next: function() {}
                },
                data: [
                    {
                        id: '5968fcad629fa84ab65a5247',
                        firstname: 'Ada',
                        lastname: 'Lovelace',
                        awards: null,
                        known: [
                            'mathematics',
                            'computing'
                        ],
                        position: {
                            lat: 44.563836,
                            lng: 6.495139
                        },
                        description: `Augusta Ada King, Countess of Lovelace (née Byron; 10 December 1815 – 27 November 1852) was an English mathematician and writer,
                    chiefly known for her work on Charles Babbage's proposed mechanical general-purpose computer,
                    the Analytical Engine. She was the first to recognise that the machine had applications beyond pure calculation,
                    and published the first algorithm intended to be carried out by such a machine.
                    As a result, she is sometimes regarded as the first to recognise the full potential of a "computing machine" and the first computer programmer.`,
                        bornAt: '1815-12-10T00:00:00.000Z',
                        diedAt: '1852-11-27T00:00:00.000Z'
                    }, {
                        id: '5968fcad629fa84ab65a5246',
                        firstname: 'Grace',
                        lastname: 'Hopper',
                        awards: [
                            'Defense Distinguished Service Medal',
                            'Legion of Merit',
                            'Meritorious Service Medal',
                            'American Campaign Medal',
                            'World War II Victory Medal',
                            'National Defense Service Medal',
                            'Armed Forces Reserve Medal',
                            'Naval Reserve Medal',
                            'Presidential Medal of Freedom'
                        ],
                        known: null,
                        position: {
                            lat: 43.614624,
                            lng: 3.879995
                        },
                        description: `Grace Brewster Murray Hopper (née Murray; December 9, 1906 – January 1, 1992)
                    was an American computer scientist and United States Navy rear admiral.
                    One of the first programmers of the Harvard Mark I computer,
                    she was a pioneer of computer programming who invented one of the first compiler related tools.
                    She popularized the idea of machine-independent programming languages, which led to the development of COBOL,
                    an early high-level programming language still in use today.`,
                        bornAt: '1815-12-10T00:00:00.000Z',
                        diedAt: '1852-11-27T00:00:00.000Z'
                    }
                ]
            },
            DependencyData: {
                total: 25,
                limit: 10,
                skip: 0,
                links: {
                    previous: undefined,
                    next: function() {}
                },
                data: [
                    {
                        id: '5968fcad629fa84ab65a5247',
                        firstname: 'Ada',
                        lastname: 'Lovelace',
                        awards: null,
                        known: [
                            'mathematics',
                            'computing'
                        ],
                        position: {
                            lat: 44.563836,
                            lng: 6.495139
                        },
                        description: `Augusta Ada King, Countess of Lovelace (née Byron; 10 December 1815 – 27 November 1852) was an English mathematician and writer,
                    chiefly known for her work on Charles Babbage's proposed mechanical general-purpose computer,
                    the Analytical Engine. She was the first to recognise that the machine had applications beyond pure calculation,
                    and published the first algorithm intended to be carried out by such a machine.
                    As a result, she is sometimes regarded as the first to recognise the full potential of a "computing machine" and the first computer programmer.`,
                        bornAt: '1815-12-10T00:00:00.000Z',
                        diedAt: '1852-11-27T00:00:00.000Z'
                    }, {
                        id: '5968fcad629fa84ab65a5246',
                        firstname: 'Grace',
                        lastname: 'Hopper',
                        awards: [
                            'Defense Distinguished Service Medal',
                            'Legion of Merit',
                            'Meritorious Service Medal',
                            'American Campaign Medal',
                            'World War II Victory Medal',
                            'National Defense Service Medal',
                            'Armed Forces Reserve Medal',
                            'Naval Reserve Medal',
                            'Presidential Medal of Freedom'
                        ],
                        known: null,
                        position: {
                            lat: 43.614624,
                            lng: 3.879995
                        },
                        description: `Grace Brewster Murray Hopper (née Murray; December 9, 1906 – January 1, 1992)
                    was an American computer scientist and United States Navy rear admiral.
                    One of the first programmers of the Harvard Mark I computer,
                    she was a pioneer of computer programming who invented one of the first compiler related tools.
                    She popularized the idea of machine-independent programming languages, which led to the development of COBOL,
                    an early high-level programming language still in use today.`,
                        bornAt: '1815-12-10T00:00:00.000Z',
                        diedAt: '1852-11-27T00:00:00.000Z'
                    }
                ]
            },
            jsonData: {
                total: 25,
                limit: 10,
                skip: 0,
                links: {
                    previous: undefined,
                    next: function() {}
                },
                data: [
                    {
                        id: '5968fcad629fa84ab65a5247',
                        firstname: 'Ada',
                        lastname: 'Lovelace',
                        awards: null,
                        known: [
                            'mathematics',
                            'computing'
                        ],
                        position: {
                            lat: 44.563836,
                            lng: 6.495139
                        },
                        description: `Augusta Ada King, Countess of Lovelace (née Byron; 10 December 1815 – 27 November 1852) was an English mathematician and writer,
                    chiefly known for her work on Charles Babbage's proposed mechanical general-purpose computer,
                    the Analytical Engine. She was the first to recognise that the machine had applications beyond pure calculation,
                    and published the first algorithm intended to be carried out by such a machine.
                    As a result, she is sometimes regarded as the first to recognise the full potential of a "computing machine" and the first computer programmer.`,
                        bornAt: '1815-12-10T00:00:00.000Z',
                        diedAt: '1852-11-27T00:00:00.000Z'
                    }, {
                        id: '5968fcad629fa84ab65a5246',
                        firstname: 'Grace',
                        lastname: 'Hopper',
                        awards: [
                            'Defense Distinguished Service Medal',
                            'Legion of Merit',
                            'Meritorious Service Medal',
                            'American Campaign Medal',
                            'World War II Victory Medal',
                            'National Defense Service Medal',
                            'Armed Forces Reserve Medal',
                            'Naval Reserve Medal',
                            'Presidential Medal of Freedom'
                        ],
                        known: null,
                        position: {
                            lat: 43.614624,
                            lng: 3.879995
                        },
                        description: `Grace Brewster Murray Hopper (née Murray; December 9, 1906 – January 1, 1992)
                    was an American computer scientist and United States Navy rear admiral.
                    One of the first programmers of the Harvard Mark I computer,
                    she was a pioneer of computer programming who invented one of the first compiler related tools.
                    She popularized the idea of machine-independent programming languages, which led to the development of COBOL,
                    an early high-level programming language still in use today.`,
                        bornAt: '1815-12-10T00:00:00.000Z',
                        diedAt: '1852-11-27T00:00:00.000Z'
                    }
                ]
            }
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
            this.listLoading = true
            this.query_taskName = this.$route.params.taskName
            getHarDepData({ taskName: this.query_taskName }).then(response => {
                console.log('aaaaa')
                console.log(response)
                this.HarData = JSON.parse(response.data.harJSON)
                this.DependencyData = JSON.parse(response.data.requestMap)
            }).catch(err => {
                this.$notify.error({
                    title: '错误',
                    message: '接口异常，请关闭界面重试'
                })
            })
        }

    },

    getPageData() { // 获取pageInfo接口数据
        const pdata = {}
        pdata.projectName = this.projectName
        pdata.taskId = this.query_taskName
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

