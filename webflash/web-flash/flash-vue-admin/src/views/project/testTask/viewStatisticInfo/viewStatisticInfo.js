/* eslint-disable handle-callback-err */
import { getInterfaceData } from '@/api/project/testTask'
import { resultCheck, getSwaggerDepData } from '@/api/project/testCase'
import JsonViewer from 'vue-json-viewer'
import { flamegraph } from 'd3-flame-graph'
import { Bar, RangeBar } from '@antv/g2plot'
// import echarts from 'echarts'
import * as d3 from 'd3'
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
            wallTime: {},
            flameGraphData:
                JSON.parse('{"children":[{"children":[{"children":[{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":2}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":3},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":2}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":3},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/92708982-77af-4318-be25-57ccb0ff69ad","value":1},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1},{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/92708982-77af-4318-be25-57ccb0ff69ad","value":5},{"children":[],"name":"ts-price-1.0: /api/v1/priceservice/prices/92708982-77af-4318-be25-57ccb0ff69ad/GaoTieOne","value":2},{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1},{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/travel","value":18}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo","value":20},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":3},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":2}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":3}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":4},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":3},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":3}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":3},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/92708982-77af-4318-be25-57ccb0ff69ad","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1234","value":4},{"children":[],"name":"ts-order-1.0: /api/v1/orderservice/order/tickets","value":1},{"children":[{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/GaoTieOne","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/train_types/G1234","value":2},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":1}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":13},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/aefcef3f-3f42-46e8-afd7-6cb2a928bd3d","value":2},{"children":[],"name":"ts-order-1.0: /api/v1/orderservice/order/Thu%20Mar%2002%2008:00:00%20CST%202023/G1235","value":2},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":2}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":4},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/aefcef3f-3f42-46e8-afd7-6cb2a928bd3d","value":2}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1235","value":3},{"children":[],"name":"ts-order-1.0: /api/v1/orderservice/order/tickets","value":1},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":1}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":13},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":2}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":3},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/aefcef3f-3f42-46e8-afd7-6cb2a928bd3d","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1235","value":2},{"children":[],"name":"ts-order-1.0: /api/v1/orderservice/order/tickets","value":1},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":2}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":12},{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/GaoTieOne","value":1},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/a3f256c1-0e43-4f7d-9c21-121bf258101f","value":1},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1},{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/a3f256c1-0e43-4f7d-9c21-121bf258101f","value":1},{"children":[],"name":"ts-price-1.0: /api/v1/priceservice/prices/a3f256c1-0e43-4f7d-9c21-121bf258101f/GaoTieOne","value":2},{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/travel","value":13}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo","value":14},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":3},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":3}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":3},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/a3f256c1-0e43-4f7d-9c21-121bf258101f","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1236","value":3},{"children":[],"name":"ts-order-1.0: /api/v1/orderservice/order/tickets","value":1},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":2}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":16},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":4},{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/GaoTieOne","value":2},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/f3d4d4ef-693b-4456-8eed-59c0d717dd08","value":2}],"name":"ts-travel-1.0: /api/v1/travelservice/trips/left","value":219},{"children":[{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":4},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/0b23bd3e-876a-4af3-b920-c50a90c90b04","value":1},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/9fc9c261-3263-4bfa-82f8-bb44e06b2f52","value":2},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/d693a2c5-ef87-4a3c-bef8-600b43f62c68","value":1},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1},{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/ZhiDa","value":1},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/d693a2c5-ef87-4a3c-bef8-600b43f62c68","value":1},{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1},{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/travel","value":13}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo","value":18},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":3}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":4},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":3},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/d693a2c5-ef87-4a3c-bef8-600b43f62c68","value":1}],"name":"ts-travel2-1.0: /api/v1/travel2service/routes/Z1236","value":3},{"children":[],"name":"ts-order-other-1.0: /api/v1/orderOtherService/orderOther/tickets","value":1},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":2}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":13},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":3}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":3},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":6}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":6},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/d693a2c5-ef87-4a3c-bef8-600b43f62c68","value":1}],"name":"ts-travel2-1.0: /api/v1/travel2service/routes/Z1236","value":4},{"children":[],"name":"ts-order-other-1.0: /api/v1/orderOtherService/orderOther/tickets","value":1},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":2}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":13},{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/ZhiDa","value":1},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/20eb7122-3a11-423f-b10a-be0dc5bce7db","value":1},{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/1367db1f-461e-4ab7-87ad-2bcc05fd9cb7","value":1}],"name":"ts-travel2-1.0: /api/v1/travel2service/trips/left","value":97},{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/92708982-77af-4318-be25-57ccb0ff69ad","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1234","value":3},{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/aefcef3f-3f42-46e8-afd7-6cb2a928bd3d","value":2}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1235","value":3},{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/a3f256c1-0e43-4f7d-9c21-121bf258101f","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1236","value":3}],"name":"ts-route-plan-1.0: /api/v1/routeplanservice/routePlan/cheapestRoute","value":336},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":3}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":4},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/92708982-77af-4318-be25-57ccb0ff69ad","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1234","value":3},{"children":[],"name":"ts-order-1.0: /api/v1/orderservice/order/tickets","value":1},{"children":[{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/GaoTieOne","value":2}],"name":"ts-travel-1.0: /api/v1/travelservice/train_types/G1234","value":3},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":1}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":14},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":4},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":3}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":5},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/92708982-77af-4318-be25-57ccb0ff69ad","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1234","value":3},{"children":[],"name":"ts-order-1.0: /api/v1/orderservice/order/tickets","value":2},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":2}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":13},{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/namelist","value":2},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":2}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":3},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/d693a2c5-ef87-4a3c-bef8-600b43f62c68","value":1}],"name":"ts-travel2-1.0: /api/v1/travel2service/routes/Z1236","value":3},{"children":[{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/ZhiDa","value":2}],"name":"ts-travel2-1.0: /api/v1/travel2service/train_types/Z1236","value":4},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":2}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":15},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":3},{"children":[{"children":[],"name":"ts-order-other-1.0: /api/v1/orderOtherService/orderOther/tickets","value":2},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":1}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":16},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":3}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":4},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/aefcef3f-3f42-46e8-afd7-6cb2a928bd3d","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1235","value":3},{"children":[],"name":"ts-order-1.0: /api/v1/orderservice/order/tickets","value":1},{"children":[{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/GaoTieOne","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/train_types/G1235","value":2},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":1}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":14},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":0}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":4},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/aefcef3f-3f42-46e8-afd7-6cb2a928bd3d","value":2}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1235","value":3},{"children":[],"name":"ts-order-1.0: /api/v1/orderservice/order/tickets","value":1},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":2}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":14},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Nan%20Jing","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Nan%20Jing","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Nan%20Jing","value":4},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":3},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/a3f256c1-0e43-4f7d-9c21-121bf258101f","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1236","value":3},{"children":[{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/GaoTieOne","value":1}],"name":"ts-travel-1.0: /api/v1/travelservice/train_types/G1236","value":3},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":2}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":13},{"children":[{"children":[{"children":[],"name":"ts-station-1.0: /api/v1/stationservice/stations/id/Shang%20Hai","value":1}],"name":"ts-basic-1.0: /api/v1/basicservice/basic/Shang%20Hai","value":2}],"name":"ts-ticketinfo-1.0: /api/v1/ticketinfoservice/ticketinfo/Shang%20Hai","value":3},{"children":[{"children":[{"children":[],"name":"ts-route-1.0: /api/v1/routeservice/routes/a3f256c1-0e43-4f7d-9c21-121bf258101f","value":2}],"name":"ts-travel-1.0: /api/v1/travelservice/routes/G1236","value":6},{"children":[{"children":[],"name":"ts-train-1.0: /api/v1/trainservice/trains/GaoTieOne","value":2}],"name":"ts-travel-1.0: /api/v1/travelservice/train_types/G1236","value":3},{"children":[],"name":"ts-config-1.0: /api/v1/configservice/configs/DirectTicketAllocationProportion","value":1}],"name":"ts-seat-1.0: /api/v1/seatservice/seats/left_tickets","value":16}],"name":"ts-travel-plan-1.0: /api/v1/travelplanservice/travelPlan/cheapest","value":541}'),
            resList: {},
            resProdConsDep: {},
            line_data: {
                'xAxis': [16, 17, 18, 19, 20, 21, 22, 37, 40, 44, 49],
                'seriesData': ['0.06', '0.28', '0.11', '0.11', '0.06', '0.06', '0.06', '0.11', '0.06', '0.06', '0.06']
            },
            API_data: {
                'xAxis': ['9:54:42 114', '9:54:36 953', '9:52:30 14', '9:54:21 77', '9:53:0 834', '9:54:26 234', '9:54:21 30', '9:54:42 40', '9:53:6 120', '9:54:5 20', '9:54:5 31', '9:54:26 276', '9:54:37 19', '9:54:10 196', '9:54:10 224'],
                'seriesData': [9, 10, 14, 4, 16, 5, 12, 4, 4, 3, 5, 4, 12, 4, 3]
            },
            bar_data_src: [
                {
                    type: '分类一', values: [1, 99], child: [
                        {
                            type: '分类二', values: [2, 50], child: [
                                {
                                    type: '分类三',
                                    values: [3, 45],
                                    child: [
                                        {
                                            type: '分类四',
                                            values: [4, 20],
                                            child: [
                                                {
                                                    type: '分类五', values: [5, 15], child: []
                                                },
                                                {
                                                    type: '分类六', values: [16, 19], child: []
                                                }
                                            ]
                                        }, {
                                            type: '分类七', values: [21, 40], child: [
                                                {
                                                    type: '分类八', values: [22, 30], child: []
                                                },
                                                {
                                                    type: '分类0', values: [31, 38], child: []
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            type: '分类1', values: [51, 95], child: [
                                {
                                    type: '分类2', values: [52, 80], child: [{
                                        type: '分类3', values: [53, 76], child: [{
                                            type: '分类4', values: [56, 70], child: [{
                                                type: '分类5', values: [60, 65], child: []
                                            }]
                                        }]
                                    }]
                                },
                                {
                                    type: '分类6', values: [81, 92], child: [{
                                        type: '分类7', values: [85, 90], child: []
                                    }]
                                }
                            ]
                        }
                    ]
                }, {
                    type: '分类一', values: [1, 99], child: [
                        {
                            type: '分类二', values: [2, 50], child: [
                                {
                                    type: '分类三',
                                    values: [3, 45],
                                    child: [
                                        {
                                            type: '分类四',
                                            values: [4, 20],
                                            child: [
                                                {
                                                    type: '分类五', values: [5, 15], child: []
                                                },
                                                {
                                                    type: '分类六', values: [16, 19], child: []
                                                }
                                            ]
                                        }, {
                                            type: '分类七', values: [21, 40], child: [
                                                {
                                                    type: '分类八', values: [22, 30], child: []
                                                },
                                                {
                                                    type: '分类0', values: [31, 38], child: []
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            type: '分类1', values: [51, 95], child: [
                                {
                                    type: '分类2', values: [52, 80], child: [{
                                        type: '分类3', values: [53, 76], child: [{
                                            type: '分类4', values: [56, 70], child: [{
                                                type: '分类5', values: [60, 65], child: []
                                            }]
                                        }]
                                    }]
                                },
                                {
                                    type: '分类6', values: [81, 92], child: [{
                                        type: '分类7', values: [85, 90], child: []
                                    }]
                                }
                            ]
                        }
                    ]
                }
            ],
            bar_data_src_current: [],
            bar_heights: [],
            bar_data_src_color: []
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
    watch: {
        bar_data_src: {
            handler: function(val) {
                // const that = this
                setTimeout(() => {
                    // console.log('bar_data: ', that.bar_data_src)
                    for (let i = 0; i < this.bar_data_src.length; i++) {
                        this.drawBar(i)
                    }
                }, 1)
            },
            deep: true
        }
    },
    methods: {

        init() {
            this.fetchData()
        },

        fetchData() {
            this.listLoading = true
            console.log('this.$route: ', this.$route)
            const taskName = this.$route.query.taskName
            let interfaceName = this.$route.query.interfaceName
            console.log('taskName: ', taskName)
            console.log('interfaceName: ', interfaceName)
            interfaceName = interfaceName.replaceAll('ภ', '/')
            getInterfaceData({ taskName: taskName, interfaceName: interfaceName }).then(response => {
                const data = JSON.parse(response.data)
                this.line_data = JSON.parse(data.line_data)
                this.API_data = JSON.parse(data.API_data)
                this.flameGraphData = JSON.parse(data.flame_graph_data)
                this.bar_data_src = JSON.parse(data.bar_data_src)

                let maxLenYLabel = 0
                for (let i = 0; i < this.bar_data_src.length; i++) {
                    const tmp_data = this.flattenJson(this.bar_data_src[i])
                    this.bar_heights[i] = tmp_data.length
                    tmp_data.forEach(data_item => {
                        if (data_item.type.length > maxLenYLabel) {
                            maxLenYLabel = data_item.type.length
                        }
                    })
                }
                this.fixYLabelLength(this.bar_data_src, maxLenYLabel)
                // this.drawBar(0)
                this.drawFlameGraph('chart')
                this.drawLine()
                this.drawAPI()
                console.log('aaa: ', this.bar_data_src.length)
            }).catch(err => {
                this.$notify.error({
                    title: '错误',
                    message: '接口异常，请关闭界面重试'
                })
            })
        },

        // 修改type长度，以使原点在纵列对齐
        fixYLabelLength(bar_data_src, maxLenYLabel) {
            console.log('maxLenYLabel: ', maxLenYLabel)
            function traverse(node) {
                node.type += '  '
                if (node.type.length < maxLenYLabel) {
                    const tmp = node.type.length
                    for (let i = 0; i < (maxLenYLabel - tmp); i++) {
                        // node.type += ' '
                        node.type = ' ' + node.type
                    }
                }
                if (node.child && node.child.length > 0) {
                    node.child.forEach(child => {
                        traverse(child)
                    })
                }
            }
            bar_data_src.forEach(bat_data_src_tmp => {
                traverse(bat_data_src_tmp)
            })
        },

        drawFlameGraph() {
            const labelWallTime = (d) => {
                return d.data.name
            }

            const renderFlame = (selector, data, labelFunc) => {
                const flameWith = 800
                const flame = flamegraph()
                flame.selfValue(false)
                    .width(flameWith)
                    .height(150)
                    .cellHeight(18)
                    .transitionDuration(750)
                    // .inverted(true)
                    .label(labelFunc)
                    .getName(labelFunc)
                    .onHover(labelFunc)
                    .transitionDuration(750)
                    .minFrameSize(5)
                    .transitionEase(d3.easeCubic)
                    // .sort(true)
                    // //Example to sort in reverse order
                    // //.sort(function(a,b){ return d3.descending(a.name, b.name);})
                    .title('')
                // .onClick(onClick)
                // .differential(false)

                // .onClick()

                d3.select(selector)
                    .datum(data)
                    .call(flame)
            }

            renderFlame(this.$refs.chart, this.flameGraphData, labelWallTime)
        },

        drawLine() {
            const option = {
                title: {
                    text: '请求耗时-占比分布图'
                },
                xAxis: {
                    type: 'category',
                    data: this.line_data.xAxis
                    // data: [1, 2, 3, 4, 6, 7, 8, 9, 10, 13]
                    // data: [1, 2, 15, 17, 18, 19, 32, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 56, 60]

                },
                yAxis: {
                    type: 'value'
                },
                series: [
                    {
                        data: this.line_data.seriesData,
                        // data: ['0.0302', '0.0302', '0.0605', '0.0605', '0.0605', '0.1209', '0.1453', '0.1986', '0.2128', '0.1314', '0.1756', '0.1610', '0.1521', '0.1233', '0.0605', '0.0605', '0.0302', '0.0302', '0.0605', '0.0302', '0.0605', '0.0302', '0.0401', '0.0204'],
                        // data: ['0.4043', '0.1596', '0.0745', '0.0319', '0.0851', '0.1702', '0.0319', '0.0213', '0.0106', '0.0106'],
                        type: 'line',
                        smooth: true
                    }
                ]
            }
            const myChart = this.$echarts.init(document.getElementById('myChart'))
            myChart.setOption(option)
        },

        drawAPI() {
            const options = {
                title: {
                    text: '请求耗时-时间分布图'
                },
                xAxis: {
                    type: 'category',
                    // data: this.options2_data.map(item => item.date)
                    data: this.API_data.xAxis
                },
                yAxis: {
                    type: 'value'
                },
                series: [{
                    // data: this.options2_data.map(item => item.aqi),
                    data: this.API_data.seriesData,
                    type: 'line'
                }],
                visualMap: {
                    top: 50,
                    right: 10,
                    pieces: [],
                    outOfRange: {
                        color: '#999'
                    },
                    showLabel: false,
                    show: false
                }
            }
            const max_series_data = Math.max(...this.API_data.seriesData)
            options.visualMap.pieces = [
                {
                    gt: 0,
                    lte: max_series_data / 5,
                    color: '#93CE07'
                },
                {
                    gt: max_series_data / 5,
                    lte: max_series_data / 5 * 2,
                    color: '#FBDB0F'
                },
                {
                    gt: max_series_data / 5 * 2,
                    lte: max_series_data / 5 * 3,
                    color: '#FC7D02'
                },
                {
                    gt: max_series_data / 5 * 2,
                    lte: max_series_data / 5 * 3,
                    color: '#FD0100'
                },
                {
                    gt: max_series_data / 5 * 3,
                    lte: max_series_data / 5 * 4,
                    color: '#AA069F'
                },
                {
                    gt: max_series_data / 5 * 4,
                    color: '#AC3B2A'
                }
            ]
            const myChart = this.$echarts.init(document.getElementById('chart-container'))
            myChart.setOption(options)
        },

        drawBar(idx) {
            const chartContainerId = 'chartContainer' + idx
            // const container = this.$refs[chartContainerId]
            const container = document.getElementById(chartContainerId)
            this.bar_data_src_color = JSON.parse(JSON.stringify(this.bar_data_src[idx]))
            this.calColors(this.bar_data_src_color)
            // this.calColorsRandom(this.bar_data_src_color, 15)
            console.log('data i: ', idx, this.flattenJson(this.bar_data_src[idx]))
            const colors = this.getJsonColor(this.bar_data_src_color)
            console.log('color: ', JSON.stringify(colors))
            this.bar_data_src_current[idx] = this.bar_data_src[idx]
            const barPlot = new Bar(container, {
                data: this.flattenJson(this.bar_data_src[idx]),
                xField: 'values',
                yField: 'type',
                scale: {
                    x: {
                        // func(v[0]) + '-' + func(v[1])
                        formatter: (v) => this.stampToTime(v[0]) + '-' + this.stampToTime(v[1])
                    }
                },
                meta: {
                    values: {
                        alias: '时间',
                        formatter: (v) => this.stampToTime(v)
                    }
                },
                // tooltip: {
                //     formatter: (datum) => ({ name: '区间', value: this.stampToTime(datum[0]) + '-' + this.stampToTime(datum[1]) })
                // },
                isRange: true,

                color: ({ type }) => {
                    if (colors[type]) {
                        return colors[type]
                    } else {
                        return colors[type.substring(2)] || colors[type.substring(0, type.length - 2) + '  ']
                    }
                },
                forceFit: true,
                // height: 500,

                maxBarWidth: 12,
                minBarWidth: 12,
                barStyle: {
                    stroke: 'black',
                    lineWidth: 0.5,
                    strokeOpacity: 0.7,
                    radius: [3.5, 3.5, 3.5, 3.5]
                },

                xAxis: {
                    line: {
                        style: {
                            stroke: 'black',
                            lineWidth: 1,
                            strokeOpacity: 0.4

                        }
                    },
                    min: this.bar_data_src[idx].values[0] - 1,
                    max: this.bar_data_src[idx].values[1] + 1
                    // range: [0, 1]
                },
                yAxis: {
                    label: {
                        style: {
                            fontSize: 12,
                            fill: '#666',
                            fontWeight: 'bold',
                            fontFamily: 'Consolas'
                        }
                    }
                },
                label: {
                    visible: true,
                    leftStyle: {
                        fill: '#3e5bdb'
                    },
                    rightStyle: {
                        fill: '#dd3121'
                    }
                }

            })

            barPlot.on('element:click', (evt) => {
                const eventData = evt.data
                if (eventData.data) {
                    this.click(barPlot, eventData, idx)
                }
            })

            barPlot.render()
            console.log('draw finish: ', idx)
        },

        click(barPlot, eventData, idx) {
            const data = eventData.data
            const type = data.type
            const values = data.values
            let bar_data_new = []
            let bar_data_src_new = {}
            const bar_data_src_tmp = JSON.parse(JSON.stringify(this.bar_data_src[idx]))
            if (type.startsWith('>-') || type.endsWith('>-')) {
                bar_data_src_new = this.expandBarData(bar_data_src_tmp, type, values)
            } else {
                // 旧版折叠，直接清空child，会导致展开后展开所有子项的问题
                /*
                const bar_data_src_new = this.clearChild(bar_data_src_tmp, type, values)
                const bar_data_new_tmp = this.flattenJson(bar_data_src_new)
                const bar_data_new = this.getIntersection(barPlot.options.data, bar_data_new_tmp)
                this.bar_heights[idx] = bar_data_new
                barPlot.changeData(bar_data_new)
                */
                // 新版折叠， 增加当前数据的存储，展开后不展开已折叠的子项
                bar_data_src_new = this.flodBarData(bar_data_src_tmp, type, values)
            }
            this.bar_data_src_current[idx] = bar_data_src_new
            bar_data_new = this.flattenJsonTillFlod(bar_data_src_new)
            barPlot.changeData(bar_data_new)
            this.bar_heights[idx] = bar_data_new.length
            console.log('高度： ', this.bar_heights[0])
        },

        flattenJson(json) {
            const result = []
            // 递归函数，参数为当前遍历的节点
            function traverse(node) {
                // 将当前节点的信息放入结果数组中
                result.push({
                    type: node.type,
                    values: node.values
                })
                // 如果当前节点有子节点，则对每个子节点递归调用 traverse 函数
                if (node.child && node.child.length > 0) {
                    node.child.forEach(child => {
                        traverse(child)
                    })
                }
            }
            // 从最顶层节点开始遍历
            traverse(json)
            return result
        },

        flattenJsonTillFlod(json) {
            const result = []
            // 递归函数，参数为当前遍历的节点
            function traverse(node) {
                // 将当前节点的信息放入结果数组中
                result.push({
                    type: node.type,
                    values: node.values
                })
                // 如果当前节点有子节点并且未折叠，则对每个子节点递归调用 traverse 函数
                if ((!node.type.startsWith('>-')) && (!node.type.endsWith('>-')) && node.child && node.child.length > 0) {
                    node.child.forEach(child => {
                        traverse(child)
                    })
                }
            }
            // 从最顶层节点开始遍历
            traverse(json)
            this.height = result.length
            return result
        },

        stampToTime(stamp) {
            // 获得系统的时间，单位为毫秒,转换为妙
            const totalMilliSeconds = stamp
            const milliSeconds = Math.round(stamp % 1000)
            const totalSeconds = Math.round(totalMilliSeconds / 1000)

            // 求出现在的秒
            const currentSecond = Math.round(totalSeconds % 60)

            // 求出现在的分
            const totalMinutes = Math.round(totalSeconds / 60)
            const currentMinute = Math.round(totalMinutes % 60)

            // 求出现在的小时
            const totalHour = Math.round(totalMinutes / 60)
            const currentHour = Math.round(totalHour % 24)

            // 显示时间
            // System.out.println("总毫秒为： " + totalMilliSeconds);
            return currentHour + ':' + currentMinute + ':' + currentSecond + ' ' + milliSeconds
        },

        flodBarData(bar_data_src, type, values) {
            if (bar_data_src.type === type && bar_data_src.values[0] === values[0] && bar_data_src.values[1] === values[1]) {
                // bar_data_src.type = '>-' + type
                // bar_data_src.type = bar_data_src.type.substring(0, bar_data_src.type.length - 2)
                bar_data_src.type = bar_data_src.type.substring(0, bar_data_src.type.length - 2) + '>-'
            } else {
                for (const child of bar_data_src.child) {
                    this.flodBarData(child, type, values)
                }
            }
            return bar_data_src
        },

        expandBarData(bar_data_src, type, values) {
            if (bar_data_src.type === type && bar_data_src.values[0] === values[0] && bar_data_src.values[1] === values[1]) {
                bar_data_src.type = type.substring(0, type.length - 2)
                bar_data_src.type += '  '
            } else {
                for (const child of bar_data_src.child) {
                    this.expandBarData(child, type, values)
                }
            }
            return bar_data_src
        },

        // 折叠目标行后的数据 ∩ 折叠前的数据 = 需要展示的数据
        getIntersection(data1, data2) {
            const result = []
            for (const data2_tmp of data1) {
                for (const data1_tmp of data2) {
                    if ((data2_tmp.type === data1_tmp.type || data2_tmp.type === data1_tmp.type.substring(2)) && data2_tmp.values[0] === data1_tmp.values[0] && data2_tmp.values[1] === data1_tmp.values[1]) {
                        result.push(data1_tmp)
                        break
                    }
                }
            }
            return result
        },

        clearChild(data, type, values) {
            if (data.type === type && data.values[0] === values[0] && data.values[1] === values[1]) {
                data.child = []
                data.type = '>-' + type
            } else {
                for (const child of data.child) {
                    this.clearChild(child, type, values)
                }
            }
            return data
        },

        getJsonColor(json) {
            const result = []
            // 递归函数，参数为当前遍历的节点
            function traverse(node) {
                // 将当前节点的信息放入结果数组中
                result.push({
                    type: node.type,
                    color: node.color
                })
                // 如果当前节点有子节点，则对每个子节点递归调用 traverse 函数
                if (node.child && node.child.length > 0) {
                    node.child.forEach(child => {
                        traverse(child)
                    })
                }
            }
            // 从最顶层节点开始遍历
            traverse(json)
            const result_list = result.reduce((acc, cur) => {
                acc[cur.type] = cur.color
                return acc
            }, {})
            return result_list
        },

        calColors(input, depth) {
            if (!depth) {
                depth = 1
            }
            var red = Math.max(255 - 20 * depth, 1)// 基础颜色为(85, 105, 246)，深度越深，红色通道加 10
            var green = Math.max(165 - 20 * depth, 1)
            var blue = Math.min(0 + 20 * depth, 254)
            input.color = '#' + red.toString(16) + green.toString(16) + blue.toString(16) // 将 RGB 颜色值转换成 16 进制字符串
            if (input.child) {
                for (var i = 0; i < input.child.length; i++) {
                    this.calColors(input.child[i], depth + 1) // 递归处理 child 属性，深度加 1
                }
            }
        },

        // 随机，不好用
        calColorsRandom(input, depth, randomness) {
            if (!depth) {
                depth = 1
            }
            var red = Math.max(255 - 20 * depth + this.getRandomInt(-randomness, randomness), 1)// 基础颜色为(85, 105, 246)，深度越深，红色通道加 10
            var green = Math.max(165 - 20 * depth + this.getRandomInt(-randomness, randomness), 1)
            var blue = Math.min(0 + 20 * depth + this.getRandomInt(-randomness, randomness), 254)
            input.color = '#' + red.toString(16) + green.toString(16) + blue.toString(16) // 将 RGB 颜色值转换成 16 进制字符串
            if (input.child) {
                for (var i = 0; i < input.child.length; i++) {
                    this.calColorsRandom(input.child[i], depth + 1) // 递归处理 child 属性，深度加 1
                }
            }
        },

        getRandomInt(min, max) {
            return Math.floor(Math.random() * (max - min + 1)) + min
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
