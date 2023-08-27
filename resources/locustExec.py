from wsgiref.headers import Headers
from click import argument
from locust import HttpUser, TaskSet, task,  events
import requests
import json
import logging


# 请求列表
test_list = []
# 添加header，对应
g_gen_id = -1               # 代数  my_token
g_testset_id = -1           # 测试套件ID Task_ID
g_test_id = -1              # 测试用例ID Test_ID
microsvcDiagnoserTockenPath = "http://127.0.0.1:8347/collect/tockenRecord"

@events.init_command_line_parser.add_listener
def _(parser):
    parser.add_argument("--testset-name", type=str, default="", help="testset file name")
    parser.add_argument("--testsets-path", type=str, default="", help="testset dir path")
    parser.add_argument("--testset-id", type=int, default="", help="test index")        # 第idx个测试套件
    parser.add_argument("--test-id", type=int, default="", help="test index")           # 第idx个测试用例

def read_test(testset_name: str, testsets_path: str, testset_id: int, test_id: int):
    global g_gen_id
    global g_testset_id
    global g_test_id
    f = None
    try:
        f = open(testsets_path + testset_name)
        test_set = f.read()
        test_lists = test_set.split("========\n")
        test_list = test_lists[test_id+1].split('\n')
        g_gen_id = str(testset_name.split('-')[1])
        g_testset_id = str(testset_id)
        g_test_id = str(test_id)
    # print('g_gen_id:', g_gen_id, 'g_testset_id:', g_testset_id, 'g_test_id:', g_test_id, test_list)
    finally:
        if f:
            f.close()
    return test_list[0:-1]
    


class WebsiteUser(HttpUser):


    @events.test_start.add_listener
    def on_test_start(environment, **kwargs):
        global test_list
        test_list = read_test(environment.parsed_options.testset_name, environment.parsed_options.testsets_path, environment.parsed_options.testset_id, environment.parsed_options.test_id)
        

    @task
    def about(self):
        for test in test_list[0:-1]:
            test_json = json.loads(test)
            headers = test_json['headers']
            argument = test_json['argument']
            path = test_json['path']
            method = test_json['method']
            arg_in_url = test_json['argInUrl']
            
            if arg_in_url == 'true':
                for arg_name in argument.keys():
                    path = path.replace(arg_name, str(argument[arg_name])) 
            
            
            header = {'Tool_Type': 'MsProf', 'my_token': g_gen_id, 'Task_ID': g_testset_id, 'Test_ID': g_test_id}
            # header = {'MsProf_flag': 'MsProf', 'my_token': g_gen_id, 'Task_ID': g_testset_id, 'Test_ID': g_test_id}
            header.update(headers)
            # print(header)

            
            # header = {'Task_ID': '1111111111111111', 'Test_ID': '22222222222'}
            # print('path: ', path)
            if method == 'POST':
                postResult = self.client.post(path, json=argument, headers = header)
                try:
                    logging.info('postResult.text: {}'.format(postResult.text))
                    response = json.loads(postResult.text)
                    logging.info('response: {}'.format(response))
                    # 记录token
                    if response['status'] == 1 and response['msg'] == 'login success' :
                        logging.info('token:: {}'.format(response['data']['token']))
                        requests.post(microsvcDiagnoserTockenPath, json={'data': response['data']['token']}, headers = header)
                except json.JSONDecodeError:
                    logging.info("json.loads解析空字符串异常")
            else:
                self.client.get(path, headers = header)
        # pass
        
    
    host = "http://133.133.135.182:32677"
    min_wait = 1000
    max_wait = 5000


