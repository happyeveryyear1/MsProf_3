import http from 'k6/http';
import { sleep } from 'k6';
import { SharedArray } from 'k6/data'

const baseUrl = 'http://39.104.62.233:32677'      // 被测系统基础地址

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

    // 请求循环发送
    for (let test of testList) {
        // 读取当前请求
        const testJson = JSON.parse(test)
        const headers = testJson['headers'] || {}
        const argument = JSON.stringify(testJson['argument'])
        const path = baseUrl + testJson['path']
        const method = testJson['method']
        
        // header生成，注意tag为工具类别标识
        const headerTmp = { 'tag': 'MsProf', 'Content-Type': 'application/json' }
        
        for (let key in headerTmp) {
            headers[key] = headerTmp[key];
        }

        const options = {
            headers: headers
        };

        // post和get请求
        if (method == 'POST') {
            const res = http.post(path, argument, options);
            console.log('post res: ', res.body)
        } else {
            const res = http.get(path, options);
            console.log('get res', res.body)
        }

    }

    sleep(1); // 等待 1 秒
}

// 环境：
// K6
// 启动命令
// k6 run  --env durationParam=15s --env vue=10 --env testSetsPath=D:\\MicrosvcDiagnose\\microsvcDiagnoser\\testsets\\ --env testSetName=testset-0-1.test --env testId=1 k6Exec.js