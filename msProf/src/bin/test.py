import json
import requests

test_dir_path = "D:\\MicrosvcDiagnose\\microsvcDiagnoser\\testsets\\"
test_name = "testset 0-1.test"
test_idx = 1


f = open(test_dir_path + test_name)
test_set = f.read()
test_lists = test_set.split("========\n")
print(test_lists)
test_list = test_lists[test_idx].split('\n')
for test in test_list[0:-1]:
    # print('test: ', test)
    test_json = json.loads(test)
    argument = test_json['argument']
    path = test_json['path']
    method = test_json['method']
    arg_in_url = test_json['argInUrl']
    
    if arg_in_url == 'true':
        print('path before: ', path)
        print('argument: ', argument)
        print('arg_in_url: ', arg_in_url)
        for arg_name in argument.keys():
            path = path.replace(arg_name, str(argument[arg_name]))
        print('path: ', path)
    if method == 'PSOT':
        r = requests.post('http://127.0.0.1' + path, argument)
    else:
        r = requests.get('http://127.0.0.1' + path)