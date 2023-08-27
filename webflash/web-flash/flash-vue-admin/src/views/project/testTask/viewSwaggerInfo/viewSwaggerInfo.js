/* eslint-disable handle-callback-err */
import { resultCheck, getSwaggerDepData } from '@/api/project/testCase'
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
            jsonData: {
                'Station': {
                    'type': 'object',
                    'properties': {
                        'id': {
                            'type': 'string'
                        },
                        'name': {
                            'type': 'string'
                        },
                        'stayTime': {
                            'type': 'integer',
                            'format': 'int32'
                        }
                    }
                },
                'HttpEntity': {
                    'type': 'object',
                    'properties': {
                        'body': {
                            'type': 'object'
                        }
                    }
                },
                'Contacts': {
                    'type': 'object',
                    'properties': {
                        'accountId': {
                            'type': 'string'
                        },
                        'documentNumber': {
                            'type': 'string'
                        },
                        'documentType': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'id': {
                            'type': 'string'
                        },
                        'name': {
                            'type': 'string'
                        },
                        'phoneNumber': {
                            'type': 'string'
                        }
                    }
                },
                'Config': {
                    'type': 'object',
                    'properties': {
                        'description': {
                            'type': 'string'
                        },
                        'name': {
                            'type': 'string'
                        },
                        'value': {
                            'type': 'string'
                        }
                    }
                },
                'TrainType': {
                    'type': 'object',
                    'properties': {
                        'averageSpeed': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'confortClass': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'economyClass': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'id': {
                            'type': 'string'
                        }
                    }
                },
                'PriceInfo': {
                    'type': 'object',
                    'properties': {
                        'basicPriceRate': {
                            'type': 'number',
                            'format': 'double'
                        },
                        'firstClassPriceRate': {
                            'type': 'number',
                            'format': 'double'
                        },
                        'id': {
                            'type': 'string'
                        },
                        'routeId': {
                            'type': 'string'
                        },
                        'trainType': {
                            'type': 'string'
                        }
                    }
                },
                'Order': {
                    'type': 'object',
                    'properties': {
                        'accountId': {
                            'type': 'string'
                        },
                        'boughtDate': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'coachNumber': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'contactsDocumentNumber': {
                            'type': 'string'
                        },
                        'contactsName': {
                            'type': 'string'
                        },
                        'documentType': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'from': {
                            'type': 'string'
                        },
                        'id': {
                            'type': 'string'
                        },
                        'price': {
                            'type': 'string'
                        },
                        'seatClass': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'seatNumber': {
                            'type': 'string'
                        },
                        'status': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'to': {
                            'type': 'string'
                        },
                        'trainNumber': {
                            'type': 'string'
                        },
                        'travelDate': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'travelTime': {
                            'type': 'string',
                            'format': 'date-time'
                        }
                    }
                },
                'RouteInfo': {
                    'type': 'object',
                    'properties': {
                        'distanceList': {
                            'type': 'string'
                        },
                        'endStation': {
                            'type': 'string'
                        },
                        'id': {
                            'type': 'string'
                        },
                        'loginId': {
                            'type': 'string'
                        },
                        'startStation': {
                            'type': 'string'
                        },
                        'stationList': {
                            'type': 'string'
                        }
                    }
                },
                'Consign': {
                    'type': 'object',
                    'properties': {
                        'accountId': {
                            'type': 'string'
                        },
                        'consignee': {
                            'type': 'string'
                        },
                        'from': {
                            'type': 'string'
                        },
                        'handleDate': {
                            'type': 'string'
                        },
                        'id': {
                            'type': 'string'
                        },
                        'orderId': {
                            'type': 'string'
                        },
                        'phone': {
                            'type': 'string'
                        },
                        'targetDate': {
                            'type': 'string'
                        },
                        'to': {
                            'type': 'string'
                        },
                        'weight': {
                            'type': 'number',
                            'format': 'double'
                        },
                        'within': {
                            'type': 'boolean'
                        }
                    }
                },
                'FoodOrder': {
                    'type': 'object',
                    'properties': {
                        'foodName': {
                            'type': 'string'
                        },
                        'foodType': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'id': {
                            'type': 'string'
                        },
                        'orderId': {
                            'type': 'string'
                        },
                        'price': {
                            'type': 'number',
                            'format': 'double'
                        },
                        'stationName': {
                            'type': 'string'
                        },
                        'storeName': {
                            'type': 'string'
                        }
                    }
                },
                'Seat': {
                    'type': 'object',
                    'properties': {
                        'destStation': {
                            'type': 'string'
                        },
                        'seatType': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'startStation': {
                            'type': 'string'
                        },
                        'trainNumber': {
                            'type': 'string'
                        },
                        'travelDate': {
                            'type': 'string',
                            'format': 'date-time'
                        }
                    }
                },
                'OrderTicketsInfo': {
                    'type': 'object',
                    'properties': {
                        'accountId': {
                            'type': 'string'
                        },
                        'assurance': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'consigneeName': {
                            'type': 'string'
                        },
                        'consigneePhone': {
                            'type': 'string'
                        },
                        'consigneeWeight': {
                            'type': 'number',
                            'format': 'double'
                        },
                        'contactsId': {
                            'type': 'string'
                        },
                        'date': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'foodName': {
                            'type': 'string'
                        },
                        'foodPrice': {
                            'type': 'number',
                            'format': 'double'
                        },
                        'foodType': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'from': {
                            'type': 'string'
                        },
                        'handleDate': {
                            'type': 'string'
                        },
                        'seatType': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'stationName': {
                            'type': 'string'
                        },
                        'storeName': {
                            'type': 'string'
                        },
                        'to': {
                            'type': 'string'
                        },
                        'tripId': {
                            'type': 'string'
                        },
                        'within': {
                            'type': 'boolean'
                        }
                    }
                },
                'RebookInfo': {
                    'type': 'object',
                    'properties': {
                        'date': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'loginId': {
                            'type': 'string'
                        },
                        'oldTripId': {
                            'type': 'string'
                        },
                        'orderId': {
                            'type': 'string'
                        },
                        'seatType': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'tripId': {
                            'type': 'string'
                        }
                    }
                },
                'SecurityConfig': {
                    'type': 'object',
                    'properties': {
                        'description': {
                            'type': 'string'
                        },
                        'id': {
                            'type': 'string'
                        },
                        'name': {
                            'type': 'string'
                        },
                        'value': {
                            'type': 'string'
                        }
                    }
                },
                'Response': {
                    'type': 'object',
                    'properties': {
                        'data': {
                            'type': 'object'
                        },
                        'msg': {
                            'type': 'string'
                        },
                        'status': {
                            'type': 'integer',
                            'format': 'int32'
                        }
                    }
                },
                'TripInfo': {
                    'type': 'object',
                    'properties': {
                        'departureTime': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'endPlace': {
                            'type': 'string'
                        },
                        'startingPlace': {
                            'type': 'string'
                        }
                    }
                },
                'TransferTravelInfo': {
                    'type': 'object',
                    'properties': {
                        'fromStationName': {
                            'type': 'string'
                        },
                        'toStationName': {
                            'type': 'string'
                        },
                        'trainType': {
                            'type': 'string'
                        },
                        'travelDate': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'viaStationName': {
                            'type': 'string'
                        }
                    }
                },
                'TripAllDetailInfo': {
                    'type': 'object',
                    'properties': {
                        'from': {
                            'type': 'string'
                        },
                        'to': {
                            'type': 'string'
                        },
                        'travelDate': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'tripId': {
                            'type': 'string'
                        }
                    }
                },
                'TravelInfo': {
                    'type': 'object',
                    'properties': {
                        'endTime': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'routeId': {
                            'type': 'string'
                        },
                        'startingStationId': {
                            'type': 'string'
                        },
                        'startingTime': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'stationsId': {
                            'type': 'string'
                        },
                        'terminalStationId': {
                            'type': 'string'
                        },
                        'trainTypeId': {
                            'type': 'string'
                        },
                        'tripId': {
                            'type': 'string'
                        }
                    }
                },
                'UserDto': {
                    'type': 'object',
                    'properties': {
                        'documentNum': {
                            'type': 'string'
                        },
                        'documentType': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'email': {
                            'type': 'string'
                        },
                        'gender': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'password': {
                            'type': 'string'
                        },
                        'userName': {
                            'type': 'string'
                        }
                    }
                },
                'PaymentInfo': {
                    'type': 'object',
                    'properties': {
                        'orderId': {
                            'type': 'string'
                        },
                        'price': {
                            'type': 'string'
                        },
                        'tripId': {
                            'type': 'string'
                        },
                        'userId': {
                            'type': 'string'
                        }
                    }
                },
                'AccountInfo': {
                    'type': 'object',
                    'properties': {
                        'money': {
                            'type': 'string'
                        },
                        'userId': {
                            'type': 'string'
                        }
                    }
                },
                'NotifyInfo': {
                    'type': 'object',
                    'properties': {
                        'date': {
                            'type': 'string'
                        },
                        'email': {
                            'type': 'string'
                        },
                        'endPlace': {
                            'type': 'string'
                        },
                        'orderNumber': {
                            'type': 'string'
                        },
                        'price': {
                            'type': 'string'
                        },
                        'seatClass': {
                            'type': 'string'
                        },
                        'seatNumber': {
                            'type': 'string'
                        },
                        'startingPlace': {
                            'type': 'string'
                        },
                        'startingTime': {
                            'type': 'string'
                        },
                        'username': {
                            'type': 'string'
                        }
                    }
                },
                'QueryInfo': {
                    'type': 'object',
                    'properties': {
                        'boughtDateEnd': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'boughtDateStart': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'enableBoughtDateQuery': {
                            'type': 'boolean'
                        },
                        'enableStateQuery': {
                            'type': 'boolean'
                        },
                        'enableTravelDateQuery': {
                            'type': 'boolean'
                        },
                        'loginId': {
                            'type': 'string'
                        },
                        'state': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'travelDateEnd': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'travelDateStart': {
                            'type': 'string',
                            'format': 'date-time'
                        }
                    }
                },
                'OrderInfo': {
                    'type': 'object',
                    'properties': {
                        'boughtDateEnd': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'boughtDateStart': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'enableBoughtDateQuery': {
                            'type': 'boolean'
                        },
                        'enableStateQuery': {
                            'type': 'boolean'
                        },
                        'enableTravelDateQuery': {
                            'type': 'boolean'
                        },
                        'loginId': {
                            'type': 'string'
                        },
                        'state': {
                            'type': 'integer',
                            'format': 'int32'
                        },
                        'travelDateEnd': {
                            'type': 'string',
                            'format': 'date-time'
                        },
                        'travelDateStart': {
                            'type': 'string',
                            'format': 'date-time'
                        }
                    }
                },
                'BasicAuthDto': {
                    'type': 'object',
                    'properties': {
                        'password': {
                            'type': 'string'
                        },
                        'username': {
                            'type': 'string'
                        },
                        'verificationCode': {
                            'type': 'string'
                        }
                    }
                }

            },
            jsonData2: {
                'Station': {
                    '新增': [
                        '/api/v1/adminbasicservice/adminbasic/stations'
                    ],
                    '删除': [
                        '/api/v1/adminbasicservice/adminbasic/stations'
                    ],
                    '修改': [
                        '/api/v1/adminbasicservice/adminbasic/stations'
                    ]
                },
                'TrainType': {
                    '新增': [
                        '/api/v1/adminbasicservice/adminbasic/trains',
                        '/api/v1/trainservice/trains'
                    ],
                    '修改': [
                        '/api/v1/adminbasicservice/adminbasic/trains',
                        '/api/v1/trainservice/trains'
                    ]
                },
                'BasicAuthDto': {
                    '新增': [
                        '/api/v1/users/login'
                    ]
                },
                'FoodOrder': {
                    '新增': [
                        '/api/v1/foodservice/orders'
                    ],
                    '修改': [
                        '/api/v1/foodservice/orders'
                    ]
                },
                'AccountInfo': {
                    '新增': [
                        '/api/v1/inside_pay_service/inside_payment/account'
                    ]
                },
                'OrderInfo': {
                    '新增': [
                        '/api/v1/orderservice/order/refresh'
                    ]
                },
                'Config': {
                    '新增': [
                        '/api/v1/adminbasicservice/adminbasic/configs',
                        '/api/v1/configservice/configs'
                    ],
                    '修改': [
                        '/api/v1/adminbasicservice/adminbasic/configs',
                        '/api/v1/configservice/configs'
                    ]
                },
                'PriceInfo': {
                    '新增': [
                        '/api/v1/adminbasicservice/adminbasic/prices'
                    ],
                    '删除': [
                        '/api/v1/adminbasicservice/adminbasic/prices'
                    ],
                    '修改': [
                        '/api/v1/adminbasicservice/adminbasic/prices'
                    ]
                },
                'TravelInfo': {
                    '新增': [
                        '/api/v1/admintravelservice/admintravel'
                    ],
                    '修改': [
                        '/api/v1/admintravelservice/admintravel'
                    ]
                },
                'RebookInfo': {
                    '新增': [
                        '/api/v1/rebookservice/rebook',
                        '/api/v1/rebookservice/rebook/difference'
                    ]
                },
                'RouteInfo': {
                    '新增': [
                        '/api/v1/adminrouteservice/adminroute'
                    ]
                },
                'Order': {
                    '新增': [
                        '/api/v1/adminorderservice/adminorder'
                    ],
                    '修改': [
                        '/api/v1/adminorderservice/adminorder'
                    ]
                },
                'TripInfo': {
                    '新增': [
                        '/api/v1/travelplanservice/travelPlan/cheapest',
                        '/api/v1/travelplanservice/travelPlan/minStation',
                        '/api/v1/travelplanservice/travelPlan/quickest',
                        '/api/v1/travelservice/trips/left',
                        '/api/v1/travel2service/trips/left'
                    ]
                },
                'SecurityConfig': {
                    '新增': [
                        '/api/v1/securityservice/securityConfigs'
                    ],
                    '修改': [
                        '/api/v1/securityservice/securityConfigs'
                    ]
                },
                'NotifyInfo': {
                    '新增': [
                        '/api/v1/notifyservice/notification/order_changed_success',
                        '/api/v1/notifyservice/notification/order_create_success',
                        '/api/v1/notifyservice/notification/preserve_success'
                    ]
                },
                'OrderTicketsInfo': {
                    '新增': [
                        '/api/v1/preserveotherservice/preserveOther',
                        '/api/v1/preserveservice/preserve'
                    ]
                },
                'PaymentInfo': {
                    '新增': [
                        '/api/v1/inside_pay_service/inside_payment',
                        '/api/v1/inside_pay_service/inside_payment/difference'
                    ]
                },
                'Consign': {
                    '新增': [
                        '/api/v1/consignservice/consigns'
                    ],
                    '修改': [
                        '/api/v1/consignservice/consigns'
                    ]
                },
                'Contacts': {
                    '新增': [
                        '/api/v1/adminbasicservice/adminbasic/contacts',
                        '/api/v1/contactservice/contacts',
                        '/api/v1/contactservice/contacts/admin'
                    ],
                    '修改': [
                        '/api/v1/adminbasicservice/adminbasic/contacts',
                        '/api/v1/contactservice/contacts'
                    ]
                },
                'UserDto': {
                    '新增': [
                        '/api/v1/adminuserservice/users'
                    ],
                    '修改': [
                        '/api/v1/adminuserservice/users'
                    ]
                },
                'QueryInfo': {
                    '新增': [
                        '/api/v1/orderOtherService/orderOther/refresh'
                    ]
                }
            },
            resList: {},
            resProdConsDep: {}

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
            getSwaggerDepData({ taskName: this.query_taskName }).then(response => {
                console.log('aaaaa')
                console.log(response)
                this.resList = JSON.parse(response.data.resList)
                this.resProdConsDep = JSON.parse(response.data.resProdConsDep)
            }).catch(err => {
                this.$notify.error({
                    title: '错误',
                    message: '接口异常，请关闭界面重试'
                })
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
