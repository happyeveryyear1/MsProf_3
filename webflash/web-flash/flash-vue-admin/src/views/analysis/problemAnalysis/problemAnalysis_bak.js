import { getCurrentVersion, bottleneck, changeVersion, getSpecVersion } from '@/api/project/testCase'

export default {
    data() {
        return {
            list: [
                'ts-auth-service:12340 /api/v1/users/login',
                'ts-order-service:12031 /api/v1/orderservice/order/refresh',
                'ts-station-service:12345 /api/v1/stationservice/stations/namelist',
                'ts-consign-service:16111 /api/v1/consignservice/consigns/account/',
                'ts-admin-user-service:16115 /api/v1/adminuserservice/users',
                'ts-user-service:12342 /api/v1/userservice/users',
                'ts-admin-order-service:16112 /api/v1/adminorderservice/adminorder',
                'ts-order-service:12031 /api/v1/orderservice/order',
                'ts-order-other-service:12032 /api/v1/orderOtherService/orderOther',
                'ts-verification-code-service:15678 /api/v1/verifycode/generate'
            ], // 性能分析列表
            analyseResult: '', // 瓶颈分析结果
            currentVersion: '', // 当前版本号
            yamlPath: '', // yaml地址
            isAnalyse: false,
            isChange: false,
            listLoading: false,
            img: require('../../../assets/img/img1.png'),
            formVisible: false,
            formTitle: '当前版本信息',
            isAdd: false,
            versionIsChange: false,
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
            specVersionIsChange: false,
            specVersion: [
                { 'ts-order-service': -1 }
            ],
            specFormTitle: '特定服务版本',
            specVersionLoading: false,
            specFormVisible: false
        }
    },

    // created() {
    //   this.init()
    // },

    methods: {
    // init() {
    // },

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
                    } this.$notify.info({
                        title: 'Info',
                        message: '版本切换成功'
                    })
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
