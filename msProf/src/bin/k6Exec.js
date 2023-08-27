import http from 'k6/http';
import { sleep } from 'k6';
import { SharedArray } from 'k6/data'

const baseUrl = 'http://39.104.62.233:32677'      // 被测系统基础地址
const microsvcDiagnoserTockenPath = "http://127.0.0.1:8347/collect/tockenRecord"

// 读取测试用例
const testList = new SharedArray('some name', function () {
    const requests = open(__ENV.testSetsPath + __ENV.testSetName);
    const testLists = requests.split("========\n")
    const testList = testLists[parseInt(__ENV.testId) + 1].split('\n')
    testList.pop()
    return testList;
});

// 执行参数
export let options = {
    vus: __ENV.vus, // 虚拟用户数
    duration: __ENV.durationParam, // 默认测试持续时间
};

// 测试主程序
export default function () {
    const testSetId = __ENV.testSetId;
    const testId = __ENV.testId;
    const genId = __ENV.testSetName.split('-')[1]

    // 请求循环发送
    for (let test of testList) {
        // 读取当前请求
        const testJson = JSON.parse(test)
        const headers = testJson['headers'] || {}
        const argument = JSON.stringify(testJson['argument'])
        const path = baseUrl + testJson['path']
        const method = testJson['method']
        const argInUrl = testJson['argInUrl']

        const headerTmp = { 'tag': 'MsProf', 'my_token': genId, 'Task_ID': testSetId, 'Test_ID': testId, 'Content-Type': 'application/json' }
        
        for (let key in headerTmp) {
            headers[key] = headerTmp[key];
        }

        const options = {
            headers: headers
        };

        // post和get请求
        if (method == 'POST') {
            const res = http.post(path, argument, options);
            const resJson = JSON.parse(res.body)
            if (resJson['status'] == 1 && resJson['msg'] == 'login success' ){
                console.log('token: ', resJson['data']['token'])
                http.post(microsvcDiagnoserTockenPath, JSON.stringify({'data': resJson['data']['token']}))
            }
        } else {
            const resJson = http.get(path, options);
        }

    }

    sleep(1); // 等待 1 秒
}

// 环境：
// K6
// 启动命令
// k6 run  --env durationParam=15s --env vue=10 --env testSetsPath=D:\\MicrosvcDiagnose\\microsvcDiagnoser\\testsets\\ --env testSetName=testset-0-1.test --env testSetId=2 --env testId=1 k6Exec.js